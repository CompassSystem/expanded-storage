package compasses.expandedstorage.common.helpers.client;

import compasses.expandedstorage.common.client.gui.PickScreen;
import compasses.expandedstorage.common.client.gui.config.ConfigOverviewScreen;
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
        return new ConfigOverviewScreen(returnToScreen);
    }
}
