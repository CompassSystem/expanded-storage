package ellemes.expandedstorage.common;

import ellemes.expandedstorage.api.EsChestType;
import ellemes.expandedstorage.common.block.AbstractChestBlock;
import ellemes.expandedstorage.common.block.BarrelBlock;
import ellemes.expandedstorage.common.block.ChestBlock;
import ellemes.expandedstorage.common.block.MiniStorageBlock;
import ellemes.expandedstorage.common.block.OpenableBlock;
import ellemes.expandedstorage.common.entity.ChestMinecart;
import ellemes.expandedstorage.common.entity.TieredEntityType;
import ellemes.expandedstorage.common.item.BlockUpgradeBehaviour;
import ellemes.expandedstorage.common.item.EntityMutatorBehaviour;
import ellemes.expandedstorage.common.item.EntityUpgradeBehaviour;
import ellemes.expandedstorage.common.item.MutationMode;
import ellemes.expandedstorage.common.item.ToolUsageResult;
import ellemes.expandedstorage.common.misc.TagReloadListener;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class OldConversionCode {
    private static final Map<Predicate<Block>, BlockUpgradeBehaviour> BLOCK_UPGRADE_BEHAVIOURS = new HashMap<>();
    private static final Map<Predicate<Entity>, EntityUpgradeBehaviour> ENTITY_UPGRADE_BEHAVIOURS = new HashMap<>();
    private static final Map<Map.Entry<Predicate<Entity>, MutationMode>, EntityMutatorBehaviour> ENTITY_MUTATOR_BEHAVIOURS = new HashMap<>();

    static void registerMutationBehaviour(Predicate<Entity> predicate, MutationMode mode, EntityMutatorBehaviour behaviour) {
        OldConversionCode.ENTITY_MUTATOR_BEHAVIOURS.put(Map.entry(predicate, mode), behaviour);
    }

    public static EntityMutatorBehaviour getEntityMutatorBehaviour(Entity entity, MutationMode mode) {
        for (Map.Entry<Map.Entry<Predicate<Entity>, MutationMode>, EntityMutatorBehaviour> entry : OldConversionCode.ENTITY_MUTATOR_BEHAVIOURS.entrySet()) {
            Map.Entry<Predicate<Entity>, MutationMode> pair = entry.getKey();
            if (pair.getValue() == mode && pair.getKey().test(entity)) return entry.getValue();
        }
        return null;
    }

    private static boolean upgradeSingleBlockToChest(Level level, BlockState state, BlockPos pos, ResourceLocation from, ResourceLocation to) {
        Block block = state.getBlock();
        boolean isExpandedStorageChest = block instanceof ChestBlock;
        int inventorySize = !isExpandedStorageChest ? Utils.WOOD_STACK_COUNT : ((OpenableBlock) block).getSlotCount();
        if (isExpandedStorageChest && ((OpenableBlock) block).getObjTier() == from || !isExpandedStorageChest && from == Utils.WOOD_TIER_ID) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
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
                if (toBlock == null) {
                    return false;
                }
                NonNullList<ItemStack> inventory = NonNullList.withSize(toBlock.getSlotCount(), ItemStack.EMPTY);
                LockCode code = LockCode.fromTag(tag);
                ContainerHelper.loadAllItems(tag, inventory);
                level.removeBlockEntity(pos);
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
                if (level.setBlockAndUpdate(pos, newState)) {
                    BlockEntity newEntity = level.getBlockEntity(pos);
                    //noinspection ConstantConditions
                    CompoundTag newTag = newEntity.saveWithoutMetadata();
                    ContainerHelper.saveAllItems(newTag, inventory);
                    code.addToTag(newTag);
                    newEntity.load(newTag);
                    return true;
                } else {
                    level.setBlockEntity(blockEntity);
                }
            }
        }
        return false;
    }

    private static boolean upgradeSingleBlockToOldChest(Level level, BlockState state, BlockPos pos, ResourceLocation from, ResourceLocation to) {
        if (((OpenableBlock) state.getBlock()).getObjTier() == from) {
            AbstractChestBlock toBlock = (AbstractChestBlock) CommonMain.getTieredObject(CommonMain.OLD_CHEST_OBJECT_TYPE, to);
            if (toBlock == null) {
                return false;
            }
            NonNullList<ItemStack> inventory = NonNullList.withSize(toBlock.getSlotCount(), ItemStack.EMPTY);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            //noinspection ConstantConditions
            CompoundTag tag = blockEntity.saveWithoutMetadata();
            LockCode code = LockCode.fromTag(tag);
            ContainerHelper.loadAllItems(tag, inventory);
            level.removeBlockEntity(pos);
            BlockState newState = toBlock.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING)).setValue(AbstractChestBlock.CURSED_CHEST_TYPE, state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE));
            if (level.setBlockAndUpdate(pos, newState)) {
                BlockEntity newEntity = level.getBlockEntity(pos);
                //noinspection ConstantConditions
                CompoundTag newTag = newEntity.saveWithoutMetadata();
                ContainerHelper.saveAllItems(newTag, inventory);
                code.addToTag(newTag);
                newEntity.load(newTag);
                return true;
            } else {
                level.setBlockEntity(blockEntity);
            }
        }
        return false;
    }

    public static BlockUpgradeBehaviour getBlockUpgradeBehaviour(Block block) {
        for (Map.Entry<Predicate<Block>, BlockUpgradeBehaviour> entry : OldConversionCode.BLOCK_UPGRADE_BEHAVIOURS.entrySet()) {
            if (entry.getKey().test(block)) return entry.getValue();
        }
        return null;
    }

    public static EntityUpgradeBehaviour getEntityUpgradeBehaviour(Entity entity) {
        for (Map.Entry<Predicate<Entity>, EntityUpgradeBehaviour> entry : OldConversionCode.ENTITY_UPGRADE_BEHAVIOURS.entrySet()) {
            if (entry.getKey().test(entity)) return entry.getValue();
        }
        return null;
    }

    private static void defineBlockUpgradeBehaviour(Predicate<Block> target, BlockUpgradeBehaviour behaviour) {
        OldConversionCode.BLOCK_UPGRADE_BEHAVIOURS.put(target, behaviour);
    }

    private static void defineEntityUpgradeBehaviour(Predicate<Entity> target, EntityUpgradeBehaviour behaviour) {
        OldConversionCode.ENTITY_UPGRADE_BEHAVIOURS.put(target, behaviour);
    }

    public static void init(TagKey<Block> chestTag, TagKey<Block> barrelTag, TagReloadListener tagReloadListener) {
        Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof ChestBlock || block instanceof net.minecraft.world.level.block.ChestBlock || block.defaultBlockState().is(chestTag);
        OldConversionCode.defineBlockUpgradeBehaviour(isUpgradableChestBlock, (context, from, to) -> {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            Player player = context.getPlayer();
            ItemStack handStack = context.getItemInHand();
            if (state.getBlock() instanceof ChestBlock) {
                EsChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
                Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (AbstractChestBlock.getBlockType(type) == DoubleBlockCombiner.BlockType.SINGLE) {
                    boolean upgradeSucceeded = OldConversionCode.upgradeSingleBlockToChest(level, state, pos, from, to);
                    if (upgradeSucceeded) handStack.shrink(1);
                    return upgradeSucceeded;
                } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                    BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(type, facing));
                    BlockState otherState = level.getBlockState(otherPos);
                    boolean firstSucceeded = OldConversionCode.upgradeSingleBlockToChest(level, state, pos, from, to);
                    boolean secondSucceeded = OldConversionCode.upgradeSingleBlockToChest(level, otherState, otherPos, from, to);
                    if (firstSucceeded && secondSucceeded) handStack.shrink(2);
                    else if (firstSucceeded || secondSucceeded) handStack.shrink(1);
                    return firstSucceeded || secondSucceeded;
                }
            } else {
                if (net.minecraft.world.level.block.ChestBlock.getBlockType(state) == DoubleBlockCombiner.BlockType.SINGLE) {
                    boolean upgradeSucceeded = OldConversionCode.upgradeSingleBlockToChest(level, state, pos, from, to);
                    if (upgradeSucceeded) handStack.shrink(1);
                    return upgradeSucceeded;
                } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                    BlockPos otherPos = pos.relative(net.minecraft.world.level.block.ChestBlock.getConnectedDirection(state));
                    BlockState otherState = level.getBlockState(otherPos);
                    boolean firstSucceeded = OldConversionCode.upgradeSingleBlockToChest(level, state, pos, from, to);
                    boolean secondSucceeded = OldConversionCode.upgradeSingleBlockToChest(level, otherState, otherPos, from, to);
                    if (firstSucceeded && secondSucceeded) handStack.shrink(2);
                    else if (firstSucceeded || secondSucceeded) handStack.shrink(1);
                    return firstSucceeded || secondSucceeded;
                }
            }

            return false;
        });

        OldConversionCode.defineEntityUpgradeBehaviour(e -> e instanceof ChestMinecart || e instanceof MinecartChest, (player, hand, entity, from, to) -> {
            if (entity instanceof MinecartChest minecartChest && from.equals(Utils.WOOD_TIER_ID) && minecartChest.getContainerSize() == 27 ||
                    entity instanceof ChestMinecart chestMinecart && ((TieredEntityType<ChestMinecart>) chestMinecart.getType()).getObjTier().equals(from)) {
                TieredEntityType<ChestMinecart> toObject = (TieredEntityType<ChestMinecart>) CommonMain.getTieredObject(CommonMain.MINECART_CHEST_OBJECT_TYPE, to);
                if (toObject != null) {
                    if (entity.getLevel().isClientSide()) {
                        return OldConversionCode.simulateSpawnUpgradedMinecartChest(entity);
                    }
                    boolean upgradeSucceeded = OldConversionCode.spawnUpgradedMinecartChest((ServerLevel) entity.getLevel(), toObject, entity);
                    if (upgradeSucceeded && !player.isCreative()) {
                        player.getItemInHand(hand).shrink(1);
                    }
                    return upgradeSucceeded;
                }
            }
            return false;
        });

        CommonMain.registerMutationBehaviour(b -> b instanceof ChestBlock, MutationMode.SWAP_THEME, (context, level, state, pos, stack) -> {
            List<Block> blocks = tagReloadListener.getChestCycleBlocks();
            int index = blocks.indexOf(state.getBlock());
            if (index != -1) { // Cannot change style e.g. iron chest, ect.
                Block next = blocks.get((index + 1) % blocks.size());
                EsChestType chestType = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
                if (chestType != EsChestType.SINGLE) {
                    BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                    BlockState otherState = level.getBlockState(otherPos);
                    level.setBlock(otherPos, next.withPropertiesOf(otherState), Block.UPDATE_SUPPRESS_LIGHT);
                }
                level.setBlockAndUpdate(pos, next.withPropertiesOf(state));
                return ToolUsageResult.slowSuccess();
            }
            return ToolUsageResult.fail();
        });

        OldConversionCode.registerMutationBehaviour(e -> e instanceof ChestMinecart, MutationMode.SWAP_THEME, (level, entity, stack) -> {
            List<? extends EntityType<?>> entityTypes = tagReloadListener.getMinecartChestCycleEntityTypes();
            int index = entityTypes.indexOf(entity.getType());
            if (index != -1) { // Cannot change style e.g. iron chest, ect.
                if (!level.isClientSide()) {
                    EntityType<ChestMinecart> next = (EntityType<ChestMinecart>) entityTypes.get((index + 1) % entityTypes.size());
                    OldConversionCode.spawnUpgradedMinecartChest((ServerLevel) level, next, entity);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        });

        Predicate<Block> isUpgradableOldChestBlock = (block) -> block.getClass() == AbstractChestBlock.class;
        OldConversionCode.defineBlockUpgradeBehaviour(isUpgradableOldChestBlock, (context, from, to) -> {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            Player player = context.getPlayer();
            ItemStack handStack = context.getItemInHand();
            if (AbstractChestBlock.getBlockType(state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE)) == DoubleBlockCombiner.BlockType.SINGLE) {
                boolean upgradeSucceeded = OldConversionCode.upgradeSingleBlockToOldChest(level, state, pos, from, to);
                if (upgradeSucceeded) handStack.shrink(1);
                return upgradeSucceeded;
            } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                BlockPos otherPos = pos.relative(AbstractChestBlock.getDirectionToAttached(state));
                BlockState otherState = level.getBlockState(otherPos);
                boolean firstSucceeded = OldConversionCode.upgradeSingleBlockToOldChest(level, state, pos, from, to);
                boolean secondSucceeded = OldConversionCode.upgradeSingleBlockToOldChest(level, otherState, otherPos, from, to);
                if (firstSucceeded && secondSucceeded) handStack.shrink(2);
                else if (firstSucceeded || secondSucceeded) handStack.shrink(1);
                return firstSucceeded || secondSucceeded;
            }
            return false;
        });

        Predicate<Block> isUpgradableBarrelBlock = (block) -> block instanceof BarrelBlock || block instanceof net.minecraft.world.level.block.BarrelBlock || block.defaultBlockState().is(barrelTag);
        OldConversionCode.defineBlockUpgradeBehaviour(isUpgradableBarrelBlock, (context, from, to) -> {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            boolean isExpandedStorageBarrel = block instanceof BarrelBlock;
            int inventorySize = !isExpandedStorageBarrel ? Utils.WOOD_STACK_COUNT : ((OpenableBlock) block).getSlotCount();
            if (isExpandedStorageBarrel && ((OpenableBlock) block).getObjTier() == from || !isExpandedStorageBarrel && from == Utils.WOOD_TIER_ID) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
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
                    level.removeBlockEntity(pos);
                    BlockState newState = toBlock.defaultBlockState().setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                    if (level.setBlockAndUpdate(pos, newState)) {
                        BlockEntity newEntity = level.getBlockEntity(pos);
                        //noinspection ConstantConditions
                        CompoundTag newTag = newEntity.saveWithoutMetadata();
                        ContainerHelper.saveAllItems(newTag, inventory);
                        code.addToTag(newTag);
                        newEntity.load(newTag);
                        context.getItemInHand().shrink(1);
                        return true;
                    } else {
                        level.setBlockEntity(blockEntity);
                    }
                }
            }
            return false;
        });

        Predicate<Block> isMiniStorage = b -> b instanceof MiniStorageBlock;
        CommonMain.registerMutationBehaviour(isMiniStorage, MutationMode.SWAP_THEME, (context, level, state, pos, stack) -> {
            String itemName = stack.getHoverName().getString();
            List<Block> blocks;
            boolean isSparrow = itemName.equals("Sparrow");
            if (itemName.equals("Sunrise")) {
                blocks = tagReloadListener.getMiniChestSecretCycleBlocks();
            } else if (isSparrow) {
                blocks = tagReloadListener.getMiniChestSecretCycle2Blocks();
            } else {
                blocks = tagReloadListener.getMiniChestCycleBlocks();
            }
            int index = blocks.indexOf(state.getBlock());
            if (index != -1) { // Illegal state / misconfigured tag
                Block next = blocks.get((index + 1) % blocks.size());
                level.setBlockAndUpdate(pos, next.withPropertiesOf(state));
                return ToolUsageResult.slowSuccess();
            } else if (isSparrow) {
                ResourceLocation blockId = ((MiniStorageBlock) state.getBlock()).getBlockId();
                String newId = blockId.getPath();
                if (newId.contains("_with_sparrow")) {
                    newId = newId.substring(0, newId.length() - 13);
                } else {
                    newId = newId + "_with_sparrow";
                }
                Block next = Registry.BLOCK.get(new ResourceLocation(blockId.getNamespace(), newId));
                level.setBlockAndUpdate(pos, next.withPropertiesOf(state));
                return ToolUsageResult.slowSuccess();
            }
            return ToolUsageResult.fail();
        });
    }

    private static boolean simulateSpawnUpgradedMinecartChest(Entity original) {
        boolean isMinecraftCart = original instanceof MinecartChest;
        boolean isOurCart = original instanceof ChestMinecart;
        return isOurCart || isMinecraftCart;
    }

    private static boolean spawnUpgradedMinecartChest(ServerLevel level, EntityType<ChestMinecart> newType, Entity original) {
        if (!simulateSpawnUpgradedMinecartChest(original)) {
            return false;
        }
        ChestMinecart newCart = newType.create(level, null, original.hasCustomName() ? original.getCustomName() : null, null, original.getOnPos(), MobSpawnType.COMMAND, true, false);
        if (newCart != null) {
            boolean isMinecraftCart = original instanceof MinecartChest;
            newCart.loadInventoryFromTag(ContainerHelper.saveAllItems(new CompoundTag(), isMinecraftCart ? ((MinecartChest) original).getItemStacks() : ((ChestMinecart) original).getItems()));
            newCart.setPos(original.position());
            newCart.setXRot(original.getXRot());
            newCart.setYRot(original.getYRot());
            newCart.setDeltaMovement(original.getDeltaMovement());
            if (original.hasCustomName()) {
                newCart.setCustomName(original.getCustomName());
            }
            level.addFreshEntityWithPassengers(newCart);
            ((Clearable) original).clearContent();
            original.remove(Entity.RemovalReason.DISCARDED);
            return true;
        }
        return false;
    }
}
