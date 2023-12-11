package compass_system.expanded_storage.barrel.block

import compass_system.expanded_storage._core.block.StorageBlock
import compass_system.expanded_storage.barrel.BarrelInitializer
import compass_system.expanded_storage.barrel.Tiers
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties

open class BarrelBlock(properties: Properties, val tier: Tiers) : StorageBlock(properties), EntityBlock {
    init {
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.NORTH).setValue(BlockStateProperties.OPEN, false))
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = BarrelInitializer.BLOCK_ENTITY_TYPE.create(pos, state)

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(BlockStateProperties.FACING)
        builder.add(BlockStateProperties.OPEN)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(BarrelBlock.FACING, context.nearestLookingDirection.opposite)
    }

    override fun getDescriptionId(): String {
        if (descriptionId == null) {
            val baseIdLength = BarrelInitializer.barrelBlocks[this]!!.toString().length
            val idPath = BuiltInRegistries.BLOCK.getKey(this).path
            val first = idPath.take(idPath.length - baseIdLength - 1)
            descriptionId = "block.expanded-storage.${first}_barrel"
        }

        return descriptionId!!
    }
}