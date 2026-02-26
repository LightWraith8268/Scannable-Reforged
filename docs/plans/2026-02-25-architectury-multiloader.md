# Architectury Multi-Loader Restructuring Plan

> **Goal:** Restructure from NeoForge-only to Architectury multi-loader (common + neoforge + fabric) for MC 1.21.1

## Architecture

Three Gradle subprojects: `common/`, `neoforge/`, `fabric/`
- ~80% of code goes to `common/` (compiles against vanilla MC via Architectury Loom)
- ~10% each for `neoforge/` and `fabric/` (platform bridges + entry points)
- Architectury API 13.x provides cross-platform wrappers for registration, networking, menus

## Branching Strategy

- `main` = stable releases
- `dev/1.21.1` = active development
- `dev/1.x.x` = future MC version ports

## Phase 0: Build System

- Create `gradle/libs.versions.toml` version catalog
- Convert to `settings.gradle.kts` with Architectury multi-loader structure
- Create `common/build.gradle.kts`, `neoforge/build.gradle.kts`, `fabric/build.gradle.kts`
- Update `gradle.properties` with `enabledPlatforms=fabric,neoforge`

## Phase 1: Common Module (~50 files)

Move all platform-neutral code:
- API layer (already platform-neutral)
- Client GUI, rendering, scanning, filters, shaders, audio
- Game logic (items, containers, scanning modules, inventory)
- Config annotations and base config classes
- Shared mixin (LevelRendererMixin)

Create abstraction bridges (`@ExpectPlatform`):
1. ConfigManager.add() / initialize()
2. ItemEnergyStorage.of()
3. ScannerModuleItem.getModule()
4. ProviderCacheManager.initialize()

Replace NeoForge-specific imports:
- `@OnlyIn(Dist.CLIENT)` -> `@Environment(EnvType.CLIENT)`
- `Tags.Blocks.ORES_*` -> direct `TagKey.create()` with `c:` namespace
- Registration via Architectury DeferredRegister/RegistrySupplier

## Phase 2: NeoForge Module (~10 files)

- Entry point (`@Mod` class)
- Client event wiring (tick, render, GUI layers, menu screens)
- Capabilities registration (energy, item handler, scanner module)
- ConfigManager implementation (ModConfigSpec)
- Energy implementation (ComponentEnergyStorage)
- ScannerItemMixin (IItemExtension)

## Phase 3: Fabric Module (~10 files)

- Entry points (ModInitializer, ClientModInitializer)
- Client event wiring (Fabric callbacks)
- ConfigManager implementation (ForgeConfigApiPort)
- Energy implementation (Team Reborn Energy / Fabric Transfer API)
- ScannerModuleProvider interface
- ScannerItemMixin (Fabric approach)

## Phase 4: Verification

- `./gradlew :common:build`
- `./gradlew :neoforge:build`
- `./gradlew :fabric:build`
- In-game testing both jars

## Key Decisions

- DataComponents registration: verify Architectury 13.x DeferredRegister supports DATA_COMPONENT_TYPE
- Networking: verify Architectury 13.x codec-based payload support for 1.21.1
- Fabric energy: needs fresh implementation with DataComponent backing (not NBT)
- Tags: use `c:` namespace directly in common (both platforms converged on this in 1.21.1)
