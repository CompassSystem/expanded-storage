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
            target.bindWrite(false);

            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.setIdentity();
            poseStack.translate(0.0F, 0.0F, -2000.0F);
            RenderSystem.applyModelViewMatrix();
            PoseStack stack = new PoseStack();

            RenderSystem.setShaderTexture(0, Utils.id("textures/gui/container/atlas_gen.png"));
//            // int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight

            // top left corner
            blit(stack, 0, 0, 7, 17, 1, 1, 7, 17, 64, 96);

            stack.popPose();
            RenderSystem.applyModelViewMatrix();

            NativeImage image = new NativeImage(target.width, target.height, false);
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
