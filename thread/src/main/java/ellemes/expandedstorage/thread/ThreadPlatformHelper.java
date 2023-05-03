package ellemes.expandedstorage.thread;

import ellemes.expandedstorage.common.inventory.AbstractHandler;
import ellemes.expandedstorage.common.inventory.ServerScreenHandlerFactory;
import ellemes.expandedstorage.common.misc.PlatformHelper;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.recipe.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.EntityConversionRecipe;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ThreadPlatformHelper implements PlatformHelper {
    private final ExtendedScreenHandlerType<AbstractHandler> menuType;
    private MinecraftServer minecraftServer;

    {
        menuType = Registry.register(BuiltInRegistries.MENU, Utils.HANDLER_TYPE_ID, new ExtendedScreenHandlerType<>(AbstractHandler::createClientMenu));
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
                sendPacket(player, ThreadMain.UPDATE_RECIPES_ID, buffer);
            }
        } else {
            sendPacket(target, ThreadMain.UPDATE_RECIPES_ID, buffer);
        }
    }

    protected abstract void sendPacket(ServerPlayer player, ResourceLocation packetId, FriendlyByteBuf buffer);

    @Override
    public boolean canDestroyBamboo(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    public void setServerInstance(MinecraftServer server) {
        this.minecraftServer = server;
    }
}
