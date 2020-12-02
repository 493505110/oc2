package li.cil.oc2.api.bus.device.object;

import li.cil.oc2.api.bus.device.DeviceInterface;
import li.cil.oc2.api.bus.device.DeviceMethod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A reflection based implementation of {@link DeviceInterface} using the {@link Callback}
 * annotation to discover {@link DeviceMethod}s in a target object via
 * {@link Callbacks#collectMethods(Object)}.
 */
public final class ObjectDeviceInterface implements DeviceInterface {
    private final Object object;
    private final ArrayList<String> typeNames;
    private final List<DeviceMethod> methods;
    private final String className;

    /**
     * Creates a new object device with methods in the specified object and the
     * specified list of type names.
     *
     * @param object    the object containing methods provided by this device.
     * @param typeNames the type names of the device.
     */
    public ObjectDeviceInterface(final Object object, final List<String> typeNames) {
        this.object = object;
        this.typeNames = new ArrayList<>(typeNames);
        this.methods = Callbacks.collectMethods(object);
        this.className = object.getClass().getSimpleName();

        if (object instanceof NamedDevice) {
            this.typeNames.addAll(((NamedDevice) object).getDeviceTypeNames());
        }
    }

    /**
     * Creates a new object device with methods in the specified object and the specified
     * type name. For convenience, the type name may be {@code null}, in which case using
     * this constructor is equivalent to using {@link #ObjectDeviceInterface(Object)}.
     *
     * @param object   the object containing methods provided by this device.
     * @param typeName the type name of the device.
     */
    public ObjectDeviceInterface(final Object object, @Nullable final String typeName) {
        this(object, typeName != null ? Collections.singletonList(typeName) : Collections.emptyList());
    }

    /**
     * Creates a new object device with methods in the specified object and no explicit type name.
     *
     * @param object the object containing the methods provided by this device.
     */
    public ObjectDeviceInterface(final Object object) {
        this(object, Collections.emptyList());
    }

    @Override
    public List<String> getTypeNames() {
        return typeNames;
    }

    @Override
    public List<DeviceMethod> getMethods() {
        return methods;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ObjectDeviceInterface that = (ObjectDeviceInterface) o;
        return object.equals(that.object);
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    @Override
    public String toString() {
        return className;
    }
}
