package ellemes.expandedstorage.thread.datagen.providers;

import ellemes.expandedstorage.thread.datagen.content.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.data.loot.BlockLoot;

public final class BlockLootProvider extends FabricBlockLootTableProvider {
    public BlockLootProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateBlockLootTables() {
        this.add(ModBlocks.WOOD_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.PUMPKIN_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.PRESENT, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.IRON_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.GOLD_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.DIAMOND_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.OBSIDIAN_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.NETHERITE_CHEST, BlockLoot::createNameableBlockEntityTable);

        this.add(ModBlocks.OLD_WOOD_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.OLD_IRON_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.OLD_GOLD_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.OLD_DIAMOND_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.OLD_OBSIDIAN_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.OLD_NETHERITE_CHEST, BlockLoot::createNameableBlockEntityTable);

        this.add(ModBlocks.IRON_BARREL, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.GOLD_BARREL, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.DIAMOND_BARREL, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.OBSIDIAN_BARREL, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.NETHERITE_BARREL, BlockLoot::createNameableBlockEntityTable);

        this.add(ModBlocks.VANILLA_WOOD_MINI_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.WOOD_MINI_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.PUMPKIN_MINI_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.RED_MINI_PRESENT, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.WHITE_MINI_PRESENT, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.CANDY_CANE_MINI_PRESENT, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.GREEN_MINI_PRESENT, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.LAVENDER_MINI_PRESENT, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.PINK_AMETHYST_MINI_PRESENT, BlockLoot::createNameableBlockEntityTable);

        this.add(ModBlocks.VANILLA_WOOD_MINI_CHEST_WITH_SPARROW, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.WOOD_MINI_CHEST_WITH_SPARROW, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.PUMPKIN_MINI_CHEST_WITH_SPARROW, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.RED_MINI_PRESENT_WITH_SPARROW, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.WHITE_MINI_PRESENT_WITH_SPARROW, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.CANDY_CANE_MINI_PRESENT_WITH_SPARROW, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.GREEN_MINI_PRESENT_WITH_SPARROW, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.LAVENDER_MINI_PRESENT_WITH_SPARROW, BlockLoot::createNameableBlockEntityTable);
        this.add(ModBlocks.PINK_AMETHYST_MINI_PRESENT_WITH_SPARROW, BlockLoot::createNameableBlockEntityTable);
    }

    @Override
    public String getName() {
        return "Expanded Storage - Loot Tables";
    }
}