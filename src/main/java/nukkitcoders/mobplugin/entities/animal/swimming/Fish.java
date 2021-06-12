package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.SwimmingAnimal;
import nukkitcoders.mobplugin.entities.monster.flying.EnderDragon;
import nukkitcoders.mobplugin.utils.Utils;

public abstract class Fish extends SwimmingAnimal {

    public Fish(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 3);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (item.getId() == Item.BUCKET && item.getDamage() == 0 && this.isInsideOfWater()) {
            this.close();
            if (item.getCount() <= 1) {
                player.getInventory().setItemInHand(Item.get(Item.BUCKET, this.getBucketMeta(), 1));
                return false;
            } else {
                if (!player.isCreative()) {
                    player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                }
                player.getInventory().addItem(Item.get(Item.BUCKET, this.getBucketMeta(), 1));
                return true;
            }
        } if (item.getId() == Item.NAME_TAG) {
            if (item.hasCustomName()) {
                this.setNameTag(item.getCustomName());
                this.setNameTagVisible(true);

                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                return true;
            }
        }
        return false;
    }

    abstract int getBucketMeta();
}
