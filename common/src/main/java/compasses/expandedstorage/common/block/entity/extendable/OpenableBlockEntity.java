package compasses.expandedstorage.common.block.entity.extendable;

import compasses.expandedstorage.common.block.strategies.ItemAccess;
import compasses.expandedstorage.common.block.strategies.Lockable;
import compasses.expandedstorage.common.misc.VisualLockType;
import ellemes.expandedstorage.api.v3.OpenableInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class OpenableBlockEntity extends BlockEntity implements OpenableInventory, Nameable {
    private final ResourceLocation blockId;
    private final Component defaultName;
    private ItemAccess itemAccess;
    private Lockable lockable;
    private Component customName;
    private @Nullable UUID diamondLock;
    private @Nullable Component goldLock;
    private @Nullable LockCode vanillaLock;
    // Client only
    private VisualLockType visualLockType = VisualLockType.DEFAULT;

    public OpenableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ResourceLocation blockId, Component defaultName, @Nullable Supplier<Lockable> lockable) {
        super(type, pos, state);
        this.blockId = blockId;
        this.defaultName = defaultName;
        if (lockable != null) {
            this.setLockable(lockable.get());
        }
    }

    @Override
    public boolean canBeUsedBy(ServerPlayer player) {
        boolean lockOpenable = true;
        if (this.getLockable() != null && this.getLockable().isLockPresent()) {
            lockOpenable = this.getLockable().canPlayerOpenLock(player);
        } else if (diamondLock != null) {
            lockOpenable = diamondLock.equals(player.getUUID());
        } else {
            ItemStack stackInHand = player.getItemInHand(player.getUsedItemHand());
            if (goldLock != null) {
                boolean isGoldKey = !stackInHand.isEmpty() && BuiltInRegistries.ITEM.getKey(stackInHand.getItem()).toString().equals("expandedstorage:gold_key");
                lockOpenable = isGoldKey && stackInHand.getHoverName().equals(goldLock);
            } else if (vanillaLock != null) {
                lockOpenable = vanillaLock.unlocksWith(stackInHand);
            }
        }
        return this.isValidAndPlayerInRange(player) && lockOpenable;
    }

    protected final boolean isValidAndPlayerInRange(Player player) {
        //noinspection DataFlowIssue
        return this.getLevel().getBlockEntity(this.getBlockPos()) == this && player.distanceToSqr(Vec3.atCenterOf(this.getBlockPos())) <= 36.0D;
    }

    @Override
    public Component getInventoryTitle() {
        return this.getName();
    }

    public abstract NonNullList<ItemStack> getItems();

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CustomName", Tag.TAG_STRING)) {
            customName = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
        if (tag.contains("VisualLock", Tag.TAG_INT)) {
            int value = tag.getInt("VisualLock");
            if (value < 0 || value > 2) {
                value = 0;
            }
            visualLockType = VisualLockType.values()[value];
        }
        boolean readValidLock = false;
        if (lockable != null) {
            readValidLock = lockable.readLock(tag);
        }
        if (!readValidLock) {
            // Read base lock
            Optional<UUID> diamondLock = OpenableBlockEntity.diamondLockFromTag(tag);
            if (diamondLock.isPresent()) {
                this.diamondLock = diamondLock.get();
            } else {
                Optional<Component> goldLock = OpenableBlockEntity.goldLockFromTag(tag);
                if (goldLock.isPresent()) {
                    this.goldLock = goldLock.get();
                } else {
                    this.vanillaLock = LockCode.fromTag(tag);
                }
            }
        }
    }

    // Client Only
    public final VisualLockType getVisualLock() {
        return visualLockType;
    }

    private static Optional<Component> goldLockFromTag(CompoundTag tag) {
        if (tag.contains("Gold_Lock", Tag.TAG_STRING)) {
            return Optional.ofNullable(Component.Serializer.fromJson(tag.getString("Gold_Lock")));
        }
        return Optional.empty();
    }

    private static Optional<UUID> diamondLockFromTag(CompoundTag tag) {
        if (tag.hasUUID("Diamond_Lock")) {
            return Optional.of(tag.getUUID("Diamond_Lock"));
        }
        return Optional.empty();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.hasCustomName()) {
            tag.putString("CustomName", Component.Serializer.toJson(customName));
        }
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

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        VisualLockType value = diamondLock != null ? VisualLockType.DIAMOND : goldLock != null ? VisualLockType.GOLD : VisualLockType.DEFAULT;
        updateTag.putInt("VisualLock", value.ordinal());
        if (customName != null) {
            updateTag.putString("CustomName", Component.Serializer.toJson(customName));
        }
        return updateTag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public final ResourceLocation getBlockId() {
        return blockId;
    }

    public ItemAccess getItemAccess() {
        return itemAccess;
    }

    protected void setItemAccess(ItemAccess itemAccess) {
        if (this.itemAccess == null) this.itemAccess = itemAccess;
    }

    public Lockable getLockable() {
        return lockable;
    }

    protected void setLockable(Lockable lockable) {
        if (this.lockable == null) this.lockable = lockable;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return customName;
    }

    public final void setCustomName(Component name) {
        customName = name;
    }

    @NotNull
    public final Component getName() {
        return this.hasCustomName() ? customName : defaultName;
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        if (customName != null) {
            updateTag.putString("CustomName", Component.Serializer.toJson(customName));
        }
        return updateTag;
    }

    public boolean isDinnerbone() {
        return this.hasCustomName() && customName.getString().equals("Dinnerbone");
    }
}
