package compasses.expandedstorage.thread.compat.inventory_tabs;

import compasses.expandedstorage.common.block.AbstractChestBlock;
import ellemes.expandedstorage.api.EsChestType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExpandedChestTab extends ExpandedBlockTab<AbstractChestBlock> {
    public ExpandedChestTab(AbstractChestBlock block, BlockPos selfPos) {
        super(block, selfPos);
    }

    @Override
    public boolean shouldBeRemoved() {
        ClientLevel level = Minecraft.getInstance().level;
        BlockState state = level.getBlockState(blockPos);

        if (super.shouldBeRemoved()) {
            return true;
        } else {
            AbstractChestBlock chestBlock = (AbstractChestBlock) state.getBlock();
            return chestBlock.isAccessBlocked(level, blockPos) || state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE;
        }
    }
}
