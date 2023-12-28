package compasses.expandedstorage.impl.block.misc;

import compasses.expandedstorage.impl.block.strategies.ItemAccess;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public interface DoubleItemAccess extends ItemAccess {
    Storage<ItemVariant> getSingle();

    void setOther(DoubleItemAccess other);

    boolean hasCachedAccess();
}
