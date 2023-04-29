package ellemes.expandedstorage.common.client.gui;

import com.google.common.collect.ImmutableSortedSet;
import com.mojang.blaze3d.vertex.PoseStack;
import ellemes.expandedstorage.api.client.function.ScreenSize;
import ellemes.expandedstorage.api.client.gui.AbstractScreen;
import ellemes.expandedstorage.api.inventory.AbstractHandler;
import ellemes.expandedstorage.common.CommonClient;
import ellemes.expandedstorage.common.client.PickButton;
import ellemes.expandedstorage.common.client.gui.widget.ScreenPickButton;
import ellemes.expandedstorage.common.misc.PlatformHelper;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class FakePickScreen extends AbstractScreen {
    private static final Component TITLE = Component.translatable("screen.ellemes_container_lib.screen_picker_title");
    private final Set<ResourceLocation> options = ImmutableSortedSet.copyOf(PickScreen.BUTTON_SETTINGS.keySet());
    private final List<ScreenPickButton> optionButtons = new ArrayList<>(options.size());
    private int topPadding;

    public FakePickScreen(AbstractHandler handler, Inventory playerInventory, Component title, ScreenSize screenSize) {
        super(handler, playerInventory, title, screenSize);
        for (int i = 0; i < menu.getInventory().getContainerSize(); i++) {
            menu.addClientSlot(new Slot(menu.getInventory(), i, 0, 0));
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                menu.addClientSlot(new Slot(playerInventory, y * 9 + x + 9, 0, 0));
            }
        }
        for (int x = 0; x < 9; x++) {
            menu.addClientSlot(new Slot(playerInventory, x, 0, 0));
        }
    }

    @Override
    protected void renderBg(PoseStack stack, float f, int i, int j) {
        this.renderBackground(stack);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onClose() {
        ResourceLocation preference = CommonClient.platformHelper().configWrapper().getPreferredScreenType();
        if (preference.equals(Utils.UNSET_SCREEN_TYPE)) {
            minecraft.player.closeContainer();
        } else {
            int invSize = menu.getInventory().getContainerSize();
            if (AbstractScreen.getScreenSize(preference, invSize, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight()) == null) {
                minecraft.player.displayClientMessage(Component.translatable("generic.ellemes_container_lib.label").withStyle(ChatFormatting.GOLD).append(Component.translatable("chat.ellemes_container_lib.cannot_display_screen", Component.translatable("screen." + preference.getNamespace() + "." + preference.getPath() + "_screen")).withStyle(ChatFormatting.WHITE)), false);
                minecraft.player.closeContainer();
                return;
            }
            menu.clearSlots();
            minecraft.setScreen(AbstractScreen.createScreen(menu, Minecraft.getInstance().player.getInventory(), this.getTitle()));
        }
    }

    @Override
    protected void init() {
        super.init();
        ResourceLocation preference = CommonClient.platformHelper().configWrapper().getPreferredScreenType();
        int choices = options.size();
        int columns = Math.min(Math.floorDiv(width, 96), choices);
        int innerPadding = Math.min((width - columns * 96) / (columns + 1), 20); // 20 is smallest gap for any screen.
        int outerPadding = (width - (((columns - 1) * innerPadding) + (columns * 96))) / 2;
        int x = 0;
        int topPadding = (height - 96) / 2;
        this.topPadding = topPadding;
        optionButtons.clear();
        for (ResourceLocation option : options) {
            PickButton settings = PickScreen.BUTTON_SETTINGS.get(option);
            boolean isWarn = settings.getWarningTest().test(width, height);
            boolean isCurrent = option.equals(preference);
            Button.OnTooltip tooltip = new Button.OnTooltip() {
                private static final Component CURRENT_OPTION_TEXT = Component.translatable("screen.ellemes_container_lib.current_option_notice").withStyle(ChatFormatting.GOLD);

                @Override
                public void onTooltip(Button button, PoseStack stack, int x, int y) {
                    List<Component> tooltip = new ArrayList<>(4);
                    tooltip.add(button.getMessage());
                    if (isCurrent) {
                        tooltip.add(CURRENT_OPTION_TEXT);
                    }
                    if (isWarn) {
                        tooltip.addAll(settings.getWarningText());
                    }
                    FakePickScreen.this.renderTooltip(stack, tooltip, Optional.empty(), x, y);
                }

                @Override
                public void narrateTooltip(Consumer<Component> consumer) {
                    if (isCurrent) {
                        consumer.accept(CURRENT_OPTION_TEXT);
                    }
                    if (isWarn) {
                        MutableComponent text = Component.literal("");
                        for (Component component : settings.getWarningText()) {
                            text.append(component);
                        }
                        consumer.accept(text);
                    }
                }
            };
            optionButtons.add(this.addRenderableWidget(new ScreenPickButton(outerPadding + (innerPadding + 96) * x, topPadding, 96, 96,
                    settings.getTexture(), settings.getTitle(), isWarn, isCurrent, button -> this.updatePlayerPreference(option), tooltip)));
            x++;
        }
    }

    private void updatePlayerPreference(ResourceLocation selection) {
        CommonClient.platformHelper().configWrapper().setPreferredScreenType(selection);
        this.onClose();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        this.renderBackground(stack);
        for (Widget widget : this.renderables) {
            widget.render(stack, mouseX, mouseY, delta);
        }
        optionButtons.forEach(button -> button.renderButtonTooltip(stack, mouseX, mouseY));
        GuiComponent.drawCenteredString(stack, font, TITLE, width / 2, Math.max(topPadding / 2, 0), 0xFFFFFFFF);
    }

    public static ScreenSize retrieveScreenSize(int slots, int scaledWidth, int scaledHeight) {
        return ScreenSize.of(0, 0);
    }
}
