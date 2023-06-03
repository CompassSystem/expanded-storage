package ellemes.expandedstorage.api.v4;

import compasses.expandedstorage.common.CommonMain;
import ellemes.expandedstorage.api.inventory.AbstractHandler;
import ellemes.expandedstorage.api.v3.OpenableInventory;
import ellemes.expandedstorage.api.v3.OpenableInventoryProvider;
import ellemes.expandedstorage.api.v3.context.BaseContext;
import ellemes.expandedstorage.api.v3.context.BlockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.function.Consumer;

public class InventoryOpeningApi {
    private InventoryOpeningApi() {
        throw new IllegalStateException("InventoryOpeningApi should not be instantiated.");
    }

    public static void openBlockInventory(ServerPlayer player, BlockPos pos, OpenableInventoryProvider<BlockContext> inventory) {
        InventoryOpeningApi.s_openInventory(player, inventory.getOpenableInventory(new BlockContext(player.getLevel(), player, pos)), inventory::onInitialOpen, inventory.getForcedScreenType());
    }

    public static void openEntityInventory(ServerPlayer player, OpenableInventoryProvider<BaseContext> inventory) {
        InventoryOpeningApi.s_openInventory(player, inventory.getOpenableInventory(new BaseContext(player.getLevel(), player)), inventory::onInitialOpen, inventory.getForcedScreenType());
    }

    private static void s_openInventory(ServerPlayer player, OpenableInventory inventory, Consumer<ServerPlayer> onInitialOpen, ResourceLocation forcedScreenType) {
        Component title = inventory.getInventoryTitle();
        if (!inventory.canBeUsedBy(player)) {
            player.displayClientMessage(Component.translatable("container.isLocked", title), true);
            player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
        if (!player.isSpectator()) {
            onInitialOpen.accept(player);
        }
        CommonMain.platformHelper().openScreenHandler(player, inventory.getInventory(), (syncId, inv, playerInv) -> new AbstractHandler(syncId, inv, playerInv, null), title, forcedScreenType);
    }
}