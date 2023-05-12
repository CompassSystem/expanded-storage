package compasses.expandedstorage.fabric;

import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.block.BarrelBlock;
import compasses.expandedstorage.common.block.misc.CopperBlockHelper;
import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.common.registration.Content;
import compasses.expandedstorage.common.registration.ContentConsumer;
import compasses.expandedstorage.common.registration.NamedValue;
import compasses.expandedstorage.thread.ThreadMain;
import compasses.expandedstorage.thread.ThreadCommonHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.client.renderer.RenderType;
import org.slf4j.LoggerFactory;

public final class FabricMain implements ModInitializer {
    @Override
    public void onInitialize() {
        // todo: we should replace this with a nice warning screen before the main menu
        FabricLoader fabricLoader = FabricLoader.getInstance();
        if (fabricLoader.isModLoaded("quilt_loader")) {
            LoggerFactory.getLogger(Utils.MOD_ID).warn("Please use Expanded Storage for Quilt instead.");
            System.exit(0);
            return;
        }
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
                        .andThenIf(isClient, ThreadMain::registerClientStuff)
                        .andThenIf(isClient, this::registerBarrelRenderLayers)
                        .andThen(this::registerOxidisableAndWaxableBlocks)
        );

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ((ThreadCommonHelper) CommonMain.platformHelper()).setServerInstance(null);
        });
    }

    private void registerOxidisableAndWaxableBlocks(Content content) {
        CopperBlockHelper.oxidisation().forEach(OxidizableBlocksRegistry::registerOxidizableBlockPair);
        CopperBlockHelper.dewaxing().inverse().forEach(OxidizableBlocksRegistry::registerWaxableBlockPair);
    }

    private void registerBarrelRenderLayers(Content content) {
        for (NamedValue<BarrelBlock> block : content.getBarrelBlocks()) {
            BlockRenderLayerMap.INSTANCE.putBlock(block.getValue(), RenderType.cutoutMipped());
        }
    }
}
