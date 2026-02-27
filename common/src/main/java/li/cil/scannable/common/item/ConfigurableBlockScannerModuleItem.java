package li.cil.scannable.common.item;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import dev.architectury.registry.menu.MenuRegistry;
import li.cil.scannable.common.config.Constants;
import li.cil.scannable.common.config.Strings;
import li.cil.scannable.common.container.BlockModuleContainerMenu;
import li.cil.scannable.common.data.ModDataComponents;
import li.cil.scannable.common.scanning.ConfigurableBlockScannerModule;
import li.cil.scannable.common.scanning.filter.IgnoredBlocks;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ConfigurableBlockScannerModuleItem extends ScannerModuleItem {
    public static boolean isLocked(final ItemStack stack) {
        // Locked state is not stored in data components for now; always false.
        // If needed in the future, a Boolean data component can be added.
        return false;
    }

    public static List<Block> getBlocks(final ItemStack stack) {
        final List<ResourceLocation> locations = stack.getOrDefault(ModDataComponents.CONFIGURED_BLOCKS.get(), List.of());
        if (locations.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Block> result = new ArrayList<>();
        for (final ResourceLocation location : locations) {
            BuiltInRegistries.BLOCK.getOptional(location).ifPresent(result::add);
        }
        return result;
    }

    public static boolean addBlock(final ItemStack stack, final Block block) {
        final Optional<ResourceKey<Block>> registryName = BuiltInRegistries.BLOCK.getResourceKey(block);
        if (registryName.isEmpty()) {
            return false;
        }

        if (isLocked(stack)) {
            return false;
        }

        final ResourceLocation blockId = registryName.get().location();
        final List<ResourceLocation> locations = new ArrayList<>(stack.getOrDefault(ModDataComponents.CONFIGURED_BLOCKS.get(), List.of()));

        if (locations.contains(blockId)) {
            return true;
        }
        if (locations.size() >= Constants.CONFIGURABLE_MODULE_SLOTS) {
            return false;
        }

        locations.add(blockId);
        stack.set(ModDataComponents.CONFIGURED_BLOCKS.get(), locations);
        return true;
    }

    public static void setBlockAt(final ItemStack stack, final int index, final Block block) {
        if (index < 0 || index >= Constants.CONFIGURABLE_MODULE_SLOTS) {
            return;
        }

        final Optional<ResourceKey<Block>> registryName = BuiltInRegistries.BLOCK.getResourceKey(block);
        if (registryName.isEmpty()) {
            return;
        }

        if (isLocked(stack)) {
            return;
        }

        final ResourceLocation blockId = registryName.get().location();
        final List<ResourceLocation> locations = new ArrayList<>(stack.getOrDefault(ModDataComponents.CONFIGURED_BLOCKS.get(), List.of()));

        final int oldIndex = locations.indexOf(blockId);
        if (oldIndex == index) {
            return;
        }

        if (index >= locations.size()) {
            locations.add(blockId);
        } else {
            locations.set(index, blockId);
        }

        if (oldIndex >= 0) {
            locations.remove(oldIndex);
        }

        stack.set(ModDataComponents.CONFIGURED_BLOCKS.get(), locations);
    }

    public static void removeBlockAt(final ItemStack stack, final int index) {
        if (index < 0 || index >= Constants.CONFIGURABLE_MODULE_SLOTS) {
            return;
        }

        if (isLocked(stack)) {
            return;
        }

        final List<ResourceLocation> locations = new ArrayList<>(stack.getOrDefault(ModDataComponents.CONFIGURED_BLOCKS.get(), List.of()));
        if (index < locations.size()) {
            locations.remove(index);
            stack.set(ModDataComponents.CONFIGURED_BLOCKS.get(), locations);
        }
    }

    // --------------------------------------------------------------------- //

    public ConfigurableBlockScannerModuleItem() {
        super(ConfigurableBlockScannerModule.INSTANCE);
    }

    // --------------------------------------------------------------------- //
    // Item

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        final List<Block> blocks = getBlocks(stack);
        if (!blocks.isEmpty()) {
            tooltip.add(Strings.TOOLTIP_BLOCKS_LIST_CAPTION);
            blocks.forEach(b -> tooltip.add(Strings.listItem(b.getName())));
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
                    return new BlockModuleContainerMenu(id, inventory, hand);
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
    public InteractionResult useOn(final UseOnContext context) {
        final Level level = context.getLevel();
        if (level.isEmptyBlock(context.getClickedPos())) {
            return InteractionResult.PASS;
        }

        final Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        final ItemStack stack = context.getItemInHand();
        final BlockState state = level.getBlockState(context.getClickedPos());

        if (IgnoredBlocks.contains(state)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Strings.MESSAGE_BLOCK_IGNORED, true);
            }
            player.getCooldowns().addCooldown(this, 10);
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }

        if (!addBlock(stack, state.getBlock())) {
            if (!level.isClientSide() && !ConfigurableBlockScannerModuleItem.isLocked(stack)) {
                player.displayClientMessage(Strings.MESSAGE_NO_FREE_SLOTS, true);
            }
        }

        // Always succeed to prevent opening item UI.
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
