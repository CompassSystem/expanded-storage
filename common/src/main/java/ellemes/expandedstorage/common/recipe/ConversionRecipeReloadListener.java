package ellemes.expandedstorage.common.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ellemes.expandedstorage.common.recipe.block.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.EntityConversionRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
                parseRecipe(name, json);
            } catch (JsonSyntaxException e) {
                LOGGER.error("Invalid conversion recipe " + name, e);
            }
        });

//        blockRecipes.add(new BlockConversionRecipe<>(RecipeType.UPGRADE, PartialBlockState.of(ModBlocks.IRON_CHEST), List.of(PartialBlockState.of(ModBlocks.WOOD_CHEST))));
//        blockRecipes.add(new BlockConversionRecipe<>(RecipeType.UPGRADE, PartialBlockState.of(ModBlocks.GOLD_CHEST), List.of(PartialBlockState.of(ModBlocks.IRON_CHEST))));

        ConversionRecipeManager.INSTANCE.replaceAllRecipes(blockRecipes, entityRecipes, true);
    }

    private void parseRecipe(ResourceLocation name, JsonElement json) throws JsonSyntaxException {
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("root must be a json object");
        }
        JsonObject root = json.getAsJsonObject();
        if (!root.has("type")) {
            throw new JsonSyntaxException("Missing type entry");
        }
        if (!root.get("type").isJsonPrimitive() || !root.get("type").getAsJsonPrimitive().isString()) {
            throw new JsonSyntaxException("type entry must be a String");
        }
        ResourceLocation type = ResourceLocation.tryParse(root.getAsJsonPrimitive("type").getAsString());
        if (type == null) {
            throw new JsonSyntaxException("type entry must be a valid ResourceLocation");
        }
        boolean requiresUpgrade = false;
        if (root.has("requires_upgrade")) {
            JsonElement requiresUpgradeElement = root.get("requires_upgrade");
            if (requiresUpgradeElement.isJsonPrimitive() && requiresUpgradeElement.getAsJsonPrimitive().isBoolean()) {
                requiresUpgrade = requiresUpgradeElement.getAsBoolean();
            } else {
                throw new JsonSyntaxException("requires_upgrade must be a boolean.");
            }
        }

        if (type.toString().equals("expandedstorage:block_conversion")) {
            parseBlockRecipe(name, root, requiresUpgrade);
        } else if (type.toString().equals("expandedstorage:entity_conversion")) {
            parseEntityRecipe(name, root, requiresUpgrade);
        } else {
            throw new JsonSyntaxException("type must be either: \"expandedstorage:block_conversion\" or \"expandedstorage:entity_conversion\"");
        }
    }

    private void parseBlockRecipe(ResourceLocation name, JsonObject root, boolean requiresUpgrade) {

    }

    private void parseEntityRecipe(ResourceLocation name, JsonObject root, boolean requiresUpgrade) {

    }
}
