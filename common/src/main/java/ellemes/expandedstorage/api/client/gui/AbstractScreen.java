package ellemes.expandedstorage.api.client.gui;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ellemes.expandedstorage.api.client.ScreenConstructor;
import ellemes.expandedstorage.api.client.function.ScreenSize;
import ellemes.expandedstorage.api.client.function.ScreenSizeRetriever;
import ellemes.expandedstorage.api.inventory.AbstractHandler;
import ellemes.expandedstorage.common.CommonClient;
import ellemes.expandedstorage.common.client.SizedSimpleTexture;
import ellemes.expandedstorage.common.client.gui.PickScreen;
import ellemes.expandedstorage.common.misc.ErrorlessTextureGetter;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractScreen extends AbstractContainerScreen<AbstractHandler> {
    private static final Map<ResourceLocation, ScreenConstructor<?>> SCREEN_CONSTRUCTORS = new HashMap<>();
    private static final Map<ResourceLocation, ScreenSizeRetriever> SIZE_RETRIEVERS = new HashMap<>();
    private static final Set<ResourceLocation> PREFERS_SINGLE_SCREEN = new HashSet<>();

    protected final int inventoryWidth, inventoryHeight, totalSlots;
    protected final ResourceLocation textureLocation;
    protected final int textureWidth, textureHeight;
    //todo:temp
    public static Path savePath = Path.of("genned_image.png");

    protected AbstractScreen(AbstractHandler handler, Inventory playerInventory, Component title, ScreenSize screenSize) {
        super(handler, playerInventory, title);
        totalSlots = handler.getInventory().getContainerSize();
        inventoryWidth = screenSize.getWidth();
        inventoryHeight = screenSize.getHeight();
        textureLocation = Utils.id("textures/gui/container/shared_" + inventoryWidth + "_" + inventoryHeight + ".png");
        boolean isTexturePresent = ((ErrorlessTextureGetter) Minecraft.getInstance().getTextureManager()).isTexturePresent(textureLocation);

        // todo: not rendering correctly...
        if (!isTexturePresent) {
            int guiWidth = 36 + Utils.SLOT_SIZE * inventoryWidth;
            int guiHeight = 132 + Utils.SLOT_SIZE * inventoryHeight;
            int textureWidth = (int) (Math.ceil(guiWidth / 16.0f) * 16);
            int textureHeight = (int) (Math.ceil(guiHeight / 16.0f) * 16);
            TextureTarget target = new TextureTarget(textureWidth, textureHeight, true, Minecraft.ON_OSX);
            target.bindWrite(true);

            PoseStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushPose();
            modelViewStack.setIdentity();
            modelViewStack.translate(0.0F, 0.0F, -2000.0F);
            RenderSystem.applyModelViewMatrix();

            this.renderGui(new PoseStack());

            NativeImage image = new NativeImage(textureWidth, textureHeight, false);
            target.bindRead();
            image.downloadTexture(0, false);
            image.flipY();

            // todo: temp
            try {
                image.writeToFile(savePath);
            } catch (IOException e) {
                System.out.println("Failed to save genned image.");
            }
            // end-todo

            DynamicTexture texture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(textureLocation, texture);


            target.destroyBuffers();
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(textureLocation);

        if (texture instanceof DynamicTexture dynamicTexture) {
            textureWidth = dynamicTexture.getPixels().getWidth();
            textureHeight = dynamicTexture.getPixels().getHeight();
        } else if (texture instanceof SizedSimpleTexture simpleTexture) {
            textureWidth = simpleTexture.getWidth();
            textureHeight = simpleTexture.getHeight();
        } else {
            throw new IllegalStateException();
        }
    }

    private void rect(PoseStack stack, int x, int y, int width, int height, float uOffset, float vOffset) {
        blit(stack, x, y, width, height, uOffset, vOffset, width, height, 96, 96);
    }

    private void renderGui(PoseStack stack) {
        RenderSystem.setShaderTexture(0, Utils.id("textures/gui/container/atlas_gen.png"));
//            // int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight

        // top
        {
            // left
            rect(stack, 0, 0, 7, 17, 1, 1);
            // middle
            for (int x = 0; x < inventoryWidth; x++) {
                rect(stack, 7 + x * Utils.SLOT_SIZE, 0, 18, 17, 9, 1);
            }
            // right
            rect(stack, 7 + inventoryWidth * Utils.SLOT_SIZE, 0, 7, 17, 28, 1);
            // scrollbar
            rect(stack, 7 + inventoryWidth * Utils.SLOT_SIZE + 7, 0, 22, 17, 36, 1);
        }

        // main container
        {
            for (int y = 0; y < inventoryHeight; y++) {
                int scollbarYOffset = y == 0 ? 19 : y == inventoryHeight - 1 ? 57 : 38;
                // left
                rect(stack, 0, 17 + Utils.SLOT_SIZE * y, 7, 18, 1, 19);
                // middle
                for (int x = 0; x < inventoryWidth; x++) {
                    rect(stack, 7 + Utils.SLOT_SIZE * x, 17 + Utils.SLOT_SIZE * y, Utils.SLOT_SIZE, Utils.SLOT_SIZE, 9, 19);
                }
                // right
                rect(stack, 7 + Utils.SLOT_SIZE * inventoryWidth, 17 + Utils.SLOT_SIZE * y, 7, 18, 28, 19);
                // scrollbar
                rect(stack, 7 + Utils.SLOT_SIZE * inventoryWidth + 7, 17 + Utils.SLOT_SIZE * y, 22, 18, 36, scollbarYOffset);
            }
        }

        // divider below main container
        {
            // left
            rect(stack, 0, 17 + Utils.SLOT_SIZE * inventoryHeight, 7, 14, 1, 38);

            //middle
            for (int x = 0; x < inventoryWidth; x++) {
                rect(stack, 7 + Utils.SLOT_SIZE * x, 17 + Utils.SLOT_SIZE * inventoryHeight, Utils.SLOT_SIZE, 14, 9, 38);
            }

            //right
            rect(stack, 7 + Utils.SLOT_SIZE * inventoryWidth, 17 + Utils.SLOT_SIZE * inventoryHeight, 7, 14, 28, 38);

            if (inventoryWidth > 9) {
                // scrollbar
                rect(stack, 7 + Utils.SLOT_SIZE * inventoryWidth + 7, 17 + Utils.SLOT_SIZE * inventoryHeight, 22, 17, 59, 76);
                rect(stack, 7 + Utils.SLOT_SIZE * inventoryWidth + 7, 17 + Utils.SLOT_SIZE * inventoryHeight + 17, 12, 15, 59, 1);
            } else {
                // scrollbar
                rect(stack, 7 + Utils.SLOT_SIZE * inventoryWidth + 7, 17 + Utils.SLOT_SIZE * inventoryHeight, 22, 7, 36, 76);
                rect(stack, 7 + Utils.SLOT_SIZE * inventoryWidth + 7, 17 + Utils.SLOT_SIZE * inventoryHeight + 7, 12, 15, 59, 1);
            }
        }

        // bottom of main container
        {
            if (inventoryWidth > 9) {
                // left
                rect(stack, 0, 17 + Utils.SLOT_SIZE * inventoryHeight + 7 + 3, 7, 7, 1, 58);
                // middle
                int sideParts = (int) Math.ceil((inventoryWidth - 9) / 2.0f);
                for (int i = 0; i < sideParts; i++) {
                    rect(stack, 7 + Utils.SLOT_SIZE * i, 17 + Utils.SLOT_SIZE * inventoryHeight + 7 + 3, 18, 7, 9, 58);
                    rect(stack, 7 + Utils.SLOT_SIZE * (inventoryWidth - i - 1), 17 + Utils.SLOT_SIZE * inventoryHeight + 7 + 3, 18, 7, 9, 58);
                }
                // right
                rect(stack, 7 + Utils.SLOT_SIZE * inventoryWidth, 17 + Utils.SLOT_SIZE * inventoryHeight + 7 + 3, 7, 7, 28, 58);
            }
        }
        int startX = (int) ((inventoryWidth - 9) / 2.0f * Utils.SLOT_SIZE);
        // player inventory
        {
            for (int y = 0; y < 3; y++) {
                // left
                rect(stack, startX, 17 + Utils.SLOT_SIZE * (inventoryHeight + y) + 14, 7, 18, 1, 19);
                //middle
                for (int x = 0; x < 9; x++) {
                    rect(stack, startX + 7 + Utils.SLOT_SIZE * x, 17 + Utils.SLOT_SIZE * (inventoryHeight + y) + 14, 18, 18, 9, 19);
                }
                //right
                rect(stack, startX + 7 + 9 * Utils.SLOT_SIZE, 17 + Utils.SLOT_SIZE * (inventoryHeight + y) + 14, 7, 18, 28, 19);
            }
            // left
            rect(stack, startX, 17 + Utils.SLOT_SIZE * (inventoryHeight + 3) + 14, 7, 4, 1, 53);
            rect(stack, startX, 17 + Utils.SLOT_SIZE * (inventoryHeight + 3) + 14 + 4, 7, 18, 1, 19);
            rect(stack, startX, 17 + Utils.SLOT_SIZE * (inventoryHeight + 4) + 14 + 4, 7, 7, 1, 58);
            //middle
            for (int x = 0; x < 9; x++) {
                rect(stack, startX + 7 + Utils.SLOT_SIZE * x, 17 + Utils.SLOT_SIZE * (inventoryHeight + 3) + 14, 18, 4, 9, 53);
                rect(stack, startX + 7 + Utils.SLOT_SIZE * x, 17 + Utils.SLOT_SIZE * (inventoryHeight + 3) + 14 + 4, 18, 18, 9, 19);
                rect(stack, startX + 7 + Utils.SLOT_SIZE * x, 17 + Utils.SLOT_SIZE * (inventoryHeight + 4) + 14 + 4, 18, 7, 9, 58);
            }
            //right
            rect(stack, startX + 7 + 9 * Utils.SLOT_SIZE, 17 + Utils.SLOT_SIZE * (inventoryHeight + 3) + 14, 7, 4, 28, 53);
            rect(stack, startX + 7 + 9 * Utils.SLOT_SIZE, 17 + Utils.SLOT_SIZE * (inventoryHeight + 3) + 14 + 4, 7, 18, 28, 19);
            rect(stack, startX + 7 + 9 * Utils.SLOT_SIZE, 17 + Utils.SLOT_SIZE * (inventoryHeight + 4) + 14 + 4, 7, 7, 28, 58);
        }

        if (inventoryWidth > 9) {
            rect(stack, startX, 17 + Utils.SLOT_SIZE * (inventoryHeight) + 14, 3, 3, 20, 66);
            rect(stack, startX + Utils.SLOT_SIZE * 9 + 11, 17 + Utils.SLOT_SIZE * (inventoryHeight) + 14, 3, 3, 24, 66);
        }

        // blank slots
        {
            for (int x = 0; x < inventoryWidth; x++) {
                rect(stack, 7 + Utils.SLOT_SIZE * x, 17 + Utils.SLOT_SIZE * (inventoryHeight + 4) + 14 + 4 + 7, Utils.SLOT_SIZE, Utils.SLOT_SIZE, 1, 66);
            }
        }

    }

    public static AbstractScreen createScreen(AbstractHandler handler, Inventory playerInventory, Component title) {
        ResourceLocation forcedScreenType = handler.getForcedScreenType();
        ResourceLocation preference = forcedScreenType != null ? forcedScreenType : CommonClient.platformHelper().configWrapper().getPreferredScreenType();
        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int slots = handler.getInventory().getContainerSize();

        if (forcedScreenType == null && AbstractScreen.canSingleScreenDisplay(slots, scaledWidth, scaledHeight) && AbstractScreen.shouldPreferSingleScreen(preference)) {
            preference = Utils.SINGLE_SCREEN_TYPE;
        }

        ScreenSize screenSize = AbstractScreen.SIZE_RETRIEVERS.get(preference).get(slots, scaledWidth, scaledHeight);
        if (screenSize == null) {
            throw new IllegalStateException("screenSize should never be null...");
        }
        return AbstractScreen.SCREEN_CONSTRUCTORS.get(preference).createScreen(handler, playerInventory, title, screenSize);
    }

    private static boolean shouldPreferSingleScreen(ResourceLocation type) {
        return AbstractScreen.PREFERS_SINGLE_SCREEN.contains(type);
    }

    private static boolean canSingleScreenDisplay(int slots, int scaledWidth, int scaledHeight) {
        if (slots <= 54) {
            return true;
        }
        if (scaledHeight >= 276) {
            if (slots <= 81) {
                return true;
            }
            if (scaledWidth >= 230 && slots <= 108) {
                return true;
            }
            if (scaledWidth >= 284 && slots <= 135) {
                return true;
            }
            if (scaledWidth >= 338 && slots <= 162) {
                return true;
            }
        }
        if (scaledWidth >= 338) {
            if (scaledHeight >= 330 && slots <= 216) {
                return true;
            }
            return scaledHeight >= 384 && slots <= 270;
        }
        return false;
    }

    public static void declareScreenType(ResourceLocation type, ScreenConstructor<?> screenConstructor) {
        AbstractScreen.SCREEN_CONSTRUCTORS.putIfAbsent(type, screenConstructor);
    }

    public static void declareScreenSizeRetriever(ResourceLocation type, ScreenSizeRetriever retriever) {
        AbstractScreen.SIZE_RETRIEVERS.putIfAbsent(type, retriever);
    }

    public static boolean isScreenTypeDeclared(ResourceLocation type) {
        return AbstractScreen.SCREEN_CONSTRUCTORS.containsKey(type);
    }

    public static void setPrefersSingleScreen(ResourceLocation type) {
        AbstractScreen.PREFERS_SINGLE_SCREEN.add(type);
    }

    @Nullable
    public static ScreenSize getScreenSize(ResourceLocation type, int slots, int scaledWidth, int scaledHeight) {
        return AbstractScreen.SIZE_RETRIEVERS.get(type).get(slots, scaledWidth, scaledHeight);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        this.renderTooltip(stack, mouseX, mouseY);

//        Render gui test code
//        RenderSystem.disableDepthTest();
//        RenderSystem.enableBlend();
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.25f);
//        renderGui(stack);
//        RenderSystem.enableDepthTest();
//        RenderSystem.disableBlend();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public final boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.handleKeyPress(keyCode, scanCode, modifiers)) {
            return true;
        } else if (CommonClient.platformHelper().isConfigKeyPressed(keyCode, scanCode, modifiers) && menu.getForcedScreenType() == null
                && !CommonClient.platformHelper().configWrapper().getPreferredScreenType().equals(Utils.UNSET_SCREEN_TYPE)) {
            minecraft.setScreen(new PickScreen(this));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * @return true if a screen specific keybinding is pressed otherwise false to follow through with additional checks.
     */
    protected boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @NotNull
    @ApiStatus.OverrideOnly
    public List<Rect2i> getExclusionZones() {
        return List.of();
    }

    public int getInventoryWidth() {
        return inventoryWidth;
    }
}
