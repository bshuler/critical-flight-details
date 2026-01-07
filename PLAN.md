# Critical Flight Display - Modernization & Multi-Platform Expansion Plan

## Executive Summary

This plan outlines the complete modernization of Critical Flight Display from a single Minecraft 1.16.1 Fabric mod to a multi-loader, multi-version universal client-side mod with comprehensive CI/CD and testing infrastructure.

**Key Decisions:**
- **Multi-Version Strategy**: Stonecutter preprocessor (single codebase with version-specific code paths)
- **Multi-Loader Strategy**: Architectury + Stonecraft (Fabric, Forge, NeoForge, Quilt)
- **Scope**: Client-side only (no server-side features)

---

## Phase 1: Infrastructure & Branch Setup ✅ COMPLETED

### 1.1 Git Branch Structure
```
main          → Production releases only (triggers CurseForge/Modrinth deploy)
test          → Integration testing branch
dev           → Active development branch (we work here)
```

### 1.2 Completed Tasks
- [x] Create development branch
- [x] Update `.gitignore` for modern tooling
- [x] Set up CI/CD workflows

---

## Phase 2: Upgrade to Latest Minecraft & Fabric ✅ COMPLETED

### 2.1 Achieved Versions
| Component | Before | After |
|-----------|--------|-------|
| Minecraft | 1.16.1 | 1.21.4 |
| Fabric Loader | 0.8.8 | 0.16.10 |
| Fabric Loom | 0.4-SNAPSHOT | 1.9-SNAPSHOT |
| Java | 8 | 21 |
| Gradle | 6.5 | 8.11.1 |

### 2.2 Completed Tasks
- [x] Update Gradle wrapper to 8.11.1
- [x] Update build.gradle with modern Fabric Loom
- [x] Migrate from jcenter() to mavenCentral()
- [x] Update to Java 21
- [x] Update Yarn mappings to 1.21.4+build.8
- [x] Update Fabric API to 0.114.0+1.21.4
- [x] Migrate to DrawContext rendering API
- [x] Update Mixin injection points
- [x] Update fabric.mod.json

---

## Phase 3: Comprehensive Testing Framework ✅ COMPLETED

### 3.1 Testing Infrastructure
- [x] JUnit 5 dependency added
- [x] Test source set created (`src/test/java`)
- [x] Speed calculation unit tests implemented

### 3.2 Test Coverage
- [x] Distance calculation tests
- [x] Speed calculation tests
- [x] Edge case handling tests

---

## Phase 4: CI/CD Pipeline ✅ COMPLETED

### 4.1 Implemented Workflows
```
.github/workflows/
├── build.yml    → Build & test on push/PR to dev/test/main
└── release.yml  → Deploy to CurseForge & Modrinth on version tags
```

### 4.2 Required Secrets (to be configured)
```yaml
CURSEFORGE_ID       → CurseForge project ID
CURSEFORGE_TOKEN    → CurseForge API token
MODRINTH_ID         → Modrinth project ID
MODRINTH_TOKEN      → Modrinth API token
```

---

## Phase 5: Multi-Version Support (Stonecutter)

### 5.1 Chosen Strategy: Stonecutter Preprocessor

