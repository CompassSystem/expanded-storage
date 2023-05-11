package ellemes.expandedstorage.forge.datagen.providers;

import ellemes.expandedstorage.common.datagen.content.ModTags;
import ellemes.expandedstorage.common.datagen.providers.TagHelper;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.registration.ModBlocks;
import ellemes.expandedstorage.common.registration.ModItems;
import ellemes.expandedstorage.forge.datagen.content.ForgeTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public final class TagProvider {
    public static final class Block extends BlockTagsProvider {
        public Block(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, Utils.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags() {
            TagHelper.registerBlockTags(this::tag);
            this.tag(Tags.Blocks.CHESTS_WOODEN).add(ModBlocks.WOOD_CHEST);
            this.tag(ModTags.Blocks.ES_WOODEN_CHESTS)
                .addTag(Tags.Blocks.CHESTS_WOODEN);
        }

        @Override
        public String getName() {
            return "Expanded Storage - Block Tags";
        }
    }

    public static final class Item extends ItemTagsProvider {
        public Item(DataGenerator generator, BlockTagsProvider blockTagsProvider, ExistingFileHelper existingFileHelper) {
            super(generator, blockTagsProvider, Utils.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags() {
            TagHelper.registerItemTags(this::tag);
            this.tag(Tags.Items.CHESTS_WOODEN).add(ModItems.WOOD_CHEST);
            this.tag(ModTags.Items.ES_WOODEN_CHESTS)
                .addTag(Tags.Items.CHESTS_WOODEN);
            this.tag(ForgeTags.Items.BAMBOO)
                .add(Items.BAMBOO);
        }

        @Override
        public String getName() {
            return "Expanded Storage - Item Tags";
        }
    }

    public static final class EntityType extends EntityTypeTagsProvider {
        public EntityType(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, Utils.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags() {
            TagHelper.registerEntityTypeTags(this::tag);
        }

        @Override
        public String getName() {
            return "Expanded Storage - Entity Type Tags";
        }
    }
}
