package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.EntitySmite;
import cn.nukkit.entity.passive.EntityVillager;
import cn.nukkit.event.entity.CreatureSpawnEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.potion.Effect;
import nukkitcoders.mobplugin.MobPlugin;
import nukkitcoders.mobplugin.entities.animal.walking.Villager;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.route.WalkerRouteFinder;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ZombieVillager extends WalkingMonster implements EntitySmite {

    public static final int NETWORK_ID = 44;
    private boolean isHealing = false;

    public ZombieVillager(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.route = new WalkerRouteFinder(this);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return this.isBaby() ? 0.3f : 0.6f;
    }

    @Override
    public float getHeight() {
        return this.isBaby() ? 0.95f : 1.9f;
    }

    @Override
    public double getSpeed() {
        return 1.1;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setDamage(new float[]{0, 2.5f, 3, 4.5f});
        this.setMaxHealth(20);
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && this.distanceSquared(player) < 1) {
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
            pk.event = 4;
            Server.broadcastPacket(this.getViewers().values(), pk);
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (getServer().getDifficulty() == 0) {
            this.close();
            return true;
        }

        boolean hasUpdate = super.entityBaseTick(tickDiff);

        if (MobPlugin.shouldMobBurn(level, this)) {
            this.setOnFire(100);
        }

        if (followTarget == null || followTarget.isClosed()) {
            for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(16, 16, 16), this)) {
                if (entity instanceof Villager) {
                    setFollowTarget(entity, true);
                    setTarget(entity);
                    break;
                }
            }
        }

        return hasUpdate;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        for (Item item : super.getDrops()) {
            drops.add(item);
        }

        if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
            drops.add(Item.get(Item.ROTTEN_FLESH, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 2)));
        } else {
            drops.add(Item.get(Item.ROTTEN_FLESH, 0, Utils.rand(0, 2)));
        }

        if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
            if (Utils.rand(1, 1000) <= 25 + ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() * 10) {
                Item[] droppingItem = new Item[]{Item.get(Item.IRON_INGOT), Item.get(Item.CARROT), Item.get(Item.POTATO)};
                drops.add(droppingItem[Utils.rand(0, droppingItem.length - 1)]);
            }
        } else {
            if (Utils.rand(1, 40) == 1) {
                Item[] droppingItem = new Item[]{Item.get(Item.IRON_INGOT), Item.get(Item.CARROT), Item.get(Item.POTATO)};
                drops.add(droppingItem[Utils.rand(0, droppingItem.length - 1)]);
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 12 : 5;
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Zombie Villager";
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (item.getId() == Item.GOLDEN_APPLE && this.hasEffect(Effect.WEAKNESS) && !this.isHealing) {
            EntityEventPacket pk = new EntityEventPacket();
            pk.eid = this.getId();
            pk.event = 16;
            Server.broadcastPacket(this.getViewers().values(), pk);
            this.isHealing = true;
            this.getEffects().clear();
            int ticks = Utils.rand(20 * 60 * 3, 20 * 60 * 5);
            switch (this.getServer().getDifficulty()) {
                case 0:
                    this.addEffect(Effect.getEffect(Effect.STRENGTH).setVisible(true).setDuration(ticks).setAmplifier(1));
                    break;

                case 1:
                    this.addEffect(Effect.getEffect(Effect.STRENGTH).setVisible(true).setDuration(ticks).setAmplifier(2));
                    break;

                case 2:
                    this.addEffect(Effect.getEffect(Effect.STRENGTH).setVisible(true).setDuration(ticks).setAmplifier(3));
                    break;
            }

            Server.getInstance().getScheduler().scheduleDelayedTask(MobPlugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    Entity ent = Entity.createEntity("Villager", ZombieVillager.this);
                    if (ent != null) {
                        CreatureSpawnEvent cse = new CreatureSpawnEvent(EntityVillager.NETWORK_ID, ZombieVillager.this, ent.namedTag, CreatureSpawnEvent.SpawnReason.CURED);
                        ZombieVillager.this.getServer().getPluginManager().callEvent(cse);

                        if (cse.isCancelled()) {
                            ent.close();
                            return;
                        }

                        ent.yaw = ZombieVillager.this.yaw;
                        ent.pitch = ZombieVillager.this.pitch;
                        ent.setImmobile(ZombieVillager.this.isImmobile());
                        if (ZombieVillager.this.hasCustomName()) {
                            ent.setNameTag(ZombieVillager.this.getNameTag());
                            ent.setNameTagVisible(ZombieVillager.this.isNameTagVisible());
                            ent.setNameTagAlwaysVisible(ZombieVillager.this.isNameTagAlwaysVisible());
                        }

                        if (!ZombieVillager.this.isClosed()) {
                            ZombieVillager.this.close();
                            ZombieVillager.this.isHealing = false;
                            ent.spawnToAll();
                        }
                    }
                }
            }, ticks);
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return !player.closed && player.spawned && player.isAlive() && (player.isSurvival() || player.isAdventure()) && distance <= 100;
        }
        return creature.isAlive() && !creature.closed && distance <= 100;
    }
}
