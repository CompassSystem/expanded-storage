package ninjaphenix.expandedstorage.base.internal_api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.inventory.ContainerMenuFactory;
import ninjaphenix.expandedstorage.base.platform.NetworkWrapper;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Experimental
public abstract class AbstractOpenableStorageBlock extends AbstractStorageBlock implements EntityBlock, WorldlyContainerHolder {
    private final ResourceLocation openStat;
    private final int slots;

    public AbstractOpenableStorageBlock(Properties properties, ResourceLocation blockId, ResourceLocation blockTier,
                                        ResourceLocation openStat, int slots) {
        super(properties, blockId, blockTier);
        this.openStat = openStat;
        this.slots = slots;
    }

    public final int getSlotCount() {
        return slots;
    }

    public final Component getContainerName() {
        return new TranslatableComponent(this.getDescriptionId());
    }

    @Override
    @SuppressWarnings("deprecation")
    public final InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            ContainerMenuFactory menuFactory = this.createContainerFactory(state, level, pos);
            if (menuFactory != null) {
                if (menuFactory.canPlayerOpen(serverPlayer)) {
                    NetworkWrapper.getInstance().s2c_openMenu(serverPlayer, menuFactory);
                    serverPlayer.awardStat(openStat);
                    PiglinAi.angerNearbyPiglins(serverPlayer, true);
                }
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean bl) {
        if (!state.is(newState.getBlock())) {
            BlockEntity temp = level.getBlockEntity(pos);
            if (temp instanceof AbstractOpenableStorageBlockEntity) {
                Containers.dropContents(level, pos, ((AbstractOpenableStorageBlockEntity) temp));
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, bl);
        }
    }

    protected ContainerMenuFactory createContainerFactory(BlockState state, LevelAccessor level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof AbstractOpenableStorageBlockEntity)) {
            return null;
        }
        AbstractOpenableStorageBlockEntity container = (AbstractOpenableStorageBlockEntity) entity;
        return new ContainerMenuFactory() {
            @Override
            public void writeClientData(ServerPlayer player, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(pos).writeInt(container.getContainerSize());
            }

            @Override
            public Component displayName() {
                return container.getDisplayName();
            }

            @Override
            public boolean canPlayerOpen(ServerPlayer player) {
                if (container.canPlayerInteractWith(player)) {
                    return true;
                }
                AbstractStorageBlockEntity.alertBlockLocked(player, this.displayName());
                return false;
            }

            @Override
            public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                if (container.canPlayerInteractWith(player) && container.stillValid(player)) {
                    return NetworkWrapper.getInstance().createMenu(windowId, container.getBlockPos(), container, playerInventory, this.displayName());
                }
                return null;
            }
        };
    }

    @Override // Keep for hoppers.
    public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        BlockEntity temp = level.getBlockEntity(pos);
        if (temp instanceof AbstractOpenableStorageBlockEntity) {
            return (AbstractOpenableStorageBlockEntity) temp;
        }
        return null;
    }
}
