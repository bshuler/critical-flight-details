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

    create(rootProject) {
        fun mc(mcVersion: String, vararg loaders: String) {
            for (loader in loaders) version("$mcVersion-$loader", mcVersion)
        }

        // Minecraft versions - Fabric only for now
        // NeoForge/Forge support requires additional Stonecraft configuration
        mc("1.21.4", "fabric")
        mc("1.20.6", "fabric")
        mc("1.20.1", "fabric")
        mc("1.19.4", "fabric")
        mc("1.18.2", "fabric")

        // Default active version
        vcsVersion = "1.21.4-fabric"
    }
}

rootProject.name = "critical-flight-details"
