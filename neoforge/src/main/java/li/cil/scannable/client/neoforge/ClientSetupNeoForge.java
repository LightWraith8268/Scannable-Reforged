package li.cil.scannable.client.neoforge;

import li.cil.scannable.api.API;
import li.cil.scannable.client.ClientSetup;
import li.cil.scannable.client.ScanManager;
import li.cil.scannable.client.renderer.OverlayRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = API.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetupNeoForge {
    @SubscribeEvent
    public static void handleSetupEvent(final FMLClientSetupEvent event) {
        ClientSetup.initialize();

        NeoForge.EVENT_BUS.addListener(ClientSetupNeoForge::handleClientTickEvent);
        NeoForge.EVENT_BUS.addListener(ClientSetupNeoForge::handleRenderLevelEvent);
    }

    @SubscribeEvent
    public static void handleRegisterGuiLayersEvent(final RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR,
            ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "scanner_overlay"),
            (graphics, deltaTracker) -> {
                ScanManager.renderGui(deltaTracker.getGameTimeDeltaPartialTick(false));
                OverlayRenderer.render(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
            });
    }

    private static void handleClientTickEvent(final ClientTickEvent.Post event) {
        ScanManager.tick();
    }

    private static void handleRenderLevelEvent(final RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            ScanManager.setMatrices(event.getPoseStack(), event.getProjectionMatrix());
            ScanManager.renderLevel(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }
}
