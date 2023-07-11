package compasses.expandedstorage.impl.client.config;

import compasses.expandedstorage.impl.misc.Utils;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public final class LegacyFactory implements Converter<Map<String, Object>, ConfigV0> {
    public static final LegacyFactory INSTANCE = new LegacyFactory();

    private LegacyFactory() {

    }

    @Override
    public ConfigV0 fromSource(Map<String, Object> source) {
        if (source.get("preferred_container_type") instanceof String temp && source.get("restrictive_scrolling") instanceof Boolean restrictiveScrolling) {
            if ("expandedstorage:paged".equals(temp)) {
                temp = Utils.PAGINATED_SCREEN_TYPE.toString();
            } else if ("expandedstorage:scrollable".equals(temp)) {
                temp = Utils.SCROLLABLE_SCREEN_TYPE.toString();
            }
            return new ConfigV0(ResourceLocation.tryParse(temp), restrictiveScrolling, true, List.of(Utils.PAGINATED_SCREEN_TYPE, Utils.SCROLLABLE_SCREEN_TYPE));
        }
        return null;
    }

    @Override
    public Map<String, Object> toSource(ConfigV0 target) {
        throw new UnsupportedOperationException("Legacy configs cannot be saved.");
    }

    @Override
    public int getSourceVersion() {
        return -1;
    }

    @Override
    public int getTargetVersion() {
        return 0;
    }
}
