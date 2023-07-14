package compasses.expandedstorage.impl.item;

import compasses.expandedstorage.api.EsChestType;
import compasses.expandedstorage.impl.block.AbstractChestBlock;
import compasses.expandedstorage.impl.block.BarrelBlock;
import compasses.expandedstorage.impl.block.MiniStorageBlock;
import compasses.expandedstorage.impl.block.entity.OpenableBlockEntity;
import compasses.expandedstorage.impl.misc.Utils;
import compasses.expandedstorage.impl.recipe.BlockConversionRecipe;
import compasses.expandedstorage.impl.recipe.ConversionRecipeManager;
import compasses.expandedstorage.impl.recipe.EntityConversionRecipe;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class StorageMutator extends Item implements EntityInteractableItem {
    public StorageMutator(Properties settings) {
        super(settings);
    }

    public static MutationMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains("mode", Tag.TAG_BYTE)) {
            tag.putByte("mode", (byte) 0);
        }

        return MutationMode.from(tag.getByte("mode"));
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        ToolUsageResult result = StorageMutator.tryMutation(state.getBlock(), StorageMutator.getMode(stack), context, level, state, pos, stack);

        if (result == null) {
            return InteractionResult.FAIL;
        }

        if (result.getResult().shouldSwing()) {
            //noinspection ConstantConditions
            context.getPlayer().getCooldowns().addCooldown(this, result.getDelay());
        }

        return result.getResult();
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();
        MutationMode nextMode = StorageMutator.getMode(stack).next();

        tag.putByte("mode", nextMode.toByte());
        tag.remove("pos");

        player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.description_" + nextMode, Utils.ALT_USE), true);
        player.getCooldowns().addCooldown(this, Utils.TOOL_USAGE_QUICK_DELAY);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);

        StorageMutator.getMode(stack);
    }

    @NotNull
    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();

        StorageMutator.getMode(stack);

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag context) {
        MutationMode mode = StorageMutator.getMode(stack);
        list.add(Component.translatable("tooltip.expandedstorage.storage_mutator.tool_mode", Component.translatable("tooltip.expandedstorage.storage_mutator." + mode)).withStyle(ChatFormatting.GRAY));
        list.add(Component.translatable("tooltip.expandedstorage.storage_mutator.description_" + mode, Utils.ALT_USE).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult es_interactEntity(Level level, Entity entity, Player player, InteractionHand hand, ItemStack stack) {
        MutationMode mode = StorageMutator.getMode(stack);

        if (mode == MutationMode.SWAP_THEME) {
            EntityConversionRecipe<?> recipe = ConversionRecipeManager.INSTANCE.getEntityRecipe(entity, stack);

            if (recipe != null) {
                InteractionResult result = recipe.process(level, player, stack, entity);

                if (result.shouldSwing()) {
                    player.getCooldowns().addCooldown(this, Utils.TOOL_USAGE_DELAY);
                }

                return result;
            }
        }

        return InteractionResult.FAIL;
    }

    public static ToolUsageResult tryMutation(Block block, MutationMode mode, UseOnContext context, Level level, BlockState state, BlockPos pos, ItemStack stack) {
        if (mode == MutationMode.SWAP_THEME) {
            return swapBlockTheme(context, level, state, pos, stack);
        }

        if (mode == MutationMode.ROTATE) {
            if (block instanceof MiniStorageBlock) {
                return rotateMiniStorageBlock(context, level, state, pos, stack);
            }

            if (block instanceof BarrelBlock ||
                    block instanceof net.minecraft.world.level.block.BarrelBlock ||
                    block.defaultBlockState().is(ConventionalBlockTags.WOODEN_BARRELS)
            ) {
                return rotateBarrelBlock(context, level, state, pos, stack);
            }
        }

        if (block instanceof AbstractChestBlock) {
            if (mode == MutationMode.MERGE) {
                return mergeChestBlock(context, level, state, pos, stack);
            } else if (mode == MutationMode.SPLIT) {
                return splitChestBlock(context, level, state, pos, stack);
            } else if (mode == MutationMode.ROTATE) {
                return rotateChestBlock(context, level, state, pos, stack);
            }
        }

        return null;
    }

    private static ToolUsageResult swapBlockTheme(UseOnContext context, Level level, BlockState state, BlockPos pos, ItemStack stack) {
        BlockConversionRecipe<?> recipe = ConversionRecipeManager.INSTANCE.getBlockRecipe(state, stack);

        if (recipe == null) {
            return ToolUsageResult.fail();
        }

        return recipe.process(level, context.getPlayer(), stack, state, pos);
    }

    private static ToolUsageResult rotateMiniStorageBlock(UseOnContext context, Level level, BlockState state, BlockPos pos, ItemStack stack) {
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, state.rotate(Rotation.CLOCKWISE_90));
        }

        return ToolUsageResult.slowSuccess();
    }

    private static ToolUsageResult rotateBarrelBlock(UseOnContext context, Level level, BlockState state, BlockPos pos, ItemStack stack) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, state.cycle(BlockStateProperties.FACING));
            }

            return ToolUsageResult.slowSuccess();
        }

        return ToolUsageResult.fail();
    }

    private static ToolUsageResult rotateChestBlock(UseOnContext context, Level level, BlockState state, BlockPos pos, ItemStack stack) {
        if (!level.isClientSide()) {
            EsChestType chestType = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);

            if (chestType == EsChestType.SINGLE) {
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
            } else {
                BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                BlockState otherState = level.getBlockState(otherPos);

                if (chestType == EsChestType.TOP || chestType == EsChestType.BOTTOM) {
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
                    level.setBlockAndUpdate(otherPos, otherState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
                } else {
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE).getOpposite()));
                    level.setBlockAndUpdate(otherPos, otherState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, otherState.getValue(AbstractChestBlock.CURSED_CHEST_TYPE).getOpposite()));
                }
            }
        }

        return ToolUsageResult.slowSuccess();
    }

    private static ToolUsageResult splitChestBlock(UseOnContext context, Level level, BlockState state, BlockPos pos, ItemStack stack) {
        if (state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, EsChestType.SINGLE));
                // note: other state is updated to single via neighbour update
            }

            return ToolUsageResult.slowSuccess();
        }

        return ToolUsageResult.fail();
    }

    private static ToolUsageResult mergeChestBlock(UseOnContext context, Level level, BlockState state, BlockPos pos, ItemStack stack) {
        Player player = context.getPlayer();
        if (player == null) {
            return ToolUsageResult.fail();
        }

        if (state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE) {
            return ToolUsageResult.fail();
        }

        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains("pos")) {
            if (!level.isClientSide()) {
                tag.put("pos", NbtUtils.writeBlockPos(pos));
            }

            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_start", Utils.ALT_USE), true);
            return ToolUsageResult.fastSuccess();
        }

        tag.remove("pos");

        BlockPos otherPos = NbtUtils.readBlockPos(tag.getCompound("pos"));
        BlockState otherState = level.getBlockState(otherPos);
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());

        if (direction == null) {
            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_not_adjacent"), true);
            return ToolUsageResult.fail();
        }

        if (state.getBlock() != otherState.getBlock()) {
            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_wrong_block"), true);
            return ToolUsageResult.fail();
        }

        if (otherState.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE) {
            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_already_double_chest"), true);
            return ToolUsageResult.fail();
        }

        if (state.getValue(BlockStateProperties.HORIZONTAL_FACING) != otherState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_wrong_facing"), true);
            return ToolUsageResult.fail();
        }

        boolean firstIsDinnerbone = level.getBlockEntity(pos) instanceof OpenableBlockEntity blockEntity && blockEntity.isDinnerbone();
        boolean secondIsDinnerbone = level.getBlockEntity(otherPos) instanceof OpenableBlockEntity blockEntity && blockEntity.isDinnerbone();

        if (firstIsDinnerbone != secondIsDinnerbone) {
            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_wrong_block"), true);
            return ToolUsageResult.fail();
        }

        if (!level.isClientSide()) {
            EsChestType chestType = AbstractChestBlock.getChestType(state.getValue(BlockStateProperties.HORIZONTAL_FACING), direction);
            level.setBlockAndUpdate(pos, state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, chestType));
            // note: other state is updated via neighbour update
        }

        player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_end"), true);
        return ToolUsageResult.slowSuccess();
    }
}
