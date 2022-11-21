package ellemes.expandedstorage.common.datagen.content;

import ellemes.expandedstorage.common.entity.ChestMinecart;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class ModEntityTypes {
    public static final EntityType<ChestMinecart> WOOD_CHEST_MINECART = entityType(Utils.id("wood_chest_minecart"));
    public static final EntityType<ChestMinecart> PUMPKIN_CHEST_MINECART = entityType(Utils.id("pumpkin_chest_minecart"));
    public static final EntityType<ChestMinecart> PRESENT_MINECART = entityType(Utils.id("present_minecart"));
    public static final EntityType<ChestMinecart> BAMBOO_CHEST_MINECART = entityType(Utils.id("bamboo_chest_minecart"));

    private static <T extends Entity> EntityType<T> entityType(ResourceLocation id) {
        return (EntityType<T>) Registry.ENTITY_TYPE.get(id);
    }
}
