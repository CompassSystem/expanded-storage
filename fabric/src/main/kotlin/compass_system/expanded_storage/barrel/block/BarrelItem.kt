package compass_system.expanded_storage.barrel.block

import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block

class BarrelItem(block: Block, properties: Properties) : BlockItem(block, properties) {

    override fun getDefaultInstance(): ItemStack {
        val default = super.getDefaultInstance()
        val tag = default.getOrCreateTag()

        tag.putString("base", "minecraft:barrel")

        return default
    }
}