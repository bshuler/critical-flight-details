pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("gg.meza.stonecraft") version "1.12.+"
    id("dev.kikugie.stonecutter") version "0.9.+"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(version: String, vararg loaders: String) {
            // Make the relevant version directories named "1.21.4-fabric", "1.21.4-neoforge", etc.
            for (it in loaders) version("$version-$it", version)
        }

        // Loader coverage mandate: Fabric + NeoForge for 1.20.5+,
        // Fabric + Forge for <=1.20.4.
        mc("26.3", "fabric", "neoforge")
        mc("26.2", "fabric", "neoforge")
        mc("26.1.2", "fabric", "neoforge")
        mc("26.1.1", "fabric", "neoforge")
        mc("26.1", "fabric", "neoforge")
        mc("1.21.11", "fabric", "neoforge")
        mc("1.21.10", "fabric", "neoforge")
        mc("1.21.9", "fabric", "neoforge")
        mc("1.21.8", "fabric", "neoforge")
        mc("1.21.7", "fabric", "neoforge")
        mc("1.21.6", "fabric", "neoforge")
        mc("1.21.5", "fabric", "neoforge")
        mc("1.21.4", "fabric", "neoforge")
        mc("1.21.1", "fabric", "neoforge")
        mc("1.20.6", "fabric", "neoforge")
        mc("1.20.4", "fabric", "forge")
        mc("1.20.1", "fabric", "forge")
        mc("1.19.4", "fabric", "forge")
        mc("1.18.2", "fabric", "forge")

        vcsVersion = "1.21.4-fabric"
    }
    create(rootProject)
}

rootProject.name = "critical-flight-details"
