package ellemes.expandedstorage.common.client;

import ellemes.expandedstorage.common.client.function.ScreenSize;
import ellemes.expandedstorage.common.client.gui.AbstractScreen;
import ellemes.expandedstorage.common.inventory.AbstractHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public interface ScreenConstructor<T extends AbstractScreen> {
    @NotNull
    T createScreen(AbstractHandler handler, Inventory playerInventory, Component title, ScreenSize screenSize);
}
