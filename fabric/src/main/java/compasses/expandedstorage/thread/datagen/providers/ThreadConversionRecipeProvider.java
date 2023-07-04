package compasses.expandedstorage.thread.datagen.providers;

import compasses.expandedstorage.common.datagen.providers.ConversionRecipeProvider;
import compasses.expandedstorage.common.recipe.conditions.IsInTagCondition;
import compasses.expandedstorage.common.recipe.conditions.OrCondition;
import compasses.expandedstorage.common.recipe.conditions.RecipeCondition;
import compasses.expandedstorage.thread.datagen.content.ThreadTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

public class ThreadConversionRecipeProvider extends ConversionRecipeProvider {
    public ThreadConversionRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    protected void registerBlockRecipes() {
        super.registerBlockRecipes(
                new OrCondition(new IsInTagCondition(ThreadTags.Blocks.WOODEN_BARRELS), RecipeCondition.IS_WOODEN_BARREL),
                new OrCondition(new IsInTagCondition(ThreadTags.Blocks.WOODEN_CHESTS), RecipeCondition.IS_WOODEN_CHEST)
        );
    }

    @Override
    protected void registerEntityRecipes() {
        super.registerEntityRecipes(new IsInTagCondition(ThreadTags.Entities.WOODEN_CHEST_MINECARTS));
    }
}
