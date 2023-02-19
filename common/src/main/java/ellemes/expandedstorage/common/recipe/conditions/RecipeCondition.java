package ellemes.expandedstorage.common.recipe.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ellemes.expandedstorage.common.recipe.misc.JsonHelper;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This uhhhhh system is uhhhhhhhhh very uhhhhhhhhh yes...
 * Is it over-engineer, probably not going to be used and eventually be removed? Yes!
 */
public interface RecipeCondition {
    Map<ResourceLocation, Function<FriendlyByteBuf, RecipeCondition>> RECIPE_DESERIALIZERS = new HashMap<>();
    RecipeCondition IS_WOODEN_CHEST = new IsInstanceOfCondition(ChestBlock.class);
    RecipeCondition IS_WOODEN_BARREL = new IsInstanceOfCondition(BarrelBlock.class);

    private static <T> RecipeCondition tryReadGenericCondition(JsonElement condition, Registry<T> registry) {
        if (condition.isJsonObject()) {
            JsonObject object = condition.getAsJsonObject();
            if (object.has("tag")) {
                TagKey<T> tag = TagKey.create(registry.key(), JsonHelper.getJsonResourceLocation(object, "tag"));
                return new IsInTagCondition(tag);
            } else if (object.has("id")) {
                return new IsRegistryObject(registry, JsonHelper.getJsonResourceLocation(object, "id"));
            }
        } else if (condition.isJsonArray()) {
            JsonArray conditions = condition.getAsJsonArray();
            RecipeCondition[] recipeConditions = new RecipeCondition[conditions.size()];
            Function<JsonElement, RecipeCondition> function = registry == Registry.BLOCK ? RecipeCondition::readBlockCondition : RecipeCondition::readEntityCondition;
            for (int i = 0; i < conditions.size(); i++) {
                recipeConditions[i] = function.apply(conditions.get(i));
            }
            return new AndCondition(Arrays.asList(recipeConditions));
        } else {
            throw new JsonSyntaxException("condition must be an Object or an Array.");
        }
        return null;
    }

    static RecipeCondition readBlockCondition(JsonElement condition) {
        RecipeCondition generic = tryReadGenericCondition(condition, Registry.BLOCK);
        if (generic != null) {
            return generic;
        }
        if (condition.isJsonObject()) {
            JsonObject recipeCondition = condition.getAsJsonObject();
            if (recipeCondition.has("condition")) {
                ResourceLocation conditionId = JsonHelper.getJsonResourceLocation(recipeCondition, "condition");
                if (conditionId.toString().equals("expandedstorage:is_wooden_chest")) {
                    return RecipeCondition.IS_WOODEN_CHEST;
                } else if (conditionId.toString().equals("expandedstorage:is_wooden_barrel")) {
                    return RecipeCondition.IS_WOODEN_BARREL;
                } else {
                    throw new IllegalArgumentException("condition with id " + conditionId + " doesn't exist.");
                }
            }
        }
        throw new JsonSyntaxException("Unknown recipe condition");
    }

    static RecipeCondition readEntityCondition(JsonElement condition) {
        RecipeCondition generic = tryReadGenericCondition(condition, Registry.ENTITY_TYPE);
        if (generic != null) {
            return generic;
        }
        throw new JsonSyntaxException("Unknown recipe condition");
    }

    // Honestly not sure if we like this solution
    static Object unwrap(Object subject) {
        if (subject instanceof BlockState state) {
            return state.getBlock();
        } else if (subject instanceof Entity entity) {
            return entity.getType();
        }
        return subject;
    }

    boolean test(Object subject);

    ResourceLocation getNetworkId();

    void writeToBuffer(FriendlyByteBuf buffer);

    static <T> RecipeCondition readFromBuffer(FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        return RECIPE_DESERIALIZERS.get(id).apply(buffer);
    }

    JsonElement toJson();
}

