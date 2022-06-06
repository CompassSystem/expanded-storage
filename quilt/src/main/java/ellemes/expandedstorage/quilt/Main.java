package ellemes.expandedstorage.quilt;

import ellemes.expandedstorage.Common;
import ellemes.expandedstorage.TagReloadListener;
import ellemes.expandedstorage.Utils;
import ellemes.expandedstorage.api.EsChestType;
import ellemes.expandedstorage.block.AbstractChestBlock;
import ellemes.expandedstorage.block.BarrelBlock;
import ellemes.expandedstorage.block.ChestBlock;
import ellemes.expandedstorage.block.MiniChestBlock;
import ellemes.expandedstorage.block.OpenableBlock;
import ellemes.expandedstorage.block.entity.BarrelBlockEntity;
import ellemes.expandedstorage.block.entity.ChestBlockEntity;
import ellemes.expandedstorage.block.entity.MiniChestBlockEntity;
import ellemes.expandedstorage.block.entity.OldChestBlockEntity;
import ellemes.expandedstorage.block.entity.extendable.OpenableBlockEntity;
import ellemes.expandedstorage.block.misc.BasicLockable;
import ellemes.expandedstorage.block.misc.DoubleItemAccess;
import ellemes.expandedstorage.client.ChestBlockEntityRenderer;
import ellemes.expandedstorage.quilt.compat.carrier.CarrierCompat;
import ellemes.expandedstorage.quilt.compat.htm.HTMLockable;
import ellemes.expandedstorage.registration.NamedValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.Version;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.item.group.api.QuiltItemGroup;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//@SuppressWarnings("deprecation")
public final class Main implements ModInitializer {
    private boolean isCarrierCompatEnabled = false;

    @SuppressWarnings({"UnstableApiUsage"})
    private static Storage<ItemVariant> getItemAccess(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @SuppressWarnings("unused") Direction context) {
        if (blockEntity instanceof OldChestBlockEntity entity) {
            DoubleItemAccess access = entity.getItemAccess();
            EsChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (access.hasCachedAccess() || type == EsChestType.SINGLE) {
                //noinspection unchecked
                return (Storage<ItemVariant>) access.get();
            }
            if (world.getBlockEntity(pos.relative(AbstractChestBlock.getDirectionToAttached(type, facing))) instanceof OldChestBlockEntity otherEntity) {
                DoubleItemAccess otherAccess = otherEntity.getItemAccess();
                if (otherAccess.hasCachedAccess()) {
                    //noinspection unchecked
                    return (Storage<ItemVariant>) otherAccess.get();
                }
                DoubleItemAccess first, second;
                if (AbstractChestBlock.getBlockType(type) == DoubleBlockCombiner.BlockType.FIRST) {
                    first = access;
                    second = otherAccess;
                } else {
                    first = otherAccess;
                    second = access;
                }
                first.setOther(second);
                //noinspection unchecked
                return (Storage<ItemVariant>) first.get();
            }

        } else if (blockEntity instanceof OpenableBlockEntity entity) {
            //noinspection unchecked
            return (Storage<ItemVariant>) entity.getItemAccess().get();
        }
        return null;
    }

