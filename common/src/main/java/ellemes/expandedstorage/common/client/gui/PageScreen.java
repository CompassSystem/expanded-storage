package ellemes.expandedstorage.common.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import ellemes.expandedstorage.api.client.function.ScreenSize;
import ellemes.expandedstorage.api.client.gui.AbstractScreen;
import ellemes.expandedstorage.api.client.gui.TexturedRect;
import ellemes.expandedstorage.api.inventory.AbstractHandler;
import ellemes.expandedstorage.common.CommonClient;
import ellemes.expandedstorage.common.client.gui.widget.PageButton;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.anti_ad.mc.ipn.api.IPNButton;
import org.anti_ad.mc.ipn.api.IPNGuiHint;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@IPNGuiHint(button = IPNButton.MOVE_TO_CONTAINER, horizontalOffset = 58)
public final class PageScreen extends AbstractScreen {
    private final ResourceLocation textureLocation;
    private final int textureWidth, textureHeight;
    private final Set<TexturedRect> blankArea = new LinkedHashSet<>();
    private final int blankSlots, pages;
    private PageButton leftPageButton, rightPageButton;
    private int page;
    private MutableComponent currentPageText;
    private float pageTextX;

    public PageScreen(AbstractHandler handler, Inventory playerInventory, Component title, ScreenSize screenSize) {
        super(handler, playerInventory, title, screenSize);

        this.initializeSlots(playerInventory);

        textureLocation = Utils.id("textures/gui/container/shared_" + inventoryWidth + "_" + inventoryHeight + ".png");
        textureWidth = switch (inventoryWidth) {
            case 9 -> inventoryHeight == 3 ? 176 : 208;
            case 12 -> 256;
            case 15 -> 320;
            case 18 -> 368;
            default -> throw new IllegalStateException("Unexpected value: " + inventoryWidth);
        };
        textureHeight = switch (inventoryHeight) {
            case 3 -> 192;
            case 6 -> 240;
            case 9 -> 304;
            default -> throw new IllegalStateException("Unexpected value: " + inventoryHeight);
        };

        int slotsPerPage = inventoryWidth * inventoryHeight;
        pages = Mth.ceil((double) totalSlots / slotsPerPage);
        int lastPageSlots = totalSlots - (pages - 1) * slotsPerPage;
        blankSlots = slotsPerPage - lastPageSlots;

        imageWidth = Utils.CONTAINER_PADDING_LDR + Utils.SLOT_SIZE * inventoryWidth + Utils.CONTAINER_PADDING_LDR;
        imageHeight = Utils.CONTAINER_HEADER_HEIGHT + Utils.SLOT_SIZE * inventoryHeight + 14 + Utils.SLOT_SIZE * 3 + 4 + Utils.SLOT_SIZE + Utils.CONTAINER_PADDING_LDR;
    }

    private static boolean regionIntersects(AbstractWidget widget, int x, int y, int width, int height) {
        return widget.getX() <= x + width && y <= widget.getY() + widget.getHeight() || x <= widget.getX() + widget.getWidth() && widget.getY() <= y + height;
    }

    public static ScreenSize retrieveScreenSize(int slots, int scaledWidth, int scaledHeight) {
        ArrayList<Pair<ScreenSize, ScreenSize>> options = new ArrayList<>();
        PageScreen.addEntry(options, slots, 9, 3);
        PageScreen.addEntry(options, slots, 9, 6);
        if (scaledHeight >= 276 && slots > 54) {
            PageScreen.addEntry(options, slots, 9, 9);
        }
        Pair<ScreenSize, ScreenSize> picked = null;
        for (Pair<ScreenSize, ScreenSize> option : options) {
            if (picked == null) {
                picked = option;
            } else {
                ScreenSize pickedMeta = picked.getSecond();
                ScreenSize iterMeta = option.getSecond();
                ScreenSize iterDim = option.getFirst();
                if (pickedMeta.getHeight() == iterMeta.getHeight() && iterMeta.getWidth() < pickedMeta.getWidth()) {
                    picked = option;
                } else if (CommonClient.platformHelper().configWrapper().preferSmallerScreens() && pickedMeta.getWidth() == iterMeta.getWidth() + 1 && iterMeta.getHeight() <= iterDim.getWidth() * iterDim.getHeight() / 2.0) {

                } else if (iterMeta.getWidth() < pickedMeta.getWidth() && iterMeta.getHeight() <= iterDim.getWidth() * iterDim.getHeight() / 2.0) {
                    picked = option;
                }
            }
        }
        return picked.getFirst();
    }

    private static void addEntry(ArrayList<Pair<ScreenSize, ScreenSize>> options, int slots, int width, int height) {
        int pages = Mth.ceil((double) slots / (width * height));
        int blanked = slots - pages * width * height;
        options.add(new Pair<>(ScreenSize.of(width, height), ScreenSize.of(pages, blanked)));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(textureLocation, leftPos, topPos, 0, 0, imageWidth, imageHeight, textureWidth, textureHeight);
        blankArea.forEach(image -> image.render(graphics));
    }

