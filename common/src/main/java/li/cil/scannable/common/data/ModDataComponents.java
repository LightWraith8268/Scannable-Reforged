package li.cil.scannable.common.data;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.scannable.util.RegistryUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;

public final class ModDataComponents {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
        RegistryUtils.get(Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<Integer>> ENERGY =
        COMPONENTS.register("energy", () -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build());

    public static final RegistrySupplier<DataComponentType<ItemContainerContents>> SCANNER_INVENTORY =
        COMPONENTS.register("scanner_inventory", () -> DataComponentType.<ItemContainerContents>builder()
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
            .build());

    public static final RegistrySupplier<DataComponentType<List<ResourceLocation>>> CONFIGURED_BLOCKS =
        COMPONENTS.register("configured_blocks", () -> DataComponentType.<List<ResourceLocation>>builder()
            .persistent(ResourceLocation.CODEC.listOf())
            .networkSynchronized(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()))
            .build());

    public static final RegistrySupplier<DataComponentType<List<ResourceLocation>>> CONFIGURED_ENTITIES =
        COMPONENTS.register("configured_entities", () -> DataComponentType.<List<ResourceLocation>>builder()
            .persistent(ResourceLocation.CODEC.listOf())
            .networkSynchronized(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()))
            .build());

    public static void initialize() {
    }
}
