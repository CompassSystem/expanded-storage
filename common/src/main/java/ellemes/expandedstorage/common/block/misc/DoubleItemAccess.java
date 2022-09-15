package ellemes.expandedstorage.common.block.misc;

import ellemes.expandedstorage.common.block.strategies.ItemAccess;

public interface DoubleItemAccess extends ItemAccess {
    Object getSingle();

    void setOther(DoubleItemAccess other);

    boolean hasCachedAccess();
}
