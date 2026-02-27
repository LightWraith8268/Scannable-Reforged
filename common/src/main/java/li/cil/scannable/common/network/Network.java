package li.cil.scannable.common.network;

import dev.architectury.networking.NetworkManager;
import li.cil.scannable.common.container.AbstractModuleContainerMenu;
import li.cil.scannable.common.network.message.RemoveConfiguredModuleItemAtPayload;
import li.cil.scannable.common.network.message.SetConfiguredModuleItemAtPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;

public final class Network {
    public static void initialize() {
        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            SetConfiguredModuleItemAtPayload.TYPE,
            SetConfiguredModuleItemAtPayload.STREAM_CODEC,
            (payload, context) -> {
                if (context.getPlayer() instanceof ServerPlayer player
                        && player.containerMenu != null
                        && player.containerMenu.containerId == payload.windowId()
                        && player.containerMenu instanceof AbstractModuleContainerMenu menu) {
                    context.queue(() -> menu.setItemAt(payload.index(), payload.value()));
                }
            });

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            RemoveConfiguredModuleItemAtPayload.TYPE,
            RemoveConfiguredModuleItemAtPayload.STREAM_CODEC,
            (payload, context) -> {
                if (context.getPlayer() instanceof ServerPlayer player
                        && player.containerMenu != null
                        && player.containerMenu.containerId == payload.windowId()
                        && player.containerMenu instanceof AbstractModuleContainerMenu menu) {
                    context.queue(() -> menu.removeItemAt(payload.index()));
                }
            });
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(final SetConfiguredModuleItemAtPayload payload) {
        NetworkManager.sendToServer(payload);
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(final RemoveConfiguredModuleItemAtPayload payload) {
        NetworkManager.sendToServer(payload);
    }
}
