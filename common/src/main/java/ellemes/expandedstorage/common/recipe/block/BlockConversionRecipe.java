package ellemes.expandedstorage.common.recipe.block;

import ellemes.expandedstorage.api.EsChestType;
import ellemes.expandedstorage.common.block.AbstractChestBlock;
import ellemes.expandedstorage.common.recipe.RecipeType;
import ellemes.expandedstorage.common.recipe.misc.RecipeCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockConversionRecipe<O extends Block> {
    private final RecipeType recipeType;
    private final PartialBlockState<O> output;
    private final Collection<RecipeCondition<BlockState>> inputs;

    public BlockConversionRecipe(RecipeType recipeType, PartialBlockState<O> output, Collection<RecipeCondition<BlockState>> inputs) {
        this.recipeType = recipeType;
        this.output = output;
        this.inputs = inputs;
    }

    public boolean inputMatches(BlockState state) {
        return inputs.stream().anyMatch(partial -> partial.test(state));
    }

    public final void process(Level level, BlockState state, BlockPos pos) {

    }

    public final int getUsageCount(BlockState input) {
        boolean isVanillaDoubleChest = input.getBlock() instanceof ChestBlock && input.getValue(ChestBlock.TYPE) != ChestType.SINGLE;
        boolean isOurDoubleChest = input.getBlock() instanceof AbstractChestBlock && input.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE;
        return isVanillaDoubleChest || isOurDoubleChest ? 2 : 1;
    }

    public final PartialBlockState<O> getOutputType() {
        return output;
    }

    public RecipeType getType() {
        return recipeType;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(recipeType);
        output.writeToBuffer(buffer);
        buffer.writeCollection(inputs, (b, recipeCondition) -> recipeCondition.writeToBuffer(b));
    }

    public static BlockConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        RecipeType recipeType = buffer.readEnum(RecipeType.class);
        PartialBlockState<?> output = PartialBlockState.readFromBuffer(buffer);
        List<RecipeCondition<BlockState>> inputs = buffer.readCollection(ArrayList::new, RecipeCondition::readFromBuffer);
        return new BlockConversionRecipe<>(recipeType, output, inputs);
    }
}
