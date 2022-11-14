package ellemes.expandedstorage.fabric;

import ellemes.expandedstorage.common.block.OpenableBlock;
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
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
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

        CreativeModeTab group = FabricItemGroupBuilder.build(Utils.id("tab"), () -> new ItemStack(Registry.ITEM.get(Utils.id("netherite_chest")))); // Fabric API is dumb.
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
        Map<String, OpenableBlock> blocks = content
                .getBlocks().stream()
                .map(it -> Map.entry(it.getName().getPath(), it.getValue()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        OxidizableBlocksRegistry.registerOxidizableBlockPair(blocks.get("copper_chest"), blocks.get("exposed_copper_chest"));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(blocks.get("exposed_copper_chest"), blocks.get("weathered_copper_chest"));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(blocks.get("weathered_copper_chest"), blocks.get("oxidized_copper_chest"));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(blocks.get("old_copper_chest"), blocks.get("old_exposed_copper_chest"));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(blocks.get("old_exposed_copper_chest"), blocks.get("old_weathered_copper_chest"));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(blocks.get("old_weathered_copper_chest"), blocks.get("old_oxidized_copper_chest"));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(blocks.get("copper_barrel"), blocks.get("exposed_copper_barrel"));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(blocks.get("exposed_copper_barrel"), blocks.get("weathered_copper_barrel"));
        OxidizableBlocksRegistry.registerOxidizableBlockPair(blocks.get("weathered_copper_barrel"), blocks.get("oxidized_copper_barrel"));

        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("copper_chest"), blocks.get("waxed_copper_chest"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("exposed_copper_chest"), blocks.get("waxed_exposed_copper_chest"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("weathered_copper_chest"), blocks.get("waxed_weathered_copper_chest"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("oxidized_copper_chest"), blocks.get("waxed_oxidized_copper_chest"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("old_copper_chest"), blocks.get("waxed_old_copper_chest"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("old_exposed_copper_chest"), blocks.get("waxed_old_exposed_copper_chest"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("old_weathered_copper_chest"), blocks.get("waxed_old_weathered_copper_chest"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("old_oxidized_copper_chest"), blocks.get("waxed_old_oxidized_copper_chest"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("copper_barrel"), blocks.get("waxed_copper_barrel"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("exposed_copper_barrel"), blocks.get("waxed_exposed_copper_barrel"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("weathered_copper_barrel"), blocks.get("waxed_weathered_copper_barrel"));
        OxidizableBlocksRegistry.registerWaxableBlockPair(blocks.get("oxidized_copper_barrel"), blocks.get("waxed_oxidized_copper_barrel"));
    }

    private void registerBarrelRenderLayers(Content content) {
        for (NamedValue<BarrelBlock> block : content.getBarrelBlocks()) {
            BlockRenderLayerMap.INSTANCE.putBlock(block.getValue(), RenderType.cutoutMipped());
        }
    }
}
