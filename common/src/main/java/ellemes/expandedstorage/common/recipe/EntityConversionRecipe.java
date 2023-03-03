package ellemes.expandedstorage.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ellemes.expandedstorage.common.entity.ChestMinecart;
import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
import ellemes.expandedstorage.common.recipe.misc.RecipeTool;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityConversionRecipe<O extends Entity> extends ConversionRecipe<Entity> {
    private final EntityType<O> output;

    public EntityConversionRecipe(RecipeTool recipeTool, EntityType<O> output, Collection<? extends RecipeCondition> inputs) {
        super(recipeTool, inputs);
        this.output = output;
    }

    public InteractionResult process(Level level, Player player, ItemStack tool, Entity input) {
        if (tool.isEmpty()) { // How...
            return InteractionResult.FAIL;
        }

        if (input.getType() == output) {
            return InteractionResult.FAIL;
        }

        if (!simulateSpawnUpgradedMinecartChest(input)) {
            return InteractionResult.FAIL;
        } else if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ChestMinecart newCart = (ChestMinecart) output.create(serverLevel, null, input.hasCustomName() ? input.getCustomName() : null, null, input.getOnPos(), MobSpawnType.COMMAND, true, false);

        if (newCart == null) {
            return InteractionResult.FAIL;
        }

        boolean isMinecraftCart = input instanceof AbstractMinecartContainer;
        NonNullList<ItemStack> items = isMinecraftCart ? ((AbstractMinecartContainer) input).itemStacks : ((ChestMinecart) input).getItems();
        int inserted = newCart.replaceInventoryWith(items);
        if (inserted < items.size()) {
            Vec3 pos = input.position();
            for (int i = inserted; i < items.size(); i++) {
                Containers.dropItemStack(level, pos.x(), pos.y(), pos.z(), items.get(i));
            }
        }
        newCart.setPos(input.position());
        newCart.setXRot(input.getXRot());
        newCart.setYRot(input.getYRot());
        newCart.setDeltaMovement(input.getDeltaMovement());
        if (input.hasCustomName()) {
            newCart.setCustomName(input.getCustomName());
        }
        serverLevel.addFreshEntityWithPassengers(newCart);
        ((Clearable) input).clearContent();
        input.remove(Entity.RemovalReason.DISCARDED);

        if (recipeTool instanceof RecipeTool.UpgradeTool && !player.isCreative()) {
            tool.setCount(tool.getCount() - 1);
        }

        return InteractionResult.CONSUME;
    }

    private static boolean simulateSpawnUpgradedMinecartChest(Entity original) {
        boolean isMinecraftCart = original instanceof AbstractMinecartContainer;
        boolean isOurCart = original instanceof ChestMinecart;
        return isOurCart || isMinecraftCart;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        recipeTool.writeToBuffer(buffer);
        buffer.writeResourceLocation(Registry.ENTITY_TYPE.getKey(output));
        buffer.writeCollection(inputs, (b, condition) -> {
            b.writeResourceLocation(condition.getNetworkId());
            condition.writeToBuffer(buffer);
        });
    }

    public static EntityConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        RecipeTool recipeTool = RecipeTool.fromNetworkBuffer(buffer);
        EntityType<?> output = Registry.ENTITY_TYPE.get(buffer.readResourceLocation());
        List<RecipeCondition> inputs = buffer.readCollection(ArrayList::new, RecipeCondition::readFromBuffer);
        return new EntityConversionRecipe<>(recipeTool, output, inputs);
    }

    public JsonElement toJson() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "expandedstorage:entity_conversion");
        recipe.add("tool", recipeTool.toJson());
        recipe.addProperty("result", output.builtInRegistryHolder().key().location().toString());
        JsonArray jsonInputs = new JsonArray();
        for (RecipeCondition input : inputs) {
            jsonInputs.add(input.toJson());
        }
        recipe.add("inputs", jsonInputs);
        return recipe;
    }
}
