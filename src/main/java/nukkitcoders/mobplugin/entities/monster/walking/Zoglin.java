package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.EntitySmite;
import cn.nukkit.entity.item.EntityArmorStand;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.BaseEntity;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.entities.monster.flying.Ghast;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Zoglin extends WalkingMonster implements EntitySmite {

    public final static int NETWORK_ID = 126;

    public Zoglin(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 1 : Utils.rand(1, 3);
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(40);
        if (this.isBaby()) {
            this.setDamage(new float[]{0, 0.5f, 0.5f, 0.75f});
        } else {
            this.setDamage(new float[]{0, Utils.rand(3, 5), Utils.rand(3, 8), Utils.rand(4.5f, 12)});
        }
    }

    @Override
    public float getWidth() {
        return this.isBaby() ? 0.425f : 0.9f;
    }

    @Override
    public float getHeight() {
        return this.isBaby() ? 0.425f : 0.9f;
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 30 && player.distanceSquared(this) <= 1.5) {
            this.attackDelay = 0;
            HashMap<EntityDamageEvent.DamageModifier, Float> damage = new HashMap<>();
            damage.put(EntityDamageEvent.DamageModifier.BASE, this.getDamage());

            if (player instanceof Player) {
                HashMap<Integer, Float> armorValues = new ArmorPoints();

                float points = 0;
                for (Item i : ((Player) player).getInventory().getArmorContents()) {
                    points += armorValues.getOrDefault(i.getId(), 0f);
                }

                damage.put(EntityDamageEvent.DamageModifier.ARMOR, (float) (damage.getOrDefault(EntityDamageEvent.DamageModifier.ARMOR, 0f) - Math.floor(damage.getOrDefault(EntityDamageEvent.DamageModifier.BASE, 1f) * points * 0.04)));
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
        for (Item item : super.getDrops()) {
            drops.add(item);
        }

        if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
            drops.add(Item.get(Item.ROTTEN_FLESH, 0, Utils.rand(1, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 3)));
        } else {
            drops.add(Item.get(Item.ROTTEN_FLESH, 0, Utils.rand(1, 3)));
        }
        return drops.toArray(new Item[0]);
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {

        if (followTarget == null || followTarget.isClosed()) {
            for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(32, 32, 32), this)) {
                if ((entity instanceof BaseEntity || entity instanceof EntityArmorStand) && !(entity instanceof Creeper || entity instanceof Ghast || entity instanceof Zoglin)) {
                    if (!(entity instanceof EntityArmorStand))
                        setFollowTarget(entity, true);
                    setTarget(entity);
                    break;
                }
            }
        }
        return super.entityBaseTick(tickDiff);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {

        if (creature instanceof Player) {
            return super.targetOption(creature, distance);
        }
        return creature.isAlive() && !creature.closed && distance <= 144;
    }
}
