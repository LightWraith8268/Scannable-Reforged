# NeoForge 1.21.1 Modding Research

Comprehensive research on NeoForge APIs and patterns for Minecraft 1.21.1, gathered from official documentation, MDK templates, migration primers, and Javadocs.

---

## 1. Key Version Numbers

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.219 |
| Gradle Plugin (ModDevGradle) | 2.0.140 |
| Java | 21 |
| Parchment mappings | 2024.11.17 (for MC 1.21.1) |
| Loader version range | `[1,)` |
| Minecraft version range | `[1.21.1]` |
| Foojay resolver plugin | 1.0.0 |

---

## 2. Build System Setup (build.gradle + settings.gradle)

### settings.gradle

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}
```

### gradle.properties

```properties
org.gradle.jvmargs=-Xmx1G
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true

parchment_minecraft_version=1.21.1
parchment_mappings_version=2024.11.17

minecraft_version=1.21.1
minecraft_version_range=[1.21.1]
neo_version=21.1.219
loader_version_range=[1,)

mod_id=scannable
mod_name=Scannable
mod_license=MIT
mod_version=1.8.0
mod_group_id=li.cil.scannable
```

### build.gradle (complete MDK-based template)

```groovy
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'net.neoforged.moddev' version '2.0.140'
    id 'idea'
}

version = mod_version
group = mod_group_id

base {
    archivesName = mod_id
}

// MC 1.21.1 ships Java 21
java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    // NeoForge version
    version = project.neo_version

    // Parchment mappings (human-readable parameter names)
    parchment {
        mappingsVersion = project.parchment_mappings_version
        minecraftVersion = project.parchment_minecraft_version
    }

    // Access Transformers are auto-detected from META-INF/accesstransformer.cfg
    // Uncomment to specify explicitly:
    // accessTransformers = project.files('src/main/resources/META-INF/accesstransformer.cfg')

    runs {
        client {
            client()
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        server {
            server()
            programArgument '--nogui'
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        data {
            data()
            programArguments.addAll '--mod', project.mod_id, '--all',
                '--output', file('src/generated/resources/').getAbsolutePath(),
                '--existing', file('src/main/resources/').getAbsolutePath()
        }
        configureEach {
            systemProperty 'forge.logging.markers', 'REGISTRIES'
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        "${mod_id}" {
            sourceSet(sourceSets.main)
        }
    }
}

// Include generated resources (data generators)
sourceSets.main.resources { srcDir 'src/generated/resources' }

configurations {
    runtimeClasspath.extendsFrom localRuntime
}

dependencies {
    // Optional dependencies example:
    // compileOnly "mezz.jei:jei-${mc_version}-neoforge-api:${jei_version}"
    // localRuntime "mezz.jei:jei-${mc_version}-neoforge:${jei_version}"
}

// Template processing for neoforge.mods.toml
var generateModMetadata = tasks.register("generateModMetadata", ProcessResources) {
    var replaceProperties = [
        minecraft_version      : minecraft_version,
        minecraft_version_range: minecraft_version_range,
        neo_version            : neo_version,
        loader_version_range   : loader_version_range,
        mod_id                 : mod_id,
        mod_name               : mod_name,
        mod_license            : mod_license,
        mod_version            : mod_version,
    ]
    inputs.properties replaceProperties
    expand replaceProperties
    from "src/main/templates"
    into "build/generated/sources/modMetadata"
}
sourceSets.main.resources.srcDir generateModMetadata
neoForge.ideSyncTask generateModMetadata

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}

idea {
    module {
        downloadSources = true
        downloadJavadoc = true
    }
}
```

**Important notes:**
- The `neoforge.mods.toml` file goes in `src/main/templates/META-INF/neoforge.mods.toml` so that Gradle property substitution works via `generateModMetadata`.
- ModDevGradle is simpler and recommended over NeoGradle for single-version projects.
- No explicit repository needed for NeoForge itself -- the plugin handles it.

---

## 3. neoforge.mods.toml Format

Location: `src/main/templates/META-INF/neoforge.mods.toml`

```toml
modLoader="javafml"
loaderVersion="${loader_version_range}"
license="${mod_license}"
issueTrackerURL="https://github.com/MightyPirates/Scannable/issues"

[[mods]]
modId="${mod_id}"
version="${mod_version}"
displayName="${mod_name}"
# updateJSONURL=""
# displayURL=""
logoFile="icon.png"
# credits=""
authors="fnuecke"
description='''
Scannable adds a scanner item that can be used to find specific blocks and entities nearby.
'''

# Mixin configuration
[[mixins]]
config="${mod_id}.mixins.json"

# Access Transformers (auto-detected from META-INF/accesstransformer.cfg if omitted)
#[[accessTransformers]]
#file="META-INF/accesstransformer.cfg"

# Dependencies
[[dependencies.${mod_id}]]
    modId="neoforge"
    type="required"
    versionRange="[${neo_version},)"
    ordering="NONE"
    side="BOTH"

[[dependencies.${mod_id}]]
    modId="minecraft"
    type="required"
    versionRange="${minecraft_version_range}"
    ordering="NONE"
    side="BOTH"
```

**Key fields:**
- `modLoader` -- always `"javafml"` for `@Mod` mods
- `loaderVersion` -- FML major version range, typically `"[1,)"`
- `license` -- mandatory
- `[[mods]]` -- array of tables, each defining one mod in the JAR
- `[[mixins]]` with `config` -- path to mixin JSON file
- `[[accessTransformers]]` with `file` -- path to AT file
- `[[dependencies.modid]]` -- dependency declarations with `type` (required/optional/incompatible/discouraged)
- `type` field on dependencies is **new** vs older Forge (replaces `mandatory=true/false`)

---

## 4. DataComponents System (Replacing NBT)

### Overview

In 1.21, `ItemStack.getTag()`, `getOrCreateTag()`, `hasTag()`, `setTag()` are all **removed**. Data is stored as typed, immutable DataComponents.

### Registering Custom DataComponentType

```java
// Use DeferredRegister.DataComponents for the specialized helper
public static final DeferredRegister.DataComponents DATA_COMPONENTS =
    DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);

