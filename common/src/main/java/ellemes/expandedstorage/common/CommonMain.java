package ellemes.expandedstorage.common;

import com.google.common.collect.ImmutableSet;
import ellemes.expandedstorage.api.EsChestType;
import ellemes.expandedstorage.common.block.AbstractChestBlock;
import ellemes.expandedstorage.common.block.BarrelBlock;
import ellemes.expandedstorage.common.block.ChestBlock;
import ellemes.expandedstorage.common.block.CopperBarrelBlock;
import ellemes.expandedstorage.common.block.MiniChestBlock;
import ellemes.expandedstorage.common.block.OpenableBlock;
import ellemes.expandedstorage.common.block.entity.BarrelBlockEntity;
import ellemes.expandedstorage.common.block.entity.ChestBlockEntity;
import ellemes.expandedstorage.common.block.entity.MiniChestBlockEntity;
import ellemes.expandedstorage.common.block.entity.OldChestBlockEntity;
import ellemes.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import ellemes.expandedstorage.common.block.misc.DoubleItemAccess;
import ellemes.expandedstorage.common.block.strategies.ItemAccess;
import ellemes.expandedstorage.common.block.strategies.Lockable;
import ellemes.expandedstorage.common.client.MiniChestScreen;
import ellemes.expandedstorage.common.client.TextureCollection;
import ellemes.expandedstorage.common.entity.ChestMinecart;
import ellemes.expandedstorage.common.entity.TieredEntityType;
import ellemes.expandedstorage.common.item.BlockUpgradeBehaviour;
import ellemes.expandedstorage.common.item.ChestMinecartItem;
import ellemes.expandedstorage.common.item.EntityInteractableItem;
import ellemes.expandedstorage.common.item.EntityMutatorBehaviour;
import ellemes.expandedstorage.common.item.EntityUpgradeBehaviour;
import ellemes.expandedstorage.common.item.MutationMode;
import ellemes.expandedstorage.common.item.BlockMutatorBehaviour;
import ellemes.expandedstorage.common.item.StorageConversionKit;
import ellemes.expandedstorage.common.item.StorageMutator;
import ellemes.expandedstorage.common.misc.TagReloadListener;
import ellemes.expandedstorage.common.misc.TieredObject;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.registration.Content;
import ellemes.expandedstorage.common.registration.ContentConsumer;
import ellemes.expandedstorage.common.registration.NamedValue;
import ellemes.expandedstorage.common.registration.ObjectConsumer;
import ellemes.expandedstorage.common.misc.Tier;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
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
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class CommonMain {
    public static final ResourceLocation BARREL_OBJECT_TYPE = Utils.id("barrel");
    public static final ResourceLocation CHEST_OBJECT_TYPE = Utils.id("chest");
    public static final ResourceLocation OLD_CHEST_OBJECT_TYPE = Utils.id("old_chest");
    public static final ResourceLocation MINI_CHEST_OBJECT_TYPE = Utils.id("mini_chest");
    public static final ResourceLocation MINECART_CHEST_OBJECT_TYPE = Utils.id("minecart_chest");

    private static final Map<Predicate<Block>, BlockUpgradeBehaviour> BLOCK_UPGRADE_BEHAVIOURS = new HashMap<>();
    private static final Map<Predicate<Entity>, EntityUpgradeBehaviour> ENTITY_UPGRADE_BEHAVIOURS = new HashMap<>();
    private static final Map<Map.Entry<Predicate<Block>, MutationMode>, BlockMutatorBehaviour> BLOCK_MUTATOR_BEHAVIOURS = new HashMap<>();
    private static final Map<Map.Entry<Predicate<Entity>, MutationMode>, EntityMutatorBehaviour> ENTITY_MUTATOR_BEHAVIOURS = new HashMap<>();
    private static final Map<Map.Entry<ResourceLocation, ResourceLocation>, TieredObject> TIERED_OBJECTS = new HashMap<>();
    private static final Map<ResourceLocation, TextureCollection> CHEST_TEXTURES = new HashMap<>();

    private static NamedValue<BlockEntityType<ChestBlockEntity>> chestBlockEntityType;
    private static NamedValue<BlockEntityType<OldChestBlockEntity>> oldChestBlockEntityType;
    private static NamedValue<BlockEntityType<BarrelBlockEntity>> barrelBlockEntityType;
    private static NamedValue<BlockEntityType<MiniChestBlockEntity>> miniChestBlockEntityType;

    private static Function<OpenableBlockEntity, ItemAccess> itemAccess;
    private static Supplier<Lockable> lockable;

    public static BlockEntityType<ChestBlockEntity> getChestBlockEntityType() {
        return chestBlockEntityType.getValue();
    }

    public static BlockEntityType<OldChestBlockEntity> getOldChestBlockEntityType() {
        return oldChestBlockEntityType.getValue();
    }

    public static BlockEntityType<BarrelBlockEntity> getBarrelBlockEntityType() {
        return barrelBlockEntityType.getValue();
    }

    public static BlockEntityType<MiniChestBlockEntity> getMiniChestBlockEntityType() {
        return miniChestBlockEntityType.getValue();
    }

    public static ResourceLocation[] getChestTextures(List<ResourceLocation> blocks) {
        ResourceLocation[] textures = new ResourceLocation[blocks.size() * EsChestType.values().length];
        int index = 0;
        for (ResourceLocation blockId : blocks) {
            for (EsChestType type : EsChestType.values()) {
                textures[index++] = CommonMain.getChestTexture(blockId, type);
            }
        }
        return textures;
    }

    private static boolean upgradeSingleBlockToChest(Level world, BlockState state, BlockPos pos, ResourceLocation from, ResourceLocation to) {
        Block block = state.getBlock();
        boolean isExpandedStorageChest = block instanceof ChestBlock;
        int inventorySize = !isExpandedStorageChest ? Utils.WOOD_STACK_COUNT : ((OpenableBlock) block).getSlotCount();
        if (isExpandedStorageChest && ((OpenableBlock) block).getObjTier() == from || !isExpandedStorageChest && from == Utils.WOOD_TIER_ID) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            //noinspection ConstantConditions
            CompoundTag tag = blockEntity.saveWithoutMetadata();
            boolean verifiedSize = blockEntity instanceof Container inventory && inventory.getContainerSize() == inventorySize;
            if (!verifiedSize) { // Cannot verify inventory size, we'll let it upgrade if it has or has less than 27 items
                if (tag.contains("Items", Tag.TAG_LIST)) {
                    ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
                    if (items.size() <= inventorySize) {
                        verifiedSize = true;
                    }
                }
            }
            if (verifiedSize) {
                ChestBlock toBlock = (ChestBlock) CommonMain.getTieredObject(CommonMain.CHEST_OBJECT_TYPE, to);
                NonNullList<ItemStack> inventory = NonNullList.withSize(toBlock.getSlotCount(), ItemStack.EMPTY);
                LockCode code = LockCode.fromTag(tag);
                ContainerHelper.loadAllItems(tag, inventory);
                world.removeBlockEntity(pos);
                // Needs fixing up to check for vanilla states.
                BlockState newState = toBlock.defaultBlockState()
                                             .setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                                             .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                if (state.hasProperty(ChestBlock.CURSED_CHEST_TYPE)) {
                    newState = newState.setValue(ChestBlock.CURSED_CHEST_TYPE, state.getValue(ChestBlock.CURSED_CHEST_TYPE));
                } else if (state.hasProperty(BlockStateProperties.CHEST_TYPE)) {
                    ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);
                    newState = newState.setValue(ChestBlock.CURSED_CHEST_TYPE, type == ChestType.LEFT ? EsChestType.RIGHT : type == ChestType.RIGHT ? EsChestType.LEFT : EsChestType.SINGLE);
                }
                if (world.setBlockAndUpdate(pos, newState)) {
                    BlockEntity newEntity = world.getBlockEntity(pos);
                    //noinspection ConstantConditions
                    CompoundTag newTag = newEntity.saveWithoutMetadata();
                    ContainerHelper.saveAllItems(newTag, inventory);
                    code.addToTag(newTag);
                    newEntity.load(newTag);
                    return true;
                } else {
                    world.setBlockEntity(blockEntity);
                }
            }
        }
        return false;
    }

    private static boolean upgradeSingleBlockToOldChest(Level world, BlockState state, BlockPos pos, ResourceLocation from, ResourceLocation to) {
        if (((OpenableBlock) state.getBlock()).getObjTier() == from) {
            AbstractChestBlock toBlock = (AbstractChestBlock) CommonMain.getTieredObject(CommonMain.OLD_CHEST_OBJECT_TYPE, to);
            NonNullList<ItemStack> inventory = NonNullList.withSize(toBlock.getSlotCount(), ItemStack.EMPTY);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            //noinspection ConstantConditions
            CompoundTag tag = blockEntity.saveWithoutMetadata();
            LockCode code = LockCode.fromTag(tag);
            ContainerHelper.loadAllItems(tag, inventory);
            world.removeBlockEntity(pos);
            BlockState newState = toBlock.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING)).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE));
            if (world.setBlockAndUpdate(pos, newState)) {
                BlockEntity newEntity = world.getBlockEntity(pos);
                //noinspection ConstantConditions
                CompoundTag newTag = newEntity.saveWithoutMetadata();
                ContainerHelper.saveAllItems(newTag, inventory);
                code.addToTag(newTag);
                newEntity.load(newTag);
                return true;
            } else {
                world.setBlockEntity(blockEntity);
            }
        }
        return false;
    }

    private static void defineTierUpgradePath(List<NamedValue<Item>> items, boolean wrapTooltipManually, CreativeModeTab group, Tier... tiers) {
        int numTiers = tiers.length;
        for (int fromIndex = 0; fromIndex < numTiers - 1; fromIndex++) {
            Tier fromTier = tiers[fromIndex];
            for (int toIndex = fromIndex + 1; toIndex < numTiers; toIndex++) {
                Tier toTier = tiers[toIndex];
                ResourceLocation itemId = Utils.id(fromTier.getId().getPath() + "_to_" + toTier.getId().getPath() + "_conversion_kit");
                Item.Properties settings = fromTier.getItemSettings()
                                                   .andThen(toTier.getItemSettings())
                                                   .apply(new Item.Properties().tab(group).stacksTo(16));
                items.add(new NamedValue<>(itemId, () -> new StorageConversionKit(settings, fromTier.getId(), toTier.getId(), wrapTooltipManually)));
            }
        }
    }

    public static BlockUpgradeBehaviour getBlockUpgradeBehaviour(Block block) {
        for (Map.Entry<Predicate<Block>, BlockUpgradeBehaviour> entry : CommonMain.BLOCK_UPGRADE_BEHAVIOURS.entrySet()) {
            if (entry.getKey().test(block)) return entry.getValue();
        }
        return null;
    }

    public static EntityUpgradeBehaviour getEntityUpgradeBehaviour(Entity entity) {
        for (Map.Entry<Predicate<Entity>, EntityUpgradeBehaviour> entry : CommonMain.ENTITY_UPGRADE_BEHAVIOURS.entrySet()) {
            if (entry.getKey().test(entity)) return entry.getValue();
        }
        return null;
    }

    private static void defineBlockUpgradeBehaviour(Predicate<Block> target, BlockUpgradeBehaviour behaviour) {
        CommonMain.BLOCK_UPGRADE_BEHAVIOURS.put(target, behaviour);
    }

    private static void defineEntityUpgradeBehaviour(Predicate<Entity> target, EntityUpgradeBehaviour behaviour) {
        CommonMain.ENTITY_UPGRADE_BEHAVIOURS.put(target, behaviour);
    }

    public static void registerTieredObject(TieredObject object) {
        CommonMain.TIERED_OBJECTS.putIfAbsent(Map.entry(object.getObjType(), object.getObjTier()), object);
    }

    public static TieredObject getTieredObject(ResourceLocation objectType, ResourceLocation objectTier) {
        return CommonMain.TIERED_OBJECTS.get(Map.entry(objectType, objectTier));
    }

    public static void declareChestTextures(ResourceLocation block, ResourceLocation singleTexture, ResourceLocation leftTexture, ResourceLocation rightTexture, ResourceLocation topTexture, ResourceLocation bottomTexture, ResourceLocation frontTexture, ResourceLocation backTexture) {
        if (!CommonMain.CHEST_TEXTURES.containsKey(block)) {
            TextureCollection collection = new TextureCollection(singleTexture, leftTexture, rightTexture, topTexture, bottomTexture, frontTexture, backTexture);
            CommonMain.CHEST_TEXTURES.put(block, collection);
        } else {
            throw new IllegalArgumentException("Tried registering chest textures for \"" + block + "\" which already has textures.");
        }
    }

    public static ResourceLocation getChestTexture(ResourceLocation block, EsChestType chestType) {
        if (CommonMain.CHEST_TEXTURES.containsKey(block)) return CommonMain.CHEST_TEXTURES.get(block).getTexture(chestType);
        return MissingTextureAtlasSprite.getLocation();
    }

    private static void registerMutationBehaviour(Predicate<Block> predicate, MutationMode mode, BlockMutatorBehaviour behaviour) {
        CommonMain.BLOCK_MUTATOR_BEHAVIOURS.put(Map.entry(predicate, mode), behaviour);
    }

    private static void registerMutationBehaviour(Predicate<Entity> predicate, MutationMode mode, EntityMutatorBehaviour behaviour) {
        CommonMain.ENTITY_MUTATOR_BEHAVIOURS.put(Map.entry(predicate, mode), behaviour);
    }

    public static BlockMutatorBehaviour getBlockMutatorBehaviour(Block block, MutationMode mode) {
        for (Map.Entry<Map.Entry<Predicate<Block>, MutationMode>, BlockMutatorBehaviour> entry : CommonMain.BLOCK_MUTATOR_BEHAVIOURS.entrySet()) {
            Map.Entry<Predicate<Block>, MutationMode> pair = entry.getKey();
            if (pair.getValue() == mode && pair.getKey().test(block)) return entry.getValue();
        }
        return null;
    }

    public static EntityMutatorBehaviour getEntityMutatorBehaviour(Entity entity, MutationMode mode) {
        for (Map.Entry<Map.Entry<Predicate<Entity>, MutationMode>, EntityMutatorBehaviour> entry : CommonMain.ENTITY_MUTATOR_BEHAVIOURS.entrySet()) {
            Map.Entry<Predicate<Entity>, MutationMode> pair = entry.getKey();
            if (pair.getValue() == mode && pair.getKey().test(entity)) return entry.getValue();
        }
        return null;
    }

    public static void constructContent(Function<OpenableBlockEntity, ItemAccess> itemAccess, Supplier<Lockable> lockable,
                                        CreativeModeTab group, boolean isClient, TagReloadListener tagReloadListener, ContentConsumer contentRegistrationConsumer,
            /*Base*/ boolean manuallyWrapTooltips,
            /*Chest*/ TagKey<Block> chestTag, BiFunction<ChestBlock, Item.Properties, BlockItem> chestItemMaker, Function<OpenableBlockEntity, ItemAccess> chestAccessMaker,
            /*Old Chest*/
            /*Barrel*/ TagKey<Block> barrelTag,
            /*Mini Chest*/ BiFunction<MiniChestBlock, Item.Properties, BlockItem> miniChestItemMaker) {
        CommonMain.itemAccess = itemAccess;
        CommonMain.lockable = lockable;

        final Tier woodTier = new Tier(Utils.WOOD_TIER_ID, Utils.WOOD_STACK_COUNT, UnaryOperator.identity(), UnaryOperator.identity());
        final Tier copperTier = new Tier(Utils.COPPER_TIER_ID, 45, Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
        final Tier ironTier = new Tier(Utils.id("iron"), 54, Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
        final Tier goldTier = new Tier(Utils.id("gold"), 81, Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
        final Tier diamondTier = new Tier(Utils.id("diamond"), 108, Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
        final Tier obsidianTier = new Tier(Utils.id("obsidian"), 108, Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
        final Tier netheriteTier = new Tier(Utils.id("netherite"), 135, Properties::requiresCorrectToolForDrops, Item.Properties::fireResistant);
        final Properties woodSettings = Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.5f).sound(SoundType.WOOD);
        final Properties pumpkinSettings = Properties.of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE).strength(1).sound(SoundType.WOOD);
        final Properties bambooSettings = Properties.of(Material.BAMBOO, MaterialColor.PLANT).strength(1).sound(SoundType.BAMBOO);
        final Properties copperSettings = Properties.of(Material.METAL, MaterialColor.COLOR_ORANGE).strength(3.0F, 6.0F).sound(SoundType.COPPER);
        final Properties ironSettings = Properties.of(Material.METAL, MaterialColor.METAL).strength(5, 6).sound(SoundType.METAL);
        final Properties goldSettings = Properties.of(Material.METAL, MaterialColor.GOLD).strength(3, 6).sound(SoundType.METAL);
        final Properties diamondSettings = Properties.of(Material.METAL, MaterialColor.DIAMOND).strength(5, 6).sound(SoundType.METAL);
        final Properties obsidianSettings = Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(50, 1200);
        final Properties netheriteSettings = Properties.of(Material.METAL, MaterialColor.COLOR_BLACK).strength(50, 1200).sound(SoundType.NETHERITE_BLOCK);
        List<ResourceLocation> stats = new ArrayList<>();
        Function<String, ResourceLocation> statMaker = (id) -> {
            ResourceLocation statId = Utils.id(id);
            stats.add(statId);
            return statId;
        };

        List<NamedValue<Item>> baseItems = new ArrayList<>(22);
        /*Base*/
        {
            baseItems.add(new NamedValue<>(Utils.id("storage_mutator"), () -> new StorageMutator(new Item.Properties().stacksTo(1).tab(group))));
            CommonMain.defineTierUpgradePath(baseItems, manuallyWrapTooltips, group, woodTier, copperTier, ironTier, goldTier, diamondTier, obsidianTier, netheriteTier);
        }

        List<NamedValue<ChestBlock>> chestBlocks = new ArrayList<>(6 + 3);
        List<NamedValue<BlockItem>> chestItems = new ArrayList<>(6 + 3);
        List<NamedValue<EntityType<ChestMinecart>>> chestMinecartEntityTypes = new ArrayList<>(6 + 3);
        List<NamedValue<ChestMinecartItem>> chestMinecartItems = new ArrayList<>(6 + 3);
        /*Chest*/
        {
            final ResourceLocation woodStat = statMaker.apply("open_wood_chest");
            final ResourceLocation pumpkinStat = statMaker.apply("open_pumpkin_chest");
            final ResourceLocation presentStat = statMaker.apply("open_present");
            final ResourceLocation bambooStat = statMaker.apply("open_bamboo_chest");
//            final ResourceLocation copperStat = statMaker.apply("open_copper_chest");
            final ResourceLocation ironStat = statMaker.apply("open_iron_chest");
            final ResourceLocation goldStat = statMaker.apply("open_gold_chest");
            final ResourceLocation diamondStat = statMaker.apply("open_diamond_chest");
            final ResourceLocation obsidianStat = statMaker.apply("open_obsidian_chest");
            final ResourceLocation netheriteStat = statMaker.apply("open_netherite_chest");

            final Properties presentSettings = Properties.of(Material.WOOD, state -> {
                EsChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
                if (type == EsChestType.SINGLE) return MaterialColor.COLOR_RED;
                else if (type == EsChestType.FRONT || type == EsChestType.BACK) return MaterialColor.PLANT;
                return MaterialColor.SNOW;
            }).strength(2.5f).sound(SoundType.WOOD);

            ObjectConsumer chestMaker = (id, stat, tier, settings) -> {
                NamedValue<ChestBlock> block = new NamedValue<>(id, () -> new ChestBlock(tier.getBlockSettings().apply(settings), id, tier.getId(), stat, tier.getSlotCount()));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> chestItemMaker.apply(block.getValue(), tier.getItemSettings().apply(new Item.Properties().tab(group))));
                ResourceLocation cartId = new ResourceLocation(id.getNamespace(), id.getPath() + "_minecart");
                NamedValue<ChestMinecartItem> cartItem = new NamedValue<>(cartId, () -> new ChestMinecartItem(new Item.Properties().tab(group), cartId));
                NamedValue<EntityType<ChestMinecart>> cartEntityType = new NamedValue<>(cartId, () -> new TieredEntityType<>((type, level) -> {
                    return new ChestMinecart(type, level, cartItem.getValue(), block.getValue());
                }, MobCategory.MISC, true, true, false, false, ImmutableSet.of(), EntityDimensions.scalable(0.98F, 0.7F), 8, 3, CommonMain.MINECART_CHEST_OBJECT_TYPE, block.getValue().getObjTier()));
                chestBlocks.add(block);
                chestItems.add(item);
                chestMinecartEntityTypes.add(cartEntityType);
                chestMinecartItems.add(cartItem);
            };

//            BiConsumer<ResourceLocation, WeatheringCopper.WeatherState> copperChestMaker = (id, weatherState) -> {
//                NamedValue<ChestBlock> block = new NamedValue<>(id, () -> new CopperChestBlock(copperTier.getBlockSettings().apply(copperSettings), id, copperStat, copperTier.getSlotCount(), weatherState));
//                NamedValue<BlockItem> item = new NamedValue<>(id, () -> new BlockItem(block.getValue(), copperTier.getItemSettings().apply(new Item.Properties().tab(group))));
////                ResourceLocation cartId = new ResourceLocation(id.getNamespace(), id.getPath() + "_minecart");
////                NamedValue<ChestMinecartItem> cartItem = new NamedValue<>(cartId, () -> new ChestMinecartItem(new Item.Properties().tab(group), cartId));
////                NamedValue<EntityType<ChestMinecart>> cartEntityType = new NamedValue<>(cartId, () -> EntityType.Builder.<ChestMinecart>of((type, level) -> {
////                    return new ChestMinecart(type, level, cartItem.getValue(), block.getValue());
////                }, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8).build(cartId.getPath()));
//                chestBlocks.add(block);
//                chestItems.add(item);
////                chestMinecartEntityTypes.add(cartEntityType);
////                chestMinecartItems.add(cartItem);
//            };

            chestMaker.apply(Utils.id("wood_chest"), woodStat, woodTier, woodSettings);
            chestMaker.apply(Utils.id("pumpkin_chest"), pumpkinStat, woodTier, pumpkinSettings);
            chestMaker.apply(Utils.id("present"), presentStat, woodTier, presentSettings);
            chestMaker.apply(Utils.id("bamboo_chest"), bambooStat, woodTier, bambooSettings);
//            copperChestMaker.accept(Utils.id("copper_chest"), WeatheringCopper.WeatherState.UNAFFECTED);
//            copperChestMaker.accept(Utils.id("exposed_copper_chest"), WeatheringCopper.WeatherState.EXPOSED);
//            copperChestMaker.accept(Utils.id("weathered_copper_chest"), WeatheringCopper.WeatherState.WEATHERED);
//            copperChestMaker.accept(Utils.id("oxidized_copper_chest"), WeatheringCopper.WeatherState.OXIDIZED);
//            chestMaker.apply(Utils.id("waxed_copper_chest"), copperStat, copperTier, copperSettings);
//            chestMaker.apply(Utils.id("waxed_exposed_copper_chest"), copperStat, copperTier, copperSettings);
//            chestMaker.apply(Utils.id("waxed_weathered_copper_chest"), copperStat, copperTier, copperSettings);
//            chestMaker.apply(Utils.id("waxed_oxidized_copper_chest"), copperStat, copperTier, copperSettings);
            chestMaker.apply(Utils.id("iron_chest"), ironStat, ironTier, ironSettings);
            chestMaker.apply(Utils.id("gold_chest"), goldStat, goldTier, goldSettings);
            chestMaker.apply(Utils.id("diamond_chest"), diamondStat, diamondTier, diamondSettings);
            chestMaker.apply(Utils.id("obsidian_chest"), obsidianStat, obsidianTier, obsidianSettings);
            chestMaker.apply(Utils.id("netherite_chest"), netheriteStat, netheriteTier, netheriteSettings);

            if (isClient) {
                chestBlocks.forEach(block -> {
                    String blockId = block.getName().getPath();
                    CommonMain.declareChestTextures(block.getName(),
                            Utils.id("entity/" + blockId + "/single"),
                            Utils.id("entity/" + blockId + "/left"),
                            Utils.id("entity/" + blockId + "/right"),
                            Utils.id("entity/" + blockId + "/top"),
                            Utils.id("entity/" + blockId + "/bottom"),
                            Utils.id("entity/" + blockId + "/front"),
                            Utils.id("entity/" + blockId + "/back")
                    );
                });
            }

            CommonMain.chestBlockEntityType = new NamedValue<>(CommonMain.CHEST_OBJECT_TYPE, () -> BlockEntityType.Builder.of((pos, state) -> new ChestBlockEntity(CommonMain.getChestBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), chestAccessMaker, CommonMain.lockable), chestBlocks.stream().map(NamedValue::getValue).toArray(ChestBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.CHEST_OBJECT_TYPE.toString())));

            Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof ChestBlock || block instanceof net.minecraft.world.level.block.ChestBlock || block.defaultBlockState().is(chestTag);
            CommonMain.defineBlockUpgradeBehaviour(isUpgradableChestBlock, (context, from, to) -> {
                Level world = context.getLevel();
                BlockPos pos = context.getClickedPos();
                BlockState state = world.getBlockState(pos);
                Player player = context.getPlayer();
                ItemStack handStack = context.getItemInHand();
                if (state.getBlock() instanceof ChestBlock) {
                    EsChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
                    Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (AbstractChestBlock.getBlockType(type) == DoubleBlockCombiner.BlockType.SINGLE) {
                        boolean upgradeSucceeded = CommonMain.upgradeSingleBlockToChest(world, state, pos, from, to);
                        if (upgradeSucceeded) handStack.shrink(1);
                        return upgradeSucceeded;
                    } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                        BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(type, facing));
                        BlockState otherState = world.getBlockState(otherPos);
                        boolean firstSucceeded = CommonMain.upgradeSingleBlockToChest(world, state, pos, from, to);
                        boolean secondSucceeded = CommonMain.upgradeSingleBlockToChest(world, otherState, otherPos, from, to);
                        if (firstSucceeded && secondSucceeded) handStack.shrink(2);
                        else if (firstSucceeded || secondSucceeded) handStack.shrink(1);
                        return firstSucceeded || secondSucceeded;
                    }
                } else {
                    if (net.minecraft.world.level.block.ChestBlock.getBlockType(state) == DoubleBlockCombiner.BlockType.SINGLE) {
                        boolean upgradeSucceeded = CommonMain.upgradeSingleBlockToChest(world, state, pos, from, to);
                        if (upgradeSucceeded) handStack.shrink(1);
                        return upgradeSucceeded;
                    } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                        BlockPos otherPos = pos.relative(net.minecraft.world.level.block.ChestBlock.getConnectedDirection(state));
                        BlockState otherState = world.getBlockState(otherPos);
                        boolean firstSucceeded = CommonMain.upgradeSingleBlockToChest(world, state, pos, from, to);
                        boolean secondSucceeded = CommonMain.upgradeSingleBlockToChest(world, otherState, otherPos, from, to);
                        if (firstSucceeded && secondSucceeded) handStack.shrink(2);
                        else if (firstSucceeded || secondSucceeded) handStack.shrink(1);
                        return firstSucceeded || secondSucceeded;
                    }
                }

                return false;
            });

            CommonMain.defineEntityUpgradeBehaviour(e -> e instanceof ChestMinecart || e instanceof MinecartChest, (entity, from, to) -> {
                if (entity instanceof MinecartChest minecartChest && from.equals(Utils.WOOD_TIER_ID)) {
                    var toObject = (TieredEntityType<ChestMinecart>) CommonMain.getTieredObject(CommonMain.MINECART_CHEST_OBJECT_TYPE, to);
                    if (toObject != null) {
                        System.out.println(toObject.getObjTier() + ": " + toObject.getObjType());
                        NonNullList<ItemStack> items = minecartChest.getItemStacks();
                        return true;
                    }
                } else if (entity instanceof ChestMinecart minecartChest && ((TieredEntityType<ChestMinecart>) minecartChest.getType()).getObjTier().equals(from)) {
                    var toObject = (TieredEntityType<ChestMinecart>) CommonMain.getTieredObject(CommonMain.MINECART_CHEST_OBJECT_TYPE, to);
                    if (toObject != null) {
                        System.out.println(toObject.getObjTier() + ": " + toObject.getObjType());
                        NonNullList<ItemStack> items = minecartChest.getItems();
                        return true;
                    }
                }
                return false;
            });

            CommonMain.registerMutationBehaviour(b -> b instanceof ChestBlock, MutationMode.SWAP_THEME, (context, world, state, pos, stack) -> {
                List<Block> blocks = tagReloadListener.getChestCycleBlocks();
                int index = blocks.indexOf(state.getBlock());
                if (index != -1) { // Cannot change style e.g. iron chest, ect.
                    Block next = blocks.get((index + 1) % blocks.size());
                    EsChestType chestType = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
                    if (chestType != EsChestType.SINGLE) {
                        BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                        BlockState otherState = world.getBlockState(otherPos);
                        world.setBlock(otherPos, next.withPropertiesOf(otherState), Block.UPDATE_SUPPRESS_LIGHT);
                    }
                    world.setBlockAndUpdate(pos, next.withPropertiesOf(state));
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            });

            CommonMain.registerMutationBehaviour(e -> e instanceof ChestMinecart, MutationMode.SWAP_THEME, (level, entity, stack) -> {
                List<? extends EntityType<?>> entityTypes = tagReloadListener.getMinecartChestCycleEntityTypes();
                int index = entityTypes.indexOf(entity.getType());
                if (index != -1) { // Cannot change style e.g. iron chest, ect.
                    EntityType<ChestMinecart> next = (EntityType<ChestMinecart>) entityTypes.get((index + 1) % entityTypes.size());
                    CompoundTag saved = new CompoundTag();
                    entity.save(saved);
                    ChestMinecart newCart = next.spawn((ServerLevel) level, saved, stack.hasCustomHoverName() ? stack.getHoverName() : null, null, entity.getOnPos(), MobSpawnType.COMMAND, true, false);
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            });
        }

        List<NamedValue<AbstractChestBlock>> oldChestBlocks = new ArrayList<>(6);
        List<NamedValue<BlockItem>> oldChestItems = new ArrayList<>(6);
        /*Old Chest*/
        {
            final ResourceLocation woodStat = statMaker.apply("open_old_wood_chest");
//            final ResourceLocation copperStat = statMaker.apply("open_old_copper_chest");
            final ResourceLocation ironStat = statMaker.apply("open_old_iron_chest");
            final ResourceLocation goldStat = statMaker.apply("open_old_gold_chest");
            final ResourceLocation diamondStat = statMaker.apply("open_old_diamond_chest");
            final ResourceLocation obsidianStat = statMaker.apply("open_old_obsidian_chest");
            final ResourceLocation netheriteStat = statMaker.apply("open_old_netherite_chest");
            ObjectConsumer chestMaker = (id, stat, tier, settings) -> {
                NamedValue<AbstractChestBlock> block = new NamedValue<>(id, () -> new AbstractChestBlock(tier.getBlockSettings().apply(settings), id, tier.getId(), stat, tier.getSlotCount()));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> new BlockItem(block.getValue(), tier.getItemSettings().apply(new Item.Properties().tab(group))));
                oldChestBlocks.add(block);
                oldChestItems.add(item);
            };

//            BiConsumer<ResourceLocation, WeatheringCopper.WeatherState> copperChestMaker = (id, weatherState) -> {
//                NamedValue<AbstractChestBlock> block = new NamedValue<>(id, () -> new OldCopperChestBlock(copperTier.getBlockSettings().apply(copperSettings), id, copperStat, copperTier.getSlotCount(), weatherState));
//                NamedValue<BlockItem> item = new NamedValue<>(id, () -> new BlockItem(block.getValue(), copperTier.getItemSettings().apply(new Item.Properties().tab(group))));
//                oldChestBlocks.add(block);
//                oldChestItems.add(item);
//            };

            chestMaker.apply(Utils.id("old_wood_chest"), woodStat, woodTier, woodSettings);
//            copperChestMaker.accept(Utils.id("old_copper_chest"), WeatheringCopper.WeatherState.UNAFFECTED);
//            copperChestMaker.accept(Utils.id("old_exposed_copper_chest"), WeatheringCopper.WeatherState.EXPOSED);
//            copperChestMaker.accept(Utils.id("old_weathered_copper_chest"), WeatheringCopper.WeatherState.WEATHERED);
//            copperChestMaker.accept(Utils.id("old_oxidized_copper_chest"), WeatheringCopper.WeatherState.OXIDIZED);
//            chestMaker.apply(Utils.id("waxed_old_copper_chest"), copperStat, copperTier, copperSettings);
//            chestMaker.apply(Utils.id("waxed_old_exposed_copper_chest"), copperStat, copperTier, copperSettings);
//            chestMaker.apply(Utils.id("waxed_old_weathered_copper_chest"), copperStat, copperTier, copperSettings);
//            chestMaker.apply(Utils.id("waxed_old_oxidized_copper_chest"), copperStat, copperTier, copperSettings);
            chestMaker.apply(Utils.id("old_iron_chest"), ironStat, ironTier, ironSettings);
            chestMaker.apply(Utils.id("old_gold_chest"), goldStat, goldTier, goldSettings);
            chestMaker.apply(Utils.id("old_diamond_chest"), diamondStat, diamondTier, diamondSettings);
            chestMaker.apply(Utils.id("old_obsidian_chest"), obsidianStat, obsidianTier, obsidianSettings);
            chestMaker.apply(Utils.id("old_netherite_chest"), netheriteStat, netheriteTier, netheriteSettings);

            CommonMain.oldChestBlockEntityType = new NamedValue<>(CommonMain.OLD_CHEST_OBJECT_TYPE, () -> BlockEntityType.Builder.of((pos, state) -> new OldChestBlockEntity(CommonMain.getOldChestBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), chestAccessMaker, CommonMain.lockable), oldChestBlocks.stream().map(NamedValue::getValue).toArray(AbstractChestBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.OLD_CHEST_OBJECT_TYPE.toString())));

            Predicate<Block> isUpgradableOldChestBlock = (block) -> block.getClass() == AbstractChestBlock.class;
            CommonMain.defineBlockUpgradeBehaviour(isUpgradableOldChestBlock, (context, from, to) -> {
                Level world = context.getLevel();
                BlockPos pos = context.getClickedPos();
                BlockState state = world.getBlockState(pos);
                Player player = context.getPlayer();
                ItemStack handStack = context.getItemInHand();
                if (AbstractChestBlock.getBlockType(state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE)) == DoubleBlockCombiner.BlockType.SINGLE) {
                    boolean upgradeSucceeded = CommonMain.upgradeSingleBlockToOldChest(world, state, pos, from, to);
                    if (upgradeSucceeded) handStack.shrink(1);
                    return upgradeSucceeded;
                } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                    BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                    BlockState otherState = world.getBlockState(otherPos);
                    boolean firstSucceeded = CommonMain.upgradeSingleBlockToOldChest(world, state, pos, from, to);
                    boolean secondSucceeded = CommonMain.upgradeSingleBlockToOldChest(world, otherState, otherPos, from, to);
                    if (firstSucceeded && secondSucceeded) handStack.shrink(2);
                    else if (firstSucceeded || secondSucceeded) handStack.shrink(1);
                    return firstSucceeded || secondSucceeded;
                }
                return false;
            });
        }

        /*Both Chests*/
        {
            Predicate<Block> isChestBlock = b -> b instanceof AbstractChestBlock;
            CommonMain.registerMutationBehaviour(isChestBlock, MutationMode.MERGE, (context, world, state, pos, stack) -> {
                Player player = context.getPlayer();
                if (state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == EsChestType.SINGLE) {
                    CompoundTag tag = stack.getOrCreateTag();
                    if (tag.contains("pos")) {
                        BlockPos otherPos = NbtUtils.readBlockPos(tag.getCompound("pos"));
                        BlockState otherState = world.getBlockState(otherPos);
                        Direction direction = Direction.fromNormal(otherPos.subtract(pos));
                        if (direction != null) {
                            if (state.getBlock() == otherState.getBlock()) {
                                if (otherState.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == EsChestType.SINGLE) {
                                    if (state.getValue(BlockStateProperties.HORIZONTAL_FACING) == otherState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                                        if (!world.isClientSide()) {
                                            EsChestType chestType = AbstractChestBlock.getChestType(state.getValue(BlockStateProperties.HORIZONTAL_FACING), direction);
                                            world.setBlockAndUpdate(pos, state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, chestType));
                                            // note: other state is updated via neighbour update
                                            tag.remove("pos");
                                            //noinspection ConstantConditions
                                            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_end"), true);
                                        }
                                        return InteractionResult.SUCCESS;
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
                                player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_wrong_block", state.getBlock().getName()), true);
                            }
                        } else {
                            //noinspection ConstantConditions
                            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_not_adjacent"), true);
                        }
                    } else {
                        if (!world.isClientSide()) {
                            tag.put("pos", NbtUtils.writeBlockPos(pos));
                            //noinspection ConstantConditions
                            player.displayClientMessage(Component.translatable("tooltip.expandedstorage.storage_mutator.merge_start", Utils.ALT_USE), true);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.FAIL;
            });
            CommonMain.registerMutationBehaviour(isChestBlock, MutationMode.SPLIT, (context, world, state, pos, stack) -> {
                if (state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) != EsChestType.SINGLE) {
                    if (!world.isClientSide()) {
                        world.setBlockAndUpdate(pos, state.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, EsChestType.SINGLE));
                        // note: other state is updated to single via neighbour update
                    }
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            });
            CommonMain.registerMutationBehaviour(isChestBlock, MutationMode.ROTATE, (context, world, state, pos, stack) -> {
                if (!world.isClientSide()) {
                    EsChestType chestType = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
                    if (chestType == EsChestType.SINGLE) {
                        world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
                    } else {
                        BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                        BlockState otherState = world.getBlockState(otherPos);
                        if (chestType == EsChestType.TOP || chestType == EsChestType.BOTTOM) {
                            world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
                            world.setBlockAndUpdate(otherPos, otherState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
                        } else {
                            world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE).getOpposite()));
                            world.setBlockAndUpdate(otherPos, otherState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, otherState.getValue(AbstractChestBlock.CURSED_CHEST_TYPE).getOpposite()));
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            });
        }

        List<NamedValue<BarrelBlock>> barrelBlocks = new ArrayList<>(6 - 1);
        List<NamedValue<BlockItem>> barrelItems = new ArrayList<>(6 - 1);
        /*Barrel*/
        {
            final ResourceLocation copperStat = statMaker.apply("open_copper_barrel");
            final ResourceLocation ironStat = statMaker.apply("open_iron_barrel");
            final ResourceLocation goldStat = statMaker.apply("open_gold_barrel");
            final ResourceLocation diamondStat = statMaker.apply("open_diamond_barrel");
            final ResourceLocation obsidianStat = statMaker.apply("open_obsidian_barrel");
            final ResourceLocation netheriteStat = statMaker.apply("open_netherite_barrel");

            final Properties copperBarrelSettings = Properties.of(Material.WOOD).strength(3, 6).sound(SoundType.WOOD);
            final Properties ironBarrelSettings = Properties.of(Material.WOOD).strength(5, 6).sound(SoundType.WOOD);
            final Properties goldBarrelSettings = Properties.of(Material.WOOD).strength(3, 6).sound(SoundType.WOOD);
            final Properties diamondBarrelSettings = Properties.of(Material.WOOD).strength(5, 6).sound(SoundType.WOOD);
            final Properties obsidianBarrelSettings = Properties.of(Material.WOOD).strength(50, 1200).sound(SoundType.WOOD);
            final Properties netheriteBarrelSettings = Properties.of(Material.WOOD).strength(50, 1200).sound(SoundType.WOOD);

            ObjectConsumer barrelMaker = (id, stat, tier, settings) -> {
                NamedValue<BarrelBlock> block = new NamedValue<>(id, () -> new BarrelBlock(tier.getBlockSettings().apply(settings), id, tier.getId(), stat, tier.getSlotCount()));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> new BlockItem(block.getValue(), tier.getItemSettings().apply(new Item.Properties().tab(group))));
                barrelBlocks.add(block);
                barrelItems.add(item);
            };

            BiConsumer<ResourceLocation, WeatheringCopper.WeatherState> copperBarrelMaker = (id, weatherState) -> {
                NamedValue<BarrelBlock> block = new NamedValue<>(id, () -> new CopperBarrelBlock(copperTier.getBlockSettings().apply(copperSettings), id, copperStat, copperTier.getSlotCount(), weatherState));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> new BlockItem(block.getValue(), copperTier.getItemSettings().apply(new Item.Properties().tab(group))));
                barrelBlocks.add(block);
                barrelItems.add(item);
            };

            copperBarrelMaker.accept(Utils.id("copper_barrel"), WeatheringCopper.WeatherState.UNAFFECTED);
            copperBarrelMaker.accept(Utils.id("exposed_copper_barrel"), WeatheringCopper.WeatherState.EXPOSED);
            copperBarrelMaker.accept(Utils.id("weathered_copper_barrel"), WeatheringCopper.WeatherState.WEATHERED);
            copperBarrelMaker.accept(Utils.id("oxidized_copper_barrel"), WeatheringCopper.WeatherState.OXIDIZED);
            barrelMaker.apply(Utils.id("waxed_copper_barrel"), copperStat, copperTier, copperBarrelSettings);
            barrelMaker.apply(Utils.id("waxed_exposed_copper_barrel"), copperStat, copperTier, copperBarrelSettings);
            barrelMaker.apply(Utils.id("waxed_weathered_copper_barrel"), copperStat, copperTier, copperBarrelSettings);
            barrelMaker.apply(Utils.id("waxed_oxidized_copper_barrel"), copperStat, copperTier, copperBarrelSettings);
            barrelMaker.apply(Utils.id("iron_barrel"), ironStat, ironTier, ironBarrelSettings);
            barrelMaker.apply(Utils.id("gold_barrel"), goldStat, goldTier, goldBarrelSettings);
            barrelMaker.apply(Utils.id("diamond_barrel"), diamondStat, diamondTier, diamondBarrelSettings);
            barrelMaker.apply(Utils.id("obsidian_barrel"), obsidianStat, obsidianTier, obsidianBarrelSettings);
            barrelMaker.apply(Utils.id("netherite_barrel"), netheriteStat, netheriteTier, netheriteBarrelSettings);

            CommonMain.barrelBlockEntityType = new NamedValue<>(CommonMain.BARREL_OBJECT_TYPE, () -> BlockEntityType.Builder.of((pos, state) -> new BarrelBlockEntity(CommonMain.getBarrelBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), CommonMain.itemAccess, CommonMain.lockable), barrelBlocks.stream().map(NamedValue::getValue).toArray(BarrelBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.BARREL_OBJECT_TYPE.toString())));

            Predicate<Block> isUpgradableBarrelBlock = (block) -> block instanceof BarrelBlock || block instanceof net.minecraft.world.level.block.BarrelBlock || block.defaultBlockState().is(barrelTag);
            CommonMain.defineBlockUpgradeBehaviour(isUpgradableBarrelBlock, (context, from, to) -> {
                Level world = context.getLevel();
                BlockPos pos = context.getClickedPos();
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                boolean isExpandedStorageBarrel = block instanceof BarrelBlock;
                int inventorySize = !isExpandedStorageBarrel ? Utils.WOOD_STACK_COUNT : ((OpenableBlock) block).getSlotCount();
                if (isExpandedStorageBarrel && ((OpenableBlock) block).getObjTier() == from || !isExpandedStorageBarrel && from == Utils.WOOD_TIER_ID) {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    //noinspection ConstantConditions
                    CompoundTag tag = blockEntity.saveWithoutMetadata();
                    boolean verifiedSize = blockEntity instanceof Container inventory && inventory.getContainerSize() == inventorySize;
                    if (!verifiedSize) { // Cannot verify inventory size, we'll let it upgrade if it has or has less than 27 items
                        if (tag.contains("Items", Tag.TAG_LIST)) {
                            ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
                            if (items.size() <= inventorySize) {
                                verifiedSize = true;
                            }
                        }
                    }
                    if (verifiedSize) {
                        OpenableBlock toBlock = (OpenableBlock) CommonMain.getTieredObject(CommonMain.BARREL_OBJECT_TYPE, to);
                        NonNullList<ItemStack> inventory = NonNullList.withSize(toBlock.getSlotCount(), ItemStack.EMPTY);
                        LockCode code = LockCode.fromTag(tag);
                        ContainerHelper.loadAllItems(tag, inventory);
                        world.removeBlockEntity(pos);
                        BlockState newState = toBlock.defaultBlockState().setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                        if (world.setBlockAndUpdate(pos, newState)) {
                            BlockEntity newEntity = world.getBlockEntity(pos);
                            //noinspection ConstantConditions
                            CompoundTag newTag = newEntity.saveWithoutMetadata();
                            ContainerHelper.saveAllItems(newTag, inventory);
                            code.addToTag(newTag);
                            newEntity.load(newTag);
                            context.getItemInHand().shrink(1);
                            return true;
                        } else {
                            world.setBlockEntity(blockEntity);
                        }
                    }
                }
                return false;
            });

            CommonMain.registerMutationBehaviour(isUpgradableBarrelBlock, MutationMode.ROTATE, (context, world, state, pos, stack) -> {
                if (state.hasProperty(BlockStateProperties.FACING)) {
                    if (!world.isClientSide()) {
                        world.setBlockAndUpdate(pos, state.cycle(BlockStateProperties.FACING));
                    }
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            });
        }

        List<NamedValue<MiniChestBlock>> miniChestBlocks = new ArrayList<>();
        List<NamedValue<BlockItem>> miniChestItems = new ArrayList<>();
        /*Mini Chest*/
        {
            final ResourceLocation woodStat = statMaker.apply("open_wood_mini_chest");
            final ResourceLocation pumpkinStat = statMaker.apply("open_pumpkin_mini_chest");
            final ResourceLocation redPresentStat = statMaker.apply("open_red_mini_present");
            final ResourceLocation whitePresentStat = statMaker.apply("open_white_mini_present");
            final ResourceLocation candyCanePresentStat = statMaker.apply("open_candy_cane_mini_present");
            final ResourceLocation greenPresentStat = statMaker.apply("open_green_mini_present");
            final ResourceLocation lavenderPresentStat = statMaker.apply("open_lavender_mini_present");
            final ResourceLocation pinkAmethystPresentStat = statMaker.apply("open_pink_amethyst_mini_present");
            final ResourceLocation ironStat = statMaker.apply("open_iron_mini_chest");
            final ResourceLocation goldStat = statMaker.apply("open_gold_mini_chest");
            final ResourceLocation diamondStat = statMaker.apply("open_diamond_mini_chest");
            final ResourceLocation obsidianStat = statMaker.apply("open_obsidian_mini_chest");
            final ResourceLocation netheriteStat = statMaker.apply("open_netherite_mini_chest");
            // Init block settings
            Properties redPresentSettings = Properties.of(Material.WOOD, MaterialColor.COLOR_RED).strength(2.5f).sound(SoundType.WOOD);
            Properties whitePresentSettings = Properties.of(Material.WOOD, MaterialColor.SNOW).strength(2.5f).sound(SoundType.WOOD);
            Properties candyCanePresentSettings = Properties.of(Material.WOOD, MaterialColor.SNOW).strength(2.5f).sound(SoundType.WOOD);
            Properties greenPresentSettings = Properties.of(Material.WOOD, MaterialColor.PLANT).strength(2.5f).sound(SoundType.WOOD);
            Properties lavenderPresentSettings = Properties.of(Material.WOOD, MaterialColor.COLOR_PURPLE).strength(2.5f).sound(SoundType.WOOD);
            Properties pinkAmethystPresentSettings = Properties.of(Material.WOOD, MaterialColor.COLOR_PURPLE).strength(2.5f).sound(SoundType.WOOD);

            ObjectConsumer chestMaker = (id, stat, tier, settings) -> {
                NamedValue<MiniChestBlock> block = new NamedValue<>(id, () -> new MiniChestBlock(tier.getBlockSettings().apply(settings), id, tier.getId(), stat));
                NamedValue<BlockItem> item = new NamedValue<>(id, () -> miniChestItemMaker.apply(block.getValue(), tier.getItemSettings().apply(new Item.Properties().tab(group))));
                miniChestBlocks.add(block);
                miniChestItems.add(item);

                ResourceLocation sparrowId = new ResourceLocation(id.getNamespace(), id.getPath() + "_with_sparrow");
                NamedValue<MiniChestBlock> block_with_sparrow = new NamedValue<>(sparrowId, () -> new MiniChestBlock(tier.getBlockSettings().apply(settings), sparrowId, tier.getId(), stat));
                NamedValue<BlockItem> item_with_sparrow = new NamedValue<>(sparrowId, () -> miniChestItemMaker.apply(block_with_sparrow.getValue(), tier.getItemSettings().apply(new Item.Properties().tab(group))));
                miniChestBlocks.add(block_with_sparrow);
                miniChestItems.add(item_with_sparrow);
            };

            chestMaker.apply(Utils.id("vanilla_wood_mini_chest"), woodStat, woodTier, woodSettings);
            chestMaker.apply(Utils.id("wood_mini_chest"), woodStat, woodTier, woodSettings);
            chestMaker.apply(Utils.id("pumpkin_mini_chest"), pumpkinStat, woodTier, pumpkinSettings);
            chestMaker.apply(Utils.id("red_mini_present"), redPresentStat, woodTier, redPresentSettings);
            chestMaker.apply(Utils.id("white_mini_present"), whitePresentStat, woodTier, whitePresentSettings);
            chestMaker.apply(Utils.id("candy_cane_mini_present"), candyCanePresentStat, woodTier, candyCanePresentSettings);
            chestMaker.apply(Utils.id("green_mini_present"), greenPresentStat, woodTier, greenPresentSettings);
            chestMaker.apply(Utils.id("lavender_mini_present"), lavenderPresentStat, woodTier, lavenderPresentSettings);
            chestMaker.apply(Utils.id("pink_amethyst_mini_present"), pinkAmethystPresentStat, woodTier, pinkAmethystPresentSettings);
            chestMaker.apply(Utils.id("iron_mini_chest"), ironStat, ironTier, ironSettings);
            chestMaker.apply(Utils.id("gold_mini_chest"), goldStat, goldTier, goldSettings);
            chestMaker.apply(Utils.id("diamond_mini_chest"), diamondStat, diamondTier, diamondSettings);
            chestMaker.apply(Utils.id("obsidian_mini_chest"), obsidianStat, obsidianTier, obsidianSettings);
            chestMaker.apply(Utils.id("netherite_mini_chest"), netheriteStat, netheriteTier, netheriteSettings);

            CommonMain.miniChestBlockEntityType = new NamedValue<>(CommonMain.MINI_CHEST_OBJECT_TYPE, () -> BlockEntityType.Builder.of((pos, state) -> new MiniChestBlockEntity(CommonMain.getMiniChestBlockEntityType(), pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), CommonMain.itemAccess, CommonMain.lockable), miniChestBlocks.stream().map(NamedValue::getValue).toArray(MiniChestBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, CommonMain.MINI_CHEST_OBJECT_TYPE.toString())));

            if (isClient) {
                MiniChestScreen.registerScreenType();
            }

            Predicate<Block> isMiniChest = b -> b instanceof MiniChestBlock;
            CommonMain.registerMutationBehaviour(isMiniChest, MutationMode.ROTATE, (context, world, state, pos, stack) -> {
                if (!world.isClientSide()) {
                    world.setBlockAndUpdate(pos, state.rotate(Rotation.CLOCKWISE_90));
                }
                return InteractionResult.SUCCESS;
            });
            CommonMain.registerMutationBehaviour(isMiniChest, MutationMode.SWAP_THEME, (context, world, state, pos, stack) -> {
                MiniChestBlock block = (MiniChestBlock) state.getBlock();
                String itemName = stack.getHoverName().getString();
                if (block.getObjTier() != Utils.WOOD_TIER_ID && itemName.equals("Sparrow")) {
                    ResourceLocation blockId = block.getBlockId();
                    String newId = blockId.getPath();
                    if (newId.contains("_with_sparrow")) {
                        newId = newId.substring(0, newId.length() - 13);
                    } else {
                        newId = newId + "_with_sparrow";
                    }
                    Block next = Registry.BLOCK.get(new ResourceLocation(blockId.getNamespace(), newId));
                    world.setBlockAndUpdate(pos, next.withPropertiesOf(state));
                    return InteractionResult.SUCCESS;
                } else {
                    List<Block> blocks;
                    if (itemName.equals("Sunrise")) {
                        blocks = tagReloadListener.getMiniChestSecretCycleBlocks();
                    } else if (itemName.equals("Sparrow")) {
                        blocks = tagReloadListener.getMiniChestSecretCycle2Blocks();
                    } else {
                        blocks = tagReloadListener.getMiniChestCycleBlocks();
                    }
                    int index = blocks.indexOf(state.getBlock());
                    if (index != -1) { // Illegal state / misconfigured tag
                        Block next = blocks.get((index + 1) % blocks.size());
                        world.setBlockAndUpdate(pos, next.withPropertiesOf(state));
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.FAIL;
            });
        }

        contentRegistrationConsumer.accept(new Content(
                stats,
                baseItems,

                chestBlocks,
                chestItems,
                chestMinecartEntityTypes,
                chestMinecartItems,
                chestBlockEntityType,

                oldChestBlocks,
                oldChestItems,
                oldChestBlockEntityType,

                barrelBlocks,
                barrelItems,
                barrelBlockEntityType,

                miniChestBlocks,
                miniChestItems,
                miniChestBlockEntityType
        ));
    }

    public static <T> void iterateNamedList(List<NamedValue<? extends T>> list, BiConsumer<ResourceLocation, T> consumer) {
        list.forEach(it -> consumer.accept(it.getName(), it.getValue()));
    }

    public static Optional<ItemAccess> getItemAccess(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof OldChestBlockEntity entity) {
            DoubleItemAccess access = entity.getItemAccess();
            EsChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (access.hasCachedAccess() || type == EsChestType.SINGLE) {
                return Optional.of(access);
            }
            if (world.getBlockEntity(pos.relative(AbstractChestBlock.getDirectionToAttached(type, facing))) instanceof OldChestBlockEntity otherEntity) {
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

    public static InteractionResult interactWithEntity(Level world, Player player, InteractionHand hand, Entity entity) {
        if (player.isSpectator() || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack handStack = player.getItemInHand(hand);
        if (handStack.getItem() instanceof EntityInteractableItem item) {
            if (player.getCooldowns().isOnCooldown(handStack.getItem())) {
                return InteractionResult.CONSUME;
            }
            InteractionResult result = item.es_interactEntity(world, entity, player, hand, handStack);
            if (result == InteractionResult.FAIL) {
                result = InteractionResult.CONSUME;
            }
            return result;
        }
        return InteractionResult.PASS;
    }
}
