package compasses.expandedstorage.impl;

import compasses.expandedstorage.impl.block.OpenableBlock;
import compasses.expandedstorage.impl.block.misc.BasicLockable;
import compasses.expandedstorage.impl.block.misc.CopperBlockHelper;
import compasses.expandedstorage.impl.block.strategies.ItemAccess;
import compasses.expandedstorage.impl.block.strategies.Lockable;
import compasses.expandedstorage.impl.inventory.handler.AbstractHandler;
import compasses.expandedstorage.impl.item.ChestMinecartItem;
import compasses.expandedstorage.impl.misc.Utils;
import compasses.expandedstorage.impl.recipe.BlockConversionRecipe;
import compasses.expandedstorage.impl.recipe.ConversionRecipeReloadListener;
import compasses.expandedstorage.impl.recipe.EntityConversionRecipe;
import compasses.expandedstorage.impl.registration.NamedValue;
import compasses.expandedstorage.impl.block.misc.ChestItemAccess;
import compasses.expandedstorage.impl.block.misc.GenericItemAccess;
import compasses.expandedstorage.impl.compat.htm.HTMLockable;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class FabricMain implements ModInitializer {
    public static final ResourceLocation UPDATE_RECIPES_ID = Utils.id("update_conversion_recipes");

    public static MinecraftServer serverInstance;
    private static Supplier<CommonMain.Initializer> temporaryInitializerSupplier;

    public static Path getLocalConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(Utils.MOD_ID);
    }

    public static ExtendedScreenHandlerType<AbstractHandler> getScreenHandlerType() {
        return menuType;
    }

    private static final ExtendedScreenHandlerType<AbstractHandler> menuType;

    static {
        menuType = Registry.register(BuiltInRegistries.MENU, Utils.HANDLER_TYPE_ID, new ExtendedScreenHandlerType<>(AbstractHandler::createClientMenu));
    }

    @Override
    public void onInitialize() {
        FabricLoader loader = FabricLoader.getInstance();

        CommonMain.Initializer initializer = new CommonMain.Initializer();

        Supplier<Lockable> lockable = loader.isModLoaded("htm") ? HTMLockable::new : BasicLockable::new;

        initializer.commonInit();
        initializer.baseInit(true);
        initializer.chestInit(lockable, BlockItem::new, ChestItemAccess::new, ChestMinecartItem::new);
        initializer.oldChestInit(lockable, ChestItemAccess::new);
        initializer.commonChestInit();
        initializer.barrelInit(GenericItemAccess::new, lockable, ConventionalBlockTags.WOODEN_BARRELS);
        initializer.miniStorageBlockInit(GenericItemAccess::new, lockable, BlockItem::new);

        FabricMain.registerContent(initializer);

        registerOxidisableAndWaxableBlocks();

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

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> serverInstance = null);
    }

    public static void registerContent(CommonMain.Initializer initializer) {
        for (ResourceLocation stat : initializer.stats) {
            Registry.register(BuiltInRegistries.CUSTOM_STAT, stat, stat);
        }

        CommonMain.iterateNamedList(initializer.getBlocks(), (name, value) -> {
            Registry.register(BuiltInRegistries.BLOCK, name, value);
        });

        //noinspection UnstableApiUsage
        ItemStorage.SIDED.registerForBlocks(FabricMain::getItemAccess, initializer.getBlocks().stream().map(NamedValue::getValue).toArray(OpenableBlock[]::new));

        CommonMain.iterateNamedList(initializer.getItems(), (name, value) -> Registry.register(BuiltInRegistries.ITEM, name, value));

        CommonMain.iterateNamedList(initializer.getEntityTypes(), (name, value) -> {
            Registry.register(BuiltInRegistries.ENTITY_TYPE, name, value);
        });

        FabricMain.registerBlockEntity(initializer.getChestBlockEntityType());
        FabricMain.registerBlockEntity(initializer.getOldChestBlockEntityType());
        FabricMain.registerBlockEntity(initializer.getBarrelBlockEntityType());
        FabricMain.registerBlockEntity(initializer.getMiniStorageBlockEntityType());

        temporaryInitializerSupplier = () -> initializer;
    }

    private static <T extends BlockEntity> void registerBlockEntity(NamedValue<BlockEntityType<T>> blockEntityType) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityType.getName(), blockEntityType.getValue());
    }

    private void registerOxidisableAndWaxableBlocks() {
        CopperBlockHelper.oxidisation().forEach(OxidizableBlocksRegistry::registerOxidizableBlockPair);
        CopperBlockHelper.dewaxing().inverse().forEach(OxidizableBlocksRegistry::registerWaxableBlockPair);
    }

    @SuppressWarnings({"UnstableApiUsage"})
    public static Storage<ItemVariant> getItemAccess(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @SuppressWarnings("unused") Direction context) {
        return CommonMain.getItemAccess(level, pos, state, blockEntity).map(ItemAccess::get).orElse(null);
    }

    public static CommonMain.Initializer getInitializeForClient() {
        CommonMain.Initializer initializer = temporaryInitializerSupplier.get();
        temporaryInitializerSupplier = null;

        return initializer;
    }

    public static void sendConversionRecipesToClient(@Nullable ServerPlayer target, List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeCollection(blockRecipes, (b, recipe) -> recipe.writeToBuffer(b));
        buffer.writeCollection(entityRecipes, (b, recipe) -> recipe.writeToBuffer(b));
        if (target == null) {
            for (ServerPlayer player : FabricMain.serverInstance.getPlayerList().getPlayers()) {
                sendPacket(player, FabricMain.UPDATE_RECIPES_ID, buffer);
            }
        } else {
            sendPacket(target, FabricMain.UPDATE_RECIPES_ID, buffer);
        }
    }

    private static void sendPacket(ServerPlayer player, ResourceLocation packetId, FriendlyByteBuf buffer) {
        ServerPlayNetworking.send(player, packetId, buffer); // canSend doesn't work :think:
    }
}
