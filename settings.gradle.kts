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

        // Minecraft 1.21.x - Latest (NeoForge era)
        mc("1.21.4", "fabric", "neoforge")

        // Minecraft 1.20.x - Trails & Tales
        mc("1.20.6", "fabric", "neoforge")  // Last 1.20.x with NeoForge
        mc("1.20.1", "fabric")  // Note: Forge removed - Stonecraft/Architectury Loom incompatibility

        // Minecraft 1.19.x - The Wild Update (MatrixStack era)
        mc("1.19.4", "fabric")  // Fabric only - Forge not supported by Stonecraft

        // Minecraft 1.18.x - Caves & Cliffs Part 2
        mc("1.18.2", "fabric")  // Fabric only - Forge not supported by Stonecraft

        // Default active version (NeoForge so root project can compile NeoForgeHudRenderer)
        vcsVersion = "1.21.4-neoforge"
    }
}

rootProject.name = "critical-flight-details"
