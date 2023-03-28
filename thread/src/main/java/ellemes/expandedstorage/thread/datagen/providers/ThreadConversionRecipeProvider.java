package ellemes.expandedstorage.thread.datagen.providers;

import ellemes.expandedstorage.common.datagen.providers.ConversionRecipeProvider;
import ellemes.expandedstorage.common.recipe.conditions.IsInTagCondition;
import ellemes.expandedstorage.common.recipe.conditions.OrCondition;
import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
import ellemes.expandedstorage.thread.datagen.content.ThreadTags;
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
