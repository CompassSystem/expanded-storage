package compasses.expandedstorage.common.client.gui;

import compasses.expandedstorage.common.client.gui.widget.CollapsiblePanelWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ScreenConfigurationScreen extends Screen {
    private final Supplier<Screen> returnToScreen;

    public ScreenConfigurationScreen(@NotNull Supplier<Screen> returnToScreen) {
        super(Component.translatable("dataPack.bundle.description"));
        this.returnToScreen = returnToScreen;
    }

    @Override
    protected void init() {
        super.init();
        CollapsiblePanelWidget screenBoundariesPanel = new CollapsiblePanelWidget(0, 0, 100, height, Component.literal(""));
        Button openScreenBoundariesPanelButton = Button.builder(Component.literal("Open screen boundaries selection panel"), button1 -> screenBoundariesPanel.toggleHidden())
                .pos(10, 10)
                .size(20, 20)
                .build();
        screenBoundariesPanel.setPanelOpenButton(openScreenBoundariesPanelButton);
        addRenderableWidget(screenBoundariesPanel);
        addRenderableWidget(openScreenBoundariesPanelButton);

        System.out.println(width);
        CollapsiblePanelWidget screenConfigurationPanel = new CollapsiblePanelWidget(width - 100, 0, 100, height, Component.literal(""));
        Button openScreenConfigurationPanelButton = Button.builder(Component.literal("Open screen configuration panel"), button1 -> screenConfigurationPanel.toggleHidden())
                              .pos(width - 30, 10)
                              .size(20, 20)
                              .build();
        screenConfigurationPanel.setPanelOpenButton(openScreenConfigurationPanelButton);
        addRenderableWidget(screenConfigurationPanel);
        addRenderableWidget(openScreenConfigurationPanelButton);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(returnToScreen.get());
    }
}
