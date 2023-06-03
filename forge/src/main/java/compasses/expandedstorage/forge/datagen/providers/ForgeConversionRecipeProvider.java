package compasses.expandedstorage.forge.datagen.providers;

import compasses.expandedstorage.common.datagen.providers.ConversionRecipeProvider;
import compasses.expandedstorage.common.recipe.conditions.IsInTagCondition;
import compasses.expandedstorage.common.recipe.conditions.OrCondition;
import compasses.expandedstorage.common.recipe.conditions.RecipeCondition;
import compasses.expandedstorage.forge.datagen.content.ForgeTags;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;

public class ForgeConversionRecipeProvider extends ConversionRecipeProvider {
    public ForgeConversionRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void registerBlockRecipes() {
        super.registerBlockRecipes(
                new OrCondition(new IsInTagCondition(Tags.Blocks.BARRELS_WOODEN), RecipeCondition.IS_WOODEN_BARREL),
                new OrCondition(new IsInTagCondition(Tags.Blocks.CHESTS_WOODEN), RecipeCondition.IS_WOODEN_CHEST)
        );
    }

    @Override
    protected void registerEntityRecipes() {
        super.registerEntityRecipes(
                new IsInTagCondition(ForgeTags.Entities.WOODEN_CHEST_MINECARTS)
        );
    }
}
