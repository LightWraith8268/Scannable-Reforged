package li.cil.scannable.common;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.scannable.api.API;
import li.cil.scannable.common.config.CommonConfig;
import li.cil.scannable.common.config.Strings;
import li.cil.scannable.common.energy.ItemEnergyStorage;
import li.cil.scannable.common.item.Items;
import li.cil.scannable.common.item.ModItem;
import li.cil.scannable.common.item.ScannerItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(API.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> COMMON = TABS.register("common", () ->
        CreativeTabRegistry.create(builder -> {
            builder.icon(() -> new ItemStack(Items.SCANNER.get()));
            builder.title(Strings.CREATIVE_TAB_TITLE);
            builder.displayItems((parameters, output) -> {
                if (CommonConfig.useEnergy) {
                    for (final RegistrySupplier<ScannerItem> scannerSupplier : List.of(
                            Items.SCANNER, Items.SCANNER_2, Items.SCANNER_3, Items.SCANNER_4, Items.SCANNER_5)) {
                        final var stack = new ItemStack(scannerSupplier.get());
                        ItemEnergyStorage.of(stack).ifPresent(energy -> {
                            energy.receiveEnergy(Integer.MAX_VALUE, false);
                            output.accept(stack);
                        });
                    }
                }

                BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof ModItem)
                    .forEach(item -> output.accept(new ItemStack(item)));
            });
        }));

    public static void initialize() {
        TABS.register();
    }
}
