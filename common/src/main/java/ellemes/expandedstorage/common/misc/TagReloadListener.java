package ellemes.expandedstorage.common.misc;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class TagReloadListener {
    public static final TagKey<Block> chestCycle = TagKey.create(Registries.BLOCK, Utils.id("chest_cycle"));
    public static final TagKey<Block> miniChestCycle = TagKey.create(Registries.BLOCK, Utils.id("mini_chest_cycle"));
    public static final TagKey<Block> miniChestSecretCycle = TagKey.create(Registries.BLOCK, Utils.id("mini_chest_secret_cycle"));
    public static final TagKey<Block> miniChestSecretCycle2 = TagKey.create(Registries.BLOCK, Utils.id("mini_chest_secret_cycle_2"));
    public static final TagKey<EntityType<?>> minecartChestCycle = TagKey.create(Registries.ENTITY_TYPE, Utils.id("minecart_chest_cycle"));
    private List<Block> chestCycleBlocks = null;
    private List<Block> miniChestCycleBlocks = null;
    private List<Block> miniChestSecretCycleBlocks = null;
    private List<Block> miniChestSecretCycle2Blocks = null;
    private List<? extends  EntityType<?>> minecartChestCycleEntityTypes = null;

    public void postDataReload() { // todo: get level I think
        chestCycleBlocks = BuiltInRegistries.BLOCK.getOrCreateTag(chestCycle).stream().map(Holder::value).toList();
        miniChestCycleBlocks = BuiltInRegistries.BLOCK.getOrCreateTag(miniChestCycle).stream().map(Holder::value).toList();
        miniChestSecretCycleBlocks = BuiltInRegistries.BLOCK.getOrCreateTag(miniChestSecretCycle).stream().map(Holder::value).toList();
        miniChestSecretCycle2Blocks = BuiltInRegistries.BLOCK.getOrCreateTag(miniChestSecretCycle2).stream().map(Holder::value).toList();

        minecartChestCycleEntityTypes = BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(minecartChestCycle).stream().map(Holder::value).toList();
    }

    public List<Block> getChestCycleBlocks() {
        if (chestCycleBlocks == null) { // In case no reload has happened yet, any better way to do this?
            this.postDataReload();
        }
        return chestCycleBlocks;
    }

    public List<Block> getMiniChestCycleBlocks() {
        if (miniChestCycleBlocks == null) { // In case no reload has happened yet, any better way to do this?
            this.postDataReload();
        }
        return miniChestCycleBlocks;
    }

    public List<Block> getMiniChestSecretCycleBlocks() {
        if (miniChestSecretCycleBlocks == null) { // In case no reload has happened yet, any better way to do this?
            this.postDataReload();
        }
        return miniChestSecretCycleBlocks;
    }

    public List<Block> getMiniChestSecretCycle2Blocks() {
        if (miniChestSecretCycle2Blocks == null) { // In case no reload has happened yet, any better way to do this?
            this.postDataReload();
        }
        return miniChestSecretCycle2Blocks;
    }

    public List<? extends EntityType<?>> getMinecartChestCycleEntityTypes() {
        if (minecartChestCycleEntityTypes == null) { // In case no reload has happened yet, any better way to do this?
            this.postDataReload();
        }
        return minecartChestCycleEntityTypes;
    }
}
