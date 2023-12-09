package compass_system.expanded_storage.client.model

import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.*
import net.minecraft.resources.ResourceLocation
import java.util.function.Function

class ForwardingBarrelUnbakedModel(private val tier: ResourceLocation, private val open: Boolean) : UnbakedModel {
    override fun getDependencies(): Collection<ResourceLocation> = emptyList()

    override fun resolveParents(function: Function<ResourceLocation, UnbakedModel>) {

    }

    override fun bake(
        modelBaker: ModelBaker,
        textureGetter: Function<Material, TextureAtlasSprite>,
        modelState: ModelState,
        modelId: ResourceLocation
    ): BakedModel {
        return ForwardingBarrelBakedModel(tier, open)
    }
}