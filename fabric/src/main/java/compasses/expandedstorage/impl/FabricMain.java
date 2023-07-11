package compasses.expandedstorage.impl;

import compasses.expandedstorage.impl.block.OpenableBlock;
import compasses.expandedstorage.impl.block.misc.BasicLockable;
import compasses.expandedstorage.impl.block.misc.CopperBlockHelper;
import compasses.expandedstorage.impl.block.strategies.Lockable;
import compasses.expandedstorage.impl.inventory.handler.AbstractHandler;
import compasses.expandedstorage.impl.misc.Utils;
import compasses.expandedstorage.impl.recipe.BlockConversionRecipe;
import compasses.expandedstorage.impl.recipe.ConversionRecipeReloadListener;
import compasses.expandedstorage.impl.recipe.EntityConversionRecipe;
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
import net.fabricmc.loader.api.FabricLoader;
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
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class FabricMain implements ModInitializer {
    public static final ResourceLocation UPDATE_RECIPES_ID = Utils.id("update_conversion_recipes");

    public static MinecraftServer serverInstance;
    private static Supplier<CommonMain.Initializer> temporaryInitializerSupplier;

    private static final ExtendedScreenHandlerType<AbstractHandler> menuType;

    static {
        menuType = Registry.register(BuiltInRegistries.MENU, Utils.id("handler_type"), new ExtendedScreenHandlerType<>(AbstractHandler::createClientMenu));
    }

    @Override
    public void onInitialize() {
        FabricLoader loader = FabricLoader.getInstance();

        CommonMain.Initializer initializer = new CommonMain.Initializer();

        Supplier<Lockable> lockable = loader.isModLoaded("htm") ? HTMLockable::new : BasicLockable::new;

        initializer.commonInit();
        initializer.baseInit(true);
        initializer.chestInit(lockable);
        initializer.oldChestInit(lockable);
        initializer.commonChestInit();
        initializer.barrelInit(lockable, ConventionalBlockTags.WOODEN_BARRELS);
        initializer.miniStorageBlockInit(lockable);

        //noinspection UnstableApiUsage
        ItemStorage.SIDED.registerForBlocks(CommonMain::getItemAccess, initializer.getBlocks().toArray(OpenableBlock[]::new));

        temporaryInitializerSupplier = () -> initializer;

        registerOxidisableAndWaxableBlocks();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Utils.id("tab"), FabricItemGroup
                .builder()
                .icon(() -> BuiltInRegistries.ITEM.get(Utils.id("netherite_chest")).getDefaultInstance())
                .displayItems((itemDisplayParameters, output) -> {
                    CommonMain.generateDisplayItems(itemDisplayParameters, stack -> {
                        output.accept(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                    });
                })
                .title(Component.translatable("itemGroup.expandedstorage.tab")).build()
        );

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> CommonMain.onPlayerUseEntity(world, player, hand, entity));

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

    private void registerOxidisableAndWaxableBlocks() {
        CopperBlockHelper.oxidisation().forEach(OxidizableBlocksRegistry::registerOxidizableBlockPair);
        CopperBlockHelper.dewaxing().inverse().forEach(OxidizableBlocksRegistry::registerWaxableBlockPair);
    }

    public static ExtendedScreenHandlerType<AbstractHandler> getScreenHandlerType() {
        return menuType;
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
