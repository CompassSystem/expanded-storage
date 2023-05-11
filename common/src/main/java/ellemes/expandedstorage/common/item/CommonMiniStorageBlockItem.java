package ellemes.expandedstorage.common.item;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CommonMiniStorageBlockItem extends BlockItem {
    public CommonMiniStorageBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            items.add(this.getDefaultInstance());

            ItemStack stack = new ItemStack(this);
            CompoundTag tag = new CompoundTag();
            CompoundTag blockStateTag = new CompoundTag();
            blockStateTag.putString("sparrow", "true");
            tag.put("BlockStateTag", blockStateTag);
            stack.setTag(tag);
            items.add(stack);
        }
    }
}
