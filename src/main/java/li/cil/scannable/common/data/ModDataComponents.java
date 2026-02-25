package li.cil.scannable.common.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
        DeferredRegister.createDataComponents("scannable");

    public static final Supplier<DataComponentType<Integer>> ENERGY =
        COMPONENTS.registerComponentType("energy",
            builder -> builder
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<DataComponentType<ItemContainerContents>> SCANNER_INVENTORY =
        COMPONENTS.registerComponentType("scanner_inventory",
            builder -> builder
                .persistent(ItemContainerContents.CODEC)
                .networkSynchronized(ItemContainerContents.STREAM_CODEC));

    public static final Supplier<DataComponentType<List<ResourceLocation>>> CONFIGURED_BLOCKS =
        COMPONENTS.registerComponentType("configured_blocks",
            builder -> builder
                .persistent(ResourceLocation.CODEC.listOf())
                .networkSynchronized(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())));

    public static final Supplier<DataComponentType<List<ResourceLocation>>> CONFIGURED_ENTITIES =
        COMPONENTS.registerComponentType("configured_entities",
            builder -> builder
                .persistent(ResourceLocation.CODEC.listOf())
                .networkSynchronized(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())));
}
