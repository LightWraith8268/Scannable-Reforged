# Scannable - NeoForge 1.21.1 Migration

## Project Overview

Scannable is a Minecraft mod that adds a handheld area scanner. Players craft a Scanner item, equip it with swappable Scanner Modules, and activate it to perform a pulse scan that reveals nearby points of interest with a visual wave effect and HUD markers.

**Migration:** MC 1.20.4 Architectury multi-loader → MC 1.21.1 NeoForge-only.

## Key Files & Directories

```
D:/Coding/Minecraft/Scannable/
├── CLAUDE.md                          # This file
├── build.gradle                       # NeoForge 1.21.1 build (ModDevGradle 2.0.140)
├── settings.gradle
├── gradle.properties                  # Versions: MC 1.21.1, NeoForge 21.1.219, Java 21
├── docs/plans/                        # Implementation plan
├── neoforge-1.21.1-research.md        # API research reference
├── source/                            # Original 1.20.4 source (READ-ONLY reference)
│   ├── common/                        # Platform-independent code
│   ├── neoforge/                      # NeoForge platform implementations
│   ├── fabric/                        # Fabric (not used)
│   └── forge/                         # Old Forge (not used)
└── src/main/                          # NEW NeoForge 1.21.1 source
    ├── java/li/cil/scannable/
    │   ├── Scannable.java             # @Mod entry point
    │   ├── api/                       # Public API interfaces
    │   ├── common/                    # Shared logic (items, config, containers, network, scanning)
    │   ├── client/                    # Client-only (rendering, GUI, shaders, scan providers)
    │   ├── mixin/                     # Mixins (LevelRenderer hook, reequip animation)
    │   └── util/                      # ConfigManager, utilities
    ├── resources/
    │   ├── META-INF/accesstransformer.cfg
    │   ├── scannable.mixins.json
    │   ├── assets/scannable/          # Models, textures, shaders, sounds, lang
    │   └── data/scannable/            # Tags, recipes, advancements
    └── templates/
        └── META-INF/neoforge.mods.toml
```

## Architecture

### Package: `li.cil.scannable`

| Package | Purpose |
|---------|---------|
| `api.scanning` | Public interfaces: `ScannerModule`, `BlockScannerModule`, `EntityScannerModule`, `ScanResult`, `ScanResultProvider` |
| `api.prefab` | `AbstractScanResultProvider` base class with rendering helpers |
| `common.item` | `ScannerItem` (main device), `ScannerModuleItem` (module base), 10 module items |
| `common.container` | `ScannerContainerMenu`, `BlockModuleContainerMenu`, `EntityModuleContainerMenu` |
| `common.inventory` | `ScannerContainer` (9-slot SimpleContainer backed by DataComponent) |
| `common.energy` | `ItemEnergyStorage` interface (wraps NeoForge `IEnergyStorage`) |
| `common.data` | `ModDataComponents` - custom `DataComponentType` definitions |
| `common.network` | `Network` + 2 C2S `CustomPacketPayload` records |
| `common.scanning` | 9 scanner module enum singletons + `ProviderCacheManager` |
| `common.config` | `CommonConfig`, `Constants`, `Strings` |
| `client` | `ScanManager` (scan lifecycle state machine), `ClientConfig` |
| `client.renderer` | `ScannerRenderer` (GLSL wave effect), `OverlayRenderer` (HUD pie chart) |
| `client.shader` | `Shaders` - loads `scan_effect` + `scan_result` GLSL programs |
| `client.scanning` | `ScanResultProviders` (custom registry), `ScanResultProviderBlock`, `ScanResultProviderEntity` |
| `client.scanning.filter` | 8 filter classes for block/entity matching |
| `client.gui` | 4 screen classes for scanner + configurable module GUIs |
| `client.audio` | `SoundManager` for scan charge/activate sounds |
| `mixin` | `LevelRendererMixin` (render injection), `ScannerItemMixin` (reequip suppression) |
| `util` | `ConfigManager` (annotation-based config → ModConfigSpec), `TooltipUtils`, `UnitConversion` |

### Key Design Patterns

1. **All scanning is client-side only.** `ScanResultProvider` is client-only. Server validates energy/modules only.
2. **DataComponents replace NBT.** Energy, inventory, and configured block/entity lists use typed `DataComponentType`.
3. **Singleton enum modules.** All scanner modules are static `enum INSTANCE` singletons with lazy filter caches.
4. **Custom registry.** `ScanResultProvider` has its own `Registry` registered via `NewRegistryEvent`.
5. **Two-shader rendering.** `scan_effect.fsh` = expanding sphere wave, `scan_result.fsh` = pulsating result icons.

