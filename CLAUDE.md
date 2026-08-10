# CLAUDE.md - Critical Flight Display Minecraft Mod

## Project Overview

Critical Flight Display is a client-side Minecraft mod that draws a HUD overlay
while the player is Elytra-flying (`isFallFlying()`). It shows:

- **Pitch readout** - numeric pitch (inverted sign from the raw camera pitch).
- **Horizon/pitch ladder** - a vertical line down the left third and right
  third of the screen, with horizontal tick marks that scroll vertically as
  pitch changes (an artificial-horizon-style ladder, like an aircraft attitude
  indicator).
- **Speed readout** - a simple derived speed value, sampled roughly every 10
  world-ticks from the player's position delta.

It has no config screen, no keybinds, and no server component - purely a
client HUD. It renders only while flying with an Elytra.

## Supported Platforms (target matrix)

Loader coverage is **mandatory**, not best-effort: every Minecraft version
target must build for every loader viable on that version - Fabric +
NeoForge for 1.20.5+, Fabric + Forge for ≤1.20.4. A cell is only skipped when
the loader itself cannot exist for that version (see PLAN.md's "exact
blockers" list), never by choice.

| Minecraft Version | Fabric | NeoForge | Forge |
|--------------------|--------|----------|-------|
| 1.21.4             | ✅      | ✅        | ❌ (Forge's `IGuiOverlay` API is gone by 1.20.5+, and 1.20.5+ is NeoForge's lane) |
| 1.20.1             | ✅      | ❌        | ✅ (≤1.20.4 is Forge's lane) |
| 1.19.4             | ✅      | ❌        | ✅ (≤1.20.4 is Forge's lane) |
| 1.18.2             | ✅      | ❌        | ✅ (≤1.20.4 is Forge's lane) |

**Quilt**: not a separate build target. Quilt runs Fabric jars natively, so
every Fabric jar above is Quilt-compatible as-is - nothing extra to build.

**One-jar-per-version (Forgix)**: investigated the
[Forgix](https://github.com/PacifistMC/Forgix) Gradle plugin to merge each
version's per-loader jars into a single all-loader jar. It's actively
maintained (release 2.0.0, 2026-08-02) and works by repackaging already-built
jars so each loader only loads its own package - structurally independent of
Stonecutter/Stonecraft. It needs manual `inputJar` wiring per version cell
because Forgix's auto-detection expects subprojects named `fabric`/`forge`/
`neoforge`, not Stonecutter's `<version>-<loader>` naming. See PLAN.md
("One-jar-per-version investigation: Forgix" / "Appendix: Forgix outcome")
for the full writeup and attempt result.

This mirrors the sibling `critical-orientation` mod's matrix exactly. See
`PLAN.md` for why the *actual* newest stable Minecraft version (`26.2`, per
`https://meta.fabricmc.net/v2/versions/game`) is documented but **not** part
of the buildable matrix yet — the mapping/toolchain ecosystem this project
depends on (Stonecraft → Architectury Loom) does not yet support Minecraft's
post-obfuscation versioning line (`26.1`+).

## Tech Stack

- **Language**: Java 21 (toolchain via Gradle; source/target level pinned per
  Minecraft version's own requirement where lower JDKs were originally used -
  Gradle toolchains auto-provision, nothing installed system-wide)
- **Build System**: Gradle 9.7.0 with Stonecutter `0.9.+` + the
  `gg.meza.stonecraft` `1.12.+` plugin (current confirmed-working versions,
  verified live against `meza/Stonecraft`'s own source/e2e fixtures - not the
  older `0.5`/`1.9.+` pair originally read from `critical-orientation` before
  it was independently bumped to this same current pair)
- **Multi-Loader**: Stonecraft (Architectury + Stonecutter combined)
- **Rendering hooks**: modernized away from the original 1.16 Mixin into
  `InGameHud` - see "Architecture" below.

## Repository Structure

```
critical-flight-details/
├── .github/workflows/
│   └── build.yml                 # CI: chiseledBuild + test across the full matrix on every push/PR
├── build.gradle.kts              # Stonecraft build configuration (central script)
├── settings.gradle.kts           # Stonecutter version/loader matrix
├── stonecutter.gradle.kts        # Active version pointer
├── gradle.properties             # Mod metadata
├── LICENSE                       # CC0-1.0 license
├── README.md                     # User-facing documentation
├── CLAUDE.md                     # AI assistant guidance (this file)
├── PLAN.md                       # Modernization roadmap + status checklist
├── versions/                     # Generated per-version/loader subprojects (gitignored)
└── src/
    ├── main/java/net/critical/flight_display/
    │   ├── FlightDisplayClient.java   # Loader-specific entry point + HUD-event registration
    │   ├── FlightHudMath.java         # Pure, loader-agnostic HUD layout/speed math
    │   └── hud/FlightHudRenderer.java # Loader/version-conditional draw code
    ├── main/resources/
    │   ├── fabric.mod.json
    │   ├── META-INF/
    │   │   ├── mods.toml              # Forge metadata (1.18.2/1.19.4/1.20.1)
    │   │   └── neoforge.mods.toml     # NeoForge metadata (1.21.4)
    │   └── assets/flight_display/
    │       ├── lang/en_us.json
    │       └── icon.png
    └── test/java/net/critical/flight_display/
        └── FlightHudMathTest.java     # Unit tests for the pure math
```

This mod is client-only (no server logic at all), so **everything lives under
`src/main`, including loader/render code** - there is no `src/client` split.
Stonecraft/Loom only wires a separate `src/client/java` source set into the
compile task when the build script opts in via
`loom.splitEnvironmentSourceSets()`; this project's `build.gradle.kts` never
calls that, so a `src/client/java` directory would be silently skipped
(`NO-SOURCE`) rather than compiled. `src/main`'s compile classpath does carry
the full Minecraft dependency here for exactly that reason. (A sibling mod
using the split-source-set convention may see the opposite: `src/main` with
no Minecraft dependency, and client code required to live in
`src/client/java`. Both are valid Stonecraft configurations - check whether
the project's build script calls `splitEnvironmentSourceSets()` before
assuming either layout.)

## Architecture

### Original (1.16.1-era) design

- `FlightDisplay` (`ModInitializer`) - just logged a startup message.
- `GameInfoMixin` - a `@Mixin` into `InGameHud` that injected into its
  constructor and into `render()` at a `hudHidden` field read, then called
  `FlightDisplayHud.draw()` if the player was fall-flying.
- `FlightDisplayHud.draw()` - drew everything with a raw `MatrixStack`,
  `TextRenderer.draw(...)`, and immediate-mode `Tessellator`/`BufferBuilder`/
  `GL11.GL_LINES` calls.

This whole approach breaks going forward for two independent reasons:
1. Mixin-injecting into `InGameHud` internals is fragile across MC versions
   (field/method signatures and even the class name change: `InGameHud` on
   Fabric/Yarn vs `Gui`/`ForgeGui` on Forge/NeoForge/Mojang-mapped).
2. Raw `GL11`/`Tessellator` immediate-mode drawing was removed from Minecraft
   well before 1.21; `DrawContext`/`GuiGraphics` replaced it starting in 1.20.

### Modernized design

The Mixin is gone entirely. HUD rendering now hooks the **official**,
supported per-loader HUD render event instead of reaching into `InGameHud`:

- **Fabric** (all 4 targeted versions): Fabric API's
  `net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback`. Deprecated
  in newer fabric-api releases in favor of `HudElementRegistry`, but still
  functional through 1.21.4 and lets one code path cover 1.18.2-1.21.4
  without an extra HUD-layer-registration branch.
- **NeoForge** (1.21.4 only): `net.neoforged.neoforge.client.event.RenderGuiEvent.Post`
  on `NeoForge.EVENT_BUS`.
- **Forge** - the registration API differs *within* the Forge lane itself,
  not just at the Forge/NeoForge boundary:
  - **1.18.2**: `OverlayRegistry.registerOverlayTop(...)` called from
    `FMLClientSetupEvent`, drawing against `ForgeIngameGui` + `PoseStack`.
  - **1.19.4 and 1.20.1**: `RegisterGuiOverlaysEvent` +
    `event.registerAboveAll(...)` (the `IGuiOverlay`-shaped callback),
    drawing against `ForgeGui`; only the draw-call signature changes between
    them (`PoseStack` for 1.19.4, `GuiGraphics` for 1.20.1 - see the
    `MatrixStack`/`PoseStack` → `GuiGraphics` row below).
  - Both `OverlayRegistry` and `RegisterGuiOverlaysEvent`/`IGuiOverlay` are
    gone by Forge 1.20.5+, which is exactly why the Forge targets stop at
    1.20.1 - see PLAN.md.

All three loaders use Mojang's official mappings end-to-end (Stonecraft's
default namespace here, matching `critical-orientation`) - so Fabric,
Forge, and NeoForge all resolve to the same Mojmap class names
(`GuiGraphics`, `LocalPlayer`, `PoseStack`, etc.); there is no separate
Yarn-mapped `DrawContext`/`InGameHud` naming in this codebase.

Drawing primitives:
- `<1.20` (Fabric 1.18.2/1.19.4, Forge 1.18.2/1.19.4): `PoseStack` +
  `client.font.draw(poseStack, text, x, y, color)` + `GuiComponent.fill(poseStack,
  x1, y1, x2, y2, color)` for the ladder lines (both the horizon rails and
  tick marks are pure horizontal/vertical segments, so a 1px-thick filled
  rect stands in for the old `GL11.GL_LINES` draw with identical visual
  result).
- `>=1.20` (Fabric 1.20.1/1.21.4, Forge 1.20.1, NeoForge 1.21.4):
  `GuiGraphics` on every loader - `guiGraphics.drawString(...)` and
  `guiGraphics.fill(x1, y1, x2, y2, color)`. Fabric 1.21.4's callback also
  receives a `DeltaTracker` instead of a raw `float tickDelta`, but the mod
  doesn't use partial-tick interpolation so this is a signature-only change.

All layout and speed math (hash spacing, pitch offset, distance-sampling
speed calc) lives in `FlightHudMath`, which takes only primitives
(`width`, `height`, `pitch`, player coordinates, world time) and returns
plain values/records. It has **no Minecraft-class dependency**, is shared
unmodified across every loader/version subproject, and is unit-tested in
`FlightHudMathTest`.

Note: the original speed formula
`(dx² + dy² + dz²) * 0.5` (no square root) is preserved as-is for a faithful
port - it is not true Euclidean distance, but changing player-visible
gameplay-adjacent numeric behavior is out of scope for a compile-target
modernization pass. Flagged here so nobody "fixes" it by accident later.

## Stonecutter Preprocessor

Same mechanism as `critical-orientation`. Real excerpts from
`FlightDisplayClient.java` / `FlightHudRenderer.java` (loader conditions can
be combined with a version bound, e.g. `forge && <1.19`):

```java
//? if fabric {
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
//?} elif neoforge {
/*import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
*///?} elif forge && <1.19 {
/*import net.minecraftforge.client.gui.OverlayRegistry;
*///?} elif forge {
/*import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
*///?}
```

```java
//? if fabric && <1.20 {
/*client.font.draw(poseStack, text, x, y, color);*/
//?} elif forge && <1.20 {
/*client.font.draw(poseStack, text, x, y, color);*/
//?} else {
guiGraphics.drawString(client.font, text, x, y, color, false);
//?}
```

- Active version set in `stonecutter.gradle.kts` - never hand-edit the
  `/* [SC] DO NOT EDIT */` marker; switch it with
  `./gradlew "Set active project to <mc>-<loader>"` (e.g.
  `./gradlew "Set active project to 1.20.1-forge"`) if the active node ever
  needs to change.
- Comments are (un)commented per-subproject during `chiseledBuild`.

## Build Commands

```bash
# Build all versions and loaders
./gradlew chiseledBuild

# Build one version/loader
./gradlew :1.21.4-fabric:build

# Run tests (loader-agnostic math only)
./gradlew test

# Run the Minecraft client for the active version (for manual smoke-testing only;
# NOT required to consider a version "done")
./gradlew runClient
```

## Version Configuration

Managed in `gradle.properties`:
- `mod.id`: `flight_display`
- `mod.name`: `Critical Flight Display`
- `mod.version`: current release version
- `mod.group`: `net.critical`

## Code Conventions

- Package namespace: `net.critical.flight_display` (kept from the original
  mod rather than renamed, to preserve mod-id/package identity across the
  version history).
- Mod ID: `flight_display`
- Shared logic in `FlightHudMath` (loader-agnostic, unit-testable).
- Use Stonecutter `//? if fabric|neoforge|forge` and `//? if >=1.20` for
  loader/version-specific code.
- No Mixins. If a future feature genuinely needs a Mixin, prefer the
  smallest possible `@Mixin` surface and document why the official event API
  couldn't do it.

## Porting Notes / Known API Break Points

| Change | Versions affected | Notes |
|---|---|---|
| `Entity.level()` doesn't exist yet; only `getLevel()` | <1.20 (1.18.2, 1.19.4) | Confirmed directly against the layered mappings' `mappings.tiny` - `level()` is a real 1.20+ Mojmap addition, not a Yarn-naming artifact |
| `PoseStack`/`client.font.draw(...)` → `GuiGraphics`/`guiGraphics.drawString(...)` | 1.19.4 → 1.20.1 (both loaders) | Signature-only change; same visual result |
| `InGameHud` Mixin → `HudRenderCallback`/overlay events | all | Mixin removed outright in the port |
| Immediate-mode `GL11`/`Tessellator` line drawing removed | pre-1.20 code only | Replaced with `fill()` 1px rects |
| Forge `OverlayRegistry` → `RegisterGuiOverlaysEvent`/`IGuiOverlay` | 1.18.2 → 1.19.4 | Not a single uniform Forge API across the whole Forge lane - 1.18.2 alone still uses the older `OverlayRegistry` |
| Forge `RegisterGuiOverlaysEvent`/`IGuiOverlay` removed entirely | Forge 1.20.5+ | Why Forge targets stop at 1.20.1 |
| NeoForge fork from Forge | 1.20.2+ | Why NeoForge only appears at 1.21.4, mirroring `critical-orientation` |
| Yarn mappings frozen at 1.21.11 | 26.1+ | Minecraft shipped fully unobfuscated starting 26.1; Fabric stopped maintaining Yarn. See PLAN.md "Newest stable Minecraft version" section. |
| `GuiGraphics.extractRenderState(...)`/`GuiRenderState` replaces `Gui.render(...)`; `KeyMapping.Category` replaces `String` category | 26.2+ | Not yet attempted here - see PLAN.md "Appendix: 26.2 probe" |

## Distribution

- Previously on CurseForge: `critical-flight-details`.
- GitHub releases.
- Licensed under CC0-1.0 (public domain).
