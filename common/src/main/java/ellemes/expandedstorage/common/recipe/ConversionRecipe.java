package ellemes.expandedstorage.common.recipe;

import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
import ellemes.expandedstorage.common.recipe.misc.RecipeTool;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public abstract class ConversionRecipe<T> {
    protected final RecipeTool recipeTool;
    protected final Collection<? extends RecipeCondition> inputs;

    public ConversionRecipe(RecipeTool recipeTool, Collection<? extends RecipeCondition> inputs) {
        this.recipeTool = recipeTool;
        this.inputs = inputs;
    }

    public boolean inputMatches(T thing) {
        for (RecipeCondition input : inputs) {
            if (input.test(thing)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPreferredRecipe(T thing) {
        for (RecipeCondition input : inputs) {
            if (input.test(thing) && input.isExactMatch()) {
                return true;
            }
        }
        return false;
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
}
