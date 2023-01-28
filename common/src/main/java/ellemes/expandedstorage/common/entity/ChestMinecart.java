package ellemes.expandedstorage.common.entity;

import ellemes.container_library.api.v3.OpenableInventory;
import ellemes.container_library.api.v3.OpenableInventoryProvider;
import ellemes.container_library.api.v3.client.ScreenOpeningApi;
import ellemes.container_library.api.v3.context.BaseContext;
import ellemes.expandedstorage.common.block.ChestBlock;
import ellemes.expandedstorage.common.misc.ExposedInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ChestMinecart extends AbstractMinecart implements ExposedInventory, OpenableInventoryProvider<BaseContext>, OpenableInventory {
    private final NonNullList<ItemStack> inventory;
    private final Item dropItem;
    private final BlockState renderBlockState;
    private final Component title;

    public ChestMinecart(EntityType<?> entityType, Level level, Item dropItem, ChestBlock block) {
        super(entityType, level);
        this.dropItem = dropItem;
        renderBlockState = block.defaultBlockState();
        title = dropItem.getDescription();
        inventory = NonNullList.withSize(block.getSlotCount(), ItemStack.EMPTY);
    }

    @Override
    public void destroy(DamageSource source) {
        super.destroy(source);
        if (level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.spawnAtLocation(dropItem);
            Containers.dropContents(level, this, this);

            if (!level.isClientSide()) {
                Entity entity = source.getDirectEntity();
                if (entity != null && entity.getType() == EntityType.PLAYER) {
                    PiglinAi.angerNearbyPiglins((Player) entity, true);
                }
            }
        }
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(dropItem);
    }

    @Override
    public BlockState getDisplayBlockState() {
        return renderBlockState;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        boolean isClient = level.isClientSide();
        if (isClient) {
            ScreenOpeningApi.openEntityInventory(this);
        }
        return InteractionResult.sidedSuccess(isClient);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!level.isClientSide() && reason.shouldDestroy()) {
            Containers.dropContents(level, this, this);
        }
        super.remove(reason);
    }

    @Override
    public Type getMinecartType() {
        return Type.CHEST;
    }

    public static ChestMinecart createMinecart(Level level, Vec3 pos, ResourceLocation cartItemId) {
        ChestMinecart cart = (ChestMinecart) Registry.ENTITY_TYPE.get(cartItemId).create(level);
        cart.setPos(pos);
        return cart;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isAlive() && player.distanceToSqr(this) <= 36.0D;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.saveInventoryToTag(tag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loadInventoryFromTag(tag);
    }

    @Override
    public OpenableInventory getOpenableInventory(BaseContext context) {
        return this;
    }

    @Override
    public boolean canBeUsedBy(ServerPlayer player) {
        return this.stillValid(player);
    }

    @Override
    public Container getInventory() {
        return this;
    }

    @Override
    public Component getInventoryTitle() {
        return this.hasCustomName() ? this.getCustomName() : title;
    }

    @Override
    public int getDefaultDisplayOffset() {
        return 8;
    }
}
