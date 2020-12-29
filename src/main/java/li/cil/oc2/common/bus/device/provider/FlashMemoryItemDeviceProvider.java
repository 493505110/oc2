package li.cil.oc2.common.bus.device.provider;

import li.cil.oc2.Config;
import li.cil.oc2.api.bus.device.DeviceType;
import li.cil.oc2.api.bus.device.DeviceTypes;
import li.cil.oc2.api.bus.device.ItemDevice;
import li.cil.oc2.api.bus.device.data.Firmware;
import li.cil.oc2.api.bus.device.provider.ItemDeviceQuery;
import li.cil.oc2.common.bus.device.ByteBufferFlashMemoryDevice;
import li.cil.oc2.common.bus.device.FirmwareFlashMemoryDevice;
import li.cil.oc2.common.bus.device.provider.util.AbstractItemDeviceProvider;
import li.cil.oc2.common.init.Items;
import li.cil.oc2.common.item.FlashMemoryItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;

public final class FlashMemoryItemDeviceProvider extends AbstractItemDeviceProvider {
    public FlashMemoryItemDeviceProvider() {
        super(Items.FLASH_MEMORY_ITEM);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected Optional<ItemDevice> getItemDevice(final ItemDeviceQuery query) {
        final ItemStack stack = query.getItemStack();

        final Firmware firmware = FlashMemoryItem.getFirmware(stack);
        if (firmware != null) {
            return Optional.of(new FirmwareFlashMemoryDevice(stack, firmware));
        }

        final int size = MathHelper.clamp(FlashMemoryItem.getCapacity(stack), 0, Config.maxFlashMemorySize);
        return Optional.of(new ByteBufferFlashMemoryDevice(stack, size));
    }

    @Override
    protected Optional<DeviceType> getItemDeviceType(final ItemDeviceQuery query) {
        return Optional.of(DeviceTypes.FLASH_MEMORY);
    }
}
