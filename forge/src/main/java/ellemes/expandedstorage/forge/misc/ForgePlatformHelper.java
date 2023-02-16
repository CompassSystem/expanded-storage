package ellemes.expandedstorage.forge.misc;

import ellemes.expandedstorage.api.inventory.AbstractHandler;
import ellemes.expandedstorage.common.inventory.ServerScreenHandlerFactory;
import ellemes.expandedstorage.common.misc.PlatformHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ForgePlatformHelper implements PlatformHelper {
    private MenuType<AbstractHandler> menuType;
    private ForgeClientHelper clientPlatformHelper;

    public static ForgePlatformHelper instance() {
        return (ForgePlatformHelper) PlatformHelper.instance();
    }

    @Override
    public ForgeClientHelper clientHelper() {
        if (clientPlatformHelper == null) {
            clientPlatformHelper = new ForgeClientHelper();
        }
        return clientPlatformHelper;
    }

    @Override
    public MenuType<AbstractHandler> getScreenHandlerType() {
        if (menuType == null) {
            menuType = new MenuType<>((IContainerFactory<AbstractHandler>) AbstractHandler::createClientMenu);
        }
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
}
