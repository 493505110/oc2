package li.cil.oc2.common.bus;

import li.cil.oc2.api.bus.DeviceBusController;
import li.cil.oc2.api.bus.DeviceBusElement;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.bus.device.DeviceInterface;
import li.cil.oc2.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class TileEntityDeviceBusController implements DeviceBusController {
    public enum State {
        SCAN_PENDING,
        TOO_COMPLEX,
        MULTIPLE_CONTROLLERS,
        READY,
    }

    private static final int MAX_BUS_ELEMENT_COUNT = 128;

    private final TileEntity tileEntity;
    @Nullable private final Runnable onBeforeClearDevices;

    private final Set<DeviceBusElement> elements = new HashSet<>();
    private final ConcurrentHashMap<UUID, Device> devices = new ConcurrentHashMap<>();

    private int scanDelay;

    public TileEntityDeviceBusController(final TileEntity tileEntity) {
        this(tileEntity, null);
    }

    public TileEntityDeviceBusController(final TileEntity tileEntity, @Nullable final Runnable onBeforeBusScan) {
        this.tileEntity = tileEntity;
        this.onBeforeClearDevices = onBeforeBusScan;
    }

    @Override
    public void scheduleBusScan() {
        if (!devices.isEmpty() && onBeforeClearDevices != null) {
            onBeforeClearDevices.run();
        }

        for (final DeviceBusElement element : elements) {
            element.removeController(this);
        }

        elements.clear();
        devices.clear();

        scanDelay = 0; // scan as soon as possible
    }

    @Override
    public void scanDevices() {
        devices.clear();

        final HashMap<DeviceInterface, ArrayList<Device>> groupedDevices = new HashMap<>();

        for (final DeviceBusElement element : elements) {
            for (final Device device : element.getLocalDevices()) {
                groupedDevices.computeIfAbsent(device.getIdentifiedDevice(), d -> new ArrayList<>()).add(device);
            }
        }

        for (final ArrayList<Device> group : groupedDevices.values()) {
            final Device device = selectDeviceDeterministically(group);
            devices.putIfAbsent(device.getUniqueIdentifier(), device);
        }
    }

    @Override
    public Collection<Device> getDevices() {
        return devices.values();
    }

    @Override
    public Optional<Device> getDevice(final UUID uuid) {
        return Optional.ofNullable(devices.get(uuid));
    }

    public State scan() {
        if (scanDelay < 0) {
            return State.READY;
        }

        if (scanDelay-- > 0) {
            return State.SCAN_PENDING;
        }

        assert scanDelay == -1;

        final World world = tileEntity.getWorld();
        if (world == null || world.isRemote()) {
            return State.SCAN_PENDING;
        }

        final Stack<ScanEdge> queue = new Stack<>();
        final HashSet<ScanEdge> seenEdges = new HashSet<>(); // to avoid duplicate edge scans
        final HashSet<BlockPos> busPositions = new HashSet<>(); // to track number of seen blocks for limit

        final Direction[] faces = Direction.values();
        for (final Direction face : faces) {
            final ScanEdge edgeIn = new ScanEdge(tileEntity.getPos(), face);
            queue.add(edgeIn);
            seenEdges.add(edgeIn);
        }

        // When we belong to a bus with multiple controllers we finish the scan and register
        // with all bus elements so that an element can easily trigger a scan on all connected
        // controllers -- without having to scan through the bus itself.
        boolean hasMultipleControllers = false;
        while (!queue.isEmpty()) {
            final ScanEdge edge = queue.pop();
            assert seenEdges.contains(edge);

            final ChunkPos chunkPos = new ChunkPos(edge.position);
            if (!world.chunkExists(chunkPos.x, chunkPos.z)) {
                // If we have an unloaded chunk neighbor we cannot know whether our neighbor in that
                // chunk would cause a scan once it is loaded, so we'll just retry every so often.
                scanDelay = 20;
                elements.clear();
                return State.SCAN_PENDING;
            }

            final TileEntity tileEntity = world.getTileEntity(edge.position);
            if (tileEntity == null) {
                for (final Direction face : faces) {
                    seenEdges.add(new ScanEdge(edge.position, face));
                }

                continue;
            }

            if (tileEntity.getCapability(Capabilities.DEVICE_BUS_CONTROLLER_CAPABILITY, edge.face)
                    .map(controller -> !Objects.equals(controller, this)).orElse(false)) {
                hasMultipleControllers = true;
            }

            final LazyOptional<DeviceBusElement> capability = tileEntity.getCapability(Capabilities.DEVICE_BUS_ELEMENT_CAPABILITY, edge.face);
            if (capability.isPresent()) {
                if (busPositions.add(edge.position) && busPositions.size() > MAX_BUS_ELEMENT_COUNT) {
                    elements.clear();
                    return State.TOO_COMPLEX; // This return is the reason this is not in the ifPresent below.
                }
            }

            capability.ifPresent(element -> {
                elements.add(element);

                for (final Direction face : faces) {
                    final LazyOptional<DeviceBusElement> otherCapability = tileEntity.getCapability(Capabilities.DEVICE_BUS_ELEMENT_CAPABILITY, face);
                    otherCapability.ifPresent(otherElement -> {
                        final boolean isConnectedToIncomingEdge = Objects.equals(otherElement, element);
                        if (!isConnectedToIncomingEdge) {
                            return;
                        }

                        final ScanEdge edgeIn = new ScanEdge(edge.position, face);
                        seenEdges.add(edgeIn);

                        final ScanEdge edgeOut = new ScanEdge(edge.position.offset(face), face.getOpposite());
                        if (seenEdges.add(edgeOut)) {
                            queue.add(edgeOut);
                        }
                    });
                }
            });
        }

        for (final DeviceBusElement element : elements) {
            element.addController(this);
        }

        if (hasMultipleControllers) {
            return State.MULTIPLE_CONTROLLERS;
        }

        scanDevices();

        return State.READY;
    }

    private static Device selectDeviceDeterministically(final ArrayList<Device> devices) {
        Device deviceWithLowestUuid = devices.get(0);
        for (int i = 1; i < devices.size(); i++) {
            final Device device = devices.get(i);
            if (device.getUniqueIdentifier().compareTo(deviceWithLowestUuid.getUniqueIdentifier()) < 0) {
                deviceWithLowestUuid = device;
            }
        }

        return deviceWithLowestUuid;
    }

    private static final class ScanEdge {
        public final BlockPos position;
        public final Direction face;

        public ScanEdge(final BlockPos position, final Direction face) {
            this.position = position;
            this.face = face;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ScanEdge scanEdge = (ScanEdge) o;
            return position.equals(scanEdge.position) &&
                   face == scanEdge.face;
        }

        @Override
        public int hashCode() {
            return Objects.hash(position, face);
        }
    }
}