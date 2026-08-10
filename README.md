# Critical Flight Display Mod

Available to download from [Curseforge](https://www.curseforge.com/minecraft/mc-mods/critical-flight-details).

A Minecraft Mod that enhances the game's HUD with details for Elytra flying:

- **HUD Features:**
    - pitch display
    - horizon graphic
    - speed

---

## Images

![In-game HUD Example](images/ingame_hud.png)

---

## Supported versions and loaders

Built with [Stonecutter](https://stonecutter.kikugie.dev/) +
[Stonecraft](https://stonecraft.meza.gg/) from a single codebase:

| Minecraft Version | Fabric | NeoForge | Forge |
|--------------------|--------|----------|-------|
| 1.21.4             | ✅      | ✅        | -     |
| 1.20.1             | ✅      | -        | ✅     |
| 1.19.4             | ✅      | -        | ✅     |
| 1.18.2             | ✅      | -        | ✅     |

Quilt users: install the Fabric jar for your Minecraft version - Quilt runs
Fabric mods natively, nothing separate is built for it.

See [`CLAUDE.md`](CLAUDE.md) and [`PLAN.md`](PLAN.md) for the full
architecture writeup and version-porting notes.

---

## Installation

- Install [Fabric Loader](https://fabricmc.net/use/),
  [NeoForge](https://neoforged.net/), or [Forge](https://files.minecraftforge.net/)
  for your target Minecraft version.
- Download the matching mod `.jar` from
  [GitHub Releases](https://github.com/bshuler/critical-flight-details/releases/latest)
  or [Curseforge](https://www.curseforge.com/minecraft/mc-mods/critical-flight-details).
- Put the downloaded `.jar` in your `.minecraft/mods` folder.
- Done!

---

## Building from source

Requires JDK 21 (Gradle toolchains provision anything else a specific
version needs).

```bash
git clone https://github.com/bshuler/critical-flight-details.git
cd critical-flight-details

# Build every Minecraft version/loader in the matrix above:
./gradlew chiseledBuild

# Or build just one version/loader:
./gradlew :1.21.4-fabric:build

# Run the loader-agnostic unit tests:
./gradlew test
```

Built jars land per version/loader under
`versions/<mc-version>-<loader>/build/libs/`, e.g.
`versions/1.21.4-fabric/build/libs/flight_display-fabric-2.0.0+mc1.21.4.jar`.

---

## Planned features

- Allow the user to toggle the HUD utilities, both individually and as a
  whole, either with hotkeys or a settings screen, possibly both
- Add altitude indicators
- Add compass

---

## FAQ

- **Does this mod work on versions below 1.18.2?**
    - Not currently built for anything below 1.18.2 - see the version table
      above for the supported matrix.

- **Will this mod get me banned from *X multiplayer server*?**
    - Maybe, maybe not - the mod is entirely client-sided and does not
      require it to be installed on the server, and mostly shows things
      already available to you at all times, like an extended but
      simplified F3 menu. Some servers may not allow it regardless; check a
      server's rules before using it. Please don't create issues here
      asking about that, since I won't know.

- **Will you add '*X feature not present in the [Planned Features](#planned-features) section*'**?
    - Maybe, and only if it fits with the other features of the mod, create
      [an issue](https://github.com/bshuler/critical-flight-details/issues/new)
      about it - I only work on this project in my spare time, but I'd be
      happy to add wanted features when I can.
