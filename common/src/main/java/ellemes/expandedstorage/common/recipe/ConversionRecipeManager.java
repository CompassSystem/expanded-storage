package ellemes.expandedstorage.common.recipe;

import ellemes.expandedstorage.common.recipe.block.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.EntityConversionRecipe;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversionRecipeManager {
    public static final ConversionRecipeManager INSTANCE = new ConversionRecipeManager();

    private final List<BlockConversionRecipe<?>> blockRecipes = new ArrayList<>();
    private final List<EntityConversionRecipe<?>> entityRecipes = new ArrayList<>();

    public BlockConversionRecipe<?> getBlockRecipe(BlockState state, ItemStack tool) {
        for (BlockConversionRecipe<?> recipe : blockRecipes) {
            if (recipe.toolMatches(tool) && recipe.inputMatches(state)) {
                return recipe;
            }
        }
        return null;
    }

    public EntityConversionRecipe<?> getEntityRecipe(Entity entity, ItemStack tool) {
        for (EntityConversionRecipe<?> recipe : entityRecipes) {
            if (recipe.toolMatches(tool) && recipe.inputMatches(entity)) {
                return recipe;
            }
        }
        return null;
    }

    public List<BlockConversionRecipe<?>> getBlockRecipes() {
        return Collections.unmodifiableList(blockRecipes);
    }

    public List<EntityConversionRecipe<?>> getEntityRecipes() {
        return Collections.unmodifiableList(entityRecipes);
    }

    public void replaceAllRecipes(List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes) {
        System.out.println("DEBUG");
        System.out.println("UPDATING CONVERSION RECIPES");
        this.blockRecipes.clear();
        this.blockRecipes.addAll(blockRecipes);
        this.entityRecipes.clear();
        this.entityRecipes.addAll(entityRecipes);
    }
}
