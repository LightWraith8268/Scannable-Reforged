# Scannable Reforged

A Minecraft mod that adds a scanner to find blocks and entities in the world. Port of [Scannable](https://github.com/MightyPirates/Scannable) by Florian "Sangar" Nücke to NeoForge 1.21.1.

## About

This is a NeoForge-only port of the original Scannable mod. The original was built with the Architectury multi-loader framework targeting Minecraft 1.20.4. This reforged version targets **Minecraft 1.21.1** on **NeoForge**.

### Changes from Original
- Migrated from Architectury multi-loader (Common/Fabric/Forge/NeoForge) to NeoForge-only
- Updated to Minecraft 1.21.1 APIs (DataComponents, new vertex API, capability system, etc.)
- Replaced Team Reborn Energy with NeoForge built-in IEnergyStorage
- Uses NeoForge convention tags (`c:ores/*`) for automatic mod ore detection
- Built-in shader support for scan effects

### Features
- **Scanner** - Hold right-click to scan the area around you
- **Modules** - Configurable modules to scan for specific blocks, entities, ores, fluids, and chests
- **Shader Effects** - Visual scan wave and result highlighting
- **All Mod Ores** - Automatically detects ores from any mod using NeoForge convention tags
- **Configurable** - Full config support for scan range, energy costs, and more

## Attribution

This project is a derivative work based on [Scannable](https://github.com/MightyPirates/Scannable) by [MightyPirates (Florian "Sangar" Nücke)](https://github.com/MightyPirates).

Original source code is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.

All images/textures and localization strings are in the public domain (CC0 1.0 Universal).

## Building

```bash
./gradlew build
```

Requires Java 21.

## License

[MIT License](LICENSE) - Same as the original Scannable mod.
