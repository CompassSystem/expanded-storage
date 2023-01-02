package ellemes.container_library.thread.wrappers;

import ellemes.container_library.Utils;
import ellemes.container_library.inventory.ServerScreenHandlerFactory;
import ellemes.container_library.thread.ScreenHandlerFactoryAdapter;
import ellemes.container_library.wrappers.NetworkWrapper;
import io.github.flemmli97.flan.api.ClaimHandler;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;

public abstract class ThreadNetworkWrapper extends NetworkWrapper {
    public static final ResourceLocation CHANNEL_NAME = Utils.id("channel");
    private final boolean flanPresent;

    public ThreadNetworkWrapper(boolean flanPresent, boolean ftbChunksPresent) {
        super(ftbChunksPresent);
        this.flanPresent = flanPresent;
    }

    @Override
    protected void openScreenHandler(ServerPlayer player, Container inventory, ServerScreenHandlerFactory factory, Component title, ResourceLocation forcedScreenType) {
        player.openMenu(new ScreenHandlerFactoryAdapter(title, inventory, factory, forcedScreenType));
    }

    @Override
    public boolean canOpenInventory(ServerPlayer player, BlockPos pos) {
        if (flanPresent) {
            return ClaimHandler.getPermissionStorage(player.getLevel()).getForPermissionCheck(pos).canInteract(player, PermissionRegistry.OPENCONTAINER, pos, true) && super.canOpenInventory(player, pos);
        }
        return super.canOpenInventory(player, pos);
    }

    @Override
    protected boolean canOpenInventory(ServerPlayer player, Entity entity) {
        if (flanPresent) {
            BlockPos pos = entity.getOnPos();
            return ClaimHandler.getPermissionStorage(player.getLevel()).getForPermissionCheck(pos).canInteract(player, PermissionRegistry.OPENCONTAINER, pos, true) && super.canOpenInventory(player, pos);
        }
        return super.canOpenInventory(player, entity);
    }
}
