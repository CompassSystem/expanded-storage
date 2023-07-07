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
import compasses.expandedstorage.impl.block.misc.DoubleItemAccess;
import compasses.expandedstorage.impl.block.strategies.ItemAccess;
import compasses.expandedstorage.impl.block.strategies.Lockable;
import compasses.expandedstorage.impl.entity.ChestMinecart;
import compasses.expandedstorage.impl.item.BlockMutatorBehaviour;
import compasses.expandedstorage.impl.item.ChestMinecartItem;
import compasses.expandedstorage.impl.item.EntityInteractableItem;
import compasses.expandedstorage.impl.item.MutationMode;
import compasses.expandedstorage.impl.item.StorageConversionKit;
import compasses.expandedstorage.impl.item.StorageMutator;
import compasses.expandedstorage.impl.item.ToolUsageResult;
import compasses.expandedstorage.impl.misc.Tier;
import compasses.expandedstorage.impl.misc.Utils;
import compasses.expandedstorage.impl.recipe.BlockConversionRecipe;
import compasses.expandedstorage.impl.recipe.ConversionRecipeManager;
import compasses.expandedstorage.impl.registration.ModItems;
import compasses.expandedstorage.impl.registration.NamedValue;
import compasses.expandedstorage.impl.registration.ObjectConsumer;
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
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
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

        private ResourceLocation defineStat(String id) {
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

        public void chestInit(
                Supplier<Lockable> lockable,

                BiFunction<ChestBlock, Item.Properties, BlockItem> chestItemMaker,
                Function<OpenableBlockEntity, ItemAccess> chestAccessMaker,

                BiFunction<Item.Properties, ResourceLocation, ChestMinecartItem> chestMinecartItemMaker
        ) {
            final ResourceLocation woodStat = defineStat("open_wood_chest");
            final ResourceLocation pumpkinStat = defineStat("open_pumpkin_chest");
            final ResourceLocation presentStat = defineStat("open_present");
            final ResourceLocation bambooStat = defineStat("open_bamboo_chest");
            final ResourceLocation mossStat = defineStat("open_moss_chest");
            final ResourceLocation ironStat = defineStat("open_iron_chest");
            final ResourceLocation goldStat = defineStat("open_gold_chest");
            final ResourceLocation diamondStat = defineStat("open_diamond_chest");
            final ResourceLocation obsidianStat = defineStat("open_obsidian_chest");
            final ResourceLocation netheriteStat = defineStat("open_netherite_chest");

            final Block.Properties presentSettings = Block.Properties.of().mapColor(state -> {
                EsChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
                if (type == EsChestType.SINGLE) return MapColor.COLOR_RED;
                else if (type == EsChestType.FRONT || type == EsChestType.BACK) return MapColor.PLANT;
                return MapColor.SNOW;
            }).strength(2.5f).sound(SoundType.WOOD);

            ObjectConsumer chestMaker = (id, stat, tier, settings) -> {
                NamedValue<ChestBlock> block = new NamedValue<>(id, () -> new ChestBlock(tier.getBlockSettings().apply(settings), stat, tier.getSlotCount()));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> chestItemMaker.apply(block.getValue(), tier.getItemSettings().apply(new Item.Properties())));
                ResourceLocation cartId = new ResourceLocation(id.getNamespace(), id.getPath() + "_minecart");
                NamedValue<ChestMinecartItem> cartItem = new NamedValue<>(cartId, () -> chestMinecartItemMaker.apply(new Item.Properties(), cartId));
                NamedValue<EntityType<ChestMinecart>> cartEntityType = new NamedValue<>(cartId, () -> new EntityType<>((type, level) -> {
                    return new ChestMinecart(type, level, cartItem.getValue(), block.getValue());
                }, MobCategory.MISC, true, true, false, false, ImmutableSet.of(), EntityDimensions.scalable(0.98F, 0.7F), 8, 3, FeatureFlagSet.of()));

                chestBlocks.add(Registry.register(BuiltInRegistries.BLOCK, block.getName(), block.getValue()));
                chestItems.add(Registry.register(BuiltInRegistries.ITEM, item.getName(), item.getValue()));
                chestMinecartEntityTypes.add(Registry.register(BuiltInRegistries.ENTITY_TYPE, cartEntityType.getName(), cartEntityType.getValue()));
                chestMinecartItems.add(Registry.register(BuiltInRegistries.ITEM, cartItem.getName(), cartItem.getValue()));
            };

            ObjectConsumer mossChestMaker = (id, stat, tier, settings) -> {
                NamedValue<ChestBlock> block = new NamedValue<>(id, () -> new MossChestBlock(tier.getBlockSettings().apply(settings), stat, tier.getSlotCount()));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> chestItemMaker.apply(block.getValue(), tier.getItemSettings().apply(new Item.Properties())));
                ResourceLocation cartId = new ResourceLocation(id.getNamespace(), id.getPath() + "_minecart");
                NamedValue<ChestMinecartItem> cartItem = new NamedValue<>(cartId, () -> chestMinecartItemMaker.apply(new Item.Properties(), cartId));
                NamedValue<EntityType<ChestMinecart>> cartEntityType = new NamedValue<>(cartId, () -> new EntityType<>((type, level) -> {
                    return new ChestMinecart(type, level, cartItem.getValue(), block.getValue());
                }, MobCategory.MISC, true, true, false, false, ImmutableSet.of(), EntityDimensions.scalable(0.98F, 0.7F), 8, 3, FeatureFlagSet.of()));

                chestBlocks.add(Registry.register(BuiltInRegistries.BLOCK, block.getName(), block.getValue()));
                chestItems.add(Registry.register(BuiltInRegistries.ITEM, item.getName(), item.getValue()));
                chestMinecartEntityTypes.add(Registry.register(BuiltInRegistries.ENTITY_TYPE, cartEntityType.getName(), cartEntityType.getValue()));
                chestMinecartItems.add(Registry.register(BuiltInRegistries.ITEM, cartItem.getName(), cartItem.getValue()));
            };

            chestMaker.apply(Utils.id("wood_chest"), woodStat, Tiers.WOOD, Properties.WOOD);
            chestMaker.apply(Utils.id("pumpkin_chest"), pumpkinStat, Tiers.WOOD, Properties.PUMPKIN);
            chestMaker.apply(Utils.id("present"), presentStat, Tiers.WOOD, presentSettings);
            chestMaker.apply(Utils.id("bamboo_chest"), bambooStat, Tiers.WOOD, Properties.BAMBOO);
            mossChestMaker.apply(Utils.id("moss_chest"), mossStat, Tiers.WOOD, Properties.MOSS);
            chestMaker.apply(Utils.id("iron_chest"), ironStat, Tiers.IRON, Properties.IRON);
            chestMaker.apply(Utils.id("gold_chest"), goldStat, Tiers.GOLD, Properties.GOLD);
            chestMaker.apply(Utils.id("diamond_chest"), diamondStat, Tiers.DIAMOND, Properties.DIAMOND);
            chestMaker.apply(Utils.id("obsidian_chest"), obsidianStat, Tiers.OBSIDIAN, Properties.OBSIDIAN);
            chestMaker.apply(Utils.id("netherite_chest"), netheriteStat, Tiers.NETHERITE, Properties.NETHERITE);

            CommonMain.chestBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CommonMain.CHEST_OBJECT_TYPE, BlockEntityType.Builder.of((pos, state) -> new ChestBlockEntity(CommonMain.getChestBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), chestAccessMaker, lockable), chestBlocks.toArray(ChestBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.CHEST_OBJECT_TYPE.toString())));
        }

        public final List<AbstractChestBlock> oldChestBlocks = new ArrayList<>(tiers);
        public final List<BlockItem> oldChestItems = new ArrayList<>(tiers);

        public void oldChestInit(
                Supplier<Lockable> lockable,

                Function<OpenableBlockEntity, ItemAccess> chestAccessMaker
        ) {
            final ResourceLocation woodStat = defineStat("open_old_wood_chest");
//            final ResourceLocation copperStat = defineStat("open_old_copper_chest");
            final ResourceLocation ironStat = defineStat("open_old_iron_chest");
            final ResourceLocation goldStat = defineStat("open_old_gold_chest");
            final ResourceLocation diamondStat = defineStat("open_old_diamond_chest");
            final ResourceLocation obsidianStat = defineStat("open_old_obsidian_chest");
            final ResourceLocation netheriteStat = defineStat("open_old_netherite_chest");
            ObjectConsumer chestMaker = (id, stat, tier, settings) -> {
                NamedValue<AbstractChestBlock> block = new NamedValue<>(id, () -> new AbstractChestBlock(tier.getBlockSettings().apply(settings), stat, tier.getSlotCount()));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> new BlockItem(block.getValue(), tier.getItemSettings().apply(new Item.Properties())));
                oldChestBlocks.add(Registry.register(BuiltInRegistries.BLOCK, block.getName(), block.getValue()));
                oldChestItems.add(Registry.register(BuiltInRegistries.ITEM, item.getName(), item.getValue()));
            };

            chestMaker.apply(Utils.id("old_wood_chest"), woodStat, Tiers.WOOD, Properties.WOOD);
            chestMaker.apply(Utils.id("old_iron_chest"), ironStat, Tiers.IRON, Properties.IRON);
            chestMaker.apply(Utils.id("old_gold_chest"), goldStat, Tiers.GOLD, Properties.GOLD);
            chestMaker.apply(Utils.id("old_diamond_chest"), diamondStat, Tiers.DIAMOND, Properties.DIAMOND);
            chestMaker.apply(Utils.id("old_obsidian_chest"), obsidianStat, Tiers.OBSIDIAN, Properties.OBSIDIAN);
            chestMaker.apply(Utils.id("old_netherite_chest"), netheriteStat, Tiers.NETHERITE, Properties.NETHERITE);

            CommonMain.oldChestBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CommonMain.OLD_CHEST_OBJECT_TYPE, BlockEntityType.Builder.of((pos, state) -> new OldChestBlockEntity(CommonMain.getOldChestBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), chestAccessMaker, lockable), oldChestBlocks.toArray(AbstractChestBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.OLD_CHEST_OBJECT_TYPE.toString())));
        }

        public void commonChestInit() {
            Predicate<Block> isChestBlock = b -> b instanceof AbstractChestBlock;
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
        public final List<BlockItem> barrelItems = new ArrayList<>(tiers - 1);

        public void barrelInit(
                Function<OpenableBlockEntity, ItemAccess> itemAccess,
                Supplier<Lockable> lockable,

                TagKey<Block> barrelTag
        ) {
            final ResourceLocation copperStat = defineStat("open_copper_barrel");
            final ResourceLocation ironStat = defineStat("open_iron_barrel");
            final ResourceLocation goldStat = defineStat("open_gold_barrel");
            final ResourceLocation diamondStat = defineStat("open_diamond_barrel");
            final ResourceLocation obsidianStat = defineStat("open_obsidian_barrel");
            final ResourceLocation netheriteStat = defineStat("open_netherite_barrel");

            final Block.Properties copperBarrelProperties = Block.Properties.of()
                                                                            .mapColor(MapColor.WOOD)
                                                                            .instrument(NoteBlockInstrument.BASS)
                                                                            .strength(3, 6)
                                                                            .sound(SoundType.WOOD)
                                                                            .ignitedByLava();
            final Block.Properties ironBarrelProperties = Block.Properties.of()
                                                                          .mapColor(MapColor.WOOD)
                                                                          .instrument(NoteBlockInstrument.BASS)
                                                                          .strength(5, 6)
                                                                          .sound(SoundType.WOOD)
                                                                          .ignitedByLava();
            final Block.Properties goldBarrelProperties = Block.Properties.of()
                                                                          .mapColor(MapColor.WOOD)
                                                                          .instrument(NoteBlockInstrument.BASS)
                                                                          .strength(3, 6)
                                                                          .sound(SoundType.WOOD)
                                                                          .ignitedByLava();
            final Block.Properties diamondBarrelProperties = Block.Properties.of()
                                                                             .mapColor(MapColor.WOOD)
                                                                             .instrument(NoteBlockInstrument.BASS)
                                                                             .strength(5, 6)
                                                                             .sound(SoundType.WOOD)
                                                                             .ignitedByLava();
            final Block.Properties obsidianBarrelProperties = Block.Properties.of()
                                                                              .mapColor(MapColor.WOOD)
                                                                              .instrument(NoteBlockInstrument.BASS)
                                                                              .strength(50, 1200)
                                                                              .sound(SoundType.WOOD)
                                                                              .ignitedByLava();
            final Block.Properties netheriteBarrelProperties = Block.Properties.of()
                                                                               .mapColor(MapColor.WOOD)
                                                                               .instrument(NoteBlockInstrument.BASS)
                                                                               .strength(50, 1200)
                                                                               .sound(SoundType.WOOD);

            ObjectConsumer barrelMaker = (id, stat, tier, settings) -> {
                NamedValue<BarrelBlock> block = new NamedValue<>(id, () -> new BarrelBlock(tier.getBlockSettings().apply(settings), stat, tier.getSlotCount()));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> new BlockItem(block.getValue(), tier.getItemSettings().apply(new Item.Properties())));

                barrelBlocks.add(Registry.register(BuiltInRegistries.BLOCK, block.getName(), block.getValue()));
                barrelItems.add(Registry.register(BuiltInRegistries.ITEM, item.getName(), item.getValue()));
            };

            BiConsumer<ResourceLocation, WeatheringCopper.WeatherState> copperBarrelMaker = (id, weatherState) -> {
                NamedValue<BarrelBlock> block = new NamedValue<>(id, () -> new CopperBarrelBlock(Tiers.COPPER.getBlockSettings().apply(copperBarrelProperties), copperStat, Tiers.COPPER.getSlotCount(), weatherState));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> new BlockItem(block.getValue(), Tiers.COPPER.getItemSettings().apply(new Item.Properties())));

                barrelBlocks.add(Registry.register(BuiltInRegistries.BLOCK, block.getName(), block.getValue()));
                barrelItems.add(Registry.register(BuiltInRegistries.ITEM, item.getName(), item.getValue()));
            };

            copperBarrelMaker.accept(Utils.id("copper_barrel"), WeatheringCopper.WeatherState.UNAFFECTED);
            copperBarrelMaker.accept(Utils.id("exposed_copper_barrel"), WeatheringCopper.WeatherState.EXPOSED);
            copperBarrelMaker.accept(Utils.id("weathered_copper_barrel"), WeatheringCopper.WeatherState.WEATHERED);
            copperBarrelMaker.accept(Utils.id("oxidized_copper_barrel"), WeatheringCopper.WeatherState.OXIDIZED);
            barrelMaker.apply(Utils.id("waxed_copper_barrel"), copperStat, Tiers.COPPER, copperBarrelProperties);
            barrelMaker.apply(Utils.id("waxed_exposed_copper_barrel"), copperStat, Tiers.COPPER, copperBarrelProperties);
            barrelMaker.apply(Utils.id("waxed_weathered_copper_barrel"), copperStat, Tiers.COPPER, copperBarrelProperties);
            barrelMaker.apply(Utils.id("waxed_oxidized_copper_barrel"), copperStat, Tiers.COPPER, copperBarrelProperties);
            barrelMaker.apply(Utils.id("iron_barrel"), ironStat, Tiers.IRON, ironBarrelProperties);
            barrelMaker.apply(Utils.id("gold_barrel"), goldStat, Tiers.GOLD, goldBarrelProperties);
            barrelMaker.apply(Utils.id("diamond_barrel"), diamondStat, Tiers.DIAMOND, diamondBarrelProperties);
            barrelMaker.apply(Utils.id("obsidian_barrel"), obsidianStat, Tiers.OBSIDIAN, obsidianBarrelProperties);
            barrelMaker.apply(Utils.id("netherite_barrel"), netheriteStat, Tiers.NETHERITE, netheriteBarrelProperties);

            CommonMain.barrelBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CommonMain.BARREL_OBJECT_TYPE, BlockEntityType.Builder.of((pos, state) -> new BarrelBlockEntity(CommonMain.getBarrelBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), itemAccess, lockable), barrelBlocks.toArray(BarrelBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.BARREL_OBJECT_TYPE.toString())));

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
        public final List<BlockItem> miniStorageItems = new ArrayList<>();

        public void miniStorageBlockInit(
                Function<OpenableBlockEntity, ItemAccess> itemAccess,
                Supplier<Lockable> lockable,

                BiFunction<MiniStorageBlock, Item.Properties, BlockItem> miniChestItemMaker
        ) {
            final ResourceLocation woodChestStat = defineStat("open_wood_mini_chest");
            final ResourceLocation pumpkinChestStat = defineStat("open_pumpkin_mini_chest");
            final ResourceLocation redPresentStat = defineStat("open_red_mini_present");
            final ResourceLocation whitePresentStat = defineStat("open_white_mini_present");
            final ResourceLocation candyCanePresentStat = defineStat("open_candy_cane_mini_present");
            final ResourceLocation greenPresentStat = defineStat("open_green_mini_present");
            final ResourceLocation lavenderPresentStat = defineStat("open_lavender_mini_present");
            final ResourceLocation pinkAmethystPresentStat = defineStat("open_pink_amethyst_mini_present");
            final ResourceLocation ironChestStat = defineStat("open_iron_mini_chest");
            final ResourceLocation goldChestStat = defineStat("open_gold_mini_chest");
            final ResourceLocation diamondChestStat = defineStat("open_diamond_mini_chest");
            final ResourceLocation obsidianChestStat = defineStat("open_obsidian_mini_chest");
            final ResourceLocation netheriteChestStat = defineStat("open_netherite_mini_chest");
            final ResourceLocation barrelStat = defineStat("open_mini_barrel");
            final ResourceLocation copperBarrelStat = defineStat("open_copper_mini_barrel");
            final ResourceLocation ironBarrelStat = defineStat("open_iron_mini_barrel");
            final ResourceLocation goldBarrelStat = defineStat("open_gold_mini_barrel");
            final ResourceLocation diamondBarrelStat = defineStat("open_diamond_mini_barrel");
            final ResourceLocation obsidianBarrelStat = defineStat("open_obsidian_mini_barrel");
            final ResourceLocation netheriteBarrelStat = defineStat("open_netherite_mini_barrel");

            // Init block settings
            final Block.Properties redPresentSettings = Block.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties whitePresentSettings = Block.Properties.of().mapColor(MapColor.SNOW).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties candyCanePresentSettings = Block.Properties.of().mapColor(MapColor.SNOW).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties greenPresentSettings = Block.Properties.of().mapColor(MapColor.PLANT).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties lavenderPresentSettings = Block.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties pinkAmethystPresentSettings = Block.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(2.5f).sound(SoundType.WOOD);
            final Block.Properties woodBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD);
            final Block.Properties copperBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(3, 6).sound(SoundType.WOOD);
            final Block.Properties ironBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(5, 6).sound(SoundType.WOOD);
            final Block.Properties goldBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(3, 6).sound(SoundType.WOOD);
            final Block.Properties diamondBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(5, 6).sound(SoundType.WOOD);
            final Block.Properties obsidianBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(50, 1200).sound(SoundType.WOOD);
            final Block.Properties netheriteBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(50, 1200).sound(SoundType.WOOD);

            Function<Boolean, ObjectConsumer> miniStorageMaker = (hasRibbon) -> (id, stat, tier, settings) -> {
                NamedValue<MiniStorageBlock> block = new NamedValue<>(id, () -> new MiniStorageBlock(tier.getBlockSettings().apply(settings), stat, hasRibbon));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> miniChestItemMaker.apply(block.getValue(), tier.getItemSettings().apply(new Item.Properties())));

                miniStorageBlocks.add(Registry.register(BuiltInRegistries.BLOCK, block.getName(), block.getValue()));
                miniStorageItems.add(Registry.register(BuiltInRegistries.ITEM, item.getName(), item.getValue()));
            };

            ObjectConsumer miniStorageMakerNoRibbon = miniStorageMaker.apply(false);
            ObjectConsumer miniStorageMakerRibbon = miniStorageMaker.apply(true);

            BiConsumer<ResourceLocation, WeatheringCopper.WeatherState> copperMiniBarrelMaker = (id, weatherState) -> {
                NamedValue<MiniStorageBlock> block = new NamedValue<>(id, () -> new CopperMiniStorageBlock(Tiers.COPPER.getBlockSettings().apply(copperBarrelSettings), copperBarrelStat, weatherState));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> miniChestItemMaker.apply(block.getValue(), Tiers.COPPER.getItemSettings().apply(new Item.Properties())));

                miniStorageBlocks.add(Registry.register(BuiltInRegistries.BLOCK, block.getName(), block.getValue()));
                miniStorageItems.add(Registry.register(BuiltInRegistries.ITEM, item.getName(), item.getValue()));
            };

            miniStorageMakerNoRibbon.apply(Utils.id("vanilla_wood_mini_chest"), woodChestStat, Tiers.WOOD, Properties.WOOD);
            miniStorageMakerNoRibbon.apply(Utils.id("wood_mini_chest"), woodChestStat, Tiers.WOOD, Properties.WOOD);
            miniStorageMakerNoRibbon.apply(Utils.id("pumpkin_mini_chest"), pumpkinChestStat, Tiers.WOOD, Properties.PUMPKIN);
            miniStorageMakerRibbon.apply(Utils.id("red_mini_present"), redPresentStat, Tiers.WOOD, redPresentSettings);
            miniStorageMakerRibbon.apply(Utils.id("white_mini_present"), whitePresentStat, Tiers.WOOD, whitePresentSettings);
            miniStorageMakerNoRibbon.apply(Utils.id("candy_cane_mini_present"), candyCanePresentStat, Tiers.WOOD, candyCanePresentSettings);
            miniStorageMakerRibbon.apply(Utils.id("green_mini_present"), greenPresentStat, Tiers.WOOD, greenPresentSettings);
            miniStorageMakerRibbon.apply(Utils.id("lavender_mini_present"), lavenderPresentStat, Tiers.WOOD, lavenderPresentSettings);
            miniStorageMakerRibbon.apply(Utils.id("pink_amethyst_mini_present"), pinkAmethystPresentStat, Tiers.WOOD, pinkAmethystPresentSettings);
            miniStorageMakerNoRibbon.apply(Utils.id("iron_mini_chest"), ironChestStat, Tiers.IRON, Properties.IRON);
            miniStorageMakerNoRibbon.apply(Utils.id("gold_mini_chest"), goldChestStat, Tiers.GOLD, Properties.GOLD);
            miniStorageMakerNoRibbon.apply(Utils.id("diamond_mini_chest"), diamondChestStat, Tiers.DIAMOND, Properties.DIAMOND);
            miniStorageMakerNoRibbon.apply(Utils.id("obsidian_mini_chest"), obsidianChestStat, Tiers.OBSIDIAN, Properties.OBSIDIAN);
            miniStorageMakerNoRibbon.apply(Utils.id("netherite_mini_chest"), netheriteChestStat, Tiers.NETHERITE, Properties.NETHERITE);

            miniStorageMakerNoRibbon.apply(Utils.id("mini_barrel"), barrelStat, Tiers.WOOD, woodBarrelSettings);
            copperMiniBarrelMaker.accept(Utils.id("copper_mini_barrel"), WeatheringCopper.WeatherState.UNAFFECTED);
            copperMiniBarrelMaker.accept(Utils.id("exposed_copper_mini_barrel"), WeatheringCopper.WeatherState.EXPOSED);
            copperMiniBarrelMaker.accept(Utils.id("weathered_copper_mini_barrel"), WeatheringCopper.WeatherState.WEATHERED);
            copperMiniBarrelMaker.accept(Utils.id("oxidized_copper_mini_barrel"), WeatheringCopper.WeatherState.OXIDIZED);
            miniStorageMakerNoRibbon.apply(Utils.id("waxed_copper_mini_barrel"), copperBarrelStat, Tiers.COPPER, copperBarrelSettings);
            miniStorageMakerNoRibbon.apply(Utils.id("waxed_exposed_copper_mini_barrel"), copperBarrelStat, Tiers.COPPER, copperBarrelSettings);
            miniStorageMakerNoRibbon.apply(Utils.id("waxed_weathered_copper_mini_barrel"), copperBarrelStat, Tiers.COPPER, copperBarrelSettings);
            miniStorageMakerNoRibbon.apply(Utils.id("waxed_oxidized_copper_mini_barrel"), copperBarrelStat, Tiers.COPPER, copperBarrelSettings);
            miniStorageMakerNoRibbon.apply(Utils.id("iron_mini_barrel"), ironBarrelStat, Tiers.IRON, ironBarrelSettings);
            miniStorageMakerNoRibbon.apply(Utils.id("gold_mini_barrel"), goldBarrelStat, Tiers.GOLD, goldBarrelSettings);
            miniStorageMakerNoRibbon.apply(Utils.id("diamond_mini_barrel"), diamondBarrelStat, Tiers.DIAMOND, diamondBarrelSettings);
            miniStorageMakerNoRibbon.apply(Utils.id("obsidian_mini_barrel"), obsidianBarrelStat, Tiers.OBSIDIAN, obsidianBarrelSettings);
            miniStorageMakerNoRibbon.apply(Utils.id("netherite_mini_barrel"), netheriteBarrelStat, Tiers.NETHERITE, netheriteBarrelSettings);

            CommonMain.miniStorageBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CommonMain.MINI_STORAGE_OBJECT_TYPE, BlockEntityType.Builder.of((pos, state) -> new MiniStorageBlockEntity(CommonMain.getMiniStorageBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), itemAccess, lockable), miniStorageBlocks.toArray(MiniStorageBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.MINI_STORAGE_OBJECT_TYPE.toString())));

            Predicate<Block> isMiniStorage = b -> b instanceof MiniStorageBlock;
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

    public static Optional<ItemAccess> getItemAccess(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof OldChestBlockEntity entity) {
            DoubleItemAccess access = entity.getItemAccess();
            EsChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            if (access.hasCachedAccess() || type == EsChestType.SINGLE) {
                return Optional.of(access);
            }

            if (level.getBlockEntity(pos.relative(AbstractChestBlock.getDirectionToAttached(type, facing))) instanceof OldChestBlockEntity otherEntity) {
                DoubleItemAccess otherAccess = otherEntity.getItemAccess();

                if (otherAccess.hasCachedAccess()) {
                    return Optional.of(otherAccess);
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

                return Optional.of(first);
            }
        } else if (blockEntity instanceof OpenableBlockEntity entity) {
            return Optional.of(entity.getItemAccess());
        }

        return Optional.empty();
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
