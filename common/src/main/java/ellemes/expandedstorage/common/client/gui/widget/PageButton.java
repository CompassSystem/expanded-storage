package ellemes.expandedstorage.common.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class PageButton extends Button {
    private static final ResourceLocation TEXTURE = Utils.id("textures/gui/page_buttons.png");
    private final int textureOffset;

    public PageButton(int x, int y, int textureOffset, Component message, OnPress onPress) {
        super(x, y, 12, 12, message, onPress, Button.DEFAULT_NARRATION);
        this.textureOffset = textureOffset;
        this.setTooltip(Tooltip.create(message));
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            this.setFocused(false);
        }
    }

    private int getTextureY() {
        if (!this.active) {
            return 0;
        } else if (this.isHoveredOrFocused()) {
            return 24;
        }
        return 12;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, PageButton.TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GuiComponent.blit(stack, this.getX(), this.getY(), textureOffset * 12, this.getTextureY(), width, height, 32, 48);
    }
}
