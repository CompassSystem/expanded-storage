package ellemes.container_library.client.gui;

import com.google.common.collect.ImmutableSortedSet;
import com.mojang.blaze3d.vertex.PoseStack;
import ellemes.container_library.CommonClient;
import ellemes.container_library.api.client.function.ScreenSizePredicate;
import ellemes.container_library.api.client.gui.AbstractScreen;
import ellemes.container_library.api.inventory.AbstractHandler;
import ellemes.container_library.client.PickButton;
import ellemes.container_library.client.gui.widget.ScreenPickButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class PickScreen extends Screen {
    private static final Component CURRENT_OPTION_TEXT = Component.translatable("screen.ellemes_container_lib.current_option_notice").withStyle(ChatFormatting.GOLD);
    private static final Map<ResourceLocation, PickButton> BUTTON_SETTINGS = new HashMap<>();
    private final Set<ResourceLocation> options = ImmutableSortedSet.copyOf(PickScreen.BUTTON_SETTINGS.keySet());
    private final Supplier<Screen> returnToScreen;
    private final @NotNull Runnable onOptionPicked;
    private final AbstractHandler handler;
    private int topPadding;

    public PickScreen(AbstractScreen currentScreen) {
        this(currentScreen.getMenu(), () -> {
            return AbstractScreen.createScreen(currentScreen.getMenu(), Minecraft.getInstance().player.getInventory(), currentScreen.getTitle());
        }, () -> {});
    }

    public PickScreen(Supplier<Screen> returnToScreen) {
        this(null, returnToScreen, () -> {});
    }

    public PickScreen(@NotNull Runnable onOptionPicked) {
        this(null, () -> null, onOptionPicked);
    }

    private PickScreen(@Nullable AbstractHandler handler, Supplier<Screen> returnToScreen, Runnable onOptionPicked) {
        super(Component.translatable("screen.ellemes_container_lib.screen_picker_title"));
        this.handler = handler;
        this.returnToScreen = returnToScreen;
        this.onOptionPicked = onOptionPicked;
    }

    @Deprecated
    @ApiStatus.Internal
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static void declareButtonSettings(ResourceLocation type, ResourceLocation texture, Component title, ScreenSizePredicate warningTest, List<Component> warningText) {
        PickScreen.BUTTON_SETTINGS.putIfAbsent(type, new PickButton(texture, title, warningTest, warningText));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onClose() {
        if (handler != null) {
            ResourceLocation preference = CommonClient.getConfigWrapper().getPreferredScreenType();
            int invSize = handler.getInventory().getContainerSize();
            if (AbstractScreen.getScreenSize(preference, invSize, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight()) == null) {
                minecraft.player.displayClientMessage(Component.translatable("generic.ellemes_container_lib.label").withStyle(ChatFormatting.GOLD).append(Component.translatable("chat.ellemes_container_lib.cannot_display_screen", Component.translatable("screen." + preference.getNamespace() + "." + preference.getPath() + "_screen")).withStyle(ChatFormatting.WHITE)), false);
                minecraft.player.closeContainer();
                return;
            }
            handler.clearSlots();
        }
        minecraft.setScreen(returnToScreen.get());
    }

    @Override
    public boolean isPauseScreen() {
        //noinspection ConstantConditions
        return minecraft.level == null;
    }

    @Override
    protected void init() {
        super.init();
        ResourceLocation preference = CommonClient.getConfigWrapper().getPreferredScreenType();
        int choices = options.size();
        int columns = Math.min(Mth.intFloorDiv(width, 96), choices);
        int innerPadding = Math.min((width - columns * 96) / (columns + 1), 20); // 20 is smallest gap for any screen.
        int outerPadding = (width - (((columns - 1) * innerPadding) + (columns * 96))) / 2;
        int x = 0;
        int topPadding = (height - 96) / 2;
        this.topPadding = topPadding;
        for (ResourceLocation option : options) {
            PickButton settings = PickScreen.BUTTON_SETTINGS.get(option);
            boolean isWarn = settings.getWarningTest().test(width, height);
            boolean isCurrent = option.equals(preference);
            MutableComponent tooltipMessage = Component.literal("").append(settings.getTitle());
            if (isCurrent) {
                tooltipMessage.append("\n");
                tooltipMessage.append(CURRENT_OPTION_TEXT);
            }
            if (isWarn) {
                tooltipMessage.append("\n");
                settings.getWarningText().forEach(tooltipMessage::append);
            }
            this.addRenderableWidget(new ScreenPickButton(outerPadding + (innerPadding + 96) * x, topPadding, 96, 96,
                    settings.getTexture(), settings.getTitle(), isWarn, isCurrent, __ -> this.updatePlayerPreference(option), Tooltip.create(tooltipMessage, tooltipMessage)));
            x++;
        }
    }

    private void updatePlayerPreference(ResourceLocation selection) {
        CommonClient.getConfigWrapper().setPreferredScreenType(selection);
        onOptionPicked.run();
        this.onClose();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        GuiComponent.drawCenteredString(stack, font, title, width / 2, Math.max(topPadding / 2, 0), 0xFFFFFFFF);
    }
}
