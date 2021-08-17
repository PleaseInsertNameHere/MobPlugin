package nukkitcoders.mobplugin.utils;

import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;

public class PiglinInventory extends ContainerInventory {
    public PiglinInventory(InventoryHolder holder) {
        super(holder, InventoryType.CHEST);
    }

    @Override
    public int getSize() {
        return 8;
    }
}
