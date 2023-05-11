package ellemes.expandedstorage.common.datagen.providers;

import ellemes.expandedstorage.common.datagen.content.ModEntityTypes;
import ellemes.expandedstorage.common.registration.ModItems;
import ellemes.expandedstorage.common.datagen.content.ModTags;
import ellemes.expandedstorage.common.registration.ModBlocks;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public class TagHelper {
    public static void registerBlockTags(Function<TagKey<Block>, TagsProvider.TagAppender<Block>> tagMaker) {
        tagMaker.apply(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.COPPER_BARREL)
                .add(ModBlocks.EXPOSED_COPPER_BARREL)
                .add(ModBlocks.WEATHERED_COPPER_BARREL)
                .add(ModBlocks.OXIDIZED_COPPER_BARREL)
                .add(ModBlocks.WAXED_COPPER_BARREL)
                .add(ModBlocks.WAXED_EXPOSED_COPPER_BARREL)
                .add(ModBlocks.WAXED_WEATHERED_COPPER_BARREL)
                .add(ModBlocks.WAXED_OXIDIZED_COPPER_BARREL)
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
                .add(ModBlocks.MINI_BARREL)
                .add(ModBlocks.COPPER_MINI_BARREL)
                .add(ModBlocks.EXPOSED_COPPER_MINI_BARREL)
                .add(ModBlocks.WEATHERED_COPPER_MINI_BARREL)
                .add(ModBlocks.OXIDIZED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_EXPOSED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_WEATHERED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_OXIDIZED_COPPER_MINI_BARREL)
                .add(ModBlocks.IRON_MINI_BARREL)
                .add(ModBlocks.GOLD_MINI_BARREL)
                .add(ModBlocks.DIAMOND_MINI_BARREL)
                .add(ModBlocks.OBSIDIAN_MINI_BARREL)
                .add(ModBlocks.NETHERITE_MINI_BARREL);
        tagMaker.apply(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.IRON_CHEST)
                .add(ModBlocks.GOLD_CHEST)
                .add(ModBlocks.DIAMOND_CHEST)
                .add(ModBlocks.OBSIDIAN_CHEST)
                .add(ModBlocks.NETHERITE_CHEST)
                .add(ModBlocks.OLD_IRON_CHEST)
                .add(ModBlocks.OLD_GOLD_CHEST)
                .add(ModBlocks.OLD_DIAMOND_CHEST)
                .add(ModBlocks.OLD_OBSIDIAN_CHEST)
                .add(ModBlocks.OLD_NETHERITE_CHEST)
                .add(ModBlocks.IRON_MINI_CHEST)
                .add(ModBlocks.GOLD_MINI_CHEST)
                .add(ModBlocks.DIAMOND_MINI_CHEST)
                .add(ModBlocks.OBSIDIAN_MINI_CHEST)
                .add(ModBlocks.NETHERITE_MINI_CHEST);
        tagMaker.apply(BlockTags.MINEABLE_WITH_HOE)
                .add(ModBlocks.MOSS_CHEST);
        tagMaker.apply(BlockTags.GUARDED_BY_PIGLINS)
                .add(ModBlocks.COPPER_BARREL)
                .add(ModBlocks.EXPOSED_COPPER_BARREL)
                .add(ModBlocks.WEATHERED_COPPER_BARREL)
                .add(ModBlocks.OXIDIZED_COPPER_BARREL)
                .add(ModBlocks.WAXED_COPPER_BARREL)
                .add(ModBlocks.WAXED_EXPOSED_COPPER_BARREL)
                .add(ModBlocks.WAXED_WEATHERED_COPPER_BARREL)
                .add(ModBlocks.WAXED_OXIDIZED_COPPER_BARREL)
                .add(ModBlocks.IRON_BARREL)
                .add(ModBlocks.GOLD_BARREL)
                .add(ModBlocks.DIAMOND_BARREL)
                .add(ModBlocks.OBSIDIAN_BARREL)
                .add(ModBlocks.NETHERITE_BARREL)
                .add(ModBlocks.WOOD_CHEST)
                .add(ModBlocks.PUMPKIN_CHEST)
                .add(ModBlocks.PRESENT)
                .add(ModBlocks.BAMBOO_CHEST)
                .add(ModBlocks.MOSS_CHEST)
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
                .add(ModBlocks.IRON_MINI_CHEST)
                .add(ModBlocks.GOLD_MINI_CHEST)
                .add(ModBlocks.DIAMOND_MINI_CHEST)
                .add(ModBlocks.OBSIDIAN_MINI_CHEST)
                .add(ModBlocks.NETHERITE_MINI_CHEST)
                .add(ModBlocks.MINI_BARREL)
                .add(ModBlocks.COPPER_MINI_BARREL)
                .add(ModBlocks.EXPOSED_COPPER_MINI_BARREL)
                .add(ModBlocks.WEATHERED_COPPER_MINI_BARREL)
                .add(ModBlocks.OXIDIZED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_EXPOSED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_WEATHERED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_OXIDIZED_COPPER_MINI_BARREL)
                .add(ModBlocks.IRON_MINI_BARREL)
                .add(ModBlocks.GOLD_MINI_BARREL)
                .add(ModBlocks.DIAMOND_MINI_BARREL)
                .add(ModBlocks.OBSIDIAN_MINI_BARREL)
                .add(ModBlocks.NETHERITE_MINI_BARREL);
        tagMaker.apply(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.OBSIDIAN_BARREL)
                .add(ModBlocks.NETHERITE_BARREL)
                .add(ModBlocks.OBSIDIAN_CHEST)
                .add(ModBlocks.NETHERITE_CHEST)
                .add(ModBlocks.OLD_OBSIDIAN_CHEST)
                .add(ModBlocks.OLD_NETHERITE_CHEST)
                .add(ModBlocks.OBSIDIAN_MINI_CHEST)
                .add(ModBlocks.NETHERITE_MINI_CHEST)
                .add(ModBlocks.OBSIDIAN_MINI_BARREL)
                .add(ModBlocks.NETHERITE_MINI_BARREL);
        tagMaker.apply(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.GOLD_BARREL)
                .add(ModBlocks.DIAMOND_BARREL)
                .add(ModBlocks.GOLD_CHEST)
                .add(ModBlocks.DIAMOND_CHEST)
                .add(ModBlocks.OLD_GOLD_CHEST)
                .add(ModBlocks.OLD_DIAMOND_CHEST)
                .add(ModBlocks.GOLD_MINI_CHEST)
                .add(ModBlocks.DIAMOND_MINI_CHEST)
                .add(ModBlocks.GOLD_MINI_BARREL)
                .add(ModBlocks.DIAMOND_MINI_BARREL);
        tagMaker.apply(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.COPPER_BARREL)
                .add(ModBlocks.EXPOSED_COPPER_BARREL)
                .add(ModBlocks.WEATHERED_COPPER_BARREL)
                .add(ModBlocks.OXIDIZED_COPPER_BARREL)
                .add(ModBlocks.WAXED_COPPER_BARREL)
                .add(ModBlocks.WAXED_EXPOSED_COPPER_BARREL)
                .add(ModBlocks.WAXED_WEATHERED_COPPER_BARREL)
                .add(ModBlocks.WAXED_OXIDIZED_COPPER_BARREL)
                .add(ModBlocks.IRON_BARREL)
                .add(ModBlocks.IRON_CHEST)
                .add(ModBlocks.OLD_IRON_CHEST)
                .add(ModBlocks.IRON_MINI_CHEST)
                .add(ModBlocks.COPPER_MINI_BARREL)
                .add(ModBlocks.EXPOSED_COPPER_MINI_BARREL)
                .add(ModBlocks.WEATHERED_COPPER_MINI_BARREL)
                .add(ModBlocks.OXIDIZED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_EXPOSED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_WEATHERED_COPPER_MINI_BARREL)
                .add(ModBlocks.WAXED_OXIDIZED_COPPER_MINI_BARREL)
                .add(ModBlocks.IRON_MINI_BARREL);
        tagMaker.apply(ModTags.Blocks.COPPER_BARRELS)
                .add(ModBlocks.COPPER_BARREL)
                .add(ModBlocks.EXPOSED_COPPER_BARREL)
                .add(ModBlocks.WEATHERED_COPPER_BARREL)
                .add(ModBlocks.OXIDIZED_COPPER_BARREL)
                .add(ModBlocks.WAXED_COPPER_BARREL)
                .add(ModBlocks.WAXED_EXPOSED_COPPER_BARREL)
                .add(ModBlocks.WAXED_WEATHERED_COPPER_BARREL)
                .add(ModBlocks.WAXED_OXIDIZED_COPPER_BARREL);
        tagMaker.apply(ModTags.Blocks.ES_WOODEN_CHESTS)
                .add(ModBlocks.PUMPKIN_CHEST)
                .add(ModBlocks.PRESENT)
                .add(ModBlocks.BAMBOO_CHEST)
                .add(ModBlocks.MOSS_CHEST);
//        tagMaker.apply(ModTags.Blocks.COPPER_CHESTS)
//                .add(ModBlocks.COPPER_CHEST)
//                .add(ModBlocks.EXPOSED_COPPER_CHEST)
//                .add(ModBlocks.WEATHERED_COPPER_CHEST)
//                .add(ModBlocks.OXIDIZED_COPPER_CHEST)
//                .add(ModBlocks.WAXED_COPPER_CHEST)
//                .add(ModBlocks.WAXED_EXPOSED_COPPER_CHEST)
//                .add(ModBlocks.WAXED_WEATHERED_COPPER_CHEST)
//                .add(ModBlocks.WAXED_OXIDIZED_COPPER_CHEST);
//        tagMaker.apply(ModTags.Blocks.OLD_COPPER_CHESTS)
//                .add(ModBlocks.OLD_COPPER_CHEST)
//                .add(ModBlocks.OLD_EXPOSED_COPPER_CHEST)
//                .add(ModBlocks.OLD_WEATHERED_COPPER_CHEST)
//                .add(ModBlocks.OLD_OXIDIZED_COPPER_CHEST)
//                .add(ModBlocks.WAXED_OLD_COPPER_CHEST)
//                .add(ModBlocks.WAXED_OLD_EXPOSED_COPPER_CHEST)
//                .add(ModBlocks.WAXED_OLD_WEATHERED_COPPER_CHEST)
//                .add(ModBlocks.WAXED_OLD_OXIDIZED_COPPER_CHEST);
    }

    public static void registerItemTags(Function<TagKey<Item>, TagsProvider.TagAppender<Item>> tagMaker) {
        tagMaker.apply(ModTags.Items.ES_WOODEN_CHESTS)
                .add(ModItems.PUMPKIN_CHEST)
                .add(ModItems.PRESENT)
                .add(ModItems.BAMBOO_CHEST)
                .add(ModItems.MOSS_CHEST);
        tagMaker.apply(ItemTags.PIGLIN_LOVED)
                .add(ModItems.WOOD_TO_GOLD_CONVERSION_KIT)
                .add(ModItems.COPPER_TO_GOLD_CONVERSION_KIT)
                .add(ModItems.IRON_TO_GOLD_CONVERSION_KIT)
                .add(ModItems.GOLD_TO_DIAMOND_CONVERSION_KIT)
                .add(ModItems.GOLD_TO_OBSIDIAN_CONVERSION_KIT)
                .add(ModItems.GOLD_TO_NETHERITE_CONVERSION_KIT)
                .add(ModItems.GOLD_BARREL)
                .add(ModItems.GOLD_CHEST)
                .add(ModItems.GOLD_CHEST_MINECART)
                .add(ModItems.OLD_GOLD_CHEST)
                .add(ModItems.GOLD_MINI_CHEST)
                .add(ModItems.GOLD_MINI_BARREL);
    }

    public static void registerEntityTypeTags(Function<TagKey<EntityType<?>>, TagsProvider.TagAppender<EntityType<?>>> tagMaker) {
        tagMaker.apply(ModTags.Entities.ES_WOODEN_CHEST_MINECARTS)
                .add(EntityType.CHEST_MINECART)
                .add(ModEntityTypes.WOOD_CHEST_MINECART)
                .add(ModEntityTypes.PUMPKIN_CHEST_MINECART)
                .add(ModEntityTypes.PRESENT_MINECART)
                .add(ModEntityTypes.BAMBOO_CHEST_MINECART)
                .add(ModEntityTypes.MOSS_CHEST_MINECART);
    }
}