// Simple integer component (e.g., for energy stored)
public static final Supplier<DataComponentType<Integer>> ENERGY_STORED =
    DATA_COMPONENTS.registerComponentType("energy_stored",
        builder -> builder
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
    );

// Record-based component for complex data
public record ScannerState(boolean isActive, int moduleCount) {
    public static final Codec<ScannerState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.fieldOf("is_active").forGetter(ScannerState::isActive),
            Codec.INT.fieldOf("module_count").forGetter(ScannerState::moduleCount)
        ).apply(instance, ScannerState::new));

    public static final StreamCodec<ByteBuf, ScannerState> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, ScannerState::isActive,
            ByteBufCodecs.VAR_INT, ScannerState::moduleCount,
            ScannerState::new);
}

public static final Supplier<DataComponentType<ScannerState>> SCANNER_STATE =
    DATA_COMPONENTS.registerComponentType("scanner_state",
        builder -> builder
            .persistent(ScannerState.CODEC)
            .networkSynchronized(ScannerState.STREAM_CODEC)
    );
```

### Builder API

- `.persistent(Codec<T>)` -- for saving to disk. Must be provided unless network-only.
- `.networkSynchronized(StreamCodec<ByteBuf, T>)` -- for client sync. If omitted, wraps the persistent Codec automatically (less efficient).
- `.cacheEncoding()` -- caches encoding results for rarely-changing values.
- At least one of `persistent` or `networkSynchronized` must be provided.

### ItemStack Operations

```java
// GET a component value (returns null if absent)
Integer energy = stack.get(MyComponents.ENERGY_STORED.get());

// SET a component value (returns the old value)
stack.set(MyComponents.ENERGY_STORED.get(), 1000);

// UPDATE with transformation (handles get + modify + set)
stack.update(MyComponents.ENERGY_STORED.get(),
    0,  // default if component absent
    current -> current - 10);

// REMOVE a component
stack.remove(MyComponents.ENERGY_STORED.get());

