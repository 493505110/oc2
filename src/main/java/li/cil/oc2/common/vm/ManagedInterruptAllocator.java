package li.cil.oc2.common.vm;

import li.cil.oc2.api.bus.device.sedna.InterruptAllocator;
import li.cil.sedna.riscv.device.R5PlatformLevelInterruptController;

import java.util.BitSet;
import java.util.OptionalInt;

public final class ManagedInterruptAllocator implements InterruptAllocator {
    private final BitSet interrupts;
    private final BitSet reservedInterrupts;
    private final BitSet managedInterrupts;
    private final int interruptCount;
    private boolean isFrozen;
    private int managedMask;

    public ManagedInterruptAllocator(final BitSet interrupts, final BitSet reservedInterrupts, final int interruptCount) {
        this.interrupts = interrupts;
        this.reservedInterrupts = reservedInterrupts;
        this.managedInterrupts = new BitSet();
        this.interruptCount = interruptCount;
    }

    public void freeze() {
        isFrozen = true;
        managedMask = (int) managedInterrupts.toLongArray()[0];
    }

    public void invalidate() {
        interrupts.andNot(managedInterrupts);
        managedMask = 0;
    }

    public boolean isMaskValid(final int mask) {
        return (mask & ~managedMask) == 0;
    }

    @Override
    public OptionalInt claimInterrupt(final int interrupt) {
        if (isFrozen) {
            throw new IllegalStateException();
        }

        if (interrupt < 1 || interrupt > R5PlatformLevelInterruptController.INTERRUPT_COUNT) {
            throw new IllegalArgumentException();
        }

        final int interruptBit = interrupt - 1;
        if (interrupts.get(interruptBit)) {
            return claimInterrupt();
        } else {
            interrupts.set(interruptBit);
            reservedInterrupts.set(interruptBit);
            managedInterrupts.set(interruptBit);
            return OptionalInt.of(interrupt);
        }
    }

    @Override
    public OptionalInt claimInterrupt() {
        if (isFrozen) {
            throw new IllegalStateException();
        }

        final int interruptBit = reservedInterrupts.nextClearBit(0);
        if (interruptBit >= interruptCount) {
            return OptionalInt.empty();
        }

        interrupts.set(interruptBit);
        reservedInterrupts.set(interruptBit);
        managedInterrupts.set(interruptBit);

        final int interrupt = interruptBit + 1;
        return OptionalInt.of(interrupt);
    }
}
