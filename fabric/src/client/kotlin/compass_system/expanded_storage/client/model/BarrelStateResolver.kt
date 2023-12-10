package compass_system.expanded_storage.client.model

import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver
import net.minecraft.client.renderer.block.model.MultiVariant
import net.minecraft.client.renderer.block.model.Variant
import net.minecraft.client.resources.model.BlockModelRotation
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

object BarrelStateResolver : BlockStateResolver {
    override fun resolveBlockStates(context: BlockStateResolver.Context) {
        val block = context.block()

        val closedModelId = block.builtInRegistryHolder().key().location().withPath { it.replaceFirst("waxed_", "") }

        val closedState = block.defaultBlockState().setValue(BlockStateProperties.OPEN, false)

        context.setModel(closedState.facing(Direction.UP), MultiVariant(listOf(Variant(closedModelId, BlockModelRotation.X0_Y0.rotation, false, 1))))
        context.setModel(closedState.facing(Direction.DOWN), MultiVariant(listOf(Variant(closedModelId, BlockModelRotation.X180_Y0.rotation, false, 1))))
        context.setModel(closedState.facing(Direction.NORTH), MultiVariant(listOf(Variant(closedModelId, BlockModelRotation.X90_Y0.rotation, false, 1))))
        context.setModel(closedState.facing(Direction.SOUTH), MultiVariant(listOf(Variant(closedModelId, BlockModelRotation.X90_Y180.rotation, false, 1))))
        context.setModel(closedState.facing(Direction.EAST), MultiVariant(listOf(Variant(closedModelId, BlockModelRotation.X90_Y90.rotation, false, 1))))
        context.setModel(closedState.facing(Direction.WEST), MultiVariant(listOf(Variant(closedModelId, BlockModelRotation.X90_Y270.rotation, false, 1))))

        val openState = closedState.setValue(BlockStateProperties.OPEN, true)
        val openModelId = closedModelId.withSuffix("_open")

        context.setModel(openState.facing(Direction.UP), MultiVariant(listOf(Variant(openModelId, BlockModelRotation.X0_Y0.rotation, false, 1))))
        context.setModel(openState.facing(Direction.DOWN), MultiVariant(listOf(Variant(openModelId, BlockModelRotation.X180_Y0.rotation, false, 1))))
        context.setModel(openState.facing(Direction.NORTH), MultiVariant(listOf(Variant(openModelId, BlockModelRotation.X90_Y0.rotation, false, 1))))
        context.setModel(openState.facing(Direction.SOUTH), MultiVariant(listOf(Variant(openModelId, BlockModelRotation.X90_Y180.rotation, false, 1))))
        context.setModel(openState.facing(Direction.EAST), MultiVariant(listOf(Variant(openModelId, BlockModelRotation.X90_Y90.rotation, false, 1))))
        context.setModel(openState.facing(Direction.WEST), MultiVariant(listOf(Variant(openModelId, BlockModelRotation.X90_Y270.rotation, false, 1))))
    }

    inline fun BlockState.facing(direction: Direction): BlockState = setValue(BlockStateProperties.FACING, direction)
}