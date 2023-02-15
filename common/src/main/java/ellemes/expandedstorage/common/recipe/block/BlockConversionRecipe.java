package ellemes.expandedstorage.common.recipe.block;

import ellemes.expandedstorage.api.EsChestType;
import ellemes.expandedstorage.common.block.AbstractChestBlock;
import ellemes.expandedstorage.common.item.ToolUsageResult;
import ellemes.expandedstorage.common.recipe.misc.RecipeCondition;
import ellemes.expandedstorage.common.recipe.misc.RecipeTool;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockConversionRecipe<O extends Block> {
    private final RecipeTool recipeTool;
    private final PartialBlockState<O> output;
    private final Collection<RecipeCondition<BlockState>> inputs;

    public BlockConversionRecipe(RecipeTool recipeTool, PartialBlockState<O> output, Collection<RecipeCondition<BlockState>> inputs) {
        this.recipeTool = recipeTool;
        this.output = output;
        this.inputs = inputs;
    }

    public boolean inputMatches(BlockState state) {
        return inputs.stream().anyMatch(condition -> condition.test(state));
    }

    public final ToolUsageResult process(Level level, BlockState state, BlockPos pos) {
        return ToolUsageResult.fail();
    }

    public final int getUsageCount(BlockState input) {
        boolean isVanillaDoubleChest = input.getBlock() instanceof ChestBlock && input.getValue(ChestBlock.TYPE) != ChestType.SINGLE;
        boolean isOurDoubleChest = input.getBlock() instanceof AbstractChestBlock && input.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE;
        return isVanillaDoubleChest || isOurDoubleChest ? 2 : 1;
    }

    public final PartialBlockState<O> getOutputType() {
        return output;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        recipeTool.writeToBuffer(buffer);
        output.writeToBuffer(buffer);
        buffer.writeCollection(inputs, (b, recipeCondition) -> recipeCondition.writeToBuffer(b));
    }

    public static BlockConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        RecipeTool recipeTool = RecipeTool.fromNetworkBuffer(buffer);
        PartialBlockState<?> output = PartialBlockState.readFromBuffer(buffer);
        List<RecipeCondition<BlockState>> inputs = buffer.readCollection(ArrayList::new, RecipeCondition::readFromBuffer);
        return new BlockConversionRecipe<>(recipeTool, output, inputs);
    }

    public boolean toolMatches(ItemStack tool) {
        return recipeTool.isMatchFor(tool);
    }
}
