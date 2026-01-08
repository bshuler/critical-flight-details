import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
}

// Configure Java toolchain based on Minecraft version
// 1.20.5+ requires Java 21, 1.17-1.20.4 requires Java 17, 1.14-1.16 requires Java 8
val mcVersion = mod.minecraftVersion
val versionParts = mcVersion.split(".")
val major = versionParts[0].toIntOrNull() ?: 1
val minor = versionParts.getOrNull(1)?.toIntOrNull() ?: 0
val patch = versionParts.getOrNull(2)?.toIntOrNull() ?: 0
val javaVersion = when {
    // 1.20.5+ requires Java 21
    major > 1 || (minor > 20) || (minor == 20 && patch >= 5) -> 21
    // 1.17-1.20.4 requires Java 17
    minor >= 17 -> 17
    // 1.14-1.16.x can use Java 8, but we'll use 17 for compatibility with modern Gradle
    else -> 17
}

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
        // Quilt requires Quilted Fabric API
        if (mod.loader == "quilt") {
            requires("qsl")
        }
    }
    curseforge {
        clientRequired = true
        serverRequired = false
        // Fabric requires Fabric API
        if (mod.isFabric) {
            requires("fabric-api")
        }
        // Quilt requires Quilted Fabric API
        if (mod.loader == "quilt") {
            requires("qsl")
        }
    }
}

// Configure JAR naming: critical-flight-details-X.X.X+fabric-1.21.4.jar
tasks.withType<Jar> {
    archiveBaseName.set(project.property("archives_base_name") as String)
    archiveVersion.set("${mod.version}+${mod.loader}-${mod.minecraftVersion}")
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
