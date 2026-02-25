package li.cil.scannable;

import li.cil.scannable.client.ClientSetup;
import li.cil.scannable.common.ModCapabilities;
import li.cil.scannable.common.ModCreativeTabs;
import li.cil.scannable.common.container.Containers;
import li.cil.scannable.common.data.ModDataComponents;
import li.cil.scannable.common.item.Items;
import li.cil.scannable.common.network.Network;
import li.cil.scannable.util.ConfigManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Scannable.MOD_ID)
public class Scannable {
    public static final String MOD_ID = "scannable";

    public Scannable(IEventBus modEventBus, ModContainer modContainer) {
        // Config
        ConfigManager.initialize(modContainer);

        // Registries
        ModDataComponents.COMPONENTS.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        Containers.CONTAINERS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);

        // Network
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, Network::register);

        // Capabilities
        modEventBus.addListener(RegisterCapabilitiesEvent.class, ModCapabilities::register);

        // Client
        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.register(modEventBus);
        }
    }
}
