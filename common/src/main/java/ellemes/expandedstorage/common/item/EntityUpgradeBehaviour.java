package ellemes.expandedstorage.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface EntityUpgradeBehaviour {
    boolean tryUpgradeEntity(Player player, InteractionHand hand, Entity entity, ResourceLocation from, ResourceLocation to);
}
