package ellemes.expandedstorage.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ellemes.expandedstorage.api.EsChestType;
import ellemes.expandedstorage.api.ExpandedStorageAccessors;
import ellemes.expandedstorage.common.block.AbstractChestBlock;
import ellemes.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import ellemes.expandedstorage.common.block.strategies.Lockable;
import ellemes.expandedstorage.common.item.ToolUsageResult;
import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
import ellemes.expandedstorage.common.recipe.misc.PartialBlockState;
import ellemes.expandedstorage.common.recipe.misc.RecipeTool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class BlockConversionRecipe<O extends Block> extends ConversionRecipe<BlockState> {
    private final PartialBlockState<O> output;

    public BlockConversionRecipe(RecipeTool recipeTool, PartialBlockState<O> output, Collection<? extends RecipeCondition> inputs) {
        super(recipeTool, inputs);
        this.output = output;
    }

    // todo: this split double chests, we don't know why.
    public final ToolUsageResult process(Level level, Player player, ItemStack tool, BlockState state, BlockPos pos) {
        List<BlockPos> convertPositions = new ArrayList<>();
        convertPositions.add(pos);
        if (!output.matches(state)) {
            if (state.hasProperty(BlockStateProperties.CHEST_TYPE)) {
                ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);
                if (type != ChestType.SINGLE) {
                    convertPositions.add(pos.relative(ChestBlock.getConnectedDirection(state)));
                }
            } else if (state.hasProperty(AbstractChestBlock.CURSED_CHEST_TYPE) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                ExpandedStorageAccessors.getAttachedChestDirection(state).ifPresent(direction -> convertPositions.add(pos.relative(direction)));
            }
        }

        if (tool.getCount() < convertPositions.size() && !player.isCreative()) {
            return ToolUsageResult.fail();
        }

        HashMap<BlockPos, List<ItemStack>> items = new HashMap<>();
        HashMap<BlockPos, Component> customNames = new HashMap<>();
        HashMap<BlockPos, CompoundTag> locks = new HashMap<>();
        // probably replace these maps with a single for loop.

        for (BlockPos position : convertPositions) {
            BlockEntity entity = level.getBlockEntity(position);

            if (entity instanceof OpenableBlockEntity blockEntity) {
                items.put(position, blockEntity.getItems());
                if (blockEntity.hasCustomName()) {
                    customNames.put(position, blockEntity.getName());
                }
                locks.put(position, blockEntity.saveWithoutMetadata());
            } else if (entity instanceof RandomizableContainerBlockEntity blockEntity) {
                NonNullList<ItemStack> entityItems = blockEntity.getItems();
                if (entityItems.size() > 27) {
                    return ToolUsageResult.fail();
                }
                items.put(position, entityItems);
                if (blockEntity.hasCustomName()) {
                    customNames.put(position, blockEntity.getName());
                }
                locks.put(position, blockEntity.saveWithoutMetadata());
            } else {
                return ToolUsageResult.fail();
            }
        }

        for (int posIndex = convertPositions.size() - 1; posIndex >= 0; posIndex--) {
            BlockPos position = convertPositions.get(posIndex);

            BlockState originalState = level.getBlockState(position);
            BlockState newState = output.getBlock().withPropertiesOf(originalState);
            if (originalState.hasProperty(BlockStateProperties.CHEST_TYPE) && newState.hasProperty(AbstractChestBlock.CURSED_CHEST_TYPE)) {
                newState = newState.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, EsChestType.from(originalState.getValue(BlockStateProperties.CHEST_TYPE)));
            }
            newState = output.transform(newState);
            level.removeBlockEntity(position);
            level.setBlockAndUpdate(position, newState);
            BlockEntity entity = level.getBlockEntity(position);
            if (entity instanceof OpenableBlockEntity blockEntity) {
                List<ItemStack> inventory = items.get(position);
                List<ItemStack> newInventory = blockEntity.getItems();
                int commonSize = Math.min(inventory.size(), newInventory.size());
                for (int i = 0; i < commonSize; i++) {
                    newInventory.set(i, inventory.get(i));
                }

                if (newInventory.size() < inventory.size()) { // Why in god's name is someone making an upgrade convert to a smaller chest...
                    for (int i = newInventory.size(); i < inventory.size(); i++) {
                        Containers.dropItemStack(level, position.getX(), position.getY(), position.getZ(), inventory.get(i));
                    }
                }

                if (customNames.containsKey(position)) {
                    blockEntity.setCustomName(customNames.get(position));
                }
                Lockable lockable = blockEntity.getLockable();
                lockable.readLock(locks.get(position));
            }
        }

        if (recipeTool instanceof RecipeTool.UpgradeTool) {
            tool.setCount(tool.getCount() - convertPositions.size());
        }

        return ToolUsageResult.slowSuccess();
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        recipeTool.writeToBuffer(buffer);
        output.writeToBuffer(buffer);
        buffer.writeCollection(inputs, (b, recipeCondition) -> {
            b.writeResourceLocation(recipeCondition.getNetworkId());
            recipeCondition.writeToBuffer(b);
        });
    }

    public static BlockConversionRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
        RecipeTool recipeTool = RecipeTool.fromNetworkBuffer(buffer);
        PartialBlockState<?> output = PartialBlockState.readFromBuffer(buffer);
        List<RecipeCondition> inputs = buffer.readCollection(ArrayList::new, RecipeCondition::readFromBuffer);
        return new BlockConversionRecipe<>(recipeTool, output, inputs);
    }

    public JsonElement toJson() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "expandedstorage:block_conversion");
        recipe.add("tool", recipeTool.toJson());
        recipe.add("result", output.toJson());
        JsonArray jsonInputs = new JsonArray();
        for (RecipeCondition input : inputs) {
            jsonInputs.add(input.toJson());
        }
        recipe.add("inputs", jsonInputs);
        return recipe;
    }
}