// CHECK if component exists
boolean has = stack.has(MyComponents.ENERGY_STORED.get());
```

### Setting Default Components on Items

```java
// In item registration
new Item(new Item.Properties()
    .component(MyComponents.ENERGY_STORED.get(), 0)
    .component(MyComponents.SCANNER_STATE.get(), new ScannerState(false, 0))
);
```

### Component Value Requirements

- Must implement `hashCode()` and `equals()` properly
- Must be treated as **immutable** when stored
- Java records are ideal

### Migration from NBT

| Before (1.20.4) | After (1.21) |
|------------------|--------------|
| `stack.getOrCreateTag().putInt("energy", 100)` | `stack.set(ENERGY.get(), 100)` |
| `stack.getTag().getInt("energy")` | `stack.get(ENERGY.get())` |
| `stack.hasTag()` | `stack.has(COMPONENT.get())` |
| `CompoundTag` storage | Typed `DataComponentType<T>` |

---

## 5. NeoForge Capability System

### Overview

Capabilities in NeoForge 1.21.1 are **not** attached via `AttachCapabilitiesEvent` as in old Forge. Instead, providers are registered statically in `RegisterCapabilitiesEvent`.

### Built-in Capabilities

```java
// Item capabilities
Capabilities.EnergyStorage.ITEM    // IEnergyStorage for ItemStacks
Capabilities.ItemHandler.ITEM      // IItemHandler for ItemStacks
Capabilities.FluidHandler.ITEM     // IFluidHandlerItem for ItemStacks

// Block capabilities
Capabilities.EnergyStorage.BLOCK   // IEnergyStorage for blocks
Capabilities.ItemHandler.BLOCK     // IItemHandler for blocks
Capabilities.FluidHandler.BLOCK    // IFluidHandler for blocks

// Entity capabilities
Capabilities.EnergyStorage.ENTITY  // IEnergyStorage for entities
Capabilities.ItemHandler.ENTITY    // IItemHandler for entities
```

### Registering Item Energy Capability

```java
@SubscribeEvent
public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerItem(
        Capabilities.EnergyStorage.ITEM,                    // capability
        (stack, context) -> new ComponentEnergyStorage(      // provider lambda
            stack,                                           // MutableDataComponentHolder
            MyComponents.ENERGY_STORED.get(),                // DataComponentType<Integer>
            10000,                                           // capacity
            1000,                                            // maxReceive
            1000                                             // maxExtract
        ),
        MyItems.SCANNER.get()                                // items to register for
    );
}
```

### ComponentEnergyStorage

NeoForge provides `ComponentEnergyStorage` -- an `IEnergyStorage` implementation backed by a DataComponent. It writes energy values directly to the ItemStack's DataComponent.

Constructor variants:
- `ComponentEnergyStorage(holder, componentType, capacity)`
- `ComponentEnergyStorage(holder, componentType, capacity, maxTransfer)`
- `ComponentEnergyStorage(holder, componentType, capacity, maxReceive, maxExtract)`

### Querying Capabilities

```java
// On an ItemStack
IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM, null);
if (energy != null) {
    int stored = energy.getEnergyStored();
    energy.extractEnergy(100, false);
}

// On a block
IItemHandler handler = level.getCapability(
    Capabilities.ItemHandler.BLOCK, pos, Direction.NORTH);
```

### Registering Custom Capabilities

```java
// Create a custom capability
public static final ItemCapability<IMyInterface, Void> MY_CAP =
    ItemCapability.createVoid(
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "my_capability"),
        IMyInterface.class);
```

---

## 6. Network/Payload System

### Defining a Payload

```java
public record ScanRequestPayload(
    List<ResourceLocation> modules
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ScanRequestPayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "scan_request")
        );

    public static final StreamCodec<RegistryFriendlyByteBuf, ScanRequestPayload> STREAM_CODEC =
        StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ScanRequestPayload::modules,
            ScanRequestPayload::new
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
```

### Registration

```java
@SubscribeEvent
public static void registerPayloads(RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar registrar = event.registrar("1");  // protocol version

    // Client-to-server
    registrar.playToServer(
        ScanRequestPayload.TYPE,
        ScanRequestPayload.STREAM_CODEC,
        ServerPayloadHandler::handleScanRequest
    );

    // Server-to-client
    registrar.playToClient(
        ScanResultPayload.TYPE,
        ScanResultPayload.STREAM_CODEC,
        ClientPayloadHandler::handleScanResult
    );

    // Bidirectional
    registrar.playBidirectional(
        MyData.TYPE,
        MyData.STREAM_CODEC,
        new DirectionalPayloadHandler<>(
            ClientPayloadHandler::handle,
            ServerPayloadHandler::handle
        )
    );
}
```

### Handlers

```java
public class ServerPayloadHandler {
    public static void handleScanRequest(ScanRequestPayload data, IPayloadContext context) {
        // Already on main thread by default
        ServerPlayer player = (ServerPlayer) context.player();
        // process scan request...
    }
}

