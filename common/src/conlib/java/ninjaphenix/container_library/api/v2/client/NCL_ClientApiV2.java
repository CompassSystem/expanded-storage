package ninjaphenix.container_library.api.v2.client;

import ellemes.container_library.CommonClient;
import ellemes.container_library.api.v3.client.ScreenTypeApi;
import ellemes.container_library.client.gui.PickScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import ninjaphenix.container_library.api.client.ScreenConstructor;
import ninjaphenix.container_library.api.client.function.ScreenSizePredicate;
import ninjaphenix.container_library.api.client.function.ScreenSizeRetriever;
import ninjaphenix.container_library.api.client.gui.AbstractScreen;

import java.util.List;
import java.util.Objects;

public final class NCL_ClientApiV2 {
    private NCL_ClientApiV2() {
        throw new IllegalStateException("NCL_ClientApi cannot be instantiated.");
    }

    /**
     * Call on client side to attempt to open an inventory, sort of internal, should be accessed through:
     * {@link ninjaphenix.container_library.api.v2.OpenableBlockEntityProviderV2}.
     *
     * @return true if a valid screen type is already selected.
     * @deprecated Use method with skipOptionCheck boolean
     */
    @Deprecated
    public static boolean openInventoryAt(BlockPos pos, InteractionHand hand, BlockHitResult hit) {
        return NCL_ClientApiV2.openInventoryAt(pos, hand, hit, false);
    }

    /**
     * Call on client side to attempt to open an inventory, sort of internal, should be accessed through:
     * {@link ninjaphenix.container_library.api.v2.OpenableBlockEntityProviderV2}.
     * <p>
     * If {@code skipOptionCheck} is true then the user's screen preference is not checked.
     *
     * @return true if a valid screen type is already selected.
     */
    public static boolean openInventoryAt(BlockPos pos, InteractionHand hand, BlockHitResult hit, boolean skipOptionCheck) {
        Objects.requireNonNull(pos, "pos must not be null");
        if (!skipOptionCheck && !AbstractScreen.isScreenTypeDeclared(CommonClient.getConfigWrapper().getPreferredScreenType())) {
            Minecraft.getInstance().setScreen(new PickScreen(() -> {
                Minecraft.getInstance().getConnection().send(new ServerboundUseItemOnPacket(hand, hit));
            }));
            return false;
        }
        return true;
    }

    /**
     * Register button for screen type pick screen with an optional error message.
     * Note: texture must be 96 x 288 ( 3 images: normal, hovered, current )
     *
     * @deprecated Use {@link ScreenTypeApi#registerScreenButton(ResourceLocation, ResourceLocation, Component, ScreenSizePredicate, List)} instead.
     */
    @Deprecated
    public static void registerScreenButton(ResourceLocation type, ResourceLocation texture, Component title, ScreenSizePredicate warningTest, List<Component> warningText) {
        ScreenTypeApi.registerScreenButton(type, texture, title, warningTest, warningText);
    }

    /**
     * Register button for screen type pick screen
     * Note: texture must be 96 x 288 ( 3 images: normal, hovered, current )
     *
     * @deprecated Use {@link ScreenTypeApi#registerScreenButton(ResourceLocation, ResourceLocation, Component)} instead.
     */
    @Deprecated
    public static void registerScreenButton(ResourceLocation type, ResourceLocation texture, Component title) {
        ScreenTypeApi.registerScreenButton(type, texture, title, ScreenSizePredicate::noTest, List.of());
    }

    /**
     * Register screen constructor.
     *
     * @deprecated Use {@link ScreenTypeApi#registerScreenType(ResourceLocation, ScreenConstructor)} instead.
     */
    @Deprecated
    public static void registerScreenType(ResourceLocation type, ScreenConstructor<?> screenConstructor) {
        ScreenTypeApi.registerScreenType(type, screenConstructor);
    }

    /**
     * Register default screen sizes, it is planned to allow players to override the default screen sizes in the future.
     *
     * @deprecated Use {@link ScreenTypeApi#registerDefaultScreenSize(ResourceLocation, ScreenSizeRetriever)} instead.
     */
    @Deprecated
    public static void registerDefaultScreenSize(ResourceLocation type, ScreenSizeRetriever retriever) {
        ScreenTypeApi.registerDefaultScreenSize(type, retriever);
    }

    /**
     * Uses the single screen type over the specified type if the single screen will visually fit in the game window.
     * Note: May be renamed in the future.
     *
     * @deprecated Use {@link ScreenTypeApi#setPrefersSingleScreen(ResourceLocation)} instead.
     */
    @Deprecated
    public static void setPrefersSingleScreen(ResourceLocation type) {
        ScreenTypeApi.setPrefersSingleScreen(type);
    }
}
