package ellemes.expandedstorage.common.recipe.block;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class SingleBlockConversionRecipe<O extends Block> extends BlockConversionRecipe<O> {
    private final Block inputBlock;

    public SingleBlockConversionRecipe(Block inputBlock, O output) {
        super(output);
        this.inputBlock = inputBlock;
    }

    @Override
    public boolean inputMatches(Block input) {
        return inputBlock == input;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        super.writeToBuffer(buffer);
        buffer.writeResourceLocation(Registry.BLOCK.getKey(inputBlock));
    }

    public static SingleBlockConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        ResourceLocation output = buffer.readResourceLocation();
        ResourceLocation input = buffer.readResourceLocation();
        return new SingleBlockConversionRecipe<>(Registry.BLOCK.get(input), Registry.BLOCK.get(output));
    }
}
