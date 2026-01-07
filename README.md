# Critical Flight Display

[![Build Status](https://img.shields.io/github/actions/workflow/status/bshuler/critical-flight-details/build.yml?branch=main&style=flat-square&logo=github)](https://github.com/bshuler/critical-flight-details/actions/workflows/build.yml)
[![GitHub Release](https://img.shields.io/github/v/release/bshuler/critical-flight-details?style=flat-square&logo=github)](https://github.com/bshuler/critical-flight-details/releases/latest)
[![License](https://img.shields.io/github/license/bshuler/critical-flight-details?style=flat-square)](LICENSE)

### Supported Minecraft Versions

[![MC 1.21.4](https://img.shields.io/badge/MC-1.21.4-brightgreen?style=flat-square)](https://github.com/bshuler/critical-flight-details/releases)
[![MC 1.20.6](https://img.shields.io/badge/MC-1.20.6-green?style=flat-square)](https://github.com/bshuler/critical-flight-details/releases)
[![MC 1.20.1](https://img.shields.io/badge/MC-1.20.1-green?style=flat-square)](https://github.com/bshuler/critical-flight-details/releases)
[![MC 1.19.4](https://img.shields.io/badge/MC-1.19.4-yellowgreen?style=flat-square)](https://github.com/bshuler/critical-flight-details/releases)
[![MC 1.18.2](https://img.shields.io/badge/MC-1.18.2-yellowgreen?style=flat-square)](https://github.com/bshuler/critical-flight-details/releases)

### Supported Mod Loaders

[![Fabric](https://img.shields.io/badge/Fabric-supported-blue?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAA5ElEQVQoz62SvQ3CMBCFHcQKsAILsABLsAArQEVBQUNDQ0lDQ0FDQ0lJSQMNNDCCRyDxz7vIFpYSKh5y8d1nvzv7DBH95wCbqDcAHwARxQBsAM6IuIsxduBT1VuMcUtEcSb6DFEXInK99wOA6g8BapGiVL3NiojoRH0BeGqtUymlu4i8ABDRunROANg7504p5buI7AM4moBxgE1mDr+glJqccxvr/S6E8GIppb1z7gxgG0JYIuLYJkBEWGvnfd/PTDfBOXcIISwAgJkXhBCmzLzIyqf0Bf+BmaeIOCWiGRHd/tTqDX8eFpfXoCdMAAAAAElFTkSuQmCC)](https://fabricmc.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-supported-orange?style=flat-square)](https://neoforged.net/)
[![Forge](https://img.shields.io/badge/Forge-supported-red?style=flat-square)](https://files.minecraftforge.net/)

### Requirements

[![Java 21](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://adoptium.net/)
[![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://adoptium.net/)

---

A client-side Minecraft mod that enhances the HUD with flight information while using an Elytra. Inspired by real aircraft instrumentation.

![In-game HUD Example](images/ingame_hud.png)

## Features

- **Pitch Indicator** - Visual pitch display with degree markers
- **Horizon Lines** - Vertical reference lines for orientation
- **Speed Display** - Current velocity in blocks per second
- **Altitude Display** - Absolute Y coordinate (MSL) and height above ground (AGL)
- **Compass/Heading** - Cardinal directions with degree heading
- **Fully Configurable** - Toggle any element, customize colors and positions

## Version Compatibility

| Minecraft | Fabric | NeoForge | Forge | Java |
|-----------|--------|----------|-------|------|
| 1.21.4    | ✅     | ✅       | -     | 21   |
| 1.20.6    | ✅     | ✅       | -     | 21   |
| 1.20.1    | ✅     | -        | ✅    | 17   |
| 1.19.4    | ✅     | -        | ✅    | 17   |
| 1.18.2    | ✅     | -        | ✅    | 17   |

> **Note:** NeoForge split from Forge at MC 1.20.2. Versions 1.20.1 and earlier use Forge.

---

## Installation

### Fabric
1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the Fabric version of the mod from [Releases](https://github.com/bshuler/critical-flight-details/releases/latest)
4. Place the `.jar` in your `.minecraft/mods` folder

### NeoForge (1.20.5+)
1. Install [NeoForge](https://neoforged.net/)
2. Download the NeoForge version from [Releases](https://github.com/bshuler/critical-flight-details/releases/latest)
3. Place the `.jar` in your `.minecraft/mods` folder

### Forge (1.20.1 and earlier)
1. Install [Forge](https://files.minecraftforge.net/)
2. Download the Forge version from [Releases](https://github.com/bshuler/critical-flight-details/releases/latest)
3. Place the `.jar` in your `.minecraft/mods` folder

---

## Configuration

The mod uses a JSON configuration file at `.minecraft/config/flight_display.json`:

```json
{
  "showPitchIndicator": true,
  "showSpeedDisplay": true,
  "showHorizonLines": true,
  "showAltitudeDisplay": true,
  "showHeadingDisplay": true,
  "hudLeftPosition": 0.333,
  "hudRightPosition": 0.667,
  "pitchIndicatorColor": -65536,
  "horizonLineColor": -16711936,
  "textColor": -65536,
  "altitudeColor": -256,
  "headingColor": -16711681,
  "showTextShadow": true,
  "speedUpdateInterval": 10,
  "showAltitudeAboveGround": true,
  "showAltitudeAbsolute": true
}
```

Delete the config file to reset to defaults. Changes take effect on game restart.

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/bshuler/critical-flight-details.git
cd critical-flight-details

# Build all versions (Fabric + NeoForge + Forge × all MC versions)
./gradlew chiseledBuild

# Build only the active version (default: 1.21.4-fabric)
./gradlew build

# Run the client for testing
./gradlew runClient
```

Built JARs are located in `versions/*/build/libs/`.

---

## FAQ

**Does this mod work on servers?**
> Yes, it's entirely client-side. No server installation required.

**Will this get me banned from multiplayer servers?**
> The mod only displays information already available to you (coordinates, direction). It's similar to a simplified F3 menu. However, always check server rules before using any mods.

**How do I switch between loader/version builds?**
> Each release includes separate JARs for each loader and Minecraft version. Download the one matching your setup.

---

## License

[MIT License](LICENSE)

---

## Links

- [GitHub Releases](https://github.com/bshuler/critical-flight-details/releases)
- [Issue Tracker](https://github.com/bshuler/critical-flight-details/issues)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/critical-flight-details) *(coming soon)*
- [Modrinth](https://modrinth.com/mod/critical-flight-display) *(coming soon)*
