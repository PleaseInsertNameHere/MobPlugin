package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntitySmite;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.potion.Effect;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.route.WalkerRouteFinder;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Husk extends WalkingMonster implements EntityAgeable, EntitySmite {

    public static final int NETWORK_ID = 47;
    Item item;

    public Husk(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.route = new WalkerRouteFinder(this);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return this.isBaby() ? 0.31875f : 0.6375f;
    }

    @Override
    public float getHeight() {
        return this.isBaby() ? 1.009375f : 2.01875f;
    }

    @Override
    public double getSpeed() {
        return 1.1;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setDamage(new float[]{0, 2.5f, 3, 4.5f});
        this.setMaxHealth(20);
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && player.distanceSquared(this) <= 1) {
            this.attackDelay = 0;
            player.attack(new EntityDamageByEntityEvent(this, player, DamageCause.ENTITY_ATTACK, getDamage()));
            EntityEventPacket pk = new EntityEventPacket();
            pk.eid = this.getId();
            pk.event = 4;
            Server.broadcastPacket(this.getViewers().values(), pk);
            if (player.hasEffect(Effect.HUNGER) && player.getEffect(Effect.HUNGER).getAmplifier() == 0) {
                player.addEffect(Effect.getEffect(Effect.HUNGER).setDuration(20 * 30));
            }
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            drops.add(Item.get(Item.ROTTEN_FLESH, 0, Utils.rand(0, 2)));
            if (Utils.rand(1, 20) == 1) {
                Item[] droppingItem = new Item[]{Item.get(Item.IRON_INGOT), Item.get(Item.CARROT), Item.get(Item.POTATO)};
                drops.add(droppingItem[Utils.rand(0, droppingItem.length - 1)]);
            }
        }
        if (item != null && !item.isNull()) {
            if (Utils.rand(1, 200) <= 17) {
                drops.add(item);
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        int xp = 0;
        xp += this.isBaby() ? 12 : 5;
        xp += item == null ? 0 : Utils.rand(1, 3);
        return xp;
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);

        MobEquipmentPacket pk = new MobEquipmentPacket();
        pk.eid = this.getId();
        pk.item = setRandomItem();
        pk.hotbarSlot = 10;
        player.dataPacket(pk);
    }

    private Item setRandomItem() {
        int rnd = Utils.rand(1, 2);
        return item = rnd == 1 ? Item.get(Item.IRON_SHOVEL, Utils.rand(1, ItemTool.DURABILITY_IRON), 1) : Item.get(Item.IRON_SWORD, Utils.rand(1, ItemTool.DURABILITY_IRON), 1);
    }

    @Override
    public boolean attack(float damage) {
        return super.attack(damage);
    }
}
