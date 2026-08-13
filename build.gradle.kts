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
