package ellemes.expandedstorage.common.recipe.misc;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface RecipeCondition<T> {
    RecipeCondition<BlockState> IS_WOODEN_CHEST = new IsInstanceOfCondition(ChestBlock.class);
    RecipeCondition<BlockState> IS_WOODEN_BARREL = new IsInstanceOfCondition(BarrelBlock.class);

    boolean test(T subject);

    void writeToBuffer(FriendlyByteBuf buffer);

    static <T> RecipeCondition<T> readFromBuffer(FriendlyByteBuf buffer) {
        // todo fill
        return null;
    }
}

class IsInTagCondition<T> implements RecipeCondition<T> {
    private final Set<T> values;

    public IsInTagCondition(Registry<T> registry, TagKey<T> tagKey) {
        this.values = registry.getTag(tagKey).orElseThrow().stream().map(Holder::value).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean test(T subject) {
        return values.contains(subject);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {

    }
}

class IsInstanceOfCondition<T> implements RecipeCondition<T> {
    private final Class<?> clazz;

    public IsInstanceOfCondition(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean test(T subject) {
        return clazz.isAssignableFrom(subject.getClass());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {

    }
}

class IsRegistryObject<T> implements RecipeCondition<T> {
    private final T value;

    public IsRegistryObject(Registry<T> registry, ResourceLocation id) {
        this.value = registry.get(id);
    }

    public IsRegistryObject(T value) {
        this.value = value;
    }

    @Override
    public boolean test(T subject) {
        return subject == value;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {

    }
}

class WrappedSatisfiesCondition<W, U> implements RecipeCondition<W> {
    private final Function<W, U> unwrapper;
    private final RecipeCondition<U> base;

    public WrappedSatisfiesCondition(Function<W, U> unwrapper, RecipeCondition<U> base) {
        this.unwrapper = unwrapper;
        this.base = base;
    }

    @Override
    public boolean test(W subject) {
        U unwrapped = unwrapper.apply(subject);
        return base.test(unwrapped);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {

    }
}

class AndCondition<T> implements RecipeCondition<T> {
    private final RecipeCondition<T>[] conditions;

    @SafeVarargs
    public AndCondition(RecipeCondition<T>... conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(T subject) {
        for (RecipeCondition<T> condition : conditions) {
            if (!condition.test(subject)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {

    }
}

//class Test {
//    public static void init() {
//        RecipeCondition<BlockState> test = new AndCondition<>(
//                new WrappedSatisfiesCondition<>(BlockState::getBlock, new IsRegistryObject<>(ModBlocks.GOLD_CHEST)),
//                subject -> subject.hasProperty(AbstractChestBlock.CURSED_CHEST_TYPE) && subject.getValue(AbstractChestBlock.CURSED_CHEST_TYPE) == EsChestType.SINGLE
//        );
//        WrappedSatisfiesCondition<BlockState, Block> condition =
//                new WrappedSatisfiesCondition<>(
//                        BlockStateBase::getBlock,
//                        new IsRegistryObject<>(Registry.BLOCK, new ResourceLocation("expandedstorage", "gold_chest"))
//                );
//    }
//}

// is block
// complex action e.g. has specific property value
// is in tag
// is instanceof class
