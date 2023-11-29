package compass_system.expanded_storage

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object ExpandedStorage : ModInitializer {
    private val logger = LoggerFactory.getLogger("expanded-storage")

	override fun onInitialize() {
		logger.info("Hello world!")
	}
}