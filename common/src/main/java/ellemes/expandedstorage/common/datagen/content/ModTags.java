package ellemes.expandedstorage.common.datagen.content;

import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    public static class Items {
        public static final TagKey<Item> ES_WOODEN_CHESTS = tag(Registry.ITEM_REGISTRY, Utils.id("wooden_chests"));
    }

    private static <T> TagKey<T> tag(ResourceKey<Registry<T>> registry, ResourceLocation id) {
        return TagKey.create(registry, id);
    }
}
