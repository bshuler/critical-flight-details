# Critical Flight Display - Modernization Plan

## Executive Summary

Transform Critical Flight Display from a single-version (Minecraft
1.16.1, Java 8, Fabric Loom 0.4) client HUD mod into a Stonecutter
multi-version, Stonecraft multi-loader mod, mirroring the already-modernized
sibling repo `critical-orientation`. The Mixin-based HUD injection is
replaced by the official per-loader HUD render event, and the old
immediate-mode (`GL11`/`Tessellator`) drawing is replaced with
`DrawContext`/`GuiGraphics` fill/text calls.

Bar for "done" per version/loader cell: it **compiles and builds**
(`./gradlew :<version>-<loader>:build` / `chiseledBuild` green). Running the
game client is not required.

---

## Newest stable Minecraft version (checked live, not from training memory)

Queried `https://meta.fabricmc.net/v2/versions/game` on 2026-08-10:

```
{'version': '26.2', 'stable': True}       <- newest stable
{'version': '26.1.2', 'stable': True}
{'version': '26.1.1', 'stable': True}
{'version': '26.1', 'stable': True}
{'version': '1.21.11', 'stable': True}    <- newest stable under the old numbering
```

Minecraft moved to a new calendar-style version line (`26.1`, `26.2`, ...)
starting March 2026, and this **is not just a rename** - `26.1` was the first
Minecraft: Java Edition release shipped fully **unobfuscated**. As a direct
consequence:

