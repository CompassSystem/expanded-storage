package ellemes.expandedstorage.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ellemes.expandedstorage.api.EsChestType;
import ellemes.expandedstorage.api.ExpandedStorageAccessors;
import ellemes.expandedstorage.common.block.AbstractChestBlock;
import ellemes.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import ellemes.expandedstorage.common.item.ToolUsageResult;
import ellemes.expandedstorage.common.recipe.conditions.RecipeCondition;
import ellemes.expandedstorage.common.recipe.misc.PartialBlockState;
import ellemes.expandedstorage.common.recipe.misc.RecipeTool;
import net.minecraft.core.BlockPos;
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

    private record InputState(BlockState state, BlockEntity entity) {

    }

    public final ToolUsageResult process(Level level, Player player, ItemStack tool, BlockState clickedState, BlockPos clickedPos) {
        List<BlockPos> convertPositions = new ArrayList<>();
        convertPositions.add(clickedPos);
        if (clickedState.hasProperty(BlockStateProperties.CHEST_TYPE)) {
            ChestType type = clickedState.getValue(BlockStateProperties.CHEST_TYPE);
            if (type != ChestType.SINGLE) {
                convertPositions.add(clickedPos.relative(ChestBlock.getConnectedDirection(clickedState)));
            }
        } else if (clickedState.hasProperty(AbstractChestBlock.CURSED_CHEST_TYPE) && clickedState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            ExpandedStorageAccessors.getAttachedChestDirection(clickedState).ifPresent(direction -> convertPositions.add(clickedPos.relative(direction)));
        }

        if (tool.getCount() < convertPositions.size() && !player.isCreative()) {
            return ToolUsageResult.fail();
        }

        HashMap<BlockPos, InputState> originalStates = new HashMap<>();

        for (BlockPos position : convertPositions) {
            BlockEntity entity = level.getBlockEntity(position);

            if (!(entity instanceof OpenableBlockEntity || entity instanceof RandomizableContainerBlockEntity)) {
                return ToolUsageResult.fail();
            }
            originalStates.put(position, new InputState(level.getBlockState(position), entity));
        }

        int toolsUsed = 0;
        for (BlockPos position : convertPositions) {
            InputState input = originalStates.get(position);
            BlockState originalState = input.state();
            BlockState newState = output.getBlock().withPropertiesOf(originalState);
            if (originalState.hasProperty(BlockStateProperties.CHEST_TYPE) && newState.hasProperty(AbstractChestBlock.CURSED_CHEST_TYPE)) {
                newState = newState.setValue(AbstractChestBlock.CURSED_CHEST_TYPE, EsChestType.from(originalState.getValue(BlockStateProperties.CHEST_TYPE)));
            }
            newState = output.transform(newState);
            if (newState != originalState) {
                List<ItemStack> originalItems;
                Component customName = null;
                CompoundTag tagForLock = input.entity().saveWithoutMetadata();

                if (input.entity() instanceof OpenableBlockEntity entity) {
                    originalItems = entity.getItems();
                    if (entity.hasCustomName()) {
                        customName = entity.getName();
                    }
                } else if (input.entity() instanceof RandomizableContainerBlockEntity entity) {
                    originalItems = entity.getItems();
                    customName = entity.getCustomName();
                } else {
                    throw new IllegalStateException();
                }

                level.removeBlockEntity(position);
                if (level.setBlockAndUpdate(position, newState)) {
                    if (level.getBlockEntity(position) instanceof OpenableBlockEntity entity) {
                        List<ItemStack> newInventory = entity.getItems();
                        int commonSize = Math.min(originalItems.size(), newInventory.size());
                        for (int i = 0; i < commonSize; i++) {
                            newInventory.set(i, originalItems.get(i));
                        }

                        if (newInventory.size() < originalItems.size()) { // Why in god's name is someone making an upgrade convert to a smaller chest...
                            for (int i = newInventory.size(); i < originalItems.size(); i++) {
                                Containers.dropItemStack(level, position.getX(), position.getY(), position.getZ(), originalItems.get(i));
                            }
                        }

                        entity.setCustomName(customName);
                        entity.getLockable().readLock(tagForLock);
                    }
                    toolsUsed++;
                } else {
                    level.setBlockEntity(input.entity());
                }
            }
        }

        if (recipeTool instanceof RecipeTool.UpgradeTool) {
            tool.setCount(tool.getCount() - toolsUsed);
        }

        return toolsUsed > 0 ? ToolUsageResult.slowSuccess() : ToolUsageResult.fail();
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
