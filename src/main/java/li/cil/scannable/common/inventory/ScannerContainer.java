package li.cil.scannable.common.inventory;

import li.cil.scannable.common.data.ModDataComponents;
import li.cil.scannable.common.item.Items;
import li.cil.scannable.common.item.ScannerItem;
import li.cil.scannable.common.item.ScannerModuleItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class ScannerContainer extends SimpleContainer {
    private static final int ACTIVE_MODULE_COUNT = 3;
    private static final int INACTIVE_MODULE_COUNT = 6;
    private static final int TOTAL_MODULE_COUNT = ACTIVE_MODULE_COUNT + INACTIVE_MODULE_COUNT;

    private final ItemStack container;

    public ScannerContainer(final ItemStack container) {
        super(TOTAL_MODULE_COUNT);
        this.container = container;

        // Load from DataComponent
        final ItemContainerContents contents = container.getOrDefault(
            ModDataComponents.SCANNER_INVENTORY.get(), ItemContainerContents.EMPTY);
        final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_MODULE_COUNT, ItemStack.EMPTY);
        contents.copyInto(items);
        for (int i = 0; i < items.size(); i++) {
            super.setItem(i, items.get(i));
        }
    }

    public static ScannerContainer of(final ItemStack container) {
        if (container.getItem() instanceof ScannerItem) {
            return new ScannerContainer(container);
        } else {
            return new ScannerContainer(new ItemStack(Items.SCANNER.get()));
        }
    }

    public ContainerSlice getActiveModules() {
        return new ContainerSlice(this, 0, ACTIVE_MODULE_COUNT);
    }

    public ContainerSlice getInactiveModules() {
        return new ContainerSlice(this, ACTIVE_MODULE_COUNT, INACTIVE_MODULE_COUNT);
    }

    // --------------------------------------------------------------------- //
    // Container

    @Override
    public void setItem(final int i, final ItemStack itemStack) {
        if (canPlaceItem(i, itemStack)) {
            super.setItem(i, itemStack);
        }
    }

    @Override
    public boolean canPlaceItem(final int i, final ItemStack stack) {
        return isModule(stack) && super.canPlaceItem(i, stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();

        // Save to DataComponent
        final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_MODULE_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < TOTAL_MODULE_COUNT; i++) {
            items.set(i, getItem(i));
        }
        container.set(ModDataComponents.SCANNER_INVENTORY.get(), ItemContainerContents.fromItems(items));
    }

    // --------------------------------------------------------------------- //
    // SimpleContainer

    @Override
    public ItemStack addItem(final ItemStack stack) {
        if (canAddItem(stack)) {
            return super.addItem(stack);
        } else {
            return stack;
        }
    }

    @Override
    public boolean canAddItem(final ItemStack stack) {
        return isModule(stack) && super.canAddItem(stack);
    }

    // --------------------------------------------------------------------- //

    private boolean isModule(final ItemStack stack) {
        // All built-in modules, including those without capability such as the range module.
        if (stack.getItem() instanceof ScannerModuleItem) {
            return true;
        }

        // External modules declared via capability/interface.
        if (ScannerModuleItem.getModule(stack).isPresent()) {
            return true;
        }

        return false;
    }
}
