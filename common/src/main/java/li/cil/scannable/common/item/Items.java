package li.cil.scannable.common.item;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.scannable.common.config.CommonConfig;
import li.cil.scannable.common.scanning.*;
import li.cil.scannable.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public final class Items {
    private static final DeferredRegister<Item> ITEMS = RegistryUtils.get(Registries.ITEM);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<ScannerItem> SCANNER = ITEMS.register("scanner",
        () -> new ScannerItem(new Item.Properties().stacksTo(1), () -> CommonConfig.energyCapacityScanner, () -> CommonConfig.baseScanRadius));
    public static final RegistrySupplier<ScannerItem> SCANNER_2 = ITEMS.register("scanner_2",
        () -> new ScannerItem(new Item.Properties().stacksTo(1), () -> CommonConfig.energyCapacityScanner2, () -> CommonConfig.baseScanRadius2));
    public static final RegistrySupplier<ScannerItem> SCANNER_3 = ITEMS.register("scanner_3",
        () -> new ScannerItem(new Item.Properties().stacksTo(1), () -> CommonConfig.energyCapacityScanner3, () -> CommonConfig.baseScanRadius3));
    public static final RegistrySupplier<ScannerItem> SCANNER_4 = ITEMS.register("scanner_4",
        () -> new ScannerItem(new Item.Properties().stacksTo(1), () -> CommonConfig.energyCapacityScanner4, () -> CommonConfig.baseScanRadius4));
    public static final RegistrySupplier<ScannerItem> SCANNER_5 = ITEMS.register("scanner_5",
        () -> new ScannerItem(new Item.Properties().stacksTo(1), () -> CommonConfig.energyCapacityScanner5, () -> CommonConfig.baseScanRadius5));

    public static final RegistrySupplier<Item> BLANK_MODULE = ITEMS.register("blank_module", ModItem::new);
    public static final RegistrySupplier<ScannerModuleItem> RANGE_MODULE = ITEMS.register("range_module", () -> new ScannerModuleItem(RangeScannerModule.TIER_1));
    public static final RegistrySupplier<ScannerModuleItem> RANGE_MODULE_2 = ITEMS.register("range_module_2", () -> new ScannerModuleItem(RangeScannerModule.TIER_2));
    public static final RegistrySupplier<ScannerModuleItem> RANGE_MODULE_3 = ITEMS.register("range_module_3", () -> new ScannerModuleItem(RangeScannerModule.TIER_3));
    public static final RegistrySupplier<ScannerModuleItem> RANGE_MODULE_4 = ITEMS.register("range_module_4", () -> new ScannerModuleItem(RangeScannerModule.TIER_4));
    public static final RegistrySupplier<ConfigurableEntityScannerModuleItem> ENTITY_MODULE = ITEMS.register("entity_module", ConfigurableEntityScannerModuleItem::new);
    public static final RegistrySupplier<ScannerModuleItem> FRIENDLY_ENTITY_MODULE = ITEMS.register("friendly_entity_module", () -> new ScannerModuleItem(FriendlyEntityScannerModule.INSTANCE));
    public static final RegistrySupplier<ScannerModuleItem> HOSTILE_ENTITY_MODULE = ITEMS.register("hostile_entity_module", () -> new ScannerModuleItem(HostileEntityScannerModule.INSTANCE));
    public static final RegistrySupplier<ConfigurableBlockScannerModuleItem> BLOCK_MODULE = ITEMS.register("block_module", ConfigurableBlockScannerModuleItem::new);
    public static final RegistrySupplier<ScannerModuleItem> COMMON_ORES_MODULE = ITEMS.register("common_ores_module", () -> new ScannerModuleItem(CommonOresBlockScannerModule.INSTANCE));
    public static final RegistrySupplier<ScannerModuleItem> RARE_ORES_MODULE = ITEMS.register("rare_ores_module", () -> new ScannerModuleItem(RareOresBlockScannerModule.INSTANCE));
    public static final RegistrySupplier<ScannerModuleItem> FLUID_MODULE = ITEMS.register("fluid_module", () -> new ScannerModuleItem(FluidBlockScannerModule.INSTANCE));
    public static final RegistrySupplier<ScannerModuleItem> CHEST_MODULE = ITEMS.register("chest_module", () -> new ScannerModuleItem(ChestScannerModule.INSTANCE));

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }
}
