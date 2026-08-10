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
        // Fabric + Forge for <=1.20.4. Every viable loader cell is built -
        // see PLAN.md "Target Matrix" for the exact blockers on skipped cells.
        mc("1.21.4", "fabric", "neoforge")
        mc("1.20.1", "fabric", "forge")
        mc("1.19.4", "fabric", "forge")
        mc("1.18.2", "fabric", "forge")

        vcsVersion = "1.21.4-fabric"
    }
    create(rootProject)
}

rootProject.name = "critical-flight-details"
