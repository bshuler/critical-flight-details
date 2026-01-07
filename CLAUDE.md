# CLAUDE.md - AI Assistant Guide for Critical Flight Display

## Project Overview

**Critical Flight Display** is a Minecraft Fabric mod that enhances the game's HUD with flight information while using an Elytra. The mod is client-side only and displays:
- Pitch indicator with visual horizon lines
- Speed display calculated from player movement
- Visual reference lines for orientation

**Target Minecraft Version:** 1.16.1 (with Fabric Loader 0.8.8+)

## Repository Structure

```
critical-flight-details/
├── src/main/
│   ├── java/net/critical/flight_display/
│   │   ├── FlightDisplay.java          # Main mod entry point (ModInitializer)
│   │   ├── hud/
│   │   │   └── FlightDisplayHud.java   # HUD rendering logic (pitch, speed, lines)
│   │   └── mixin/
│   │       └── GameInfoMixin.java      # Mixin to inject HUD into InGameHud
│   └── resources/
│       ├── fabric.mod.json             # Mod metadata and entry points
│       ├── flight_display.mixins.json  # Mixin configuration
│       └── assets/modid/               # Mod icon assets (elytra.png, elytra.ico)
├── build.gradle                        # Gradle build configuration with fabric-loom
├── gradle.properties                   # Version numbers and mod properties
├── settings.gradle                     # Plugin repository configuration
└── .github/workflows/gradlepublish.yml # CI for publishing to GitHub Packages
```

## Build Commands

```bash
# Build the mod JAR
./gradlew build

# Output location: build/libs/critical-flight-details-<version>.jar

# Refresh dependencies after changing versions
./gradlew --refresh-dependencies

# Remap mixin locations when updating Minecraft version
./gradlew migrateMappings --mappings "<minecraft_version>+build.<build_number>"

# Run the Minecraft client with the mod
./gradlew runClient

# Publish to GitHub Packages (requires USERNAME and TOKEN env vars)
./gradlew publish
```

## Key Technical Patterns

### Mixin System
- The mod uses SpongePowered Mixin to inject code into Minecraft's `InGameHud` class
- `GameInfoMixin.java` injects at two points:
  1. `<init>` constructor - initializes the FlightDisplayHud
  2. `render` method - draws the HUD when player is Elytra flying (`isFallFlying()`)
- Mixins are registered in `flight_display.mixins.json`
- All mixins are client-side only (`@Environment(EnvType.CLIENT)`)

### HUD Rendering
- `FlightDisplayHud` implements `Drawable` for rendering
- Uses OpenGL via LWJGL for line drawing (`GL11.GL_LINES`)
- Uses Minecraft's `TextRenderer` for text display
- Speed calculation based on 3D distance traveled over game ticks

### Entry Point
- Main class `FlightDisplay` implements `ModInitializer`
- Registered in `fabric.mod.json` under `entrypoints.main`

## Development Conventions

### Code Style
- Java 8 compatibility required (`sourceCompatibility = JavaVersion.VERSION_1_8`)
- Package structure: `net.critical.flight_display`
- Client-only code must use `@Environment(EnvType.CLIENT)` annotation

### Version Management
- Mod version defined in `gradle.properties` as `mod_version`
- Minecraft/Fabric versions also in `gradle.properties`
- Version is injected into `fabric.mod.json` via Gradle's `processResources`

### Naming Conventions
- Mixin classes suffixed with `Mixin` (e.g., `GameInfoMixin`)
- HUD components in `hud` subpackage

## Dependencies

| Dependency | Purpose |
|------------|---------|
| Minecraft 1.16.1 | Target game version |
| Yarn mappings 1.16.1+build.9 | Deobfuscation mappings |
| Fabric Loader 0.8.8+ | Mod loader |
| Fabric API 0.14.0 | Fabric utilities |

## Important Notes for AI Assistants

1. **Client-Side Only**: This mod only runs on the client. All code modifying game behavior must be annotated with `@Environment(EnvType.CLIENT)`

2. **Mixin Injection Points**: When modifying where the HUD renders, be careful with the injection point. The current target is `FIELD` at `hudHidden:Z` ordinal 2

3. **Rendering Context**: The HUD only renders when `minecraftClient.player.isFallFlying()` is true (during Elytra flight)

4. **No Server Requirements**: The mod does not require server-side installation

5. **Speed Calculation**: Speed is calculated every 10 game ticks using Euclidean distance formula

6. **Planned Features** (from README):
   - Toggle HUD utilities with hotkeys/settings
   - Altitude indicators
   - Compass display

## Testing

Manual testing is required:
1. Run `./gradlew runClient`
2. Enter a Minecraft world in Creative/Survival mode
3. Equip Elytra and start flying
4. Verify HUD elements appear (pitch, speed, horizon lines)

## License

MIT License - See LICENSE file
