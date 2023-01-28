package ellemes.expandedstorage.common.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ellemes.container_library.api.client.function.ScreenSize;
import ellemes.container_library.api.client.gui.AbstractScreen;
import ellemes.container_library.api.inventory.AbstractHandler;
import ellemes.container_library.api.v3.client.ScreenTypeApi;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MiniStorageScreen extends AbstractScreen {
    private static final ResourceLocation TEXTURE = Utils.id("textures/gui/container/mini_chest_screen.png");
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 176;

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
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        GuiComponent.blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight, MiniStorageScreen.TEXTURE_WIDTH, MiniStorageScreen.TEXTURE_HEIGHT);
    }

    @NotNull
    @Override
    public List<Rect2i> getExclusionZones() {
        return List.of();
    }
}
