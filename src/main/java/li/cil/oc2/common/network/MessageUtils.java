package li.cil.oc2.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MessageUtils {
    @SuppressWarnings("unchecked")
    public static <T extends TileEntity> void withServerTileEntityAt(final Supplier<NetworkEvent.Context> context, final BlockPos pos, final Class<T> clazz, final Consumer<T> callback) {
        final ServerPlayerEntity player = context.get().getSender();
        if (player == null) {
            return;
        }

        final ServerWorld world = player.getServerWorld();
        final ChunkPos chunkPos = new ChunkPos(pos);
        if (world.chunkExists(chunkPos.x, chunkPos.z)) {
            final TileEntity tileEntity = world.getTileEntity(pos);
            if (clazz.isInstance(tileEntity)) {
                callback.accept((T) tileEntity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends TileEntity> void withClientTileEntityAt(final BlockPos pos, final Class<T> clazz, final Consumer<T> callback) {
        final ClientWorld world = Minecraft.getInstance().world;
        if (world == null) {
            return;
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (clazz.isInstance(tileEntity)) {
            callback.accept((T) tileEntity);
        }
    }
}
