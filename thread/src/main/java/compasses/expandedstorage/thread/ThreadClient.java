package compasses.expandedstorage.thread;

import compasses.expandedstorage.common.CommonClient;
import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.client.gui.AbstractScreen;
import compasses.expandedstorage.common.misc.ClientPlatformHelper;
import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.common.recipe.BlockConversionRecipe;
import compasses.expandedstorage.common.recipe.ConversionRecipeManager;
import compasses.expandedstorage.common.recipe.EntityConversionRecipe;
import compasses.expandedstorage.common.registration.Content;
import compasses.expandedstorage.common.registration.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ThreadClient {
    public static void initialize(ClientPlatformHelper helper, Consumer<Content> extraClientInit) {
        CommonClient.initialize(helper);
        MenuScreens.register(CommonMain.platformHelper().getScreenHandlerType(), AbstractScreen::createScreen);

        extraClientInit.accept(ThreadMain.doClientInit());

        ItemProperties.registerGeneric(Utils.id("sparrow"), CommonClient::hasSparrowProperty);
        ItemProperties.register(ModItems.STORAGE_MUTATOR, Utils.id("tool_mode"), CommonClient::currentMutatorToolMode);
    }

    @SuppressWarnings("unused")
    public static void handleUpdateRecipesPacket(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer) {
        List<BlockConversionRecipe<?>> blockRecipes = new ArrayList<>(buffer.readCollection(ArrayList::new, BlockConversionRecipe::readFromBuffer));
        List<EntityConversionRecipe<?>> entityRecipes = new ArrayList<>(buffer.readCollection(ArrayList::new, EntityConversionRecipe::readFromBuffer));
        client.execute(() -> ConversionRecipeManager.INSTANCE.replaceAllRecipes(blockRecipes, entityRecipes));
    }
}
