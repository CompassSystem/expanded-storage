package compasses.expandedstorage.fabric;

import compasses.expandedstorage.common.inventory.handler.AbstractHandler;
import compasses.expandedstorage.common.inventory.handler.ServerScreenHandlerFactory;
import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.common.recipe.BlockConversionRecipe;
import compasses.expandedstorage.common.recipe.EntityConversionRecipe;
import compasses.expandedstorage.thread.misc.ScreenHandlerFactoryAdapter;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
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

import java.nio.file.Path;
import java.util.List;

public class FabricCommonHelper {
    private final ExtendedScreenHandlerType<AbstractHandler> menuType;
    private MinecraftServer minecraftServer;

    {
        menuType = Registry.register(BuiltInRegistries.MENU, Utils.HANDLER_TYPE_ID, new ExtendedScreenHandlerType<>(AbstractHandler::createClientMenu));
    }

    public MenuType<AbstractHandler> getScreenHandlerType() {
        return menuType;
    }

    public void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType) {
        player.openMenu(new ScreenHandlerFactoryAdapter(title, inventory, factory, forcedScreenType));
    }

    public void sendConversionRecipesToClient(@Nullable ServerPlayer target, List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeCollection(blockRecipes, (b, recipe) -> recipe.writeToBuffer(b));
        buffer.writeCollection(entityRecipes, (b, recipe) -> recipe.writeToBuffer(b));
        if (target == null) {
            for (ServerPlayer player : minecraftServer.getPlayerList().getPlayers()) {
                sendPacket(player, FabricMain.UPDATE_RECIPES_ID, buffer);
            }
        } else {
            sendPacket(target, FabricMain.UPDATE_RECIPES_ID, buffer);
        }
    }

    public boolean canDestroyBamboo(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    public void setServerInstance(MinecraftServer server) {
        this.minecraftServer = server;
    }

    public Path getGlobalConfigPath() {
        Path minecraftPath = Path.of(System.getProperty("user.home"));
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows")) {
            minecraftPath = minecraftPath.resolve("AppData/Roaming/.minecraft/");
        } else if (os.startsWith("Mac")) {
            minecraftPath = minecraftPath.resolve("Library/Application Support/minecraft/");
        } else { // Assume Linux
            minecraftPath = minecraftPath.resolve(".minecraft/");
        }

        return minecraftPath.resolve(Utils.MOD_ID + "_global_config/");
    }

    protected void sendPacket(ServerPlayer player, ResourceLocation packetId, FriendlyByteBuf buffer) {
        ServerPlayNetworking.send(player, packetId, buffer); // canSend doesn't work :think:
    }

    public Path getLocalConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(Utils.MOD_ID);
    }
}
