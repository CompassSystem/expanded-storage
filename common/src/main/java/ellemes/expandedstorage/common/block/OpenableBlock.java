package ellemes.expandedstorage.common.block;

import ellemes.container_library.api.v3.OpenableInventoryProvider;
import ellemes.container_library.api.v3.client.ScreenOpeningApi;
import ellemes.container_library.api.v3.context.BlockContext;
import ellemes.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class OpenableBlock extends Block implements OpenableInventoryProvider<BlockContext>, EntityBlock, WeatheringCopper {
    private final ResourceLocation blockId;
    private final ResourceLocation blockTier;
    private final ResourceLocation openingStat;
    private final int slotCount;

    public OpenableBlock(Properties settings, ResourceLocation blockId, ResourceLocation blockTier, ResourceLocation openingStat, int slotCount) {
        super(settings);
        this.blockId = blockId;
        this.blockTier = blockTier;
        this.openingStat = openingStat;
        this.slotCount = slotCount;
    }

    public Component getInventoryTitle() {
        return this.getName();
    }

    public abstract ResourceLocation getBlockType();

    public final ResourceLocation getBlockId() {
        return blockId;
    }

    public final int getSlotCount() {
        return slotCount;
    }

    public final ResourceLocation getBlockTier() {
        return blockTier;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean bl) {
        if (state.getBlock().getClass() != newState.getBlock().getClass()) {
            if (world.getBlockEntity(pos) instanceof OpenableBlockEntity entity) {
                Containers.dropContents(world, pos, entity.getItems());
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, bl);
        } else {
            if (state.getBlock() != newState.getBlock() && world.getBlockEntity(pos) instanceof OpenableBlockEntity entity) {
                CompoundTag tag = entity.saveWithoutMetadata();
                world.removeBlockEntity(pos);
                if (world.getBlockEntity(pos) instanceof OpenableBlockEntity newEntity) {
                    newEntity.load(tag);
                }
            }
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName() && world.getBlockEntity(pos) instanceof OpenableBlockEntity entity) {
            entity.setCustomName(stack.getHoverName());
        }
    }

    @Override
    public void onInitialOpen(ServerPlayer player) {
        player.awardStat(openingStat);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        boolean isClient = world.isClientSide();
        if (isClient) {
            ScreenOpeningApi.openBlockInventory(pos);
        }
        return InteractionResult.sidedSuccess(isClient);
    }

    /**
     * todo: when copper chest side oxidises make sure to update the other block before this side is updated but said block to become a single block.
     *  Code should be in {@link AbstractChestBlock#updateShape}
    **/
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block otherBlock, BlockPos otherPos, boolean bl) {
        super.neighborChanged(state, level, pos, otherBlock, otherPos, bl);
        System.out.println(state.getBlock());
        System.out.println(pos);
        System.out.println(level.getBlockState(otherPos));
        System.out.println(otherPos);
    }

    @Override
    public void onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource source) {
        if (blockTier == Utils.COPPER_TIER_ID) {
            WeatheringCopper.super.onRandomTick(state, level, pos, source);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource source) {
        this.onRandomTick(state, level, pos, source);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return blockTier == Utils.COPPER_TIER_ID && !blockId.getPath().contains("oxidized") && !blockId.getPath().contains("waxed");
    }

    // Note: only valid for copper blocks.
    @Override
    public Optional<BlockState> getNext(BlockState state) {
        String newPath = blockId.getPath();
        if (newPath.contains("waxed_")) {
            return Optional.empty();
        }
        if (newPath.contains("weathered_")) {
            newPath = newPath.replace("weathered_", "oxidized_");
        } else if (newPath.contains("exposed_")) {
            newPath = newPath.replace("exposed_", "weathered_");
        } else if (!newPath.contains("oxidized_")) {
            newPath = newPath.replace("copper", "exposed_copper");
        } else {
            return Optional.empty();
        }
        ResourceLocation newBlockId = new ResourceLocation(blockId.getNamespace(), newPath);
        return Optional.of(Registry.BLOCK.get(newBlockId).withPropertiesOf(state));
    }

    // Note: only valid for copper blocks.
    @Override
    public WeatherState getAge() {
        String path = blockId.getPath();
        if (path.contains("oxidized_")) {
            return WeatherState.OXIDIZED;
        } else if (path.contains("weathered_")) {
            return WeatherState.WEATHERED;
        } else if (path.contains("exposed_")) {
            return WeatherState.EXPOSED;
        } else {
            return WeatherState.UNAFFECTED;
        }
    }
}
