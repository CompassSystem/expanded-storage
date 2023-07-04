package compasses.expandedstorage.common.client;

import compasses.expandedstorage.common.client.function.ScreenSize;
import compasses.expandedstorage.common.client.gui.AbstractScreen;
import compasses.expandedstorage.common.inventory.handler.AbstractHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public interface ScreenConstructor<T extends AbstractScreen> {
    @NotNull
    T createScreen(AbstractHandler handler, Inventory playerInventory, Component title, ScreenSize screenSize);
}
