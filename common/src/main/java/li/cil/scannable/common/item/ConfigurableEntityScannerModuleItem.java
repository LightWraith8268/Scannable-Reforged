package li.cil.scannable.common.item;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import dev.architectury.registry.menu.MenuRegistry;
import li.cil.scannable.common.config.Constants;
import li.cil.scannable.common.config.Strings;
import li.cil.scannable.common.container.EntityModuleContainerMenu;
import li.cil.scannable.common.data.ModDataComponents;
import li.cil.scannable.common.scanning.ConfigurableEntityScannerModule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ConfigurableEntityScannerModuleItem extends ScannerModuleItem {
    public static boolean isLocked(final ItemStack stack) {
        return false;
    }

    public static List<EntityType<?>> getEntityTypes(final ItemStack stack) {
        final List<ResourceLocation> locations = stack.getOrDefault(ModDataComponents.CONFIGURED_ENTITIES.get(), List.of());
        if (locations.isEmpty()) {
            return Collections.emptyList();
        }

        final List<EntityType<?>> result = new ArrayList<>();
        for (final ResourceLocation location : locations) {
            final Optional<EntityType<?>> entityType = EntityType.byString(location.toString());
            entityType.ifPresent(result::add);
        }
        return result;
    }

    private static boolean addEntityType(final ItemStack stack, final EntityType<?> entityType) {
        final Optional<ResourceKey<EntityType<?>>> registryName = BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType);
        if (registryName.isEmpty()) {
            return false;
        }

        if (isLocked(stack)) {
            return false;
        }

        final ResourceLocation entityId = registryName.get().location();
        final List<ResourceLocation> locations = new ArrayList<>(stack.getOrDefault(ModDataComponents.CONFIGURED_ENTITIES.get(), List.of()));

        if (locations.contains(entityId)) {
            return true;
        }
        if (locations.size() >= Constants.CONFIGURABLE_MODULE_SLOTS) {
            return false;
        }

        locations.add(entityId);
        stack.set(ModDataComponents.CONFIGURED_ENTITIES.get(), locations);
        return true;
    }

    public static void setEntityTypeAt(final ItemStack stack, final int index, final EntityType<?> entityType) {
        if (index < 0 || index >= Constants.CONFIGURABLE_MODULE_SLOTS) {
            return;
        }

        final Optional<ResourceKey<EntityType<?>>> registryName = BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType);
        if (registryName.isEmpty()) {
            return;
        }

        if (isLocked(stack)) {
            return;
        }

        final ResourceLocation entityId = registryName.get().location();
        final List<ResourceLocation> locations = new ArrayList<>(stack.getOrDefault(ModDataComponents.CONFIGURED_ENTITIES.get(), List.of()));

        final int oldIndex = locations.indexOf(entityId);
        if (oldIndex == index) {
            return;
        }

        if (index >= locations.size()) {
            locations.add(entityId);
        } else {
            locations.set(index, entityId);
        }

        if (oldIndex >= 0) {
            locations.remove(oldIndex);
        }

        stack.set(ModDataComponents.CONFIGURED_ENTITIES.get(), locations);
    }

    public static void removeEntityTypeAt(final ItemStack stack, final int index) {
        if (index < 0 || index >= Constants.CONFIGURABLE_MODULE_SLOTS) {
            return;
        }

        if (isLocked(stack)) {
            return;
        }

        final List<ResourceLocation> locations = new ArrayList<>(stack.getOrDefault(ModDataComponents.CONFIGURED_ENTITIES.get(), List.of()));
        if (index < locations.size()) {
            locations.remove(index);
            stack.set(ModDataComponents.CONFIGURED_ENTITIES.get(), locations);
        }
    }

    // --------------------------------------------------------------------- //

    public ConfigurableEntityScannerModuleItem() {
        super(ConfigurableEntityScannerModule.INSTANCE);
    }

    // --------------------------------------------------------------------- //
    // Item

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        final List<EntityType<?>> entities = getEntityTypes(stack);
        if (!entities.isEmpty()) {
            tooltip.add(Strings.TOOLTIP_ENTITIES_LIST_CAPTION);
            entities.forEach(e -> tooltip.add(Strings.listItem(e.getDescription())));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            MenuRegistry.openExtendedMenu(serverPlayer, new ExtendedMenuProvider() {
                @Override
                public Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Override
                public AbstractContainerMenu createMenu(final int id, final Inventory inventory, final Player player) {
                    return new EntityModuleContainerMenu(id, inventory, hand);
                }

                @Override
                public void saveExtraData(final FriendlyByteBuf buffer) {
                    buffer.writeEnum(hand);
                }
            });
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult interactLivingEntity(final ItemStack stack, final Player player, final LivingEntity target, final InteractionHand hand) {
        if (addEntityType(player.getItemInHand(hand), target.getType())) {
            player.swing(hand);
            player.getInventory().setChanged();
        } else {
            if (!player.level().isClientSide() && !ConfigurableEntityScannerModuleItem.isLocked(stack)) {
                player.displayClientMessage(Strings.MESSAGE_NO_FREE_SLOTS, true);
            }
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }
}
