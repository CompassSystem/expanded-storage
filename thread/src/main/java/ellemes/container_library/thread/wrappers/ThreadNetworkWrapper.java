package ellemes.container_library.thread.wrappers;

import ellemes.container_library.inventory.ServerScreenHandlerFactory;
import ellemes.container_library.thread.ScreenHandlerFactoryAdapter;
import ellemes.container_library.wrappers.NetworkWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

public class ThreadNetworkWrapper extends NetworkWrapper {
    public ThreadNetworkWrapper() {
        super();
    }

    @Override
    protected void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType) {
        player.openMenu(new ScreenHandlerFactoryAdapter(title, inventory, factory, forcedScreenType));
    }
}
