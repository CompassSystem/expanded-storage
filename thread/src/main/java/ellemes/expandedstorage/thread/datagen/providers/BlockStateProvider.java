package ellemes.expandedstorage.thread.datagen.providers;

import ellemes.expandedstorage.common.datagen.providers.ModelHelper;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

public class BlockStateProvider extends FabricModelProvider {
    public BlockStateProvider(FabricDataGenerator generator) {
        super(generator);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerators generator) {
        Consumer<Item> generateFlatItemModel = item -> generator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        ModelHelper.registerItemModels(generateFlatItemModel);
//        Consumer<Item> minecartChestParentModel = item -> generator.generateFlatItem(item, Items.CHEST_MINECART, ModelTemplates.FLAT_ITEM);
//        ModelHelper.registerChestItemModels(minecartChestParentModel);

    }

    @Override
    public String getName() {
        return "Expanded Storage - BlockStates / Models";
    }
}
