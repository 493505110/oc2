package li.cil.oc2.common.item;

import li.cil.oc2.api.API;
import li.cil.oc2.client.renderer.tileentity.RobotItemStackRenderer;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

public final class Items {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, API.MOD_ID);

    ///////////////////////////////////////////////////////////////////

    public static final RegistryObject<Item> COMPUTER = register(Constants.COMPUTER_BLOCK_NAME, Blocks.COMPUTER);
    public static final RegistryObject<Item> BUS_CABLE = register(Constants.BUS_CABLE_BLOCK_NAME, Blocks.BUS_CABLE);
    public static final RegistryObject<Item> NETWORK_CONNECTOR = register(Constants.NETWORK_CONNECTOR_BLOCK_NAME, Blocks.NETWORK_CONNECTOR);
    public static final RegistryObject<Item> NETWORK_HUB = register(Constants.NETWORK_HUB_BLOCK_NAME, Blocks.NETWORK_HUB);
    public static final RegistryObject<Item> REDSTONE_INTERFACE = register(Constants.REDSTONE_INTERFACE_BLOCK_NAME, Blocks.REDSTONE_INTERFACE);

    ///////////////////////////////////////////////////////////////////

    public static final RegistryObject<Item> WRENCH = register(Constants.WRENCH_ITEM_NAME, WrenchItem::new);

    public static final RegistryObject<Item> BUS_INTERFACE = register(Constants.BUS_INTERFACE_ITEM_NAME, BusInterfaceItem::new);
    public static final RegistryObject<Item> NETWORK_CABLE = register(Constants.NETWORK_CABLE_ITEM_NAME, NetworkCableItem::new);
    public static final RegistryObject<Item> ROBOT = register(Constants.ROBOT_ENTITY_NAME, RobotItem::new, commonProperties().setISTER(() -> RobotItemStackRenderer::new));

    public static final RegistryObject<Item> MEMORY = register(Constants.MEMORY_ITEM_NAME, MemoryItem::new, new Item.Properties());
    public static final RegistryObject<Item> HARD_DRIVE = register(Constants.HARD_DRIVE_ITEM_NAME, HardDriveItem::new, new Item.Properties());
    public static final RegistryObject<Item> FLASH_MEMORY = register(Constants.FLASH_MEMORY_ITEM_NAME, FlashMemoryItem::new, new Item.Properties());
    public static final RegistryObject<Item> REDSTONE_INTERFACE_CARD = register(Constants.REDSTONE_INTERFACE_CARD_ITEM_NAME);
    public static final RegistryObject<Item> NETWORK_INTERFACE_CARD = register(Constants.NETWORK_INTERFACE_CARD_ITEM_NAME);

    public static final RegistryObject<Item> INVENTORY_OPERATIONS_MODULE = register(Constants.INVENTORY_OPERATIONS_MODULE_ITEM_NAME);
    public static final RegistryObject<Item> BLOCK_OPERATIONS_MODULE = register(Constants.BLOCK_OPERATIONS_MODULE_ITEM_NAME, commonProperties().maxDamage(2500));

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    ///////////////////////////////////////////////////////////////////

    private static RegistryObject<Item> register(final String name) {
        return register(name, ModItem::new);
    }

    private static RegistryObject<Item> register(final String name, final Item.Properties properties) {
        return register(name, ModItem::new, properties);
    }

    private static <T extends Item> RegistryObject<T> register(final String name, final Function<Item.Properties, T> factory) {
        return register(name, factory, commonProperties());
    }

    private static <T extends Item> RegistryObject<T> register(final String name, final Function<Item.Properties, T> factory, final Item.Properties properties) {
        return ITEMS.register(name, () -> factory.apply(properties));
    }

    private static <T extends Block> RegistryObject<Item> register(final String name, final RegistryObject<T> block) {
        return register(name, (properties) -> new ModBlockItem(block.get(), properties));
    }

    private static Item.Properties commonProperties() {
        return new Item.Properties().group(ItemGroup.COMMON);
    }
}
