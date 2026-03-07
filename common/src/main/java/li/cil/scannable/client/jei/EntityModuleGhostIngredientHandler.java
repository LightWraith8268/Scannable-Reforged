package li.cil.scannable.client.jei;

import li.cil.scannable.client.gui.AbstractConfigurableScannerModuleContainerScreen;
import li.cil.scannable.client.gui.ConfigurableEntityScannerModuleContainerScreen;
import li.cil.scannable.common.config.Constants;
import li.cil.scannable.common.network.Network;
import li.cil.scannable.common.network.message.SetConfiguredModuleItemAtPayload;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.List;

public class EntityModuleGhostIngredientHandler implements IGhostIngredientHandler<ConfigurableEntityScannerModuleContainerScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(final ConfigurableEntityScannerModuleContainerScreen screen, final ITypedIngredient<I> ingredient, final boolean doStart) {
        final List<Target<I>> targets = new ArrayList<>();

        final ItemStack itemStack = ingredient.getItemStack().orElse(ItemStack.EMPTY);
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpawnEggItem spawnEggItem)) {
            return targets;
        }

        final EntityType<?> entityType = spawnEggItem.getType(itemStack);

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
                    BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).ifPresent(entityResourceKey ->
                        Network.sendToServer(new SetConfiguredModuleItemAtPayload(
                            screen.getMenu().containerId, targetSlot, entityResourceKey.location())));
                }
            });
        }

        return targets;
    }

    @Override
    public void onComplete() {
    }
}
