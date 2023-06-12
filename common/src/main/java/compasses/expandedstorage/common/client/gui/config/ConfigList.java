package compasses.expandedstorage.common.client.gui.config;

import compasses.expandedstorage.common.client.gui.widget.LabelWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

public class ConfigList extends ContainerObjectSelectionList<ConfigEntry> {
    public ConfigList(Minecraft minecraft, int width, int height, int top, int bottom, int rowHeight) {
        super(minecraft, width, height, top, bottom, rowHeight);
    }

    public void addSection(String label) {
        Component component = Component.translatable(label);
        addEntry(new ConfigEntry(List.of(new LabelWidget((width / 2) - (Minecraft.getInstance().font.width(component) / 2), 0, component, null))));
    }

    public void addEntry(Function<Integer, AbstractWidget> widget) {
        addEntry(new ConfigEntry(List.of(widget.apply(width))));
    }

    public void addLabelledEntry(String labelLanguageKey, Function<Integer, AbstractWidget> widgetFunction) {
        addEntry(new ConfigEntry(List.of(
                new LabelWidget(5, 0,  Component.translatable(labelLanguageKey + ".label"), Component.translatable(labelLanguageKey + ".hint")),
                widgetFunction.apply(width)
        )));
    }
}
