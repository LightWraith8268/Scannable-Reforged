package li.cil.scannable.common.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RemoveConfiguredModuleItemAtPayload(
    int windowId,
    int index
) implements CustomPacketPayload {

    public static final Type<RemoveConfiguredModuleItemAtPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("scannable", "remove_module_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveConfiguredModuleItemAtPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RemoveConfiguredModuleItemAtPayload::windowId,
            ByteBufCodecs.VAR_INT, RemoveConfiguredModuleItemAtPayload::index,
            RemoveConfiguredModuleItemAtPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
