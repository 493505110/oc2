package li.cil.oc2.common.util;

import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.Nullable;

public final class NBTUtils {
    public static <T extends Enum<T>> void putEnum(final CompoundTag compound, final String key, @Nullable final Enum<T> value) {
        if (value != null) {
            compound.putInt(key, value.ordinal());
        }
    }

    @Nullable
    public static <T extends Enum<T>> T getEnum(final CompoundTag compound, final String key, final Class<T> enumType) {
        if (!compound.contains(key, NBTTagIds.TAG_INT)) {
            return null;
        }

        final int ordinal = compound.getInt(key);
        try {
            return enumType.getEnumConstants()[ordinal];
        } catch (final IndexOutOfBoundsException ignored) {
            return null;
        }
    }
}
