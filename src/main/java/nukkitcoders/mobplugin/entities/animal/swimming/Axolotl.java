package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.utils.DyeColor;
import nukkitcoders.mobplugin.utils.Utils;

public class Axolotl extends Fish {

    public static final int NETWORK_ID = 130;

    public int color = 0;

    public Axolotl(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.3f;
    }

    @Override
    int getBucketMeta() {
        return 12;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(14);

        if(!this.namedTag.containsInt("Variant")) {
            randomColor();
        } else {
            setColor(this.namedTag.getInt("Variant"));
        }
    }

    private int randomColor() {
        setColor(Utils.random.nextInt(5));

        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
        this.namedTag.putInt("Variant", color);
        this.setDataProperty(new IntEntityData(DATA_VARIANT, color));
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && (player.getInventory().getItemInHand().getId() == Item.RAW_FISH || player.getInventory().getItemInHand().getId() == Item.RAW_SALMON) && distance <= 40;
        }
        return false;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        boolean att =  super.attack(source);
        if (source.isCancelled()) {
            return att;
        }

        EntityEventPacket pk0 = new EntityEventPacket();
        pk0.eid = this.getId();
        pk0.event = EntityEventPacket.TAME_SUCCESS;

        this.level.addChunkPacket(this.getChunkX() >> 4,this.getChunkZ() >> 4,pk0);
        this.level.addSound(this.getLocation(), Sound.RAID_HORN);
        return att;
    }

    public int getColor() {
        return namedTag.getInt("Variant");
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 7);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }
}
