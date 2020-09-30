package li.cil.oc2.common.network;

import li.cil.oc2.common.tile.ComputerTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public final class TerminalBlockInputMessage extends AbstractTerminalBlockMessage {
    public TerminalBlockInputMessage(final ComputerTileEntity tileEntity, final ByteBuffer data) {
        super(tileEntity, data);
    }

    public TerminalBlockInputMessage(final PacketBuffer buffer) {
        super(buffer);
    }

    public static boolean handleInput(final AbstractTerminalBlockMessage message, final Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> MessageUtils.withServerTileEntityAt(context, message.pos, ComputerTileEntity.class,
                (tileEntity) -> tileEntity.getTerminal().putInput(ByteBuffer.wrap(message.data))));
        return true;
    }
}
