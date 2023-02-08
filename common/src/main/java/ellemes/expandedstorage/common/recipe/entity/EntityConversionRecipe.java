package ellemes.expandedstorage.common.recipe.entity;

import ellemes.expandedstorage.common.recipe.RecipeType;
import ellemes.expandedstorage.common.recipe.misc.RecipeCondition;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class EntityConversionRecipe<O extends Entity> {
    private final RecipeType recipeType;
    private final EntityType<O> output;
    private final Collection<RecipeCondition<Entity>> inputs;

    public EntityConversionRecipe(RecipeType recipeType, EntityType<O> output, Collection<RecipeCondition<Entity>> inputs) {
        this.recipeType = recipeType;
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

    public RecipeType getType() {
        return recipeType;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(Registry.ENTITY_TYPE.getKey(output));
        buffer.writeCollection(inputs, (b, condition) -> condition.writeToBuffer(buffer));
    }

    public static EntityConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        return null;
    }
}
