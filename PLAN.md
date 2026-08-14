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

**Loader coverage mandate (updated requirement, supersedes "if feasible"):**
every Minecraft version target must build for **every loader viable on that
version** — Fabric + NeoForge for 1.20.5+, Fabric + Forge for ≤1.20.4. No
loader cell is skipped by choice; a cell is only ❌ when the loader itself
does not support that Minecraft version (a hard external blocker, recorded
below). Quilt is not a separate build target: it runs Fabric jars natively,
so Quilt compatibility is documented (README.md/CLAUDE.md) rather than built.

| Minecraft Version | Fabric | NeoForge | Forge | Status |
|--------------------|--------|----------|-------|--------|
| 1.21.4             | ✅      | ✅        | ❌     | ☐ |
| 1.20.1             | ✅      | ❌        | ✅     | ☐ |
| 1.19.4             | ✅      | ❌        | ✅     | ☐ |
| 1.18.2             | ✅      | ❌        | ✅     | ☐ |

Exact blockers for the ❌ cells (not a choice to skip — the loader/API does
not exist for that version):
- **Forge @ 1.21.4 = ❌**: Forge removed `IGuiOverlay`/
  `RegisterGuiOverlaysEvent` (the HUD overlay API this mod relies on) in
  1.20.5+, and per the mandate above 1.20.5+ is NeoForge's lane, not
  Forge's — so this is doubly excluded (API removed *and* out of Forge's
  mandated version band).
- **NeoForge @ 1.20.1/1.19.4/1.18.2 = ❌**: per the mandate, NeoForge's lane
  is 1.20.5+ only; these versions are Forge's lane. (NeoForge itself did not
  exist as a separate project before its fork from Forge in mid-2023, so it
  has no build for these versions regardless of the mandate.)

Every other cell (Fabric on all 4; NeoForge on 1.21.4; Forge on 1.20.1/
1.19.4/1.18.2) is mandatory — none may be dropped silently.

### One-jar-per-version investigation: Forgix

