package compasses.expandedstorage.impl.block.strategies;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public interface ItemAccess {
    Storage<ItemVariant> get();
}
