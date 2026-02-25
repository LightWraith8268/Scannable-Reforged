package li.cil.scannable.common.item;

import li.cil.scannable.api.API;
import li.cil.scannable.common.scanning.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Items {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final DeferredItem<ScannerItem> SCANNER = ITEMS.register("scanner", () -> new ScannerItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BLANK_MODULE = ITEMS.register("blank_module", ModItem::new);
    public static final DeferredItem<ScannerModuleItem> RANGE_MODULE = ITEMS.register("range_module", () -> new ScannerModuleItem(RangeScannerModule.INSTANCE));
    public static final DeferredItem<ConfigurableEntityScannerModuleItem> ENTITY_MODULE = ITEMS.register("entity_module", ConfigurableEntityScannerModuleItem::new);
    public static final DeferredItem<ScannerModuleItem> FRIENDLY_ENTITY_MODULE = ITEMS.register("friendly_entity_module", () -> new ScannerModuleItem(FriendlyEntityScannerModule.INSTANCE));
    public static final DeferredItem<ScannerModuleItem> HOSTILE_ENTITY_MODULE = ITEMS.register("hostile_entity_module", () -> new ScannerModuleItem(HostileEntityScannerModule.INSTANCE));
    public static final DeferredItem<ConfigurableBlockScannerModuleItem> BLOCK_MODULE = ITEMS.register("block_module", ConfigurableBlockScannerModuleItem::new);
    public static final DeferredItem<ScannerModuleItem> COMMON_ORES_MODULE = ITEMS.register("common_ores_module", () -> new ScannerModuleItem(CommonOresBlockScannerModule.INSTANCE));
    public static final DeferredItem<ScannerModuleItem> RARE_ORES_MODULE = ITEMS.register("rare_ores_module", () -> new ScannerModuleItem(RareOresBlockScannerModule.INSTANCE));
    public static final DeferredItem<ScannerModuleItem> FLUID_MODULE = ITEMS.register("fluid_module", () -> new ScannerModuleItem(FluidBlockScannerModule.INSTANCE));
    public static final DeferredItem<ScannerModuleItem> CHEST_MODULE = ITEMS.register("chest_module", () -> new ScannerModuleItem(ChestScannerModule.INSTANCE));

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }
}
