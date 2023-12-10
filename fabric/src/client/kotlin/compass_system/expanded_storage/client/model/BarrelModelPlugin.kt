package compass_system.expanded_storage.client.model

import compass_system.expanded_storage.BarrelEntry
import compass_system.expanded_storage.barrel.BarrelInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.renderer.block.BlockModelShaper
import net.minecraft.client.renderer.texture.SpriteContents
import net.minecraft.client.resources.metadata.animation.FrameSize
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceMetadata
import net.minecraft.world.level.block.state.properties.BlockStateProperties

class BarrelModelPlugin(private val blocks: List<BarrelEntry>) : ModelLoadingPlugin {
    private var generatedModels = mutableMapOf<ResourceLocation, UnbakedModel>()

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

                val generatedClosedModelId = "${texture}.${entry.id.namespace}.${entry.id.path}"
                generatedModels[ResourceLocation("expanded-storage", generatedClosedModelId)] = closedModel
                generatedModels[ResourceLocation("expanded-storage", "item/$generatedClosedModelId")] = closedModel
                if ("copper" in generatedClosedModelId) {
                    generatedModels[ResourceLocation("expanded-storage", "item/waxed_$generatedClosedModelId")] = closedModel
                }

                val generatedOpenModelId = "${texture}.${entry.id.namespace}.${entry.id.path}_open"
                generatedModels[ResourceLocation("expanded-storage", generatedOpenModelId)] = openModel
            }
        }

        context.addModels(generatedModels.keys)

        BarrelInitializer.barrelBlocks.forEach { block ->
            context.registerBlockStateResolver(block, BarrelStateResolver)
        }

        context.resolveModel().register {
            if (it.id() in generatedModels) {
                return@register generatedModels[it.id()]
            }

            return@register null
        }
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