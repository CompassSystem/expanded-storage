package compasses.expandedstorage.quilt;

import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.block.misc.CopperBlockHelper;
import compasses.expandedstorage.thread.ThreadCommonHelper;
import compasses.expandedstorage.thread.ThreadMain;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.Version;
import org.quiltmc.loader.api.VersionFormatException;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.block.content.registry.api.ReversibleBlockEntry;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

public final class QuiltMain implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        boolean isCarrierCompatEnabled = QuiltLoader.getModContainer("carrier").map(it -> {
            try {
                Version.Semantic version = Version.Semantic.of(it.metadata().version().raw());
                return version.compareTo(Version.Semantic.of(new int[]{1, 8, 0}, "", "")) > 0;
            } catch (VersionFormatException e) {
                System.err.println("Carrier compat broke, cannot parse mod version.");
                return false;
            }
        }).orElse(false);

        ThreadMain.constructContent(new QuiltCommonHelper(), QuiltLoader.isModLoaded("htm"), "Quilt", "Quilt", initializer -> {
                    if (isCarrierCompatEnabled) {
                        ThreadMain.registerCarrierCompat(initializer);
                    }
                    this.registerWaxedContent();
                }
        );

        ServerLifecycleEvents.STOPPED.register(server -> {
            ((ThreadCommonHelper) CommonMain.platformHelper()).setServerInstance(null);
        });
    }

    private void registerWaxedContent() {
        CopperBlockHelper.dewaxing().forEach((waxed, dewaxed) -> {
            BlockContentRegistries.WAXABLE.put(dewaxed, new ReversibleBlockEntry(waxed, true));
        });
        CopperBlockHelper.oxidisation().forEach((before, next) -> {
            BlockContentRegistries.OXIDIZABLE.put(before, new ReversibleBlockEntry(next, true));
        });
    }
}
