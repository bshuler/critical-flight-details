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
    id("gg.meza.stonecraft") version "1.9.+"
    id("dev.kikugie.stonecutter") version "0.8.+"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(version: String, vararg loaders: String) {
            for (loader in loaders) vers("$version-$loader", version)
        }
        // Start with 1.21.4 for Fabric and NeoForge
        mc("1.21.4", "fabric", "neoforge")
        // Will add more versions later:
        // mc("1.20.6", "fabric", "neoforge")
        // mc("1.20.1", "fabric", "forge")
        vcsVersion = "1.21.4-fabric"
    }
    create(rootProject)
}

rootProject.name = "critical-flight-details"