public class ClientPayloadHandler {
    public static void handleScanResult(ScanResultPayload data, IPayloadContext context) {
        // Process on client main thread
        // context.enqueueWork(() -> { ... }) for network-thread handlers
    }
}
```

### Sending Packets

```java
// Client to Server
PacketDistributor.sendToServer(new ScanRequestPayload(modules));

// Server to specific player
PacketDistributor.sendToPlayer(serverPlayer, new ScanResultPayload(results));

// Server to all tracking a chunk
PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunkPos, payload);

// Server to all players
PacketDistributor.sendToAllPlayers(payload);
```

**Size limits:** Client payloads max 1 MiB; Server payloads max 32 KiB.

### StreamCodec Patterns

```java
// Composite for records
StreamCodec.composite(
    ByteBufCodecs.INT, MyRecord::field1,
    ByteBufCodecs.STRING_UTF8, MyRecord::field2,
    MyRecord::new
);

// For lists
ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())

// For optionals
ByteBufCodecs.optional(ByteBufCodecs.INT)

// Unit codec (no data)
StreamCodec.unit(MyEnum.DEFAULT)
```

---

## 7. DeferredRegister in NeoForge 1.21.1

### Basic Pattern

```java
// Generic register
public static final DeferredRegister<SoundEvent> SOUNDS =
    DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);

// Specialized helpers
public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
public static final DeferredRegister.DataComponents DATA_COMPONENTS =
    DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
```

### Registering Items

```java
public static final DeferredItem<Item> SCANNER = ITEMS.registerItem(
    "scanner",
    properties -> new ScannerItem(properties),
    new Item.Properties().stacksTo(1)
);

// Simple items
public static final DeferredItem<Item> MODULE_BLANK = ITEMS.registerSimpleItem(
    "module_blank",
    new Item.Properties()
);
```

### Registering Menus

```java
public static final DeferredRegister<MenuType<?>> MENUS =
    DeferredRegister.create(Registries.MENU, MOD_ID);

public static final Supplier<MenuType<ScannerContainerMenu>> SCANNER_MENU =
    MENUS.register("scanner", () ->
        IMenuType.create(ScannerContainerMenu::new));
```

### Registering Sound Events

```java
public static final DeferredHolder<SoundEvent, SoundEvent> SCANNER_CHARGE =
    SOUNDS.register("scanner_charge", () ->
        SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "scanner_charge")));
```

### Registering Creative Tabs

```java
public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
    DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
    CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup." + MOD_ID))
        .icon(() -> SCANNER.get().getDefaultInstance())
        .displayItems((params, output) -> {
            output.accept(SCANNER.get());
            output.accept(MODULE_BLANK.get());
        })
        .build());
```

### Adding to Existing Tabs

```java
// Via BuildCreativeModeTabContentsEvent on mod event bus
@SubscribeEvent
public static void addCreative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
        event.accept(MyItems.SCANNER.get());
    }
}
```

### Custom Registries

```java
// Define registry key
public static final ResourceKey<Registry<ScanResultProvider>> SCAN_PROVIDER_KEY =
    ResourceKey.createRegistryKey(
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "scan_result_provider"));

// Build registry
public static final Registry<ScanResultProvider> SCAN_PROVIDER_REGISTRY =
    new RegistryBuilder<>(SCAN_PROVIDER_KEY)
        .sync(true)
        .create();

// Register the registry itself
@SubscribeEvent
public static void registerRegistries(NewRegistryEvent event) {
    event.register(SCAN_PROVIDER_REGISTRY);
}

