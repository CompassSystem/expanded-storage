package compasses.expandedstorage.common.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import compasses.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.api.client.function.ScreenSize;
import ellemes.expandedstorage.api.client.gui.AbstractScreen;
import ellemes.expandedstorage.api.inventory.AbstractHandler;
import ellemes.expandedstorage.api.v3.client.ScreenTypeApi;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class MiniStorageScreen extends AbstractScreen {
    public MiniStorageScreen(AbstractHandler handler, Inventory playerInventory, Component title, ScreenSize screenSize) {
        super(handler, playerInventory, title, screenSize);
        this.initializeSlots(playerInventory);
    }

    public static ScreenSize retrieveScreenSize(int slots, int scaledWidth, int scaledHeight) {
        return ScreenSize.of(1, 1);
    }

    public static void registerScreenType() {
        ScreenTypeApi.registerScreenType(Utils.id("mini_chest"), MiniStorageScreen::new);
        ScreenTypeApi.registerDefaultScreenSize(Utils.id("mini_chest"), MiniStorageScreen::retrieveScreenSize);
    }

    private void initializeSlots(Inventory playerInventory) {
        menu.addClientSlot(new Slot(menu.getInventory(), 0, 80, 35));
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                menu.addClientSlot(new Slot(playerInventory, 9 + x + y * 9, 8 + x * 18, 84 + y * 18));
            }
        }
        for (int x = 0; x < 9; x++) {
            menu.addClientSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    protected void renderBg(PoseStack stack, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, textureLocation);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        GuiComponent.blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight, textureWidth, textureHeight);
    }
}
