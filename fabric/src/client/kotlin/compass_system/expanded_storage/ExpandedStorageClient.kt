package compass_system.expanded_storage

import compass_system.expanded_storage.client.model.BarrelModelPlugin
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.BarrelBlock

object ExpandedStorageClient : ClientModInitializer {
    val modelPlugin = run {
        val barrels = BuiltInRegistries.BLOCK.entrySet()
            .filter { it.value is BarrelBlock }
            .map {
                BarrelEntry(
                    it.key.location(),
                    it.value as BarrelBlock
                )
            }
            .toList()

        BarrelModelPlugin(barrels)
    }

    override fun onInitializeClient() {
        ExpandedStorage.logger.info("Expanded Storage Client Initializing")

        ModelLoadingPlugin.register(modelPlugin)
    }
}