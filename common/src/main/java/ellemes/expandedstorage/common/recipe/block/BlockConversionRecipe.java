package ellemes.expandedstorage.common.recipe.block;

import ellemes.expandedstorage.api.EsChestType;
import ellemes.expandedstorage.common.block.AbstractChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public abstract class BlockConversionRecipe<O extends Block> {
    private final O output;

    public BlockConversionRecipe(O output) {
        this.output = output;
    }

    public abstract boolean inputMatches(Block input);

    public final void process(Level level, BlockState state, BlockPos pos) {

    }

    public final int getUsageCount(BlockState input) {
        boolean isVanillaDoubleChest = input.getBlock() instanceof ChestBlock && input.getValue(ChestBlock.TYPE) != ChestType.SINGLE;
        boolean isOurDoubleChest = input.getBlock() instanceof AbstractChestBlock && input.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE;
        return isVanillaDoubleChest || isOurDoubleChest ? 2 : 1;
    }

    public final O getOutputType() {
        return output;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(Registry.BLOCK.getKey(output));
    }
}
