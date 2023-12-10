package compass_system.expanded_storage.barrel

import compass_system.expanded_storage.ExpandedStorage.resloc
import compass_system.expanded_storage.barrel.block.BarrelBlock
import compass_system.expanded_storage.barrel.block.BarrelBlockEntityType
import compass_system.expanded_storage.barrel.block.CopperBarrelBlock
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.WeatheringCopper.WeatherState
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.state.BlockBehaviour.Properties as BlockProperties
import net.minecraft.world.level.block.BarrelBlock as VanillaBarrelBlock

object BarrelInitializer : ModInitializer {
    internal val BLOCK_ENTITY_TYPE = BarrelBlockEntityType(null)
    val barrelBlocks = mutableListOf<BarrelBlock>()

    override fun onInitialize() {
        val blocksToRegister = mutableMapOf<ResourceLocation, VanillaBarrelBlock>()

        BuiltInRegistries.BLOCK.entrySet().forEach { (key, block) ->
            if (block is VanillaBarrelBlock) {
                blocksToRegister[key.location()] = block
            }
        }

        blocksToRegister.forEach { (id, block) ->
            regiserExpandedStorageBarrels(id, block)
        }

        RegistryEntryAddedCallback.event(BuiltInRegistries.BLOCK).register { _, id, block ->
            if (block is VanillaBarrelBlock) {
                regiserExpandedStorageBarrels(id, block)
            }
        }

        Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation("expanded-storage", "barrel"),
            BLOCK_ENTITY_TYPE
        )
    }

    private fun regiserExpandedStorageBarrels(id: ResourceLocation, block: VanillaBarrelBlock) {
        val weakMetalProperties = baseProperties().strength(3F, 6F)

        barrelBlocks.addAll(
            listOf(
                createCopperBarrel("copper", weakMetalProperties, WeatherState.UNAFFECTED, id, block),
                createCopperBarrel("exposed_copper", weakMetalProperties, WeatherState.EXPOSED, id, block),
                createCopperBarrel("weathered_copper", weakMetalProperties, WeatherState.WEATHERED, id, block),
                createCopperBarrel("oxidized_copper", weakMetalProperties, WeatherState.OXIDIZED, id, block),
                createBarrel("waxed_copper", Tiers.COPPER, weakMetalProperties, id, block),
                createBarrel("waxed_exposed_copper", Tiers.COPPER, weakMetalProperties, id, block),
                createBarrel("waxed_weathered_copper", Tiers.COPPER, weakMetalProperties, id, block),
                createBarrel("waxed_oxidized_copper", Tiers.COPPER, weakMetalProperties, id, block),
                createBarrel("iron", Tiers.IRON, baseProperties().strength(5F, 6F), id, block),
                createBarrel("gold", Tiers.GOLD, weakMetalProperties, id, block),
                createBarrel("diamond", Tiers.DIAMOND, baseProperties().strength(5F, 6F), id, block),
                createBarrel("netherite", Tiers.NETHERITE, baseProperties().strength(50F, 1200F), id, block)
            )
        )

    }

    private fun baseProperties() = BlockProperties.of()
        .mapColor(MapColor.WOOD)
        .instrument(NoteBlockInstrument.BASS)
        .sound(SoundType.WOOD)
        .ignitedByLava()

    private fun createCopperBarrel(
        barrelTierId: String,
        properties: BlockBehaviour.Properties,
        weatherState: WeatherState,
        id: ResourceLocation,
        baseBlock: VanillaBarrelBlock
    ): CopperBarrelBlock {
        val block = CopperBarrelBlock(Tiers.COPPER.applyBlockProperties(properties), weatherState,)
        val item = BlockItem(block, Tiers.COPPER.applyItemProperties(Item.Properties()))

        val barrelId = resloc("${barrelTierId}.${id.namespace}.${id.path}")

        Registry.register(BuiltInRegistries.BLOCK, barrelId, block)
        Registry.register(BuiltInRegistries.ITEM, barrelId, item)

        return block
    }

    private fun createBarrel(
        barrelTierId: String,
        tier: Tiers,
        properties: BlockBehaviour.Properties,
        baseId: ResourceLocation,
        baseBlock: VanillaBarrelBlock
    ): BarrelBlock {
        val block = BarrelBlock(tier.applyBlockProperties(properties), tier)
        val item = BlockItem(block, tier.applyItemProperties(Item.Properties()))

        val barrelId = resloc("${barrelTierId}.${baseId.namespace}.${baseId.path}")

        Registry.register(BuiltInRegistries.BLOCK, barrelId, block)
        Registry.register(BuiltInRegistries.ITEM, barrelId, item)

        return block
    }
}