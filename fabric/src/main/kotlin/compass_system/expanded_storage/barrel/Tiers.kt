package compass_system.expanded_storage.barrel

import compass_system.expanded_storage.ExpandedStorage.resloc
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.state.BlockBehaviour.Properties as BlockProperties

enum class Tiers(
    val slots: Int,
    val id: ResourceLocation,
    private val requiresTool: Boolean = true,
    private val isLavaResistant: Boolean = false
) {
    WOOD(27, resloc("wood"), requiresTool = false),
    COPPER(45, resloc("copper")),
    IRON(54, resloc("iron")),
    GOLD(81, resloc("gold")),
    DIAMOND(108, resloc("diamond")),
    NETHERITE(135, resloc("netherite"), isLavaResistant = true);

    fun applyBlockProperties(properties: BlockProperties) : BlockProperties{
        if (requiresTool) {
            properties.requiresCorrectToolForDrops()
        }

        return properties
    }

    fun applyItemProperties(properties: Item.Properties) : Item.Properties{
        if (isLavaResistant) {
            properties.fireResistant()
        }

        return properties
    }

    fun getStatId(blockType: String) = resloc("open_${id}_${blockType}")
}