package compasses.expandedstorage.common.block.entity.extendable;

import compasses.expandedstorage.common.block.OpenableBlock;
import compasses.expandedstorage.common.block.strategies.ItemAccess;
import compasses.expandedstorage.common.block.strategies.Lockable;
import compasses.expandedstorage.common.inventory.OpenableInventory;
import compasses.expandedstorage.common.misc.LockHolder;
import compasses.expandedstorage.common.misc.VisualLockType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class OpenableBlockEntity extends BlockEntity implements OpenableInventory, Nameable {
    private final Component defaultName;
    private ItemAccess itemAccess;
    private Component customName;
    // Client only
    private VisualLockType visualLockType = VisualLockType.NONE;
    private LockHolder lockHolder = new LockHolder(this::syncBlockEntityData);

    public OpenableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component defaultName, @Nullable Supplier<Lockable> lockable) {
        super(type, pos, state);
        this.defaultName = defaultName;
        if (lockable != null) {
            lockHolder.setLockable(lockable.get());
        }
    }

    @Override
    public boolean canBeUsedBy(ServerPlayer player) {
        return this.isValidAndPlayerInRange(player) && lockHolder.canOpen(player);
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
        lockHolder.readLockFromTag(tag);
    }

    // Client Only
    public final VisualLockType getVisualLock() {
        return visualLockType;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.hasCustomName()) {
            tag.putString("CustomName", Component.Serializer.toJson(customName));
        }
        getLockHolder().saveLockToTag(tag);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        updateTag.putInt("VisualLock", lockHolder.getVisualStyle().ordinal());
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

    public ItemAccess getItemAccess() {
        return itemAccess;
    }

    protected void setItemAccess(ItemAccess itemAccess) {
        if (this.itemAccess == null) this.itemAccess = itemAccess;
    }

    @Nullable
    public Lockable getLockable() {
        return lockHolder.getLockable();
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

    public boolean isDinnerbone() {
        return this.hasCustomName() && customName.getString().equals("Dinnerbone");
    }

    public void copyLockFrom(LockHolder other) {
        lockHolder.copyFrom(other);
    }

    private void syncBlockEntityData(VisualLockType type) {
        level.setBlock(worldPosition, getBlockState().setValue(OpenableBlock.LOCK_TYPE, type), Block.UPDATE_ALL);
    }

    public LockHolder getLockHolder() {
        return lockHolder;
    }

    public void setVisualLockStyle(VisualLockType visualStyle) {
        visualLockType = visualStyle;
    }
}