    private void initializeSlots(Inventory playerInventory) {
        menu.resetSlotPositions(true, inventoryWidth, inventoryHeight);
        int playerInvLeft = (inventoryWidth * Utils.SLOT_SIZE + 14) / 2 - 80;
        int playerInvTop = Utils.SLOT_SIZE + 14 + (inventoryHeight * Utils.SLOT_SIZE);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                menu.addClientSlot(new Slot(playerInventory, y * 9 + x + 9, playerInvLeft + Utils.SLOT_SIZE * x, playerInvTop + y * Utils.SLOT_SIZE));
            }
        }
        for (int x = 0; x < 9; x++) {
            menu.addClientSlot(new Slot(playerInventory, x, playerInvLeft + Utils.SLOT_SIZE * x, playerInvTop + 58));
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        if (inventoryWidth > 9 && mouseY >= top + Utils.CONTAINER_HEADER_HEIGHT + inventoryHeight * Utils.SLOT_SIZE + Utils.CONTAINER_HEADER_HEIGHT) {
            int outsideRegion = (imageWidth - (Utils.CONTAINER_PADDING_LDR + 9 * Utils.SLOT_SIZE + Utils.CONTAINER_PADDING_LDR)) / 2;
            if (mouseX < left + outsideRegion || mouseX > left + imageWidth - outsideRegion) {
                return true;
            }
        }
        return super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    private void setPage(int oldPage, int newPage) {
        if (newPage == 0 || newPage > pages) {
            return;
        }
        page = newPage;
        if (newPage > oldPage) {
            if (page == pages) {
                rightPageButton.setActive(false);
                // todo: calculate blankArea once & add boolean field
                if (blankSlots > 0) {
                    int rows = Math.floorDiv(blankSlots, inventoryWidth);
                    int remainder = (blankSlots - inventoryWidth * rows);
                    int yTop = topPos + Utils.CONTAINER_HEADER_HEIGHT + (inventoryHeight - 1) * Utils.SLOT_SIZE;
                    int xLeft = leftPos + Utils.CONTAINER_PADDING_LDR;
                    for (int i = 0; i < rows; i++) {
                        blankArea.add(new TexturedRect(textureLocation, xLeft, yTop, inventoryWidth * Utils.SLOT_SIZE, Utils.SLOT_SIZE,
                                Utils.CONTAINER_PADDING_LDR, imageHeight, textureWidth, textureHeight));
                        yTop -= Utils.SLOT_SIZE;
                    }
                    if (remainder > 0) {
                        int xRight = leftPos + Utils.CONTAINER_PADDING_LDR + inventoryWidth * Utils.SLOT_SIZE;
                        int width = remainder * Utils.SLOT_SIZE;
                        blankArea.add(new TexturedRect(textureLocation, xRight - width, yTop, width, Utils.SLOT_SIZE,
                                Utils.CONTAINER_PADDING_LDR, imageHeight, textureWidth, textureHeight));
                    }
                }
            }
            if (!leftPageButton.active) {
                leftPageButton.setActive(true);
            }
        } else if (newPage < oldPage) {
            if (page == 1) {
                leftPageButton.setActive(false);
            }
            blankArea.clear();
            if (!rightPageButton.active) {
                rightPageButton.setActive(true);
            }
        }
        int slotsPerPage = inventoryWidth * inventoryHeight;
        int oldMin = slotsPerPage * (oldPage - 1);
        int oldMax = Math.min(oldMin + slotsPerPage, totalSlots);
        menu.moveSlotRange(oldMin, oldMax, -2000);
        int newMin = slotsPerPage * (newPage - 1);
        int newMax = Math.min(newMin + slotsPerPage, totalSlots);
        menu.moveSlotRange(newMin, newMax, 2000);
        this.setPageText();
    }

    private void setPageText() {
        currentPageText = Component.translatable("screen.ellemes_container_lib.page_x_y", page, pages);
        pageTextX = (leftPageButton.getX() + leftPageButton.getWidth() + rightPageButton.getX()) / 2.0f - font.width(currentPageText) / 2.0f + 0.5f;
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        int currentPage = page;
        if (currentPage != 1) {
            menu.resetSlotPositions(false, inventoryWidth, inventoryHeight);
            super.resize(client, width, height);
            blankArea.clear();
            this.setPage(1, currentPage);
            return;
        }
        super.resize(client, width, height);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 0x404040, false);
        if (currentPageText != null) {
            // todo: remove cast if possible
            graphics.drawString(font, currentPageText.getVisualOrderText(), (int) (pageTextX - leftPos), imageHeight - 94, 0x404040, false);
        }
    }

    @Override
    protected boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.setPage(page, Screen.hasShiftDown() ? pages : page + 1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            this.setPage(page, Screen.hasShiftDown() ? 1 : page - 1);
            return true;
        }
        return false;
    }

    public void addPageButtons() {
        int width = 54;
        int x = this.leftPos + imageWidth - 61;
        int originalX = x;
        int y = this.topPos + imageHeight - 96;
        List<AbstractWidget> renderableChildren = new ArrayList<>();
        for (var child : this.children()) {
            if (child instanceof AbstractWidget widget) {
                renderableChildren.add(widget);
            }
        }
        renderableChildren.sort(Comparator.comparingInt(a -> -a.getX()));
        for (AbstractWidget widget : renderableChildren) {
            if (PageScreen.regionIntersects(widget, x, y, width, 12)) {
                x = widget.getX() - width - 2;
            }
        }
        page = 1;
        // Honestly this is dumb.
        if (x == originalX && CommonClient.platformHelper().isModLoaded("inventoryprofiles")) {
            x -= 14;
        }
        leftPageButton = new PageButton(x, y, 0,
                Component.translatable("screen.ellemes_container_lib.prev_page"), button -> this.setPage(page, page - 1));
        leftPageButton.active = false;
        this.addRenderableWidget(leftPageButton);
        rightPageButton = new PageButton(x + 42, y, 1,
                Component.translatable("screen.ellemes_container_lib.next_page"), button -> this.setPage(page, page + 1));
        this.addRenderableWidget(rightPageButton);
        this.setPageText();
    }
}