// Use DeferredRegister for entries
public static final DeferredRegister<ScanResultProvider> PROVIDERS =
    DeferredRegister.create(SCAN_PROVIDER_KEY, MOD_ID);

public static final DeferredHolder<ScanResultProvider, BlockScanResultProvider> BLOCK_PROVIDER =
    PROVIDERS.register("block", () -> new BlockScanResultProvider());
```

### Activating Registration

All DeferredRegisters must be attached to the mod event bus:

```java
public MyMod(IEventBus modEventBus, ModContainer modContainer) {
    ITEMS.register(modEventBus);
    BLOCKS.register(modEventBus);
    SOUNDS.register(modEventBus);
    MENUS.register(modEventBus);
    CREATIVE_TABS.register(modEventBus);
    DATA_COMPONENTS.register(modEventBus);
    PROVIDERS.register(modEventBus);
}
```

### Key Changes from 1.20.4

- `RegistryObject<T>` replaced by `DeferredHolder<R, T>` (implements vanilla `Holder`)
- Specialized helpers: `DeferredRegister.Blocks`, `DeferredRegister.Items`, `DeferredRegister.DataComponents`
- `DeferredBlock<T>` and `DeferredItem<T>` are specialized holder types
- Can use `Supplier<T>` if you do not need `Holder` functionality

---

## 8. ResourceLocation Changes

The `ResourceLocation` constructor is now **private**. The class is **final**.

| Before (1.20.4) | After (1.21) |
|------------------|--------------|
| `new ResourceLocation("modid", "path")` | `ResourceLocation.fromNamespaceAndPath("modid", "path")` |
| `new ResourceLocation("modid:path")` | `ResourceLocation.parse("modid:path")` |
| `new ResourceLocation("minecraft", "path")` | `ResourceLocation.withDefaultNamespace("path")` |
| `ResourceLocation.of(...)` | `ResourceLocation.bySeparator(...)` |
| `ResourceLocation.isValidResourceLocation(...)` | Removed entirely |

---

## 9. Access Transformers

### File Location

Default: `src/main/resources/META-INF/accesstransformer.cfg`

NeoForge auto-detects this location. Can also be specified in `neoforge.mods.toml`:

```toml
[[accessTransformers]]
file="META-INF/accesstransformer.cfg"
```

Or in `build.gradle`:

```groovy
neoForge {
    accessTransformers = project.files('src/main/resources/META-INF/accesstransformer.cfg')
}
```

### File Format

```
# Comment
<access_modifier> <fully_qualified_class>
<access_modifier> <fully_qualified_class> <field_name>
<access_modifier> <fully_qualified_class> <method_name>(<param_descriptors>)<return_descriptor>
```

### Access Modifiers

- `public` -- visible everywhere
- `protected` -- visible to package + subclasses
- `default` -- package-private
- `private` -- class-only
- Append `+f` to add final, `-f` to remove final

### Examples

```
# Make a class public
public net.minecraft.client.renderer.LevelRenderer

# Make a field public and non-final
public-f net.minecraft.server.MinecraftServer random

