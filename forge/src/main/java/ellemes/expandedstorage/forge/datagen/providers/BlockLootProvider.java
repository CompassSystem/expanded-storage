package ellemes.expandedstorage.forge.datagen.providers;

import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.datagen.providers.BlockLootTableHelper;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class BlockLootProvider extends BlockLootSubProvider {
    public BlockLootProvider(Set<Item> set, FeatureFlagSet arg) {
        super(set, arg);
    }

    @Override
    protected void generate() {
        BlockLootTableHelper.registerLootTables(this::add, this::createNameableBlockEntityTable);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ForgeRegistries.BLOCKS.getEntries().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(Utils.MOD_ID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }
}
