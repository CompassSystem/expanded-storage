package ellemes.expandedstorage.common.recipe;

import com.google.gson.JsonElement;
import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
import ellemes.expandedstorage.common.recipe.misc.RecipeTool;
import net.minecraft.world.item.ItemStack;

public abstract class ConversionRecipe<T> {
    protected final RecipeTool recipeTool;
    protected final RecipeCondition input;

    public ConversionRecipe(RecipeTool recipeTool, RecipeCondition input) {
        this.recipeTool = recipeTool;
        this.input = input;
    }

    public boolean inputMatches(T thing) {
        return input.test(thing);
    }

    public boolean isPreferredRecipe(T thing) {
        return input.test(thing) && input.isExactMatch();
    }

    public int getRecipeWeight(T thing, ItemStack tool) {
        int weight = 5;

        if (recipeTool.isMatchFor(tool) && inputMatches(thing)) {
            if (recipeTool instanceof RecipeTool.MutatorTool mutatorTool && mutatorTool.getRequiredName() != null) {
                weight += 10;
            }
        } else {
            return 0;
        }

        if (isPreferredRecipe(thing)) {
            weight += 5;
        }

        return weight;
    }

    public abstract JsonElement toJson();
}
