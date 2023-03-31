package ellemes.expandedstorage.common.block.entity.extendable;

import ellemes.expandedstorage.api.v3.OpenableInventory;
import ellemes.expandedstorage.common.block.strategies.ItemAccess;
import ellemes.expandedstorage.common.block.strategies.Lockable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class OpenableBlockEntity extends BlockEntity implements OpenableInventory {
    private final ResourceLocation blockId;
    private final Component defaultName;
    private ItemAccess itemAccess;
    private Lockable lockable;
    private Component customName;

    public OpenableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ResourceLocation blockId, Component defaultName) {
        super(type, pos, state);
        this.blockId = blockId;
        this.defaultName = defaultName;
    }

    @Override
    public boolean canBeUsedBy(ServerPlayer player) {
        return this.isValidAndPlayerInRange(player) && this.getLockable().canPlayerOpenLock(player);
    }

    protected final boolean isValidAndPlayerInRange(Player player) {
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
        lockable.readLock(tag);
        if (tag.contains("CustomName", Tag.TAG_STRING)) {
            customName = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        lockable.writeLock(tag);
        if (this.hasCustomName()) {
            tag.putString("CustomName", Component.Serializer.toJson(customName));
        }
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

    public final boolean hasCustomName() {
        return customName != null;
    }

    public final void setCustomName(Component name) {
        customName = name;
    }

    public final Component getName() {
        return this.hasCustomName() ? customName : defaultName;
    }

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
