package ellemes.expandedstorage.common.recipe.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ellemes.expandedstorage.common.recipe.ConversionRecipe;
import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
import ellemes.expandedstorage.common.recipe.misc.RecipeTool;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityConversionRecipe<O extends Entity> extends ConversionRecipe<Entity> {
    private final EntityType<O> output;

    public EntityConversionRecipe(RecipeTool recipeTool, EntityType<O> output, Collection<? extends RecipeCondition> inputs) {
        super(recipeTool, inputs);
        this.output = output;
    }

    public InteractionResult process(Level level, Entity input) {
        return InteractionResult.FAIL;
    }

    public int getUsageCount(Entity input) {
        return 1;
    }

    public EntityType<O> getOutputType() {
        return output;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        recipeTool.writeToBuffer(buffer);
        buffer.writeResourceLocation(Registry.ENTITY_TYPE.getKey(output));
        buffer.writeCollection(inputs, (b, condition) -> {
            b.writeResourceLocation(condition.getNetworkId());
            condition.writeToBuffer(buffer);
        });
    }

    public static EntityConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        RecipeTool recipeTool = RecipeTool.fromNetworkBuffer(buffer);
        EntityType<?> output = Registry.ENTITY_TYPE.get(buffer.readResourceLocation());
        List<RecipeCondition> inputs = buffer.readCollection(ArrayList::new, RecipeCondition::readFromBuffer);
        return new EntityConversionRecipe<>(recipeTool, output, inputs);
    }

    public JsonElement toJson() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "expandedstorage:entity_conversion");
        recipe.add("tool", recipeTool.toJson());
        recipe.addProperty("result", output.builtInRegistryHolder().key().location().toString());
        JsonArray jsonInputs = new JsonArray();
        for (RecipeCondition input : inputs) {
            jsonInputs.add(input.toJson());
        }
        recipe.add("inputs", jsonInputs);
        return recipe;
    }
}
