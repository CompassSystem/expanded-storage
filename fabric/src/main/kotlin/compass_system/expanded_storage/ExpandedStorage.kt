package compass_system.expanded_storage

import compass_system.expanded_storage.barrel.BarrelInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ExpandedStorage : ModInitializer {
	const val MOD_ID = "expanded-storage"
	val logger: Logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		BarrelInitializer.onInitialize()

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, resloc("expanded-storage"), FabricItemGroup.builder()
			.title(Component.translatable("item_group.expanded-storage"))
			.displayItems { displayParams, output ->

				val items = BarrelInitializer.barrelBlocks.groupBy { it.builtInRegistryHolder().key().location().path.split(".").takeLast(2)[0] }.toSortedMap()

				items.remove("minecraft")!!.forEach { output.accept(it) }

				items.forEach { (namespace, blocks) ->
					blocks.forEach { output.accept(it) }
				}
			}
			.icon { Items.BARREL.defaultInstance }
			.build())
	}

	fun resloc(path: String) = ResourceLocation(MOD_ID, path)
}