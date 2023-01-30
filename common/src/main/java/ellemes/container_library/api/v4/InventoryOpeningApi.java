package ellemes.container_library.api.v4;

import ellemes.container_library.CommonMain;
import ellemes.container_library.api.v3.OpenableInventoryProvider;
import ellemes.container_library.api.v3.context.BlockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class InventoryOpeningApi {
    private InventoryOpeningApi() {
        throw new IllegalStateException("InventoryOpeningApi should not be instantiated.");
    }

    public static void openBlockInventory(ServerPlayer player, BlockPos pos, OpenableInventoryProvider<BlockContext> inventory) {
        CommonMain.getNetworkWrapper().s_openInventory(player, inventory.getOpenableInventory(new BlockContext(player.getLevel(), player, pos)), inventory::onInitialOpen, inventory.getForcedScreenType());
    }
}
