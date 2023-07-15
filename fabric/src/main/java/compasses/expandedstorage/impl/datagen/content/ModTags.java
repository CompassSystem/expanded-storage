package compasses.expandedstorage.impl.datagen.content;

import compasses.expandedstorage.impl.Utils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static class Blocks {
        public static final TagKey<Block> ES_WOODEN_CHESTS = tag(Registries.BLOCK, Utils.id("wooden_chests"));
        public static final TagKey<Block> COPPER_BARRELS = tag(Registries.BLOCK, Utils.id("copper_barrels"));
    }

    public static class Items {
        public static final TagKey<Item> ES_WOODEN_CHESTS = tag(Registries.ITEM, Utils.id("wooden_chests"));
    }

    public static class Entities {
        public static final TagKey<EntityType<?>> ES_WOODEN_CHEST_MINECARTS = tag(Registries.ENTITY_TYPE, Utils.id("wooden_chest_minecarts"));
    }

    private static <T> TagKey<T> tag(ResourceKey<Registry<T>> registry, ResourceLocation id) {
        return TagKey.create(registry, id);
    }
}
