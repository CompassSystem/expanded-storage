package ellemes.expandedstorage.thread;

import ellemes.expandedstorage.api.inventory.AbstractHandler;
import ellemes.expandedstorage.common.inventory.ServerScreenHandlerFactory;
import ellemes.expandedstorage.common.misc.ClientPlatformHelper;
import ellemes.expandedstorage.common.misc.PlatformHelper;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.recipe.block.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.EntityConversionRecipe;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ThreadPlatformHelper implements PlatformHelper {
    private ThreadClientHelper clientHelper;

    private final ExtendedScreenHandlerType<AbstractHandler> menuType;
    private MinecraftServer minecraftServer;

    {
        menuType = Registry.register(Registry.MENU, Utils.HANDLER_TYPE_ID, new ExtendedScreenHandlerType<>(AbstractHandler::createClientMenu));
    }

    public static ThreadPlatformHelper instance() {
        return (ThreadPlatformHelper) PlatformHelper.instance();
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

    @Override
    public void sendConversionRecipesToClient(@Nullable ServerPlayer target, List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeCollection(blockRecipes, (b, recipe) -> recipe.writeToBuffer(b));
        buffer.writeCollection(entityRecipes, (b, recipe) -> recipe.writeToBuffer(b));
        if (target == null) {
            for (ServerPlayer player : minecraftServer.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, ThreadMain.UPDATE_RECIPES_ID, buffer);
            }
        } else {
            ServerPlayNetworking.send(target, ThreadMain.UPDATE_RECIPES_ID, buffer);
        }
    }

    public void setServerInstance(MinecraftServer server) {
        this.minecraftServer = server;
    }
}
