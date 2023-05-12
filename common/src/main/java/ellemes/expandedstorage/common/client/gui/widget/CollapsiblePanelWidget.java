package ellemes.expandedstorage.common.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CollapsiblePanelWidget extends AbstractWidget {
    private AbstractWidget openPanelButton;
    private final List<AbstractWidget> children = new ArrayList<>();

    public CollapsiblePanelWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);

        visible = false;
    }

    public void setPanelOpenButton(AbstractWidget widget) {
        openPanelButton = widget;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(Screen.BACKGROUND_LOCATION, this.getX(), this.getY(), 0, 0.0F, 0.0F, width, height, 32, 32);
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
