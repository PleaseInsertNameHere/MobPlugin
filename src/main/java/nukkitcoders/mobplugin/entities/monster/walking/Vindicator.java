package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.route.WalkerRouteFinder;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class Vindicator extends WalkingMonster {

    public static final int NETWORK_ID = 57;

    private boolean angry;

    public Vindicator(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.route = new WalkerRouteFinder(this);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.9f;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setDamage(new float[]{0, 12, 18, 21});
        this.setMaxHealth(24);
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && player.distanceSquared(this) <= 1) {
            this.attackDelay = 0;
            HashMap<EntityDamageEvent.DamageModifier, Float> damage = new HashMap<>();
            damage.put(EntityDamageEvent.DamageModifier.BASE, this.getDamage());

            if (player instanceof Player) {
                HashMap<Integer, Float> armorValues = new ArmorPoints();

                float points = 0;
                for (Item i : ((Player) player).getInventory().getArmorContents()) {
                    points += armorValues.getOrDefault(i.getId(), 0f);
                }

                damage.put(EntityDamageEvent.DamageModifier.ARMOR,
                        (float) (damage.getOrDefault(EntityDamageEvent.DamageModifier.ARMOR, 0f) - Math.floor(damage.getOrDefault(EntityDamageEvent.DamageModifier.BASE, 1f) * points * 0.04)));
            }
            player.attack(new EntityDamageByEntityEvent(this, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage));
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
            drops.add(Item.get(Item.EMERALD, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 1)));
        } else {
            drops.add(Item.get(Item.EMERALD, 0, Utils.rand(0, 1)));
        }
        if (this.getDataFlag(DATA_FLAGS, DATA_FLAG_IS_ILLAGER_CAPTAIN)) {
            Item illagerBanner = Item.get(Item.BANNER, 15);
            illagerBanner.setCompoundTag(Base64.getDecoder().decode("CgAAAwQAVHlwZQEAAAAA"));
            drops.add(illagerBanner);
        }
        if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
            if (Utils.rand(1, 200) <= 17 + ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() * 2) {
                drops.add(Item.get(Item.IRON_AXE, Utils.rand(1, Item.get(Item.IRON_AXE).getMaxDurability()), 1));
            }
        } else {
            if (Utils.rand(1, 200) <= 17) {
                drops.add(Item.get(Item.IRON_AXE, Utils.rand(1, Item.get(Item.IRON_AXE).getMaxDurability()), 1));
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return 5;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (getServer().getDifficulty() == 0) {
            this.close();
            return true;
        }

        if (this.getFollowTarget() != null) {
            if (!this.angry) {
                this.angry = true;
                this.setDataFlag(DATA_FLAGS, DATA_FLAG_ANGRY, true);
            }
            if (this.getDataPropertyLong(DATA_TARGET_EID) != this.getFollowTarget().getId()) {
                this.setDataProperty(new LongEntityData(DATA_TARGET_EID, this.getFollowTarget().getId()));
            }
        } else {
            if (this.angry) {
                this.angry = false;
                this.setDataFlag(DATA_FLAGS, DATA_FLAG_ANGRY, false);
            }
            if (this.getDataPropertyLong(DATA_TARGET_EID) != 0) {
                this.setDataProperty(new LongEntityData(DATA_TARGET_EID, 0));
            }
        }

        return super.entityBaseTick(tickDiff);
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);

        MobEquipmentPacket pk = new MobEquipmentPacket();
        pk.eid = this.getId();
        pk.item = Item.get(Item.IRON_AXE);
        pk.windowId = ContainerIds.INVENTORY;
        pk.inventorySlot = pk.hotbarSlot = 0;
        player.dataPacket(pk);
    }
}
