package compasses.expandedstorage.common.client;

import com.mojang.blaze3d.vertex.PoseStack;
import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.block.entity.ChestBlockEntity;
import compasses.expandedstorage.common.entity.ChestMinecart;
import compasses.expandedstorage.common.registration.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ChestMinecartRenderer extends MinecartRenderer<ChestMinecart> {
    private final ChestBlockEntity internalEntity;

    public ChestMinecartRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context, modelLayerLocation);
        internalEntity = CommonMain.getChestBlockEntityType().create(BlockPos.ZERO, ModBlocks.WOOD_CHEST.defaultBlockState());
    }

    @Override
    protected void renderMinecartContents(ChestMinecart entity, float partialTicks, BlockState state, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        internalEntity.setBlockState(entity.getDisplayBlockState());
        internalEntity.setVisualLockStyle(entity.getEntityData().get(ChestMinecart.VISUAL_LOCK));
        Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(internalEntity)
                 .render(internalEntity, partialTicks, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
