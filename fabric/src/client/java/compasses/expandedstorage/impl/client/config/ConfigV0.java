package compasses.expandedstorage.impl.client.config;

import compasses.expandedstorage.impl.Utils;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigV0 implements Config {
    private final boolean restrictiveScrolling;
    private final boolean preferSmallerScreens;
    private final List<ResourceLocation> preferSingleScreens;
    private ResourceLocation screenType;

    public ConfigV0() {
        this(null, false, true, List.of(Utils.PAGINATED_SCREEN_TYPE, Utils.SCROLLABLE_SCREEN_TYPE));
    }

    public ConfigV0(ResourceLocation screenType, boolean restrictiveScrolling, boolean preferSmallerScreens, List<ResourceLocation> preferSingleScreens) {
        if (String.valueOf(screenType).equals("expandedstorage:auto")) {
            this.screenType = null;
        } else {
            this.screenType = screenType;
        }
        this.restrictiveScrolling = restrictiveScrolling;
        this.preferSmallerScreens = preferSmallerScreens;
        this.preferSingleScreens = preferSingleScreens;
    }

    public ResourceLocation getScreenType() {
        return screenType;
    }

    public void setScreenType(ResourceLocation screenType) {
        this.screenType = screenType;
    }

    public boolean isScrollingRestricted() {
        return restrictiveScrolling;
    }

    public boolean preferSmallerScreens() {
        return this.preferSmallerScreens;
    }

    public boolean prefersSingleScreen(ResourceLocation type) {
        return preferSingleScreens.contains(type);
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Converter<Map<String, Object>, ConfigV0> getConverter() {
        return Factory.INSTANCE;
    }

    public static final class Factory implements Converter<Map<String, Object>, ConfigV0> {
        public static final Factory INSTANCE = new Factory();

        private Factory() {

        }

        @Override
        public ConfigV0 fromSource(Map<String, Object> source) {
            if (source.get("container_type") instanceof String screenType && source.get("restrictive_scrolling") instanceof Boolean restrictiveScrolling) {
                Boolean preferSmallerScreens = Boolean.TRUE;
                if (source.containsKey("prefer_smaller_screens") && source.get("prefer_smaller_screens") instanceof Boolean bool) {
                    preferSmallerScreens = bool;
                }
                List<ResourceLocation> preferSingleScreens = List.of(Utils.PAGINATED_SCREEN_TYPE, Utils.SCROLLABLE_SCREEN_TYPE);
                if (source.containsKey("prefer_single_screens") && source.get("prefer_single_screens") instanceof List value) {
                    preferSingleScreens = (List<ResourceLocation>) value.stream().map(it -> ResourceLocation.tryParse(it.toString())).dropWhile(it -> it == null).toList();
                }
                return new ConfigV0(ResourceLocation.tryParse(screenType), restrictiveScrolling, preferSmallerScreens, preferSingleScreens);
            }
            return null;
        }

        @Override
        public Map<String, Object> toSource(ConfigV0 target) {
            Map<String, Object> values = new HashMap<>();
            values.put("container_type", target.screenType);
            values.put("restrictive_scrolling", target.restrictiveScrolling);
            values.put("prefer_bigger_screens", target.preferSmallerScreens);
            values.put("prefer_single_screens", target.preferSingleScreens);
            return values;
        }

        @Override
        public int getSourceVersion() {
            return 0;
        }

        @Override
        public int getTargetVersion() {
            return 0;
        }
    }
}
