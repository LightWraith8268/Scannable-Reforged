# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Infinite Semver](https://semver.org/).

## [1.8.0] - 2026-02-25

### Added
- Initial port of Scannable to NeoForge 1.21.1
- Built-in shader support for scan wave and result highlighting effects
- Automatic mod ore detection via NeoForge convention tags (c:ores/*)
- DataComponent-based item data storage replacing legacy NBT
- NeoForge IEnergyStorage integration for scanner energy

### Changed
- Migrated from Architectury multi-loader to NeoForge-only
- Updated to Minecraft 1.21.1 rendering APIs (new vertex format, buffer builders)
- Replaced Team Reborn Energy with NeoForge built-in energy system
- Replaced ForgeConfigApiPort with NeoForge native ModConfigSpec
- Updated network system to NeoForge CustomPacketPayload with StreamCodec

### Removed
- Fabric and Forge loader support (NeoForge-only for now)
- Architectury dependency and @ExpectPlatform abstractions
- Legacy NBT-based item data storage
