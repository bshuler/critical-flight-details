# CLAUDE.md - AI Assistant Guide for Critical Flight Display

## Project Overview

**Critical Flight Display** is a multi-loader Minecraft mod that enhances the game's HUD with flight information while using an Elytra. The mod is client-side only and displays:
- Pitch indicator with visual horizon lines
- Speed display (in blocks per second)
- Altitude display (absolute Y and height above ground)
- Compass/heading display with cardinal directions
- Visual reference lines for orientation

**Supported Loaders:** Fabric, NeoForge, Forge (via Stonecraft/Stonecutter)
**Supported Minecraft Versions:** 1.21.4, 1.20.6, 1.20.1, 1.19.4, 1.18.2

## Build System: Stonecraft + Stonecutter

This project uses [Stonecraft](https://stonecraft.meza.gg/) for multi-loader, multi-version support:
- **Stonecutter**: Preprocessor for version-specific code paths
- **Architectury**: Platform abstraction layer
- **Single codebase**: One source tree builds for all platforms

### Preprocessor Syntax

**Loader conditionals:**
```java
//? if fabric {
// Fabric-specific code here
//?} else if neoforge {
/*
// NeoForge-specific code here
*///?} else if forge {
/*
// Forge-specific code here (pre-1.20.5)
*///?}
```

**Version conditionals:**
```java
//? if >=1.21 {
// Code for 1.21+ (uses RenderTickCounter)
//?} else {
/*
// Code for 1.20.x (uses float tickDelta)
*///?}
```

## Repository Structure

```
critical-flight-details/
├── src/main/
│   ├── java/net/critical/flight_display/
│   │   ├── FlightDisplayClient.java     # Entry point (multi-loader)
│   │   ├── NeoForgeHudRenderer.java     # NeoForge event handler
│   │   ├── ForgeHudRenderer.java        # Forge event handler (1.20.1)
│   │   ├── config/
│   │   │   └── FlightDisplayConfig.java # Configuration (JSON-based)
│   │   ├── hud/
│   │   │   └── FlightDisplayHud.java    # HUD rendering (multi-loader)
│   │   └── mixin/
│   │       └── InGameHudMixin.java      # Fabric mixin
│   └── resources/
│       ├── fabric.mod.json              # Fabric metadata
│       ├── META-INF/neoforge.mods.toml  # NeoForge metadata
│       ├── flight_display.mixins.json   # Mixin configuration
│       └── assets/flight_display/
├── versions/
│   └── dependencies/
│       ├── 1.21.4.properties            # 1.21.4 dependencies
│       ├── 1.20.6.properties            # 1.20.6 dependencies
│       ├── 1.20.1.properties            # 1.20.1 dependencies (Forge)
│       ├── 1.19.4.properties            # 1.19.4 dependencies (MatrixStack)
│       └── 1.18.2.properties            # 1.18.2 dependencies (MatrixStack)
├── .github/workflows/
│   ├── build.yml                        # Multi-loader CI build
│   └── release.yml                      # Multi-version release
├── build.gradle.kts                     # Stonecraft build config
├── settings.gradle.kts                  # Stonecutter version matrix
├── stonecutter.gradle.kts               # Active version selector
├── gradle.properties                    # Mod metadata
└── PLAN.md                              # Implementation roadmap
```

## Build Commands

```bash
# Build active version (1.21.4-fabric by default)
./gradlew build

# Build ALL versions (all loaders × all MC versions)
./gradlew chiseledBuild

# Run Minecraft client with mod
./gradlew runClient

# Run tests
./gradlew test

# Switch active version (examples)
./gradlew "Set active project to 1.21.4-neoforge"
./gradlew "Set active project to 1.20.6-fabric"
./gradlew "Set active project to 1.20.1-forge"
```

## Version Matrix

| MC Version | Fabric | NeoForge | Forge | Rendering API |
|------------|--------|----------|-------|---------------|
| 1.21.4     | ✓      | ✓        | -     | DrawContext   |
| 1.20.6     | ✓      | ✓        | -     | DrawContext   |
| 1.20.1     | ✓      | -        | ✓     | DrawContext   |
| 1.19.4     | ✓      | -        | ✓     | MatrixStack   |
| 1.18.2     | ✓      | -        | ✓     | MatrixStack   |

**Notes:**
- NeoForge split from Forge after 1.20.1. Versions 1.20.2-1.20.4 had transitional support.
- DrawContext was introduced in 1.20. Earlier versions use MatrixStack.
- 1.19.4 and 1.18.2 use `RenderGameOverlayEvent` (Forge) instead of `RenderGuiOverlayEvent`.

## Key Technical Patterns

### Platform-Specific Entry Points

**Fabric:**
- Entry: `FlightDisplayClient implements ClientModInitializer`
- HUD: Mixin injection into `InGameHud.render()`

**NeoForge (1.20.5+):**
- Entry: `FlightDisplayClient` with `@Mod` annotation
- HUD: Event subscriber for `RenderGuiLayerEvent.Post`

**Forge (1.20.1 and earlier):**
- Entry: `FlightDisplayClient` with `@Mod` annotation
- HUD: Event subscriber for `RenderGuiOverlayEvent.Post`

### HUD Rendering (Multi-Loader)

The HUD uses platform-specific rendering APIs:
- **Fabric**: `DrawContext`, `MinecraftClient`, Yarn mappings
- **NeoForge/Forge**: `GuiGraphics`, `Minecraft`, Mojang mappings

### Version-Specific API Differences

| API | 1.21+ | 1.20.x |
|-----|-------|--------|
| Render method | `RenderTickCounter` | `float tickDelta` |
| Draw text | `drawText()` | `drawText()` / `drawString()` |
| Player pitch | `getPitch(1.0f)` | `getPitch(1.0f)` / `getXRot()` |
| World time | `world.getTime()` | `world.getTime()` / `level.getGameTime()` |

Stonecutter preprocessor comments handle all differences automatically.

### Mixin System (Fabric Only)

- Mixins inject into `InGameHud` class
- Registered in `flight_display.mixins.json`
- NeoForge uses events instead

## Development Conventions

### Code Style
- **Java 21** required
- Package: `net.critical.flight_display`
- Use `//? if loader {` preprocessor for platform code
- Fabric annotations: `@Environment(EnvType.CLIENT)`
- NeoForge annotations: `@OnlyIn(Dist.CLIENT)`

### Version Management
- Mod metadata in `gradle.properties`
- Version-specific deps in `versions/dependencies/*.properties`
- Changelog in `CHANGELOG.md`

## Configuration

The mod uses a simple JSON configuration file stored in the config directory:
- **Fabric**: `.minecraft/config/flight_display.json`
- **Forge/NeoForge**: `.minecraft/config/flight_display.json`

### Configuration Options

```json
{
  "showPitchIndicator": true,     // Show pitch indicator and hash marks
  "showSpeedDisplay": true,       // Show speed display
  "showHorizonLines": true,       // Show vertical reference lines
  "showAltitudeDisplay": true,    // Show altitude display
  "showHeadingDisplay": true,     // Show compass/heading display
  "hudLeftPosition": 0.333,       // Left edge position (0.0-1.0)
  "hudRightPosition": 0.667,      // Right edge position (0.0-1.0)
  "pitchIndicatorColor": -65536,  // Pitch indicator color (ARGB)
  "horizonLineColor": -16711936,  // Horizon line color (ARGB)
  "textColor": -65536,            // Text color (ARGB)
  "altitudeColor": -256,          // Altitude display color (ARGB, yellow)
  "headingColor": -16711681,      // Heading display color (ARGB, cyan)
  "showTextShadow": true,         // Show shadow behind text
  "speedUpdateInterval": 10,      // Ticks between speed updates
  "showAltitudeAboveGround": true,// Show AGL (height above ground)
  "showAltitudeAbsolute": true    // Show absolute Y coordinate
}
```

### Configuration Class
- Located at: `net.critical.flight_display.config.FlightDisplayConfig`
- Uses Gson for JSON serialization
- Supports live reload via `FlightDisplayConfig.reload()`

## CI/CD Pipeline

### Build Workflow (`build.yml`)
- Triggers on push/PR to dev/test/main
- Builds all loaders with `chiseledBuild`
- Uploads separate artifacts for Fabric and NeoForge

### Release Workflow (`release.yml`)
- Triggers on version tags (v*)
- Publishes Fabric and NeoForge separately
- Deploys to CurseForge and Modrinth

### Required Secrets
```
MODRINTH_ID      - Modrinth project ID
MODRINTH_TOKEN   - Modrinth API token
CURSEFORGE_ID    - CurseForge project ID
CURSEFORGE_TOKEN - CurseForge API token
```

## Dependencies

Version-specific dependencies are stored in `versions/dependencies/`:

**1.21.4:**
| Component | Fabric | NeoForge |
|-----------|--------|----------|
| Loader | 0.16.10 | 21.4.0 |
| API | Fabric API 0.114.0 | - |
| Java | 21 | 21 |

**1.20.6:**
| Component | Fabric | NeoForge |
|-----------|--------|----------|
| Loader | 0.15.11 | 20.6.119 |
| API | Fabric API 0.100.0 | - |
| Java | 21 | 21 |

**1.20.1:**
| Component | Fabric | Forge |
|-----------|--------|-------|
| Loader | 0.15.11 | 47.3.0 |
| API | Fabric API 0.92.2 | - |
| Java | 17 | 17 |

**1.19.4 (MatrixStack):**
| Component | Fabric | Forge |
|-----------|--------|-------|
| Loader | 0.15.11 | 45.3.0 |
| API | Fabric API 0.87.2 | - |
| Java | 17 | 17 |

**1.18.2 (MatrixStack):**
| Component | Fabric | Forge |
|-----------|--------|-------|
| Loader | 0.15.11 | 40.2.21 |
| API | Fabric API 0.77.0 | - |
| Java | 17 | 17 |

## Important Notes for AI Assistants

1. **Client-Side Only**: No server-side code needed

2. **Multi-Loader Code**: Use Stonecutter preprocessor:
   ```java
   //? if fabric {
   // Fabric code
   //?} else if neoforge {
   /*// NeoForge code*///?} else if forge {
   /*// Forge code (1.20.1 and earlier)*///?}
   ```

3. **Multi-Version Code**: Use version conditionals:
   ```java
   //? if >=1.21 {
   // 1.21+ API
   //?} else {
   /*// 1.20.x API*///?}
   ```

4. **Rendering Differences**:
   - Fabric: `DrawContext`, `textRenderer`, `world`
   - NeoForge/Forge: `GuiGraphics`, `font`, `level`

5. **Event Systems**:
   - Fabric: Mixin injection
   - NeoForge: `RenderGuiLayerEvent` + `@SubscribeEvent`
   - Forge: `RenderGuiOverlayEvent` + `@SubscribeEvent`

6. **Active Version**: Check `stonecutter.gradle.kts` for current build target

7. **Testing**: Tests run on the active version only

8. **Forge vs NeoForge**: NeoForge split from Forge after 1.20.1. Use Forge for 1.20.1 and earlier.

## Testing

### Manual Testing
1. Run `./gradlew runClient`
2. Enter a world, equip Elytra, fly
3. Verify HUD elements appear

### Testing Different Versions/Loaders
```bash
# Test NeoForge 1.21.4
./gradlew "Set active project to 1.21.4-neoforge"
./gradlew runClient

# Test Fabric 1.20.6
./gradlew "Set active project to 1.20.6-fabric"
./gradlew runClient

# Test Forge 1.20.1
./gradlew "Set active project to 1.20.1-forge"
./gradlew runClient
```

### Build Matrix Test
```bash
# Build all versions to verify compilation
./gradlew chiseledBuild
```

## License

MIT License - See LICENSE file
