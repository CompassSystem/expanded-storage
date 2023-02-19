package ellemes.expandedstorage.thread.datagen.providers;

import ellemes.expandedstorage.common.datagen.providers.ConversionRecipeProvider;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.recipe.block.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.block.PartialBlockState;
import ellemes.expandedstorage.common.recipe.conditions.IsInTagCondition;
import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
import ellemes.expandedstorage.common.registration.ModBlocks;
import ellemes.expandedstorage.thread.datagen.content.ThreadTags;
import net.minecraft.data.DataGenerator;

import java.util.List;

public class ThreadConversionRecipeProvider extends ConversionRecipeProvider {
    public ThreadConversionRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void registerBlockRecipes() {
        super.registerBlockRecipes();
        registerBlockRecipe(Utils.id("vanilla_to_wood_chest"),
                new BlockConversionRecipe<>(ConversionRecipeProvider.UNNAMED_MUTATOR, new PartialBlockState<>(ModBlocks.WOOD_CHEST), List.of(
                        new IsInTagCondition(ThreadTags.Blocks.WOODEN_CHESTS),
                        RecipeCondition.IS_WOODEN_CHEST
                ))
        );
    }
}