Bert asked to investigate merging each Minecraft version's per-loader jars
into a single "works on all loaders" jar via the
[Forgix](https://github.com/PacifistMC/Forgix) Gradle plugin.

**Maintenance status (checked live via `gh api`, not training memory,
2026-08-10):** actively maintained — latest release `2.0.0` published
2026-08-02, most recent commit `2026-08-10` (same day as this check), repo
not archived, low open-issue count with a healthy closed-issue history
(bug reports get fixed, e.g. issue #61 "JarJar de-duplication not working
for Fabric+NeoForge builds" - closed). The one alternative surfaced
(`firstdarkdev/modfusioner`, "based on Forgix") is stale (`pushed_at`
2024-06-02) and not a better choice.

**How it works:** Forgix operates *after* each loader's jar is already
built - it merges already-built jars by repackaging so each modloader's
entrypoint only ever touches its own package (JVM never loads classes the
active loader doesn't call). This makes it structurally independent of
whatever produced the input jars, so it is not inherently incompatible with
Stonecutter/Stonecraft.

**The actual friction point:** Forgix's built-in auto-detection expects
subprojects literally named `fabric`, `forge`, `neoforge`, `quilt`. Stonecutter
names its generated subprojects `<mc-version>-<loader>` (e.g.
`1.21.4-neoforge`), so auto-detection will not find them. Forgix does support
a manual override (`inputJar = project(":path").tasks.<task>.archiveFile`),
so wiring is possible per Minecraft-version cell, e.g.:

```kotlin
// root build.gradle.kts, one merge task per MC version that has 2+ loaders
forgix {
    fabric { inputJar = project(":1.21.4-fabric").tasks.named("remapJar").flatMap { (it as AbstractArchiveTask).archiveFile } }
    neoforge { inputJar = project(":1.21.4-neoforge").tasks.named("remapJar").flatMap { (it as AbstractArchiveTask).archiveFile } }
    archiveVersion = "1.21.4"
}
```

**Decision:** attempt this wiring as a stretch step *after* every per-loader
build cell above is green (the mandatory bar). If it causes build breakage
(the closed-issue history shows real friction around Mixins/TinyRemapper
with remapped jars), fall back to shipping per-loader jars from the single
codebase - which already satisfies "one codebase, every loader" even without
a literal single merged artifact - and record the exact failure here rather
than silently dropping it. Outcome recorded in the "Forgix outcome" appendix
below once attempted.

---

## Phase 1: Documentation & Planning

- [x] Read `critical-orientation` template files (settings/stonecutter/build
      gradle.kts, gradle.properties, src layout, CLAUDE.md, PLAN.md)
- [x] Determine newest stable Minecraft version live from Fabric meta API
- [x] Write `CLAUDE.md`
- [x] Write `PLAN.md` (this file)
- [x] Commit Phase 1

## Phase 2: Stonecutter/Stonecraft scaffold

Versions pinned to the **current** confirmed-working toolchain (verified live
against `meza/Stonecraft`'s own source + e2e fixtures, not the mid-edit
`critical-orientation` template): `dev.kikugie.stonecutter` `0.9.+`,
`gg.meza.stonecraft` `1.12.+`, Gradle `9.7.0`.

- [x] `settings.gradle.kts` - Stonecutter plugin + version/loader matrix
      (`shared { fun mc(...) { version(...) }; create(rootProject) }` DSL)
- [x] `stonecutter.gradle.kts` - active version pointer
- [x] `build.gradle.kts` - Stonecraft central script
- [x] `gradle.properties` - mod metadata (`mod.id`, `mod.name`, `mod.version`,
      `mod.group`, `mod.description`)
- [x] `versions/dependencies/<mc>.properties` per version cell
      (`minecraft_version`, `loader_version`, `fabric_version`, plus
      `forge_version`/`neoforge_version`/`yarn_mappings` as applicable -
      schema confirmed against `Dependencies.kt` + real fixtures)
- [x] Gradle wrapper bumped to `9.7.0` (current Stonecutter 0.9.x era)
- [x] Remove old `build.gradle` / `settings.gradle` (Groovy DSL, Loom 0.4)
- [x] Commit Phase 2

## Phase 3: Source port, latest-first

Working directly in the shared `src/` tree, adding Stonecutter conditionals
as each older target is walked back to, per the task brief.

- [x] `FlightHudMath` - extract pure layout/pitch-offset/speed math out of
      the old `FlightDisplayHud.draw()`, loader-agnostic, unit-tested
- [x] `FlightHudRenderer` - the actual draw calls, version/loader-branched
- [x] `FlightDisplayClient` - loader entrypoint registering the HUD hook
- [x] Remove `GameInfoMixin` / `flight_display.mixins.json` (superseded by
      official HUD render events - see CLAUDE.md Architecture section)
- [x] `fabric.mod.json`, `META-INF/mods.toml`, `META-INF/neoforge.mods.toml`
- [x] 1.21.4-fabric builds green
- [x] 1.21.4-neoforge builds green
- [x] 1.20.1-fabric builds green
- [x] 1.20.1-forge builds green
- [x] 1.19.4-fabric builds green
- [x] 1.19.4-forge builds green
- [x] 1.18.2-fabric builds green
- [x] 1.18.2-forge builds green
- [x] `./gradlew chiseledBuild` green for the whole matrix
- [x] Commit Phase 3 (split into multiple incremental commits per version as
      the walk-back proceeded: `6d9ed84`, `4b17935`, `fa42c2d`)

## Phase 3b: Forgix one-jar merge (stretch, after Phase 3 is fully green)

- [ ] Apply `io.github.pacifistmc.forgix` at the root
- [ ] Wire manual `inputJar` overrides per MC-version cell (Stonecutter's
      `<version>-<loader>` subproject names don't match Forgix's
      auto-detected `fabric`/`forge`/`neoforge` names)
- [ ] `mergeJars` produces one jar for 1.21.4 (fabric+neoforge)
- [ ] `mergeJars` produces one jar for 1.20.1 (fabric+forge)
- [ ] `mergeJars` produces one jar for 1.19.4 (fabric+forge)
- [ ] `mergeJars` produces one jar for 1.18.2 (fabric+forge)
- [ ] Record outcome in "Forgix outcome" appendix; fall back to per-loader
      jars (already satisfies the mandate) for any cell where it breaks the
      build, with the exact error recorded

## Phase 4: Repo hygiene

- [x] Default branch is `main` (confirmed via `gh repo view --json
      defaultBranchRef`; already correct, no rename needed)
- [x] Replaced `.github/workflows/gradlepublish.yml` (1.16-era, JDK 8,
      `gradle publish` - none of which matches the current build) with
      `build.yml`: a `chiseledBuild` + `test` CI workflow mirroring
      `critical-orientation`'s, minus any release/publish steps (out of
      scope per the task brief - no Modrinth/CurseForge publishing)
- [x] Updated `README.md` version/build instructions to match the new
      matrix (`chiseledBuild`, per-version/loader jar paths, supported
      version table)
- [x] Removed unused `src/main/resources/assets/flight_display/icon.ico`
      (dead asset - only `icon.png` is referenced from any of the three
      loader manifests; `.ico` was never wired up anywhere)
- [ ] Final commit + push

---

## Phase 5: Test coverage (JaCoCo) + Folia

### Test coverage

JaCoCo wiring mirrors the sibling `critical-orientation` template exactly
(that repo's `build.gradle.kts` was read for reference, not modified):
`jacoco` plugin applied at the root Stonecraft script (shared across every
version/loader subproject), `tasks.test { finalizedBy(jacocoTestReport) }`,
an explicit `jacocoExcludes` file-pattern list applied to both
`jacocoTestReport` and `jacocoTestCoverageVerification`'s `classDirectories`,
a `LINE` / `COVEREDRATIO` / `minimum = 1.00` violation rule, and
`tasks.check { dependsOn(jacocoTestCoverageVerification) }` so the bar is
enforced by `check`/`build`, not just advisory.

Per the task brief, coverage was driven and verified on the **active**
Stonecutter project only (`1.21.4-fabric`) — never across the full matrix.

**Coverage result (active project, `1.21.4-fabric`): 100% line coverage**
(32/32 lines, 235/235 instructions covered per
`versions/1.21.4-fabric/build/reports/jacoco/test/jacocoTestReport.xml`) on
the in-scope class set. `jacocoTestCoverageVerification` passes at the
enforced 100% `LINE`/`COVEREDRATIO` bar.

**In scope (3 classes analyzed — confirmed nonzero, not a malformed-include
false pass):**
- `net.critical.flight_display.FlightHudMath` (+ its two records,
  `FlightHudMath$Layout` and `FlightHudMath$SpeedSample`) — pure,
  loader-agnostic layout/pitch-ladder/speed math with zero Minecraft-class
  dependency. Already had a thorough `FlightHudMathTest` from the Phase 3
  port; no gaps found, no changes needed to reach 100%.

**Excluded (documented, not silent):**
- `net.critical.flight_display.FlightDisplayClient` — untestable headless:
  its `elif` bodies are `@Mod`-annotated loader entry points
  (`ClientModInitializer`, NeoForge's `IEventBus`/`NeoForge.EVENT_BUS`,
  Forge's `FMLJavaModLoadingContext`) that register against real
  loader/event-bus singletons at construction/class-load time — there is no
  headless double for any of them.
- `net.critical.flight_display.hud.FlightHudRenderer` — untestable headless:
  every code path calls `Minecraft.getInstance()`, reads `LocalPlayer`/
  `GuiGraphics`/`PoseStack` state, and issues real draw calls; it exists
  purely to marshal Minecraft-client state into `FlightHudMath`'s already-
  tested pure functions and then draw the result. There is nothing left to
  test in this class that doesn't require a running game client.

No bugs were found in `FlightHudMath` while wiring coverage — the existing
Phase 3 test suite (`FlightHudMathTest`) already exercised every branch,
including the documented negative-modulo and clipping edge cases.

**Running tests + coverage locally** (see also CLAUDE.md):

```bash
# Active project only - per-repo policy, never run test across the whole matrix
./gradlew ":1.21.4-fabric:test" ":1.21.4-fabric:jacocoTestReport"
./gradlew ":1.21.4-fabric:jacocoTestCoverageVerification"   # enforces the 100% LINE bar
```

### Folia

Folia n/a — client mod. Folia is a server-side Paper/Bukkit fork
(region-threaded scheduler for servers); this mod has no server component
at all (client-only HUD overlay, `side = CLIENT` in every loader manifest,
no `src/server`/common logic). The Folia compatibility work in this brief
(scheduler audits, `folia-supported: true` in `plugin.yml`, etc.) does not
apply to a client-only mod.

---

## Appendix: 26.2 probe (stretch goal, outside the guaranteed matrix)

Documented separately so a future pass can pick this up once the toolchain
catches up. Not required for this task's bar.

Attempted: a bare `fabric-loom` 1.17 project (no Stonecutter/Stonecraft)
against Minecraft `26.2`, Fabric Loader `0.19.3`, Gradle `9.5.1`, using
Mojang's own (unobfuscated) class names directly instead of Yarn.

**Known API-break points for 26.2**, relayed from sibling mods' own porting
work against the same version (not independently re-derived here, but
consistent with this mod's own HUD-render-hook architecture, so recorded
verbatim for whoever picks this probe up):

- The GUI render pipeline this mod's `HudRenderCallback`/`RenderGuiEvent`/
  `RegisterGuiOverlaysEvent` hooks all ultimately draw through is rewritten
  outright: `GuiGraphics` and `Gui.render(...)` are gone. In their place,
  rendering happens via `extractRenderState(DeltaTracker, boolean, boolean)`
  populating a `net.minecraft.client.renderer.state.gui.GuiRenderState`.
  This mod would need its HUD draw calls (`fill`, `drawString`) re-targeted
  at whatever the new state-object equivalent is - a real redesign of
  `FlightHudRenderer`'s drawing layer, not a signature tweak.
- `KeyMapping`'s constructor now takes a `KeyMapping.Category` instead of a
  raw `String` for its category argument. Not used by this mod today (no
  keybinds - see CLAUDE.md "Project Overview"), so this specific break is
  moot for `critical-flight-details`, but worth knowing if a config
  keybind is ever added.
- Fabric API's `KeyBindingHelper`/`ClientCommandManager` moved packages
  relative to 1.21.4. Same "moot unless a keybind/command is added" caveat
  as above.

Net assessment: the GUI-pipeline rewrite is the real blocker for this mod
specifically, since HUD drawing is this mod's entire purpose. Any 26.2 port
attempt should design the HUD draw call as its own small abstraction (e.g.
an interface with one `drawLadder(...)`-style method) with the
`GuiRenderState`-based endpoint in mind from the start, rather than trying
to retrofit the existing `GuiGraphics`-based calls after the fact.

Result: _(filled in after the probe is actually run - see commit history /
final task report for outcome)_.

---

## Appendix: Forgix outcome (one-jar-per-version merge)

Investigation (maintenance status, mechanism, integration path) is written up
under "One-jar-per-version investigation: Forgix" above. This section records
the actual attempt once Phase 3 is green.

Result: _(filled in after Phase 3b is actually attempted)_.

---

## Appendix: Key Resources

- [Stonecutter Documentation](https://stonecutter.kikugie.dev/)
- [Stonecraft](https://stonecraft.meza.gg/)
- [Forgix](https://github.com/PacifistMC/Forgix)
- [Fabric porting docs](https://docs.fabricmc.net/develop/porting/)
- [Fabric API HudRenderCallback → HudElementRegistry deprecation](https://docs.fabricmc.net/)
- [NeoForge RenderGuiEvent](https://docs.neoforged.net/)
- [Forge IGuiOverlay / RegisterGuiOverlaysEvent (removed 1.20.5+)](https://docs.minecraftforge.net/)

## Coverage in context (measured 2026-08-13)

Read from the JaCoCo XML report, not from whether the gate passes:

- **Analysed surface:** 3 of 5 compiled classes (60%).
- **Line coverage of that surface:** 100.0% (32 lines analysed).
- Classes outside that surface are excluded by the documented exclusion list. They
  are not covered by any test and are not runtime-verified.
  Measured from `critical-flight-details/versions/1.18.2-fabric/build/reports/jacoco/test/jacocoTestReport.xml`.

A passing `check` means "no regression inside the analysed surface" — it does not
mean the whole codebase is tested to that percentage.

That is not a theoretical caveat. Deleting the mod's HUD registration outright —
so it draws nothing at all — leaves `check` fully green: unit tests, Tier 1
loaded tests, and this 100% gate all pass. Measured, not reasoned about; see
"Tier 3: client gametest" below, which is the tier that catches it.

## Tier 1: loaded-game testing (added 2026-08-13)

The coverage numbers above measure *headless* tests against pure logic. Until
now nothing in this repo verified that the mod's assumptions survive contact
with a real game.

`net.fabricmc:fabric-loader-junit` closes that gap on the Fabric cells - it
stands a real Fabric loader up inside the JUnit worker, so a test can bootstrap
Minecraft and assert against loaded game data. `LoadedGameTest` uses it for four
things a headless test structurally cannot do: prove the item registry really
loaded, prove a real loader discovers this mod from its processed
`fabric.mod.json`, prove every declared `depends` range is satisfiable in that
specific cell, and pin the pitch-ladder math to vanilla's own clamped pitch
range via `Mth.clamp`.

The dependency-range check is the one with teeth. In a Stonecutter matrix a cell
can compile and package flawlessly while declaring a `minecraft` range that
excludes the very version it was built for; the jar then fails at load time, in
front of a user, with nothing in CI having gone red. That failure mode is now
caught per cell at test time.

**Verified, not assumed:** `./gradlew test` runs it in all 4 Fabric cells
(1.18.2, 1.19.4, 1.20.1, 1.21.4) with 5 tests each, 0 failures, 0 errors, parsed
from `versions/*/build/test-results/test/*LoadedGameTest.xml`.

**What it does not cover.** This is a loaded *game*, not a loaded *client*: no
window, no render pass, no player entity. `FlightDisplayClient` and
`FlightHudRenderer` remain untested and excluded, as documented above.

**NeoForge cells have no equivalent** - not an oversight, NeoForge's loaded-test
path is ModDevGradle-only. See the junit-fml comment in `build.gradle.kts`.

## Tier 3: client gametest (added 2026-08-14)

Tier 1 proved the mod *loads*. It could not prove the mod *draws*. This tier
boots a real Minecraft client on a real GL context, flies a real player with a
real Elytra, screenshots the frame, and reads the pixels back.

`src/gametest/java/net/critical/flight_display/gametest/FlightHudClientGameTest.java`,
on `fabric-client-gametest-api-v1` 4.1.1 via Loom's generated
`runClientGameTest` task.

```bash
./gradlew :1.21.4-fabric:runClientGameTest
```

**Only 1.21.4-fabric is eligible.** `fabric-client-gametest-api-v1` does not
exist for the three older Fabric cells, and the NeoForge equivalent needs
ModDevGradle rather than Architectury Loom (same blocker as Tier 1). The
`clientGameTestSupported` gate in `build.gradle.kts` is
`mod.isFabric && stonecutter.eval(current.version, ">=1.21.4")`, so the other
seven cells never see the source set. Verified: a full `chiseledBuild` runs
`compileGametestJava` exactly once, in `1.21.4-fabric`, and all 8 shipped jars
contain zero entries matching `gametest`.

### What it asserts

Three frames, in order:

| Frame | State | Assertion |
|---|---|---|
| `0000_flight-hud-absent-grounded` | survival, on the ground, no screen | **zero** HUD-red pixels inside the ladder box |
| `0001_flight-hud-visible-flying` | gliding, pitch -20 | 12 hash rows at grid offset 0 |
| `0002_flight-hud-pitch-scrolled` | gliding, pitch -25 | 11 hash rows at grid offset -d/2 |

The two flying frames are what make this more than a "did it crash" test. The
ladder scrolls with pitch: `pitchOffset` is
`(distanceBetweenHashes / 10) * (rawTruncatedPitch % 10)`, so pitch -20 gives
offset 0 and pitch -25 gives offset -d/2. At offset 0, 12 marks fall inside
`[top, bottom]`; at half a spacing, one falls out and 11 remain. Asserting both
the *count* and the *grid phase* means the test fails if the ladder renders but
stops responding to pitch - which a "some red pixels are present" check would
happily pass.

The test does not call `FlightHudMath.computeLayout` to decide where to look. It
recomputes the thirds itself from the client's own gui dimensions, so a wrong
`computeLayout` is caught rather than agreed with.

### Reading the screenshot

`ClientGameTestContext.takeScreenshot(String)` returns a `java.nio.file.Path`,
so the test reads its own screenshot back with `NativeImage` (`getPixel` returns
ARGB - bytecode-verified: it calls `getPixelABGR` then `ARGB.fromABGR`).
`NativeImage` rather than `javax.imageio`, because AWT initialization inside a
live LWJGL client is not safe on macOS.

**Minecraft renders the same requested colour as two different pixel values.**
`RED` in `FlightHudRenderer` is `0xFFFF0000`. A `guiGraphics.fill` quad lands as
exactly `0xFFFF0000`. A `guiGraphics.drawString` glyph lands as `0xFFFC0000` -
green and blue exactly 0, red 252 rather than 255. It is not alpha blending;
there is no background leak. Because the "Speed:" readout draws on top of a hash
mark, an exact match against `0xFFFF0000` measured that row at 82.5% coverage and
dropped it, producing `expected 12 hash marks at pitch -20.0, found 11`.

The fix is a band predicate rather than an exact match: `A == 255 && R >= 0xF0 &&
G == 0 && B == 0`. That was validated empirically before being trusted - it
matches **0 pixels in an entire grounded vanilla frame** (sky, terrain, hearts,
hotbar and chat all miss it) and **8164 while flying, every one inside the ladder
box**, in exactly those two shades. It is also the driver-independent choice,
since the `0xFC` is a shader artifact that a software GL stack need not
reproduce.

Tolerance for a row's distance from its expected grid position is
`max(3.0, hashSpacing * 0.4)`. A correct renderer's error budget is ~2.5px (int
truncation at each `(int) layout.left()` fill call, plus run-centre rounding) and
measured under 1px; a whole-spacing scroll is 14.5px and a half-spacing scroll
7.3px, so 0.4 spacings clears the real error by 2x while still failing both
scroll errors.

### Staging

Elytra flight has to be produced, not asserted into being:

- `TestServerContext#runCommand` **swallows command failures**, so every staging
  command is verified by its observable effect rather than by its return. It also
  dispatches from world spawn, so position-relative commands are wrapped in
  `execute as @p at @s run …`.
- Gliding is started by holding the **real jump key** in a retry loop, because
  vanilla `LocalPlayer` only sends `START_FALL_FLYING` on a fresh key press -
  setting the flag directly would test the test, not the game.
- `setUseConsistentSettings(true)` plus `guiScale = 2` in `modSettings` fixes the
  gui dimensions, but the test still reads the real scale back off each
  screenshot rather than assuming it.

### Negative controls

Three deliberate breakages, each run to confirm it fails with the right
diagnosis, then reverted (`git diff --stat src/main/` empty afterwards):

| Control | Break | Result |
|---|---|---|
| NC1 | delete the `HudRenderCallback` registration in `FlightDisplayClient` | fires - no HUD at all |
| NC2 | delete the `isFallFlying()` guard in `FlightHudRenderer` | fires - `found 7956 HUD-red pixels inside the ladder box` on the grounded frame |
| NC3 | zero out `pitchOffset` | fires - wrong grid phase |

**NC2 initially did not fire, and that was a defect in the test, not in the
mod.** After ruling out a stale build (`javap -c` confirmed the `isFallFlying`
call was gone from the rebuilt class), reading screenshot 0000 by eye showed
"Loading terrain…" - a `ReceivingLevelScreen`. Minecraft renders no in-game HUD
behind a screen, so the grounded control was photographing a frame in which *no
mod* could have drawn anything. It could not fail. The fix is `awaitLiveHudFrame`,
which waits for `screen == null && level != null && player != null &&
player.onGround()` before the control frame is taken. A negative control that
cannot fail is worse than no control, because the suite looks stronger than it
is.

**The controls are not equally valuable.** NC3 is also caught by the unit tests
(`pitchOffsetMatchesOriginalFormula` and
`pitchOffsetPreservesJavaNegativeModuloBehavior` both fail: 19 tests, 2 failures),
so its role here is proving the math reaches *pixels*. NC1 and NC2 are the ones
invisible to every other tier.

### The coverage gap, demonstrated

With NC1 applied - the mod registering no HUD renderer at all, drawing literally
nothing - `./gradlew :1.21.4-fabric:check` was **BUILD SUCCESSFUL**. Unit tests
green, Tier 1 loaded tests green, the 100% JaCoCo line-coverage gate green. This
is the concrete meaning of the exclusion list under "Coverage in context": the
two excluded classes are the entire user-visible behaviour of the mod, and until
this tier nothing in CI could tell whether they worked.

### CI

`.github/workflows/build.yml` gains a `client-gametest` job: xvfb plus a software
GL stack (`LIBGL_ALWAYS_SOFTWARE=true`, `MESA_GL_VERSION_OVERRIDE=3.3`), a fixed
`1920x1080x24` virtual screen, and an `if: always()` upload of the screenshots and
logs - those artifacts are most useful precisely when the run failed, since the
assertion messages quote pixel coordinates.

It is deliberately a **separate job and not a gate on the release artifacts**: a
flake in a software GL stack should not withhold jars that compiled and passed
every headless test.

### Tier 4 (NeoForge `testframework`) remains blocked

NeoForge's own gametest framework is ModDevGradle-only. This repo builds NeoForge
through Architectury Loom (via Stonecraft), so there is no supported path to it
without splitting the NeoForge cell out of the Stonecutter matrix onto a separate
toolchain. Documented as blocked rather than attempted.
