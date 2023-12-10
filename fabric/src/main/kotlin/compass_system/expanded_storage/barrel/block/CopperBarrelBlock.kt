package compass_system.expanded_storage.barrel.block

import compass_system.expanded_storage.barrel.Tiers
import net.minecraft.world.level.block.WeatheringCopper

class CopperBarrelBlock(
    properties: Properties,
    private val weatherState: WeatheringCopper.WeatherState,
) : BarrelBlock(properties, Tiers.COPPER), WeatheringCopper {
    override fun getAge(): WeatheringCopper.WeatherState = weatherState
}