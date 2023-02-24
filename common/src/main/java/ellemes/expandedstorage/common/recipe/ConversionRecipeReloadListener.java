package ellemes.expandedstorage.common.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ellemes.expandedstorage.common.recipe.misc.PartialBlockState;
import ellemes.expandedstorage.common.recipe.misc.JsonHelper;
import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
import ellemes.expandedstorage.common.recipe.misc.RecipeTool;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConversionRecipeReloadListener extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("expanded-storage");
    private final List<BlockConversionRecipe<?>> blockRecipes = new ArrayList<>();
    private final List<EntityConversionRecipe<?>> entityRecipes = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().create();

    public ConversionRecipeReloadListener() {
        super(GSON, "conversion_recipes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        recipes.forEach((name, json) -> {
            try {
                parseRecipe(json);
            } catch (Exception e) {
                LOGGER.error("Invalid conversion recipe " + name, e);
            }
        });

        ConversionRecipeManager.INSTANCE.replaceAllRecipes(blockRecipes, entityRecipes);
    }

    private void parseRecipe(JsonElement json) throws JsonSyntaxException {
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("root must be a json object");
        }
        JsonObject root = json.getAsJsonObject();

        ResourceLocation type = JsonHelper.getJsonResourceLocation(root, "type");
        RecipeTool recipeTool = RecipeTool.fromJsonObject(JsonHelper.getJsonObject(root, "tool"));

        if (type.toString().equals("expandedstorage:block_conversion")) {
            parseBlockRecipe(root, recipeTool);
        } else if (type.toString().equals("expandedstorage:entity_conversion")) {
            parseEntityRecipe(root, recipeTool);
        } else {
            throw new JsonSyntaxException("type must be either: \"expandedstorage:block_conversion\" or \"expandedstorage:entity_conversion\"");
        }
    }

    private void parseBlockRecipe(JsonObject root, RecipeTool recipeTool) {
        JsonArray inputs = JsonHelper.getJsonArray(root, "inputs");
        RecipeCondition[] recipeInputs = new RecipeCondition[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            JsonElement input = inputs.get(i);
            recipeInputs[i] = RecipeCondition.readBlockCondition(input);
        }
        PartialBlockState<?> output = PartialBlockState.readFromJson(JsonHelper.getJsonObject(root, "result"));
        blockRecipes.add(new BlockConversionRecipe<>(recipeTool, output, Arrays.asList(recipeInputs)));
    }

    private void parseEntityRecipe(JsonObject root, RecipeTool recipeTool) {
        JsonArray inputs = JsonHelper.getJsonArray(root, "inputs");
        RecipeCondition[] recipeInputs = new RecipeCondition[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            JsonElement input = inputs.get(i);
            recipeInputs[i] = RecipeCondition.readEntityCondition(input);
        }
        EntityType<?> output = Registry.ENTITY_TYPE.getOptional(JsonHelper.getJsonResourceLocation(JsonHelper.getJsonObject(root, "result"), "id")).orElseThrow();
        entityRecipes.add(new EntityConversionRecipe<>(recipeTool, output, Arrays.asList(recipeInputs)));
    }
}
