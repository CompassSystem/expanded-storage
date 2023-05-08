package ellemes.expandedstorage.common.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CollapsiblePanelWidget extends AbstractWidget {
    private AbstractWidget openPanelButton;
    private List<AbstractWidget> children = new ArrayList<>();

    public CollapsiblePanelWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);

        visible = false;
    }

    public void setPanelOpenButton(AbstractWidget widget) {
        openPanelButton = widget;
    }

    @Override
    public void renderWidget(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
        blit(stack, this.getX(), this.getY(), 0, 0.0F, 0.0F, width, height, 32, 32);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void toggleHidden() {
        openPanelButton.visible = visible;
        visible = !visible;

        for (AbstractWidget child : children) {
            child.visible = visible;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }
}
