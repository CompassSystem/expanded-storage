package compass_system.expanded_storage.client.model

import com.mojang.datafixers.util.Either
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
    private val generatedSideTextureId: ResourceLocation,
    private val textures: MutableMap<String, Either<Material, String>> = mutableMapOf()
) : BlockModel(
    barrelBaseModelId,
    arrayListOf(),
    textures,
    null,
    null,
    ItemTransforms.NO_TRANSFORMS,
    arrayListOf()
) {
    companion object {
        val barrelBaseModelId = ResourceLocation("minecraft:block/cube_bottom_top")
    }
    override fun getDependencies(): Collection<ResourceLocation> {
        val rv = super.getDependencies()
        rv.add(baseModelId)
        return rv
    }

    override fun resolveParents(resolver: Function<ResourceLocation, UnbakedModel>) {
        val barrelModel = resolver.apply(baseModelId)

        if (barrelModel is MultiVariant) {
            val variant = barrelModel.variants.first()
            val baseModel = resolver.apply(variant.modelLocation) as? BlockModel ?: return

            baseModel.resolveParents(resolver)

            val element = baseModel.elements.find { it.from == Vector3f(0f) && it.to == Vector3f(16f) } ?: return

            val topTexture = element.faces[Direction.UP]?.let { baseModel.getMaterial(it.texture) } ?: return
            val bottomTexture = element.faces[Direction.DOWN]?.let { baseModel.getMaterial(it.texture) } ?: return

            textures["top"] = Either.left(topTexture)
            textures["side"] = Either.left(Material(TextureAtlas.LOCATION_BLOCKS, generatedSideTextureId))
            textures["bottom"] = Either.left(bottomTexture)
        }

        super.resolveParents(resolver)
    }

    override fun bake(
        modelBaker: ModelBaker,
        textureGetter: Function<Material, TextureAtlasSprite>,
        modelState: ModelState,
        modelId: ResourceLocation
    ): BakedModel? = if (textures.isNotEmpty()) {
        super.bake(modelBaker, textureGetter, modelState, modelId)
    } else {
        null
    }
}