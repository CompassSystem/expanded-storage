package compass_system.expanded_storage.client.mixin;

import compass_system.expanded_storage.ExpandedStorageClient;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(SpriteLoader.class)
public class GeneratedSpriteLoaderMixin {
    @Shadow @Final private ResourceLocation location;

    @ModifyArg(
            method = "method_47659",
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/texture/SpriteLoader;stitch(Ljava/util/List;ILjava/util/concurrent/Executor;)Lnet/minecraft/client/renderer/texture/SpriteLoader$Preparations;"
        ),
            index = 0
    )
    private List<SpriteContents> addSprites(List<SpriteContents> list) {
        if (location.toString().equals("minecraft:textures/atlas/blocks.png")) {
            ArrayList<SpriteContents> sprites = new ArrayList<>(list);

            ExpandedStorageClient.INSTANCE.getModelPlugin().generateBarrelSideSprites(sprites);

            return sprites;
        }

        return list;
    }
}
