package ellemes.expandedstorage.forge.mixin;

import ellemes.expandedstorage.common.block.OpenableBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashMap;
import java.util.Map;

@Mixin(OpenableBlock.class)
public class OxidizableBlockMixin extends Block {
    private final Map<Block, Block> WAX_REMOVAL_MAP = new HashMap<>();
    private final Map<Block, Block> OXIDISATION_REMOVAL_MAP = new HashMap<>();

    public OxidizableBlockMixin(Properties arg) {
        super(arg);
    }

    @Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
        boolean isRemovingOxidisation = action == ToolActions.AXE_SCRAPE;
        boolean isRemovingWax = action == ToolActions.AXE_WAX_OFF;
        if (isRemovingOxidisation || isRemovingWax) {
            ResourceLocation blockId = ((OpenableBlock) state.getBlock()).getBlockId();
            Block returnBlock = null;
            if (action == ToolActions.AXE_SCRAPE) {
                if ((blockId.getPath().contains("exposed_") || blockId.getPath().contains("weathered_") || blockId.getPath().contains("oxidized_") ) &&
                        !blockId.getPath().contains("waxed")) {
                    returnBlock = OXIDISATION_REMOVAL_MAP.computeIfAbsent(state.getBlock(), it -> {
                        return ForgeRegistries.BLOCKS.getValue(
                                new ResourceLocation(blockId.getNamespace(),
                                        blockId.getPath()
                                               .replace("exposed_", "")
                                               .replace("weathered_", "exposed_")
                                               .replace("oxidized_", "weathered_")
                        ));
                    });
                }
            } else {
                if (blockId.getPath().contains("waxed")) {
                    returnBlock = WAX_REMOVAL_MAP.computeIfAbsent(state.getBlock(), it -> {
                        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId.getNamespace(), blockId.getPath().replace("waxed_", "")));
                    });
                }

            }
            if (returnBlock != null) {
                final BlockState[] returnState = {returnBlock.defaultBlockState()};
                state.getProperties().forEach(prop -> {
                    returnState[0] = returnState[0].setValue((Property) prop, state.getValue(prop));
                });
                return returnState[0];
            }
        }

        return null;
    }
}
