package ellemes.expandedstorage.common.recipe.entity;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Collection;

public class ManyEntityConversionRecipe<T extends Entity> extends EntityConversionRecipe<T>{
    private final Collection<EntityType<?>> inputEntities;

    public ManyEntityConversionRecipe(Collection<EntityType<?>> inputEntities, EntityType<T> output) {
        super(output);
        this.inputEntities = inputEntities;
    }

    @Override
    public boolean inputMatches(EntityType<?> input) {
        return inputEntities.contains(input);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        super.writeToBuffer(buffer);
        buffer.writeCollection(inputEntities, (b, entity) -> b.writeResourceLocation(Registry.ENTITY_TYPE.getKey(entity)));
    }

    public static ManyEntityConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        return null;
    }
}
