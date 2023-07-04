package compasses.expandedstorage.common.block.misc;

import compasses.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

import java.util.List;

public final class ChestItemAccess extends GenericItemAccess implements DoubleItemAccess {
    @SuppressWarnings("UnstableApiUsage")
    private Storage<ItemVariant> cache;

    public ChestItemAccess(OpenableBlockEntity entity) {
        super(entity);
    }

    @Override
    public Storage<ItemVariant> get() {
        return this.hasCachedAccess() ? cache : this.getSingle();
    }

    @Override
    public Storage<ItemVariant> getSingle() {
        return super.get();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void setOther(DoubleItemAccess other) {
        //noinspection unchecked
        cache = other == null ? null : new CombinedStorage<>(List.of((Storage<ItemVariant>) this.getSingle(), (Storage<ItemVariant>) other.getSingle()));
    }

    @Override
    public boolean hasCachedAccess() {
        return cache != null;
    }
}
