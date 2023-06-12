package compasses.expandedstorage.common.client.gui.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.List;

public class ConfigEntry extends ContainerObjectSelectionList.Entry<ConfigEntry> {
    private final List<AbstractWidget> children;

    public ConfigEntry(List<AbstractWidget> children) {
        this.children = children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return children;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isFocused, float partialTicks) {
        for (AbstractWidget child : children) {
            child.setY(y);
            child.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
}
