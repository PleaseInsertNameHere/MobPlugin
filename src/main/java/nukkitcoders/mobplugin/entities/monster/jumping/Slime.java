package nukkitcoders.mobplugin.entities.monster.jumping;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.CreatureSpawnEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.monster.JumpingMonster;
import nukkitcoders.mobplugin.entities.monster.walking.IronGolem;
import nukkitcoders.mobplugin.entities.monster.walking.SnowGolem;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.HashMap;

public class Slime extends JumpingMonster {

    public static final int NETWORK_ID = 37;

    public static final int SIZE_SMALL = 1;
    public static final int SIZE_MEDIUM = 2;
    public static final int SIZE_BIG = 3;

    protected int size;

    public Slime(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getHeight() {
        if (size == SIZE_BIG) {
            return 2.08f;
        } else if (size == SIZE_MEDIUM) {
            return 1.04f;
        } else if (size == SIZE_SMALL) {
            return 0.52f;
        }
        return 0.52f;
    }

    @Override
    public float getWidth() {
        if (size == SIZE_BIG) {
            return 2.08f;
        } else if (size == SIZE_MEDIUM) {
            return 1.04f;
        } else if (size == SIZE_SMALL) {
            return 0.52f;
        }
        return 0.52f;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        if (this.namedTag.contains("Size")) {
            this.size = this.namedTag.getInt("Size");
        } else {
            this.size = Utils.rand(1, 3);
        }

        if (size == SIZE_BIG) {
            this.setScale(this.getHeight());
            this.recalculateBoundingBox();
            this.setMaxHealth(16);
            this.setDamage(new float[]{0, 3, 4, 6});
        } else if (size == SIZE_MEDIUM) {
            this.setScale(this.getHeight());
            this.recalculateBoundingBox();
            this.setMaxHealth(4);
            this.setDamage(new float[]{0, 2, 2, 3});
        } else {
            this.setScale(this.getHeight());
            this.setMaxHealth(1);
            this.setDamage(new float[]{0, 0, 0, 0});
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putInt("Size", this.size);
    }

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
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public Item[] getDrops() {
        if (this.size == SIZE_BIG) {
            CreatureSpawnEvent ev = new CreatureSpawnEvent(NETWORK_ID, this, this.namedTag, CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
            level.getServer().getPluginManager().callEvent(ev);

            if (ev.isCancelled()) {
                return new Item[]{};
            }

            for (int i = 1; i <= Utils.rand(2, 4); i++) {
                Slime entity = new Slime(this.getChunk(), Entity.getDefaultNBT(this).putInt("Size", SIZE_MEDIUM));
                entity.spawnToAll();
            }

            return new Item[]{};
        } else if (this.size == SIZE_MEDIUM) {
            CreatureSpawnEvent ev = new CreatureSpawnEvent(NETWORK_ID, this, this.namedTag, CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
            level.getServer().getPluginManager().callEvent(ev);

            if (ev.isCancelled()) {
                return new Item[]{};
            }

            for (int i = 1; i <= Utils.rand(2, 4); i++) {
                Slime entity = new Slime(this.getChunk(), Entity.getDefaultNBT(this).putInt("Size", SIZE_SMALL));
                entity.spawnToAll();
            }

            return new Item[]{};
        } else {
            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                return new Item[]{Item.get(Item.SLIMEBALL, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 2))};
            } else {
                return new Item[]{Item.get(Item.SLIMEBALL, 0, Utils.rand(0, 2))};
            }
        }
    }

    @Override
    public int getKillExperience() {
        if (this.size == SIZE_BIG) return 4;
        if (this.size == SIZE_MEDIUM) return 2;
        if (this.size == SIZE_SMALL) return 1;
        return 0;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (followTarget == null || followTarget.isClosed()) {
            for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(16, 16, 16), this)) {
                if (entity instanceof SnowGolem || entity instanceof IronGolem) {
                    setTarget(entity);
                    break;
                }
            }
        }
        return super.entityBaseTick(tickDiff);
    }
}
