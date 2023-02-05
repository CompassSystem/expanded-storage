package ellemes.expandedstorage.common.recipe.entity;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class SingleEntityConversionRecipe<T extends Entity> extends EntityConversionRecipe<T>{
    private final EntityType<?> inputEntity;

    public SingleEntityConversionRecipe(EntityType<?> input, EntityType<T> output) {
        super(output);
        this.inputEntity = input;
    }

    @Override
    public boolean inputMatches(EntityType<?> input) {
        return inputEntity == input;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        super.writeToBuffer(buffer);
        buffer.writeResourceLocation(Registry.ENTITY_TYPE.getKey(inputEntity));
    }

    public static SingleEntityConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        return null;
    }
}