# Make a method public
public net.minecraft.client.renderer.LevelRenderer renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V
```

### Type Descriptors

| Java Type | Descriptor |
|-----------|-----------|
| `boolean` | `Z` |
| `int` | `I` |
| `long` | `J` |
| `float` | `F` |
| `double` | `D` |
| `void` | `V` |
| `String` | `Ljava/lang/String;` |
| `int[]` | `[I` |

### Converting from Access Wideners (Fabric)

| Fabric AW | NeoForge AT |
|-----------|-------------|
| `accessible class ...` | `public ...` |
| `accessible field ... Descriptor` | `public ... fieldName` |
| `accessible method ... Descriptor` | `public ... methodName(params)return` |
| `mutable field ...` | `public-f ...` (removes final) |

---

## 10. Mixin Support in NeoForge 1.21.1

### Configuration

1. Declare in `neoforge.mods.toml`:

```toml
[[mixins]]
config="scannable.mixins.json"
```

2. Create mixin JSON at `src/main/resources/scannable.mixins.json`:

```json
{
  "required": true,
  "package": "li.cil.scannable.mixin",
  "compatibilityLevel": "JAVA_21",
  "minVersion": "0.8",
  "client": [
    "LevelRendererMixin",
    "MixinScannerItem"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

3. No special Gradle plugin needed -- NeoForge has built-in Mixin support.

**Notes:**
- Mixins work the same as on Fabric/Forge -- same SpongePowered Mixin library
- `compatibilityLevel` should be `JAVA_21` for 1.21.1
- Separate `client` and `server` arrays for sided mixins
- NeoForge will load the mixin config automatically when declared in `neoforge.mods.toml`

---

## 11. CreativeModeTab Registration

### Registering a Custom Tab

```java
public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
    DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SCANNABLE_TAB =
    CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup." + MOD_ID))
        .withTabsBefore(CreativeModeTabs.COMBAT)  // ordering
        .icon(() -> SCANNER.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            output.accept(SCANNER.get());
            output.accept(MODULE_BLANK.get());
            // add more items...
        })
        .build());
```

### Adding Items to Vanilla/Existing Tabs

```java
@SubscribeEvent
public static void buildContents(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
        event.accept(MyItems.SCANNER.get());
    }
}
```

This event fires on the **mod event bus**, only on the **logical client**.

---

## 12. ModConfigSpec / NeoForge Config System

### Defining Configuration

```java
public class CommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Boolean
    public static final ModConfigSpec.BooleanValue USE_ENERGY =
        BUILDER
            .comment("Whether scanners require energy to operate")
            .define("useEnergy", true);

    // Integer with range
    public static final ModConfigSpec.IntValue SCAN_RANGE =
        BUILDER
            .comment("The range of the scanner in blocks")
            .defineInRange("scanRange", 64, 16, 256);

    // Double with range
    public static final ModConfigSpec.DoubleValue ENERGY_COST =
        BUILDER
            .comment("Energy cost per scan")
            .defineInRange("energyCost", 500.0, 0.0, 10000.0);

    // String
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_NAME =
        BUILDER
            .comment("Default scanner display name")
            .define("defaultName", "Scanner");

    // List
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_BLACKLIST =
        BUILDER
            .comment("Blocks that cannot be scanned")
            .defineListAllowEmpty("blockBlacklist",
                List.of("minecraft:bedrock"),
                () -> "",
                obj -> obj instanceof String s && ResourceLocation.tryParse(s) != null);

    // Enum
    public static final ModConfigSpec.EnumValue<ScanMode> SCAN_MODE =
        BUILDER
            .comment("Default scan mode")
            .defineEnum("scanMode", ScanMode.NORMAL);

    // Sections
    static {
        BUILDER.push("advanced");
        // ... define advanced config values ...
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
```

### Registration

```java
public MyMod(IEventBus modEventBus, ModContainer modContainer) {
    modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    // Optional: custom file name
    // modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, "scannable-server.toml");
}
```

### Config Types

| Type | Storage | Synced | When Loaded |
|------|---------|--------|-------------|
| `CLIENT` | Client config dir | No | Before `FMLCommonSetupEvent` |
| COMMON | Client & server config dir | No | Before `FMLCommonSetupEvent` |
| SERVER | World save dir (per-world) | Yes (to client) | Before `ServerAboutToStartEvent` |
| STARTUP | Config dir | No | Immediately |

### Reading Values

```java
// Config values are wrapped -- call .get() / .getAsInt() / .getAsBoolean()
int range = CommonConfig.SCAN_RANGE.getAsInt();
boolean useEnergy = CommonConfig.USE_ENERGY.getAsBoolean();
```

**Important:** Config values must not be accessed before configs are loaded. Accessing them too early throws `IllegalStateException`. Safe to access after `FMLCommonSetupEvent` or in gameplay code.

### Config Events

```java
@SubscribeEvent
public static void onConfigLoad(ModConfigEvent.Loading event) {
    // React to config loading
}

@SubscribeEvent
public static void onConfigReload(ModConfigEvent.Reloading event) {
    // React to config changes
}
```

### No Changes from 1.20.4

The ModConfigSpec system is essentially unchanged between 1.20.4 and 1.21.1. The API is stable.

---

## 13. Rendering Changes (1.20.4 to 1.21)

### Vertex System Overhaul

The vertex API was significantly rewritten in 1.21:

| Old (1.20.4) | New (1.21) |
|--------------|-----------|
| `.vertex(x, y, z)` | `.addVertex(x, y, z)` |
| `.color(r, g, b, a)` | `.setColor(r, g, b, a)` or `.setColor(argb)` |
| `.uv(u, v)` | `.setUv(u, v)` |
| `.overlayCoords(u, v)` | `.setOverlay(overlay)` or `.setUv1(u, v)` |
| `.uv2(u, v)` | `.setLight(light)` or `.setUv2(u, v)` |
| `.normal(x, y, z)` | `.setNormal(x, y, z)` |
| `.endVertex()` | **Removed** (auto-called) |
| `RGBA floats` | Single ARGB int in many places |

### BufferBuilder Changes

```java
// Old
BufferBuilder builder = Tesselator.getInstance().getBuilder();
builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
// ... add vertices
Tesselator.getInstance().end();

// New
BufferBuilder builder = Tesselator.getInstance()
    .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
// ... add vertices with addVertex/setUv/setColor
BufferUploader.drawWithShader(builder.buildOrThrow());
```

- `BufferBuilder$RenderedBuffer` renamed to `MeshData`
- `end()` on RenderType replaced with `draw()`

### Shader Changes

- `ShaderInstance` can **no longer change blend mode** -- only `EffectInstance` can (for PostPass)
- Custom shaders still registered via `RegisterShadersEvent` in 1.21.1

```java
@SubscribeEvent
public static void registerShaders(RegisterShadersEvent event) throws IOException {
    event.registerShader(
        new ShaderInstance(
            event.getResourceProvider(),
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "scan_effect"),
            DefaultVertexFormat.POSITION_TEX_COLOR
        ),
        shader -> scanEffectShader = shader  // store via callback, not directly
    );
}
```

**Important:** Do NOT store the ShaderInstance passed to the event. Store the one passed to the callback Consumer.

### Shader Renames

| Old | New |
|-----|-----|
| `minecraft:position_color_tex` | `minecraft:position_tex_color` |
| `minecraft:rendertype_armor_glint` | `minecraft:rendertype_armor_entity_glint` |
| `minecraft:rendertype_glint_direct` | `minecraft:rendertype_glint` |

### Color System Change

Many rendering methods that previously took four float arguments (r, g, b, a) now take a single ARGB integer:

```java
// Old
model.renderToBuffer(poseStack, vertexConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

// New
model.renderToBuffer(poseStack, vertexConsumer, light, overlay, 0xFFFFFFFF);  // ARGB int
```

### GLSL Shader Updates

For custom GLSL shaders (.vsh/.fsh), the vertex format attribute names may need updating to match the new DefaultVertexFormat field names. The shader JSON format remains the same for 1.21.1 but check that attribute names in the JSON match the vertex format you use.

### DeltaTracker Replaces Separate Timing Parameters

```java
// Old
public void render(float partialTick) { ... }

// New
public void render(DeltaTracker deltaTracker) {
    float partialTick = deltaTracker.getGameTimeDeltaPartialTick();
    float deltaTime = deltaTracker.getDeltaFrameTime();
}
```

---

## 14. Mod Main Class Structure

```java
@Mod(MyMod.MOD_ID)
public class MyMod {
    public static final String MOD_ID = "scannable";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Constructor -- FML auto-injects IEventBus and ModContainer
    public MyMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register all DeferredRegisters
        MyItems.ITEMS.register(modEventBus);
        MyComponents.DATA_COMPONENTS.register(modEventBus);
        MySounds.SOUNDS.register(modEventBus);
        MyMenus.MENUS.register(modEventBus);

        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        // Listen for mod lifecycle events
        modEventBus.addListener(this::commonSetup);

        // Listen for game events on the NeoForge bus
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Thread-safe setup
        event.enqueueWork(() -> {
            // Do registration that must happen on main thread
        });
    }
}
```

### Client-Side Event Subscriber

```java
@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) { ... }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) { ... }
}
```

---

## 15. Additional Migration Notes (1.20.4 to 1.21)

### Holder Requirements

Many APIs now require `Holder<T>` instead of direct registry objects:
- `MobEffectInstance` requires `Holder<MobEffect>`
- `ArmorItem` requires `Holder<ArmorMaterial>`
- `Entity#gameEvent()` requires `Holder<GameEvent>`

### Data Pack Folder Depluralizing

| Old | New |
|-----|-----|
| `tags/blocks/` | `tags/block/` |
| `tags/items/` | `tags/item/` |
| `tags/entity_types/` | `tags/entity_type/` |
| `recipes/` | `recipe/` |
| `loot_tables/` | `loot_table/` |
| `advancements/` | `advancement/` |
| `structures/` | `structure/` |

### AttributeModifier Changes

UUIDs replaced by ResourceLocations:
```java
// Old
new AttributeModifier(MY_UUID, "name", amount, operation);

// New
new AttributeModifier(
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "my_modifier"),
    amount, operation);
```

### Protected Block Methods

Many `Block` methods are now `protected`. Access through `BlockState` instead:

```java
// Old: block.getShape(state, level, pos, context)
// New: state.getShape(level, pos, context)
```

### RecipeInput Replaces Container

```java
// Old: Recipe<Container>
// New: Recipe<RecipeInput>
// Use CraftingInput.of() or SingleRecipeInput
```

### Java 21 Features Available

Since MC 1.21 requires Java 21, you can use:
- Records (already used for DataComponents/Payloads)
- Sealed classes/interfaces
- Pattern matching for instanceof
- Text blocks
- Switch expressions

---

## Summary of File Locations

| File | Path |
|------|------|
| build.gradle | `build.gradle` |
| settings.gradle | `settings.gradle` |
| gradle.properties | `gradle.properties` |
| neoforge.mods.toml (template) | `src/main/templates/META-INF/neoforge.mods.toml` |
| Access Transformer | `src/main/resources/META-INF/accesstransformer.cfg` |
| Mixin config | `src/main/resources/scannable.mixins.json` |
| Mod main class | `src/main/java/li/cil/scannable/Scannable.java` |
| Config class | `src/main/java/li/cil/scannable/common/config/CommonConfig.java` |

---

## Sources

- [NeoForge MDK 1.21.1 (ModDevGradle)](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle)
- [NeoForge Getting Started Docs](https://docs.neoforged.net/docs/gettingstarted/)
- [NeoForge Mod Files Docs (1.21.1)](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/)
- [NeoForge Registries Docs (1.21.1)](https://docs.neoforged.net/docs/1.21.1/concepts/registries/)
- [NeoForge Data Components Docs](https://docs.neoforged.net/docs/items/datacomponents/)
- [NeoForge Payload Registration Docs](https://docs.neoforged.net/docs/networking/payload/)
- [NeoForge Capabilities Docs](https://docs.neoforged.net/docs/1.21.5/inventories/capabilities/)
- [NeoForge Access Transformers Docs](https://docs.neoforged.net/docs/advanced/accesstransformers/)
- [NeoForge Config Docs (1.21.1)](https://docs.neoforged.net/docs/1.21.1/misc/config/)
- [1.20.4 to 1.20.5 Migration Primer](https://docs.neoforged.net/primer/docs/1.20.5/)
- [1.20.6 to 1.21 Migration Primer](https://docs.neoforged.net/primer/docs/1.21/)
- [1.20.5/6 to 1.21 Migration Gist](https://gist.github.com/ChampionAsh5357/d895a7b1a34341e19c80870720f9880f)
- [NeoForge Capability Rework Blog](https://neoforged.net/news/20.3capability-rework/)
- [ComponentEnergyStorage Javadoc](https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/energy/ComponentEnergyStorage.html)
- [RegisterShadersEvent Javadoc](https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/client/event/RegisterShadersEvent.html)
- [NeoForge Items Docs (1.21.1)](https://docs.neoforged.net/docs/1.21.1/items/)
- [ModDevGradle GitHub](https://github.com/neoforged/ModDevGradle)
- [ResourceLocation Javadoc (1.21)](https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/resources/ResourceLocation.html)
- [NeoForge Network Refactor Blog](https://neoforged.net/news/20.4networking-rework/)
- [Fabric Data Components Tutorial](https://docs.fabricmc.net/develop/items/custom-data-components)
