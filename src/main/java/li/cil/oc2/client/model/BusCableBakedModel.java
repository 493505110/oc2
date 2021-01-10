package li.cil.oc2.client.model;

import li.cil.oc2.common.block.BusCableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BusCableBakedModel implements IDynamicBakedModel {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction.Axis[] AXES = Direction.Axis.values();

    private final IBakedModel proxy;
    private final IBakedModel[] straightModelByAxis;
    private final IBakedModel[] supportModelByFace;

    public BusCableBakedModel(final IBakedModel proxy, final IBakedModel[] straightModelByAxis, final IBakedModel[] supportModelByFace) {
        this.proxy = proxy;
        this.straightModelByAxis = straightModelByAxis;
        this.supportModelByFace = supportModelByFace;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final Random rand, final IModelData extraData) {
        if (state == null) {
            return proxy.getQuads(null, side, rand, extraData);
        }

        for (int i = 0; i < AXES.length; i++) {
            final Direction.Axis axis = AXES[i];
            if (isStraightAlongAxis(state, axis)) {
                return straightModelByAxis[i].getQuads(state, side, rand, extraData);
            }
        }

        final ArrayList<BakedQuad> quads = new ArrayList<>(proxy.getQuads(state, side, rand, extraData));

        final BusCableSupportSide supportSide = extraData.getData(BusCableSupportSide.BUS_CABLE_SUPPORT_PROPERTY);
        if (supportSide != null) {
            quads.addAll(supportModelByFace[supportSide.get().getIndex()].getQuads(state, side, rand, extraData));
        }

        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return proxy.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return proxy.isGui3d();
    }

    @Override
    public boolean isSideLit() {
        return proxy.isSideLit();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return proxy.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return proxy.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return proxy.getOverrides();
    }

    @Override
    public IModelData getModelData(final IBlockDisplayReader world, final BlockPos pos, final BlockState state, final IModelData tileData) {
        return getBusCableSupportSideData(world, pos, state, tileData);
    }

    public static IModelData getBusCableSupportSideData(final IBlockDisplayReader world, final BlockPos pos, final BlockState state, final IModelData tileData) {
        Direction supportSide = null;
        for (final Direction direction : DIRECTIONS) {
            if (isNeighborInDirectionSolid(world, pos, direction)) {
                final EnumProperty<BusCableBlock.ConnectionType> property = BusCableBlock.FACING_TO_CONNECTION_MAP.get(direction);
                if (state.hasProperty(property) && state.get(property) == BusCableBlock.ConnectionType.PLUG) {
                    return tileData; // Plug is already supporting us, bail.
                }

                if (supportSide == null) { // Prefer vertical supports.
                    supportSide = direction;
                }
            }
        }

        if (supportSide != null) {
            return new ModelDataMap.Builder()
                    .withInitial(BusCableSupportSide.BUS_CABLE_SUPPORT_PROPERTY, new BusCableSupportSide(supportSide))
                    .build();
        }

        return tileData;
    }

    ///////////////////////////////////////////////////////////////////

    private static boolean isNeighborInDirectionSolid(final IBlockDisplayReader world, final BlockPos pos, final Direction direction) {
        final BlockPos neighborPos = pos.offset(direction);
        return world.getBlockState(neighborPos).isSolidSide(world, neighborPos, direction.getOpposite());
    }

    private static boolean isStraightAlongAxis(final BlockState state, final Direction.Axis axis) {
        for (final Direction direction : DIRECTIONS) {
            final EnumProperty<BusCableBlock.ConnectionType> property = BusCableBlock.FACING_TO_CONNECTION_MAP.get(direction);
            if (axis.test(direction)) {
                if (state.get(property) != BusCableBlock.ConnectionType.LINK) {
                    return false;
                }
            } else {
                if (state.get(property) != BusCableBlock.ConnectionType.NONE) {
                    return false;
                }
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////

    public static final class BusCableSupportSide {
        public static final ModelProperty<BusCableSupportSide> BUS_CABLE_SUPPORT_PROPERTY = new ModelProperty<>();

        private final Direction value;

        private BusCableSupportSide(final Direction value) {
            this.value = value;
        }

        public Direction get() {
            return value;
        }
    }
}