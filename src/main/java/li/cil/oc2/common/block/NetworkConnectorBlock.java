package li.cil.oc2.common.block;

import li.cil.oc2.common.tileentity.NetworkConnectorTileEntity;
import li.cil.oc2.common.tileentity.TileEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Objects;

public final class NetworkConnectorBlock extends HorizontalFaceBlock {
    private static final VoxelShape NEG_Z_SHAPE = Block.makeCuboidShape(5, 5, 7, 11, 11, 16);
    private static final VoxelShape POS_Z_SHAPE = Block.makeCuboidShape(5, 5, 0, 11, 11, 9);
    private static final VoxelShape NEG_X_SHAPE = Block.makeCuboidShape(7, 5, 5, 16, 11, 11);
    private static final VoxelShape POS_X_SHAPE = Block.makeCuboidShape(0, 5, 5, 9, 11, 11);
    private static final VoxelShape NEG_Y_SHAPE = Block.makeCuboidShape(5, 0, 5, 11, 9, 11);
    private static final VoxelShape POS_Y_SHAPE = Block.makeCuboidShape(5, 7, 5, 11, 16, 11);

    ///////////////////////////////////////////////////////////////////

    public NetworkConnectorBlock() {
        super(Properties
                .create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(1.5f, 6.0f));
        setDefaultState(getStateContainer().getBaseState()
                .with(HORIZONTAL_FACING, Direction.NORTH)
                .with(FACE, AttachFace.WALL));
    }

    ///////////////////////////////////////////////////////////////////

    public static Direction getFacing(final BlockState state) {
        return HorizontalFaceBlock.getFacing(state);
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return TileEntities.NETWORK_CONNECTOR_TILE_ENTITY.get().create();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(final BlockState state, final World world, final BlockPos pos, final Block changedBlock, final BlockPos changedBlockPos, final boolean isMoving) {
        if (Objects.equals(changedBlockPos, pos.offset(getFacing(state).getOpposite()))) {
            final TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof NetworkConnectorTileEntity) {
                final NetworkConnectorTileEntity connector = (NetworkConnectorTileEntity) tileEntity;
                connector.setLocalInterfaceChanged();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader world, final BlockPos pos, final ISelectionContext context) {
        switch (state.get(FACE)) {
            case WALL:
                switch (state.get(HORIZONTAL_FACING)) {
                    case EAST:
                        return POS_X_SHAPE;
                    case WEST:
                        return NEG_X_SHAPE;
                    case SOUTH:
                        return POS_Z_SHAPE;
                    case NORTH:
                    default:
                        return NEG_Z_SHAPE;
                }
            case CEILING:
                return POS_Y_SHAPE;
            case FLOOR:
            default:
                return NEG_Y_SHAPE;
        }
    }

    ///////////////////////////////////////////////////////////////////

    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACE, HORIZONTAL_FACING);
    }
}
