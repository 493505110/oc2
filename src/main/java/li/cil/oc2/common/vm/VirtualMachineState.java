package li.cil.oc2.common.vm;

import li.cil.oc2.common.bus.AbstractDeviceBusController;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public interface VirtualMachineState {
    boolean isRunning();

    AbstractDeviceBusController.BusState getBusState();

    @OnlyIn(Dist.CLIENT)
    void setBusStateClient(AbstractDeviceBusController.BusState value);

    RunState getRunState();

    @OnlyIn(Dist.CLIENT)
    void setRunStateClient(RunState value);

    @Nullable
    ITextComponent getBootError();

    @OnlyIn(Dist.CLIENT)
    void setBootErrorClient(ITextComponent value);

    void start();

    void stop();

    public enum RunState {
        STOPPED,
        LOADING_DEVICES,
        RUNNING,
    }
}
