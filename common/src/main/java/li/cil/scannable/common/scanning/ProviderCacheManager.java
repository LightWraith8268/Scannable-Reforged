package li.cil.scannable.common.scanning;

import dev.architectury.injectables.annotations.ExpectPlatform;
import li.cil.scannable.common.scanning.filter.IgnoredBlocks;

public final class ProviderCacheManager {
    @ExpectPlatform
    public static void initialize() {
        throw new AssertionError();
    }

    public static void clearCache() {
        CommonOresBlockScannerModule.clearCache();
        FluidBlockScannerModule.clearCache();
        RareOresBlockScannerModule.clearCache();
        ChestScannerModule.clearCache();
        IgnoredBlocks.clearCache();
    }
}
