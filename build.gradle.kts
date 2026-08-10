import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
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
