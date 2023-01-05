package ellemes.expandedstorage.forge.datagen.providers;

import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public final class BlockStatesAndModels extends BlockStateProvider {
    public BlockStatesAndModels(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, Utils.MOD_ID, fileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }

    @Override
    public String getName() {
        return "Expanded Storage - BlockStates / Models";
    }
}
