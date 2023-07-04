package compasses.expandedstorage.common.block.misc;

import compasses.expandedstorage.common.block.strategies.ItemAccess;

public interface DoubleItemAccess extends ItemAccess {
    Object getSingle();

    void setOther(DoubleItemAccess other);

    boolean hasCachedAccess();
}
