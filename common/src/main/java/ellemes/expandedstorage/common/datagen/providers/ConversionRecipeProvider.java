package ellemes.expandedstorage.common.datagen.providers;

import com.google.gson.JsonElement;
import ellemes.expandedstorage.common.datagen.content.ModEntityTypes;
import ellemes.expandedstorage.common.datagen.content.ModItems;
import ellemes.expandedstorage.common.datagen.content.ModTags;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.recipe.block.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.block.PartialBlockState;
import ellemes.expandedstorage.common.recipe.conditions.IsInTagCondition;
import ellemes.expandedstorage.common.recipe.conditions.IsRegistryObject;
import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
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

public abstract class ConversionRecipeProvider implements DataProvider {
    protected static final RecipeTool UNNAMED_MUTATOR = new RecipeTool.MutatorTool(null);
    protected static final RecipeTool SPARROW_MUTATOR = new RecipeTool.MutatorTool("sparrow");

    protected static final RecipeTool WOOD_TO_COPPER_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.WOOD_TO_COPPER_CONVERSION_KIT);
    protected static final RecipeTool WOOD_TO_IRON_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.WOOD_TO_IRON_CONVERSION_KIT);
    protected static final RecipeTool WOOD_TO_GOLD_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.WOOD_TO_GOLD_CONVERSION_KIT);
    protected static final RecipeTool WOOD_TO_DIAMOND_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.WOOD_TO_DIAMOND_CONVERSION_KIT);
    protected static final RecipeTool WOOD_TO_OBSIDIAN_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.WOOD_TO_OBSIDIAN_CONVERSION_KIT);
    protected static final RecipeTool WOOD_TO_NETHERITE_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.WOOD_TO_NETHERITE_CONVERSION_KIT);

    protected static final RecipeTool COPPER_TO_IRON_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.COPPER_TO_IRON_CONVERSION_KIT);
    protected static final RecipeTool COPPER_TO_GOLD_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.COPPER_TO_GOLD_CONVERSION_KIT);
    protected static final RecipeTool COPPER_TO_DIAMOND_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.COPPER_TO_DIAMOND_CONVERSION_KIT);
    protected static final RecipeTool COPPER_TO_OBSIDIAN_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.COPPER_TO_OBSIDIAN_CONVERSION_KIT);
    protected static final RecipeTool COPPER_TO_NETHERITE_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.COPPER_TO_NETHERITE_CONVERSION_KIT);

    protected static final RecipeTool IRON_TO_GOLD_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.IRON_TO_GOLD_CONVERSION_KIT);
    protected static final RecipeTool IRON_TO_DIAMOND_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.IRON_TO_DIAMOND_CONVERSION_KIT);
    protected static final RecipeTool IRON_TO_OBSIDIAN_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.IRON_TO_OBSIDIAN_CONVERSION_KIT);
    protected static final RecipeTool IRON_TO_NETHERITE_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.IRON_TO_NETHERITE_CONVERSION_KIT);

    protected static final RecipeTool GOLD_TO_DIAMOND_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.GOLD_TO_DIAMOND_CONVERSION_KIT);
    protected static final RecipeTool GOLD_TO_OBSIDIAN_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.GOLD_TO_OBSIDIAN_CONVERSION_KIT);
    protected static final RecipeTool GOLD_TO_NETHERITE_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.GOLD_TO_NETHERITE_CONVERSION_KIT);

    protected static final RecipeTool DIAMOND_TO_OBSIDIAN_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.DIAMOND_TO_OBSIDIAN_CONVERSION_KIT);
    protected static final RecipeTool DIAMOND_TO_NETHERITE_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.DIAMOND_TO_NETHERITE_CONVERSION_KIT);

    protected static final RecipeTool OBSIDIAN_TO_NETHERITE_CONVERSION_KIT = new RecipeTool.UpgradeTool(ModItems.OBSIDIAN_TO_NETHERITE_CONVERSION_KIT);

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

    protected abstract void registerBlockRecipes();

    protected void registerBlockRecipes(List<RecipeCondition> isWoodBarrel, List<RecipeCondition> isWoodChest) {
        // Chest upgrade recipes
        {
            var isWoodTier = List.of(new IsInTagCondition(ModTags.Blocks.ES_WOODEN_CHESTS));
//            var isCopperTier = List.of(new IsInTagCondition(ModTags.Blocks.COPPER_CHESTS));
            var isIronTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.IRON_CHEST.getBlockId()));
            var isGoldTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.GOLD_CHEST.getBlockId()));
            var isDiamondTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.DIAMOND_CHEST.getBlockId()));
            var isObsidianTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OBSIDIAN_CHEST.getBlockId()));
            var ironChest = new PartialBlockState<>(ModBlocks.IRON_CHEST);
            var goldChest = new PartialBlockState<>(ModBlocks.GOLD_CHEST);
            var diamondChest = new PartialBlockState<>(ModBlocks.DIAMOND_CHEST);
            var obsidianChest = new PartialBlockState<>(ModBlocks.OBSIDIAN_CHEST);
            var netheriteChest = new PartialBlockState<>(ModBlocks.NETHERITE_CHEST);
