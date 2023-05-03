package ellemes.expandedstorage.thread;

import ellemes.expandedstorage.common.client.gui.AbstractScreen;
import ellemes.expandedstorage.common.CommonClient;
import ellemes.expandedstorage.common.CommonMain;
import ellemes.expandedstorage.common.misc.ClientPlatformHelper;
import ellemes.expandedstorage.common.recipe.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.ConversionRecipeManager;
import ellemes.expandedstorage.common.recipe.EntityConversionRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ThreadClient {
    public static void initialize(ClientPlatformHelper helper) {
        CommonClient.initialize(helper);
        MenuScreens.register(CommonMain.platformHelper().getScreenHandlerType(), AbstractScreen::createScreen);
    }

    public static void handleUpdateRecipesPacket(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer) {
        List<BlockConversionRecipe<?>> blockRecipes = new ArrayList<>(buffer.readCollection(ArrayList::new, BlockConversionRecipe::readFromBuffer));
        List<EntityConversionRecipe<?>> entityRecipes = new ArrayList<>(buffer.readCollection(ArrayList::new, EntityConversionRecipe::readFromBuffer));
        client.execute(() -> ConversionRecipeManager.INSTANCE.replaceAllRecipes(blockRecipes, entityRecipes));
    }
}
