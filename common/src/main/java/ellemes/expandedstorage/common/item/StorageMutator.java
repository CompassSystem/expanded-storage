package ellemes.expandedstorage.common.item;

import ellemes.expandedstorage.common.CommonMain;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.recipe.ConversionRecipeManager;
import ellemes.expandedstorage.common.recipe.EntityConversionRecipe;
import ellemes.expandedstorage.common.registration.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class StorageMutator extends Item implements EntityInteractableItem {
    public StorageMutator(Properties settings) {
        super(settings);
    }

    public static MutationMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("mode", Tag.TAG_BYTE))
            tag.putByte("mode", (byte) 0);

        return MutationMode.from(tag.getByte("mode"));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        BlockMutatorBehaviour behaviour = CommonMain.getBlockMutatorBehaviour(state.getBlock(), StorageMutator.getMode(stack));
        if (behaviour != null) {
            ToolUsageResult returnValue = behaviour.attempt(context, level, state, pos, stack);
            if (returnValue.getResult().shouldSwing()) {
                //noinspection ConstantConditions
                context.getPlayer().getCooldowns().addCooldown(this, returnValue.getDelay());
            }
            return returnValue.getResult();
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            CompoundTag tag = stack.getOrCreateTag();
            MutationMode nextMode = StorageMutator.getMode(stack).next();
            tag.putByte("mode", nextMode.toByte());
            if (tag.contains("pos"))
                tag.remove("pos");

            if (!level.isClientSide())
                player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.description_" + nextMode, Utils.ALT_USE), true);

            player.getCooldowns().addCooldown(this, Utils.TOOL_USAGE_QUICK_DELAY);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        StorageMutator.getMode(stack);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        StorageMutator.getMode(stack);
        return stack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks) {
        if (this.allowedIn(group)) {
            for (MutationMode mode : MutationMode.values()) {
                ItemStack stack = new ItemStack(ModItems.STORAGE_MUTATOR);
                CompoundTag tag = new CompoundTag();
                tag.putByte("mode", mode.toByte());
                stack.setTag(tag);
                stacks.add(stack);
            }

            ItemStack sparrowMutator = new ItemStack(ModItems.STORAGE_MUTATOR);
            CompoundTag tag = new CompoundTag();
            tag.putByte("mode", MutationMode.SWAP_THEME.toByte());
            sparrowMutator.setTag(tag);
            sparrowMutator.setHoverName(Component.literal("Sparrow").withStyle(ChatFormatting.ITALIC));
            stacks.add(sparrowMutator);
        }
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
}
