package compasses.expandedstorage.impl.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

import java.util.Arrays;

public class OpenableInventories implements OpenableInventory {
    private final OpenableInventory[] parts;
    private final Container inventory;
    private final Component inventoryTitle;

    private OpenableInventories(Component inventoryTitle, OpenableInventory... parts) {
        this.parts = parts;
        this.inventory = VariableInventory.of(Arrays.stream(parts).map(OpenableInventory::getInventory).toArray(Container[]::new));
        this.inventoryTitle = inventoryTitle;
    }

    public static OpenableInventory of(Component inventoryTitle, OpenableInventory... parts) {
        return new OpenableInventories(inventoryTitle, parts);
    }

    @Override
    public boolean canBeUsedBy(ServerPlayer player) {
        for (OpenableInventory part : parts) {
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
