package compass_system.expanded_storage.client.model

import compass_system.expanded_storage.BarrelEntry
import compass_system.expanded_storage.ExpandedStorage
import compass_system.expanded_storage.ExpandedStorage.resloc
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.BlockModelShaper
import net.minecraft.client.renderer.texture.SpriteContents
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

class BarrelModelPlugin(private val blocks: List<BarrelEntry>) : ModelLoadingPlugin {
    override fun onInitializeModelLoader(context: ModelLoadingPlugin.Context) {
        val textures = listOf("copper", "exposed_copper", "weathered_copper", "oxidized_copper", "iron", "gold", "diamond", "netherite")
        val generatedModels = mutableMapOf<ResourceLocation, UnbakedModel>()

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
                generatedModels[ModelResourceLocation("expanded-storage", generatedClosedModelId, "")] = closedModel
                val generatedOpenModelId = "${texture}_${entry.id.namespace}_${entry.id.path}_open"
                generatedModels[ModelResourceLocation("expanded-storage", generatedOpenModelId, "")] = openModel
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

                if((path.startsWith("block/") || path.startsWith("item/")) && isBarrel) {
                    val tier = path.substring(path.indexOf('/') + 1, path.lastIndexOf('_'))

                    return@register ForwardingBarrelUnbakedModel(resloc(tier), path.endsWith("_barrel_open"))
                }
            }
            return@register null
        }
    }

    fun getBarrelModel(base: ResourceLocation, state: BlockState, textureId: ResourceLocation): BakedModel {
        val modelManager = Minecraft.getInstance().modelManager

        val open = if (state.getValue(BlockStateProperties.OPEN)) "_open" else ""


        return modelManager.getModel(
            ModelResourceLocation(
                textureId.namespace,
                "${textureId.path}_${base.namespace}_${base.path}${open}",
                BlockModelShaper.statePropertiesToString(state.values)
            )
        )
    }

    fun getBarrelBreakingParticle(base: ResourceLocation, state: BlockState, textureId: ResourceLocation): TextureAtlasSprite {
        return getBarrelModel(base, state, textureId).particleIcon
    }

    fun generateBarrelSideSprites(sprites: MutableList<SpriteContents>) {
//        val original: SpriteContents = sprites[75]

//        sprites.add(
//            SpriteContents(
//                ResourceLocation("expanded-storage", "block/air"),
//                FrameSize(original.width(), original.height()),
//                original.originalImage,
//                ResourceMetadata.EMPTY
//            )
//        )
    }
}