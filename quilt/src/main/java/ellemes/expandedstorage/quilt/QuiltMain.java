package ellemes.expandedstorage.quilt;

import ellemes.expandedstorage.common.block.misc.CopperBlockHelper;
import ellemes.expandedstorage.common.misc.TagReloadListener;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.block.BarrelBlock;
import ellemes.expandedstorage.common.registration.Content;
import ellemes.expandedstorage.common.registration.ContentConsumer;
import ellemes.expandedstorage.common.registration.NamedValue;
import ellemes.expandedstorage.thread.ThreadMain;
import net.fabricmc.api.EnvType;
import net.minecraft.client.renderer.RenderType;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.Version;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.block.content.registry.api.ReversibleBlockEntry;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.slf4j.LoggerFactory;

public final class QuiltMain implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        if (!QuiltLoader.isModLoaded("quilt_loader")) {
            LoggerFactory.getLogger(Utils.MOD_ID).warn("Please use Expanded Storage for Fabric instead.");
            System.exit(0);
            return;
        }
        boolean isCarrierCompatEnabled = QuiltLoader.getModContainer("carrier").map(it -> {
            Version carrierVersion = it.metadata().version();
            return carrierVersion.isSemantic() && carrierVersion.semantic().compareTo(Version.of("1.8.0").semantic()) > 0;
        }).orElse(false);

        boolean isClient = MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT;
        TagReloadListener tagReloadListener = new TagReloadListener();
        ThreadMain.constructContent(QuiltLoader.isModLoaded("htm"), isClient, tagReloadListener,
                ((ContentConsumer) ThreadMain::registerContent)
                        .andThenIf(isCarrierCompatEnabled, ThreadMain::registerCarrierCompat)
                        .andThenIf(isClient, ThreadMain::registerClientStuff)
                        .andThenIf(isClient, this::registerBarrelRenderLayers)
                        .andThen(this::registerWaxedContent)
        );
        ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(((server, resourceManager, error) -> tagReloadListener.postDataReload()));
    }

    private void registerWaxedContent(Content content) {
        CopperBlockHelper.dewaxing().forEach((waxed, dewaxed) -> {
            BlockContentRegistries.WAXABLE.put(dewaxed, new ReversibleBlockEntry(waxed, true));
        });
        CopperBlockHelper.oxidisation().forEach((before, next) -> {
            BlockContentRegistries.OXIDIZABLE.put(before, new ReversibleBlockEntry(next, true));
        });
    }

    private void registerBarrelRenderLayers(Content content) {
        for (NamedValue<BarrelBlock> block : content.getBarrelBlocks()) {
            BlockRenderLayerMap.put(RenderType.cutoutMipped(), block.getValue());
        }
    }
}
