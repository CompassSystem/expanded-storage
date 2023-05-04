package ellemes.expandedstorage.common.misc;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ellemes.expandedstorage.common.config.IdentifierTypeAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Type;
import java.util.Map;

public final class Utils {
    public static final String MOD_ID = "expandedstorage";
    public static final Component ALT_USE = Component.translatable("tooltip.expandedstorage.alt_use",
            Component.keybind("key.sneak").withStyle(ChatFormatting.GOLD),
            Component.keybind("key.use").withStyle(ChatFormatting.GOLD));
    public static final int WOOD_STACK_COUNT = 27;
    public static final ResourceLocation WOOD_TIER_ID = Utils.id("wood");
    public static final ResourceLocation COPPER_TIER_ID = Utils.id("copper");
    public static final int TOOL_USAGE_QUICK_DELAY = 5; // In ticks...
    public static final int TOOL_USAGE_DELAY = 20; // In ticks...
    // Gui Element Sizes
    public static final int SLOT_SIZE = 18;
    public static final int CONTAINER_HEADER_HEIGHT = 17;
    public static final int CONTAINER_PADDING_LDR = 7;
    // Handler Type ID
    public static final ResourceLocation HANDLER_TYPE_ID = Utils.containerId("handler_type");
    public static final String CONFIG_PATH = "expandedstorage.json";
    // Config Related
    public static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();
    // todo: look into possibility of replacing, might be worth exposing obj->json to configs.
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new IdentifierTypeAdapter())
                                                     .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                                     .setPrettyPrinting()
                                                     .setLenient()
                                                     .create();
    public static final int KEY_BIND_KEY = GLFW.GLFW_KEY_G;
    public static final ResourceLocation PAGE_SCREEN_TYPE = new ResourceLocation(Utils.MOD_ID, "page");
    public static final ResourceLocation SINGLE_SCREEN_TYPE = new ResourceLocation(Utils.MOD_ID, "single");
    public static final ResourceLocation SCROLL_SCREEN_TYPE = new ResourceLocation(Utils.MOD_ID, "scroll");
    public static final ResourceLocation UNSET_SCREEN_TYPE = new ResourceLocation(Utils.MOD_ID, "auto");
    public static boolean generatedGuiTexturesEnabled = false;

    private Utils() {
        throw new IllegalStateException("Should not instantiate this class.");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Utils.MOD_ID, path);
    }

    public static ResourceLocation containerId(String path) {
        return new ResourceLocation("ellemes_container_lib", path);
    }
}
