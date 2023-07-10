package compasses.expandedstorage.impl;

import com.google.common.collect.ImmutableSet;
import compasses.expandedstorage.api.EsChestType;
import compasses.expandedstorage.impl.block.AbstractChestBlock;
import compasses.expandedstorage.impl.block.BarrelBlock;
import compasses.expandedstorage.impl.block.ChestBlock;
import compasses.expandedstorage.impl.block.CopperBarrelBlock;
import compasses.expandedstorage.impl.block.CopperMiniStorageBlock;
import compasses.expandedstorage.impl.block.MiniStorageBlock;
import compasses.expandedstorage.impl.block.MossChestBlock;
import compasses.expandedstorage.impl.block.OpenableBlock;
import compasses.expandedstorage.impl.block.entity.BarrelBlockEntity;
import compasses.expandedstorage.impl.block.entity.ChestBlockEntity;
import compasses.expandedstorage.impl.block.entity.MiniStorageBlockEntity;
import compasses.expandedstorage.impl.block.entity.OldChestBlockEntity;
import compasses.expandedstorage.impl.block.entity.extendable.OpenableBlockEntity;
import compasses.expandedstorage.impl.block.strategies.Lockable;
import compasses.expandedstorage.impl.entity.ChestMinecart;
import compasses.expandedstorage.impl.inventory.OpenableInventory;
import compasses.expandedstorage.impl.item.BlockMutatorBehaviour;
import compasses.expandedstorage.impl.item.ChestMinecartItem;
import compasses.expandedstorage.impl.item.EntityInteractableItem;
import compasses.expandedstorage.impl.item.MutationMode;
import compasses.expandedstorage.impl.item.StorageConversionKit;
import compasses.expandedstorage.impl.item.StorageMutator;
import compasses.expandedstorage.impl.item.ToolUsageResult;
import compasses.expandedstorage.impl.misc.ScreenHandlerFactoryAdapter;
import compasses.expandedstorage.impl.misc.Tier;
import compasses.expandedstorage.impl.misc.Utils;
import compasses.expandedstorage.impl.recipe.BlockConversionRecipe;
import compasses.expandedstorage.impl.recipe.ConversionRecipeManager;
import compasses.expandedstorage.impl.registration.ModItems;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class CommonMain {
    public static final ResourceLocation BARREL_OBJECT_TYPE = Utils.id("barrel");
    public static final ResourceLocation CHEST_OBJECT_TYPE = Utils.id("chest");
    public static final ResourceLocation OLD_CHEST_OBJECT_TYPE = Utils.id("old_chest");
    public static final ResourceLocation MINI_STORAGE_OBJECT_TYPE = Utils.id("mini_chest");

    private static final Map<Map.Entry<Predicate<Block>, MutationMode>, BlockMutatorBehaviour> BLOCK_MUTATOR_BEHAVIOURS = new HashMap<>();

    private static BlockEntityType<ChestBlockEntity> chestBlockEntityType;
    private static BlockEntityType<OldChestBlockEntity> oldChestBlockEntityType;
    private static BlockEntityType<BarrelBlockEntity> barrelBlockEntityType;
    private static BlockEntityType<MiniStorageBlockEntity> miniStorageBlockEntityType;

    public static BlockEntityType<ChestBlockEntity> getChestBlockEntityType() {
        return chestBlockEntityType;
    }

    public static BlockEntityType<OldChestBlockEntity> getOldChestBlockEntityType() {
        return oldChestBlockEntityType;
    }

    public static BlockEntityType<BarrelBlockEntity> getBarrelBlockEntityType() {
        return barrelBlockEntityType;
    }

    public static BlockEntityType<MiniStorageBlockEntity> getMiniStorageBlockEntityType() {
        return miniStorageBlockEntityType;
    }

    private static void defineTierUpgradePath(HashSet<ResourceLocation> items, boolean wrapTooltipManually, Tier... tiers) {
        int numTiers = tiers.length;

        for (int fromIndex = 0; fromIndex < numTiers - 1; fromIndex++) {
            Tier fromTier = tiers[fromIndex];

            for (int toIndex = fromIndex + 1; toIndex < numTiers; toIndex++) {
                Tier toTier = tiers[toIndex];
                ResourceLocation itemId = Utils.id(fromTier.getId().getPath() + "_to_" + toTier.getId().getPath() + "_conversion_kit");

                if (!items.contains(itemId)) {
                    items.add(itemId);

                    Item.Properties settings = fromTier.getItemSettings()
                                                       .andThen(toTier.getItemSettings())
                                                       .apply(new Item.Properties().stacksTo(16));

                    Registry.register(BuiltInRegistries.ITEM, itemId, new StorageConversionKit(settings, fromTier.getId(), toTier.getId(), wrapTooltipManually));
                }
            }
        }
    }

    private static void registerMutationBehaviour(Predicate<Block> predicate, MutationMode mode, BlockMutatorBehaviour behaviour) {
        CommonMain.BLOCK_MUTATOR_BEHAVIOURS.put(Map.entry(predicate, mode), behaviour);
    }

    public static BlockMutatorBehaviour getBlockMutatorBehaviour(Block block, MutationMode mode) {
        for (Map.Entry<Map.Entry<Predicate<Block>, MutationMode>, BlockMutatorBehaviour> entry : CommonMain.BLOCK_MUTATOR_BEHAVIOURS.entrySet()) {
            Map.Entry<Predicate<Block>, MutationMode> pair = entry.getKey();

            if (pair.getValue() == mode && pair.getKey().test(block)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static class Initializer {
        private static class Tiers {
            static final Tier WOOD = new Tier(Utils.id("wood"), 27, UnaryOperator.identity(), UnaryOperator.identity());
            static final Tier COPPER = new Tier(Utils.id("copper"), 45, Block.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
            static final Tier IRON = new Tier(Utils.id("iron"), 54, Block.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
            static final Tier GOLD = new Tier(Utils.id("gold"), 81, Block.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
            static final Tier DIAMOND = new Tier(Utils.id("diamond"), 108, Block.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
            static final Tier OBSIDIAN = new Tier(Utils.id("obsidian"), 108, Block.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
            static final Tier NETHERITE = new Tier(Utils.id("netherite"), 135, Block.Properties::requiresCorrectToolForDrops, Item.Properties::fireResistant);
        }

        private static class Properties {
            static final BlockBehaviour.Properties WOOD = BlockBehaviour.Properties.of()
                                                                                   .mapColor(MapColor.WOOD)
                                                                                   .instrument(NoteBlockInstrument.BASS)
                                                                                   .strength(2.5f)
                                                                                   .sound(SoundType.WOOD)
                                                                                   .ignitedByLava();
            static final BlockBehaviour.Properties PUMPKIN = BlockBehaviour.Properties.of()
                                                                                      .mapColor(MapColor.COLOR_ORANGE)
                                                                                      .instrument(NoteBlockInstrument.DIDGERIDOO)
                                                                                      .strength(1.0F)
                                                                                      .sound(SoundType.WOOD);
            static final BlockBehaviour.Properties BAMBOO = BlockBehaviour.Properties.of()
                                                                                     .mapColor(MapColor.PLANT)
                                                                                     .strength(1)
                                                                                     .sound(SoundType.BAMBOO)
                                                                                     .ignitedByLava();
            static final BlockBehaviour.Properties MOSS = BlockBehaviour.Properties.of()
                                                                                   .mapColor(MapColor.COLOR_GREEN)
                                                                                   .strength(0.1F)
                                                                                   .sound(SoundType.MOSS);
            static final BlockBehaviour.Properties IRON = BlockBehaviour.Properties.of()
                                                                                   .mapColor(MapColor.METAL)
                                                                                   .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                                                                                   .strength(5, 6)
                                                                                   .sound(SoundType.METAL);
            static final BlockBehaviour.Properties GOLD = BlockBehaviour.Properties.of()
                                                                                   .mapColor(MapColor.GOLD)
                                                                                   .instrument(NoteBlockInstrument.BELL)
                                                                                   .strength(3, 6)
                                                                                   .sound(SoundType.METAL);
            static final BlockBehaviour.Properties DIAMOND = BlockBehaviour.Properties.of()
                                                                                      .mapColor(MapColor.DIAMOND)
                                                                                      .strength(5, 6)
                                                                                      .sound(SoundType.METAL);
            static final BlockBehaviour.Properties OBSIDIAN = BlockBehaviour.Properties.of()
                                                                                       .mapColor(MapColor.COLOR_BLACK)
                                                                                       .instrument(NoteBlockInstrument.BASEDRUM)
                                                                                       .strength(50, 1200);
            static final BlockBehaviour.Properties NETHERITE = BlockBehaviour.Properties.of()
                                                                                        .mapColor(MapColor.COLOR_BLACK)
                                                                                        .strength(50, 1200)
                                                                                        .sound(SoundType.NETHERITE_BLOCK);
        }

        private ResourceLocation stat(String id) {
            ResourceLocation statId = Utils.id(id);
            return Registry.register(BuiltInRegistries.CUSTOM_STAT, statId, statId);
        }

        public void commonInit() {
            CommonMain.registerMutationBehaviour(block -> true, MutationMode.SWAP_THEME, (useContext, level, state, pos, stack) -> {
                BlockConversionRecipe<?> recipe = ConversionRecipeManager.INSTANCE.getBlockRecipe(state, stack);
                if (recipe != null) {
                    return recipe.process(level, useContext.getPlayer(), stack, state, pos);
                }
                return ToolUsageResult.fail();
            });
        }

        public void baseInit(boolean manuallyWrapTooltips) {
            Registry.register(BuiltInRegistries.ITEM, Utils.id("storage_mutator"), new StorageMutator(new Item.Properties().stacksTo(1)));

            HashSet<ResourceLocation> baseItems = new HashSet<>();
            CommonMain.defineTierUpgradePath(baseItems, manuallyWrapTooltips, Tiers.WOOD, Tiers.COPPER, Tiers.IRON, Tiers.GOLD, Tiers.DIAMOND, Tiers.OBSIDIAN, Tiers.NETHERITE);
        }

        private final int tiers = 6;

        private final int chestTypes = tiers + 3; // tiers + cosmetic variants

        public final List<ChestBlock> chestBlocks = new ArrayList<>(chestTypes);
        public final List<BlockItem> chestItems = new ArrayList<>(chestTypes);
        public final List<EntityType<ChestMinecart>> chestMinecartEntityTypes = new ArrayList<>(chestTypes);
        public final List<ChestMinecartItem> chestMinecartItems = new ArrayList<>(chestTypes);

        private void createChest(ResourceLocation id, ResourceLocation stat, Tier tier, BlockBehaviour.Properties properties) {
            ChestBlock block;
            if (properties == Properties.MOSS) {
                block = new MossChestBlock(tier.getBlockSettings().apply(properties), stat, tier.getSlotCount());
            } else {
                block = new ChestBlock(tier.getBlockSettings().apply(properties), stat, tier.getSlotCount());
            }

            BlockItem item = new BlockItem(block, tier.getItemSettings().apply(new Item.Properties()));
            ResourceLocation cartId = new ResourceLocation(id.getNamespace(), id.getPath() + "_minecart");
            ChestMinecartItem cartItem = new ChestMinecartItem(new Item.Properties(), cartId);
            EntityType<ChestMinecart> cartEntityType = new EntityType<>((type, level) -> {
                return new ChestMinecart(type, level, cartItem, block);
            }, MobCategory.MISC, true, true, false, false, ImmutableSet.of(), EntityDimensions.scalable(0.98F, 0.7F), 8, 3, FeatureFlagSet.of());

            chestBlocks.add(Registry.register(BuiltInRegistries.BLOCK, id, block));
            chestItems.add(Registry.register(BuiltInRegistries.ITEM, id, item));
            chestMinecartEntityTypes.add(Registry.register(BuiltInRegistries.ENTITY_TYPE, cartId, cartEntityType));
            chestMinecartItems.add(Registry.register(BuiltInRegistries.ITEM, cartId, cartItem));
        }

        public void chestInit(Supplier<Lockable> lockable) {
            final Block.Properties presentSettings = Block.Properties.of().mapColor(state -> {
                EsChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
                if (type == EsChestType.SINGLE) return MapColor.COLOR_RED;
                else if (type == EsChestType.FRONT || type == EsChestType.BACK) return MapColor.PLANT;
                return MapColor.SNOW;
            }).strength(2.5f).sound(SoundType.WOOD);

            createChest(Utils.id("wood_chest"), stat("open_wood_chest"), Tiers.WOOD, Properties.WOOD);
            createChest(Utils.id("pumpkin_chest"), stat("open_pumpkin_chest"), Tiers.WOOD, Properties.PUMPKIN);
            createChest(Utils.id("present"), stat("open_present"), Tiers.WOOD, presentSettings);
            createChest(Utils.id("bamboo_chest"), stat("open_bamboo_chest"), Tiers.WOOD, Properties.BAMBOO);
            createChest(Utils.id("moss_chest"), stat("open_moss_chest"), Tiers.WOOD, Properties.MOSS);
            createChest(Utils.id("iron_chest"), stat("open_iron_chest"), Tiers.IRON, Properties.IRON);
            createChest(Utils.id("gold_chest"), stat("open_gold_chest"), Tiers.GOLD, Properties.GOLD);
            createChest(Utils.id("diamond_chest"), stat("open_diamond_chest"), Tiers.DIAMOND, Properties.DIAMOND);
            createChest(Utils.id("obsidian_chest"), stat("open_obsidian_chest"), Tiers.OBSIDIAN, Properties.OBSIDIAN);
            createChest(Utils.id("netherite_chest"), stat("open_netherite_chest"), Tiers.NETHERITE, Properties.NETHERITE);

            CommonMain.chestBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CommonMain.CHEST_OBJECT_TYPE, BlockEntityType.Builder.of((pos, state) -> new ChestBlockEntity(CommonMain.getChestBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), lockable), chestBlocks.toArray(ChestBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.CHEST_OBJECT_TYPE.toString())));
        }

        public final List<AbstractChestBlock> oldChestBlocks = new ArrayList<>(tiers);

        private void createOldChest(ResourceLocation id, ResourceLocation stat, Tier tier, BlockBehaviour.Properties settings) {
            AbstractChestBlock block = new AbstractChestBlock(tier.getBlockSettings().apply(settings), stat, tier.getSlotCount());
            BlockItem item = new BlockItem(block, tier.getItemSettings().apply(new Item.Properties()));

            oldChestBlocks.add(Registry.register(BuiltInRegistries.BLOCK, id, block));
            Registry.register(BuiltInRegistries.ITEM, id, item);
        }

        public void oldChestInit(Supplier<Lockable> lockable) {
            createOldChest(Utils.id("old_wood_chest"), stat("open_old_wood_chest"), Tiers.WOOD, Properties.WOOD);
            createOldChest(Utils.id("old_iron_chest"), stat("open_old_iron_chest"), Tiers.IRON, Properties.IRON);
            createOldChest(Utils.id("old_gold_chest"), stat("open_old_gold_chest"), Tiers.GOLD, Properties.GOLD);
            createOldChest(Utils.id("old_diamond_chest"), stat("open_old_diamond_chest"), Tiers.DIAMOND, Properties.DIAMOND);
            createOldChest(Utils.id("old_obsidian_chest"), stat("open_old_obsidian_chest"), Tiers.OBSIDIAN, Properties.OBSIDIAN);
            createOldChest(Utils.id("old_netherite_chest"), stat("open_old_netherite_chest"), Tiers.NETHERITE, Properties.NETHERITE);

            CommonMain.oldChestBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CommonMain.OLD_CHEST_OBJECT_TYPE, BlockEntityType.Builder.of((pos, state) -> new OldChestBlockEntity(CommonMain.getOldChestBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), lockable), oldChestBlocks.toArray(AbstractChestBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.OLD_CHEST_OBJECT_TYPE.toString())));
        }

        public void commonChestInit() {
            Predicate<Block> isChestBlock = block -> block instanceof AbstractChestBlock;
            CommonMain.registerMutationBehaviour(isChestBlock, MutationMode.MERGE, (useContext, level, state, pos, stack) -> {
                Player player = useContext.getPlayer();
                if (player == null) {
                    return ToolUsageResult.fail();
                }

                if (state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == EsChestType.SINGLE) {
                    CompoundTag tag = stack.getOrCreateTag();

                    if (tag.contains("pos")) {
                        BlockPos otherPos = NbtUtils.readBlockPos(tag.getCompound("pos"));
                        BlockState otherState = level.getBlockState(otherPos);
                        BlockPos delta = otherPos.subtract(pos);
                        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());

                        if (direction != null) {
                            if (state.getBlock() == otherState.getBlock()) {
                                if (otherState.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == EsChestType.SINGLE) {
                                    if (state.getValue(BlockStateProperties.HORIZONTAL_FACING) == otherState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                                        boolean firstIsDinnerbone = level.getBlockEntity(pos) instanceof OpenableBlockEntity blockEntity && blockEntity.isDinnerbone();
                                        boolean secondIsDinnerbone = level.getBlockEntity(otherPos) instanceof OpenableBlockEntity blockEntity && blockEntity.isDinnerbone();

                                        if (firstIsDinnerbone == secondIsDinnerbone) {
                                            if (!level.isClientSide()) {
                                                EsChestType chestType = AbstractChestBlock.getChestType(state.getValue(BlockStateProperties.HORIZONTAL_FACING), direction);
                                                level.setBlockAndUpdate(pos, state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, chestType));
                                                // note: other state is updated via neighbour update
                                                tag.remove("pos");
                                                //noinspection ConstantConditions
                                                player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_end"), true);
                                            }
                                            return ToolUsageResult.slowSuccess();
                                        }
                                        player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_wrong_block"), true);
                                    } else {
                                        //noinspection ConstantConditions
                                        player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_wrong_facing"), true);
                                    }
                                } else {
                                    //noinspection ConstantConditions
                                    player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_already_double_chest"), true);
                                }
                            } else {
                                //noinspection ConstantConditions
                                player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_wrong_block"), true);
                            }
                        } else {
                            //noinspection ConstantConditions
                            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_not_adjacent"), true);
                        }
                        tag.remove("pos");
                    } else {
                        if (!level.isClientSide()) {
                            tag.put("pos", NbtUtils.writeBlockPos(pos));
                            //noinspection ConstantConditions
                            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_start", Utils.ALT_USE), true);
                        }
                        return ToolUsageResult.fastSuccess();
                    }
                }
                return ToolUsageResult.fail();
            });

            CommonMain.registerMutationBehaviour(isChestBlock, MutationMode.SPLIT, (useContext, level, state, pos, stack) -> {
                if (state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE) {
                    if (!level.isClientSide()) {
                        level.setBlockAndUpdate(pos, state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, EsChestType.SINGLE));
                        // note: other state is updated to single via neighbour update
                    }
                    return ToolUsageResult.slowSuccess();
                }
                return ToolUsageResult.fail();
            });

            CommonMain.registerMutationBehaviour(isChestBlock, MutationMode.ROTATE, (useContext, level, state, pos, stack) -> {
                if (!level.isClientSide()) {
                    EsChestType chestType = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);

                    if (chestType == EsChestType.SINGLE) {
                        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
                    } else {
                        BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                        BlockState otherState = level.getBlockState(otherPos);

                        if (chestType == EsChestType.TOP || chestType == EsChestType.BOTTOM) {
                            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
                            level.setBlockAndUpdate(otherPos, otherState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
                        } else {
                            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE).getOpposite()));
                            level.setBlockAndUpdate(otherPos, otherState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, otherState.getValue(AbstractChestBlock.CURSED_CHEST_TYPE).getOpposite()));
                        }
                    }
                }
                return ToolUsageResult.slowSuccess();
            });
        }

        public final List<BarrelBlock> barrelBlocks = new ArrayList<>(tiers - 1);

        private void createBarrel(ResourceLocation id, ResourceLocation stat, Tier tier, Block.Properties properties) {
            BarrelBlock block = new BarrelBlock(tier.getBlockSettings().apply(properties), stat, tier.getSlotCount());
            BlockItem item = new BlockItem(block, tier.getItemSettings().apply(new Item.Properties()));

            barrelBlocks.add(Registry.register(BuiltInRegistries.BLOCK, id, block));
            Registry.register(BuiltInRegistries.ITEM, id, item);
        }

        private final ResourceLocation copperBarrelStat = stat("open_copper_barrel");

        private final Block.Properties copperBarrelProperties = Block.Properties
                .of()
                .mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.BASS)
                .strength(3, 6)
                .sound(SoundType.WOOD)
                .ignitedByLava();

        private void createCopperBarrel(ResourceLocation id, WeatheringCopper.WeatherState weatherState) {
            BarrelBlock block = new CopperBarrelBlock(Tiers.COPPER.getBlockSettings().apply(copperBarrelProperties), copperBarrelStat, Tiers.COPPER.getSlotCount(), weatherState);
            BlockItem item = new BlockItem(block, Tiers.COPPER.getItemSettings().apply(new Item.Properties()));

            barrelBlocks.add(Registry.register(BuiltInRegistries.BLOCK, id, block));
            Registry.register(BuiltInRegistries.ITEM, id, item);
        }

        public void barrelInit(
                Supplier<Lockable> lockable,

                TagKey<Block> barrelTag
        ) {
            final Block.Properties ironBarrelProperties = Block.Properties
                    .of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(5, 6)
                    .sound(SoundType.WOOD)
                    .ignitedByLava();
            final Block.Properties goldBarrelProperties = Block.Properties
                    .of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3, 6)
                    .sound(SoundType.WOOD)
                    .ignitedByLava();
            final Block.Properties diamondBarrelProperties = Block.Properties
                    .of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(5, 6)
                    .sound(SoundType.WOOD)
                    .ignitedByLava();
            final Block.Properties obsidianBarrelProperties = Block.Properties
                    .of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(50, 1200)
                    .sound(SoundType.WOOD)
                    .ignitedByLava();
            final Block.Properties netheriteBarrelProperties = Block.Properties
                    .of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(50, 1200)
                    .sound(SoundType.WOOD);

            createCopperBarrel(Utils.id("copper_barrel"), WeatheringCopper.WeatherState.UNAFFECTED);
            createCopperBarrel(Utils.id("exposed_copper_barrel"), WeatheringCopper.WeatherState.EXPOSED);
            createCopperBarrel(Utils.id("weathered_copper_barrel"), WeatheringCopper.WeatherState.WEATHERED);
            createCopperBarrel(Utils.id("oxidized_copper_barrel"), WeatheringCopper.WeatherState.OXIDIZED);
            createBarrel(Utils.id("waxed_copper_barrel"), copperMiniBarrelStat, Tiers.COPPER, copperBarrelProperties);
            createBarrel(Utils.id("waxed_exposed_copper_barrel"), copperMiniBarrelStat, Tiers.COPPER, copperBarrelProperties);
            createBarrel(Utils.id("waxed_weathered_copper_barrel"), copperMiniBarrelStat, Tiers.COPPER, copperBarrelProperties);
            createBarrel(Utils.id("waxed_oxidized_copper_barrel"), copperMiniBarrelStat, Tiers.COPPER, copperBarrelProperties);
            createBarrel(Utils.id("iron_barrel"), stat("open_iron_barrel"), Tiers.IRON, ironBarrelProperties);
            createBarrel(Utils.id("gold_barrel"), stat("open_gold_barrel"), Tiers.GOLD, goldBarrelProperties);
            createBarrel(Utils.id("diamond_barrel"), stat("open_diamond_barrel"), Tiers.DIAMOND, diamondBarrelProperties);
            createBarrel(Utils.id("obsidian_barrel"), stat("open_obsidian_barrel"), Tiers.OBSIDIAN, obsidianBarrelProperties);
            createBarrel(Utils.id("netherite_barrel"), stat("open_netherite_barrel"), Tiers.NETHERITE, netheriteBarrelProperties);

            CommonMain.barrelBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CommonMain.BARREL_OBJECT_TYPE, BlockEntityType.Builder.of((pos, state) -> new BarrelBlockEntity(CommonMain.getBarrelBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), lockable), barrelBlocks.toArray(BarrelBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.BARREL_OBJECT_TYPE.toString())));

            Predicate<Block> isUpgradableBarrelBlock = (block) -> block instanceof BarrelBlock || block instanceof net.minecraft.world.level.block.BarrelBlock || block.defaultBlockState().is(barrelTag);

            CommonMain.registerMutationBehaviour(isUpgradableBarrelBlock, MutationMode.ROTATE, (useContext, level, state, pos, stack) -> {
                if (state.hasProperty(BlockStateProperties.FACING)) {
                    if (!level.isClientSide()) {
                        level.setBlockAndUpdate(pos, state.cycle(BlockStateProperties.FACING));
                    }
                    return ToolUsageResult.slowSuccess();
                }
                return ToolUsageResult.fail();
            });
        }

        public final List<MiniStorageBlock> miniStorageBlocks = new ArrayList<>();

        private void createMiniStorageBlock(ResourceLocation id, ResourceLocation stat, Tier tier, Block.Properties properties, boolean hasRibbon) {
            MiniStorageBlock block = new MiniStorageBlock(tier.getBlockSettings().apply(properties), stat, hasRibbon);
            BlockItem item =new BlockItem(block, tier.getItemSettings().apply(new Item.Properties()));

            miniStorageBlocks.add(Registry.register(BuiltInRegistries.BLOCK, id, block));
            Registry.register(BuiltInRegistries.ITEM, id, item);
        }

        private final ResourceLocation copperMiniBarrelStat = stat("open_copper_mini_barrel");
        private final Block.Properties copperBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(3, 6).sound(SoundType.WOOD);

        private void createCopperMiniStorageBlock(ResourceLocation id, WeatheringCopper.WeatherState weatherState) {
            MiniStorageBlock block = new CopperMiniStorageBlock(Tiers.COPPER.getBlockSettings().apply(copperBarrelSettings), copperMiniBarrelStat, weatherState);
            BlockItem item = new BlockItem(block, Tiers.COPPER.getItemSettings().apply(new Item.Properties()));

            miniStorageBlocks.add(Registry.register(BuiltInRegistries.BLOCK, id, block));
            Registry.register(BuiltInRegistries.ITEM, id, item);
        }

        public void miniStorageBlockInit(Supplier<Lockable> lockable) {
            final ResourceLocation woodChestStat = stat("open_wood_mini_chest");

            // Init block settings
            final Block.Properties redPresentSettings = Block.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties whitePresentSettings = Block.Properties.of().mapColor(MapColor.SNOW).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties candyCanePresentSettings = Block.Properties.of().mapColor(MapColor.SNOW).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties greenPresentSettings = Block.Properties.of().mapColor(MapColor.PLANT).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties lavenderPresentSettings = Block.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties pinkAmethystPresentSettings = Block.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties woodBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD);
            final Block.Properties ironBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(5, 6).sound(SoundType.WOOD);
            final Block.Properties goldBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(3, 6).sound(SoundType.WOOD);
            final Block.Properties diamondBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(5, 6).sound(SoundType.WOOD);
            final Block.Properties obsidianBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(50, 1200).sound(SoundType.WOOD);
            final Block.Properties netheriteBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(50, 1200).sound(SoundType.WOOD);

            createMiniStorageBlock(Utils.id("vanilla_wood_mini_chest"), woodChestStat, Tiers.WOOD, Properties.WOOD, false);
            createMiniStorageBlock(Utils.id("wood_mini_chest"), woodChestStat, Tiers.WOOD, Properties.WOOD, false);
            createMiniStorageBlock(Utils.id("pumpkin_mini_chest"), stat("open_pumpkin_mini_chest"), Tiers.WOOD, Properties.PUMPKIN, false);
            createMiniStorageBlock(Utils.id("red_mini_present"), stat("open_red_mini_present"), Tiers.WOOD, redPresentSettings, true);
            createMiniStorageBlock(Utils.id("white_mini_present"), stat("open_white_mini_present"), Tiers.WOOD, whitePresentSettings, true);
            createMiniStorageBlock(Utils.id("candy_cane_mini_present"), stat("open_candy_cane_mini_present"), Tiers.WOOD, candyCanePresentSettings, false);
            createMiniStorageBlock(Utils.id("green_mini_present"), stat("open_green_mini_present"), Tiers.WOOD, greenPresentSettings, true);
            createMiniStorageBlock(Utils.id("lavender_mini_present"), stat("open_lavender_mini_present"), Tiers.WOOD, lavenderPresentSettings, true);
            createMiniStorageBlock(Utils.id("pink_amethyst_mini_present"), stat("open_pink_amethyst_mini_present"), Tiers.WOOD, pinkAmethystPresentSettings, true);
            createMiniStorageBlock(Utils.id("iron_mini_chest"), stat("open_iron_mini_chest"), Tiers.IRON, Properties.IRON, false);
            createMiniStorageBlock(Utils.id("gold_mini_chest"), stat("open_gold_mini_chest"), Tiers.GOLD, Properties.GOLD, false);
            createMiniStorageBlock(Utils.id("diamond_mini_chest"), stat("open_diamond_mini_chest"), Tiers.DIAMOND, Properties.DIAMOND, false);
            createMiniStorageBlock(Utils.id("obsidian_mini_chest"), stat("open_obsidian_mini_chest"), Tiers.OBSIDIAN, Properties.OBSIDIAN, false);
            createMiniStorageBlock(Utils.id("netherite_mini_chest"), stat("open_netherite_mini_chest"), Tiers.NETHERITE, Properties.NETHERITE, false);

            createMiniStorageBlock(Utils.id("mini_barrel"), stat("open_mini_barrel"), Tiers.WOOD, woodBarrelSettings, false);
            createCopperMiniStorageBlock(Utils.id("copper_mini_barrel"), WeatheringCopper.WeatherState.UNAFFECTED);
            createCopperMiniStorageBlock(Utils.id("exposed_copper_mini_barrel"), WeatheringCopper.WeatherState.EXPOSED);
            createCopperMiniStorageBlock(Utils.id("weathered_copper_mini_barrel"), WeatheringCopper.WeatherState.WEATHERED);
            createCopperMiniStorageBlock(Utils.id("oxidized_copper_mini_barrel"), WeatheringCopper.WeatherState.OXIDIZED);
            createMiniStorageBlock(Utils.id("waxed_copper_mini_barrel"), copperMiniBarrelStat, Tiers.COPPER, copperBarrelSettings, false);
            createMiniStorageBlock(Utils.id("waxed_exposed_copper_mini_barrel"), copperMiniBarrelStat, Tiers.COPPER, copperBarrelSettings, false);
            createMiniStorageBlock(Utils.id("waxed_weathered_copper_mini_barrel"), copperMiniBarrelStat, Tiers.COPPER, copperBarrelSettings, false);
            createMiniStorageBlock(Utils.id("waxed_oxidized_copper_mini_barrel"), copperMiniBarrelStat, Tiers.COPPER, copperBarrelSettings, false);
            createMiniStorageBlock(Utils.id("iron_mini_barrel"), stat("open_iron_mini_barrel"), Tiers.IRON, ironBarrelSettings, false);
            createMiniStorageBlock(Utils.id("gold_mini_barrel"), stat("open_gold_mini_barrel"), Tiers.GOLD, goldBarrelSettings, false);
            createMiniStorageBlock(Utils.id("diamond_mini_barrel"), stat("open_diamond_mini_barrel"), Tiers.DIAMOND, diamondBarrelSettings, false);
            createMiniStorageBlock(Utils.id("obsidian_mini_barrel"), stat("open_obsidian_mini_barrel"), Tiers.OBSIDIAN, obsidianBarrelSettings, false);
            createMiniStorageBlock(Utils.id("netherite_mini_barrel"), stat("open_netherite_mini_barrel"), Tiers.NETHERITE, netheriteBarrelSettings, false);

            CommonMain.miniStorageBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CommonMain.MINI_STORAGE_OBJECT_TYPE, BlockEntityType.Builder.of((pos, state) -> new MiniStorageBlockEntity(CommonMain.getMiniStorageBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), lockable), miniStorageBlocks.toArray(MiniStorageBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.MINI_STORAGE_OBJECT_TYPE.toString())));

            Predicate<Block> isMiniStorage = block -> block instanceof MiniStorageBlock;
            CommonMain.registerMutationBehaviour(isMiniStorage, MutationMode.ROTATE, (useContext, level, state, pos, stack) -> {
                if (!level.isClientSide()) {
                    level.setBlockAndUpdate(pos, state.rotate(Rotation.CLOCKWISE_90));
                }
                return ToolUsageResult.slowSuccess();
            });
        }

        public List<OpenableBlock> getBlocks() {
            List<OpenableBlock> blocks = new ArrayList<>();
            blocks.addAll(chestBlocks);
            blocks.addAll(oldChestBlocks);
            blocks.addAll(barrelBlocks);
            blocks.addAll(miniStorageBlocks);
            return blocks;
        }

        public List<EntityType<ChestMinecart>> getChestMinecartEntityTypes() {
            return chestMinecartEntityTypes;
        }

        public Map<ChestMinecartItem, EntityType<ChestMinecart>> getChestMinecartAndTypes() {
            Map<ResourceLocation, EntityType<ChestMinecart>> chestMinecartEntityTypesLookup =
                    chestMinecartEntityTypes.stream()
                                            .map(it -> Map.entry(it.builtInRegistryHolder().key().location(), it))
                                            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

            return chestMinecartItems.stream()
                                     .map(value -> Map.entry(value, chestMinecartEntityTypesLookup.get(value.builtInRegistryHolder().key().location())))
                                     .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        }
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

    public static InteractionResult onPlayerUseEntity(Level level, Player player, InteractionHand hand, Entity entity) {
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

    public static void openInventory(ServerPlayer player, OpenableInventory inventory, Consumer<ServerPlayer> onInitialOpen, ResourceLocation forcedScreenType) {
        Component title = inventory.getInventoryTitle();

        if (!inventory.canBeUsedBy(player)) {
            player.displayClientMessage(Component.translatable("container.isLocked", title), true);
            player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }

        if (!player.isSpectator()) {
            onInitialOpen.accept(player);
        }

        player.openMenu(new ScreenHandlerFactoryAdapter(title, inventory.getInventory(), forcedScreenType));
    }

    @SuppressWarnings("unused")
    public static void generateDisplayItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, Consumer<ItemStack> output) {
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
