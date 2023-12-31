package compasses.expandedstorage.impl.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {
    public static final String MOD_ID = "expandedstorage";
    public static final Logger LOGGER = LoggerFactory.getLogger("Expanded Storage");
    public static final Component ALT_USE = Component.translatable("tooltip.expandedstorage.alt_use",
            Component.keybind("key.sneak").withStyle(ChatFormatting.GOLD),
            Component.keybind("key.use").withStyle(ChatFormatting.GOLD));
    public static final int TOOL_USAGE_QUICK_DELAY = 5; // In ticks...
    public static final int TOOL_USAGE_DELAY = 20; // In ticks...
    // Gui Element Sizes
    public static final int SLOT_SIZE = 18;
    public static final int CONTAINER_HEADER_HEIGHT = 17;
    public static final int CONTAINER_PADDING_LDR = 7;
    // Handler Type ID
    public static final ResourceLocation HANDLER_TYPE_ID = Utils.id("handler_type");
    public static final int KEY_BIND_KEY = GLFW.GLFW_KEY_G;
    public static final ResourceLocation PAGINATED_SCREEN_TYPE = new ResourceLocation(Utils.MOD_ID, "paginated");
    public static final ResourceLocation SINGLE_SCREEN_TYPE = new ResourceLocation(Utils.MOD_ID, "single");
    public static final ResourceLocation SCROLLABLE_SCREEN_TYPE = new ResourceLocation(Utils.MOD_ID, "scrollable");
    public static final ResourceLocation MINI_STORAGE_SCREEN_TYPE = new ResourceLocation(Utils.MOD_ID, "mini_storage");

    private Utils() {
        throw new IllegalStateException("Should not instantiate this class.");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Utils.MOD_ID, path);
    }
}
