package ellemes.expandedstorage.common.block;

import ellemes.expandedstorage.common.CommonMain;
import ellemes.expandedstorage.common.block.entity.ChestBlockEntity;
import ellemes.expandedstorage.common.block.entity.OldChestBlockEntity;
import ellemes.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ChestBlock extends AbstractChestBlock implements SimpleWaterloggedBlock {
    public static final int SET_OBSERVER_COUNT_EVENT = 1;
    private static final VoxelShape[] SHAPES = {
            Block.box(1, 0, 0, 15, 14, 15), // Horizontal shapes, depends on orientation and chest type.
            Block.box(1, 0, 1, 16, 14, 15),
            Block.box(1, 0, 1, 15, 14, 16),
            Block.box(0, 0, 1, 15, 14, 15),
            Block.box(1, 0, 1, 15, 14, 15), // Top shape.
            Block.box(1, 0, 1, 15, 16, 15), // Bottom shape.
            Block.box(1, 0, 1, 15, 14, 15)  // Single shape.
    };

    private static final VoxelShape[] UPSIDE_DOWN_SHAPES = {
            Block.box(1, 2, 0, 15, 16, 15), // Horizontal shapes, depends on orientation and chest type.
            Block.box(1, 2, 1, 16, 16, 15),
            Block.box(1, 2, 1, 15, 16, 16),
            Block.box(0, 2, 1, 15, 16, 15),
            Block.box(1, 0, 1, 15, 16, 15), // Top shape.
            Block.box(1, 2, 1, 15, 16, 15), // Bottom shape.
            Block.box(1, 2, 1, 15, 16, 15)  // Single shape.
    };

    public ChestBlock(Properties settings, ResourceLocation openingStat, int slotCount) {
        super(settings, openingStat, slotCount);
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter blockLevel, BlockPos pos, CollisionContext context) {
        boolean upsideDown = false;

        if (blockLevel.getBlockEntity(pos) instanceof OpenableBlockEntity entity) {
            upsideDown = entity.isDinnerbone();
        }
        CursedChestType type = state.getValue(AbstractChestBlock.CURSED_CHEST_TYPE);
        if (upsideDown) {
            if (type == CursedChestType.TOP) {
                return ChestBlock.UPSIDE_DOWN_SHAPES[4];
            } else if (type == CursedChestType.BOTTOM) {
                return ChestBlock.UPSIDE_DOWN_SHAPES[5];
            } else if (type == CursedChestType.SINGLE) {
                return ChestBlock.UPSIDE_DOWN_SHAPES[6];
            } else {
                int index = (state.getValue(BlockStateProperties.HORIZONTAL_FACING).get2DDataValue() + type.getOffset()) % 4;
                return ChestBlock.UPSIDE_DOWN_SHAPES[index];
            }
        } else {
            if (type == CursedChestType.TOP) {
                return ChestBlock.SHAPES[4];
            } else if (type == CursedChestType.BOTTOM) {
                return ChestBlock.SHAPES[5];
            } else if (type == CursedChestType.SINGLE) {
                return ChestBlock.SHAPES[6];
            } else {
                int index = (state.getValue(BlockStateProperties.HORIZONTAL_FACING).get2DDataValue() + type.getOffset()) % 4;
                return ChestBlock.SHAPES[index];
            }
        }
    }

    @Override
    protected void appendAdditionalStateDefinitions(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @NotNull
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context).setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @NotNull
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, LevelAccessor level, BlockPos pos, BlockPos otherPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, otherState, level, pos, otherPos);
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected <T extends OldChestBlockEntity> BlockEntityType<T> getBlockEntityType() {
        //noinspection unchecked
        return (BlockEntityType<T>) CommonMain.getChestBlockEntityType();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() && blockEntityType == this.getBlockEntityType() ? ChestBlockEntity::progressLidAnimation : null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int event, int value) {
        super.triggerEvent(state, level, pos, event, value);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(event, value);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity entity) {
            entity.updateViewerCount(level, pos, state);
        }
    }

    @Override
    public boolean isAccessBlocked(LevelAccessor level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof OpenableBlockEntity entity) {
            if (entity.isDinnerbone()) {
                BlockPos belowPos = pos.below();
                return level.getBlockState(belowPos).isRedstoneConductor(level, belowPos);
            }
        }
        return net.minecraft.world.level.block.ChestBlock.isChestBlockedAt(level, pos);
    }

    @Override
    protected boolean areChestsCompatible(Level level, ItemStack itemInHand, BlockPos firstPos, BlockPos secondPos) {
        boolean firstIsDinnerbone = itemInHand.getHoverName().getString().equals("Dinnerbone");
        boolean secondIsDinnerbone = level.getBlockEntity(secondPos) instanceof OpenableBlockEntity second && second.isDinnerbone();

        return firstIsDinnerbone == secondIsDinnerbone;
    }
}
