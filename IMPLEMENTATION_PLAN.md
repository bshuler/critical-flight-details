# Multi-Version Implementation Plan

## Final Implementation Status

**BUILD SUCCESSFUL: 581 tasks executed**

### Supported Versions After Implementation

| MC Version | Fabric | NeoForge | Forge |
|------------|--------|----------|-------|
| 1.21.11    | Yes    | Yes      | -     |
| 1.21.10    | Yes    | Yes      | -     |
| 1.21.9     | Yes    | Yes      | -     |
| 1.21.8     | Yes    | Yes      | -     |
| 1.21.7     | Yes    | Yes      | -     |
| 1.21.6     | Yes    | Yes      | -     |
| 1.21.5     | Yes    | Yes      | -     |
| 1.21.4     | Yes    | Yes      | -     |
| 1.21.3     | Yes    | Yes      | -     |
| 1.21.2     | Yes    | Yes      | -     |
| 1.21.1     | Yes    | Yes      | -     |
| 1.21       | Yes    | Yes      | -     |
| 1.20.6     | Yes    | Yes      | -     |
| 1.20.5     | Yes    | -        | -     |
| 1.20.4     | Yes    | -        | -     |
| 1.20.3     | Yes    | -        | -     |
| 1.20.2     | Yes    | -        | -     |
| 1.20.1     | Yes    | -        | Yes   |
| 1.20       | Yes    | -        | Yes   |
| 1.19.4     | Yes    | -        | Yes   |
| 1.19.3     | Yes    | -        | Yes   |
| 1.19.2     | Yes    | -        | Yes   |
| 1.19.1     | Yes    | -        | Yes   |
| 1.19       | Yes    | -        | Yes   |
| 1.18.2     | Yes    | -        | Yes   |
| 1.18.1     | Yes    | -        | Yes   |
| 1.18       | Yes    | -        | Yes   |
| 1.17.1     | Yes    | -        | Yes   |

**Total: 53 unique builds** (up from 40)

### Not Implemented

- **Quilt**: Requires Quilted Fabric API (QFAPI) configuration not supported by current toolchain
- **1.14.x-1.16.x**: 4-5+ years old with significantly different Fabric API module structure
- **Legacy Forge/Fabric (pre-1.14)**: Would require completely different toolchain

---

## Original State (40 builds)

| Version Range | Fabric | NeoForge | Forge | Quilt |
|---------------|--------|----------|-------|-------|
| 1.21.x (12)   | Yes    | Yes      | -     | -     |
| 1.20.6        | Yes    | Yes      | -     | -     |
| 1.20.2-1.20.5 | Yes    | No*      | -     | -     |
| 1.20-1.20.1   | Yes    | -        | Yes   | -     |
| 1.19.4        | Yes    | -        | Yes   | -     |
| 1.18.2        | Yes    | -        | Yes   | -     |

*1.20.2-1.20.5 NeoForge has toolchain compatibility issues with Architectury Loom

## Target State (139 builds from MULTI_VERSION_PLAN.md)

### Tiered Implementation Approach

---

## Tier 1: Easy (Same Toolchain, Minimal Code Changes)

### 1.1 Add Quilt Loader Support
**Effort: Low** - Quilt is Fabric-compatible

Quilt can run most Fabric mods natively. We need to:
1. Add `quilt` loader entries to settings.gradle.kts
2. Create `quilt.mod.json` metadata file
3. Add Quilt dependencies in version-specific properties

**Versions to add:**
- 1.21 (quilt)
- 1.20.6, 1.20.4, 1.20.2, 1.20.1, 1.20 (quilt)
- 1.19.4, 1.19.3, 1.19.2, 1.19.1, 1.19 (quilt)
- 1.18.2 (quilt)

**Total: 11 new builds**

### 1.2 Add Missing Fabric Versions (1.18-1.19.x)
**Effort: Low** - Already have MatrixStack support for 1.18.2/1.19.4

**Versions to add:**
- 1.19.3, 1.19.2, 1.19.1, 1.19 (fabric)
- 1.18.1, 1.18 (fabric)

**Total: 6 new builds**

### 1.3 Add Missing Forge Versions (1.18-1.19.x)
**Effort: Low** - Already have ForgeHudRenderer with version conditionals

**Versions to add:**
- 1.19.3, 1.19.2, 1.19.1, 1.19 (forge)
- 1.18.1, 1.18 (forge)

**Total: 6 new builds**

---

## Tier 2: Medium (API Changes, More Conditionals)

### 2.1 Add Fabric 1.16.x-1.17.x
**Effort: Medium** - Rendering API changes

