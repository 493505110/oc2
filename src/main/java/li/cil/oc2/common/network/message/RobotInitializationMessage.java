package li.cil.oc2.common.network.message;

import li.cil.oc2.common.bus.AbstractDeviceBusController;
import li.cil.oc2.common.entity.RobotEntity;
import li.cil.oc2.common.network.MessageUtils;
import li.cil.oc2.common.serialization.NBTSerialization;
import li.cil.oc2.common.vm.VirtualMachineState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class RobotInitializationMessage {
    private int entityId;
    private AbstractDeviceBusController.BusState busState;
    private VirtualMachineState.RunState runState;
    private ITextComponent bootError;
    private CompoundNBT terminal;

    ///////////////////////////////////////////////////////////////////

    public RobotInitializationMessage(final RobotEntity robot) {
        this.entityId = robot.getEntityId();
        this.busState = robot.getState().getBusState();
        this.runState = robot.getState().getRunState();
        this.bootError = robot.getState().getBootError();
        this.terminal = NBTSerialization.serialize(robot.getTerminal());
    }

    public RobotInitializationMessage(final PacketBuffer buffer) {
        fromBytes(buffer);
    }

    ///////////////////////////////////////////////////////////////////

    public static boolean handleMessage(final RobotInitializationMessage message, final Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> MessageUtils.withClientEntity(message.entityId, RobotEntity.class,
                (robot) -> {
                    robot.getState().setBusStateClient(message.busState);
                    robot.getState().setRunStateClient(message.runState);
                    robot.getState().setBootErrorClient(message.bootError);
                    NBTSerialization.deserialize(message.terminal, robot.getTerminal());
                }));
        return true;
    }

    public void fromBytes(final PacketBuffer buffer) {
        entityId = buffer.readVarInt();
        busState = buffer.readEnumValue(AbstractDeviceBusController.BusState.class);
        runState = buffer.readEnumValue(VirtualMachineState.RunState.class);
        bootError = buffer.readTextComponent();
        terminal = buffer.readCompoundTag();
    }

    public static void toBytes(final RobotInitializationMessage message, final PacketBuffer buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeEnumValue(message.busState);
        buffer.writeEnumValue(message.runState);
        buffer.writeTextComponent(message.bootError);
        buffer.writeCompoundTag(message.terminal);
    }
}
