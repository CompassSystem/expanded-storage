package ellemes.expandedstorage.common.datagen.providers;

import com.google.gson.JsonElement;
import ellemes.expandedstorage.common.datagen.content.ModEntityTypes;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.recipe.block.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.block.PartialBlockState;
import ellemes.expandedstorage.common.recipe.conditions.IsRegistryObject;
import ellemes.expandedstorage.common.recipe.entity.EntityConversionRecipe;
import ellemes.expandedstorage.common.recipe.misc.RecipeTool;
import ellemes.expandedstorage.common.registration.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class ConversionRecipeProvider implements DataProvider {
    protected static final RecipeTool UNNAMED_MUTATOR = new RecipeTool.MutatorTool(null);
    private static final Logger LOGGER = LoggerFactory.getLogger("ConversionRecipeProvider");
    protected final DataGenerator.PathProvider pathProvider;
    private final HashMap<ResourceLocation, BlockConversionRecipe<?>> blockRecipes = new HashMap<>();
    private final HashMap<ResourceLocation, EntityConversionRecipe<?>> entityRecipes = new HashMap<>();

    public ConversionRecipeProvider(DataGenerator generator) {
        this.pathProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, "conversion_recipes");

        pathProvider.json(new ResourceLocation("expandedstorage", "block/wood_to_copper_chest"));
    }

    protected void registerBlockRecipe(ResourceLocation id, BlockConversionRecipe<?> recipe) {
        ResourceLocation realId = new ResourceLocation(id.getNamespace(), "block/" + id.getPath());
        if (blockRecipes.containsKey(realId)) {
            throw new IllegalStateException("Tried registering duplicate block recipe with id: " + realId);
        }
        blockRecipes.put(realId, recipe);
    }

    protected void registerEntityRecipe(ResourceLocation id, EntityConversionRecipe<?> recipe) {
        ResourceLocation realId = new ResourceLocation(id.getNamespace(), "entity/" + id.getPath());
        if (entityRecipes.containsKey(realId)) {
            throw new IllegalStateException("Tried registering duplicate entity recipe with id: " + realId);
        }
        entityRecipes.put(realId, recipe);
    }

    protected void simpleBlockThemeSwap(ResourceLocation id, Block from, Block to) {
        this.registerBlockRecipe(id,
                new BlockConversionRecipe<>(UNNAMED_MUTATOR, new PartialBlockState<>(to), List.of(
                        new IsRegistryObject(Registry.BLOCK, from.builtInRegistryHolder().key().location())
                ))
        );
    }

    protected void simpleEntityThemeSwap(ResourceLocation id, EntityType<?> from, EntityType<?> to) {
        this.registerEntityRecipe(id,
                new EntityConversionRecipe<>(UNNAMED_MUTATOR, to, List.of(
                    new IsRegistryObject(Registry.ENTITY_TYPE, from.builtInRegistryHolder().key().location())
                ))
        );
    }

    @Override
    public void run(CachedOutput cachedOutput) throws IOException {
        blockRecipes.clear();
        entityRecipes.clear();

        this.registerBlockRecipes();
        this.registerEntityRecipes();

        blockRecipes.forEach((id, recipe) -> {
            JsonElement json = recipe.toJson();
            Path path = pathProvider.json(id);
            try {
                DataProvider.saveStable(cachedOutput, json, path);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't save a block conversion recipe to {}", path, iOException);
            }
        });

        entityRecipes.forEach((id, recipe) -> {
            JsonElement json = recipe.toJson();
            Path path = pathProvider.json(id);
            try {
                DataProvider.saveStable(cachedOutput, json, path);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't save a entity conversion recipe to {}", path, iOException);
            }
        });
    }

    protected void registerBlockRecipes() {
        simpleBlockThemeSwap(Utils.id("wood_to_pumpkin_chest"), ModBlocks.WOOD_CHEST, ModBlocks.PUMPKIN_CHEST);
        simpleBlockThemeSwap(Utils.id("pumpkin_to_present_chest"), ModBlocks.PUMPKIN_CHEST, ModBlocks.PRESENT);
        simpleBlockThemeSwap(Utils.id("present_to_bamboo_chest"), ModBlocks.PRESENT, ModBlocks.BAMBOO_CHEST);
        simpleBlockThemeSwap(Utils.id("bamboo_to_moss_chest"), ModBlocks.BAMBOO_CHEST, ModBlocks.MOSS_CHEST);
        simpleBlockThemeSwap(Utils.id("moss_to_wood_chest"), ModBlocks.MOSS_CHEST, ModBlocks.WOOD_CHEST);
    }

    protected void registerEntityRecipes() {
        simpleEntityThemeSwap(Utils.id("vanilla_to_wood_chest"), EntityType.CHEST_MINECART, ModEntityTypes.WOOD_CHEST_MINECART);
        simpleEntityThemeSwap(Utils.id("wood_to_pumpkin_chest"), ModEntityTypes.WOOD_CHEST_MINECART, ModEntityTypes.PUMPKIN_CHEST_MINECART);
        simpleEntityThemeSwap(Utils.id("pumpkin_to_present_chest"), ModEntityTypes.PUMPKIN_CHEST_MINECART, ModEntityTypes.PRESENT_MINECART);
        simpleEntityThemeSwap(Utils.id("present_to_bamboo_chest"), ModEntityTypes.PRESENT_MINECART, ModEntityTypes.BAMBOO_CHEST_MINECART);
        simpleEntityThemeSwap(Utils.id("bamboo_to_moss_chest"), ModEntityTypes.BAMBOO_CHEST_MINECART, ModEntityTypes.MOSS_CHEST_MINECART);
        simpleEntityThemeSwap(Utils.id("moss_to_wood_chest"), ModEntityTypes.MOSS_CHEST_MINECART, ModEntityTypes.WOOD_CHEST_MINECART);
    }

    @Override
    public String getName() {
        return "Expanded Storage - Conversion Recipes";
    }
}
