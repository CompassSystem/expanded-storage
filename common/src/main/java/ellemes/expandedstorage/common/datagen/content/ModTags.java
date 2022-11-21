package ellemes.expandedstorage.common.datagen.content;

import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static class Items {
        public static final TagKey<Item> ES_WOODEN_CHESTS = tag(Registry.ITEM_REGISTRY, Utils.id("wooden_chests"));
    }

    public static class Blocks {
        public static final TagKey<Block> CHEST_CYCLE = tag(Registry.BLOCK_REGISTRY, Utils.id("chest_cycle"));
        public static final TagKey<Block> MINI_CHEST_CYCLE = tag(Registry.BLOCK_REGISTRY, Utils.id("mini_chest_cycle"));
        public static final TagKey<Block> MINI_CHEST_SECRET_CYCLE = tag(Registry.BLOCK_REGISTRY, Utils.id("mini_chest_secret_cycle"));
        public static final TagKey<Block> MINI_CHEST_SECRET_CYCLE_2 = tag(Registry.BLOCK_REGISTRY, Utils.id("mini_chest_secret_cycle_2"));
    }

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> MINECART_CHEST_CYCLE = tag(Registry.ENTITY_TYPE_REGISTRY, Utils.id("minecart_chest_cycle"));
    }

    private static <T> TagKey<T> tag(ResourceKey<Registry<T>> registry, ResourceLocation id) {
        return TagKey.create(registry, id);
    }
}
