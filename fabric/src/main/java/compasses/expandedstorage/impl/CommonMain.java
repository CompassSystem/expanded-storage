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
import compasses.expandedstorage.impl.block.strategies.Lockable;
import compasses.expandedstorage.impl.entity.ChestMinecart;
import compasses.expandedstorage.impl.inventory.OpenableInventory;
import compasses.expandedstorage.impl.item.ChestMinecartItem;
import compasses.expandedstorage.impl.item.StorageConversionKit;
import compasses.expandedstorage.impl.item.StorageMutator;
import compasses.expandedstorage.impl.misc.ScreenHandlerFactoryAdapter;
import compasses.expandedstorage.impl.misc.Tier;
import compasses.expandedstorage.impl.misc.Utils;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class CommonMain {
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

    private static void defineTierUpgradePath(HashSet<ResourceLocation> items, Tier... tiers) {
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

                    Registry.register(BuiltInRegistries.ITEM, itemId, new StorageConversionKit(settings, fromTier.getId(), toTier.getId()));
                }
            }
        }
    }

    public static class Initializer {
        private static final Tier TIERS_WOOD = new Tier(Utils.id("wood"), 27, UnaryOperator.identity(), UnaryOperator.identity());
        private static final Tier TIERS_COPPER = new Tier(Utils.id("copper"), 45, Block.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
        private static final Tier TIERS_IRON = new Tier(Utils.id("iron"), 54, Block.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
        private static final Tier TIERS_GOLD = new Tier(Utils.id("gold"), 81, Block.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
        private static final Tier TIERS_DIAMOND = new Tier(Utils.id("diamond"), 108, Block.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
        private static final Tier TIERS_OBSIDIAN = new Tier(Utils.id("obsidian"), 108, Block.Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
        private static final Tier TIERS_NETHERITE = new Tier(Utils.id("netherite"), 135, Block.Properties::requiresCorrectToolForDrops, Item.Properties::fireResistant);

        private static final BlockBehaviour.Properties PROPERTIES_WOOD = BlockBehaviour.Properties
                .of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5f).sound(SoundType.WOOD).ignitedByLava();
        private static final BlockBehaviour.Properties PROPERTIES_PUMPKIN = BlockBehaviour.Properties
                .of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.DIDGERIDOO).strength(1.0F).sound(SoundType.WOOD);
        private static final BlockBehaviour.Properties PROPERTIES_BAMBOO = BlockBehaviour.Properties
                .of().mapColor(MapColor.PLANT).strength(1).sound(SoundType.BAMBOO).ignitedByLava();
        private static final BlockBehaviour.Properties PROPERTIES_MOSS = BlockBehaviour.Properties
                .of().mapColor(MapColor.COLOR_GREEN).strength(0.1F).sound(SoundType.MOSS);
        private static final BlockBehaviour.Properties PROPERTIES_IRON = BlockBehaviour.Properties
                .of().mapColor(MapColor.METAL).instrument(NoteBlockInstrument.IRON_XYLOPHONE).strength(5, 6).sound(SoundType.METAL);
        private static final BlockBehaviour.Properties PROPERTIES_GOLD = BlockBehaviour.Properties
                .of().mapColor(MapColor.GOLD).instrument(NoteBlockInstrument.BELL).strength(3, 6).sound(SoundType.METAL);
        private static final BlockBehaviour.Properties PROPERTIES_DIAMOND = BlockBehaviour.Properties
                .of().mapColor(MapColor.DIAMOND).strength(5, 6).sound(SoundType.METAL);
        private static final BlockBehaviour.Properties PROPERTIES_OBSIDIAN = BlockBehaviour.Properties
                .of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).strength(50, 1200);
        private static final BlockBehaviour.Properties PROPERTIES_NETHERITE = BlockBehaviour.Properties
                .of().mapColor(MapColor.COLOR_BLACK).strength(50, 1200).sound(SoundType.NETHERITE_BLOCK);

        private ResourceLocation stat(String id) {
            ResourceLocation statId = Utils.id(id);
            return Registry.register(BuiltInRegistries.CUSTOM_STAT, statId, statId);
        }

        public void baseInit() {
            Registry.register(BuiltInRegistries.ITEM, Utils.id("storage_mutator"), new StorageMutator(new Item.Properties().stacksTo(1)));

            final HashSet<ResourceLocation> baseItems = new HashSet<>();
            CommonMain.defineTierUpgradePath(baseItems, TIERS_WOOD, TIERS_COPPER, TIERS_IRON, TIERS_GOLD, TIERS_DIAMOND, TIERS_OBSIDIAN, TIERS_NETHERITE);
        }

        private final int tiers = 6;

        private final int chestTypes = tiers + 3; // tiers + cosmetic variants

        public final List<ChestBlock> chestBlocks = new ArrayList<>(chestTypes);
        public final List<BlockItem> chestItems = new ArrayList<>(chestTypes);
        public final List<EntityType<ChestMinecart>> chestMinecartEntityTypes = new ArrayList<>(chestTypes);
        public final List<ChestMinecartItem> chestMinecartItems = new ArrayList<>(chestTypes);

        private void createChest(ResourceLocation id, ResourceLocation stat, Tier tier, BlockBehaviour.Properties properties) {
            final ChestBlock block;
            if (properties == PROPERTIES_MOSS) {
                block = new MossChestBlock(tier.getBlockSettings().apply(properties), stat, tier.getSlotCount());
            } else {
                block = new ChestBlock(tier.getBlockSettings().apply(properties), stat, tier.getSlotCount());
            }

            final BlockItem item = new BlockItem(block, tier.getItemSettings().apply(new Item.Properties()));
            final ResourceLocation cartId = new ResourceLocation(id.getNamespace(), id.getPath() + "_minecart");
            final ChestMinecartItem cartItem = new ChestMinecartItem(new Item.Properties(), cartId);
            final EntityType<ChestMinecart> cartEntityType = new EntityType<>((type, level) -> {
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

            createChest(Utils.id("wood_chest"), stat("open_wood_chest"), TIERS_WOOD, PROPERTIES_WOOD);
            createChest(Utils.id("pumpkin_chest"), stat("open_pumpkin_chest"), TIERS_WOOD, PROPERTIES_PUMPKIN);
            createChest(Utils.id("present"), stat("open_present"), TIERS_WOOD, presentSettings);
            createChest(Utils.id("bamboo_chest"), stat("open_bamboo_chest"), TIERS_WOOD, PROPERTIES_BAMBOO);
            createChest(Utils.id("moss_chest"), stat("open_moss_chest"), TIERS_WOOD, PROPERTIES_MOSS);
            createChest(Utils.id("iron_chest"), stat("open_iron_chest"), TIERS_IRON, PROPERTIES_IRON);
            createChest(Utils.id("gold_chest"), stat("open_gold_chest"), TIERS_GOLD, PROPERTIES_GOLD);
            createChest(Utils.id("diamond_chest"), stat("open_diamond_chest"), TIERS_DIAMOND, PROPERTIES_DIAMOND);
            createChest(Utils.id("obsidian_chest"), stat("open_obsidian_chest"), TIERS_OBSIDIAN, PROPERTIES_OBSIDIAN);
            createChest(Utils.id("netherite_chest"), stat("open_netherite_chest"), TIERS_NETHERITE, PROPERTIES_NETHERITE);

            final ResourceLocation chestObjectType = Utils.id("chest");
            CommonMain.chestBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, chestObjectType, BlockEntityType.Builder.of((pos, state) -> new ChestBlockEntity(CommonMain.chestBlockEntityType, pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), lockable), chestBlocks.toArray(ChestBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, chestObjectType.toString())));
        }

        public final List<AbstractChestBlock> oldChestBlocks = new ArrayList<>(tiers);

        private void createOldChest(ResourceLocation id, ResourceLocation stat, Tier tier, BlockBehaviour.Properties settings) {
            final AbstractChestBlock block = new AbstractChestBlock(tier.getBlockSettings().apply(settings), stat, tier.getSlotCount());
            final BlockItem item = new BlockItem(block, tier.getItemSettings().apply(new Item.Properties()));

            oldChestBlocks.add(Registry.register(BuiltInRegistries.BLOCK, id, block));
            Registry.register(BuiltInRegistries.ITEM, id, item);
        }

        public void oldChestInit(Supplier<Lockable> lockable) {
            createOldChest(Utils.id("old_wood_chest"), stat("open_old_wood_chest"), TIERS_WOOD, PROPERTIES_WOOD);
            createOldChest(Utils.id("old_iron_chest"), stat("open_old_iron_chest"), TIERS_IRON, PROPERTIES_IRON);
            createOldChest(Utils.id("old_gold_chest"), stat("open_old_gold_chest"), TIERS_GOLD, PROPERTIES_GOLD);
            createOldChest(Utils.id("old_diamond_chest"), stat("open_old_diamond_chest"), TIERS_DIAMOND, PROPERTIES_DIAMOND);
            createOldChest(Utils.id("old_obsidian_chest"), stat("open_old_obsidian_chest"), TIERS_OBSIDIAN, PROPERTIES_OBSIDIAN);
            createOldChest(Utils.id("old_netherite_chest"), stat("open_old_netherite_chest"), TIERS_NETHERITE, PROPERTIES_NETHERITE);

            final ResourceLocation oldChestObjectType = Utils.id("old_chest");
            CommonMain.oldChestBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, oldChestObjectType, BlockEntityType.Builder.of((pos, state) -> new OldChestBlockEntity(CommonMain.oldChestBlockEntityType, pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), lockable), oldChestBlocks.toArray(AbstractChestBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, oldChestObjectType.toString())));
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
            final BarrelBlock block = new CopperBarrelBlock(TIERS_COPPER.getBlockSettings().apply(copperBarrelProperties), copperBarrelStat, TIERS_COPPER.getSlotCount(), weatherState);
            final BlockItem item = new BlockItem(block, TIERS_COPPER.getItemSettings().apply(new Item.Properties()));

            barrelBlocks.add(Registry.register(BuiltInRegistries.BLOCK, id, block));
            Registry.register(BuiltInRegistries.ITEM, id, item);
        }

        public void barrelInit(Supplier<Lockable> lockable) {
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
            createBarrel(Utils.id("waxed_copper_barrel"), copperBarrelStat, TIERS_COPPER, copperBarrelProperties);
            createBarrel(Utils.id("waxed_exposed_copper_barrel"), copperBarrelStat, TIERS_COPPER, copperBarrelProperties);
            createBarrel(Utils.id("waxed_weathered_copper_barrel"), copperBarrelStat, TIERS_COPPER, copperBarrelProperties);
            createBarrel(Utils.id("waxed_oxidized_copper_barrel"), copperBarrelStat, TIERS_COPPER, copperBarrelProperties);
            createBarrel(Utils.id("iron_barrel"), stat("open_iron_barrel"), TIERS_IRON, ironBarrelProperties);
            createBarrel(Utils.id("gold_barrel"), stat("open_gold_barrel"), TIERS_GOLD, goldBarrelProperties);
            createBarrel(Utils.id("diamond_barrel"), stat("open_diamond_barrel"), TIERS_DIAMOND, diamondBarrelProperties);
            createBarrel(Utils.id("obsidian_barrel"), stat("open_obsidian_barrel"), TIERS_OBSIDIAN, obsidianBarrelProperties);
            createBarrel(Utils.id("netherite_barrel"), stat("open_netherite_barrel"), TIERS_NETHERITE, netheriteBarrelProperties);

            final ResourceLocation barrelObjectType = Utils.id("barrel");
            CommonMain.barrelBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, barrelObjectType, BlockEntityType.Builder.of((pos, state) -> new BarrelBlockEntity(CommonMain.barrelBlockEntityType, pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), lockable), barrelBlocks.toArray(BarrelBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, barrelObjectType.toString())));
        }

        public final List<MiniStorageBlock> miniStorageBlocks = new ArrayList<>();

        private void createMiniStorageBlock(ResourceLocation id, ResourceLocation stat, Tier tier, Block.Properties properties, boolean hasRibbon) {
            final MiniStorageBlock block = new MiniStorageBlock(tier.getBlockSettings().apply(properties), stat, hasRibbon);
            final BlockItem item = new BlockItem(block, tier.getItemSettings().apply(new Item.Properties()));

            miniStorageBlocks.add(Registry.register(BuiltInRegistries.BLOCK, id, block));
            Registry.register(BuiltInRegistries.ITEM, id, item);
        }

        private final ResourceLocation copperMiniBarrelStat = stat("open_copper_mini_barrel");
        private final Block.Properties copperBarrelSettings = Block.Properties.of().mapColor(MapColor.WOOD).strength(3, 6).sound(SoundType.WOOD);

        private void createCopperMiniStorageBlock(ResourceLocation id, WeatheringCopper.WeatherState weatherState) {
            MiniStorageBlock block = new CopperMiniStorageBlock(TIERS_COPPER.getBlockSettings().apply(copperBarrelSettings), copperMiniBarrelStat, weatherState);
            BlockItem item = new BlockItem(block, TIERS_COPPER.getItemSettings().apply(new Item.Properties()));

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

            createMiniStorageBlock(Utils.id("vanilla_wood_mini_chest"), woodChestStat, TIERS_WOOD, PROPERTIES_WOOD, false);
            createMiniStorageBlock(Utils.id("wood_mini_chest"), woodChestStat, TIERS_WOOD, PROPERTIES_WOOD, false);
            createMiniStorageBlock(Utils.id("pumpkin_mini_chest"), stat("open_pumpkin_mini_chest"), TIERS_WOOD, PROPERTIES_PUMPKIN, false);
            createMiniStorageBlock(Utils.id("red_mini_present"), stat("open_red_mini_present"), TIERS_WOOD, redPresentSettings, true);
            createMiniStorageBlock(Utils.id("white_mini_present"), stat("open_white_mini_present"), TIERS_WOOD, whitePresentSettings, true);
            createMiniStorageBlock(Utils.id("candy_cane_mini_present"), stat("open_candy_cane_mini_present"), TIERS_WOOD, candyCanePresentSettings, false);
            createMiniStorageBlock(Utils.id("green_mini_present"), stat("open_green_mini_present"), TIERS_WOOD, greenPresentSettings, true);
            createMiniStorageBlock(Utils.id("lavender_mini_present"), stat("open_lavender_mini_present"), TIERS_WOOD, lavenderPresentSettings, true);
            createMiniStorageBlock(Utils.id("pink_amethyst_mini_present"), stat("open_pink_amethyst_mini_present"), TIERS_WOOD, pinkAmethystPresentSettings, true);
            createMiniStorageBlock(Utils.id("iron_mini_chest"), stat("open_iron_mini_chest"), TIERS_IRON, PROPERTIES_IRON, false);
            createMiniStorageBlock(Utils.id("gold_mini_chest"), stat("open_gold_mini_chest"), TIERS_GOLD, PROPERTIES_GOLD, false);
            createMiniStorageBlock(Utils.id("diamond_mini_chest"), stat("open_diamond_mini_chest"), TIERS_DIAMOND, PROPERTIES_DIAMOND, false);
            createMiniStorageBlock(Utils.id("obsidian_mini_chest"), stat("open_obsidian_mini_chest"), TIERS_OBSIDIAN, PROPERTIES_OBSIDIAN, false);
            createMiniStorageBlock(Utils.id("netherite_mini_chest"), stat("open_netherite_mini_chest"), TIERS_NETHERITE, PROPERTIES_NETHERITE, false);

            createMiniStorageBlock(Utils.id("mini_barrel"), stat("open_mini_barrel"), TIERS_WOOD, woodBarrelSettings, false);
            createCopperMiniStorageBlock(Utils.id("copper_mini_barrel"), WeatheringCopper.WeatherState.UNAFFECTED);
            createCopperMiniStorageBlock(Utils.id("exposed_copper_mini_barrel"), WeatheringCopper.WeatherState.EXPOSED);
            createCopperMiniStorageBlock(Utils.id("weathered_copper_mini_barrel"), WeatheringCopper.WeatherState.WEATHERED);
            createCopperMiniStorageBlock(Utils.id("oxidized_copper_mini_barrel"), WeatheringCopper.WeatherState.OXIDIZED);
            createMiniStorageBlock(Utils.id("waxed_copper_mini_barrel"), copperMiniBarrelStat, TIERS_COPPER, copperBarrelSettings, false);
            createMiniStorageBlock(Utils.id("waxed_exposed_copper_mini_barrel"), copperMiniBarrelStat, TIERS_COPPER, copperBarrelSettings, false);
            createMiniStorageBlock(Utils.id("waxed_weathered_copper_mini_barrel"), copperMiniBarrelStat, TIERS_COPPER, copperBarrelSettings, false);
            createMiniStorageBlock(Utils.id("waxed_oxidized_copper_mini_barrel"), copperMiniBarrelStat, TIERS_COPPER, copperBarrelSettings, false);
            createMiniStorageBlock(Utils.id("iron_mini_barrel"), stat("open_iron_mini_barrel"), TIERS_IRON, ironBarrelSettings, false);
            createMiniStorageBlock(Utils.id("gold_mini_barrel"), stat("open_gold_mini_barrel"), TIERS_GOLD, goldBarrelSettings, false);
            createMiniStorageBlock(Utils.id("diamond_mini_barrel"), stat("open_diamond_mini_barrel"), TIERS_DIAMOND, diamondBarrelSettings, false);
            createMiniStorageBlock(Utils.id("obsidian_mini_barrel"), stat("open_obsidian_mini_barrel"), TIERS_OBSIDIAN, obsidianBarrelSettings, false);
            createMiniStorageBlock(Utils.id("netherite_mini_barrel"), stat("open_netherite_mini_barrel"), TIERS_NETHERITE, netheriteBarrelSettings, false);

            final ResourceLocation miniStorageObjectType = Utils.id("mini_chest");
            CommonMain.miniStorageBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, miniStorageObjectType, BlockEntityType.Builder.of((pos, state) -> new MiniStorageBlockEntity(CommonMain.miniStorageBlockEntityType, pos, state, ((OpenableBlock) state.getBlock()).getBlockId(), lockable), miniStorageBlocks.toArray(MiniStorageBlock[]::new)).build(Util.fetchChoiceType(References.BLOCK_ENTITY, miniStorageObjectType.toString())));
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
}
