package li.cil.oc2.common.bus;

import li.cil.ceres.api.Serialized;
import li.cil.oc2.api.bus.DeviceBus;
import li.cil.oc2.api.bus.DeviceBusElement;
import li.cil.oc2.common.ServerScheduler;
import li.cil.oc2.common.capabilities.Capabilities;
import li.cil.oc2.common.device.DeviceImpl;
import li.cil.oc2.common.device.DeviceInterfaceCollection;
import li.cil.oc2.common.device.provider.Providers;
import li.cil.oc2.common.util.WorldUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Objects;
import java.util.UUID;

public final class TileEntityDeviceBusElement {
    private static final String DEVICE_IDS_NBT_TAG_NAME = "deviceIds";
    private static final String DEVICE_ID_NBT_TAG_NAME = "deviceId";

    private static final int NEIGHBOR_COUNT = 6;

    private final TileEntity tileEntity;

    private final DeviceBusElement busElement = Objects.requireNonNull(Capabilities.DEVICE_BUS_ELEMENT_CAPABILITY.getDefaultInstance());
    private final DeviceImpl[] devices = new DeviceImpl[NEIGHBOR_COUNT];
    @Serialized private UUID[] deviceIds = new UUID[NEIGHBOR_COUNT];

    public TileEntityDeviceBusElement(final TileEntity tileEntity) {
        this.tileEntity = tileEntity;

        for (int i = 0; i < NEIGHBOR_COUNT; i++) {
            deviceIds[i] = UUID.randomUUID();
        }
    }

    public DeviceBusElement getBusElement() {
        return busElement;
    }

    public void handleNeighborChanged(final BlockPos pos) {
        final World world = tileEntity.getWorld();
        if (world == null || world.isRemote()) {
            return;
        }

        final BlockPos toPos = pos.subtract(tileEntity.getPos());
        final Direction direction = Direction.byLong(toPos.getX(), toPos.getY(), toPos.getZ());
        if (direction == null) {
            return;
        }

        final int index = direction.getIndex();

        final LazyOptional<DeviceInterfaceCollection> device = Providers.getDevice(world, pos, direction);
        final DeviceImpl identifiableDevice;

        if (device.isPresent()) {
            final String typeName = WorldUtils.getBlockName(world, pos);
            identifiableDevice = new DeviceImpl(device, deviceIds[index], typeName);
            device.addListener((ignored) -> handleNeighborChanged(pos));
        } else {
            identifiableDevice = null;
        }

        if (Objects.equals(devices[index], identifiableDevice)) {
            return;
        }

        if (devices[index] != null) {
            busElement.removeDevice(devices[index]);
        }

        devices[index] = identifiableDevice;

        if (devices[index] != null) {
            busElement.addDevice(devices[index]);
        }
    }

    public void initialize() {
        final World world = Objects.requireNonNull(tileEntity.getWorld());
        ServerScheduler.schedule(world, () -> {
            if (tileEntity.isRemoved()) {
                return;
            }

            scanNeighborsForDevices();
            scheduleBusScanInAdjacentBusElements();
        });
    }

    public void dispose() {
        busElement.scheduleScan();
    }

    public CompoundNBT write(final CompoundNBT compound) {
        final ListNBT deviceIdsNbt = new ListNBT();
        for (int i = 0; i < NEIGHBOR_COUNT; i++) {
            final CompoundNBT deviceIdNbt = new CompoundNBT();
            deviceIdNbt.putUniqueId(DEVICE_ID_NBT_TAG_NAME, deviceIds[i]);
            deviceIdsNbt.add(deviceIdNbt);
        }
        compound.put(DEVICE_IDS_NBT_TAG_NAME, deviceIdsNbt);

        return compound;
    }

    private void scanNeighborsForDevices() {
        for (final Direction direction : Direction.values()) {
            handleNeighborChanged(tileEntity.getPos().offset(direction));
        }
    }

    private void scheduleBusScanInAdjacentBusElements() {
        final World world = Objects.requireNonNull(tileEntity.getWorld());
        final BlockPos pos = tileEntity.getPos();
        for (final Direction direction : Direction.values()) {
            final BlockPos neighborPos = pos.offset(direction);
            final TileEntity tileEntity = WorldUtils.getTileEntityIfChunkExists(world, neighborPos);
            if (tileEntity == null) {
                continue;
            }

            final LazyOptional<DeviceBusElement> capability = tileEntity
                    .getCapability(Capabilities.DEVICE_BUS_ELEMENT_CAPABILITY, direction.getOpposite());
            capability.ifPresent(DeviceBus::scheduleScan);
        }
    }
}
