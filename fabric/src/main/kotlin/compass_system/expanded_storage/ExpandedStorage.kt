package compass_system.expanded_storage

import compass_system.expanded_storage.barrel.BarrelInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ExpandedStorage : ModInitializer {
	const val MOD_ID = "expanded-storage"
	val logger: Logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		BarrelInitializer.onInitialize()
	}

	fun resloc(path: String) = ResourceLocation(MOD_ID, path)
}