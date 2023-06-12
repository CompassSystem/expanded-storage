package compasses.expandedstorage.common.client.gui.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ConfigOverviewScreen extends Screen {
    private final Supplier<Screen> returnToScreen;
    private ConfigList configList;

    private boolean restrictiveScrolling = false; // todo: pull from config
    private boolean preferSingleScreens = false; // todo: pull from config
    private boolean widgetType = false; // todo: enum

    public ConfigOverviewScreen(@NotNull Supplier<Screen> returnToScreen) {
        super(Component.translatable("screen.expandedstorage.config.overview.title"));
        this.returnToScreen = returnToScreen;
    }

    @Override
    protected void init() {
        configList = new ConfigList(minecraft, width, height, 32, this.height - 32, 25);

        configList.addSection("screen.expandedstorage.config.overview.section.general");

        configList.addLabelledEntry(
                "screen.expandedstorage.config.overview.general.restrictive_scrolling",
                (x) -> CycleButton.onOffBuilder(restrictiveScrolling)
                                  .displayOnlyValue()
                                  .create(x - 55, 0, 50, 20, Component.translatable("screen.expandedstorage.config.overview.general.restrictive_scrolling.label"),
                                          (button, newState) -> {
                                              restrictiveScrolling = newState;
                                          }
                                  )
        );

        configList.addLabelledEntry(
                "screen.expandedstorage.config.overview.general.prefer_single_screens",
                (x) -> CycleButton.onOffBuilder(preferSingleScreens)
                                  .displayOnlyValue()
                                  .create(x - 55, 0, 50, 20, Component.translatable("screen.expandedstorage.config.overview.general.prefer_single_screens.label"),
                                          (button, newState) -> {
                                              preferSingleScreens = newState;
                                          }
                                  )
        );

        configList.addSection("screen.expandedstorage.config.overview.section.defaults");

        configList.addEntry(width -> {
                    return Button.builder(Component.translatable("screen.expandedstorage.config.overview.defaults.edit_screen_bounds.label"), button -> {

                                 })
                                 .bounds(5, 0, width - 10, 20)
                                 .build();
                }
        );

        configList.addLabelledEntry(
                "screen.expandedstorage.config.overview.defaults.screen_type",
                (x) -> CycleButton.booleanBuilder(Component.translatable("screen.expandedstorage.config.overview.defaults.paged_screen_type"), Component.translatable("screen.expandedstorage.config.overview.defaults.scrollable_screen_type"))
                                  .withInitialValue(widgetType)
                                  .displayOnlyValue()
                                  .create(x - 55, 0, 50, 20, Component.translatable("screen.expandedstorage.config.overview.defaults.screen_type.label"),
                                          (button, newState) -> {
                                              widgetType = newState;
                                          }
                                  )
        );

        configList.addSection("screen.expandedstorage.config.overview.section.edit_screens");

//        configList.addBig()
//        configList.addBig(OptionInstance.createBoolean("gui.done", false));

        this.addWidget(configList);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            // todo: custom saving logic.


            minecraft.setScreen(returnToScreen.get());
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderDirtBackground(graphics);

        configList.render(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawCenteredString(this.font, this.title, width / 2, 6, 0xFF_FFFFFF);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
