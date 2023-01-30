package ellemes.container_library.wrappers;

import ellemes.container_library.api.inventory.AbstractHandler;
import ellemes.container_library.api.v3.OpenableInventory;
import ellemes.container_library.inventory.ServerScreenHandlerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;

import java.util.function.Consumer;

public abstract class NetworkWrapper {
    protected NetworkWrapper() {
    }

    protected abstract void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType);

    public void s_openInventory(ServerPlayer player, OpenableInventory inventory, Consumer<ServerPlayer> onInitialOpen, ResourceLocation forcedScreenType) {
        Component title = inventory.getInventoryTitle();
        if (!inventory.canBeUsedBy(player)) {
            player.displayClientMessage(Component.translatable("container.isLocked", title), true);
            player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
        if (!player.isSpectator()) {
            onInitialOpen.accept(player);
        }
        this.openScreenHandler(player, inventory.getInventory(), (syncId, inv, playerInv) -> new AbstractHandler(syncId, inv, playerInv, null), title, forcedScreenType);
    }
}
