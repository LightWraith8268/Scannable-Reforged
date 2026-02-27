package li.cil.scannable.common.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetConfiguredModuleItemAtPayload(
    int windowId,
    int index,
    ResourceLocation value
) implements CustomPacketPayload {

    public static final Type<SetConfiguredModuleItemAtPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("scannable", "set_module_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetConfiguredModuleItemAtPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetConfiguredModuleItemAtPayload::windowId,
            ByteBufCodecs.VAR_INT, SetConfiguredModuleItemAtPayload::index,
            ResourceLocation.STREAM_CODEC, SetConfiguredModuleItemAtPayload::value,
            SetConfiguredModuleItemAtPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
