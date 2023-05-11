package ellemes.expandedstorage.thread.compat.inventory_tabs;

import com.kqp.inventorytabs.init.InventoryTabs;
import com.kqp.inventorytabs.tabs.provider.BlockTabProvider;
import com.kqp.inventorytabs.tabs.render.TabRenderInfo;
import com.kqp.inventorytabs.tabs.tab.SimpleBlockTab;
import com.kqp.inventorytabs.util.BlockUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import ellemes.expandedstorage.common.block.OpenableBlock;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class ExpandedBlockTab<T extends OpenableBlock> extends SimpleBlockTab {
    final T block;
    private ItemStack itemFrameStackOrDefault;
    private final ItemStack defaultStack;
    private Component blockCustomNameOrDefault;
    private final Component defaultHoverText;

    public ExpandedBlockTab(T block, BlockPos selfPos) {
        super(block.getBlockId(), selfPos);
        this.block = block;
        itemFrameStackOrDefault = defaultStack = new ItemStack(block);
        blockCustomNameOrDefault = defaultHoverText = Utils.translation(block.getDescriptionId());
    }

    protected boolean singleShouldBeRemoved() {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        BlockState state = level.getBlockState(blockPos);

        if (state.getBlock() != block) {
            return true;
        }

        if (InventoryTabs.getConfig().doSightChecksFlag) {
            if (BlockUtil.getLineOfSight(blockPos, player, 5D) == null) {
                return true;
            } else {
                return !BlockUtil.inRange(blockPos, player, 5D);
            }
        }
        Vec3 playerHead = player.position().add(0D, player.getEyeHeight(player.getPose()), 0D);

        return Vec3.atCenterOf(blockPos).subtract(playerHead).lengthSqr() > BlockTabProvider.SEARCH_DISTANCE * BlockTabProvider.SEARCH_DISTANCE;
    }

    @Override
    public boolean shouldBeRemoved() {
        return singleShouldBeRemoved();
    }

    @Override
    public Component getHoverText() {
        if (itemFrameStackOrDefault.hasCustomHoverName()) {
            return itemFrameStackOrDefault.getHoverName();
        }
        return blockCustomNameOrDefault;
    }

    @Override
    public void renderTabIcon(PoseStack stack, TabRenderInfo tabRenderInfo, AbstractContainerScreen<?> screen) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Font font = Minecraft.getInstance().font;
        itemRenderer.blitOffset = 100.0F;
        itemRenderer.renderAndDecorateItem(itemFrameStackOrDefault, tabRenderInfo.itemX, tabRenderInfo.itemY);
        itemRenderer.renderGuiItemDecorations(font, itemFrameStackOrDefault, tabRenderInfo.itemX, tabRenderInfo.itemY);
        itemRenderer.blitOffset = 0.0F;
    }

    public void update() {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof Nameable nameable) {
            blockCustomNameOrDefault = nameable.getDisplayName();
        } else {
            blockCustomNameOrDefault = defaultHoverText;
        }
        AABB box = this.getBlockBounds().inflate(1.0d / 16.0d);
        List<ItemFrame> frames = level.getEntitiesOfClass(ItemFrame.class, box);
        for (ItemFrame frame : frames) {
            BlockPos pos = frame.getPos().relative(frame.getDirection().getOpposite());
            if (this.isOwnedPos(pos)) {
                ItemStack stack = frame.getItem();
                if (stack.isEmpty()) {
                    continue;
                }
                itemFrameStackOrDefault = stack;
                return;
            }
        }
        itemFrameStackOrDefault = defaultStack;
    }

    protected AABB getBlockBounds() {
        return new AABB(blockPos);
    }

    protected boolean isOwnedPos(BlockPos pos) {
        return blockPos.equals(pos);
    }
}
