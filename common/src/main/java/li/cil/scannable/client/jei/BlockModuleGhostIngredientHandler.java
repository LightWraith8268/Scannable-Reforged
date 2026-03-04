package li.cil.scannable.client.jei;

import li.cil.scannable.client.gui.AbstractConfigurableScannerModuleContainerScreen;
import li.cil.scannable.client.gui.ConfigurableBlockScannerModuleContainerScreen;
import li.cil.scannable.common.config.Constants;
import li.cil.scannable.common.network.Network;
import li.cil.scannable.common.network.message.SetConfiguredModuleItemAtPayload;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class BlockModuleGhostIngredientHandler implements IGhostIngredientHandler<ConfigurableBlockScannerModuleContainerScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(final ConfigurableBlockScannerModuleContainerScreen screen, final ITypedIngredient<I> ingredient, final boolean doStart) {
        final List<Target<I>> targets = new ArrayList<>();

        final ItemStack itemStack = ingredient.getItemStack().orElse(ItemStack.EMPTY);
        if (itemStack.isEmpty()) {
            return targets;
        }

        final Block block = Block.byItem(itemStack.getItem());
        if (block == Blocks.AIR) {
            return targets;
        }

        final int leftPos = screen.getLeftPos();
        final int topPos = screen.getTopPos();

        for (int slot = 0; slot < Constants.CONFIGURABLE_MODULE_SLOTS; slot++) {
            final int x = leftPos + AbstractConfigurableScannerModuleContainerScreen.SLOTS_ORIGIN_X + slot * AbstractConfigurableScannerModuleContainerScreen.SLOT_SIZE;
            final int y = topPos + AbstractConfigurableScannerModuleContainerScreen.SLOTS_ORIGIN_Y;
            final int targetSlot = slot;

            targets.add(new Target<>() {
                @Override
                public Rect2i getArea() {
                    return new Rect2i(x, y, 16, 16);
                }

                @Override
                public void accept(final I i) {
                    BuiltInRegistries.BLOCK.getResourceKey(block).ifPresent(blockResourceKey ->
                        Network.sendToServer(new SetConfiguredModuleItemAtPayload(
                            screen.getMenu().containerId, targetSlot, blockResourceKey.location())));
                }
            });
        }

        return targets;
    }

    @Override
    public void onComplete() {
    }
}
