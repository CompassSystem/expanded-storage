package compasses.expandedstorage.common.item;

import compasses.expandedstorage.api.EsChestType;
import compasses.expandedstorage.common.block.AbstractChestBlock;
import compasses.expandedstorage.common.block.OpenableBlock;
import compasses.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import compasses.expandedstorage.common.entity.ChestMinecart;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LockItem extends Item implements EntityInteractableItem {
    /**
     * Boolean to represent the lock type, false = gold, true = diamond.
     */
    private final boolean lockType;

    public LockItem(Properties properties, boolean lockType) {
        super(properties);
        this.lockType = lockType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player != null) {
            if (player.isShiftKeyDown()) {
                BlockState state = level.getBlockState(context.getClickedPos());
                if (state.getBlock() instanceof OpenableBlock) {
                    OpenableBlockEntity entity = (OpenableBlockEntity) level.getBlockEntity(context.getClickedPos());
                    if (!entity.getLockHolder().hasLock()) {
                        if (level.isClientSide()) {
                            if (!lockType && !context.getItemInHand().hasCustomHoverName()) {
                                player.displayClientMessage(Component.translatable("chat.expandedstorage.lock_must_be_named"), true);
                                return InteractionResult.FAIL;
                            }
                            return InteractionResult.SUCCESS;
                        }
                        if (lockType) {
                            entity.getLockHolder().setDiamondLock(player.getUUID());
                            if (state.getBlock() instanceof AbstractChestBlock) {
                                if (state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE) {
                                    BlockPos otherPos = context.getClickedPos().relative(AbstractChestBlock.getDirectionToAttached(state));
                                    OpenableBlockEntity otherEntity = (OpenableBlockEntity) level.getBlockEntity(otherPos);
                                    otherEntity.getLockHolder().setDiamondLock(player.getUUID());
                                }
                            }
                        } else {
                            if (context.getItemInHand().hasCustomHoverName()) {
                                entity.getLockHolder().setGoldLock(context.getItemInHand().getHoverName());
                                if (state.getBlock() instanceof AbstractChestBlock) {
                                    if (state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE) {
                                        BlockPos otherPos = context.getClickedPos().relative(AbstractChestBlock.getDirectionToAttached(state));
                                        OpenableBlockEntity otherEntity = (OpenableBlockEntity) level.getBlockEntity(otherPos);
                                        otherEntity.getLockHolder().setGoldLock(context.getItemInHand().getHoverName());
                                    }
                                }
                            } else {
                                return InteractionResult.FAIL;
                            }
                        }
                        context.getItemInHand().setCount(context.getItemInHand().getCount() - 1);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult es_interactEntity(Level level, Entity entity, Player player, InteractionHand hand, ItemStack stack) {
        if (entity instanceof ChestMinecart chestMinecart) {
            if (!chestMinecart.getLockHolder().hasLock()) {
                if (level.isClientSide()) {
                    if (!lockType && !stack.hasCustomHoverName()) {
                        player.displayClientMessage(Component.translatable("chat.expandedstorage.lock_must_be_named"), true);
                        return InteractionResult.FAIL;
                    }
                    return InteractionResult.SUCCESS;
                }
                if (lockType) {
                    chestMinecart.getLockHolder().setDiamondLock(player.getUUID());
                } else {
                    if (stack.hasCustomHoverName()) {
                        chestMinecart.getLockHolder().setGoldLock(stack.getHoverName());
                    } else {
                        return InteractionResult.FAIL;
                    }
                }
                if (!player.isCreative()) {
                    stack.setCount(stack.getCount() - 1);
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if (lockType) {
            tooltipComponents.add(Component.translatable("tooltip.expandedstorage.diamond_lock").withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.expandedstorage.gold_lock").withStyle(ChatFormatting.GRAY));
        }
    }
}
