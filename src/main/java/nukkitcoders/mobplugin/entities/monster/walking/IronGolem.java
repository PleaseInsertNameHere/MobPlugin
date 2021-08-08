package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.passive.EntityWolf;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import nukkitcoders.mobplugin.entities.animal.walking.Llama;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.entities.monster.flying.*;
import nukkitcoders.mobplugin.entities.monster.jumping.MagmaCube;
import nukkitcoders.mobplugin.entities.monster.jumping.Slime;
import nukkitcoders.mobplugin.entities.monster.swimming.ElderGuardian;
import nukkitcoders.mobplugin.entities.monster.swimming.Guardian;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IronGolem extends WalkingMonster {

    public static final int NETWORK_ID = 20;

    public IronGolem(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setFriendly(true);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 1.4f;
    }

    @Override
    public float getHeight() {
        return 2.9f;
    }

    @Override
    public double getSpeed() {
        return 0.7;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(100);
        super.initEntity();

        this.setDamage(new float[]{0, 11.75f, 21.5f, 32.25f});
        this.setMinDamage(new float[]{0, 4.75f, 7.5f, 11.25f});
    }

    public void attackEntity(Entity player) {
        if (this.attackDelay > 40 && this.distanceSquared(player) < 4) {
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
            EntityEventPacket pk = new EntityEventPacket();
            pk.eid = this.getId();
            pk.event = EntityEventPacket.ARM_SWING;
            Server.broadcastPacket(this.getViewers().values(), pk);
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    public boolean targetOption(EntityCreature creature, double distance) {
        return (!(creature instanceof Player) || creature.getId() == this.isAngryTo && (((Player) creature).getGamemode() & 0x01) == Player.SURVIVAL) && !(creature instanceof EntityWolf) && creature.isAlive() && distance <= 100;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        drops.add(Item.get(Item.IRON_INGOT, 0, Utils.rand(3, 5)));
        drops.add(Item.get(Item.RED_FLOWER, 0, Utils.rand(0, 2)));

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return 0;
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Iron Golem";
    }

    @Override
    public boolean canDespawn() {
        return false;
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        if (ev.getCause() == EntityDamageEvent.DamageCause.FALL || ev.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            ev.setDamage(0f);
            ev.setCancelled();
        }
        if (super.attack(ev)) {
            if (ev instanceof EntityDamageByEntityEvent) {
                Entity entity = ((EntityDamageByEntityEvent) ev).getDamager();
                if (entity instanceof Player || entity instanceof Goat || entity instanceof Llama || entity instanceof SnowGolem || (entity instanceof Wolf && ((Wolf) entity).isFriendly())) {
                    if (followTarget == null ||followTarget.isClosed()) {
                        this.isAngryTo = entity.getId();
                        setFollowTarget(entity);
                        setTarget(entity);
                    }
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean canTarget(Entity entity) {
        return entity.getId() == this.isAngryTo;
    @Override  
    public boolean entityBaseTick(int tickDiff) {
        if (followTarget == null || followTarget.isClosed()) {
            for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(32, 32, 32), this)) {
                if (entity instanceof Blaze || entity instanceof Drowned || entity instanceof EnderDragon || entity instanceof Enderman || entity instanceof Endermite || entity instanceof Guardian || entity instanceof ElderGuardian || entity instanceof Hoglin || entity instanceof MagmaCube || entity instanceof Phantom || entity instanceof Piglin || entity instanceof PiglinBrute || entity instanceof Shulker || entity instanceof Silverfish || entity instanceof Skeleton || entity instanceof WitherSkeleton || entity instanceof Stray || entity instanceof Slime || entity instanceof Spider || entity instanceof Vex || entity instanceof Evoker || entity instanceof Pillager || entity instanceof Ravager || entity instanceof Vindicator || entity instanceof Witch || entity instanceof Wither || entity instanceof Zombie || entity instanceof Husk || entity instanceof ZombieVillager || entity instanceof ZombiePigman || entity instanceof Zoglin) {
                    setTarget(entity);
                    break;
                }
            }
        }
        return super.entityBaseTick(tickDiff);
    }
}
