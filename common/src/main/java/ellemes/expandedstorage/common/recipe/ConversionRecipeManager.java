package ellemes.expandedstorage.common.recipe;

import ellemes.expandedstorage.common.recipe.block.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.block.ManyBlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.block.SingleBlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.EntityConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.ManyEntityConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.SingleEntityConversionRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class ConversionRecipeManager {
    public static final ConversionRecipeManager INSTANCE = new ConversionRecipeManager();

    private final List<BlockConversionRecipe<?>> blockRecipes = new ArrayList<>();
    private final List<EntityConversionRecipe<?>> entityRecipes = new ArrayList<>();

    public BlockConversionRecipe<?> getBlockRecipe(Block block) {
        for (BlockConversionRecipe<?> recipe : blockRecipes) {
            if (recipe.inputMatches(block)) {
                return recipe;
            }
        }
        return null;
    }

    public EntityConversionRecipe<?> getEntityRecipe(EntityType<?> entity) {
        for (EntityConversionRecipe<?> recipe : entityRecipes) {
            if (recipe.inputMatches(entity)) {
                return recipe;
            }
        }
        return null;
    }

    public void writeRecipesToNetworkBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(new ResourceLocation("expandedstorage", "single_block"));
        buffer.writeCollection(blockRecipes.stream()
                                           .filter(recipe -> recipe instanceof SingleBlockConversionRecipe<?>)
                                           .map(recipe -> (SingleBlockConversionRecipe<?>) recipe).toList(),
                (b, recipe) -> recipe.writeToBuffer(b));
        buffer.writeResourceLocation(new ResourceLocation("expandedstorage", "many_block"));
        buffer.writeCollection(blockRecipes.stream()
                                           .filter(recipe -> recipe instanceof ManyBlockConversionRecipe<?>)
                                           .map(recipe -> (ManyBlockConversionRecipe<?>) recipe).toList(),
                (b, recipe) -> recipe.writeToBuffer(b));

        buffer.writeResourceLocation(new ResourceLocation("expandedstorage", "single_entity"));
        buffer.writeCollection(entityRecipes.stream()
                                           .filter(recipe -> recipe instanceof SingleEntityConversionRecipe<?>)
                                           .map(recipe -> (SingleEntityConversionRecipe<?>) recipe).toList(),
                (b, recipe) -> recipe.writeToBuffer(b));
        buffer.writeResourceLocation(new ResourceLocation("expandedstorage", "many_entity"));
        buffer.writeCollection(entityRecipes.stream()
                                           .filter(recipe -> recipe instanceof ManyEntityConversionRecipe<?>)
                                           .map(recipe -> (ManyEntityConversionRecipe<?>) recipe).toList(),
                (b, recipe) -> recipe.writeToBuffer(b));
    }

    public void replaceAllRecipes(List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes) {
        this.blockRecipes.clear();
        this.blockRecipes.addAll(blockRecipes);
        this.entityRecipes.clear();
        this.entityRecipes.addAll(entityRecipes);
    }
}
