package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.entities.monster.flying.Wither;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PiglinBrute extends WalkingMonster {

    public static final int NETWORK_ID = 127;
    public Item itemhand;

    public PiglinBrute(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);

        this.setMaxHealth(50);
        this.setDamage(new float[]{0, 7.5f, 13.5f, 17});
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
    public int getKillExperience() {
        return 20;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && this.distanceSquared(player) < 1.44) {
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

    public Item getItemhand() {
        return itemhand;
    }

    public void setItemhand(Item itemhand) {
        this.itemhand = itemhand;
        this.spawnToAll();
    }

    @Override
    protected void initEntity() {
        this.setItemhand(Item.get(Item.GOLDEN_AXE));
        super.initEntity();
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        for (Item item : super.getDrops()) {
            drops.add(item);
        }

        if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
            if (Utils.rand(1, 200) <= 17 + ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() * 2) {
                drops.add(Item.get(Item.GOLDEN_AXE, Utils.rand(1, itemhand.getMaxDurability()), 1));
            }
        } else {
            if (Utils.rand(1, 200) <= 17) {
                drops.add(Item.get(Item.GOLDEN_AXE, Utils.rand(1, itemhand.getMaxDurability()), 1));
            }
        }
        return drops.toArray(new Item[0]);
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Piglin Brute";
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (followTarget == null || followTarget.isClosed()) {
            for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(16, 16, 16), this)) {
                if (entity instanceof WitherSkeleton || entity instanceof Wither) {
                    setFollowTarget(entity, true);
                    setTarget(entity);
                    break;
                }
            }
        }
        return super.entityBaseTick(tickDiff);
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);
        if (this.itemhand != null) {
            MobEquipmentPacket pk = new MobEquipmentPacket();
            pk.eid = this.getId();
            pk.hotbarSlot = 1;
            pk.item = this.itemhand;
            for (Player all : Server.getInstance().getOnlinePlayers().values())
                all.dataPacket(pk);
        }
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return !player.closed && player.spawned && player.isAlive() && (player.isSurvival() || player.isAdventure()) && distance <= 144;
        }
        return creature.isAlive() && !creature.closed && distance <= 144;
    }
}
