package li.cil.oc2.common.vm;

import li.cil.oc2.api.bus.device.vm.MemoryRangeAllocator;
import li.cil.sedna.api.Board;
import li.cil.sedna.api.device.MemoryMappedDevice;
import li.cil.sedna.api.memory.MemoryRange;

import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;

public final class ManagedMemoryRangeAllocator implements MemoryRangeAllocator {
    private final Board board;
    private final Function<MemoryMappedDevice, OptionalLong> defaultAddress;
    private final ArrayList<MemoryMappedDevice> managedDevices = new ArrayList<>();
    private boolean isFrozen;

    ///////////////////////////////////////////////////////////////////

    public ManagedMemoryRangeAllocator(final Board board, final Function<MemoryMappedDevice, OptionalLong> defaultAddress) {
        this.board = board;
        this.defaultAddress = defaultAddress;
    }

    ///////////////////////////////////////////////////////////////////

    public void freeze() {
        isFrozen = true;
    }

    public void invalidate() {
        for (final MemoryMappedDevice device : managedDevices) {
            board.removeDevice(device);
        }
        managedDevices.clear();
    }

    @Override
    public OptionalLong claimMemoryRange(final long address, final MemoryMappedDevice device) {
        if (isFrozen) {
            throw new IllegalStateException();
        }

        if (board.addDevice(address, device)) {
            managedDevices.add(device);
            return OptionalLong.of(address);
        }

        return claimMemoryRange(device);
    }

    @Override
    public OptionalLong claimMemoryRange(final MemoryMappedDevice device) {
        if (isFrozen) {
            throw new IllegalStateException();
        }

        final OptionalLong address = defaultAddress.apply(device);
        if (address.isPresent() && board.addDevice(address.getAsLong(), device)) {
            managedDevices.add(device);
            return OptionalLong.of(address.getAsLong());
        }

        if (!board.addDevice(device)) {
            return OptionalLong.empty();
        }

        final Optional<MemoryRange> range = board.getMemoryMap().getMemoryRange(device);
        assert range.isPresent();

        managedDevices.add(device);
        return OptionalLong.of(range.get().address());
    }
}
