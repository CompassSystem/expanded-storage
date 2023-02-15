package ellemes.expandedstorage.common.recipe.entity;

import ellemes.expandedstorage.common.recipe.misc.RecipeCondition;
import ellemes.expandedstorage.common.recipe.misc.RecipeTool;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class EntityConversionRecipe<O extends Entity> {
    private final RecipeTool recipeTool;
    private final EntityType<O> output;
    private final Collection<RecipeCondition<Entity>> inputs;

    public EntityConversionRecipe(RecipeTool recipeTool, EntityType<O> output, Collection<RecipeCondition<Entity>> inputs) {
        this.recipeTool = recipeTool;
        this.output = output;
        this.inputs = inputs;
    }

    public boolean inputMatches(Entity entity) {
        return inputs.stream().anyMatch(condition -> condition.test(entity));
    }

    public void process(Level level, Entity input) {

    }

    public int getUsageCount(Entity input) {
        return 1;
    }

    public EntityType<O> getOutputType() {
        return output;
    }

    public boolean toolMatches(ItemStack tool) {
        return recipeTool.isMatchFor(tool);
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(Registry.ENTITY_TYPE.getKey(output));
        buffer.writeCollection(inputs, (b, condition) -> condition.writeToBuffer(buffer));
    }

    public static EntityConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        return null;
    }
}
