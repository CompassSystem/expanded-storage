package ellemes.expandedstorage.common.datagen.providers;

import ellemes.expandedstorage.common.registration.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BlockLootTableHelper {
    public static void registerLootTables(BiConsumer<Block, Function<Block, LootTable.Builder>> consumer, Function<Block, LootTable.Builder> lootTableBuilder) {
        consumer.accept(ModBlocks.WOOD_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.PUMPKIN_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.PRESENT, lootTableBuilder);
        consumer.accept(ModBlocks.BAMBOO_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.EXPOSED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.WEATHERED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.OXIDIZED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.WAXED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.WAXED_EXPOSED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.WAXED_WEATHERED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.WAXED_OXIDIZED_COPPER_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.IRON_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.GOLD_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.DIAMOND_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.OBSIDIAN_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.NETHERITE_CHEST, lootTableBuilder);

        consumer.accept(ModBlocks.OLD_WOOD_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.OLD_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.OLD_EXPOSED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.OLD_WEATHERED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.OLD_OXIDIZED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.WAXED_OLD_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.WAXED_OLD_EXPOSED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.WAXED_OLD_WEATHERED_COPPER_CHEST, lootTableBuilder);
//        consumer.accept(ModBlocks.WAXED_OLD_OXIDIZED_COPPER_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.OLD_IRON_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.OLD_GOLD_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.OLD_DIAMOND_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.OLD_OBSIDIAN_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.OLD_NETHERITE_CHEST, lootTableBuilder);

        consumer.accept(ModBlocks.COPPER_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.EXPOSED_COPPER_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.WEATHERED_COPPER_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.OXIDIZED_COPPER_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.WAXED_COPPER_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.WAXED_EXPOSED_COPPER_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.WAXED_WEATHERED_COPPER_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.WAXED_OXIDIZED_COPPER_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.IRON_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.GOLD_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.DIAMOND_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.OBSIDIAN_BARREL, lootTableBuilder);
        consumer.accept(ModBlocks.NETHERITE_BARREL, lootTableBuilder);

        consumer.accept(ModBlocks.VANILLA_WOOD_MINI_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.WOOD_MINI_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.PUMPKIN_MINI_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.RED_MINI_PRESENT, lootTableBuilder);
        consumer.accept(ModBlocks.WHITE_MINI_PRESENT, lootTableBuilder);
        consumer.accept(ModBlocks.CANDY_CANE_MINI_PRESENT, lootTableBuilder);
        consumer.accept(ModBlocks.GREEN_MINI_PRESENT, lootTableBuilder);
        consumer.accept(ModBlocks.LAVENDER_MINI_PRESENT, lootTableBuilder);
        consumer.accept(ModBlocks.PINK_AMETHYST_MINI_PRESENT, lootTableBuilder);
        consumer.accept(ModBlocks.IRON_MINI_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.GOLD_MINI_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.DIAMOND_MINI_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.OBSIDIAN_MINI_CHEST, lootTableBuilder);
        consumer.accept(ModBlocks.NETHERITE_MINI_CHEST, lootTableBuilder);

        consumer.accept(ModBlocks.VANILLA_WOOD_MINI_CHEST_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.WOOD_MINI_CHEST_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.PUMPKIN_MINI_CHEST_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.RED_MINI_PRESENT_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.WHITE_MINI_PRESENT_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.CANDY_CANE_MINI_PRESENT_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.GREEN_MINI_PRESENT_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.LAVENDER_MINI_PRESENT_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.PINK_AMETHYST_MINI_PRESENT_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.IRON_MINI_CHEST_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.GOLD_MINI_CHEST_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.DIAMOND_MINI_CHEST_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.OBSIDIAN_MINI_CHEST_WITH_SPARROW, lootTableBuilder);
        consumer.accept(ModBlocks.NETHERITE_MINI_CHEST_WITH_SPARROW, lootTableBuilder);
    }
}
