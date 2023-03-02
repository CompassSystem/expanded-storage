package ellemes.expandedstorage.forge.datagen.providers;

import ellemes.expandedstorage.common.datagen.providers.ConversionRecipeProvider;
import ellemes.expandedstorage.common.recipe.conditions.IsInTagCondition;
import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
import ellemes.expandedstorage.forge.datagen.content.ForgeTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.Tags;

import java.util.List;

public class ForgeConversionRecipeProvider extends ConversionRecipeProvider {
    public ForgeConversionRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerBlockRecipes() {
        super.registerBlockRecipes(
                List.of(new IsInTagCondition(Tags.Blocks.BARRELS_WOODEN), RecipeCondition.IS_WOODEN_BARREL),
                List.of(new IsInTagCondition(Tags.Blocks.CHESTS_WOODEN), RecipeCondition.IS_WOODEN_CHEST)
        );
    }

    @Override
    protected void registerEntityRecipes() {
        super.registerEntityRecipes(
                List.of(new IsInTagCondition(ForgeTags.Entities.WOODEN_CHEST_MINECARTS))
        );
    }
}
