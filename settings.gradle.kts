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

        // ===== 1.20.x =====
        // 1.20.6: Fabric + NeoForge (stable NeoForge era)
        mc("1.20.6", "fabric", "neoforge")
        // 1.20.5: Fabric only (NeoForge has Java version resolution issues)
        mc("1.20.5", "fabric")
        // 1.20.2-1.20.4: Fabric only (Forge toolchain not supported by Loom)
        mc("1.20.4", "fabric")
        mc("1.20.3", "fabric")
        mc("1.20.2", "fabric")
        // 1.20-1.20.1: Fabric + Forge (pre-NeoForge split, well-supported)
        mc("1.20.1", "fabric", "forge")
        mc("1.20", "fabric", "forge")

        // ===== Legacy versions =====
        // 1.19.4 - Fabric + Forge
        mc("1.19.4", "fabric", "forge")
        // 1.18.2 - Fabric + Forge
        mc("1.18.2", "fabric", "forge")
    }
    create(rootProject)
}

rootProject.name = "critical-flight-details"
