package compasses.expandedstorage.common.block.entity;

import compasses.expandedstorage.common.block.AbstractChestBlock;
import compasses.expandedstorage.common.block.ChestBlock;
import compasses.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import compasses.expandedstorage.common.block.strategies.ItemAccess;
import compasses.expandedstorage.common.block.strategies.Lockable;
import compasses.expandedstorage.common.block.strategies.Observable;
import ellemes.expandedstorage.api.helpers.VariableInventory;
import ellemes.expandedstorage.api.inventory.AbstractHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ChestBlockEntity extends OldChestBlockEntity {
    private final ContainerOpenersCounter manager = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            ChestBlockEntity.playSound(level, pos, state, SoundEvents.CHEST_OPEN);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            ChestBlockEntity.playSound(level, pos, state, SoundEvents.CHEST_CLOSE);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            level.blockEvent(pos, state.getBlock(), ChestBlock.SET_OBSERVER_COUNT_EVENT, newCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            Container inventory = ChestBlockEntity.this.getInventory();
            return player.containerMenu instanceof AbstractHandler handler &&
                    (handler.getInventory() == inventory ||
                            handler.getInventory() instanceof VariableInventory variableInventory && variableInventory.containsPart(inventory));
        }
    };
    private final ChestLidController lidController;

    public ChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ResourceLocation blockId,
                            Function<OpenableBlockEntity, ItemAccess> access, Supplier<Lockable> lockable) {
        super(type, pos, state, blockId, access, lockable);
        this.setObservable(new Observable() {
            @Override
            public void playerStartViewing(Player player) {
                BlockEntity self = ChestBlockEntity.this;
                manager.incrementOpeners(player, self.getLevel(), self.getBlockPos(), self.getBlockState());
            }

            @Override
            public void playerStopViewing(Player player) {
                BlockEntity self = ChestBlockEntity.this;
                manager.decrementOpeners(player, self.getLevel(), self.getBlockPos(), self.getBlockState());
            }
        });
        lidController = new ChestLidController();
    }

    @SuppressWarnings("unused")
    public static void progressLidAnimation(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        ((ChestBlockEntity) blockEntity).lidController.tickLid();
    }

    private static void playSound(Level level, BlockPos pos, BlockState state, SoundEvent sound) {
        DoubleBlockCombiner.BlockType mergeType = AbstractChestBlock.getBlockType(state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE));
        Vec3 soundPos;
        if (mergeType == DoubleBlockCombiner.BlockType.SINGLE) {
            soundPos = Vec3.atCenterOf(pos);
        } else if (mergeType == DoubleBlockCombiner.BlockType.FIRST) {
            soundPos = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(AbstractChestBlock.getDirectionToAttached(state).getNormal()).scale(0.5D));
        } else {
            return;
        }
        level.playSound(null, soundPos.x(), soundPos.y(), soundPos.z(), sound, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public boolean triggerEvent(int event, int value) {
        if (event == ChestBlock.SET_OBSERVER_COUNT_EVENT) {
            lidController.shouldBeOpen(value > 0);
            return true;
        }
        return super.triggerEvent(event, value);
    }

    public float getLidOpenness(float delta) {
        return lidController.getOpenness(delta);
    }

    public void updateViewerCount(ServerLevel level, BlockPos pos, BlockState state) {
        manager.recheckOpeners(level, pos, state);
    }
}
