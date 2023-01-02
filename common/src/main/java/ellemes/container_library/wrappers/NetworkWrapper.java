package ellemes.container_library.wrappers;

import dev.ftb.mods.ftbchunks.data.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.data.Protection;
import ellemes.container_library.Utils;
import ellemes.container_library.api.inventory.AbstractHandler;
import ellemes.container_library.api.v3.OpenableInventory;
import ellemes.container_library.api.v3.OpenableInventoryProvider;
import ellemes.container_library.api.v3.context.BlockContext;
import ellemes.container_library.api.v3.context.BaseContext;
import ellemes.container_library.inventory.ServerScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public abstract class NetworkWrapper {
    private final boolean ftbChunksLoaded;

    protected NetworkWrapper(boolean ftbChunksLoaded) {
        this.ftbChunksLoaded = ftbChunksLoaded;
    }

    protected abstract void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType);

    public abstract void c_openBlockInventory(BlockPos pos);

    public final void writeBlockData(FriendlyByteBuf buffer, BlockPos pos) {
        buffer.writeResourceLocation(Utils.id("block"));
        buffer.writeBlockPos(pos);
    }

    public abstract void c_openEntityInventory(Entity entity);

    public final void writeEntityData(FriendlyByteBuf buffer, Entity entity) {
        buffer.writeResourceLocation(Utils.id("entity"));
        buffer.writeUUID(entity.getUUID());
    }
//    public abstract void c_openItemInventory(int slotId);

//    public final void writeItemData(FriendlyByteBuf buffer, int slotId) {
//        buffer.writeResourceLocation(Utils.id("item"));
//        buffer.writeInt(slotId);
//    }

    public void s_handleOpenInventory(ServerPlayer sender, FriendlyByteBuf buffer) {
        ResourceLocation context = buffer.readResourceLocation();
        ServerLevel level = sender.getLevel();
        BaseContext inventoryContext = null;
        OpenableInventoryProvider<?> inventoryProvider = null;
        switch (context.getPath()) {
            case "block" -> {
                BlockPos pos = buffer.readBlockPos();
                if (this.canOpenInventory(sender, pos) && level.getBlockState(pos).getBlock() instanceof OpenableInventoryProvider<?> provider) {
                    inventoryContext = new BlockContext(level, sender, pos);
                    inventoryProvider = provider;
                }
            }

            case "entity" -> {
                Entity entity = level.getEntity(buffer.readUUID());
                if (this.canOpenInventory(sender, entity) && entity instanceof OpenableInventoryProvider<?> provider) {
                    inventoryContext = new BaseContext(level, sender);
                    inventoryProvider = provider;
                }
            }

//            case "item" -> {
//                int slotId = buffer.readInt();
//                ItemStack stack = sender.getInventory().getItem(slotId);
//                if (stack.getItem() instanceof OpenableInventoryProvider<?> provider) {
//                    inventoryContext = new ItemContext(level, sender, stack);
//                    inventoryProvider = provider;
//                }
//            }
        }
        if (inventoryProvider != null) {
            //noinspection unchecked
            OpenableInventory inventory = ((OpenableInventoryProvider<BaseContext>) inventoryProvider).getOpenableInventory(inventoryContext);
            // inventory can be null in some rare cases.
            if (inventory != null) {
                this.s_openInventory(sender, inventory, inventoryProvider::onInitialOpen, inventoryProvider.getForcedScreenType());
            }
        }

        buffer.release();
    }

    private void s_openInventory(ServerPlayer player, OpenableInventory inventory, Consumer<ServerPlayer> onInitialOpen, ResourceLocation forcedScreenType) {
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

    protected boolean canOpenInventory(ServerPlayer player, BlockPos pos) {
        if (ftbChunksLoaded) {
            try {
                if (FTBChunksAPI.isManagerLoaded()) {
                    return !FTBChunksAPI.getManager().protect(player, InteractionHand.MAIN_HAND, pos, Protection.INTERACT_BLOCK, null);
                }
            } catch (NoClassDefFoundError | NoSuchMethodError ignored) {
                // We should check if the mods loaded.
                System.err.println("FTB Chunks compat broke, please report to BucketOfCompasses (Expanded Storage Issues)");
            }
        }
        return true;
    }

    protected boolean canOpenInventory(ServerPlayer player, Entity entity) {
        if (ftbChunksLoaded) {
            try {
                if (FTBChunksAPI.isManagerLoaded()) {
                    return !FTBChunksAPI.getManager().protect(player, InteractionHand.MAIN_HAND, entity.getOnPos(), Protection.INTERACT_ENTITY, entity);
                }
            } catch (NoClassDefFoundError | NoSuchMethodError ignored) {
                // We should check if the mods loaded.
                System.err.println("FTB Chunks compat broke, please report to BucketOfCompasses (Expanded Storage Issues)");
            }
        }
        return true;
    }
}
