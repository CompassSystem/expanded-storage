package compass_system.expanded_storage.client.model

import compass_system.expanded_storage.ExpandedStorageClient
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.function.Supplier

class ForwardingBarrelBakedModel(private val textureId: ResourceLocation, private val open: Boolean) : BakedModel, FabricBakedModel {
    private val baseModel
        get() = Minecraft.getInstance().modelManager.blockModelShaper.getBlockModel(Blocks.BARREL.defaultBlockState())
    private val cache: MutableMap<String, BakedModel> = mutableMapOf()

    // Fabric BakedModel methods below:

    override fun isVanillaAdapter(): Boolean = false

    override fun emitBlockQuads(
        blockView: BlockAndTintGetter,
        state: BlockState,
        pos: BlockPos,
        random: Supplier<RandomSource>,
        context: RenderContext
    ) {
        val base = blockView.getBlockEntityRenderData(pos) as Block? ?: Blocks.BARREL
        val model = ExpandedStorageClient.modelPlugin.getBarrelModel(base.builtInRegistryHolder().key().location(), state, textureId)
        model.emitBlockQuads(blockView, state, pos, random, context)
    }

    override fun emitItemQuads(stack: ItemStack, random: Supplier<RandomSource>, context: RenderContext) {
        val model = cache.computeIfAbsent(getStackCacheKey(stack)) {
            val baseId = ResourceLocation(stack.tag?.getString("base") ?: "minecraft:barrel")
            val state = Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.OPEN, false).setValue(BlockStateProperties.FACING, Direction.UP)
            ExpandedStorageClient.modelPlugin.getBarrelModel(baseId, state, textureId)
        }

        model.emitItemQuads(stack, random, context)
    }

    private fun getStackCacheKey(stack: ItemStack): String {
        return stack.item.builtInRegistryHolder().key().location().toString() + (stack.tag?.getString("base")?.toString() ?: "minecraft:barrel")
    }

    // Vanilla BakedModel methods below:

    override fun getQuads(state: BlockState?, direction: Direction?, random: RandomSource): List<BakedQuad> = emptyList()

    override fun useAmbientOcclusion(): Boolean = true

    override fun isGui3d(): Boolean = false

    override fun usesBlockLight(): Boolean = true

    override fun isCustomRenderer(): Boolean = false

    override fun getParticleIcon(): TextureAtlasSprite = baseModel.particleIcon

    override fun getTransforms(): ItemTransforms = baseModel.transforms

    override fun getOverrides(): ItemOverrides = baseModel.overrides
}