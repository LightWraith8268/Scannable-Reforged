package li.cil.scannable.common.scanning;

import li.cil.scannable.api.scanning.ScanResultProvider;
import li.cil.scannable.api.scanning.ScannerModule;
import li.cil.scannable.common.config.CommonConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.annotation.Nullable;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public enum RangeScannerModule implements ScannerModule {
    TIER_1(() -> CommonConfig.energyCostModuleRange, () -> (double) CommonConfig.rangeModifierModuleRange),
    TIER_2(() -> CommonConfig.energyCostModuleRange2, () -> (double) CommonConfig.rangeModifierModuleRange2),
    TIER_3(() -> CommonConfig.energyCostModuleRange3, () -> (double) CommonConfig.rangeModifierModuleRange3),
    TIER_4(() -> CommonConfig.energyCostModuleRange4, () -> (double) CommonConfig.rangeModifierModuleRange4);

    public static final RangeScannerModule INSTANCE = TIER_1;

    private final IntSupplier energyCost;
    private final DoubleSupplier rangeModifier;

    RangeScannerModule(final IntSupplier energyCost, final DoubleSupplier rangeModifier) {
        this.energyCost = energyCost;
        this.rangeModifier = rangeModifier;
    }

    @Override
    public int getEnergyCost(final ItemStack module) {
        return energyCost.getAsInt();
    }

    @Override
    public boolean hasResultProvider() {
        return false;
    }

    @Nullable
    @Environment(EnvType.CLIENT)
    @Override
    public ScanResultProvider getResultProvider() {
        return null;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public float adjustGlobalRange(final float range) {
        return range + Mth.ceil(CommonConfig.baseScanRadius * rangeModifier.getAsDouble());
    }
}