## Version & Dependencies

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.219 |
| ModDevGradle | 2.0.140 |
| Java | 21 |
| Parchment | 2024.11.17 |
| Mappings | Official Mojang + Parchment |

## Critical API Differences (1.20.4 → 1.21.1)

### DataComponents (no more NBT)
```java
// OLD: stack.getOrCreateTag().putInt("energy", 100)
// NEW: stack.set(ModDataComponents.ENERGY.get(), 100)

// OLD: stack.getTag().getInt("energy")
// NEW: stack.get(ModDataComponents.ENERGY.get())
```

### ResourceLocation
```java
// OLD: new ResourceLocation("scannable", "path")
// NEW: ResourceLocation.fromNamespaceAndPath("scannable", "path")
```

### Vertex API
```java
// OLD: builder.vertex(x,y,z).uv(u,v).color(r,g,b,a).endVertex()
// NEW: builder.addVertex(x,y,z).setUv(u,v).setColor(r,g,b,a)  // no endVertex
```

### BufferBuilder
```java
// OLD: Tesselator.getInstance().getBuilder().begin(...)
// NEW: Tesselator.getInstance().begin(...)
// OLD: tesselator.end()
// NEW: BufferUploader.drawWithShader(builder.buildOrThrow())
```

### DeferredRegister
```java
// OLD: Architectury RegistryUtils wrapping DeferredRegister
// NEW: Direct NeoForge DeferredRegister with DeferredHolder<R,T>
```

### Network Packets
```java
// OLD: Architectury NetworkManager with custom AbstractMessage
// NEW: record implementing CustomPacketPayload + StreamCodec + PayloadRegistrar
```

### Capabilities
```java
// OLD: @ExpectPlatform bridge → stack.getCapability(...)
// NEW: Direct stack.getCapability(Capabilities.EnergyStorage.ITEM)
//      ComponentEnergyStorage for DataComponent-backed energy
```

### Data Pack Paths (depluralised in 1.21)
```
tags/items/  → tags/item/
tags/blocks/ → tags/block/
recipes/     → recipe/
advancements/ → advancement/
```

### Convention Tags
```
forge:ores/iron → c:ores/iron  (NeoForge 1.21 uses 'c' namespace)
```

## Custom DataComponents

Defined in `ModDataComponents.java`:

| Component | Type | Used By |
|-----------|------|---------|
| `ENERGY` | `Integer` | `ScannerItem` via `ComponentEnergyStorage` |
| `SCANNER_INVENTORY` | `ItemContainerContents` | `ScannerContainer` (9 module slots) |
| `CONFIGURED_BLOCKS` | `List<ResourceLocation>` | `ConfigurableBlockScannerModuleItem` |
| `CONFIGURED_ENTITIES` | `List<ResourceLocation>` | `ConfigurableEntityScannerModuleItem` |

## Build & Run

```bash
./gradlew build              # Compile and package
./gradlew runClient          # Launch Minecraft client with mod
./gradlew runServer          # Launch dedicated server with mod
./gradlew runData            # Run data generators
```

## Migration Plan

Full implementation plan: `docs/plans/2026-02-25-neoforge-1.21.1-migration.md`

12 tasks in dependency order:
1. Build System & Project Structure
2. API Layer
3. Config System
4. DataComponents
5. Item Registration
6. Menu/Container Registration
7. Energy System
8. Network Packets
9. Scanner Module Implementations
10. Capabilities & Mod Entry Point
11. Client Rendering & GUI
12. Resources (ATs, Mixins, Assets, Data)

## Reference Files

- `source/` - Original 1.20.4 Architectury source (read-only reference)
- `neoforge-1.21.1-research.md` - Comprehensive NeoForge 1.21.1 API research with code examples
- `source/neoforge/` - NeoForge-specific implementations to inline (capabilities, config, energy)
- `source/common/` - Platform-independent code to port

## Coding Conventions

- Package root: `li.cil.scannable`
- Mod ID: `scannable`
- Java 21 features allowed (records, pattern matching, sealed classes, text blocks)
- Follow existing code style from `source/` (consistent naming, enum singletons for modules)
- No Architectury dependencies - all platform bridges inlined as direct NeoForge calls
- Prefer `DeferredHolder`/`DeferredItem` over raw `Supplier` for registry objects
- Use `ResourceLocation.fromNamespaceAndPath()` everywhere (never `new ResourceLocation()`)
- DataComponents are immutable records; use `stack.set()` / `stack.get()` / `stack.update()`
