package ellemes.expandedstorage.thread.compat.inventory_tabs;

import ellemes.expandedstorage.api.EsChestType;
import ellemes.expandedstorage.common.block.AbstractChestBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Objects;


public class ExpandedDoubleChestTab extends ExpandedBlockTab<AbstractChestBlock> {
    final BlockPos otherPos;

    public ExpandedDoubleChestTab(AbstractChestBlock block, BlockPos selfPos, BlockPos otherPos) {
        super(block, selfPos);
        this.otherPos = otherPos;
    }

    @Override
    public boolean shouldBeRemoved() {
        ClientLevel level = Minecraft.getInstance().level;
        BlockState state = level.getBlockState(blockPos);

        if (singleShouldBeRemoved()) {
            return true;
        } else {
            AbstractChestBlock chestBlock = (AbstractChestBlock) state.getBlock();
            return state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == EsChestType.SINGLE ||
                    chestBlock.isAccessBlocked(level, blockPos) ||
                    chestBlock.isAccessBlocked(level, otherPos);
        }
    }

    @Override
    protected AABB getBlockBounds() {
        return new AABB(blockPos).minmax(new AABB(otherPos));
    }

    @Override
    protected boolean isOwnedPos(BlockPos pos) {
        return blockPos.equals(pos) || otherPos.equals(pos);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(this.otherPos, ((ExpandedDoubleChestTab) o).otherPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos, otherPos);
    }
}
