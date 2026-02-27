package li.cil.scannable.client.gui;

import li.cil.scannable.common.config.Strings;
import li.cil.scannable.common.container.EntityModuleContainerMenu;
import li.cil.scannable.common.item.ConfigurableEntityScannerModuleItem;
import li.cil.scannable.common.network.Network;
import li.cil.scannable.common.network.message.SetConfiguredModuleItemAtPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ConfigurableEntityScannerModuleContainerScreen extends AbstractConfigurableScannerModuleContainerScreen<EntityModuleContainerMenu, EntityType<?>> {
    public ConfigurableEntityScannerModuleContainerScreen(final EntityModuleContainerMenu container, final Inventory inventory, final Component title) {
        super(container, inventory, title, Strings.GUI_ENTITIES_LIST_CAPTION);
    }

    // --------------------------------------------------------------------- //

    @Override
    protected List<EntityType<?>> getConfiguredItems(final ItemStack stack) {
        return ConfigurableEntityScannerModuleItem.getEntityTypes(stack);
    }

    @Override
    protected Component getItemName(final EntityType<?> entityType) {
        return entityType.getDescription();
    }

    @Override
    protected void renderConfiguredItem(final GuiGraphics graphics, final EntityType<?> entityType, final int x, final int y) {
        final SpawnEggItem spawnEgg = SpawnEggItem.byId(entityType);
        if (spawnEgg != null) {
            graphics.renderFakeItem(new ItemStack(spawnEgg), x, y);
        }
    }

    @Override
    protected void configureItemAt(final ItemStack stack, final int slot, final ItemStack value) {
        if (value.getItem() instanceof SpawnEggItem spawnEggItem) {
            final EntityType<?> entityType = spawnEggItem.getType(value);
            BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).ifPresent(entityResourceKey ->
                Network.sendToServer(new SetConfiguredModuleItemAtPayload(menu.containerId, slot, entityResourceKey.location())));
        }
    }
}
