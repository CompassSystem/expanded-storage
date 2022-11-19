package ellemes.expandedstorage.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockMutatorBehaviour {
    InteractionResult attempt(UseOnContext context, Level world, BlockState state, BlockPos pos, ItemStack stack);
}
