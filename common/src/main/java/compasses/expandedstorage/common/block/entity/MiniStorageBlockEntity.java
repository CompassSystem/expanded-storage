package compasses.expandedstorage.common.block.entity;

import compasses.expandedstorage.common.block.OpenableBlock;
import compasses.expandedstorage.common.block.entity.extendable.ExposedInventoryBlockEntity;
import compasses.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import compasses.expandedstorage.common.block.strategies.ItemAccess;
import compasses.expandedstorage.common.block.strategies.Lockable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;
import java.util.function.Supplier;

public final class MiniStorageBlockEntity extends ExposedInventoryBlockEntity {
    public MiniStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ResourceLocation blockId,
                                  Function<OpenableBlockEntity, ItemAccess> access, Supplier<Lockable> lockable) {
        super(type, pos, state, blockId, ((OpenableBlock) state.getBlock()).getInventoryTitle(), 1);
        this.setItemAccess(access.apply(this));
        this.setLockable(lockable.get());
    }
}
