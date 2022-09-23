package ellemes.expandedstorage.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface EntityUpgradeBehaviour {
    boolean tryUpgradeEntity(Entity entity, ResourceLocation from, ResourceLocation to);
}
