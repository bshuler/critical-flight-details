# Critical Flight Display - Modernization & Multi-Platform Expansion Plan

## Executive Summary

This plan outlines the complete modernization of Critical Flight Display from a single Minecraft 1.16.1 Fabric mod to a multi-loader, multi-version universal mod with comprehensive CI/CD and testing infrastructure.

---

## Phase 1: Infrastructure & Branch Setup

### 1.1 Git Branch Structure
```
main          → Production releases only (triggers CurseForge/Modrinth deploy)
test          → Integration testing branch
dev           → Active development branch (we work here)
```

### 1.2 Initial Setup Tasks
- [ ] Create `dev` branch from current state
- [ ] Create `test` branch from `dev`
- [ ] Configure branch protection rules on `main`
- [ ] Update `.gitignore` for modern tooling

---

## Phase 2: Upgrade to Latest Minecraft & Fabric

### 2.1 Target Versions (Latest as of 2025)
| Component | Current | Target |
|-----------|---------|--------|
| Minecraft | 1.16.1 | 1.21.x (latest stable) |
| Fabric Loader | 0.8.8 | 0.16.x |
| Fabric Loom | 0.4-SNAPSHOT | 1.10+ |
| Java | 8 | 21 |
| Gradle | (old) | 8.14+ |

### 2.2 Upgrade Tasks
- [ ] Update `gradle/wrapper/gradle-wrapper.properties` to Gradle 8.14+
- [ ] Update `build.gradle` with modern Fabric Loom plugin
- [ ] Update `gradle.properties` with latest versions
- [ ] Migrate from `jcenter()` to `mavenCentral()` (jcenter is deprecated)
- [ ] Update Java source/target compatibility to 21
- [ ] Update Yarn mappings to latest
- [ ] Update Fabric API dependency
- [ ] Fix any API breaking changes in HUD rendering code
- [ ] Fix Mixin injection points for new Minecraft version
- [ ] Update `fabric.mod.json` with correct version constraints

### 2.3 Code Migration Checklist
- [ ] `MatrixStack` → Check for `DrawContext` changes in newer versions
- [ ] `Tessellator`/`BufferBuilder` → May need updates for modern rendering pipeline
- [ ] `GL11.GL_LINES` → Review for modern rendering API changes
- [ ] `isFallFlying()` → Verify method still exists
- [ ] Mixin target `InGameHud` → Update injection points
- [ ] `TextRenderer.draw()` → Check signature changes

---

## Phase 3: Comprehensive Testing Framework

### 3.1 Testing Strategy
```
Unit Tests        → Pure Java logic (speed calculations, etc.)
Integration Tests → Mixin loading, mod initialization
Game Tests        → Fabric's gametest framework for in-game testing
```

### 3.2 Testing Infrastructure
- [ ] Add JUnit 5 dependency for unit tests
- [ ] Add Fabric's gametest-api for in-game tests
- [ ] Create test source set (`src/test/java`)
- [ ] Create gametest source set (`src/gametest/java`)
- [ ] Set up test Gradle tasks

### 3.3 Test Coverage Targets
- [ ] Speed calculation algorithm tests
- [ ] Pitch display calculation tests
- [ ] HUD positioning logic tests
- [ ] Mod initialization tests
- [ ] Mixin injection verification tests
- [ ] Elytra flight detection tests

---

## Phase 4: CI/CD Pipeline

### 4.1 GitHub Actions Workflow Structure
```
.github/workflows/
├── build.yml           → Build & test on every push/PR
├── test.yml            → Extended test suite
├── release.yml         → Deploy to CurseForge & Modrinth (main only)
└── version-matrix.yml  → Build all supported versions
```

### 4.2 Build Pipeline (`build.yml`)
Triggers: Push to `dev`, `test`, `main` + Pull Requests

- [ ] Setup Java 21
- [ ] Cache Gradle dependencies
- [ ] Run `./gradlew build`
- [ ] Run `./gradlew test`
- [ ] Upload build artifacts
- [ ] Generate build reports

### 4.3 Release Pipeline (`release.yml`)
Triggers: Push to `main` only (with version tag)

