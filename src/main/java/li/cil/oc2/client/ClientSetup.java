package li.cil.oc2.client;

import li.cil.oc2.client.gui.ComputerContainerScreen;
import li.cil.oc2.client.renderer.NetworkCableRenderer;
import li.cil.oc2.client.renderer.tileentity.ComputerTileEntityRenderer;
import li.cil.oc2.common.container.Containers;
import li.cil.oc2.common.tileentity.TileEntities;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientSetup {
    public static void run(final FMLClientSetupEvent event) {
        NetworkCableRenderer.initialize();

        ScreenManager.registerFactory(Containers.COMPUTER_CONTAINER.get(), ComputerContainerScreen::new);

        ClientRegistry.bindTileEntityRenderer(TileEntities.COMPUTER_TILE_ENTITY.get(), ComputerTileEntityRenderer::new);
    }
}
