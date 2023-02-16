package ellemes.expandedstorage.thread;

import ellemes.expandedstorage.api.inventory.AbstractHandler;
import ellemes.expandedstorage.common.inventory.ServerScreenHandlerFactory;
import ellemes.expandedstorage.common.misc.ClientPlatformHelper;
import ellemes.expandedstorage.common.misc.PlatformHelper;
import ellemes.expandedstorage.common.misc.Utils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;

public class ThreadPlatformHelper implements PlatformHelper {
    private ThreadClientHelper clientHelper;

    private final ExtendedScreenHandlerType<AbstractHandler> menuType;

    {
        menuType = Registry.register(Registry.MENU, Utils.HANDLER_TYPE_ID, new ExtendedScreenHandlerType<>(AbstractHandler::createClientMenu));
    }

    @Override
    public ClientPlatformHelper clientHelper() {
        if (clientHelper == null) {
            clientHelper = new ThreadClientHelper();
        }
        return clientHelper;
    }

    @Override
    public MenuType<AbstractHandler> getScreenHandlerType() {
        return menuType;
    }

    @Override
    public void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType) {
        player.openMenu(new ScreenHandlerFactoryAdapter(title, inventory, factory, forcedScreenType));
    }
}
