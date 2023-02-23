package ellemes.expandedstorage.common.datagen.content;

import ellemes.expandedstorage.common.entity.ChestMinecart;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

// todo: generate these classes?
public final class ModEntityTypes {
    public static final EntityType<ChestMinecart> WOOD_CHEST_MINECART = entityType(Utils.id("wood_chest_minecart"));
    public static final EntityType<ChestMinecart> PUMPKIN_CHEST_MINECART = entityType(Utils.id("pumpkin_chest_minecart"));
    public static final EntityType<ChestMinecart> PRESENT_MINECART = entityType(Utils.id("present_minecart"));
    public static final EntityType<ChestMinecart> BAMBOO_CHEST_MINECART = entityType(Utils.id("bamboo_chest_minecart"));
    public static final EntityType<ChestMinecart> MOSS_CHEST_MINECART = entityType(Utils.id("moss_chest_minecart"));
    public static final EntityType<ChestMinecart> IRON_CHEST_MINECART = entityType(Utils.id("iron_chest_minecart"));
    public static final EntityType<ChestMinecart> GOLD_CHEST_MINECART = entityType(Utils.id("gold_chest_minecart"));
    public static final EntityType<ChestMinecart> DIAMOND_CHEST_MINECART = entityType(Utils.id("diamond_chest_minecart"));
    public static final EntityType<ChestMinecart> OBSIDIAN_CHEST_MINECART = entityType(Utils.id("obsidian_chest_minecart"));
    public static final EntityType<ChestMinecart> NETHERITE_CHEST_MINECART = entityType(Utils.id("netherite_chest_minecart"));

    private static <T extends Entity> EntityType<T> entityType(ResourceLocation id) {
        return (EntityType<T>) Registry.ENTITY_TYPE.get(id);
    }
}
