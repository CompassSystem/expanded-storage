package ellemes.expandedstorage.thread;

import com.google.common.base.Suppliers;
import ellemes.expandedstorage.common.CommonMain;
import ellemes.expandedstorage.common.block.strategies.ItemAccess;
import ellemes.expandedstorage.common.item.ChestMinecartItem;
import ellemes.expandedstorage.common.misc.TagReloadListener;
import ellemes.expandedstorage.common.block.AbstractChestBlock;
import ellemes.expandedstorage.common.block.ChestBlock;
import ellemes.expandedstorage.common.block.OpenableBlock;
import ellemes.expandedstorage.common.block.entity.ChestBlockEntity;
import ellemes.expandedstorage.common.block.misc.BasicLockable;
import ellemes.expandedstorage.common.client.ChestBlockEntityRenderer;
import ellemes.expandedstorage.common.entity.ChestMinecart;
import ellemes.expandedstorage.common.misc.TieredObject;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.registration.Content;
import ellemes.expandedstorage.common.registration.ContentConsumer;
import ellemes.expandedstorage.common.registration.NamedValue;
import ellemes.expandedstorage.thread.block.misc.ChestItemAccess;
import ellemes.expandedstorage.thread.block.misc.GenericItemAccess;
import ellemes.expandedstorage.thread.compat.carrier.CarrierCompat;
import ellemes.expandedstorage.thread.compat.htm.HTMLockable;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ThreadMain {
    @SuppressWarnings({"UnstableApiUsage"})
    public static Storage<ItemVariant> getItemAccess(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @SuppressWarnings("unused") Direction context) {
        //noinspection unchecked
        return (Storage<ItemVariant>) CommonMain.getItemAccess(level, pos, state, blockEntity).map(ItemAccess::get).orElse(null);
    }

    public static void constructContent(boolean htmPresent, boolean isClient, TagReloadListener tagReloadListener, ContentConsumer contentRegistrationConsumer) {
        CreativeModeTab group = FabricItemGroup.builder(Utils.id("tab")).icon(() -> BuiltInRegistries.ITEM.get(Utils.id("netherite_chest")).getDefaultInstance())
                .displayItems((featureFlagSet, output, someBoolean) ->  {
                    CommonMain.generateDisplayItems(featureFlagSet, stack -> {
                        output.accept(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                    });
                }).build();

        CommonMain.constructContent(GenericItemAccess::new, htmPresent ? HTMLockable::new : BasicLockable::new, isClient, tagReloadListener, contentRegistrationConsumer,
                /*Base*/ true,
                /*Chest*/ TagKey.create(Registries.BLOCK, new ResourceLocation("c", "wooden_chests")), BlockItem::new, ChestItemAccess::new,
                /*Minecart Chest*/ ChestMinecartItem::new,
                /*Old Chest*/
                /*Barrel*/ TagKey.create(Registries.BLOCK, new ResourceLocation("c", "wooden_barrels")),
                /*Mini Chest*/ BlockItem::new);

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> CommonMain.interactWithEntity(world, player, hand, entity));
    }

    public static void registerContent(Content content) {
        for (ResourceLocation stat : content.getStats()) {
            Registry.register(BuiltInRegistries.CUSTOM_STAT, stat, stat);
        }

        CommonMain.iterateNamedList(content.getBlocks(), (name, value) -> {
            Registry.register(BuiltInRegistries.BLOCK, name, value);
            CommonMain.registerTieredObject(value);
        });

        //noinspection UnstableApiUsage
        ItemStorage.SIDED.registerForBlocks(ThreadMain::getItemAccess, content.getBlocks().stream().map(NamedValue::getValue).toArray(OpenableBlock[]::new));

        CommonMain.iterateNamedList(content.getItems(), (name, value) -> Registry.register(BuiltInRegistries.ITEM, name, value));

        CommonMain.iterateNamedList(content.getEntityTypes(), (name, value) -> {
            Registry.register(BuiltInRegistries.ENTITY_TYPE, name, value);
            if (value instanceof TieredObject object) {
                CommonMain.registerTieredObject(object);
            }
        });

        ThreadMain.registerBlockEntity(content.getChestBlockEntityType());
        ThreadMain.registerBlockEntity(content.getOldChestBlockEntityType());
        ThreadMain.registerBlockEntity(content.getBarrelBlockEntityType());
        ThreadMain.registerBlockEntity(content.getMiniChestBlockEntityType());
    }

    private static <T extends BlockEntity> void registerBlockEntity(NamedValue<BlockEntityType<T>> blockEntityType) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityType.getName(), blockEntityType.getValue());
    }

    public static void registerCarrierCompat(Content content) {
        for (NamedValue<ChestBlock> block : content.getChestBlocks()) {
            CarrierCompat.registerChestBlock(block.getValue());
        }

        for (NamedValue<AbstractChestBlock> block : content.getOldChestBlocks()) {
            CarrierCompat.registerOldChestBlock(block.getValue());
        }
    }

    public static void registerClientStuff(Content content) {
        ThreadMain.Client.registerChestBlockEntityRenderer();
        ThreadMain.Client.registerItemRenderers(content.getChestItems());
        ThreadMain.Client.registerMinecartEntityRenderers(content.getChestMinecartEntityTypes());
        ThreadMain.Client.registerMinecartItemRenderers(content.getChestMinecartAndTypes());
    }

    public static class Client {
        public static void registerChestBlockEntityRenderer() {
            BlockEntityRendererRegistry.register(CommonMain.getChestBlockEntityType(), ChestBlockEntityRenderer::new);
        }

        public static void registerItemRenderers(List<NamedValue<BlockItem>> items) {
            for (NamedValue<BlockItem> item : items) {
                ChestBlockEntity renderEntity = CommonMain.getChestBlockEntityType().create(BlockPos.ZERO, item.getValue().getBlock().defaultBlockState());
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

        public static void registerMinecartEntityRenderers(List<NamedValue<EntityType<ChestMinecart>>> chestMinecartEntityTypes) {
            for (NamedValue<EntityType<ChestMinecart>> type : chestMinecartEntityTypes) {
                EntityRendererRegistry.register(type.getValue(), context -> new MinecartRenderer(context, ModelLayers.CHEST_MINECART));
            }
        }

        public static void registerMinecartItemRenderers(List<Map.Entry<NamedValue<ChestMinecartItem>, NamedValue<EntityType<ChestMinecart>>>> chestMinecartAndTypes) {
            for (var pair : chestMinecartAndTypes) {
                Supplier<ChestMinecart> renderEntity = Suppliers.memoize(() -> pair.getValue().getValue().create(Minecraft.getInstance().level));
                BuiltinItemRendererRegistry.INSTANCE.register(pair.getKey().getValue(), (itemStack, transform, stack, source, light, overlay) ->
                        Minecraft.getInstance().getEntityRenderDispatcher().render(renderEntity.get(), 0, 0, 0, 0, 0, stack, source, light));
            }
        }
    }
}
