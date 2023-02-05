package ellemes.expandedstorage.common.recipe.block;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ManyBlockConversionRecipe<T extends Block> extends BlockConversionRecipe<T>{
    private final Collection<Block> inputBlocks;

    public ManyBlockConversionRecipe(Collection<Block> inputBlocks, T output) {
        super(output);
        this.inputBlocks = inputBlocks;
    }

    @Override
    public boolean inputMatches(Block block) {
        return inputBlocks.contains(block);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        super.writeToBuffer(buffer);
        buffer.writeCollection(inputBlocks, (b, block) -> b.writeResourceLocation(Registry.BLOCK.getKey(block)));
    }

    public static ManyBlockConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        ResourceLocation output = buffer.readResourceLocation();
        List<ResourceLocation> input = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation);
        return new ManyBlockConversionRecipe<>(input.stream().map(Registry.BLOCK::get).toList(), Registry.BLOCK.get(output));
    }
}
