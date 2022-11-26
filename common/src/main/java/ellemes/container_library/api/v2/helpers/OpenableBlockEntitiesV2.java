package ellemes.container_library.api.v2.helpers;

import ellemes.container_library.api.helpers.VariableInventory;
import ellemes.container_library.api.v2.OpenableBlockEntityV2;
import ellemes.container_library.api.v3.helpers.OpenableInventories;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

import java.util.Arrays;

/**
 * Helper to wrap blocks which consist of multiple inventories into one e.g. chests.
 *
// * @deprecated Replaced by {@link OpenableInventories}.
 */
//@Deprecated
public final class OpenableBlockEntitiesV2 implements OpenableBlockEntityV2 {
    private final OpenableBlockEntityV2[] parts;
    private final Container inventory;
    private final Component inventoryTitle;

    public OpenableBlockEntitiesV2(Component inventoryTitle, OpenableBlockEntityV2... parts) {
        this.parts = parts;
        this.inventory = VariableInventory.of(Arrays.stream(parts).map(OpenableBlockEntityV2::getInventory).toArray(Container[]::new));
        this.inventoryTitle = inventoryTitle;
    }

    @Override
    public boolean canBeUsedBy(ServerPlayer player) {
        for (OpenableBlockEntityV2 part : parts) {
            if (!part.canBeUsedBy(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Container getInventory() {
        return inventory;
    }

    @Override
    public Component getInventoryTitle() {
        return inventoryTitle;
    }
}
