package compasses.expandedstorage.common.misc;

import compasses.expandedstorage.common.inventory.handler.AbstractHandler;
import compasses.expandedstorage.common.inventory.handler.ServerScreenHandlerFactory;
import compasses.expandedstorage.common.recipe.BlockConversionRecipe;
import compasses.expandedstorage.common.recipe.EntityConversionRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommonPlatformHelper {
    MenuType<AbstractHandler> getScreenHandlerType();

    void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType);

    void sendConversionRecipesToClient(@Nullable ServerPlayer target, List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes);

    boolean canDestroyBamboo(ItemStack stack);

    boolean platformMarkerExists();

    void createInvalidPlatformMarker();
}
