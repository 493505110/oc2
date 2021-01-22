package li.cil.oc2.api.bus.device.data;

import li.cil.oc2.api.API;
import li.cil.oc2.api.bus.device.vm.event.VMInitializationException;
import li.cil.sedna.api.memory.MemoryMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Implementations of this interface that are registered with the registry for
 * this type can be used as executable firmware for flash memory devices.
 * <p>
 * This is used for the built-in OpenSBI firmware and Linux kernel, for example.
 * <p>
 * To make use of registered implementations, a flash memory item with the
 * string tag {@code oc2.firmware} referencing the implementation's registry name
 * must be created. For example, if the implementation's registry name is
 * {@code my_mod:my_firmware}:
 * <pre>
 * /give &#64;p oc2:flash_memory{oc2:{firmware:"my_mod:my_firmware"}}
 * </pre>
 */
public interface Firmware extends IForgeRegistryEntry<Firmware> {
    /**
     * The registry name of the registry holding firmwares.
     */
    ResourceLocation REGISTRY = new ResourceLocation(API.MOD_ID, "firmware");

    /**
     * Runs this firmware.
     * <p>
     * This will usually load machine code into memory at the specified start address.
     *
     * @param memory       access to the memory map of the machine.
     * @param startAddress the memory address where execution will commence.
     */
    boolean run(final MemoryMap memory, final long startAddress);

    /**
     * The display name of this firmware. May be shown in the tooltip of item devices
     * using this firmware.
     *
     * @return the display name of this firmware.
     */
    ITextComponent getName();
}
