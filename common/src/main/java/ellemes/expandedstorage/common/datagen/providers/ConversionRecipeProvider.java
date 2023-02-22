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
            var isESWoodChest = List.of(new IsInTagCondition(ModTags.Blocks.ES_WOODEN_CHESTS));
//            var isCopperChest = List.of(new IsInTagCondition(ModTags.Blocks.COPPER_CHESTS));
            var isIronChest = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.IRON_CHEST.getBlockId()));
            var isGoldChest = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.GOLD_CHEST.getBlockId()));
            var isDiamondChest = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.DIAMOND_CHEST.getBlockId()));
            var isObsidianChest = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OBSIDIAN_CHEST.getBlockId()));
            var ironChest = new PartialBlockState<>(ModBlocks.IRON_CHEST);
            var goldChest = new PartialBlockState<>(ModBlocks.GOLD_CHEST);
            var diamondChest = new PartialBlockState<>(ModBlocks.DIAMOND_CHEST);
            var obsidianChest = new PartialBlockState<>(ModBlocks.OBSIDIAN_CHEST);
            var netheriteChest = new PartialBlockState<>(ModBlocks.NETHERITE_CHEST);
//            registerBlockRecipe(Utils.id("wood_to_copper_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_COPPER_CONVERSION_KIT, new PartialBlockState<>(ModBlocks.COPPER_CHEST), isWoodBarrel)
//            );
            registerBlockRecipe(Utils.id("wood_to_iron_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_IRON_CONVERSION_KIT, ironChest, isESWoodChest)
            );
            registerBlockRecipe(Utils.id("wood_to_gold_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_GOLD_CONVERSION_KIT, goldChest, isESWoodChest)
            );
            registerBlockRecipe(Utils.id("wood_to_diamond_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_DIAMOND_CONVERSION_KIT, diamondChest, isESWoodChest)
            );
            registerBlockRecipe(Utils.id("wood_to_obsidian_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isESWoodChest)
            );
            registerBlockRecipe(Utils.id("wood_to_netherite_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isESWoodChest)
            );
//            registerBlockRecipe(Utils.id("copper_to_iron_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_IRON_CONVERSION_KIT, ironChest, isCopperChest)
//            );
//            registerBlockRecipe(Utils.id("copper_to_gold_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_GOLD_CONVERSION_KIT, goldChest, isCopperChest)
//            );
//            registerBlockRecipe(Utils.id("copper_to_diamond_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_DIAMOND_CONVERSION_KIT, diamondChest, isCopperChest)
//            );
//            registerBlockRecipe(Utils.id("copper_to_obsidian_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isCopperChest)
//            );
//            registerBlockRecipe(Utils.id("copper_to_netherite_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isCopperChest)
//            );
            registerBlockRecipe(Utils.id("iron_to_gold_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_GOLD_CONVERSION_KIT, goldChest, isIronChest)
            );
            registerBlockRecipe(Utils.id("iron_to_diamond_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_DIAMOND_CONVERSION_KIT, diamondChest, isIronChest)
            );
            registerBlockRecipe(Utils.id("iron_to_obsidian_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isIronChest)
            );
            registerBlockRecipe(Utils.id("iron_to_netherite_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isIronChest)
            );
            registerBlockRecipe(Utils.id("gold_to_diamond_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_DIAMOND_CONVERSION_KIT, diamondChest, isGoldChest)
            );
            registerBlockRecipe(Utils.id("gold_to_obsidian_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isGoldChest)
            );
            registerBlockRecipe(Utils.id("gold_to_netherite_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isGoldChest)
            );
            registerBlockRecipe(Utils.id("diamond_to_obsidian_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isDiamondChest)
            );
            registerBlockRecipe(Utils.id("diamond_to_netherite_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isDiamondChest)
            );
            registerBlockRecipe(Utils.id("obsidian_to_netherite_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.OBSIDIAN_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isObsidianChest)
            );
        }

        // Old chest upgrade recipes
        {
            var isOldWoodChest = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OLD_WOOD_CHEST.getBlockId()));
//            var isOldCopperChest = List.of(new IsInTagCondition(ModTags.Blocks.OLD_COPPER_CHESTS));
            var isOldIronChest = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OLD_IRON_CHEST.getBlockId()));
            var isOldGoldChest = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OLD_GOLD_CHEST.getBlockId()));
            var isOldDiamondChest = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OLD_DIAMOND_CHEST.getBlockId()));
            var isOldObsidianChest = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OLD_OBSIDIAN_CHEST.getBlockId()));
//            var copperChest = new PartialBlockState<>(ModBlocks.OLD_COPPER_CHEST);
            var ironChest = new PartialBlockState<>(ModBlocks.OLD_IRON_CHEST);
            var goldChest = new PartialBlockState<>(ModBlocks.OLD_GOLD_CHEST);
            var diamondChest = new PartialBlockState<>(ModBlocks.OLD_DIAMOND_CHEST);
            var obsidianChest = new PartialBlockState<>(ModBlocks.OLD_OBSIDIAN_CHEST);
            var netheriteChest = new PartialBlockState<>(ModBlocks.OLD_NETHERITE_CHEST);
//            registerBlockRecipe(Utils.id("wood_to_copper_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_IRON_CONVERSION_KIT, copperChest, isOldWoodChest)
//            );
            registerBlockRecipe(Utils.id("wood_to_iron_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_IRON_CONVERSION_KIT, ironChest, isOldWoodChest)
            );
            registerBlockRecipe(Utils.id("wood_to_gold_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_GOLD_CONVERSION_KIT, goldChest, isOldWoodChest)
            );
            registerBlockRecipe(Utils.id("wood_to_diamond_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_DIAMOND_CONVERSION_KIT, diamondChest, isOldWoodChest)
            );
            registerBlockRecipe(Utils.id("wood_to_obsidian_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isOldWoodChest)
            );
            registerBlockRecipe(Utils.id("wood_to_netherite_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.WOOD_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isOldWoodChest)
            );
//            registerBlockRecipe(Utils.id("copper_to_iron_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_IRON_CONVERSION_KIT, ironChest, isOldCopperChest)
//            );
//            registerBlockRecipe(Utils.id("copper_to_gold_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_GOLD_CONVERSION_KIT, goldChest, isOldCopperChest)
//            );
//            registerBlockRecipe(Utils.id("copper_to_diamond_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_DIAMOND_CONVERSION_KIT, diamondChest, isOldCopperChest)
//            );
//            registerBlockRecipe(Utils.id("copper_to_obsidian_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isOldCopperChest)
//            );
//            registerBlockRecipe(Utils.id("copper_to_netherite_old_chest"),
//                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isOldWoodChest)
//            );
            registerBlockRecipe(Utils.id("iron_to_gold_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_GOLD_CONVERSION_KIT, goldChest, isOldIronChest)
            );
            registerBlockRecipe(Utils.id("iron_to_diamond_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_DIAMOND_CONVERSION_KIT, diamondChest, isOldIronChest)
            );
            registerBlockRecipe(Utils.id("iron_to_obsidian_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isOldIronChest)
            );
            registerBlockRecipe(Utils.id("iron_to_netherite_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isOldIronChest)
            );
            registerBlockRecipe(Utils.id("gold_to_diamond_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_DIAMOND_CONVERSION_KIT, diamondChest, isOldGoldChest)
            );
            registerBlockRecipe(Utils.id("gold_to_obsidian_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isOldGoldChest)
            );
            registerBlockRecipe(Utils.id("gold_to_netherite_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isOldGoldChest)
            );
            registerBlockRecipe(Utils.id("diamond_to_obsidian_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_OBSIDIAN_CONVERSION_KIT, obsidianChest, isOldDiamondChest)
            );
            registerBlockRecipe(Utils.id("diamond_to_netherite_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isOldDiamondChest)
            );
            registerBlockRecipe(Utils.id("obsidian_to_netherite_old_chest"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.OBSIDIAN_TO_NETHERITE_CONVERSION_KIT, netheriteChest, isOldObsidianChest)
            );
        }

        // Barrel upgrade recipes
        {
            var isCopperBarrel = List.of(new IsInTagCondition(ModTags.Blocks.COPPER_BARRELS));
            var isIronBarrel = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.IRON_BARREL.getBlockId()));
            var isGoldBarrel = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.GOLD_BARREL.getBlockId()));
            var isDiamondBarrel = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.DIAMOND_BARREL.getBlockId()));
            var isObsidianBarrel = List.of(new IsRegistryObject(Registry.BLOCK, ModBlocks.OBSIDIAN_BARREL.getBlockId()));
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
                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_IRON_CONVERSION_KIT, ironBarrel, isCopperBarrel)
            );
            registerBlockRecipe(Utils.id("copper_to_gold_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_GOLD_CONVERSION_KIT, goldBarrel, isCopperBarrel)
            );
            registerBlockRecipe(Utils.id("copper_to_diamond_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_DIAMOND_CONVERSION_KIT, diamondBarrel, isCopperBarrel)
            );
            registerBlockRecipe(Utils.id("copper_to_obsidian_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_OBSIDIAN_CONVERSION_KIT, obsidianBarrel, isCopperBarrel)
            );
            registerBlockRecipe(Utils.id("copper_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.COPPER_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isCopperBarrel)
            );
            registerBlockRecipe(Utils.id("iron_to_gold_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_GOLD_CONVERSION_KIT, goldBarrel, isIronBarrel)
            );
            registerBlockRecipe(Utils.id("iron_to_diamond_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_DIAMOND_CONVERSION_KIT, diamondBarrel, isIronBarrel)
            );
            registerBlockRecipe(Utils.id("iron_to_obsidian_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_OBSIDIAN_CONVERSION_KIT, obsidianBarrel, isIronBarrel)
            );
            registerBlockRecipe(Utils.id("iron_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.IRON_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isIronBarrel)
            );
            registerBlockRecipe(Utils.id("gold_to_diamond_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_DIAMOND_CONVERSION_KIT, diamondBarrel, isGoldBarrel)
            );
            registerBlockRecipe(Utils.id("gold_to_obsidian_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_OBSIDIAN_CONVERSION_KIT, obsidianBarrel, isGoldBarrel)
            );
            registerBlockRecipe(Utils.id("gold_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.GOLD_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isGoldBarrel)
            );
            registerBlockRecipe(Utils.id("diamond_to_obsidian_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_OBSIDIAN_CONVERSION_KIT, obsidianBarrel, isDiamondBarrel)
            );
            registerBlockRecipe(Utils.id("diamond_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.DIAMOND_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isDiamondBarrel)
            );
            registerBlockRecipe(Utils.id("obsidian_to_netherite_barrel"),
                    new BlockConversionRecipe<>(ConversionRecipeProvider.OBSIDIAN_TO_NETHERITE_CONVERSION_KIT, netheriteBarrel, isObsidianBarrel)
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
