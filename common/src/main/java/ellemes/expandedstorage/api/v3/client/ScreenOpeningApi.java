package ellemes.expandedstorage.api.v3.client;

import ellemes.expandedstorage.common.client.gui.PickScreen;
import ellemes.expandedstorage.common.client.gui.ScreenConfigurationScreen;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public class ScreenOpeningApi {
    private ScreenOpeningApi() {
        throw new IllegalStateException("ScreenOpeningApi should not be instantiated.");
    }

    /**
     * Creates the config screen returning to the screen supplied.
     */
    public static Screen createTypeSelectScreen(@NotNull Supplier<Screen> returnToScreen) {
        Objects.requireNonNull(returnToScreen, "returnToScreen must not be null, pass () -> null instead.");
        if (Utils.generatedGuiTexturesEnabled) {
            return new ScreenConfigurationScreen(returnToScreen);
        }
        return new PickScreen(returnToScreen);
    }
}