**API Differences:**
- 1.16.x uses older Fabric rendering hooks
- 1.17.x introduced some rendering changes
- All use MatrixStack (already supported)

**Versions to add:**
- 1.17.1, 1.17 (fabric)
- 1.16.5, 1.16.4, 1.16.3, 1.16.2, 1.16.1, 1.16 (fabric)

**Total: 8 new builds**

### 2.2 Add Forge 1.16.x-1.17.x
**Effort: Medium** - Different Forge event APIs

**API Differences:**
- 1.16.x uses `RenderGameOverlayEvent` (same as 1.18.2)
- 1.17.x has transitional APIs

**Versions to add:**
- 1.17.1 (forge)
- 1.16.5, 1.16.4, 1.16.3, 1.16.2, 1.16.1 (forge)

**Total: 6 new builds**

### 2.3 Try NeoForge 1.20.1-1.20.5
**Effort: Medium** - May have toolchain issues

**Known Issues:**
- 1.20.2-1.20.4: Architectury Loom doesn't support Forge toolchain
- 1.20.5: Java version resolution issues
- 1.20.1: Should work (first NeoForge version)

**Action:** Test each version individually; skip if toolchain incompatible

---

## Tier 3: Hard (Significant API Changes)

### 3.1 Add Fabric 1.14.x-1.15.x
**Effort: High** - Early Fabric API, different rendering

**API Differences:**
- 1.14.x: First Fabric version, unstable API
- 1.15.x: Improved but still early API
- Different event registration
- Different rendering context

**Versions to add:**
- 1.15.2, 1.15.1, 1.15 (fabric)
- 1.14.4, 1.14.3, 1.14.2, 1.14.1, 1.14 (fabric)

**Total: 8 new builds**

### 3.2 Add Forge 1.14.x-1.15.x
**Effort: High** - Different Forge rendering events

**API Differences:**
- `RenderGameOverlayEvent` with different parameters
- Different GUI rendering context

**Versions to add:**
- 1.15.2, 1.15.1, 1.15 (forge)
- 1.14.4, 1.14.3, 1.14.2 (forge)

**Total: 6 new builds**

---

## Tier 4: Breaking Changes (Not Recommended)

### 4.1 Legacy Fabric (Pre-1.14)
**Status: NOT FEASIBLE without breaking changes**

**Reasons:**
- Requires completely different toolchain (Legacy Fabric Loom)
- Java 8 requirement (project uses Java 17/21)
- Completely different rendering APIs
- No MatrixStack/DrawContext equivalent
- Different event systems
- 5+ years old

**Versions affected:** 1.3.2, 1.4.7, 1.5.2, 1.6.4, 1.7.10, 1.8, 1.8.9, 1.9.4, 1.10.2, 1.11.2, 1.12.2

### 4.2 Legacy Forge (Pre-1.14)
**Status: NOT FEASIBLE without breaking changes**

**Reasons:**
- Requires ForgeGradle 2.x (incompatible with modern Stonecraft)
- MCP mappings instead of Mojang/Yarn
- Java 8 requirement
- Completely different rendering system (no MatrixStack)
- Different event registration
- 5+ years old

**Versions affected:** 1.1 through 1.13.2

---

## Implementation Priority

### Phase 1: Quick Wins (23 new builds)
1. Add Quilt support for existing versions (11 builds)
2. Add Fabric 1.18-1.18.1, 1.19-1.19.3 (6 builds)
3. Add Forge 1.18-1.18.1, 1.19-1.19.3 (6 builds)

### Phase 2: Moderate Effort (14 new builds)
4. Add Fabric 1.16.x-1.17.x (8 builds)
5. Add Forge 1.16.x-1.17.x (6 builds)

### Phase 3: Higher Effort (14 new builds)
6. Add Fabric 1.14.x-1.15.x (8 builds)
7. Add Forge 1.14.x-1.15.x (6 builds)

### Phase 4: Evaluate (5 builds)
8. Test NeoForge 1.20.1-1.20.5 individually

---

## Summary

| Category | Builds | Status |
|----------|--------|--------|
| Currently Supported | 40 | Done |
| Tier 1 (Easy) | 23 | To implement |
| Tier 2 (Medium) | 19 | To implement |
| Tier 3 (Hard) | 14 | To evaluate |
| Tier 4 (Breaking) | 43 | Skip |
| **Total Feasible** | **96** | |
| **Total Requested** | **139** | |

**Coverage: 69% of requested builds feasible without breaking changes**

The 43 builds we're skipping are all pre-1.14 versions (5+ years old) that would require:
- Different toolchains (Legacy Fabric Loom, ForgeGradle 2.x)
- Java 8 instead of Java 17/21
- Completely rewritten rendering code
- Different event systems
