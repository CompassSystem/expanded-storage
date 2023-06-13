package compasses.expandedstorage.fabric;

import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.block.misc.CopperBlockHelper;
import compasses.expandedstorage.common.registration.Content;
import compasses.expandedstorage.common.registration.ContentConsumer;
import compasses.expandedstorage.thread.ThreadCommonHelper;
import compasses.expandedstorage.thread.ThreadMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;

import java.util.List;

public final class FabricMain implements ModInitializer {
    @Override
    public void onInitialize() {
        // todo: we should replace this with a nice warning screen before the main menu
        FabricLoader fabricLoader = FabricLoader.getInstance();
        boolean quiltDetected = fabricLoader.isModLoaded("quilt_loader");
        boolean isCarrierCompatEnabled;
        try {
            SemanticVersion version = SemanticVersion.parse("1.8.0");
            isCarrierCompatEnabled = fabricLoader.getModContainer("carrier").map(it -> {
                return it.getMetadata().getVersion().compareTo(version) > 0;
            }).orElse(false);
        } catch (VersionParsingException e) {
            throw new IllegalStateException("Author made a typo: ", e);
        }

        boolean isClient = fabricLoader.getEnvironmentType() == EnvType.CLIENT;
        ThreadMain.constructContent(new FabricCommonHelper(),
                fabricLoader.isModLoaded("htm"), isClient,
                ((ContentConsumer) ThreadMain::registerContent)
                        .andThenIf(isCarrierCompatEnabled, ThreadMain::registerCarrierCompat)
                        .andThen(this::registerOxidisableAndWaxableBlocks), List.of("Fabric", quiltDetected ? "Quilt" : "Fabric")
        );

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ((ThreadCommonHelper) CommonMain.platformHelper()).setServerInstance(null);
        });
    }

    private void registerOxidisableAndWaxableBlocks(Content content) {
        CopperBlockHelper.oxidisation().forEach(OxidizableBlocksRegistry::registerOxidizableBlockPair);
        CopperBlockHelper.dewaxing().inverse().forEach(OxidizableBlocksRegistry::registerWaxableBlockPair);
    }
}
