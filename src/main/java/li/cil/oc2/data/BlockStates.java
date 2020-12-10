package li.cil.oc2.data;

import com.google.common.collect.Maps;
import li.cil.oc2.OpenComputers;
import li.cil.oc2.api.API;
import li.cil.oc2.common.block.BusCableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder.PartBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;

import java.util.Map;

public class BlockStates extends BlockStateProvider {
    public BlockStates(final DataGenerator generator, final ExistingFileHelper existingFileHelper) {
        super(generator, API.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlock(OpenComputers.COMPUTER_BLOCK, OpenComputers.COMPUTER_ITEM);
        horizontalBlock(OpenComputers.REDSTONE_INTERFACE_BLOCK, OpenComputers.REDSTONE_INTERFACE_ITEM);
        horizontalBlock(OpenComputers.SCREEN_BLOCK, OpenComputers.SCREEN_ITEM);

        registerCableStates();
    }

    private <T extends Comparable<T>> boolean doesStateMatches(final BlockState state, final Map<? extends Property<T>, T> properties) {
        return properties.entrySet().stream().allMatch(entry -> state.get(entry.getKey()) == entry.getValue());
    }

    private void registerCableStates() {
        final ModelFile baseModel = models().getExistingFile(new ResourceLocation(API.MOD_ID, "block/cable_base"));
        final ModelFile linkModel = models().getExistingFile(new ResourceLocation(API.MOD_ID, "block/cable_link"));
        final ModelFile plugModel = models().getExistingFile(new ResourceLocation(API.MOD_ID, "block/cable_plug"));
        final ModelFile straightModel = models().getExistingFile(new ResourceLocation(API.MOD_ID, "block/cable_straight"));

        final MultiPartBlockStateBuilder builder = getMultipartBuilder(OpenComputers.BUS_CABLE_BLOCK.get());

        // Core element, use straight connections if and only if two opposite ends are
        // links and there are no other connections. Since there's no "not" condition we
        // have to provide all permutations for regular core piece... this sucks.
        final Map<EnumProperty<BusCableBlock.ConnectionType>, BusCableBlock.ConnectionType> straightX = Util.make(Maps.newHashMap(), map -> {
            map.put(BusCableBlock.CONNECTION_SOUTH, BusCableBlock.ConnectionType.LINK);
            map.put(BusCableBlock.CONNECTION_NORTH, BusCableBlock.ConnectionType.LINK);
            map.put(BusCableBlock.CONNECTION_UP, BusCableBlock.ConnectionType.NONE);
            map.put(BusCableBlock.CONNECTION_DOWN, BusCableBlock.ConnectionType.NONE);
            map.put(BusCableBlock.CONNECTION_EAST, BusCableBlock.ConnectionType.NONE);
            map.put(BusCableBlock.CONNECTION_WEST, BusCableBlock.ConnectionType.NONE);
        });
        final Map<EnumProperty<BusCableBlock.ConnectionType>, BusCableBlock.ConnectionType> straightY = Util.make(Maps.newHashMap(), map -> {
            map.put(BusCableBlock.CONNECTION_SOUTH, BusCableBlock.ConnectionType.NONE);
            map.put(BusCableBlock.CONNECTION_NORTH, BusCableBlock.ConnectionType.NONE);
            map.put(BusCableBlock.CONNECTION_UP, BusCableBlock.ConnectionType.LINK);
            map.put(BusCableBlock.CONNECTION_DOWN, BusCableBlock.ConnectionType.LINK);
            map.put(BusCableBlock.CONNECTION_EAST, BusCableBlock.ConnectionType.NONE);
            map.put(BusCableBlock.CONNECTION_WEST, BusCableBlock.ConnectionType.NONE);
        });
        final Map<EnumProperty<BusCableBlock.ConnectionType>, BusCableBlock.ConnectionType> straightZ = Util.make(Maps.newHashMap(), map -> {
            map.put(BusCableBlock.CONNECTION_SOUTH, BusCableBlock.ConnectionType.NONE);
            map.put(BusCableBlock.CONNECTION_NORTH, BusCableBlock.ConnectionType.NONE);
            map.put(BusCableBlock.CONNECTION_UP, BusCableBlock.ConnectionType.NONE);
            map.put(BusCableBlock.CONNECTION_DOWN, BusCableBlock.ConnectionType.NONE);
            map.put(BusCableBlock.CONNECTION_EAST, BusCableBlock.ConnectionType.LINK);
            map.put(BusCableBlock.CONNECTION_WEST, BusCableBlock.ConnectionType.LINK);
        });
        for (final BlockState state : OpenComputers.BUS_CABLE_BLOCK.get().getStateContainer().getValidStates()) {
            final ConfiguredModel.Builder<PartBuilder> model = builder.part();
            final PartBuilder part;
            if (doesStateMatches(state, straightX)) {
                part = model.modelFile(straightModel).addModel();
            } else if (doesStateMatches(state, straightY)) {
                part = model.modelFile(straightModel).rotationX(90).addModel();
            } else if (doesStateMatches(state, straightZ)) {
                part = model.modelFile(straightModel).rotationY(90).addModel();
            } else {
                part = model.modelFile(baseModel).addModel();
            }
            part
                    .condition(BusCableBlock.CONNECTION_SOUTH, state.get(BusCableBlock.CONNECTION_SOUTH))
                    .condition(BusCableBlock.CONNECTION_NORTH, state.get(BusCableBlock.CONNECTION_NORTH))
                    .condition(BusCableBlock.CONNECTION_EAST, state.get(BusCableBlock.CONNECTION_EAST))
                    .condition(BusCableBlock.CONNECTION_WEST, state.get(BusCableBlock.CONNECTION_WEST))
                    .condition(BusCableBlock.CONNECTION_UP, state.get(BusCableBlock.CONNECTION_UP))
                    .condition(BusCableBlock.CONNECTION_DOWN, state.get(BusCableBlock.CONNECTION_DOWN))
                    .end();
        }

        BusCableBlock.FACING_TO_CONNECTION_MAP.forEach((direction, connectionType) -> {
            final int rotationY = (int) direction.getHorizontalAngle();
            final int rotationX;
            if (direction == Direction.UP) {
                rotationX = 90;
            } else if (direction == Direction.DOWN) {
                rotationX = -90;
            } else {
                rotationX = 0;
            }

            builder.part()
                    .modelFile(linkModel)
                    .rotationY(rotationY)
                    .rotationX(rotationX)
                    .addModel()
                    .condition(connectionType, BusCableBlock.ConnectionType.LINK)
                    .end();

            builder.part()
                    .modelFile(plugModel)
                    .rotationY(rotationY)
                    .rotationX(rotationX)
                    .addModel()
                    .condition(connectionType, BusCableBlock.ConnectionType.PLUG)
                    .end();
        });

        itemModels().getBuilder(OpenComputers.BUS_CABLE_ITEM.getId().getPath())
                .parent(baseModel);
    }

    private void horizontalBlock(final RegistryObject<Block> block, final RegistryObject<Item> item) {
        horizontalBlock(block.get(), models().getBuilder(block.getId().getPath()));
        itemModels().getBuilder(item.getId().getPath()).parent(models().getExistingFile(block.getId()));
    }
}
