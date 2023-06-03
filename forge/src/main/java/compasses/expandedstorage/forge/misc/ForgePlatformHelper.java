package compasses.expandedstorage.forge.misc;

import ellemes.expandedstorage.api.inventory.AbstractHandler;
import compasses.expandedstorage.common.inventory.ServerScreenHandlerFactory;
import compasses.expandedstorage.common.misc.PlatformHelper;
import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.common.recipe.BlockConversionRecipe;
import compasses.expandedstorage.common.recipe.EntityConversionRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForgePlatformHelper implements PlatformHelper {
    private final SimpleChannel channel;
    private final MenuType<AbstractHandler> menuType;

    {
        channel = NetworkRegistry.newSimpleChannel(Utils.id("channel"), () -> "1.0", "1.0"::equals, "1.0"::equals);
        channel.registerMessage(0, ClientboundUpdateRecipesMessage.class, ClientboundUpdateRecipesMessage::encode, ClientboundUpdateRecipesMessage::decode, ClientboundUpdateRecipesMessage::handle);
        menuType = new MenuType<>((IContainerFactory<AbstractHandler>) AbstractHandler::createClientMenu, FeatureFlags.VANILLA_SET);
    }

    @Override
    public MenuType<AbstractHandler> getScreenHandlerType() {
        return menuType;
    }

    @Override
    public void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType) {
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return title;
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                return factory.create(syncId, inventory, playerInventory);
            }
        }, buffer -> {
            buffer.writeInt(inventory.getContainerSize());
            if (forcedScreenType != null) {
                buffer.writeResourceLocation(forcedScreenType);
            }
        });
    }

    @Override
    public void sendConversionRecipesToClient(@Nullable ServerPlayer target, List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes) {
        if (target == null) {
            // Should be valid to send updates here as remote present check has been done on join.
            channel.send(PacketDistributor.ALL.noArg(), new ClientboundUpdateRecipesMessage(blockRecipes, entityRecipes));
        } else {
            if (!channel.isRemotePresent(target.connection.connection)) {
                target.connection.disconnect(Component.translatable("text.expandedstorage.disconnect.old_version"));
            } else {
                channel.send(PacketDistributor.PLAYER.with(() -> target), new ClientboundUpdateRecipesMessage(blockRecipes, entityRecipes));
            }
        }
    }

    @Override
    public boolean canDestroyBamboo(ItemStack stack) {
        return stack.canPerformAction(ToolActions.SWORD_DIG);
    }
}
