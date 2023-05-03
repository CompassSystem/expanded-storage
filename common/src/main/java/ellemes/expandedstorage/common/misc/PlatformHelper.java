package ellemes.expandedstorage.common.misc;

import ellemes.expandedstorage.common.inventory.AbstractHandler;
import ellemes.expandedstorage.common.inventory.ServerScreenHandlerFactory;
import ellemes.expandedstorage.common.recipe.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.EntityConversionRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PlatformHelper {
    MenuType<AbstractHandler> getScreenHandlerType();

    void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType);

    void sendConversionRecipesToClient(@Nullable ServerPlayer target, List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes);

    boolean canDestroyBamboo(ItemStack stack);
}
