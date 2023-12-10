package compass_system.expanded_storage.client.model

import compass_system.expanded_storage.BarrelEntry
import compass_system.expanded_storage.ExpandedStorage
import compass_system.expanded_storage.ExpandedStorage.resloc
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.BlockModelShaper
import net.minecraft.client.renderer.block.model.BlockModelDefinition
import net.minecraft.client.renderer.block.model.MultiVariant
import net.minecraft.client.renderer.block.model.Variant
import net.minecraft.client.renderer.texture.SpriteContents
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.metadata.animation.FrameSize
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.BlockModelRotation
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceMetadata
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

class BarrelModelPlugin(private val blocks: List<BarrelEntry>) : ModelLoadingPlugin {
    var generatedModels = mutableMapOf<ResourceLocation, UnbakedModel>()

    override fun onInitializeModelLoader(context: ModelLoadingPlugin.Context) {
        val textures = listOf("copper", "exposed_copper", "weathered_copper", "oxidized_copper", "iron", "gold", "diamond", "netherite")

        blocks.forEach { entry ->
            textures.forEach { texture ->
                val generatedSideTextureId = ResourceLocation("expanded-storage", "block/${texture}_${entry.id.namespace}_${entry.id.path}_side")
                val closedModel = GeneratedBarrelUnbakedModel(
                    BlockModelShaper.stateToModelLocation(entry.defaultState()),
                    generatedSideTextureId
                )
                val openModel = GeneratedBarrelUnbakedModel(
                    BlockModelShaper.stateToModelLocation(entry.defaultState().setValue(BlockStateProperties.OPEN, true)),
                    generatedSideTextureId
                )

                val generatedClosedModelId = "${texture}_${entry.id.namespace}_${entry.id.path}"
                generatedModels[ResourceLocation("expanded-storage", generatedClosedModelId)] = closedModel
                val generatedOpenModelId = "${texture}_${entry.id.namespace}_${entry.id.path}_open"
                generatedModels[ResourceLocation("expanded-storage", generatedOpenModelId)] = openModel

                val blockModel = BlockModelDefinition(mapOf(
                    "facing=up,open=false" to MultiVariant(listOf(Variant(resloc(generatedClosedModelId), BlockModelRotation.X0_Y0.rotation, false, 1))),
                    "facing=down,open=false" to MultiVariant(listOf(Variant(resloc(generatedClosedModelId), BlockModelRotation.X180_Y0.rotation, false, 1))),
                    "facing=north,open=false" to MultiVariant(listOf(Variant(resloc(generatedClosedModelId), BlockModelRotation.X90_Y0.rotation, false, 1))),
                    "facing=south,open=false" to MultiVariant(listOf(Variant(resloc(generatedClosedModelId), BlockModelRotation.X90_Y180.rotation, false, 1))),
                    "facing=east,open=false" to MultiVariant(listOf(Variant(resloc(generatedClosedModelId), BlockModelRotation.X90_Y90.rotation, false, 1))),
                    "facing=west,open=false" to MultiVariant(listOf(Variant(resloc(generatedClosedModelId), BlockModelRotation.X90_Y270.rotation, false, 1))),

                    "facing=up,open=true" to MultiVariant(listOf(Variant(resloc(generatedOpenModelId), BlockModelRotation.X0_Y0.rotation, false, 1))),
                    "facing=down,open=true" to MultiVariant(listOf(Variant(resloc(generatedOpenModelId), BlockModelRotation.X180_Y0.rotation, false, 1))),
                    "facing=north,open=true" to MultiVariant(listOf(Variant(resloc(generatedOpenModelId), BlockModelRotation.X90_Y0.rotation, false, 1))),
                    "facing=south,open=true" to MultiVariant(listOf(Variant(resloc(generatedOpenModelId), BlockModelRotation.X90_Y180.rotation, false, 1))),
                    "facing=east,open=true" to MultiVariant(listOf(Variant(resloc(generatedOpenModelId), BlockModelRotation.X90_Y90.rotation, false, 1))),
                    "facing=west,open=true" to MultiVariant(listOf(Variant(resloc(generatedOpenModelId), BlockModelRotation.X90_Y270.rotation, false, 1))),
                ), null)

                blockModel.variants.forEach { (key, variant) ->
                    generatedModels[ModelResourceLocation(resloc(generatedClosedModelId), key)] = variant
                }
            }
        }

        context.addModels(generatedModels.keys)

        context.resolveModel().register {
            if (it.id() in generatedModels) {
                return@register generatedModels[it.id()]
            }

            if (it.id().namespace == ExpandedStorage.MOD_ID) {
                val path = it.id().path

                val isBarrel = path.endsWith("_barrel") || path.endsWith("_barrel_open")

                if ((path.startsWith("block/") || path.startsWith("item/")) && isBarrel) {
                    val tier = path.substring(path.indexOf('/') + 1, path.lastIndexOf('_'))

                    return@register ForwardingBarrelUnbakedModel(resloc(tier), path.endsWith("_barrel_open"))
                }
            }
            return@register null
        }
    }

    fun getBarrelModel(base: ResourceLocation, state: BlockState, textureId: ResourceLocation): BakedModel {
        val modelManager = Minecraft.getInstance().modelManager

        return modelManager.getModel(
            ModelResourceLocation(
                textureId.namespace,
                "${textureId.path}_${base.namespace}_${base.path}",
                BlockModelShaper.statePropertiesToString(state.values)
            )
        )
    }

    fun getBarrelBreakingParticle(base: ResourceLocation, state: BlockState, textureId: ResourceLocation): TextureAtlasSprite {
        return getBarrelModel(base, state, textureId).particleIcon
    }

    fun generateBarrelSideSprites(sprites: MutableList<SpriteContents>) {
        val textures = listOf("copper", "exposed_copper", "weathered_copper", "oxidized_copper", "iron", "gold", "diamond", "netherite")

        blocks.forEach { entry ->
            textures.forEach { texture ->
                val generatedSideTextureId = ResourceLocation("expanded-storage", "block/${texture}_${entry.id.namespace}_${entry.id.path}_side")

                val original = sprites.random()

                sprites.add(
                    SpriteContents(
                        generatedSideTextureId,
                        FrameSize(original.width(), original.height()),
                        original.originalImage,
                        ResourceMetadata.EMPTY
                    )
                )
            }
        }
    }
}