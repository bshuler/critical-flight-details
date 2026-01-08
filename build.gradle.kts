import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
}

// Configure Java toolchain based on Minecraft version
// 1.20.5+ requires Java 21, older versions use Java 17
val mcVersion = mod.minecraftVersion
val javaVersion = if (mcVersion >= "1.20.5") 21 else 17

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

// Publishing configuration for CurseForge and Modrinth
publishMods {
    modrinth {
        // Fabric requires Fabric API
        if (mod.isFabric) {
            requires("fabric-api")
        }
    }
    curseforge {
        clientRequired = true
        serverRequired = false
        // Fabric requires Fabric API
        if (mod.isFabric) {
            requires("fabric-api")
        }
    }
}

// Testing configuration
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Fix Gradle 9 task dependency issue for Forge builds
// compileTestJava needs to depend on generatePackMCMetaJson output
tasks.matching { it.name == "compileTestJava" }.configureEach {
    val generateTask = tasks.findByName("generatePackMCMetaJson")
    if (generateTask != null) {
        dependsOn(generateTask)
    }
}
