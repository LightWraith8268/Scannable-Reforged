package li.cil.scannable.common.network;

import li.cil.scannable.common.container.AbstractModuleContainerMenu;
import li.cil.scannable.common.network.message.RemoveConfiguredModuleItemAtPayload;
import li.cil.scannable.common.network.message.SetConfiguredModuleItemAtPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class Network {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
            SetConfiguredModuleItemAtPayload.TYPE,
            SetConfiguredModuleItemAtPayload.STREAM_CODEC,
            Network::handleSetConfiguredModuleItem);

        registrar.playToServer(
            RemoveConfiguredModuleItemAtPayload.TYPE,
            RemoveConfiguredModuleItemAtPayload.STREAM_CODEC,
            Network::handleRemoveConfiguredModuleItem);
    }

    public static void sendToServer(final SetConfiguredModuleItemAtPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToServer(final RemoveConfiguredModuleItemAtPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    private static void handleSetConfiguredModuleItem(
            final SetConfiguredModuleItemAtPayload payload, final IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player
                && player.containerMenu != null
                && player.containerMenu.containerId == payload.windowId()
                && player.containerMenu instanceof AbstractModuleContainerMenu menu) {
            menu.setItemAt(payload.index(), payload.value());
        }
    }

    private static void handleRemoveConfiguredModuleItem(
            final RemoveConfiguredModuleItemAtPayload payload, final IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player
                && player.containerMenu != null
                && player.containerMenu.containerId == payload.windowId()
                && player.containerMenu instanceof AbstractModuleContainerMenu menu) {
            menu.removeItemAt(payload.index());
        }
    }
}
