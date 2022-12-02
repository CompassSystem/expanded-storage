package ellemes.container_library.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ellemes.container_library.Utils;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ScreenPickButton extends Button {
    private static final ResourceLocation WARNING_TEXTURE = Utils.textureId("textures/gui/warning.png");
    private final ResourceLocation texture;
    private final boolean showWarningSymbol;
    private final boolean isCurrentPreference;

    public ScreenPickButton(int x, int y, int width, int height, ResourceLocation texture, Component message, boolean showWarningSymbol, boolean isCurrentPreference, OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        this.texture = texture;
        this.showWarningSymbol = showWarningSymbol;
        this.isCurrentPreference = isCurrentPreference;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        GuiComponent.blit(stack, this.getX(), this.getY(), 0, height * (this.isHoveredOrFocused() ? 1 : isCurrentPreference ? 2 : 0), width, height, width, height * 3);
        if (showWarningSymbol) {
            RenderSystem.setShaderTexture(0, WARNING_TEXTURE);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
            GuiComponent.blit(stack, this.getX() + width - 28, this.getY() + 9, 0, 0, 16, 32, 16, 32);
        }
    }

    public void renderButtonTooltip(PoseStack stack, int mouseX, int mouseY) {
        // todo: fix
//        if (isHovered) {
//            this.renderToolTip(stack, mouseX, mouseY);
//        } else if (this.isFocused()) {
//            this.renderToolTip(stack, this.getX(), this.getY());
//        }
    }
}
