package ellemes.expandedstorage.thread.compat.inventory_tabs;

import com.kqp.inventorytabs.tabs.provider.BlockTabProvider;
import com.kqp.inventorytabs.tabs.tab.Tab;
import ellemes.expandedstorage.common.block.OpenableBlock;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpandedChestTabProvider extends BlockTabProvider {
    @Override
    public void addAvailableTabs(LocalPlayer player, List<Tab> tabs) {
        super.addAvailableTabs(player, tabs);

        tabs.removeIf(tab -> tab instanceof ExpandedBlockTab<?> && tab.shouldBeRemoved());

        List<ExpandedDoubleChestTab> doubleChestTabs = tabs.stream()
                                                           .filter(tab -> tab instanceof ExpandedDoubleChestTab)
                                                           .map(tab -> (ExpandedDoubleChestTab) tab)
                                                           .toList();

        Set<ExpandedDoubleChestTab> duplicateDoubleChestTabs = new HashSet<>();
        for (ExpandedDoubleChestTab tab : doubleChestTabs) {
            if (!duplicateDoubleChestTabs.contains(tab)) {
                if (tab.blockPos != tab.otherPos) {
                    duplicateDoubleChestTabs.add(new ExpandedDoubleChestTab(tab.block, tab.otherPos, tab.blockPos));
                }
            }
        }

        tabs.removeAll(duplicateDoubleChestTabs);
    }

    @Override
    public boolean matches(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof OpenableBlock;
    }

    @Override
    public Tab createTab(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof AbstractChestBlock chestBlock) {
            CursedChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
            if (type == CursedChestType.SINGLE) {
                return new ExpandedChestTab(chestBlock, pos);
            } else {
                BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                return new ExpandedDoubleChestTab(chestBlock, pos, otherPos);
            }
        } else if (state.getBlock() instanceof OpenableBlock openableBlock) {
            return new ExpandedBlockTab<>(openableBlock, pos);
        }

        throw new IllegalStateException("Tried creating a tab for a block that doesn't match.");
    }
}
