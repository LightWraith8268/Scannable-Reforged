package li.cil.scannable.client.fabric;

import li.cil.scannable.client.ClientSetup;
import li.cil.scannable.client.ScanManager;
import li.cil.scannable.client.renderer.OverlayRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public final class ClientSetupFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSetup.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(instance -> ScanManager.tick());
        WorldRenderEvents.LAST.register(context -> {
            ScanManager.setMatrices(context.matrixStack(), context.projectionMatrix());
            ScanManager.renderLevel(context.tickCounter().getGameTimeDeltaPartialTick(false));
        });

        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            ScanManager.renderGui(tickCounter.getGameTimeDeltaPartialTick(false));
            OverlayRenderer.render(graphics, tickCounter.getGameTimeDeltaPartialTick(false));
        });
    }
}
