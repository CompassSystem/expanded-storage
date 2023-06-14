package compasses.expandedstorage.fabric;

import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.block.misc.CopperBlockHelper;
import compasses.expandedstorage.thread.ThreadCommonHelper;
import compasses.expandedstorage.thread.ThreadMain;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;

public final class FabricMain implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricLoader loader = FabricLoader.getInstance();
        boolean quiltDetected = loader.isModLoaded("quilt_loader");
        boolean isCarrierCompatEnabled;
        try {
            SemanticVersion version = SemanticVersion.parse("1.8.0");
            isCarrierCompatEnabled = loader.getModContainer("carrier").map(it -> {
                return it.getMetadata().getVersion().compareTo(version) > 0;
            }).orElse(false);
        } catch (VersionParsingException e) {
            throw new IllegalStateException("Author made a typo: ", e);
        }

        ThreadMain.constructContent(new FabricCommonHelper(),
                loader.isModLoaded("htm"), "Fabric", quiltDetected ? "Quilt" : "Fabric", initializer -> {
                    if (isCarrierCompatEnabled) {
                        ThreadMain.registerCarrierCompat(initializer);
                    }
                    this.registerOxidisableAndWaxableBlocks();
                }
        );

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ((ThreadCommonHelper) CommonMain.platformHelper()).setServerInstance(null);
        });
    }

    private void registerOxidisableAndWaxableBlocks() {
        CopperBlockHelper.oxidisation().forEach(OxidizableBlocksRegistry::registerOxidizableBlockPair);
        CopperBlockHelper.dewaxing().inverse().forEach(OxidizableBlocksRegistry::registerWaxableBlockPair);
    }
}
