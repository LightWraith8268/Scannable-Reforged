package li.cil.scannable.client.scanning;

import li.cil.scannable.api.API;
import li.cil.scannable.api.scanning.ScanResultProvider;
import net.minecraft.core.Registry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

@OnlyIn(Dist.CLIENT)
public final class ScanResultProviders {
    public static final Registry<ScanResultProvider> REGISTRY = new RegistryBuilder<>(ScanResultProvider.REGISTRY).sync(false).create();

    public static final DeferredRegister<ScanResultProvider> DEFERRED_REGISTER = DeferredRegister.create(ScanResultProvider.REGISTRY, API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final DeferredHolder<ScanResultProvider, ScanResultProviderBlock> BLOCKS = DEFERRED_REGISTER.register(
        API.SCAN_RESULT_PROVIDER_BLOCKS.getPath(), ScanResultProviderBlock::new);
    public static final DeferredHolder<ScanResultProvider, ScanResultProviderEntity> ENTITIES = DEFERRED_REGISTER.register(
        API.SCAN_RESULT_PROVIDER_ENTITIES.getPath(), ScanResultProviderEntity::new);

    // --------------------------------------------------------------------- //

    public static void register(final IEventBus modBus) {
        DEFERRED_REGISTER.register(modBus);
    }
}
