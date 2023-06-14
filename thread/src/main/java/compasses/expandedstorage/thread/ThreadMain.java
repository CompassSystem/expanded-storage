package compasses.expandedstorage.thread;

import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.block.ChestBlock;
import compasses.expandedstorage.common.block.OpenableBlock;
import compasses.expandedstorage.common.block.misc.BasicLockable;
import compasses.expandedstorage.common.block.strategies.ItemAccess;
import compasses.expandedstorage.common.block.strategies.Lockable;
import compasses.expandedstorage.common.item.ChestMinecartItem;
import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.common.recipe.ConversionRecipeReloadListener;
import compasses.expandedstorage.common.registration.NamedValue;
import compasses.expandedstorage.thread.block.misc.ChestItemAccess;
import compasses.expandedstorage.thread.block.misc.GenericItemAccess;
import compasses.expandedstorage.thread.compat.carrier.CarrierCompat;
import compasses.expandedstorage.thread.compat.htm.HTMLockable;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ThreadMain {
    public static final ResourceLocation UPDATE_RECIPES_ID = Utils.id("update_conversion_recipes");
    private static Supplier<CommonMain.Initializer> temporaryInitializerSupplier;

    @SuppressWarnings({"UnstableApiUsage"})
    public static Storage<ItemVariant> getItemAccess(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @SuppressWarnings("unused") Direction context) {
        //noinspection unchecked
        return (Storage<ItemVariant>) CommonMain.getItemAccess(level, pos, state, blockEntity).map(ItemAccess::get).orElse(null);
    }

    public static CommonMain.Initializer getInitializeForClient() {
        CommonMain.Initializer initializer = temporaryInitializerSupplier.get();
        temporaryInitializerSupplier = null;

        return initializer;
    }

    public static void constructContent(ThreadCommonHelper helper, boolean htmPresent, boolean isClient, String modTargetPlatform, String userPlatform, Consumer<CommonMain.Initializer> registerExtra) {
        CommonMain.Initializer initializer = new CommonMain.Initializer();

        Supplier<Lockable> lockable = htmPresent ? HTMLockable::new : BasicLockable::new;

        initializer.commonInit(helper, modTargetPlatform, userPlatform);
        initializer.baseInit(true);
        initializer.chestInit(isClient, lockable, BlockItem::new, ChestItemAccess::new, ChestMinecartItem::new);
        initializer.oldChestInit(lockable, ChestItemAccess::new);
        initializer.commonChestInit();
        initializer.barrelInit(GenericItemAccess::new, lockable, ConventionalBlockTags.WOODEN_BARRELS);
        initializer.miniStorageBlockInit(isClient, GenericItemAccess::new, lockable, BlockItem::new);

        registerContent(initializer);

        registerExtra.accept(initializer);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Utils.id("tab"),
                FabricItemGroup.builder()
                               .icon(() -> BuiltInRegistries.ITEM.get(Utils.id("netherite_chest")).getDefaultInstance())
                               .displayItems((itemDisplayParameters, output) -> {
                                   CommonMain.generateDisplayItems(itemDisplayParameters, stack -> {
                                       output.accept(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                                   });
                               })
                               .title(Component.translatable("itemGroup.expandedstorage.tab")).build());

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> CommonMain.interactWithEntity(world, player, hand, entity));

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            private final PreparableReloadListener base = new ConversionRecipeReloadListener();

            @Override
            public ResourceLocation getFabricId() {
                return Utils.id("conversion_recipe_loader");
            }

            @NotNull
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller filler1, ProfilerFiller filler2, Executor executor1, Executor executor2) {
                return base.reload(barrier, manager, filler1, filler2, executor1, executor2);
            }
        });
    }

    public static void registerContent(CommonMain.Initializer initializer) {
        for (ResourceLocation stat : initializer.stats) {
            Registry.register(BuiltInRegistries.CUSTOM_STAT, stat, stat);
        }

        CommonMain.iterateNamedList(initializer.getBlocks(), (name, value) -> {
            Registry.register(BuiltInRegistries.BLOCK, name, value);
        });

        //noinspection UnstableApiUsage
        ItemStorage.SIDED.registerForBlocks(ThreadMain::getItemAccess, initializer.getBlocks().stream().map(NamedValue::getValue).toArray(OpenableBlock[]::new));

        CommonMain.iterateNamedList(initializer.getItems(), (name, value) -> Registry.register(BuiltInRegistries.ITEM, name, value));

        CommonMain.iterateNamedList(initializer.getEntityTypes(), (name, value) -> {
            Registry.register(BuiltInRegistries.ENTITY_TYPE, name, value);
        });

        ThreadMain.registerBlockEntity(initializer.getChestBlockEntityType());
        ThreadMain.registerBlockEntity(initializer.getOldChestBlockEntityType());
        ThreadMain.registerBlockEntity(initializer.getBarrelBlockEntityType());
        ThreadMain.registerBlockEntity(initializer.getMiniStorageBlockEntityType());

        temporaryInitializerSupplier = () -> initializer;
    }

    private static <T extends BlockEntity> void registerBlockEntity(NamedValue<BlockEntityType<T>> blockEntityType) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityType.getName(), blockEntityType.getValue());
    }

    public static void registerCarrierCompat(CommonMain.Initializer initializer) {
        for (NamedValue<? extends OpenableBlock> block : initializer.getBlocks()) {
            if (block.getValue() instanceof ChestBlock chestBlock) {
                CarrierCompat.registerChestBlock(chestBlock);
            } else {
                CarrierCompat.registerOpenableBlock(block.getValue());
            }
        }
    }
}
