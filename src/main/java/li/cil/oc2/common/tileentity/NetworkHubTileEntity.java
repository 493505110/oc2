package li.cil.oc2.common.tileentity;

import li.cil.oc2.api.capabilities.NetworkInterface;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

public final class NetworkHubTileEntity extends AbstractTileEntity implements NetworkInterface {
    private static final int TTL_COST = 1;

    ///////////////////////////////////////////////////////////////////

    private final NetworkInterface[] adjacentInterfaces = new NetworkInterface[Constants.BLOCK_FACE_COUNT];
    private boolean areAdjacentInterfacesDirty = true;

    ///////////////////////////////////////////////////////////////////

    public NetworkHubTileEntity() {
        super(TileEntities.NETWORK_HUB_TILE_ENTITY.get());
    }

    ///////////////////////////////////////////////////////////////////

    public void handleNeighborChanged() {
        areAdjacentInterfacesDirty = true;
    }

    @Override
    public byte[] readEthernetFrame() {
        return null;
    }

    @Override
    public void writeEthernetFrame(final NetworkInterface source, final byte[] frame, final int timeToLive) {
        validateAdjacentInterfaces();

        for (int i = 0; i < adjacentInterfaces.length; i++) {
            if (adjacentInterfaces[i] != null) {
                adjacentInterfaces[i].writeEthernetFrame(this, frame, timeToLive - TTL_COST);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void collectCapabilities(final CapabilityCollector collector, @Nullable final Direction direction) {
        collector.offer(Capabilities.NETWORK_INTERFACE, this);
    }

    ///////////////////////////////////////////////////////////////////

    private void validateAdjacentInterfaces() {
        if (!areAdjacentInterfacesDirty || isRemoved()) {
            return;
        }

        areAdjacentInterfacesDirty = false;

        final World world = getWorld();
        if (world == null || world.isRemote()) {
            return;
        }

        final BlockPos pos = getPos();
        for (final Direction side : Constants.DIRECTIONS) {
            adjacentInterfaces[side.getIndex()] = null;

            final TileEntity neighborTileEntity = world.getTileEntity(pos.offset(side));
            if (neighborTileEntity != null) {
                final LazyOptional<NetworkInterface> capability = neighborTileEntity.getCapability(Capabilities.NETWORK_INTERFACE, side.getOpposite());
                capability.ifPresent(adjacentInterface -> {
                    adjacentInterfaces[side.getIndex()] = adjacentInterface;
                    capability.addListener(unused -> handleNeighborChanged());
                });
            }
        }
    }
}
