package compass_system.expanded_storage

import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

data class BarrelEntry(
    val id: ResourceLocation,
    val block: BarrelBlock
) {
    fun defaultState(): BlockState = block.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP)
}
