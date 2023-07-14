package compasses.expandedstorage.impl.block.entity;

import compasses.expandedstorage.impl.block.OpenableBlock;
import compasses.expandedstorage.impl.block.strategies.Lockable;
import compasses.expandedstorage.impl.inventory.VariableSidedInventory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

public class OldChestBlockEntity extends OpenableBlockEntity {
    private WorldlyContainer cachedDoubleInventory = null;
    private Storage<ItemVariant> cachedTransferStorage = null;

    public OldChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ResourceLocation blockId, Supplier<Lockable> lockable) {
        super(type, pos, state, blockId, ((OpenableBlock) state.getBlock()).getInventoryTitle(), ((OpenableBlock) state.getBlock()).getSlotCount());
        this.setLockable(lockable.get());
    }

    public void invalidateDoubleBlockCache() {
        cachedDoubleInventory = null;
        cachedTransferStorage = null;
    }

    public WorldlyContainer getCachedDoubleInventory() {
        return cachedDoubleInventory;
    }

    public void setCachedDoubleInventory(OldChestBlockEntity other) {
        this.cachedDoubleInventory = VariableSidedInventory.of(this.getInventory(), other.getInventory());
    }

    @Override
    public Storage<ItemVariant> getTransferStorage() {
        return hasCachedTransferStorage() ? cachedTransferStorage : this.getOwnTransferStorage();
    }

    public Storage<ItemVariant> getOwnTransferStorage() {
        return super.getTransferStorage();
    }

    public boolean hasCachedTransferStorage() {
        return cachedTransferStorage != null;
    }

    public void setCachedTransferStorage(OldChestBlockEntity other) {
        cachedTransferStorage = other == null ? null : new CombinedStorage<>(List.of(this.getOwnTransferStorage(), other.getOwnTransferStorage()));
    }
}
