package compasses.expandedstorage.common.block.strategies;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface Lockable {
    void writeLock(CompoundTag tag);

    boolean readLock(CompoundTag tag);

    boolean canPlayerOpenLock(ServerPlayer player);

    boolean isLockPresent();
}
