package ellemes.expandedstorage.fabric;

import ellemes.expandedstorage.common.block.misc.CopperBlockHelper;
import ellemes.expandedstorage.common.misc.TagReloadListener;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.block.BarrelBlock;
import ellemes.expandedstorage.common.registration.Content;
import ellemes.expandedstorage.common.registration.ContentConsumer;
import ellemes.expandedstorage.common.registration.NamedValue;
import ellemes.expandedstorage.thread.ThreadMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public final class FabricMain implements ModInitializer {
    @Override
    public void onInitialize() {
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

        // todo: sort generateDisplayItems manually, move to common
        CreativeModeTab group = new FabricItemGroup(Utils.id("tab")) {
            @Override
            public ItemStack makeIcon() {
                return Registry.ITEM.get(Utils.id("netherite_chest")).getDefaultInstance();
            }

            @Override
            protected void generateDisplayItems(FeatureFlagSet featureFlagSet, Output output) {
                output.acceptAll(Registry.ITEM.entrySet().stream().filter((entry) -> {
                    return entry.getKey().location().getNamespace().equals(Utils.MOD_ID);
                }).map(Map.Entry::getValue).map(Item::getDefaultInstance).collect(Collectors.toSet()));
            }
        };
        boolean isClient = fabricLoader.getEnvironmentType() == EnvType.CLIENT;
        TagReloadListener tagReloadListener = new TagReloadListener();
        ThreadMain.constructContent(
                fabricLoader.isModLoaded("htm"), group, isClient, tagReloadListener,
                ((ContentConsumer) ThreadMain::registerContent)
                        .andThenIf(isCarrierCompatEnabled, ThreadMain::registerCarrierCompat)
                        .andThenIf(isClient, ThreadMain::registerClientStuff)
                        .andThenIf(isClient, this::registerBarrelRenderLayers)
                        .andThen(this::registerOxidisableAndWaxableBlocks)
        );
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> tagReloadListener.postDataReload());
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
