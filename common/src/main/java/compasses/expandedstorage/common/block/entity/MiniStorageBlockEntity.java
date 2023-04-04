package compasses.expandedstorage.common.block.entity;

import compasses.expandedstorage.common.block.OpenableBlock;
import compasses.expandedstorage.common.block.entity.extendable.ExposedInventoryBlockEntity;
import compasses.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import compasses.expandedstorage.common.block.strategies.ItemAccess;
import compasses.expandedstorage.common.block.strategies.Lockable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public final class MiniStorageBlockEntity extends ExposedInventoryBlockEntity {
    public MiniStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                  Function<OpenableBlockEntity, ItemAccess> access, @Nullable Supplier<Lockable> lockable) {
        super(type, pos, state, ((OpenableBlock) state.getBlock()).getInventoryTitle(), lockable, 1);
        this.setItemAccess(access.apply(this));
    }
}
