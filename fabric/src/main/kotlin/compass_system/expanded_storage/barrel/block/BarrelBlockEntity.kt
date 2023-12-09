package compass_system.expanded_storage.barrel.block

import compass_system.expanded_storage._core.block.StorageBlockEntity
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class BarrelBlockEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState
) : StorageBlockEntity(type, pos, state), RenderDataBlockEntity {
    private var baseBlock: Block = Blocks.BARREL

    override fun getRenderData(): Any {
        return baseBlock
    }

    fun setBaseBlock(block: Block) {
        baseBlock = block
    }

    override fun saveAdditional(compoundTag: CompoundTag) {
        super.saveAdditional(compoundTag)

        compoundTag.putString("base", baseBlock.builtInRegistryHolder().key().location().toString())
    }

    override fun load(compoundTag: CompoundTag) {
        super.load(compoundTag)

        if (compoundTag.contains("base")) {
            baseBlock = BuiltInRegistries.BLOCK[ResourceLocation(compoundTag.getString("base"))]
        }
    }
}