Using [Stonecutter](https://stonecutter.kikugie.dev/) for multi-version management:
- Single codebase with version-specific code paths
- Preprocessor comments for conditional compilation
- Semantic version comparisons
- Works with both Fabric and Forge ecosystems

### 5.2 Target Minecraft Versions

| Version | Codename | Priority | Java |
|---------|----------|----------|------|
| 1.21.x | Tricky Trials | High (latest) | 21 |
| 1.20.x | Trails & Tales | High | 17-21 |
| 1.19.x | The Wild Update | Medium | 17 |
| 1.18.x | Caves & Cliffs Part 2 | Medium | 17 |

### 5.3 Stonecutter Configuration

```kotlin
// stonecutter.gradle.kts
stonecutter {
    versions("1.21.4", "1.20.6", "1.20.1", "1.19.4", "1.18.2")

    // Version-specific swaps
    swap("DrawContext") {
        "1.20" to "DrawContext"
        "1.19" to "MatrixStack"
    }
}
```

### 5.4 Version-Specific Code Example

```java
//? if >=1.20 {
import net.minecraft.client.gui.DrawContext;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;*/
//?}
```

---

## Phase 6: Multi-Loader Architecture (Architectury + Stonecraft)

### 6.1 Target Client-Side Mod Loaders

| Loader | Support | Notes |
|--------|---------|-------|
| Fabric | Primary | Current implementation |
| Quilt | High | Fabric-compatible, minimal changes |
| NeoForge | High | Modern Forge fork (1.20.4+) |
| Forge | Medium | Legacy support (1.20.1 and below) |

### 6.2 Chosen Architecture: Stonecraft

Using [Stonecraft](https://stonecraft.meza.gg/) which combines:
- **Stonecutter** for multi-version support
- **Architectury** for multi-loader abstraction

Benefits:
- Reduces ~500 lines of build configuration to a single plugin
- Handles Fabric, Forge, NeoForge automatically
- Tested and versioned plugin

### 6.3 Project Structure

```
critical-flight-details/
├── .github/workflows/
│   ├── build.yml
│   └── release.yml
├── src/
│   └── main/
│       ├── java/net/critical/flight_display/
│       │   ├── FlightDisplayClient.java     # Entry point (platform-specific)
│       │   ├── FlightDisplayCommon.java     # Shared initialization
│       │   ├── hud/
│       │   │   └── FlightDisplayHud.java    # HUD logic with version swaps
│       │   └── platform/
│       │       └── PlatformHelper.java      # Platform abstraction
│       └── resources/
│           ├── fabric.mod.json              # Fabric metadata
│           ├── META-INF/mods.toml           # Forge/NeoForge metadata
│           └── assets/flight_display/
├── versions/                                # Stonecutter version configs
│   ├── 1.21.4-fabric/
│   ├── 1.21.4-neoforge/
│   ├── 1.20.6-fabric/
│   ├── 1.20.6-forge/
│   └── ...
├── build.gradle.kts
├── settings.gradle.kts
├── stonecutter.gradle.kts
├── gradle.properties
└── CLAUDE.md
```

### 6.4 Platform Abstraction Layer

```java
// PlatformHelper.java - Architectury provides this
public class PlatformHelper {
    public static boolean isFabric() {
        return Platform.isFabric();
    }

    public static boolean isForge() {
        return Platform.isForge();
    }

    public static Path getConfigDir() {
        return Platform.getConfigFolder();
    }
}
```

### 6.5 Platform-Specific Entry Points

**Fabric/Quilt:**
```java
public class FlightDisplayFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FlightDisplayCommon.init();
    }
}
```

**Forge/NeoForge:**
```java
@Mod("flight_display")
public class FlightDisplayForge {
    public FlightDisplayForge() {
        FMLJavaModLoadingContext.get().getModEventBus()
            .addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        FlightDisplayCommon.init();
    }
}
```

### 6.6 HUD Rendering with Event Systems

**Fabric:** Mixin injection into InGameHud
**Forge/NeoForge:** RenderGuiEvent subscriber

```java
// Common HUD rendering logic
public class FlightDisplayHud {
    public void render(/* platform-agnostic context */) {
        //? if fabric {
        // Fabric-specific rendering
        //?} else if forge || neoforge {
        /*// Forge-specific rendering*/
        //?}
    }
}
```

---

## Phase 7: Implementation Roadmap

### Stage 1: Foundation ✅ COMPLETED
1. ~~Set up branch structure~~
2. ~~Upgrade to latest Minecraft/Fabric~~
3. ~~Get mod working on 1.21.4~~
4. ~~Add CI/CD~~
5. ~~Add testing framework~~

### Stage 2: Multi-Loader ✅ COMPLETED
6. [x] Set up Stonecraft/Architectury project structure
7. [x] Create platform abstraction layer
8. [x] Implement Fabric loader module
9. [x] Implement NeoForge loader module
10. [x] Implement Forge loader module (1.20.1 and earlier)
11. [x] Update CI/CD for multi-loader builds

### Stage 3: Multi-Version ✅ COMPLETED
12. [x] Configure Stonecutter for version management
13. [x] Add 1.20.x support (1.20.6, 1.20.1)
14. [x] Add 1.19.x support (1.19.4 with MatrixStack)
15. [x] Add 1.18.x support (1.18.2 with MatrixStack)
16. [ ] Test all version/loader combinations (pending CI)

### Stage 4: Polish (Next)
17. [ ] Add configuration system (Cloth Config)
18. [ ] Add planned features (altitude, compass)
19. [ ] Documentation and release
20. [ ] Publish to CurseForge and Modrinth

---

## Appendix A: Key Resources

### Documentation
- [Stonecutter Docs](https://stonecutter.kikugie.dev/)
- [Stonecraft](https://stonecraft.meza.gg/)
- [Architectury Docs](https://docs.architectury.dev/)
- [Fabric Documentation](https://docs.fabricmc.net/)
- [NeoForge Docs](https://docs.neoforged.net/)
- [mc-publish Action](https://github.com/marketplace/actions/mc-publish)

### Publishing Platforms
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods)
- [Modrinth](https://modrinth.com/mods)

### Example Projects Using Stonecraft/Stonecutter
- Check Stonecraft GitHub for templates and examples

---

## Appendix B: Version Compatibility Matrix

| MC Version | Fabric | NeoForge | Forge | Java |
|------------|--------|----------|-------|------|
| 1.21.4 | ✅ | ✅ | ❌ | 21 |
| 1.21.1 | ✅ | ✅ | ❌ | 21 |
| 1.20.6 | ✅ | ✅ | ❌ | 21 |
| 1.20.4 | ✅ | ✅ | ❌ | 17 |
| 1.20.1 | ✅ | ❌ | ✅ | 17 |
| 1.19.4 | ✅ | ❌ | ✅ | 17 |
| 1.18.2 | ✅ | ❌ | ✅ | 17 |

Note: NeoForge split from Forge at 1.20.2. Versions before that only have Forge.

---

## Appendix C: Rendering API Changes by Version

| Version | Rendering API | Notes |
|---------|--------------|-------|
| 1.21.x | DrawContext | Current standard |
| 1.20.x | DrawContext | Introduced in 1.20 |
| 1.19.x | MatrixStack | Pre-DrawContext |
| 1.18.x | MatrixStack | Similar to 1.19 |

Key method changes:
- `context.drawText()` (1.20+) → `textRenderer.draw(matrixStack, ...)` (1.19-)
- `BufferRenderer.drawWithGlobalProgram()` (1.20+) → `tessellator.draw()` (1.19-)