- [ ] Build release JAR
- [ ] Run full test suite
- [ ] Create GitHub Release
- [ ] Publish to CurseForge using [mc-publish](https://github.com/marketplace/actions/mc-publish)
- [ ] Publish to Modrinth using mc-publish
- [ ] Post-release notifications

### 4.4 Required Secrets
```yaml
CURSEFORGE_TOKEN    → CurseForge API token
MODRINTH_TOKEN      → Modrinth API token
GITHUB_TOKEN        → Auto-provided by GitHub Actions
```

### 4.5 mc-publish Configuration
```yaml
- uses: Kir-Antipov/mc-publish@v3.3
  with:
    modrinth-id: <project-id>
    modrinth-token: ${{ secrets.MODRINTH_TOKEN }}
    curseforge-id: <project-id>
    curseforge-token: ${{ secrets.CURSEFORGE_TOKEN }}
    github-token: ${{ secrets.GITHUB_TOKEN }}
    loaders: fabric
    game-versions: |
      1.21.x
    dependencies: |
      fabric-api | depends
```

---

## Phase 5: Multi-Version Support (Backporting)

### 5.1 Target Minecraft Versions
Support all major versions from 1.16 to latest:

| Version | Codename | Priority |
|---------|----------|----------|
| 1.21.x | Tricky Trials | High (latest) |
| 1.20.x | Trails & Tales | High |
| 1.19.x | The Wild Update | Medium |
| 1.18.x | Caves & Cliffs Part 2 | Medium |
| 1.17.x | Caves & Cliffs Part 1 | Low |
| 1.16.x | Nether Update | Low (current) |

### 5.2 Version Management Strategy
**Option A: Git Branches per Version**
```
main/1.21 → Latest Minecraft
main/1.20 → 1.20.x support
main/1.19 → 1.19.x support
...
```

**Option B: Gradle Subprojects (Recommended for multi-loader)**
```
versions/
├── 1.21/
├── 1.20/
├── 1.19/
└── ...
```

**Option C: Preprocessor-based (Architectury/Stonecutter)**
- Single codebase with version-specific code paths

### 5.3 Backporting Tasks (per version)
- [ ] Create version-specific `gradle.properties`
- [ ] Update Yarn mappings for target version
- [ ] Fix API differences
- [ ] Update Mixin injection points
- [ ] Run tests for target version
- [ ] Verify HUD rendering works

---

## Phase 6: Multi-Loader Architecture

### 6.1 Target Mod Loaders

**Client-Side Mod Loaders:**
| Loader | Type | Notes |
|--------|------|-------|
| Fabric | Client mod | Current implementation |
| Quilt | Client mod | Fabric-compatible, easy port |
| Forge | Client mod | Requires separate implementation |
| NeoForge | Client mod | Modern Forge fork (1.20.4+) |

**Server-Side Platforms (for potential server HUD features):**
| Platform | Type | Notes |
|----------|------|-------|
| Paper | Server plugin | Modern, high performance |
| Spigot | Server plugin | Legacy compatibility |
| Bukkit | Server plugin | Original API |
| Purpur | Server plugin | Paper fork with extras |
| Folia | Server plugin | Multi-threaded Paper fork |

### 6.2 Recommended Architecture: Architectury

[Architectury](https://github.com/architectury) enables write-once, deploy-everywhere:

```
critical-flight-details/
├── common/                 → Shared code (platform-agnostic)
│   └── src/main/java/
├── fabric/                 → Fabric-specific implementation
│   └── src/main/java/
├── forge/                  → Forge-specific implementation
│   └── src/main/java/
├── neoforge/               → NeoForge-specific implementation
│   └── src/main/java/
├── quilt/                  → Quilt-specific implementation
│   └── src/main/java/
└── build.gradle            → Multi-project Gradle build
```

### 6.3 Multi-Loader Migration Tasks
- [ ] Set up Architectury Gradle plugin
- [ ] Create `common` module with shared HUD logic
- [ ] Create platform-specific entry points
- [ ] Abstract rendering APIs
- [ ] Abstract Mixin/event systems
- [ ] Build matrix for all loaders

### 6.4 Platform-Specific Considerations

**Fabric/Quilt:**
- Use existing Mixin-based approach
- Quilt can run Fabric mods directly (may not need separate build)

**Forge/NeoForge:**
- Use Forge event bus instead of Mixins (or MinecraftForge Mixins)
- Different rendering setup
- Different mod metadata format (`mods.toml`)

**Server-Side (Paper/Spigot):**
- This mod is client-side HUD, so server support is limited
- Could add server-side features:
  - Config sync
  - Flight statistics storage
  - Leaderboards

---

## Phase 7: Project Structure (Final)

### 7.1 Complete Repository Structure
```
critical-flight-details/
├── .github/
│   └── workflows/
│       ├── build.yml
│       ├── test.yml
│       ├── release.yml
│       └── version-matrix.yml
├── common/
│   ├── src/
│   │   ├── main/java/net/critical/flight_display/
│   │   │   ├── FlightDisplay.java         # Common entry point
│   │   │   ├── hud/
│   │   │   │   ├── FlightDisplayHud.java  # Core HUD logic
│   │   │   │   └── HudRenderer.java       # Abstract renderer
│   │   │   └── config/
│   │   │       └── ModConfig.java         # Configuration
│   │   └── test/java/                     # Unit tests
│   └── build.gradle
├── fabric/
│   ├── src/main/
│   │   ├── java/.../
│   │   │   ├── FabricFlightDisplay.java
│   │   │   └── mixin/GameInfoMixin.java
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       └── flight_display.mixins.json
│   └── build.gradle
├── forge/
│   ├── src/main/
│   │   ├── java/.../
│   │   │   └── ForgeFlightDisplay.java
│   │   └── resources/META-INF/mods.toml
│   └── build.gradle
├── neoforge/
│   └── (similar to forge)
├── quilt/
│   └── (similar to fabric, may share)
├── build.gradle                # Root build file
├── settings.gradle             # Multi-project settings
├── gradle.properties           # Shared properties
├── CLAUDE.md
├── README.md
└── LICENSE
```

---

## Phase 8: Implementation Roadmap

### Stage 1: Foundation (Do First)
1. Set up branch structure (`dev`, `test`, `main`)
2. Upgrade to latest Minecraft/Fabric on `dev`
3. Get mod working on latest version
4. Add basic CI/CD (build on push)

### Stage 2: Quality (Do Second)
5. Add comprehensive tests
6. Add release pipeline to CurseForge/Modrinth
7. Configure branch protection

### Stage 3: Expansion (Do Third)
8. Refactor to Architectury structure
9. Add Forge/NeoForge support
10. Add Quilt support

### Stage 4: Coverage (Do Fourth)
11. Backport to 1.20.x
12. Backport to 1.19.x
13. Backport to 1.18.x
14. Backport to 1.17.x
15. Ensure 1.16.x still works

### Stage 5: Polish (Do Last)
16. Add configuration system (toggle HUD elements)
17. Add planned features (altitude, compass)
18. Documentation and release

---

## Appendix A: Key Resources

### Documentation
- [Fabric Documentation](https://docs.fabricmc.net/)
- [Architectury Docs](https://docs.architectury.dev/)
- [Forge Docs](https://docs.minecraftforge.net/)
- [NeoForge Docs](https://docs.neoforged.net/)
- [mc-publish Action](https://github.com/marketplace/actions/mc-publish)

### Publishing Platforms
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods)
- [Modrinth](https://modrinth.com/mods)
- [PaperMC Hangar](https://hangar.papermc.io/) (for server plugins)

### Tools
- [Fabric Template Generator](https://fabricmc.net/develop/template/)
- [Architectury Templates](https://github.com/architectury/architectury-templates)
- [Minecraft Version Manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json)

---

## Appendix B: Version Compatibility Matrix

| MC Version | Fabric Loader | Fabric API | Java | Loom |
|------------|---------------|------------|------|------|
| 1.21.x | 0.16.x | 0.100+ | 21 | 1.10+ |
| 1.20.x | 0.15.x | 0.90+ | 17-21 | 1.5+ |
| 1.19.x | 0.14.x | 0.60+ | 17 | 1.2+ |
| 1.18.x | 0.13.x | 0.40+ | 17 | 0.12+ |
| 1.17.x | 0.11.x | 0.40+ | 16 | 0.10+ |
| 1.16.x | 0.8.x | 0.14+ | 8 | 0.4 |

---

## Next Steps

Ready to begin? Start with:
1. **Create dev branch**: `git checkout -b dev`
2. **Update Gradle wrapper**: `./gradlew wrapper --gradle-version=8.14`
3. **Update build.gradle**: Modern Fabric Loom setup

Shall I proceed with Phase 1 implementation?
