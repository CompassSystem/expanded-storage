package compass_system.expanded_storage.client.model

import com.mojang.datafixers.util.Either
import compass_system.expanded_storage.ExpandedStorage
import net.minecraft.client.renderer.block.model.BlockModel
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.block.model.MultiVariant
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.*
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f
import java.util.function.Function

class GeneratedBarrelUnbakedModel(
    private val baseModelId: ResourceLocation,
    private val generatedSideTextureId: ResourceLocation
) : UnbakedModel {
    companion object {
        val barrelBaseModelId = ResourceLocation("minecraft:block/cube_bottom_top")
    }

    private var model: BlockModel? = null
    override fun getDependencies(): Collection<ResourceLocation> = listOf(baseModelId, barrelBaseModelId)

    override fun resolveParents(resolver: Function<ResourceLocation, UnbakedModel>) {
        val barrelModel = resolver.apply(baseModelId)

        if (barrelModel is MultiVariant) {
            val variant = barrelModel.variants.first()
            val baseModel = resolver.apply(variant.modelLocation) as? BlockModel ?: return

            baseModel.resolveParents(resolver)

            val element = baseModel.elements.find { it.from == Vector3f(0f) && it.to == Vector3f(16f) } ?: return

            val topTexture = element.faces[Direction.UP]?.let { baseModel.getMaterial(it.texture) } ?: return
            val bottomTexture = element.faces[Direction.DOWN]?.let { baseModel.getMaterial(it.texture) } ?: return

            model = BlockModel(
                barrelBaseModelId,
                arrayListOf(),
                buildMap<String, Either<Material, String>> {
                    put("top", Either.left(topTexture))
                    put("side", Either.left(Material(TextureAtlas.LOCATION_BLOCKS, generatedSideTextureId)))
                    put("bottom", Either.left(bottomTexture))
                }.toMutableMap(),
                null,
                null,
                ItemTransforms.NO_TRANSFORMS,
                arrayListOf()
            )

            model?.resolveParents(resolver)
        }
    }

    override fun bake(
        modelBaker: ModelBaker,
        textureGetter: Function<Material, TextureAtlasSprite>,
        modelState: ModelState,
        modelId: ResourceLocation
    ): BakedModel? {
        val rv =  model?.bake(modelBaker, textureGetter, modelState, modelId)
        ExpandedStorage.logger.info("GeneratedBarrelUnbakedModel.bake(): $rv")
        return rv
    }
}