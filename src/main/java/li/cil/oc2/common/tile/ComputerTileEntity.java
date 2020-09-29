package li.cil.oc2.common.tile;

import it.unimi.dsi.fastutil.bytes.ByteArrayFIFOQueue;
import li.cil.oc2.OpenComputers;
import li.cil.oc2.client.gui.terminal.Terminal;
import li.cil.oc2.common.vm.VirtualMachineRunner;
import li.cil.sedna.api.Sizes;
import li.cil.sedna.api.device.PhysicalMemory;
import li.cil.sedna.buildroot.Buildroot;
import li.cil.sedna.device.block.ByteBufferBlockDevice;
import li.cil.sedna.device.memory.Memory;
import li.cil.sedna.device.serial.UART16550A;
import li.cil.sedna.device.virtio.VirtIOBlockDevice;
import li.cil.sedna.riscv.R5Board;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.InputStream;

public final class ComputerTileEntity extends TileEntity implements ITickableTileEntity {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Terminal terminal = new Terminal();

    private VirtualMachineRunner runner;
    private R5Board board;
    private PhysicalMemory rom;
    private PhysicalMemory ram;
    private UART16550A uart;
    private VirtIOBlockDevice hdd;

    public ComputerTileEntity() {
        super(OpenComputers.COMPUTER_TILE_ENTITY.get());
    }

    public void start() {
        startVirtualMachine();
    }

    public void stop() {
        stopVirtualMachine();
    }

    @Override
    public void tick() {
        if (world == null || world.isRemote()) {
            return;
        }

        if (runner != null) {
            runner.tick();
        }
    }

    @Override
    public void remove() {
        super.remove();
        stopVirtualMachine();
    }

    @Override
    public void onChunkUnloaded() {
        stopVirtualMachine();
    }

    @Override
    public void read(final CompoundNBT compound) {
        super.read(compound);
        joinVirtualMachine();
        // TODO deserialize VM
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound) {
        final CompoundNBT result = super.write(compound);
        joinVirtualMachine();
        // todo serialize VM
        return result;
    }

    private void startVirtualMachine() {
        if (runner == null) {
            try {
                createVirtualMachine();
            } catch (final Throwable e) {
                LOGGER.warn(e);
            }
        }
    }

    private void stopVirtualMachine() {
        joinVirtualMachine();
        runner = null;
    }

    private void joinVirtualMachine() {
        if (runner != null) {
            try {
                runner.join();
            } catch (final Throwable e) {
                LOGGER.warn(e);
                runner = null;
            }
        }
    }

    private void createVirtualMachine() throws Throwable {
        board = new R5Board();
        rom = Memory.create(128 * 1024);
        ram = Memory.create(32 * 1024 * 1204);
        hdd = new VirtIOBlockDevice(board.getMemoryMap(),
                ByteBufferBlockDevice.createFromStream(Buildroot.getRootFilesystem(), true));
        uart = new UART16550A();

        hdd.getInterrupt().set(0x1, board.getInterruptController());
        uart.getInterrupt().set(0x2, board.getInterruptController());

        board.addDevice(uart);
        board.addDevice(hdd);
        board.addDevice(0x80000000, rom);
        board.addDevice(0x80000000 + 0x400000, ram);

        board.setBootargs("console=ttyS0 root=/dev/vda ro");

        board.reset();

        loadProgramFile(rom, Buildroot.getFirmware());
        loadProgramFile(ram, Buildroot.getLinuxImage());

        runner = new ConsoleRunner(board);
    }

    private static void loadProgramFile(final PhysicalMemory memory, final InputStream stream) throws Throwable {
        final BufferedInputStream bis = new BufferedInputStream(stream);
        for (int address = 0, value = bis.read(); value != -1; value = bis.read(), address++) {
            memory.store(address, (byte) value, Sizes.SIZE_8_LOG2);
        }
    }

    private final class ConsoleRunner extends VirtualMachineRunner {
        // Thread-local buffers for lock-free read/writes in inner loop.
        private final ByteArrayFIFOQueue outputBuffer = new ByteArrayFIFOQueue(1024);
        private final ByteArrayFIFOQueue inputBuffer = new ByteArrayFIFOQueue(32);

        public ConsoleRunner(final R5Board board) {
            super(board);
        }

        @Override
        protected void handleBeforeRun() {
            int value;
            while ((value = terminal.readInput()) != -1) {
                inputBuffer.enqueue((byte) value);
            }
        }

        @Override
        protected void step() {
            while (!inputBuffer.isEmpty() && uart.canPutByte()) {
                uart.putByte(inputBuffer.dequeueByte());
            }

            int value;
            while ((value = uart.read()) != -1) {
                outputBuffer.enqueue((byte) value);
            }
        }

        @Override
        protected void handleAfterRun() {
            while (!outputBuffer.isEmpty()) {
                terminal.putOutput(outputBuffer.dequeueByte());
            }
        }
    }
}
