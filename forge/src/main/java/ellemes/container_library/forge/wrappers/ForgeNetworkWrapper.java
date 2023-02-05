package ellemes.container_library.forge.wrappers;

import ellemes.container_library.Utils;
import ellemes.container_library.inventory.ServerScreenHandlerFactory;
import ellemes.container_library.wrappers.NetworkWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import org.jetbrains.annotations.Nullable;

public final class ForgeNetworkWrapper extends NetworkWrapper {
    public ForgeNetworkWrapper() {
        super();
        var channel = NetworkRegistry.newSimpleChannel(Utils.id("channel"), () -> "1.0", "1.0"::equals, "1.0"::equals);
        channel.registerMessage(0, ClientboundUpdateRecipesMessage.class, ClientboundUpdateRecipesMessage::encode, ClientboundUpdateRecipesMessage::decode, ClientboundUpdateRecipesMessage::handle);
    }

    @Override
    protected void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType) {
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