//            registerBlockRecipe(Utils.id("wood_to_copper_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_COPPER_CONVERSION_KIT, new PartialBlockState<>(ModBlocks.COPPER_CHEST), isWoodBarrel)
//            );
            registerBlockRecipe(Utils.id("wood_to_iron_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_IRON_CONVERSION_KIT, ironChest, isWoodTier)
            );
            registerBlockRecipe(Utils.id("wood_to_gold_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_GOLD_CONVERSION_KIT, goldChest, isWoodTier)
            );
            registerBlockRecipe(Utils.id("wood_to_diamond_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_DIAMOND_CONVERSION_KIT, diamondChest, isWoodTier)
            );
            registerBlockRecipe(Utils.id("wood_to_obsidian_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isWoodTier)
            );
            registerBlockRecipe(Utils.id("wood_to_netherite_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isWoodTier)
            );
//            registerBlockRecipe(Utils.id("copper_to_iron_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_IRON_CONVERSION_KIT, ironChest, isCopperTier)
//            );
//            registerBlockRecipe(Utils.id("copper_to_gold_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_GOLD_CONVERSION_KIT, goldChest, isCopperTier)
//            );
//            registerBlockRecipe(Utils.id("copper_to_diamond_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_DIAMOND_CONVERSION_KIT, diamondChest, isCopperTier)
//            );
//            registerBlockRecipe(Utils.id("copper_to_obsidian_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isCopperTier)
//            );
//            registerBlockRecipe(Utils.id("copper_to_netherite_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isCopperTier)
//            );
            registerBlockRecipe(Utils.id("iron_to_gold_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_GOLD_CONVERSION_KIT, goldChest, isIronTier)
            );
            registerBlockRecipe(Utils.id("iron_to_diamond_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_DIAMOND_CONVERSION_KIT, diamondChest, isIronTier)
            );
            registerBlockRecipe(Utils.id("iron_to_obsidian_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isIronTier)
            );
            registerBlockRecipe(Utils.id("iron_to_netherite_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isIronTier)
            );
            registerBlockRecipe(Utils.id("gold_to_diamond_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_DIAMOND_CONVERSION_KIT, diamondChest, isGoldTier)
            );
            registerBlockRecipe(Utils.id("gold_to_obsidian_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isGoldTier)
            );
            registerBlockRecipe(Utils.id("gold_to_netherite_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isGoldTier)
            );
            registerBlockRecipe(Utils.id("diamond_to_obsidian_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isDiamondTier)
            );
            registerBlockRecipe(Utils.id("diamond_to_netherite_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isDiamondTier)
            );
            registerBlockRecipe(Utils.id("obsidian_to_netherite_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.OBSIDIAN_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isObsidianTier)
            );
        }

        // Old chest upgrade recipes
        {
            var isWoodTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OLD_WOOD_CHEST.getBlockId()));
//            var isCopperTier = List.of(new IsInTagCondition(ModTags.Blocks.OLD_COPPER_CHESTS));
            var isIronTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OLD_IRON_CHEST.getBlockId()));
            var isGoldTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OLD_GOLD_CHEST.getBlockId()));
            var isDiamondTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OLD_DIAMOND_CHEST.getBlockId()));
            var isObsidianTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OLD_OBSIDIAN_CHEST.getBlockId()));
//            var copperChest = new PartialBlockState<>(ModBlocks.OLD_COPPER_CHEST);
            var ironChest = new PartialBlockState<>(ModBlocks.OLD_IRON_CHEST);
            var goldChest = new PartialBlockState<>(ModBlocks.OLD_GOLD_CHEST);
            var diamondChest = new PartialBlockState<>(ModBlocks.OLD_DIAMOND_CHEST);
            var obsidianChest = new PartialBlockState<>(ModBlocks.OLD_OBSIDIAN_CHEST);
            var netheriteChest = new PartialBlockState<>(ModBlocks.OLD_NETHERITE_CHEST);
//            registerBlockRecipe(Utils.id("wood_to_copper_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_IRON_CONVERSION_KIT, copperChest, isWoodTier)
//            );
            registerBlockRecipe(Utils.id("wood_to_iron_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_IRON_CONVERSION_KIT, ironChest, isWoodTier)
            );
            registerBlockRecipe(Utils.id("wood_to_gold_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_GOLD_CONVERSION_KIT, goldChest, isWoodTier)
            );
            registerBlockRecipe(Utils.id("wood_to_diamond_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_DIAMOND_CONVERSION_KIT, diamondChest, isWoodTier)
            );
            registerBlockRecipe(Utils.id("wood_to_obsidian_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isWoodTier)
            );
            registerBlockRecipe(Utils.id("wood_to_netherite_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isWoodTier)
            );
//            registerBlockRecipe(Utils.id("copper_to_iron_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_IRON_CONVERSION_KIT, ironChest, isCopperTier)
//            );
//            registerBlockRecipe(Utils.id("copper_to_gold_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_GOLD_CONVERSION_KIT, goldChest, isCopperTier)
//            );
//            registerBlockRecipe(Utils.id("copper_to_diamond_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_DIAMOND_CONVERSION_KIT, diamondChest, isCopperTier)
//            );
//            registerBlockRecipe(Utils.id("copper_to_obsidian_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isCopperTier)
//            );
//            registerBlockRecipe(Utils.id("copper_to_netherite_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isCopperTier)
//            );
            registerBlockRecipe(Utils.id("iron_to_gold_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_GOLD_CONVERSION_KIT, goldChest, isIronTier)
            );
            registerBlockRecipe(Utils.id("iron_to_diamond_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_DIAMOND_CONVERSION_KIT, diamondChest, isIronTier)
            );
            registerBlockRecipe(Utils.id("iron_to_obsidian_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isIronTier)
            );
            registerBlockRecipe(Utils.id("iron_to_netherite_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isIronTier)
            );
            registerBlockRecipe(Utils.id("gold_to_diamond_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_DIAMOND_CONVERSION_KIT, diamondChest, isGoldTier)
            );
            registerBlockRecipe(Utils.id("gold_to_obsidian_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isGoldTier)
            );
            registerBlockRecipe(Utils.id("gold_to_netherite_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isGoldTier)
            );
            registerBlockRecipe(Utils.id("diamond_to_obsidian_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isDiamondTier)
            );
            registerBlockRecipe(Utils.id("diamond_to_netherite_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isDiamondTier)
            );
            registerBlockRecipe(Utils.id("obsidian_to_netherite_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.OBSIDIAN_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isObsidianTier)
            );
        }

        // Barrel upgrade recipes
        {
            var isCopperTier = List.of(new IsInTagCondition(ModTags.Blocks.COPPER_BARRELS));
            var isIronTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.IRON_BARREL.getBlockId()));
            var isGoldTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.GOLD_BARREL.getBlockId()));
            var isDiamondTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.DIAMOND_BARREL.getBlockId()));
            var isObsidianTier = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OBSIDIAN_BARREL.getBlockId()));
            var ironBarrel = new PartialBlockState<>(ModBlocks.IRON_BARREL);
            var goldBarrel = new PartialBlockState<>(ModBlocks.GOLD_BARREL);
            var diamondBarrel = new PartialBlockState<>(ModBlocks.DIAMOND_BARREL);
            var obsidianBarrel = new PartialBlockState<>(ModBlocks.OBSIDIAN_BARREL);
            var netheriteBarrel = new PartialBlockState<>(ModBlocks.NETHERITE_BARREL);
            registerBlockRecipe(Utils.id("wood_to_copper_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_COPPER_CONVERSION_KIT, new PartialBlockState<>(ModBlocks.COPPER_BARREL), isWoodBarrel)
            );
            registerBlockRecipe(Utils.id("wood_to_iron_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_IRON_CONVERSION_KIT, ironBarrel, isWoodBarrel)
            );
            registerBlockRecipe(Utils.id("wood_to_gold_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_GOLD_CONVERSION_KIT, goldBarrel, isWoodBarrel)
            );
            registerBlockRecipe(Utils.id("wood_to_diamond_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_DIAMOND_CONVERSION_KIT, diamondBarrel, isWoodBarrel)
            );
            registerBlockRecipe(Utils.id("wood_to_obsidian_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_OBSIDIAN_CONVERSION_KIT, obsidianBarrel, isWoodBarrel)
            );
            registerBlockRecipe(Utils.id("wood_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isWoodBarrel)
            );
            registerBlockRecipe(Utils.id("copper_to_iron_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_IRON_CONVERSION_KIT, ironBarrel, isCopperTier)
            );
            registerBlockRecipe(Utils.id("copper_to_gold_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_GOLD_CONVERSION_KIT, goldBarrel, isCopperTier)
            );
            registerBlockRecipe(Utils.id("copper_to_diamond_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_DIAMOND_CONVERSION_KIT, diamondBarrel, isCopperTier)
            );
            registerBlockRecipe(Utils.id("copper_to_obsidian_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_OBSIDIAN_CONVERSION_KIT, obsidianBarrel, isCopperTier)
            );
            registerBlockRecipe(Utils.id("copper_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isCopperTier)
            );
            registerBlockRecipe(Utils.id("iron_to_gold_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_GOLD_CONVERSION_KIT, goldBarrel, isIronTier)
            );
            registerBlockRecipe(Utils.id("iron_to_diamond_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_DIAMOND_CONVERSION_KIT, diamondBarrel, isIronTier)
            );
            registerBlockRecipe(Utils.id("iron_to_obsidian_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_OBSIDIAN_CONVERSION_KIT, obsidianBarrel, isIronTier)
            );
            registerBlockRecipe(Utils.id("iron_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isIronTier)
            );
            registerBlockRecipe(Utils.id("gold_to_diamond_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_DIAMOND_CONVERSION_KIT, diamondBarrel, isGoldTier)
            );
            registerBlockRecipe(Utils.id("gold_to_obsidian_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_OBSIDIAN_CONVERSION_KIT, obsidianBarrel, isGoldTier)
            );
            registerBlockRecipe(Utils.id("gold_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isGoldTier)
            );
            registerBlockRecipe(Utils.id("diamond_to_obsidian_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_OBSIDIAN_CONVERSION_KIT, obsidianBarrel, isDiamondTier)
            );
            registerBlockRecipe(Utils.id("diamond_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isDiamondTier)
            );
            registerBlockRecipe(Utils.id("obsidian_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.OBSIDIAN_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isObsidianTier)
            );
        }

        // Chest theme swap recipes
        {
            registerBlockRecipe(Utils.id("vanilla_to_wood_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.UNNAMED_MUTATOR, new PartialBlockState<>(ModBlocks.WOOD_CHEST), isWoodChest)
            );

            simpleBlockThemeSwap(Utils.id("wood_to_pumpkin_chest"), ModBlocks.WOOD_CHEST, ModBlocks.PUMPKIN_CHEST);
            simpleBlockThemeSwap(Utils.id("pumpkin_to_present_chest"), ModBlocks.PUMPKIN_CHEST, ModBlocks.PRESENT);
            simpleBlockThemeSwap(Utils.id("present_to_bamboo_chest"), ModBlocks.PRESENT, ModBlocks.BAMBOO_CHEST);
            simpleBlockThemeSwap(Utils.id("bamboo_to_moss_chest"), ModBlocks.BAMBOO_CHEST, ModBlocks.MOSS_CHEST);
            simpleBlockThemeSwap(Utils.id("moss_to_wood_chest"), ModBlocks.MOSS_CHEST, ModBlocks.WOOD_CHEST);
        }

        // Mini storage theme swap recipes
        {

        }

        // Do we want to have sparrow ones cycle like normal -> sparrow -> next normal -> next sparrow -> ...

        // mini chest theme swaps
        // mini chest -> sparrow theme swaps
    }

    protected abstract void registerEntityRecipes();

    protected void registerEntityRecipes(List<RecipeCondition> isWoodenMinecart) {
        {
            var isWoodTier = List.of(new IsInTagCondition(ModTags.Entities.ES_WOODEN_CHEST_MINECARTS));
            var isIronTier = List.of(new IsRegistryObject(Registry.ENTITY_TYPE, ModEntityTypes.IRON_CHEST_MINECART.builtInRegistryHolder().key().location()));
            var isGoldTier = List.of(new IsRegistryObject(Registry.ENTITY_TYPE, ModEntityTypes.GOLD_CHEST_MINECART.builtInRegistryHolder().key().location()));
            var isDiamondTier = List.of(new IsRegistryObject(Registry.ENTITY_TYPE, ModEntityTypes.DIAMOND_CHEST_MINECART.builtInRegistryHolder().key().location()));
            var isObsidianTier = List.of(new IsRegistryObject(Registry.ENTITY_TYPE, ModEntityTypes.OBSIDIAN_CHEST_MINECART.builtInRegistryHolder().key().location()));

            registerEntityRecipe(Utils.id("wood_to_iron_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_IRON_CONVERSION_KIT, ModEntityTypes.IRON_CHEST_MINECART, isWoodTier)
            );

            registerEntityRecipe(Utils.id("wood_to_gold_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_GOLD_CONVERSION_KIT, ModEntityTypes.GOLD_CHEST_MINECART, isWoodTier)
            );

            registerEntityRecipe(Utils.id("wood_to_diamond_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_DIAMOND_CONVERSION_KIT, ModEntityTypes.DIAMOND_CHEST_MINECART, isWoodTier)
            );

            registerEntityRecipe(Utils.id("wood_to_obsidian_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_OBSIDIAN_CONVERSION_KIT, ModEntityTypes.OBSIDIAN_CHEST_MINECART, isWoodTier)
            );

            registerEntityRecipe(Utils.id("wood_to_netherite_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_NETHERITE_CONVERSION_KIT, ModEntityTypes.NETHERITE_CHEST_MINECART, isWoodTier)
            );

            registerEntityRecipe(Utils.id("iron_to_gold_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.IRON_TO_GOLD_CONVERSION_KIT, ModEntityTypes.GOLD_CHEST_MINECART, isIronTier)
            );

            registerEntityRecipe(Utils.id("iron_to_diamond_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.IRON_TO_DIAMOND_CONVERSION_KIT, ModEntityTypes.DIAMOND_CHEST_MINECART, isIronTier)
            );

            registerEntityRecipe(Utils.id("iron_to_obsidian_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.IRON_TO_OBSIDIAN_CONVERSION_KIT, ModEntityTypes.OBSIDIAN_CHEST_MINECART, isIronTier)
            );

            registerEntityRecipe(Utils.id("iron_to_netherite_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.IRON_TO_NETHERITE_CONVERSION_KIT, ModEntityTypes.NETHERITE_CHEST_MINECART, isIronTier)
            );

            registerEntityRecipe(Utils.id("gold_to_diamond_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_DIAMOND_CONVERSION_KIT, ModEntityTypes.DIAMOND_CHEST_MINECART, isGoldTier)
            );

            registerEntityRecipe(Utils.id("gold_to_obsidian_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_OBSIDIAN_CONVERSION_KIT, ModEntityTypes.OBSIDIAN_CHEST_MINECART, isGoldTier)
            );

            registerEntityRecipe(Utils.id("gold_to_netherite_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_NETHERITE_CONVERSION_KIT, ModEntityTypes.NETHERITE_CHEST_MINECART, isGoldTier)
            );

            registerEntityRecipe(Utils.id("diamond_to_obsidian_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_OBSIDIAN_CONVERSION_KIT, ModEntityTypes.OBSIDIAN_CHEST_MINECART, isDiamondTier)
            );

            registerEntityRecipe(Utils.id("diamond_to_netherite_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_NETHERITE_CONVERSION_KIT, ModEntityTypes.NETHERITE_CHEST_MINECART, isDiamondTier)
            );

            registerEntityRecipe(Utils.id("obsidian_to_netherite_chest_minecart"),
                    new EntityConversionRecipe<>(ConversionRecipeProvider.OBSIDIAN_TO_NETHERITE_CONVERSION_KIT, ModEntityTypes.NETHERITE_CHEST_MINECART, isObsidianTier)
            );
        }

        registerEntityRecipe(Utils.id("vanilla_to_wood_chest_minecart"),
                new EntityConversionRecipe<>(UNNAMED_MUTATOR, ModEntityTypes.WOOD_CHEST_MINECART, isWoodenMinecart)
        );

        simpleEntityThemeSwap(Utils.id("wood_to_pumpkin_chest_minecart"), ModEntityTypes.WOOD_CHEST_MINECART, ModEntityTypes.PUMPKIN_CHEST_MINECART);
        simpleEntityThemeSwap(Utils.id("pumpkin_to_present_chest_minecart"), ModEntityTypes.PUMPKIN_CHEST_MINECART, ModEntityTypes.PRESENT_MINECART);
        simpleEntityThemeSwap(Utils.id("present_to_bamboo_chest_minecart"), ModEntityTypes.PRESENT_MINECART, ModEntityTypes.BAMBOO_CHEST_MINECART);
        simpleEntityThemeSwap(Utils.id("bamboo_to_moss_chest_minecart"), ModEntityTypes.BAMBOO_CHEST_MINECART, ModEntityTypes.MOSS_CHEST_MINECART);
        simpleEntityThemeSwap(Utils.id("moss_to_wood_chest_minecart"), ModEntityTypes.MOSS_CHEST_MINECART, ModEntityTypes.WOOD_CHEST_MINECART);
    }

    @Override
    public String getName() {
        return "Expanded Storage - Conversion Recipes";
    }
}
