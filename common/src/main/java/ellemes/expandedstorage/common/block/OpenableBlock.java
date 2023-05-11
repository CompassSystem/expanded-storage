package ellemes.expandedstorage.common.block;

import ellemes.expandedstorage.api.v3.OpenableInventoryProvider;
import ellemes.expandedstorage.api.v3.context.BlockContext;
import ellemes.expandedstorage.api.v4.InventoryOpeningApi;
import ellemes.expandedstorage.common.CommonMain;
import ellemes.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import ellemes.expandedstorage.common.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class OpenableBlock extends Block implements OpenableInventoryProvider<BlockContext>, EntityBlock {
    private final ResourceLocation openingStat;
    private final int slotCount;

    public OpenableBlock(Properties settings, ResourceLocation openingStat, int slotCount) {
        super(settings);
        this.openingStat = openingStat;
        this.slotCount = slotCount;
    }

    public Component getInventoryTitle() {
        Component result = this.getName();
        return new TextComponent(result.getString().replace("Waxed ", ""));
    }

    public final ResourceLocation getBlockId() {
        return this.builtInRegistryHolder().key().location();
    }

    public final int getSlotCount() {
        return slotCount;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (this == ModBlocks.BAMBOO_CHEST && CommonMain.platformHelper().canDestroyBamboo(player.getMainHandItem())) {
            return 1.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean bl) {
        if (state.getBlock().getClass() != newState.getBlock().getClass()) {
            if (level.getBlockEntity(pos) instanceof OpenableBlockEntity entity) {
                Containers.dropContents(level, pos, entity.getItems());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, bl);
        } else {
            if (state.getBlock() != newState.getBlock() && level.getBlockEntity(pos) instanceof OpenableBlockEntity entity) {
                CompoundTag tag = entity.saveWithoutMetadata();
                level.removeBlockEntity(pos);
                if (level.getBlockEntity(pos) instanceof OpenableBlockEntity newEntity) {
                    newEntity.load(tag);
                }
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName() && level.getBlockEntity(pos) instanceof OpenableBlockEntity entity) {
            entity.setCustomName(stack.getHoverName());
        }
    }

    @Override
    public void onInitialOpen(ServerPlayer player) {
        player.awardStat(openingStat);
        if (!player.getLevel().isClientSide()) {
            PiglinAi.angerNearbyPiglins(player, true);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        boolean isClient = level.isClientSide();
        if (!isClient) {
            InventoryOpeningApi.openBlockInventory((ServerPlayer) player, pos, this);
        }
        return InteractionResult.sidedSuccess(isClient);
    }
}
