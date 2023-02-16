package ellemes.expandedstorage.common.misc;

import ellemes.expandedstorage.api.inventory.AbstractHandler;
import ellemes.expandedstorage.common.inventory.ServerScreenHandlerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

public interface PlatformHelper {
    @ApiStatus.Internal
    PlatformHelper INSTANCE = ServiceLoader.load(PlatformHelper.class).findFirst().orElseThrow();

    static PlatformHelper instance() {
        return INSTANCE;
    }

    ClientPlatformHelper clientHelper();

    MenuType<AbstractHandler> getScreenHandlerType();

    void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType);
}
