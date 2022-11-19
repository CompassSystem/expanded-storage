package ellemes.expandedstorage.common.entity;

import com.google.common.collect.ImmutableSet;
import ellemes.expandedstorage.common.misc.TieredObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;

public class TieredEntityType<T extends Entity> extends EntityType<T> implements TieredObject {
    private final ResourceLocation objectType;
    private final ResourceLocation objectTier;

    public TieredEntityType(EntityFactory<T> factory, MobCategory category, boolean saveable, boolean summonable,
                            boolean fireImmune, boolean spawnableFarFromPlayer, ImmutableSet<Block> spawnBlocks,
                            EntityDimensions dimensions, int maxTrackDistance, int trackTickInterval,
                            ResourceLocation objectType, ResourceLocation objectTier) {
        super(factory, category, saveable, summonable, fireImmune, spawnableFarFromPlayer, spawnBlocks, dimensions,
                maxTrackDistance, trackTickInterval);
        this.objectType = objectType;
        this.objectTier = objectTier;
    }

    @Override
    public ResourceLocation getObjType() {
        return objectType;
    }

    @Override
    public ResourceLocation getObjTier() {
        return objectTier;
    }
}
