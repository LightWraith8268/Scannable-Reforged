package li.cil.scannable.common.item;

import li.cil.scannable.api.scanning.ScannerModule;
import li.cil.scannable.common.config.CommonConfig;
import li.cil.scannable.common.config.Strings;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;

public class ScannerModuleItem extends ModItem {
    private final ScannerModule module;

    // --------------------------------------------------------------------- //

    ScannerModuleItem(final ScannerModule module) {
        super(new Item.Properties().stacksTo(1));
        this.module = module;
    }

    ScannerModuleItem(final Properties properties, final ScannerModule module) {
        super(properties);
        this.module = module;
    }

    public ScannerModule getModule() {
        return module;
    }

    public static Optional<ScannerModule> getModule(final ItemStack stack) {
        if (stack.getItem() instanceof ScannerModuleItem moduleItem) {
            return Optional.ofNullable(moduleItem.getModule());
        }
        return Optional.empty();
    }

    public static int getModuleEnergyCost(final ItemStack stack) {
        if (!CommonConfig.useEnergy) {
            return 0;
        }

        return getModule(stack)
            .map(module -> module.getEnergyCost(stack)).orElse(0);
    }

    // --------------------------------------------------------------------- //
    // Item

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        final int cost = getModuleEnergyCost(stack);
        if (cost > 0) {
            tooltip.add(Strings.energyUsage(cost));
        }
    }
}
