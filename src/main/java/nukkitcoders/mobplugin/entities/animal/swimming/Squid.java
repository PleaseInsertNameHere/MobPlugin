package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDye;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.utils.DyeColor;
import nukkitcoders.mobplugin.entities.animal.SwimmingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Squid extends SwimmingAnimal {

    public static final int NETWORK_ID = 17;

    public Squid(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.475f;
        }
        return 0.95f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.475f;
        }
        return 0.95f;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(10);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        if (!this.isBaby()) {
            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                drops.add(new ItemDye(DyeColor.BLACK.getDyeData(), Utils.rand(1, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 3)));
            } else {
                drops.add(new ItemDye(DyeColor.BLACK.getDyeData(), Utils.rand(1, 3)));
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        if (!this.isBaby()) {
            return Utils.rand(1, 3);
        }
        return 0;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        boolean att = super.attack(source);
        if (source.isCancelled()) {
            return att;
        }

        EntityEventPacket pk0 = new EntityEventPacket();
        pk0.eid = this.getId();
        pk0.event = EntityEventPacket.SQUID_INK_CLOUD;

        this.level.addChunkPacket(this.getChunkX() >> 4, this.getChunkZ() >> 4, pk0);
        return att;
    }
}
