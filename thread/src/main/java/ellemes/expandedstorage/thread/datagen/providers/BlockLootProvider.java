package ellemes.expandedstorage.thread.datagen.providers;

import ellemes.expandedstorage.common.datagen.providers.BlockLootTableHelper;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;

public final class BlockLootProvider extends FabricBlockLootTableProvider {
    public BlockLootProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate() {
        BlockLootTableHelper.registerLootTables(this::add, this::createNameableBlockEntityTable);
    }

    @Override
    public String getName() {
        return "Expanded Storage - Loot Tables";
    }

    @Override
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> resourceLocationBuilderBiConsumer) {

    }
}
