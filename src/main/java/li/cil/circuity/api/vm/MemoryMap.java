package li.cil.circuity.api.vm;

import li.cil.circuity.api.vm.device.memory.MemoryAccessException;
import li.cil.circuity.api.vm.device.memory.MemoryMappedDevice;

import javax.annotation.Nullable;
import java.util.OptionalInt;

/**
 * Represents a physical memory mapping of devices.
 */
public interface MemoryMap {
    /**
     * Tries to find a vacant memory range of the specified size in the specified ranged.
     *
     * @param start the minimum starting address for the memory range to find (inclusive).
     * @param end   the maximum starting address for the memory range to find (inclusive).
     * @param size  the size of the memory range to find.
     * @return the address of a free memory range, if one was found.
     */
    OptionalInt findFreeRange(final int start, final int end, final int size);

    boolean addDevice(final int address, final MemoryMappedDevice device);

    void removeDevice(final MemoryMappedDevice device);

    int getDeviceAddress(final MemoryMappedDevice device);

    @Nullable
    MemoryRange getMemoryRange(final int address);

    void setDirty(final MemoryRange range, final int offset);

    int load(final int address, final int sizeLog2) throws MemoryAccessException;

    void store(final int address, final int value, final int sizeLog2) throws MemoryAccessException;
}
