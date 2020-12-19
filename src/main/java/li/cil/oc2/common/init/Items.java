package li.cil.oc2.common.init;

import li.cil.oc2.Constants;
import li.cil.oc2.api.API;
import li.cil.oc2.common.item.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class Items {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, API.MOD_ID);

    ///////////////////////////////////////////////////////////////////

    public static final RegistryObject<Item> COMPUTER_ITEM = register(Constants.COMPUTER_BLOCK_NAME, Blocks.COMPUTER_BLOCK);
    public static final RegistryObject<Item> BUS_CABLE_ITEM = register(Constants.BUS_CABLE_BLOCK_NAME, Blocks.BUS_CABLE_BLOCK);
    public static final RegistryObject<Item> REDSTONE_INTERFACE_ITEM = register(Constants.REDSTONE_INTERFACE_BLOCK_NAME, Blocks.REDSTONE_INTERFACE_BLOCK);
    public static final RegistryObject<Item> SCREEN_ITEM = register(Constants.SCREEN_BLOCK_NAME, Blocks.SCREEN_BLOCK);

    ///////////////////////////////////////////////////////////////////

    public static final RegistryObject<Item> HDD_ITEM = register(Constants.HDD_ITEM_NAME);
    public static final RegistryObject<Item> RAM_8M_ITEM = register(Constants.RAM_ITEM_NAME);

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    ///////////////////////////////////////////////////////////////////

    private static RegistryObject<Item> register(final String name) {
        return ITEMS.register(name, () -> new Item(commonProperties()));
    }

    private static RegistryObject<Item> register(final String name, final RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), commonProperties()));
    }

    private static Item.Properties commonProperties() {
        return new Item.Properties().group(ItemGroup.COMMON);
    }
}
