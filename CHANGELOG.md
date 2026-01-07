# Changelog

All notable changes to Critical Flight Display will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2025-01-07

### Changed
- **Major Update**: Upgraded to Minecraft 1.21.4
- Updated to Java 21 (from Java 8)
- Updated Fabric Loader to 0.16.10
- Updated Fabric API to 0.114.0+1.21.4
- Modernized rendering pipeline using DrawContext API
- Improved speed calculation (now displays blocks per second)
- Changed mod to client-only initialization

### Fixed
- Fixed rendering compatibility with modern Minecraft versions
- Fixed deprecated API usage

### Technical
- Upgraded Gradle from 6.5 to 8.11.1
- Upgraded Fabric Loom from 0.4-SNAPSHOT to 1.9-SNAPSHOT
- Migrated from jcenter (deprecated) to mavenCentral
- Added modern CI/CD pipeline with GitHub Actions
- Added automated publishing to CurseForge and Modrinth

## [1.1.1] - Previous Release

### Features
- Pitch indicator with visual horizon lines
- Speed display calculated from player movement
- Visual reference lines for orientation
- HUD only visible during Elytra flight
