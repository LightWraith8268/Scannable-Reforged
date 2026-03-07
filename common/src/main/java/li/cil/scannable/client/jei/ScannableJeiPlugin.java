package li.cil.scannable.client.jei;

import li.cil.scannable.api.API;
import li.cil.scannable.client.gui.ConfigurableBlockScannerModuleContainerScreen;
import li.cil.scannable.client.gui.ConfigurableEntityScannerModuleContainerScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class ScannableJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerGuiHandlers(final IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(
            ConfigurableBlockScannerModuleContainerScreen.class,
            new BlockModuleGhostIngredientHandler());
        registration.addGhostIngredientHandler(
            ConfigurableEntityScannerModuleContainerScreen.class,
            new EntityModuleGhostIngredientHandler());
    }
}
