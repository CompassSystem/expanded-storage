package compasses.expandedstorage.common.misc;

import compasses.expandedstorage.common.block.strategies.Lockable;
import compasses.expandedstorage.common.item.GoldKeyItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class LockHolder {
    private final Consumer<VisualLockType> onLockChanged;
    private @Nullable Lockable lockable;
    private @Nullable UUID diamondLock;
    private @Nullable Component goldLock;
    private @Nullable LockCode vanillaLock;

    public LockHolder(Consumer<VisualLockType> onLockChanged) {
        this.onLockChanged = onLockChanged;
    }

    public boolean hasLock() {
        return diamondLock != null || goldLock != null || vanillaLock != null || (lockable != null && lockable.isLockPresent());
    }

    public void setDiamondLock(UUID playerUUID) {
        diamondLock = playerUUID;
        onLockChanged.accept(VisualLockType.DIAMOND);
    }

    public void setGoldLock(Component lockDisplayName) {
        goldLock = lockDisplayName;
        onLockChanged.accept(VisualLockType.GOLD);
    }

    public void setVanillaLock(LockCode lockKey) {
        if (lockKey != LockCode.NO_LOCK) {
            vanillaLock = lockKey;
            onLockChanged.accept(VisualLockType.NONE);
        }
    }

    public void setLockable(Lockable lockable) {
        if (this.lockable == null) {
            this.lockable = lockable;
        }
    }

    public Lockable getLockable() {
        return lockable;
    }

    public void copyFrom(LockHolder other) {
        lockable = other.lockable;
        diamondLock = other.diamondLock;
        goldLock = other.goldLock;
        vanillaLock = other.vanillaLock;
    }

    public boolean canOpen(ServerPlayer player) {
        if (this.getLockable() != null && this.getLockable().isLockPresent()) {
            return this.getLockable().canPlayerOpenLock(player);
        } else if (diamondLock != null) {
            return diamondLock.equals(player.getUUID());
        } else {
            ItemStack stackInHand = player.getItemInHand(player.getUsedItemHand());
            if (goldLock != null) {
                boolean isGoldKey = !stackInHand.isEmpty() && stackInHand.getItem() instanceof GoldKeyItem;
                return isGoldKey && stackInHand.getHoverName().equals(goldLock);
            } else if (vanillaLock != null) {
                return vanillaLock.unlocksWith(stackInHand);
            }
        }
        return true;
    }

    public void readLockFromTag(CompoundTag tag) {
        boolean readValidLock = false;
        if (lockable != null) {
            readValidLock = lockable.readLock(tag);
        }
        if (!readValidLock) {
            // Read base lock
            Optional<UUID> diamondLock = LockHolder.diamondLockFromTag(tag);
            if (diamondLock.isPresent()) {
                this.diamondLock = diamondLock.get();
            } else {
                Optional<Component> goldLock = LockHolder.goldLockFromTag(tag);
                if (goldLock.isPresent()) {
                    this.goldLock = goldLock.get();
                } else {
                    LockCode vanillaLock = LockCode.fromTag(tag);
                    if (vanillaLock != LockCode.NO_LOCK) {
                        this.vanillaLock = vanillaLock;
                    }
                }
            }
        }
    }

    private static Optional<UUID> diamondLockFromTag(CompoundTag tag) {
        if (tag.hasUUID("Diamond_Lock")) {
            return Optional.of(tag.getUUID("Diamond_Lock"));
        }
        return Optional.empty();
    }

    private static Optional<Component> goldLockFromTag(CompoundTag tag) {
        if (tag.contains("Gold_Lock", Tag.TAG_STRING)) {
            return Optional.ofNullable(Component.Serializer.fromJson(tag.getString("Gold_Lock")));
        }
        return Optional.empty();
    }

    public void saveLockToTag(CompoundTag tag) {
        if (lockable != null && lockable.isLockPresent()) {
            lockable.writeLock(tag);
        } else {
            // Write base lock
            if (diamondLock != null) {
                tag.putUUID("Diamond_Lock", diamondLock);
            }
            else if (goldLock != null) {
                tag.putString("Gold_Lock", Component.Serializer.toJson(goldLock));
            }
            else if (vanillaLock != null) {
                vanillaLock.addToTag(tag);
            }
        }
    }

    public VisualLockType getVisualStyle() {
        return diamondLock != null ? VisualLockType.DIAMOND : goldLock != null ? VisualLockType.GOLD : VisualLockType.NONE;
    }
}
