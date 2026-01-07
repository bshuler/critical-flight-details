# CLAUDE.md - AI Assistant Guide for Critical Flight Display

## Project Overview

**Critical Flight Display** is a multi-loader Minecraft mod that enhances the game's HUD with flight information while using an Elytra. The mod is client-side only and displays:
- Pitch indicator with visual horizon lines
- Speed display (in blocks per second)
- Visual reference lines for orientation

**Supported Loaders:** Fabric, NeoForge (via Stonecraft/Architectury)
**Target Minecraft Version:** 1.21.4

## Build System: Stonecraft + Stonecutter

This project uses [Stonecraft](https://stonecraft.meza.gg/) for multi-loader, multi-version support:
- **Stonecutter**: Preprocessor for version-specific code paths
- **Architectury**: Platform abstraction layer
- **Single codebase**: One source tree builds for all platforms

### Preprocessor Syntax

```java
//? if fabric {
// Fabric-specific code here
//?} else if neoforge {
/*
// NeoForge-specific code here
*///?}
```

## Repository Structure

```
critical-flight-details/
├── src/main/
│   ├── java/net/critical/flight_display/
│   │   ├── FlightDisplayClient.java     # Entry point (multi-loader)
│   │   ├── NeoForgeHudRenderer.java     # NeoForge event handler
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
│       └── 1.21.4.properties            # Version-specific deps
├── .github/workflows/
│   ├── build.yml                        # Multi-loader CI build
│   └── release.yml                      # Multi-loader release
├── build.gradle.kts                     # Stonecraft build config
├── settings.gradle.kts                  # Stonecutter setup
├── stonecutter.gradle.kts               # Active version selector
├── gradle.properties                    # Mod metadata
└── PLAN.md                              # Implementation roadmap
```

## Build Commands

```bash
# Build active version (1.21.4-fabric by default)
./gradlew build

# Build ALL versions (Fabric + NeoForge)
./gradlew chiseledBuild

# Run Minecraft client with mod
./gradlew runClient

# Run tests
./gradlew test

# Switch active version
./gradlew "Set active project to 1.21.4-neoforge"
```

## Key Technical Patterns

### Platform-Specific Entry Points

**Fabric:**
- Entry: `FlightDisplayClient implements ClientModInitializer`
- HUD: Mixin injection into `InGameHud.render()`

**NeoForge:**
- Entry: `FlightDisplayClient` with `@Mod` annotation
- HUD: Event subscriber for `RenderGuiLayerEvent.Post`

### HUD Rendering (Multi-Loader)

The HUD uses platform-specific rendering APIs:
- **Fabric**: `DrawContext`, `MinecraftClient`, Yarn mappings
- **NeoForge**: `GuiGraphics`, `Minecraft`, Mojang mappings

Stonecutter preprocessor comments handle the differences.

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

| Component | Fabric | NeoForge |
|-----------|--------|----------|
| Minecraft | 1.21.4 | 1.21.4 |
| Loader | 0.16.10 | 21.4.0 |
| API | Fabric API 0.114.0 | - |
| Mappings | Yarn | Mojang |

## Important Notes for AI Assistants

1. **Client-Side Only**: No server-side code needed

2. **Multi-Loader Code**: Use Stonecutter preprocessor:
   ```java
   //? if fabric {
   // Fabric code
   //?} else if neoforge {
   /*// NeoForge code*///?}
   ```

3. **Rendering Differences**:
   - Fabric: `DrawContext`, `textRenderer`, `world`
   - NeoForge: `GuiGraphics`, `font`, `level`

4. **Event Systems**:
   - Fabric: Mixin injection
   - NeoForge: `@SubscribeEvent` annotations

5. **Active Version**: Check `stonecutter.gradle.kts` for current build target

6. **Testing**: Tests run on the active version only

## Testing

### Manual Testing
1. Run `./gradlew runClient`
2. Enter a world, equip Elytra, fly
3. Verify HUD elements appear

### Switching Loaders for Testing
```bash
# Switch to NeoForge
./gradlew "Set active project to 1.21.4-neoforge"
./gradlew runClient
```

## License

MIT License - See LICENSE file
