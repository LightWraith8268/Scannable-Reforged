package li.cil.scannable.common.neoforge;

import li.cil.scannable.api.API;
import li.cil.scannable.api.scanning.ScannerModule;
import li.cil.scannable.common.config.CommonConfig;
import li.cil.scannable.common.data.ModDataComponents;
import li.cil.scannable.common.inventory.ScannerContainer;
import li.cil.scannable.common.item.Items;
import li.cil.scannable.common.item.ScannerModuleItem;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

@EventBusSubscriber(modid = API.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {
    public static final ItemCapability<ScannerModule, Void> SCANNER_MODULE =
        ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "scanner_module"), ScannerModule.class);

    @SubscribeEvent
    public static void register(final RegisterCapabilitiesEvent event) {
        // Energy storage for scanner
        event.registerItem(Capabilities.EnergyStorage.ITEM,
            (stack, context) -> new ComponentEnergyStorage(stack, ModDataComponents.ENERGY.get(), CommonConfig.energyCapacityScanner),
            Items.SCANNER.get());

        // Item handler for scanner inventory
        event.registerItem(Capabilities.ItemHandler.ITEM,
            (stack, context) -> new InvWrapper(ScannerContainer.of(stack)),
            Items.SCANNER.get());

        // Scanner module capability for all module items
        event.registerItem(SCANNER_MODULE,
            (stack, context) -> ((ScannerModuleItem) stack.getItem()).getModule(),
            Items.RANGE_MODULE.get(),
            Items.ENTITY_MODULE.get(),
            Items.FRIENDLY_ENTITY_MODULE.get(),
            Items.HOSTILE_ENTITY_MODULE.get(),
            Items.BLOCK_MODULE.get(),
            Items.COMMON_ORES_MODULE.get(),
            Items.RARE_ORES_MODULE.get(),
            Items.FLUID_MODULE.get(),
            Items.CHEST_MODULE.get());
    }
}
