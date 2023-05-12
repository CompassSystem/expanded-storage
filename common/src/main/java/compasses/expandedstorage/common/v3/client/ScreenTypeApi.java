package compasses.expandedstorage.common.v3.client;

import compasses.expandedstorage.common.client.ScreenConstructor;
import compasses.expandedstorage.common.client.function.ScreenSizePredicate;
import compasses.expandedstorage.common.client.function.ScreenSizeRetriever;
import compasses.expandedstorage.common.client.gui.AbstractScreen;
import compasses.expandedstorage.common.client.gui.PickScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public class ScreenTypeApi {
    private ScreenTypeApi() {
        throw new IllegalStateException("ScreenTypeApi should not be instantiated.");
    }

    /**
     * Register button for screen type pick screen with an optional error message.
     * Note: texture must be 96 x 288 ( 3 images: normal, hovered, current )
     */
    public static void registerScreenButton(ResourceLocation type, ResourceLocation texture, Component title, ScreenSizePredicate warningTest, List<Component> warningText) {
        ScreenTypeApi.requiresNonNull(type, "type");
        ScreenTypeApi.requiresNonNull(texture, "texture");
        ScreenTypeApi.requiresNonNull(title, "title");
        ScreenTypeApi.requiresNonNull(warningTest, "warningTest");
        ScreenTypeApi.requiresNonNull(warningText, "warningText");
        //noinspection deprecation
        PickScreen.declareButtonSettings(type, texture, title, warningTest, warningText);
    }

    /**
     * Register button for screen type pick screen
     * Note: texture must be 96 x 288 ( 3 images: normal, hovered, current )
     */
    public static void registerScreenButton(ResourceLocation type, ResourceLocation texture, Component title) {
        ScreenTypeApi.requiresNonNull(type, "type");
        ScreenTypeApi.requiresNonNull(texture, "texture");
        ScreenTypeApi.requiresNonNull(title, "title");
        //noinspection deprecation
        PickScreen.declareButtonSettings(type, texture, title, ScreenSizePredicate::noTest, List.of());
    }

    /**
     * Register screen constructor.
     */
    public static void registerScreenType(ResourceLocation type, ScreenConstructor<?> screenConstructor) {
        ScreenTypeApi.requiresNonNull(type, "type");
        ScreenTypeApi.requiresNonNull(screenConstructor, "screenConstructor");
        AbstractScreen.declareScreenType(type, screenConstructor);
    }

    /**
     * Register default screen sizes, it is planned to allow players to override the default screen sizes in the future.
     */
    public static void registerDefaultScreenSize(ResourceLocation type, ScreenSizeRetriever retriever) {
        ScreenTypeApi.requiresNonNull(type, "type");
        ScreenTypeApi.requiresNonNull(retriever, "retriever");
        AbstractScreen.declareScreenSizeRetriever(type, retriever);
    }

    /**
     * Uses the single screen type over the specified type if the single screen will visually fit in the game window.
     * Note: May be renamed in the future.
     */
    public static void setPrefersSingleScreen(ResourceLocation type) {
        ScreenTypeApi.requiresNonNull(type, "type");
        AbstractScreen.setPrefersSingleScreen(type);
    }

    private static void requiresNonNull(Object value, String parameterName) {
        Objects.requireNonNull(value, parameterName + " must not be null");
    }
}
