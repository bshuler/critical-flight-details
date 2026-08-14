import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
    jacoco
}

modSettings {
    clientOptions {
        fov = 90
        guiScale = 2
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }
}

dependencies {
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    if (mod.isFabric) {
        testImplementation("net.fabricmc:fabric-loader-junit:0.19.3")
    }
}

// NeoForge's transitive net.neoforged.fancymodloader:junit-fml (<=9.0.18) auto-registers a
// LauncherSessionListener that looks up a run-config file "mainargs.txt" via a relative path
// that doesn't resolve under Gradle's test-worker working directory, failing `test` at
// JUnit-launcher startup with NoSuchFileException: mainargs.txt. Forcing the upstream-fixed
// junit-fml 10.0+ doesn't work either (NoClassDefFoundError:
// net/neoforged/fml/startup/StartupArgs - it needs a newer FML core than these NeoForge
// releases ship). This repo's tests are plain pure-logic JUnit tests that need no FML
// bootstrap at all, so junit-fml is excluded from the test runtime classpath. Same approach
// as the sibling critical-orientation / EasierVillagerTrading / FlightHud /
// simple-utilities-mod / ToroHealth repos.
//
// Caveat, recorded 2026-08-13 so nobody re-derives it: excluding junit-fml is the
// right call *for these repos*, not a universal one. junit-fml is precisely
// NeoForge's own loaded-test bootstrap - it is what stands FML up so a test can
// run against a real, loaded game. NeoForge's supported loaded-test path
// (`neoForge { unitTest { enable(); testedMod = ... } }`, the `testframework`
// artifact, `@ExtendWith(EphemeralTestServerProvider.class)`, `runGameTestServer`)
// is ModDevGradle-only, and this repo builds on Architectury Loom via Stonecraft,
// so that path is unavailable here regardless. Excluding junit-fml therefore costs
// nothing today. If a cell is ever migrated to ModDevGradle, this exclusion must
// be revisited before writing any loaded NeoForge test - it would silently disable
// the very bootstrap such a test depends on.
if (mod.isNeoforge) {
    // NeoForge's own POM pulls in fancymodloader's junit-fml, which registers a JUnit
    // Platform LauncherSessionListener that unconditionally expects a mainargs.txt
    // launch-args file (only ever produced for NeoForge's in-game gametest run
    // configs). This project has no in-game tests - just plain, loader-agnostic math
    // tests - so pull it back off the test classpath. Stonecraft itself excludes this
    // same module for Minecraft <1.20.6; this mirrors that for our 1.20.6+ targets.
    configurations.named("testRuntimeClasspath") {
        exclude(group = "net.neoforged.fancymodloader", module = "junit-fml")
    }
}

// ---------------------------------------------------------------------------
// Tier 3: client gametest (a real Minecraft client, real GL context, real world)
// ---------------------------------------------------------------------------
//
// This mod draws pixels and nothing else. Its whole observable behaviour is "a red
// pitch ladder appears while you are Elytra-flying, and does not appear otherwise",
// and both halves of that sentence live in code no other tier can reach:
// FlightDisplayClient registers the per-loader HUD callback, FlightHudRenderer
// decides isFallFlying() and issues the draw calls. Both are on the JaCoCo
// exclusion list because neither can be loaded headless, and FlightHudMath - the
// only class that IS unit-tested - cannot tell whether its numbers ever reached a
// framebuffer. Drop the HudRenderCallback registration and this repo stays green
// end to end while the mod renders nothing at all.
//
// Gated `>=1.21.4` because fabric-client-gametest-api-v1 first appears around
// fabric-api 0.106 / MC 1.21.2, and `isFabric` because NeoForge's equivalent needs
// ModDevGradle (same underlying limitation as the junit-fml note above). In this
// matrix that leaves exactly one eligible cell, 1.21.4-fabric - which is also
// vcsVersion, so the test compiles from raw source rather than a generated copy.
// The three older Fabric cells (1.20.1, 1.19.4, 1.18.2) predate the API entirely.
val clientGameTestSupported = mod.isFabric &&
    stonecutter.eval(stonecutter.current.version, ">=1.21.4")

