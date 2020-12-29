package li.cil.oc2.common.container;

import li.cil.oc2.common.bus.ItemHandlerDeviceBusElement;
import li.cil.oc2.common.bus.device.ItemDeviceInfo;
import li.cil.oc2.common.util.NBTTagIds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class DeviceItemStackHandler extends ItemStackHandler {
    private static final String BUS_ELEMENT_NBT_TAG_NAME = "busElement";

    ///////////////////////////////////////////////////////////////////

    private final ItemHandlerDeviceBusElement busElement;

    ///////////////////////////////////////////////////////////////////

    public DeviceItemStackHandler(final int size, final Function<ItemStack, List<ItemDeviceInfo>> deviceLookup) {
        this(NonNullList.withSize(size, ItemStack.EMPTY), deviceLookup);
    }

    public DeviceItemStackHandler(final NonNullList<ItemStack> stacks, final Function<ItemStack, List<ItemDeviceInfo>> deviceLookup) {
        super(stacks);
        this.busElement = new ItemHandlerDeviceBusElement(getSlots(), deviceLookup);
    }

    ///////////////////////////////////////////////////////////////////

    public ItemHandlerDeviceBusElement getBusElement() {
        return busElement;
    }

    @Override
    public CompoundNBT serializeNBT() {
        final CompoundNBT nbt = super.serializeNBT();
        nbt.put(BUS_ELEMENT_NBT_TAG_NAME, busElement.serializeNBT());
        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        busElement.deserializeNBT(nbt.getList(BUS_ELEMENT_NBT_TAG_NAME, NBTTagIds.TAG_COMPOUND));
        for (int slot = 0; slot < getSlots(); slot++) {
            busElement.updateDevices(slot, getStackInSlot(slot));
        }
    }

    @NotNull
    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        if (!simulate && amount > 0) {
            busElement.handleBeforeItemRemoved(slot, getStackInSlot(slot));
        }

        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(final int slot) {
        return 1;
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void onContentsChanged(final int slot) {
        super.onContentsChanged(slot);
        busElement.updateDevices(slot, getStackInSlot(slot));
    }
}
