package ninjaphenix.expandedstorage;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableObject;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.block.misc.Property;
import ninjaphenix.expandedstorage.block.misc.PropertyRetriever;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

import java.util.List;
import java.util.Optional;

public final class FabricChestProperties {
    public static final String LOCK_TAG_KEY = "HTM_Lock";

    public static final Property<AbstractOpenableStorageBlockEntity, HTMContainerLock> LOCK_PROPERTY = new Property<>() {
        @Override
        public HTMContainerLock get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
            LockableObject firstLockable = (LockableObject) first;
            LockableObject secondLockable = (LockableObject) second;
            if (firstLockable.getLock().isLocked() || !secondLockable.getLock().isLocked()) {
                return firstLockable.getLock();
            }
            return secondLockable.getLock();
        }

        @Override
        public HTMContainerLock get(AbstractOpenableStorageBlockEntity single) {
            return ((LockableObject) single).getLock();
        }
    };

    public static final Property<AbstractOpenableStorageBlockEntity, BlockEntity> UNLOCKED_BE_PROPERTY = new Property<>() {
        @Override
        public BlockEntity get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
            LockableObject firstLockable = (LockableObject) first;
            if (!firstLockable.getLock().isLocked()) {
                return first;
            }
            LockableObject secondLockable = (LockableObject) second;
            if (!secondLockable.getLock().isLocked()) {
                return second;
            }
            return null;
        }

        @Override
        public BlockEntity get(AbstractOpenableStorageBlockEntity single) {
            return null;
        }
    };

    public static final Property<AbstractOpenableStorageBlockEntity, Object> INVENTORY_GETTER = new Property<>() {
        @Override
        public Object get(AbstractOpenableStorageBlockEntity first, AbstractOpenableStorageBlockEntity second) {
            //noinspection unchecked,deprecation,UnstableApiUsage
            return new CombinedStorage<>(List.of((Storage<ItemVariant>) PlatformUtils.getInstance().createGenericItemAccess(first),
                    (Storage<ItemVariant>) PlatformUtils.getInstance().createGenericItemAccess(second)));
        }

        @Override
        public Object get(AbstractOpenableStorageBlockEntity single) {
            return PlatformUtils.getInstance().createGenericItemAccess(single);
        }
    };


    public static Object createItemStorage(World world, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AbstractChestBlock<?> block) {
            //noinspection unchecked
            return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock<AbstractOpenableStorageBlockEntity>) block, state, world, pos, true).get(FabricChestProperties.INVENTORY_GETTER).orElse(null);
        }
        return null;
    }
}