- Fabric's Yarn mappings project stopped receiving updates after `1.21.11`
  (the last obfuscated release) - see
  [Fabric's 26.1 porting docs](https://docs.fabricmc.net/develop/porting/)
  and the [Fabric 26.2 announcement](https://fabricmc.net/2026/06/15/262.html).
  Fabric mods for `26.1`+ compile against Mojang's own (now public) class/
  method names directly - there is no more intermediary Yarn layer to
  abstract over Fabric-vs-Forge naming differences.
- This requires Fabric Loom **1.17** and Gradle **9.5.1** to develop against
  `26.2` per Fabric's own guidance - substantially newer than what this
  project's toolchain (Gradle 8.11.1, Stonecutter/Stonecraft) currently pins.
- **Architectury Loom** (the multi-loader Loom fork that Stonecraft's
  Forge/NeoForge subprojects depend on) cannot build **any** project against
  `26.1`+ yet: see the still-open upstream issue
  [architectury/architectury-loom#328 "26.1 Support"](https://github.com/architectury/architectury-loom/issues/328)
  (opened 2026-02-01, open as of 2026-08-10) - it crashes because it can't
  resolve mappings for a mappings-less (unobfuscated) version.
- **Stonecraft** itself (`gg.meza.stonecraft`, the plugin this project's
  multi-loader setup is built on, same as `critical-orientation`) has not
  published a release since **2025-12-16** - before `26.1` (2026-03-14) or
  `26.2` (2026-06-15) existed. It has no knowledge of the new version line.

### Decision

The buildable target matrix in this repo mirrors `critical-orientation`'s
proven matrix exactly, topping out at **1.21.4** (Fabric + NeoForge) rather
than the literal newest stable release (`26.2`). This is the same kind of
documented, evidence-based fallback the task brief explicitly allows for
loader infeasibility ("If NeoForge/Forge ports are infeasible... Fabric-only
is acceptable"), applied one level up: here the *whole current toolchain*
(Stonecutter+Stonecraft, pinned to older Architectury Loom) cannot target
`26.x` for **any** loader yet, not just NeoForge/Forge.

A plain (non-Stonecraft, non-Architectury) Fabric-only probe against `26.2`
using bare `fabric-loom` 1.17 was attempted separately as a stretch goal -
see "Appendix: 26.2 probe" below for the outcome.

---

## Target Matrix

| Minecraft Version | Fabric | NeoForge | Forge | Status |
|--------------------|--------|----------|-------|--------|
| 1.21.4             | ✅      | ✅        | ❌     | ☐ |
| 1.20.1             | ✅      | ❌        | ✅     | ☐ |
| 1.19.4             | ✅      | ❌        | ✅     | ☐ |
| 1.18.2             | ✅      | ❌        | ✅     | ☐ |

Forge stops at 1.20.1 because Forge removed `IGuiOverlay`/
`RegisterGuiOverlaysEvent` (the HUD overlay API this mod relies on) in
1.20.5+. NeoForge only appears at 1.21.4 because NeoForge forked from Forge
at 1.20.2 - mirrors `critical-orientation`'s own matrix and rationale
exactly.

---

## Phase 1: Documentation & Planning

- [x] Read `critical-orientation` template files (settings/stonecutter/build
      gradle.kts, gradle.properties, src layout, CLAUDE.md, PLAN.md)
- [x] Determine newest stable Minecraft version live from Fabric meta API
- [x] Write `CLAUDE.md`
- [x] Write `PLAN.md` (this file)
- [ ] Commit Phase 1

## Phase 2: Stonecutter/Stonecraft scaffold

- [ ] `settings.gradle.kts` - Stonecutter plugin + version/loader matrix
- [ ] `stonecutter.gradle.kts` - active version pointer
- [ ] `build.gradle.kts` - Stonecraft central script
- [ ] `gradle.properties` - mod metadata (`mod.id`, `mod.name`, `mod.version`,
      `mod.group`, `mod.description`)
- [ ] Gradle wrapper bumped to match template (8.11.1)
- [ ] Remove old `build.gradle` / `settings.gradle` (Groovy DSL, Loom 0.4)
- [ ] Commit Phase 2

## Phase 3: Source port, latest-first

Working directly in the shared `src/` tree, adding Stonecutter conditionals
as each older target is walked back to, per the task brief.

- [ ] `FlightHudMath` - extract pure layout/pitch-offset/speed math out of
      the old `FlightDisplayHud.draw()`, loader-agnostic, unit-tested
- [ ] `FlightHudRenderer` - the actual draw calls, version/loader-branched
- [ ] `FlightDisplayClient` - loader entrypoint registering the HUD hook
- [ ] Remove `GameInfoMixin` / `flight_display.mixins.json` (superseded by
      official HUD render events - see CLAUDE.md Architecture section)
- [ ] `fabric.mod.json`, `META-INF/mods.toml`, `META-INF/neoforge.mods.toml`
- [ ] 1.21.4-fabric builds green
- [ ] 1.21.4-neoforge builds green
- [ ] 1.20.1-fabric builds green
- [ ] 1.20.1-forge builds green
- [ ] 1.19.4-fabric builds green
- [ ] 1.19.4-forge builds green
- [ ] 1.18.2-fabric builds green
- [ ] 1.18.2-forge builds green
- [ ] `./gradlew chiseledBuild` green for the whole matrix
- [ ] Commit Phase 3 (may be split into multiple incremental commits per
      version as the walk-back proceeds)

## Phase 4: Repo hygiene

- [ ] Rename default branch `master` → `main`
- [ ] Update/retire `.github/workflows/gradlepublish.yml` (1.16-era, no
      longer matches the build) - replace with a `chiseledBuild` CI workflow
      mirroring `critical-orientation`'s `build.yml`, or remove if out of
      scope
- [ ] Update `README.md` version/build instructions to match the new matrix
- [ ] Final commit + push

---

## Appendix: 26.2 probe (stretch goal, outside the guaranteed matrix)

Documented separately so a future pass can pick this up once the toolchain
catches up. Not required for this task's bar.

Attempted: a bare `fabric-loom` 1.17 project (no Stonecutter/Stonecraft)
against Minecraft `26.2`, Fabric Loader `0.19.3`, Gradle `9.5.1`, using
Mojang's own (unobfuscated) class names directly instead of Yarn.

Result: _(filled in after the probe is actually run - see commit history /
final task report for outcome)_.

---

## Appendix: Key Resources

- [Stonecutter Documentation](https://stonecutter.kikugie.dev/)
- [Stonecraft](https://stonecraft.meza.gg/)
- [Fabric porting docs](https://docs.fabricmc.net/develop/porting/)
- [Fabric API HudRenderCallback → HudElementRegistry deprecation](https://docs.fabricmc.net/)
- [NeoForge RenderGuiEvent](https://docs.neoforged.net/)
- [Forge IGuiOverlay / RegisterGuiOverlaysEvent (removed 1.20.5+)](https://docs.minecraftforge.net/)
