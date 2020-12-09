package li.cil.oc2.common.network.message;

import li.cil.oc2.common.bus.TileEntityDeviceBusController;
import li.cil.oc2.common.network.MessageUtils;
import li.cil.oc2.common.tile.ComputerTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ComputerBusStateMessage {
    private BlockPos pos;
    private TileEntityDeviceBusController.State busState;

    public ComputerBusStateMessage(final ComputerTileEntity tileEntity) {
        this.pos = tileEntity.getPos();
        this.busState = tileEntity.getBusState();
    }

    public ComputerBusStateMessage(final PacketBuffer buffer) {
        fromBytes(buffer);
    }

    public static boolean handleMessage(final ComputerBusStateMessage message, final Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> MessageUtils.withClientTileEntityAt(message.pos, ComputerTileEntity.class,
                (tileEntity) -> tileEntity.setBusStateClient(message.busState)));
        return true;
    }

    public void fromBytes(final PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        busState = buffer.readEnumValue(TileEntityDeviceBusController.State.class);
    }

    public static void toBytes(final ComputerBusStateMessage message, final PacketBuffer buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeEnumValue(message.busState);
    }
}
