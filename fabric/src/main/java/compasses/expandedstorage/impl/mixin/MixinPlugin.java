package compasses.expandedstorage.impl.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public final class MixinPlugin implements IMixinConfigPlugin {
    private static final int MIXIN_PACKAGE_LENGTH = (MixinPlugin.class.getPackageName() + ".common.").length();

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String className = mixinClassName.substring(MIXIN_PACKAGE_LENGTH);
        return switch (className) {
            case "HTMChestCompat", "HTMLockableBlockEntityCompat" -> FabricLoader.getInstance().isModLoaded("htm");
            case "ToweletteCompat" -> FabricLoader.getInstance().isModLoaded("towelette");
            case "InventoryTabsBottomRowFix", "InventoryTabsFix" -> FabricLoader.getInstance().isModLoaded("inventorytabs");
            default -> true;
        };
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
