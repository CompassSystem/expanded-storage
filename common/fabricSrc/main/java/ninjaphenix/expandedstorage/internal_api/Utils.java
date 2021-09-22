package ninjaphenix.expandedstorage.internal_api;

import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.text.KeybindText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.function.UnaryOperator;

@Internal
@Experimental
public final class Utils {
    @Internal
    public static final String MOD_ID = "expandedstorage";
    @Internal
    public static final Text ALT_USE = new TranslatableText("tooltip.expandedstorage.alt_use",
            new KeybindText("key.sneak").formatted(Formatting.GOLD),
            new KeybindText("key.use").formatted(Formatting.GOLD));

    // Slots for storage tiers
    public static final int WOOD_STACK_COUNT = 27;
    public static final int IRON_STACK_COUNT = 54;
    public static final int GOLD_STACK_COUNT = 81;
    public static final int DIAMOND_STACK_COUNT = 108;
    public static final int OBSIDIAN_STACK_COUNT = 108;
    public static final int NETHERITE_STACK_COUNT = 135;

    // Default tiers
    public static final Tier WOOD_TIER = new Tier(Utils.id("wood"), WOOD_STACK_COUNT, UnaryOperator.identity(), UnaryOperator.identity());
    public static final Tier IRON_TIER = new Tier(Utils.id("iron"), IRON_STACK_COUNT, AbstractBlock.Settings::requiresTool, UnaryOperator.identity());
    public static final Tier GOLD_TIER = new Tier(Utils.id("gold"), GOLD_STACK_COUNT, AbstractBlock.Settings::requiresTool, UnaryOperator.identity());
    public static final Tier DIAMOND_TIER = new Tier(Utils.id("diamond"), DIAMOND_STACK_COUNT, AbstractBlock.Settings::requiresTool, UnaryOperator.identity());
    public static final Tier OBSIDIAN_TIER = new Tier(Utils.id("obsidian"), OBSIDIAN_STACK_COUNT, AbstractBlock.Settings::requiresTool, UnaryOperator.identity());
    public static final Tier NETHERITE_TIER = new Tier(Utils.id("netherite"), NETHERITE_STACK_COUNT, AbstractBlock.Settings::requiresTool, Item.Settings::fireproof);

    // Item Cooldown
    public static final int QUARTER_SECOND = 5;

    private Utils() {

    }

    @Internal
    public static Identifier id(String path) {
        return new Identifier(Utils.MOD_ID, path);
    }

    @Internal
    public static MutableText translation(String key, Object... params) {
        return new TranslatableText(key, params);
    }
}