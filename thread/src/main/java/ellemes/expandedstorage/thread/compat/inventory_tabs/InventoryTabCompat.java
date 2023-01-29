package ellemes.expandedstorage.thread.compat.inventory_tabs;

import com.kqp.inventorytabs.api.TabProviderRegistry;
import com.kqp.inventorytabs.tabs.TabManager;
import ellemes.container_library.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class InventoryTabCompat {
    private static int tickCounter = 0;
    public static void register() {
        TabProviderRegistry.register(Utils.id("double_chest_tab"), new ExpandedChestTabProvider());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (tickCounter % 20 == 0) { // Every second
                TabManager.getInstance().tabs.forEach(tab -> {
                    if (tab instanceof ExpandedBlockTab<?> expandedTab) {
                        expandedTab.update();
                    }
                });
            }
            tickCounter++;
        });
    }
}
