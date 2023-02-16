package ellemes.expandedstorage.common.inventory;

public interface InventorySlotFunction<T, U> {
    U apply(T inventory, int slot);
}
