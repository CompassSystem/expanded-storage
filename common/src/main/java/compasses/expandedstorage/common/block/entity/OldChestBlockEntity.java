package compasses.expandedstorage.common.block.entity;

import compasses.expandedstorage.common.block.OpenableBlock;
import compasses.expandedstorage.common.block.entity.extendable.InventoryBlockEntity;
import compasses.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import compasses.expandedstorage.common.block.misc.DoubleItemAccess;
import compasses.expandedstorage.common.block.strategies.ItemAccess;
import compasses.expandedstorage.common.block.strategies.Lockable;
import ellemes.expandedstorage.api.helpers.VariableSidedInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class OldChestBlockEntity extends InventoryBlockEntity {
    WorldlyContainer cachedDoubleInventory = null;

    public OldChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                               Function<OpenableBlockEntity, ItemAccess> access, @Nullable Supplier<Lockable> lockable) {
        super(type, pos, state, ((OpenableBlock) state.getBlock()).getInventoryTitle(), lockable, ((OpenableBlock) state.getBlock()).getSlotCount());
        this.setItemAccess(access.apply(this));
    }

    public void invalidateDoubleBlockCache() {
        cachedDoubleInventory = null;
        this.getItemAccess().setOther(null);
    }

    public WorldlyContainer getCachedDoubleInventory() {
        return cachedDoubleInventory;
    }

    public void setCachedDoubleInventory(OldChestBlockEntity other) {
        this.cachedDoubleInventory = VariableSidedInventory.of(this.getInventory(), other.getInventory());
    }

    @Override
    public DoubleItemAccess getItemAccess() {
        return (DoubleItemAccess) super.getItemAccess();
    }

}
