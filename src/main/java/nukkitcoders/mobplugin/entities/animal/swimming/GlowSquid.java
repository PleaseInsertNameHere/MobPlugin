package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.MinecraftItemID;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class GlowSquid extends Squid {

    public static final int NETWORK_ID = 129;

    public GlowSquid(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                drops.add(MinecraftItemID.GLOW_INK_SAC.get(Utils.rand(1, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 3)));
            } else {
                drops.add(MinecraftItemID.GLOW_INK_SAC.get(Utils.rand(1, 3)));
            }
        }
        return drops.toArray(new Item[0]);
    }
}
