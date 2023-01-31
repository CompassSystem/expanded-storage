package ellemes.container_library.api.v3.client;

import ellemes.container_library.client.gui.PickScreen;
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
        return new PickScreen(returnToScreen);
    }
}
