package compass_system.expanded_storage.barrel

import compass_system.expanded_storage.ExpandedStorage.resloc
import compass_system.expanded_storage.barrel.block.BarrelBlock
import compass_system.expanded_storage.barrel.block.BarrelBlockEntity
import compass_system.expanded_storage.barrel.block.BarrelItem
import compass_system.expanded_storage.barrel.block.CopperBarrelBlock
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.WeatheringCopper.WeatherState
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.state.BlockBehaviour.Properties as BlockProperties

object BarrelInitializer : ModInitializer {
    internal lateinit var BLOCK_ENTITY_TYPE: BlockEntityType<BarrelBlockEntity>

    override fun onInitialize() {
        val weakMetalProperties = baseProperties().strength(3F, 6F)
        val barrels = listOf(
            createCopperBarrel("copper", weakMetalProperties, WeatherState.UNAFFECTED),
            createCopperBarrel("exposed_copper", weakMetalProperties, WeatherState.EXPOSED),
            createCopperBarrel("weathered_copper", weakMetalProperties, WeatherState.WEATHERED),
            createCopperBarrel("oxidized_copper", weakMetalProperties, WeatherState.OXIDIZED),
            createBarrel("waxed_copper", Tiers.COPPER, weakMetalProperties),
            createBarrel("waxed_exposed_copper", Tiers.COPPER, weakMetalProperties),
            createBarrel("waxed_weathered_copper", Tiers.COPPER, weakMetalProperties),
            createBarrel("waxed_oxidized_copper", Tiers.COPPER, weakMetalProperties),
            createBarrel("iron", Tiers.IRON, baseProperties().strength(5F, 6F)),
            createBarrel("gold", Tiers.GOLD, weakMetalProperties),
            createBarrel("diamond", Tiers.DIAMOND, baseProperties().strength(5F, 6F)),
            createBarrel("netherite", Tiers.NETHERITE, baseProperties().strength(50F, 1200F)),
        )

        BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of({ pos, state ->
            BarrelBlockEntity(BLOCK_ENTITY_TYPE, pos, state)
        }, *barrels.toTypedArray()).build(null)

        Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation("expanded-storage", "barrel"),
            BLOCK_ENTITY_TYPE
        )
    }

    private fun baseProperties() = BlockProperties.of()
        .mapColor(MapColor.WOOD)
        .instrument(NoteBlockInstrument.BASS)
        .sound(SoundType.WOOD)
        .ignitedByLava()

    private fun createCopperBarrel(barrelId: String, properties: BlockProperties, weatherState: WeatherState): CopperBarrelBlock {
        val block = CopperBarrelBlock(Tiers.COPPER.applyBlockProperties(properties), weatherState, resloc(barrelId))
        val item = BarrelItem(block, Tiers.COPPER.applyItemProperties(Item.Properties()))

        val id = resloc("${barrelId}_barrel")

        Registry.register(BuiltInRegistries.BLOCK, id, block)
        Registry.register(BuiltInRegistries.ITEM, id, item)

        return block
    }

    private fun createBarrel(barrelId: String, tier: Tiers, properties: BlockProperties): BarrelBlock {
        val block = BarrelBlock(tier.applyBlockProperties(properties), tier, resloc(barrelId.replaceFirst("waxed_", "")))
        val item = BarrelItem(block, tier.applyItemProperties(Item.Properties()))

        val id = resloc("${barrelId}_barrel")

        Registry.register(BuiltInRegistries.BLOCK, id, block)
        Registry.register(BuiltInRegistries.ITEM, id, item)

        return block
    }
}