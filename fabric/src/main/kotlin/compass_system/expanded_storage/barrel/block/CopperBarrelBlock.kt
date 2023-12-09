package compass_system.expanded_storage.barrel.block

import compass_system.expanded_storage.barrel.Tiers
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.WeatheringCopper

class CopperBarrelBlock(
    properties: Properties,
    private val weatherState: WeatheringCopper.WeatherState,
    textureId: ResourceLocation
) : BarrelBlock(properties, Tiers.COPPER, textureId), WeatheringCopper {
    override fun getAge(): WeatheringCopper.WeatherState = weatherState
}