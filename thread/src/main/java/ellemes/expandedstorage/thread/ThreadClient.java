package ellemes.expandedstorage.thread;

import ellemes.expandedstorage.api.client.gui.AbstractScreen;
import ellemes.expandedstorage.common.CommonClient;
import ellemes.expandedstorage.common.misc.PlatformHelper;
import ellemes.expandedstorage.common.recipe.ConversionRecipeManager;
import ellemes.expandedstorage.common.recipe.block.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.EntityConversionRecipe;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

import java.util.ArrayList;
import java.util.List;

public class ThreadClient {
    public static void initialize() {
        CommonClient.initialize();
        MenuScreens.register(PlatformHelper.instance().getScreenHandlerType(), AbstractScreen::createScreen);

        ClientPlayConnectionEvents.INIT.register((_unused_1, _unused_2) -> {
            ClientPlayNetworking.registerReceiver(ThreadMain.UPDATE_RECIPES_ID, (client, handler, buffer, responseSender) -> {
                List<BlockConversionRecipe<?>> blockRecipes = new ArrayList<>(buffer.readCollection(ArrayList::new, BlockConversionRecipe::readFromBuffer));
                List<EntityConversionRecipe<?>> entityRecipes = new ArrayList<>(buffer.readCollection(ArrayList::new, EntityConversionRecipe::readFromBuffer));
                client.execute(() -> ConversionRecipeManager.INSTANCE.replaceAllRecipes(blockRecipes, entityRecipes));
            });
        });
    }
}