if (clientGameTestSupported) {
    // Loom is not applied through this script's `plugins {}` block, so there is no
    // generated `fabricApi { }` accessor; configure the extension by type.
    extensions.configure<net.fabricmc.loom.api.fabricapi.FabricApiExtension>("fabricApi") {
        configureTests {
            createSourceSet.set(true)
            modId.set("flight-display-gametest")
            // enableGameTests would wire `check` to dependsOn(runGameTest), dragging a
            // real *server* launch into every `./gradlew check`. This mod has no server
            // component at all, so it stays off.
            enableGameTests.set(false)
            enableClientGameTests.set(true)
            // eula deliberately left at its default (false): that is Mojang's EULA and
            // is not accepted automatically here. Only the dedicated-server variant
            // needs it.
        }
    }

    // Loom groups a run's classpath by registered mod. Without this it only knows the
    // gametest mod, leaving the mod under test ungrouped in the dev run. This repo does
    // not call splitEnvironmentSourceSets() (see CLAUDE.md - all client code lives in
    // src/main), so there is exactly one source set to register.
    extensions.configure<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom") {
        mods.create(mod.prop("id", "flight_display")).sourceSet(sourceSets["main"])
    }

    // Stonecraft configures `loom.runs.all { }`, and because that is an `all` hook it
    // also catches the `clientGameTest` config Loom creates later. Two collisions
    // result, both corrected here for that one run config only:
    //
    // 1. Stonecraft appends `--username=developer` to every client-environment run
    //    while Loom's configureTests passes `--username Player0`. Minecraft's
    //    joptsimple parser treats `username` as single-valued, so the client dies with
    //    MultipleArgumentsForOptionException before the first frame. Loom's value is
    //    kept: this is not a human's dev session. `runClient` still logs in as
    //    `developer`.
    //
    // 2. The same hook calls setRunDir on every config, overwriting the
    //    `build/run/clientGameTest` Loom had just set with the repo-root `../../run` -
    //    the developer's own dev directory. That is destructive, not untidy:
    //    GameTestSettings' clearRunDirectory conventions to `true` and Loom registers a
    //    `deleteGameTestRunDir` Delete task over runConfig.getRunDir(), so launching the
    //    gametest would wipe the developer's dev world, options and screenshots. The
    //    Delete task reads getRunDir() lazily at realization, so correcting it here
    //    redirects the deletion too.
    //
    // Loom here is 1.17.491, whose run configs expose the Provider API
    // (programArguments/runDirectory). Older Loom used programArgs/runDir, and on 1.17
    // `programArgs.removeAll {}` throws UnsupportedOperationException from
    // ImmutableList.set - so do not copy that idiom in from a sibling repo.
    //
    // afterEvaluate because Loom creates the run config while this script body is still
    // executing, and Stonecraft's `all` action fires at creation time.
    afterEvaluate {
        extensions.configure<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom") {
            runs.named("clientGameTest") {
                programArguments.set(
                    programArguments.get().filterNot { it.startsWith("--username=") }
                )
                runDirectory.set(layout.buildDirectory.dir("run/clientGameTest"))
            }
        }
    }

    // The test reads the screenshots it takes and asserts on their pixels, and it
    // derives the expected geometry from the client's own gui-scaled dimensions - so
    // modSettings' `guiScale = 2` above is load-bearing for it, not cosmetic. If that
    // value changes the test still passes (it reads the real scale back off the image),
    // but `guiScale = 0` (auto) would make the ladder's pixel size depend on the
    // runner's window size.

    // No explicit fabric-client-gametest-api-v1 dependency: Loom's configureTests adds
    // none, but Stonecraft already puts the whole fabric-api bundle on the compile
    // classpath and that bundle depends on every module including this one. Declaring
    // it again would risk pinning a second version.

    // Nothing in the normal build graph compiles a `gametest` source set, so a compile
    // error there would sit undetected until someone launched the game. Compiling it is
    // cheap and needs no display; make `check` do it. Running it stays opt-in via
    // runClientGameTest.
    tasks.named("check") {
        dependsOn(tasks.named("compileGametestJava"))
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

// JaCoCo scope: FlightDisplayClient (loader entry point) and FlightHudRenderer (the actual
// draw calls) both touch real Minecraft/loader classes (ClientModInitializer, Minecraft,
// LocalPlayer, GuiGraphics/PoseStack, and loader lifecycle events/overlays) at class-load or
// call time - merely referencing either class headless is unsafe/meaningless without a running
// game client. Both are excluded here and documented in PLAN.md; only FlightHudMath (pure,
// loader-agnostic layout/pitch/speed math) is in scope for the 100% line-coverage bar.
val jacocoExcludes = listOf(
    "net/critical/flight_display/FlightDisplayClient.class",
    "net/critical/flight_display/FlightDisplayClient$*.class",
    "net/critical/flight_display/hud/FlightHudRenderer.class",
    "net/critical/flight_display/hud/FlightHudRenderer$*.class",
)

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(classDirectories.files.map { fileTree(it) { exclude(jacocoExcludes) } })
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(classDirectories.files.map { fileTree(it) { exclude(jacocoExcludes) } })
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "1.00".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

// Forge subprojects auto-generate assets/pack.mcmeta into the main sourceSet's
// resources output (Stonecraft's generatePackMCMetaJson task) without declaring it
// as an input of compileTestJava/test, even though both consume sourceSets.main.output
// (which includes that resources dir) on their classpath. Gradle's task-validation
// then fails the build with an "implicit dependency" error. Wire the dependency
// explicitly once the task graph is fully known; this task doesn't exist on
// Fabric/NeoForge subprojects, so guard with findByName rather than named/getByName.
afterEvaluate {
    tasks.findByName("generatePackMCMetaJson")?.let { packMcMeta ->
        tasks.findByName("compileTestJava")?.dependsOn(packMcMeta)
        tasks.findByName("test")?.dependsOn(packMcMeta)
    }
}

// Publishing configuration (not invoked by this task - chiseledPublishMods is
// never run here; kept for parity with critical-orientation / future use)
publishMods {
    modrinth {
        if (mod.isFabric) requires("fabric-api")
    }

    curseforge {
        client = true
        server = false
        if (mod.isFabric) requires("fabric-api")
    }
}
