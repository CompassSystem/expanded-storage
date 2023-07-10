package compasses.expandedstorage.impl.mixin.common;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableChestBlock;
import compasses.expandedstorage.impl.block.AbstractChestBlock;
import compasses.expandedstorage.impl.compat.htm.HTMChestProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(AbstractChestBlock.class)
public abstract class HTMChestCompat implements LockableChestBlock {
    @Override
    public HTMContainerLock getLockAt(BlockState state, Level level, BlockPos pos) {
        //noinspection ConstantConditions
        return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock) (Object) this, state, level, pos, true).get(HTMChestProperties.LOCK_PROPERTY).orElse(null);
    }

    @Override
    public Optional<BlockEntity> getUnlockedPart(BlockState state, Level level, BlockPos pos) {
        //noinspection ConstantConditions
        return AbstractChestBlock.createPropertyRetriever((AbstractChestBlock) (Object) this, state, level, pos, true).get(HTMChestProperties.UNLOCKED_BE_PROPERTY);
    }
}
