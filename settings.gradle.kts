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
            for (loader in loaders) version("$version-$loader", version)
        }

        // Target versions and loaders
        // ===== 1.21.x (Fabric + NeoForge) =====
        // 1.21.5+ use DrawContext for line rendering (BufferRenderer removed)
        // 1.21.2+ use isGliding() instead of isFallFlying(), ShaderProgramKeys
        mc("1.21.11", "fabric", "neoforge")
        mc("1.21.10", "fabric", "neoforge")
        mc("1.21.9", "fabric", "neoforge")
        mc("1.21.8", "fabric", "neoforge")
        mc("1.21.7", "fabric", "neoforge")
        mc("1.21.6", "fabric", "neoforge")
        mc("1.21.5", "fabric", "neoforge")
        mc("1.21.4", "fabric", "neoforge")
        mc("1.21.3", "fabric", "neoforge")
        mc("1.21.2", "fabric", "neoforge")
        mc("1.21.1", "fabric", "neoforge")
        mc("1.21", "fabric", "neoforge")

        // ===== 1.20.x (Fabric + NeoForge + Forge) =====
        mc("1.20.6", "fabric", "neoforge")
        mc("1.20.5", "fabric")
        mc("1.20.4", "fabric")
        mc("1.20.3", "fabric")
        mc("1.20.2", "fabric")
        mc("1.20.1", "fabric", "forge")
        mc("1.20", "fabric", "forge")

        // ===== 1.19.x (Fabric + Forge) =====
        mc("1.19.4", "fabric", "forge")
        mc("1.19.3", "fabric", "forge")
        mc("1.19.2", "fabric", "forge")
        mc("1.19.1", "fabric", "forge")
        mc("1.19", "fabric", "forge")

        // ===== 1.18.x (Fabric + Forge) =====
        mc("1.18.2", "fabric", "forge")
        mc("1.18.1", "fabric", "forge")
        mc("1.18", "fabric", "forge")

        // ===== 1.17.x (Fabric + Forge) =====
        mc("1.17.1", "fabric", "forge")

        // Note: 1.14.x-1.16.x omitted - 4-5+ years old with significantly different APIs
        // and Fabric API module structure that would require breaking changes to support
    }
    create(rootProject)
}

rootProject.name = "critical-flight-details"
