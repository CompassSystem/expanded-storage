package ellemes.container_library.forge.wrappers;

import ellemes.expandedstorage.common.recipe.ConversionRecipeManager;
import ellemes.expandedstorage.common.recipe.block.BlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.block.ManyBlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.block.SingleBlockConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.EntityConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.ManyEntityConversionRecipe;
import ellemes.expandedstorage.common.recipe.entity.SingleEntityConversionRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientboundUpdateRecipesMessage {
    private final List<BlockConversionRecipe<?>> blockRecipes;
    private final List<EntityConversionRecipe<?>> entityRecipes;

    public ClientboundUpdateRecipesMessage(List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes) {
        this.blockRecipes = blockRecipes;
        this.entityRecipes = entityRecipes;
    }

    public static void encode(ClientboundUpdateRecipesMessage msg, FriendlyByteBuf buffer) {
        ConversionRecipeManager.INSTANCE.writeRecipesToNetworkBuffer(buffer);
    }

    public static ClientboundUpdateRecipesMessage decode(FriendlyByteBuf buffer) {
        List<BlockConversionRecipe<?>> blockRecipes = new ArrayList<>();
        List<EntityConversionRecipe<?>> entityRecipes = new ArrayList<>();
        ResourceLocation type = buffer.readResourceLocation();
        if (type.equals(new ResourceLocation("expandedstorage", "single_block"))) {
            blockRecipes.addAll(buffer.<SingleBlockConversionRecipe<?>, ArrayList<SingleBlockConversionRecipe<?>>>readCollection(ArrayList::new, SingleBlockConversionRecipe::readFromBuffer));
        }
        if (type.equals(new ResourceLocation("expandedstorage", "many_block"))) {
            blockRecipes.addAll(buffer.<ManyBlockConversionRecipe<?>, ArrayList<ManyBlockConversionRecipe<?>>>readCollection(ArrayList::new, ManyBlockConversionRecipe::readFromBuffer));
        }
        if (type.equals(new ResourceLocation("expandedstorage", "single_entity"))) {
            entityRecipes.addAll(buffer.<SingleEntityConversionRecipe<?>, ArrayList<SingleEntityConversionRecipe<?>>>readCollection(ArrayList::new, SingleEntityConversionRecipe::readFromBuffer));
        }
        if (type.equals(new ResourceLocation("expandedstorage", "many_entity"))) {
            entityRecipes.addAll(buffer.<ManyEntityConversionRecipe<?>, ArrayList<ManyEntityConversionRecipe<?>>>readCollection(ArrayList::new, ManyEntityConversionRecipe::readFromBuffer));
        }
        return new ClientboundUpdateRecipesMessage(blockRecipes, entityRecipes);
    }

    public static void handle(ClientboundUpdateRecipesMessage msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ConversionRecipeManager.INSTANCE.replaceAllRecipes(msg.blockRecipes, msg.entityRecipes);
        });
    }
}
