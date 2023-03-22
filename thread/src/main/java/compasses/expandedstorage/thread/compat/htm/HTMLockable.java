package compasses.expandedstorage.thread.compat.htm;

import com.github.fabricservertools.htm.HTMContainerLock;
import compasses.expandedstorage.common.block.strategies.Lockable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

public final class HTMLockable implements Lockable {
    public static final String LOCK_TAG_KEY = "HTM_Lock";
    private HTMContainerLock lock = new HTMContainerLock();

    @Override
    public void writeLock(CompoundTag tag) {
        CompoundTag subTag = new CompoundTag();
        lock.toTag(subTag);
        tag.put(HTMLockable.LOCK_TAG_KEY, subTag);
    }

    @Override
    public boolean readLock(CompoundTag tag) {
        if (tag.contains(HTMLockable.LOCK_TAG_KEY, Tag.TAG_COMPOUND)) {
            lock.fromTag(tag.getCompound(HTMLockable.LOCK_TAG_KEY));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canPlayerOpenLock(ServerPlayer player) {
        return !lock.isLocked() || lock.isLocked() && lock.canOpen(player);
    }

    @Override
    public boolean isLockPresent() {
        return lock.isLocked();
    }

    public HTMContainerLock getLock() {
        return lock;
    }

    public void setLock(HTMContainerLock lock) {
        this.lock = lock;
    }
}
