package ellemes.expandedstorage.common.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface EntityMutatorBehaviour {
    InteractionResult attempt(Level level, Entity entity, ItemStack stack);
}
