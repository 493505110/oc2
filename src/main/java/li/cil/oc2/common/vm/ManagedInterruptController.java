package li.cil.oc2.common.vm;

import li.cil.sedna.api.device.InterruptController;

public final class ManagedInterruptController implements InterruptController {
    private final InterruptController interruptController;
    private final ManagedInterruptAllocator allocator;
    private int managedInterrupts = 0;
    private boolean isValid = true;

    public ManagedInterruptController(final InterruptController interruptController, final ManagedInterruptAllocator allocator) {
        this.interruptController = interruptController;
        this.allocator = allocator;
    }

    public void invalidate() {
        isValid = false;
        interruptController.lowerInterrupts(managedInterrupts);
        managedInterrupts = 0;
    }

    @Override
    public void raiseInterrupts(final int mask) {
        if (!isValid) {
            throw new IllegalArgumentException();
        }

        if (allocator.isMaskValid(mask)) {
            interruptController.raiseInterrupts(mask);
            managedInterrupts |= mask;
        } else {
            throw new IllegalArgumentException("Trying to raise interrupt not allocated by this context.");
        }
    }

    @Override
    public void lowerInterrupts(final int mask) {
        if (!isValid) {
            throw new IllegalArgumentException();
        }

        if (allocator.isMaskValid(mask)) {
            interruptController.lowerInterrupts(mask);
            managedInterrupts &= ~managedInterrupts;
        } else {
            throw new IllegalArgumentException("Trying to lower interrupt not allocated by this context.");
        }
    }

    @Override
    public int getRaisedInterrupts() {
        return interruptController.getRaisedInterrupts();
    }
}