    @Override
    public void onInitialize(ModContainer mod) {
        if (QuiltLoader.isModLoaded("quilt_loader")) {
            isCarrierCompatEnabled = QuiltLoader.getModContainer("carrier").map(it -> {
                Version carrierVersion = it.metadata().version();
                if (carrierVersion.isSemantic()) {
                    return carrierVersion.semantic().compareTo(Version.of("1.8.0").semantic()) > 0;
                }
                return false;
            }).orElse(false);

            CreativeModeTab group = QuiltItemGroup.builder(Utils.id("tab")).icon(() -> new ItemStack(Registry.ITEM.get(Utils.id("netherite_chest")))).build();
            boolean isClient = MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT;
            TagReloadListener tagReloadListener = new TagReloadListener();
            Common.constructContent(GenericItemAccess::new, QuiltLoader.isModLoaded("htm") ? HTMLockable::new : BasicLockable::new, group, isClient, tagReloadListener, this::registerContent,
                    /*Base*/ true,
                    /*Chest*/ TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("c", "wooden_chests")), BlockItem::new, ChestItemAccess::new,
                    /*Old Chest*/
                    /*Barrel*/ TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("c", "wooden_barrels")),
                    /*Mini Chest*/ BlockItem::new);

            ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(((server, resourceManager, error) -> tagReloadListener.postDataReload()));
        } else {
            LoggerFactory.getLogger(Utils.MOD_ID).warn("Please use Expanded Storage for Fabric instead.");
            System.exit(0);
        }
    }

    private void registerContent(List<ResourceLocation> stats, List<NamedValue<Item>> baseItems,
                                 List<NamedValue<ChestBlock>> chestBlocks, List<NamedValue<BlockItem>> chestItems, NamedValue<BlockEntityType<ChestBlockEntity>> chestBlockEntityType,
                                 List<NamedValue<AbstractChestBlock>> oldChestBlocks, List<NamedValue<BlockItem>> oldChestItems, NamedValue<BlockEntityType<OldChestBlockEntity>> oldChestBlockEntityType,
                                 List<NamedValue<BarrelBlock>> barrelBlocks, List<NamedValue<BlockItem>> barrelItems, NamedValue<BlockEntityType<BarrelBlockEntity>> barrelBlockEntityType,
                                 List<NamedValue<MiniChestBlock>> miniChestBlocks, List<NamedValue<BlockItem>> miniChestItems, NamedValue<BlockEntityType<MiniChestBlockEntity>> miniChestBlockEntityType) {
        for (ResourceLocation stat : stats) {
            Registry.register(Registry.CUSTOM_STAT, stat, stat);
        }

        List<NamedValue<? extends OpenableBlock>> allBlocks = new ArrayList<>();
        allBlocks.addAll(chestBlocks);
        allBlocks.addAll(oldChestBlocks);
        allBlocks.addAll(barrelBlocks);
        allBlocks.addAll(miniChestBlocks);
        Common.iterateNamedList(allBlocks, (name, value) -> {
            Registry.register(Registry.BLOCK, name, value);
            Common.registerTieredBlock(value);
        });

        ItemStorage.SIDED.registerForBlocks(Main::getItemAccess, allBlocks.stream().map(NamedValue::getValue).toArray(OpenableBlock[]::new));

        if (isCarrierCompatEnabled) {
            for (NamedValue<ChestBlock> block : chestBlocks) {
                CarrierCompat.registerChestBlock(block.getValue());
            }

            for (NamedValue<AbstractChestBlock> block : oldChestBlocks) {
                CarrierCompat.registerOldChestBlock(block.getValue());
            }
        }

        List<NamedValue<? extends Item>> allItems = new ArrayList<>();
        allItems.addAll(baseItems);
        allItems.addAll(chestItems);
        allItems.addAll(oldChestItems);
        allItems.addAll(barrelItems);
        allItems.addAll(miniChestItems);
        Common.iterateNamedList(allItems, (name, value) -> Registry.register(Registry.ITEM, name, value));

        Registry.register(Registry.BLOCK_ENTITY_TYPE, chestBlockEntityType.getName(), chestBlockEntityType.getValue());
        Registry.register(Registry.BLOCK_ENTITY_TYPE, oldChestBlockEntityType.getName(), oldChestBlockEntityType.getValue());
        Registry.register(Registry.BLOCK_ENTITY_TYPE, barrelBlockEntityType.getName(), barrelBlockEntityType.getValue());
        Registry.register(Registry.BLOCK_ENTITY_TYPE, miniChestBlockEntityType.getName(), miniChestBlockEntityType.getValue());

        if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
            Main.Client.registerChestTextures(chestBlocks.stream().map(NamedValue::getName).collect(Collectors.toList()));
            Main.Client.registerItemRenderers(chestItems);
            for (NamedValue<BarrelBlock> block : barrelBlocks) {
                BlockRenderLayerMap.put(RenderType.cutoutMipped(), block.getValue());
            }
        }
    }

    private static class Client {
        public static void registerChestTextures(List<ResourceLocation> blocks) {
            ClientSpriteRegistryCallback.event(Sheets.CHEST_SHEET).register((atlasTexture, registry) -> {
                for (ResourceLocation texture : Common.getChestTextures(blocks)) registry.register(texture);
            });
            BlockEntityRendererRegistry.register(Common.getChestBlockEntityType(), ChestBlockEntityRenderer::new);
        }

        public static void registerItemRenderers(List<NamedValue<BlockItem>> items) {
            for (NamedValue<BlockItem> item : items) {
                ChestBlockEntity renderEntity = Common.getChestBlockEntityType().create(BlockPos.ZERO, item.getValue().getBlock().defaultBlockState());
                BuiltinItemRendererRegistry.INSTANCE.register(item.getValue(), (itemStack, transform, stack, source, light, overlay) ->
                        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(renderEntity, stack, source, light, overlay));
            }
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.SINGLE_LAYER, ChestBlockEntityRenderer::createSingleBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.LEFT_LAYER, ChestBlockEntityRenderer::createLeftBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.RIGHT_LAYER, ChestBlockEntityRenderer::createRightBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.TOP_LAYER, ChestBlockEntityRenderer::createTopBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.BOTTOM_LAYER, ChestBlockEntityRenderer::createBottomBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.FRONT_LAYER, ChestBlockEntityRenderer::createFrontBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.BACK_LAYER, ChestBlockEntityRenderer::createBackBodyLayer);
        }
    }
}