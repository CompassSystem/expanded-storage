package ellemes.expandedstorage.forge.datagen.providers;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LootTableProvider extends net.minecraft.data.loot.LootTableProvider {
    public LootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(new SubProviderEntry(() -> new BlockLootProvider(Set.of(), FeatureFlagSet.of()), LootContextParamSets.BLOCK)));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationTracker) {

    }

//    @Override
//    public String getName() {
//        return "Expanded Storage - Loot Tables";
//    }
}
