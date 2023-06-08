package compasses.expandedstorage.forge.block.misc;

import compasses.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import compasses.expandedstorage.common.block.strategies.ItemAccess;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class GenericItemAccess implements ItemAccess {
    private final OpenableBlockEntity entity;
    private IItemHandlerModifiable handler = null;

    public GenericItemAccess(OpenableBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public Object get() {
        if (handler == null) {
            handler = new InvWrapper(entity.getInventory());
        }
        return handler;
    }
}
