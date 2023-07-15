package compasses.expandedstorage.impl;

import compasses.expandedstorage.api.EsChestType;
import compasses.expandedstorage.impl.block.AbstractChestBlock;
import compasses.expandedstorage.impl.block.OpenableBlock;
import compasses.expandedstorage.impl.block.entity.OldChestBlockEntity;
import compasses.expandedstorage.impl.block.entity.OpenableBlockEntity;
import compasses.expandedstorage.impl.block.misc.BasicLockable;
import compasses.expandedstorage.impl.block.misc.CopperBlockHelper;
import compasses.expandedstorage.impl.block.strategies.Lockable;
import compasses.expandedstorage.impl.inventory.handler.AbstractHandler;
import compasses.expandedstorage.impl.item.EntityInteractableItem;
import compasses.expandedstorage.impl.item.MutationMode;
import compasses.expandedstorage.impl.recipe.BlockConversionRecipe;
import compasses.expandedstorage.impl.recipe.ConversionRecipeManager;
import compasses.expandedstorage.impl.recipe.ConversionRecipeReloadListener;
import compasses.expandedstorage.impl.recipe.EntityConversionRecipe;
import compasses.expandedstorage.impl.compat.htm.HTMLockable;
import compasses.expandedstorage.impl.registration.ModItems;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FabricMain implements ModInitializer {
    public static final ResourceLocation UPDATE_RECIPES_ID = Utils.id("update_conversion_recipes");

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

        initializer.baseInit();
        initializer.chestInit(lockable);
        initializer.oldChestInit(lockable);
        initializer.barrelInit(lockable);
        initializer.miniStorageBlockInit(lockable);

        //noinspection UnstableApiUsage
        ItemStorage.SIDED.registerForBlocks(FabricMain::getItemAccess, initializer.getBlocks().toArray(OpenableBlock[]::new));

        temporaryInitializerSupplier = () -> initializer;

        registerOxidisableAndWaxableBlocks();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Utils.id("tab"), FabricItemGroup
                .builder()
                .icon(() -> BuiltInRegistries.ITEM.get(Utils.id("netherite_chest")).getDefaultInstance())
                .displayItems(FabricMain::generateDisplayItems)
                .title(Component.translatable("itemGroup.expandedstorage.tab")).build()
        );

        UseEntityCallback.EVENT.register(FabricMain::onPlayerUseEntity);

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ConversionRecipeReloadListener());

        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            if (channels.contains(FabricMain.UPDATE_RECIPES_ID)) {

                FabricMain.sendConversionRecipesToClient(ConversionRecipeManager.INSTANCE.getBlockRecipes(), ConversionRecipeManager.INSTANCE.getEntityRecipes(), handler.getPlayer());
            }
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Storage<ItemVariant> getItemAccess(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction direction) {
        if (blockEntity instanceof OldChestBlockEntity entity) {
            EsChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            if (entity.hasCachedTransferStorage() || type == EsChestType.SINGLE) {
                return entity.getTransferStorage();
            }

            if (level.getBlockEntity(pos.relative(AbstractChestBlock.getDirectionToAttached(type, facing))) instanceof OldChestBlockEntity otherEntity) {
                if (otherEntity.hasCachedTransferStorage()) {
                    return otherEntity.getTransferStorage();
                }

                OldChestBlockEntity first, second;

                if (AbstractChestBlock.getBlockType(type) == DoubleBlockCombiner.BlockType.FIRST) {
                    first = entity;
                    second = otherEntity;
                } else {
                    first = otherEntity;
                    second = entity;
                }

                first.setCachedTransferStorage(second);

                return first.getTransferStorage();
            }
        } else if (blockEntity instanceof OpenableBlockEntity entity) {
            return entity.getTransferStorage();
        }

        return null;
    }

    public static InteractionResult onPlayerUseEntity(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hit) {
        if (player.isSpectator() || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        ItemStack handStack = player.getItemInHand(hand);

        if (!(handStack.getItem() instanceof EntityInteractableItem item)) {
            return InteractionResult.PASS;
        }

        if (player.getCooldowns().isOnCooldown(handStack.getItem())) {
            return InteractionResult.CONSUME;
        }

        InteractionResult result = item.es_interactEntity(level, entity, player, hand, handStack);

        if (result == InteractionResult.FAIL) {
            result = InteractionResult.CONSUME;
        }

        return result;
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

    public static void sendConversionRecipesToClient(List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes, ServerPlayer... players) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeCollection(blockRecipes, (b, recipe) -> recipe.writeToBuffer(b));
        buffer.writeCollection(entityRecipes, (b, recipe) -> recipe.writeToBuffer(b));

        for (ServerPlayer player : players) {
            ServerPlayNetworking.send(player, FabricMain.UPDATE_RECIPES_ID, buffer); // canSend doesn't work :think:
        }
    }

    @SuppressWarnings("unused")
    public static void generateDisplayItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
        Consumer<Item> wrap = item -> output.accept(item.getDefaultInstance());
        Consumer<Item> sparrowWrap = item -> {
            wrap.accept(item);

            ItemStack stack = new ItemStack(item);
            CompoundTag tag = new CompoundTag();
            CompoundTag blockStateTag = new CompoundTag();
            blockStateTag.putString("sparrow", "true");
            tag.put("BlockStateTag", blockStateTag);
            stack.setTag(tag);
            output.accept(stack);
        };

        for (MutationMode mode : MutationMode.values()) {
            ItemStack stack = new ItemStack(ModItems.STORAGE_MUTATOR);
            CompoundTag tag = new CompoundTag();
            tag.putByte("mode", mode.toByte());
            stack.setTag(tag);
            output.accept(stack);
        }

        {
            ItemStack sparrowMutator = new ItemStack(ModItems.STORAGE_MUTATOR);
            CompoundTag tag = new CompoundTag();
            tag.putByte("mode", MutationMode.SWAP_THEME.toByte());
            sparrowMutator.setTag(tag);
            sparrowMutator.setHoverName(Component.literal("Sparrow").withStyle(ChatFormatting.ITALIC));
            output.accept(sparrowMutator);
        }

        // todo: add lock stuff when finished and ported.
        wrap.accept(ModItems.WOOD_TO_COPPER_CONVERSION_KIT);
        wrap.accept(ModItems.WOOD_TO_IRON_CONVERSION_KIT);
        wrap.accept(ModItems.WOOD_TO_GOLD_CONVERSION_KIT);
        wrap.accept(ModItems.WOOD_TO_DIAMOND_CONVERSION_KIT);
        wrap.accept(ModItems.WOOD_TO_OBSIDIAN_CONVERSION_KIT);
        wrap.accept(ModItems.WOOD_TO_NETHERITE_CONVERSION_KIT);
        wrap.accept(ModItems.COPPER_TO_IRON_CONVERSION_KIT);
        wrap.accept(ModItems.COPPER_TO_GOLD_CONVERSION_KIT);
        wrap.accept(ModItems.COPPER_TO_DIAMOND_CONVERSION_KIT);
        wrap.accept(ModItems.COPPER_TO_OBSIDIAN_CONVERSION_KIT);
        wrap.accept(ModItems.COPPER_TO_NETHERITE_CONVERSION_KIT);
        wrap.accept(ModItems.IRON_TO_GOLD_CONVERSION_KIT);
        wrap.accept(ModItems.IRON_TO_DIAMOND_CONVERSION_KIT);
        wrap.accept(ModItems.IRON_TO_OBSIDIAN_CONVERSION_KIT);
        wrap.accept(ModItems.IRON_TO_NETHERITE_CONVERSION_KIT);
        wrap.accept(ModItems.GOLD_TO_DIAMOND_CONVERSION_KIT);
        wrap.accept(ModItems.GOLD_TO_OBSIDIAN_CONVERSION_KIT);
        wrap.accept(ModItems.GOLD_TO_NETHERITE_CONVERSION_KIT);
        wrap.accept(ModItems.DIAMOND_TO_OBSIDIAN_CONVERSION_KIT);
        wrap.accept(ModItems.DIAMOND_TO_NETHERITE_CONVERSION_KIT);
        wrap.accept(ModItems.OBSIDIAN_TO_NETHERITE_CONVERSION_KIT);
        wrap.accept(ModItems.WOOD_CHEST);
        wrap.accept(ModItems.PUMPKIN_CHEST);
        wrap.accept(ModItems.PRESENT);
        wrap.accept(ModItems.BAMBOO_CHEST);
        wrap.accept(ModItems.MOSS_CHEST);
        wrap.accept(ModItems.IRON_CHEST);
        wrap.accept(ModItems.GOLD_CHEST);
        wrap.accept(ModItems.DIAMOND_CHEST);
        wrap.accept(ModItems.OBSIDIAN_CHEST);
        wrap.accept(ModItems.NETHERITE_CHEST);
        wrap.accept(ModItems.WOOD_CHEST_MINECART);
        wrap.accept(ModItems.PUMPKIN_CHEST_MINECART);
        wrap.accept(ModItems.PRESENT_MINECART);
        wrap.accept(ModItems.BAMBOO_CHEST_MINECART);
        wrap.accept(ModItems.MOSS_CHEST_MINECART);
        wrap.accept(ModItems.IRON_CHEST_MINECART);
        wrap.accept(ModItems.GOLD_CHEST_MINECART);
        wrap.accept(ModItems.DIAMOND_CHEST_MINECART);
        wrap.accept(ModItems.OBSIDIAN_CHEST_MINECART);
        wrap.accept(ModItems.NETHERITE_CHEST_MINECART);
        wrap.accept(ModItems.OLD_WOOD_CHEST);
        wrap.accept(ModItems.OLD_IRON_CHEST);
        wrap.accept(ModItems.OLD_GOLD_CHEST);
        wrap.accept(ModItems.OLD_DIAMOND_CHEST);
        wrap.accept(ModItems.OLD_OBSIDIAN_CHEST);
        wrap.accept(ModItems.OLD_NETHERITE_CHEST);
        wrap.accept(ModItems.COPPER_BARREL);
        wrap.accept(ModItems.EXPOSED_COPPER_BARREL);
        wrap.accept(ModItems.WEATHERED_COPPER_BARREL);
        wrap.accept(ModItems.OXIDIZED_COPPER_BARREL);
        wrap.accept(ModItems.WAXED_COPPER_BARREL);
        wrap.accept(ModItems.WAXED_EXPOSED_COPPER_BARREL);
        wrap.accept(ModItems.WAXED_WEATHERED_COPPER_BARREL);
        wrap.accept(ModItems.WAXED_OXIDIZED_COPPER_BARREL);
        wrap.accept(ModItems.IRON_BARREL);
        wrap.accept(ModItems.GOLD_BARREL);
        wrap.accept(ModItems.DIAMOND_BARREL);
        wrap.accept(ModItems.OBSIDIAN_BARREL);
        wrap.accept(ModItems.NETHERITE_BARREL);

        sparrowWrap.accept(ModItems.VANILLA_WOOD_MINI_CHEST);
        sparrowWrap.accept(ModItems.WOOD_MINI_CHEST);
        sparrowWrap.accept(ModItems.PUMPKIN_MINI_CHEST);
        sparrowWrap.accept(ModItems.RED_MINI_PRESENT);
        sparrowWrap.accept(ModItems.WHITE_MINI_PRESENT);
        sparrowWrap.accept(ModItems.CANDY_CANE_MINI_PRESENT);
        sparrowWrap.accept(ModItems.GREEN_MINI_PRESENT);
        sparrowWrap.accept(ModItems.LAVENDER_MINI_PRESENT);
        sparrowWrap.accept(ModItems.PINK_AMETHYST_MINI_PRESENT);
        sparrowWrap.accept(ModItems.IRON_MINI_CHEST);
        sparrowWrap.accept(ModItems.GOLD_MINI_CHEST);
        sparrowWrap.accept(ModItems.DIAMOND_MINI_CHEST);
        sparrowWrap.accept(ModItems.OBSIDIAN_MINI_CHEST);
        sparrowWrap.accept(ModItems.NETHERITE_MINI_CHEST);
        sparrowWrap.accept(ModItems.MINI_BARREL);
        sparrowWrap.accept(ModItems.COPPER_MINI_BARREL);
        sparrowWrap.accept(ModItems.EXPOSED_COPPER_MINI_BARREL);
        sparrowWrap.accept(ModItems.WEATHERED_COPPER_MINI_BARREL);
        sparrowWrap.accept(ModItems.OXIDIZED_COPPER_MINI_BARREL);
        sparrowWrap.accept(ModItems.WAXED_COPPER_MINI_BARREL);
        sparrowWrap.accept(ModItems.WAXED_EXPOSED_COPPER_MINI_BARREL);
        sparrowWrap.accept(ModItems.WAXED_WEATHERED_COPPER_MINI_BARREL);
        sparrowWrap.accept(ModItems.WAXED_OXIDIZED_COPPER_MINI_BARREL);
        sparrowWrap.accept(ModItems.IRON_MINI_BARREL);
        sparrowWrap.accept(ModItems.GOLD_MINI_BARREL);
        sparrowWrap.accept(ModItems.DIAMOND_MINI_BARREL);
        sparrowWrap.accept(ModItems.OBSIDIAN_MINI_BARREL);
        sparrowWrap.accept(ModItems.NETHERITE_MINI_BARREL);
    }
}
