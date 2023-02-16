package ellemes.expandedstorage.common.misc;

import ellemes.expandedstorage.api.inventory.AbstractHandler;
import ellemes.expandedstorage.common.inventory.ServerScreenHandlerFactory;
import ellemes.expandedstorage.common.recipe.block.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.EntityConversionRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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

    void sendConversionRecipesToClient(@Nullable ServerPlayer target, List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes);
}
