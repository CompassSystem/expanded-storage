package ellemes.expandedstorage.forge.datagen.providers;

import ellemes.expandedstorage.Utils;
import ellemes.expandedstorage.forge.datagen.content.ModBlocks;
import ellemes.expandedstorage.forge.datagen.content.ModItems;
import ellemes.expandedstorage.forge.datagen.content.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public final class TagProvider {
    public static final class Block extends BlockTagsProvider {
        public Block(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, Utils.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags() {
            this.tag(Tags.Blocks.CHESTS_WOODEN).add(ModBlocks.WOOD_CHEST);
            this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.IRON_BARREL)
                .add(ModBlocks.GOLD_BARREL)
                .add(ModBlocks.DIAMOND_BARREL)
                .add(ModBlocks.OBSIDIAN_BARREL)
                .add(ModBlocks.NETHERITE_BARREL)
                .add(ModBlocks.WOOD_CHEST)
                .add(ModBlocks.PUMPKIN_CHEST)
                .add(ModBlocks.PRESENT)
                .add(ModBlocks.OLD_WOOD_CHEST)
                .add(ModBlocks.VANILLA_WOOD_MINI_CHEST)
                .add(ModBlocks.WOOD_MINI_CHEST)
                .add(ModBlocks.PUMPKIN_MINI_CHEST)
                .add(ModBlocks.RED_MINI_PRESENT)
                .add(ModBlocks.WHITE_MINI_PRESENT)
                .add(ModBlocks.CANDY_CANE_MINI_PRESENT)
                .add(ModBlocks.GREEN_MINI_PRESENT)
                .add(ModBlocks.LAVENDER_MINI_PRESENT)
                .add(ModBlocks.PINK_AMETHYST_MINI_PRESENT)
                .add(ModBlocks.VANILLA_WOOD_MINI_CHEST_WITH_SPARROW)
                .add(ModBlocks.WOOD_MINI_CHEST_WITH_SPARROW)
                .add(ModBlocks.PUMPKIN_MINI_CHEST_WITH_SPARROW)
                .add(ModBlocks.RED_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.WHITE_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.CANDY_CANE_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.GREEN_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.LAVENDER_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.PINK_AMETHYST_MINI_PRESENT_WITH_SPARROW);
            this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.IRON_CHEST)
                .add(ModBlocks.GOLD_CHEST)
                .add(ModBlocks.DIAMOND_CHEST)
                .add(ModBlocks.OBSIDIAN_CHEST)
                .add(ModBlocks.NETHERITE_CHEST)
                .add(ModBlocks.OLD_IRON_CHEST)
                .add(ModBlocks.OLD_GOLD_CHEST)
                .add(ModBlocks.OLD_DIAMOND_CHEST)
                .add(ModBlocks.OLD_OBSIDIAN_CHEST)
                .add(ModBlocks.OLD_NETHERITE_CHEST);
            this.tag(BlockTags.GUARDED_BY_PIGLINS)
                .add(ModBlocks.IRON_BARREL)
                .add(ModBlocks.GOLD_BARREL)
                .add(ModBlocks.DIAMOND_BARREL)
                .add(ModBlocks.OBSIDIAN_BARREL)
                .add(ModBlocks.NETHERITE_BARREL)
                .add(ModBlocks.WOOD_CHEST)
                .add(ModBlocks.PUMPKIN_CHEST)
                .add(ModBlocks.PRESENT)
                .add(ModBlocks.IRON_CHEST)
                .add(ModBlocks.GOLD_CHEST)
                .add(ModBlocks.DIAMOND_CHEST)
                .add(ModBlocks.OBSIDIAN_CHEST)
                .add(ModBlocks.NETHERITE_CHEST)
                .add(ModBlocks.OLD_WOOD_CHEST)
                .add(ModBlocks.OLD_IRON_CHEST)
                .add(ModBlocks.OLD_GOLD_CHEST)
                .add(ModBlocks.OLD_DIAMOND_CHEST)
                .add(ModBlocks.OLD_OBSIDIAN_CHEST)
                .add(ModBlocks.OLD_NETHERITE_CHEST)
                .add(ModBlocks.VANILLA_WOOD_MINI_CHEST)
                .add(ModBlocks.WOOD_MINI_CHEST)
                .add(ModBlocks.PUMPKIN_MINI_CHEST)
                .add(ModBlocks.RED_MINI_PRESENT)
                .add(ModBlocks.WHITE_MINI_PRESENT)
                .add(ModBlocks.CANDY_CANE_MINI_PRESENT)
                .add(ModBlocks.GREEN_MINI_PRESENT)
                .add(ModBlocks.LAVENDER_MINI_PRESENT)
                .add(ModBlocks.PINK_AMETHYST_MINI_PRESENT)
                .add(ModBlocks.VANILLA_WOOD_MINI_CHEST_WITH_SPARROW)
                .add(ModBlocks.WOOD_MINI_CHEST_WITH_SPARROW)
                .add(ModBlocks.PUMPKIN_MINI_CHEST_WITH_SPARROW)
                .add(ModBlocks.RED_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.WHITE_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.CANDY_CANE_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.GREEN_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.LAVENDER_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.PINK_AMETHYST_MINI_PRESENT_WITH_SPARROW);
            this.tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.OBSIDIAN_BARREL)
                .add(ModBlocks.NETHERITE_BARREL)
                .add(ModBlocks.OBSIDIAN_CHEST)
                .add(ModBlocks.NETHERITE_CHEST)
                .add(ModBlocks.OLD_OBSIDIAN_CHEST)
                .add(ModBlocks.OLD_NETHERITE_CHEST);
            this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.DIAMOND_BARREL)
                .add(ModBlocks.DIAMOND_CHEST)
                .add(ModBlocks.OLD_DIAMOND_CHEST);
            this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.IRON_BARREL)
                .add(ModBlocks.GOLD_BARREL)
                .add(ModBlocks.IRON_CHEST)
                .add(ModBlocks.GOLD_CHEST)
                .add(ModBlocks.OLD_IRON_CHEST)
                .add(ModBlocks.OLD_GOLD_CHEST);
            this.tag(ModTags.Blocks.CHEST_CYCLE)
                .add(ModBlocks.WOOD_CHEST)
                .add(ModBlocks.PUMPKIN_CHEST)
                .add(ModBlocks.PRESENT);
            this.tag(ModTags.Blocks.MINI_CHEST_CYCLE)
                .add(ModBlocks.VANILLA_WOOD_MINI_CHEST)
                .add(ModBlocks.WOOD_MINI_CHEST)
                .add(ModBlocks.PUMPKIN_MINI_CHEST)
                .add(ModBlocks.RED_MINI_PRESENT)
                .add(ModBlocks.WHITE_MINI_PRESENT)
                .add(ModBlocks.CANDY_CANE_MINI_PRESENT)
                .add(ModBlocks.GREEN_MINI_PRESENT);
            this.tag(ModTags.Blocks.MINI_CHEST_SECRET_CYCLE)
                .add(ModBlocks.VANILLA_WOOD_MINI_CHEST)
                .add(ModBlocks.WOOD_MINI_CHEST)
                .add(ModBlocks.PUMPKIN_MINI_CHEST)
                .add(ModBlocks.RED_MINI_PRESENT)
                .add(ModBlocks.WHITE_MINI_PRESENT)
                .add(ModBlocks.CANDY_CANE_MINI_PRESENT)
                .add(ModBlocks.GREEN_MINI_PRESENT)
                .add(ModBlocks.LAVENDER_MINI_PRESENT)
                .add(ModBlocks.PINK_AMETHYST_MINI_PRESENT);
            this.tag(ModTags.Blocks.MINI_CHEST_SECRET_CYCLE_2)
                .add(ModBlocks.VANILLA_WOOD_MINI_CHEST)
                .add(ModBlocks.WOOD_MINI_CHEST)
                .add(ModBlocks.PUMPKIN_MINI_CHEST)
                .add(ModBlocks.RED_MINI_PRESENT)
                .add(ModBlocks.WHITE_MINI_PRESENT)
                .add(ModBlocks.CANDY_CANE_MINI_PRESENT)
                .add(ModBlocks.GREEN_MINI_PRESENT)
                .add(ModBlocks.LAVENDER_MINI_PRESENT)
                .add(ModBlocks.PINK_AMETHYST_MINI_PRESENT)
                .add(ModBlocks.VANILLA_WOOD_MINI_CHEST_WITH_SPARROW)
                .add(ModBlocks.WOOD_MINI_CHEST_WITH_SPARROW)
                .add(ModBlocks.PUMPKIN_MINI_CHEST_WITH_SPARROW)
                .add(ModBlocks.RED_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.WHITE_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.CANDY_CANE_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.GREEN_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.LAVENDER_MINI_PRESENT_WITH_SPARROW)
                .add(ModBlocks.PINK_AMETHYST_MINI_PRESENT_WITH_SPARROW);
        }

        @Override
        public String getName() {
            return "Expanded Storage - Block Tags";
        }
    }

    public static final class Item extends ItemTagsProvider {
        public Item(DataGenerator generator, BlockTagsProvider blockTagsProvider, ExistingFileHelper existingFileHelper) {
            super(generator, blockTagsProvider, Utils.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags() {
            this.tag(Tags.Items.CHESTS_WOODEN).add(ModItems.WOOD_CHEST);
            this.tag(ModTags.Items.ES_WOODEN_CHESTS)
                .addTag(Tags.Items.CHESTS_WOODEN)
                .add(ModItems.PUMPKIN_CHEST)
                .add(ModItems.PRESENT);
            this.tag(ItemTags.PIGLIN_LOVED)
                .add(ModItems.WOOD_TO_GOLD_CONVERSION_KIT)
                .add(ModItems.IRON_TO_GOLD_CONVERSION_KIT)
                .add(ModItems.GOLD_TO_DIAMOND_CONVERSION_KIT)
                .add(ModItems.GOLD_TO_OBSIDIAN_CONVERSION_KIT)
                .add(ModItems.GOLD_TO_NETHERITE_CONVERSION_KIT)
                .add(ModItems.GOLD_BARREL)
                .add(ModItems.GOLD_CHEST)
                .add(ModItems.OLD_GOLD_CHEST);
        }

        @Override
        public String getName() {
            return "Expanded Storage - Item Tags";
        }
    }
}