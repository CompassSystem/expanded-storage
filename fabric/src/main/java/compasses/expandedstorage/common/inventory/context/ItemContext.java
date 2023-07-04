package compasses.expandedstorage.common.inventory.context;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ItemContext extends BaseContext {
    private final ItemStack stack;

    // todo: expose slot id?
    public ItemContext(ServerLevel level, ServerPlayer player, ItemStack stack) {
        super(level, player);
        this.stack = stack;
    }

    public ItemStack getItemStack() {
        return stack;
    }
}
