package ellemes.expandedstorage.common.item;

import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.recipe.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.ConversionRecipeManager;
import ellemes.expandedstorage.common.recipe.EntityConversionRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class StorageConversionKit extends Item implements EntityInteractableItem {
    public static final ToolUsageResult NOT_ENOUGH_UPGRADES = ToolUsageResult.fail();
    private final Component instructionsFirst;
    private final Component instructionsSecond;

    public StorageConversionKit(Properties settings, ResourceLocation fromTier, ResourceLocation toTier, boolean manuallyWrapTooltips) {
        super(settings);
        if (manuallyWrapTooltips) {
            this.instructionsFirst = Component.translatable("tooltip.expandedstorage.conversion_kit_" + fromTier.getPath() + "_" + toTier.getPath() + "_1", Utils.ALT_USE).withStyle(ChatFormatting.GRAY);
            this.instructionsSecond = Component.translatable("tooltip.expandedstorage.conversion_kit_" + fromTier.getPath() + "_" + toTier.getPath() + "_2", Utils.ALT_USE).withStyle(ChatFormatting.GRAY);
        } else {
            this.instructionsFirst = Component.translatable("tooltip.expandedstorage.conversion_kit_" + fromTier.getPath() + "_" + toTier.getPath() + "_1", Utils.ALT_USE).withStyle(ChatFormatting.GRAY).append(Component.translatable("tooltip.expandedstorage.conversion_kit_" + fromTier.getPath() + "_" + toTier.getPath() + "_2", Utils.ALT_USE).withStyle(ChatFormatting.GRAY));
            this.instructionsSecond = Component.literal("");
        }
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player != null) {
            if (player.isShiftKeyDown()) {
                ItemStack tool = context.getItemInHand();
                BlockPos pos = context.getClickedPos();
                BlockState state = level.getBlockState(pos);
                BlockConversionRecipe<?> recipe = ConversionRecipeManager.INSTANCE.getBlockRecipe(state, tool);
                if (recipe != null) {
                    if (level.isClientSide()) {
                        return InteractionResult.CONSUME;
                    }
                    ToolUsageResult result = recipe.process(level, player, tool, state, pos);
                    if (result == NOT_ENOUGH_UPGRADES) {
                        player.displayClientMessage(Component.translatable("tooltip.expandedstorage.conversion_kit.need_x_upgrades", 1), true);
                    } else if (result.getResult().shouldSwing()) {
                        player.getCooldowns().addCooldown(this, Utils.TOOL_USAGE_DELAY);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag context) {
        list.add(instructionsFirst);
        if (!instructionsSecond.getString().equals("")) {
            list.add(instructionsSecond);
        }
    }

    @Override
    public InteractionResult es_interactEntity(Level level, Entity entity, Player player, InteractionHand hand, ItemStack stack) {
        EntityConversionRecipe<?> recipe = ConversionRecipeManager.INSTANCE.getEntityRecipe(entity, stack);
        if (recipe != null) {
            if (recipe.process(level, player, stack, entity).shouldSwing()) {
                player.getCooldowns().addCooldown(this, Utils.TOOL_USAGE_DELAY);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
}
