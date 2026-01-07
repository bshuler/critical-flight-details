# CLAUDE.md - AI Assistant Guide for Critical Flight Display

## Project Overview

**Critical Flight Display** is a Minecraft Fabric mod that enhances the game's HUD with flight information while using an Elytra. The mod is client-side only and displays:
- Pitch indicator with visual horizon lines
- Speed display (in blocks per second)
- Visual reference lines for orientation

**Target Minecraft Version:** 1.21.4 (with Fabric Loader 0.16.10+)

## Repository Structure

```
critical-flight-details/
├── src/main/
│   ├── java/net/critical/flight_display/
│   │   ├── FlightDisplayClient.java    # Client mod entry point (ClientModInitializer)
│   │   ├── hud/
│   │   │   └── FlightDisplayHud.java   # HUD rendering logic (DrawContext API)
│   │   └── mixin/
│   │       └── InGameHudMixin.java     # Mixin to inject HUD into InGameHud
│   └── resources/
│       ├── fabric.mod.json             # Mod metadata and entry points
│       ├── flight_display.mixins.json  # Mixin configuration
│       └── assets/flight_display/      # Mod assets (icon.png)
├── .github/workflows/
│   ├── build.yml                       # CI build on push/PR
│   └── release.yml                     # Deploy to CurseForge/Modrinth on tag
├── build.gradle                        # Gradle build with Fabric Loom 1.9
├── gradle.properties                   # Version numbers and mod properties
├── settings.gradle                     # Plugin repository configuration
├── CHANGELOG.md                        # Version history
├── PLAN.md                             # Multi-platform expansion roadmap
└── LICENSE                             # MIT License
```

## Build Commands

```bash
# Build the mod JAR
./gradlew build

# Output location: build/libs/critical-flight-details-<version>.jar

# Refresh dependencies after changing versions
./gradlew --refresh-dependencies

# Run the Minecraft client with the mod
./gradlew runClient

# Run tests
./gradlew test

# Publish to GitHub Packages
./gradlew publish
```

## Key Technical Patterns

### Mixin System
- Uses SpongePowered Mixin to inject into Minecraft's `InGameHud` class
- `InGameHudMixin.java` injects at two points:
  1. `<init>` constructor - initializes the FlightDisplayHud
  2. `render` method tail - draws HUD when player is Elytra flying
- Mixins are registered in `flight_display.mixins.json`
- All mixins use `@Environment(EnvType.CLIENT)` annotation

### HUD Rendering (Modern API)
- Uses `DrawContext` for all rendering (replaced MatrixStack in 1.20+)
- Uses `RenderSystem` and `BufferRenderer` for line drawing
- Uses `context.drawText()` for text display
- Speed calculated as blocks per second (distance / ticks * 20)

### Entry Point
- Client entry point: `FlightDisplayClient` implements `ClientModInitializer`
- Registered in `fabric.mod.json` under `entrypoints.client`

## Development Conventions

### Code Style
- **Java 21** required (modern LTS version)
- Package structure: `net.critical.flight_display`
- Client-only code uses `@Environment(EnvType.CLIENT)` annotation
- Use `@Unique` for mixin fields to avoid conflicts

### Version Management
- All versions defined in `gradle.properties`
- Version injected into `fabric.mod.json` via Gradle's `processResources`
- Changelog maintained in `CHANGELOG.md`

### Naming Conventions
- Mixin classes suffixed with `Mixin` (e.g., `InGameHudMixin`)
- Client initializers suffixed with `Client`
- HUD components in `hud` subpackage

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Minecraft | 1.21.4 | Target game version |
| Yarn Mappings | 1.21.4+build.8 | Deobfuscation mappings |
| Fabric Loader | 0.16.10+ | Mod loader |
| Fabric API | 0.114.0+1.21.4 | Fabric utilities |
| Gradle | 8.11.1 | Build system |
| Fabric Loom | 1.9-SNAPSHOT | Gradle plugin |

## CI/CD Pipeline

### Build Workflow (`build.yml`)
- Triggers on push/PR to `dev`, `test`, `main` branches
- Builds with Java 21
- Runs tests
- Uploads build artifacts

### Release Workflow (`release.yml`)
- Triggers on tags starting with `v*` pushed to `main`
- Creates GitHub Release
- Publishes to CurseForge and Modrinth via [mc-publish](https://github.com/Kir-Antipov/mc-publish)

### Required Secrets
```
MODRINTH_ID      - Modrinth project ID
MODRINTH_TOKEN   - Modrinth API token
CURSEFORGE_ID    - CurseForge project ID
CURSEFORGE_TOKEN - CurseForge API token
```

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production releases (triggers deployment) |
| `test` | Integration testing |
| `dev`  | Active development |

## Important Notes for AI Assistants

1. **Client-Side Only**: All code must be annotated with `@Environment(EnvType.CLIENT)`

2. **Modern Rendering**: Use `DrawContext` API, not deprecated `MatrixStack` methods

3. **Mixin Injection**: Current injection uses `@At("TAIL")` on the render method

4. **Rendering Context**: HUD only renders when `player.isFallFlying()` is true

5. **No Server Requirements**: This mod does not require server-side installation

6. **Speed Calculation**: Calculated every 10 ticks using Euclidean distance, displayed as blocks/second

7. **Planned Features** (see PLAN.md):
   - Multi-loader support (Forge, NeoForge, Quilt)
   - Multi-version support (1.16 - 1.21+)
   - Configuration system
   - Altitude indicators and compass

## Testing

### Manual Testing
1. Run `./gradlew runClient`
2. Enter a Minecraft world in Creative/Survival mode
3. Equip Elytra and start flying (jump from height or use fireworks)
4. Verify HUD elements appear:
   - Pitch indicator on left
   - Speed display below pitch
   - Vertical reference lines

### Automated Testing
- Unit tests in `src/test/java/` (planned)
- Game tests using Fabric's gametest API (planned)

## License

MIT License - See LICENSE file
