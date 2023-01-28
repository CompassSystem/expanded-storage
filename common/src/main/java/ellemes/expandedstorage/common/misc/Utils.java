package ellemes.expandedstorage.common.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public final class Utils {
    public static final String MOD_ID = "expandedstorage";
    public static final Component ALT_USE = new TranslatableComponent("tooltip.expandedstorage.alt_use",
            new KeybindComponent("key.sneak").withStyle(ChatFormatting.GOLD),
            new KeybindComponent("key.use").withStyle(ChatFormatting.GOLD));
    public static final int WOOD_STACK_COUNT = 27;
    public static final ResourceLocation WOOD_TIER_ID = Utils.id("wood");
    public static final ResourceLocation COPPER_TIER_ID = Utils.id("copper");
    public static final int TOOL_USAGE_QUICK_DELAY = 5; // In ticks...
    public static final int TOOL_USAGE_DELAY = 20; // In ticks...

    private Utils() {
        throw new IllegalStateException("Should not instantiate this class.");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Utils.MOD_ID, path);
    }

    public static MutableComponent translation(String key, Object... params) {
        return new TranslatableComponent(key, params);
    }
}
