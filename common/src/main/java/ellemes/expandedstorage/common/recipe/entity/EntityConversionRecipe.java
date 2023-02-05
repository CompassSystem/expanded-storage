package ellemes.expandedstorage.common.recipe.entity;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class EntityConversionRecipe<T extends Entity> {
    private final EntityType<T> output;

    public EntityConversionRecipe(EntityType<T> output) {
        this.output = output;
    }

    public abstract boolean inputMatches(EntityType<?> input);

    public void process(Level level, Entity input) {

    }

    public int getUsageCount(Entity input) {
        return 1;
    }

    public EntityType<T> getOutputType() {
        return output;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(Registry.ENTITY_TYPE.getKey(output));
    }
}
