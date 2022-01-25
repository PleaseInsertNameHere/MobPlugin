package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.MinecraftItemID;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Hoglin extends WalkingMonster {

    public final static int NETWORK_ID = 124;
    protected int inLoveTicks = 0;

    public Hoglin(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 3);
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(40);
        if (this.isBaby()) {
            this.setDamage(new float[]{0, 0.5f, 0.5f, 0.75f});
        } else {
            this.setDamage(new float[]{0f, 5f, 8f, 12f});
            this.setMinDamage(new float[]{0f, 2.5f, 3f, 4.5f});
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
    public double getSpeed() {
        return 1.4f;
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 30 && player.distanceSquared(this) <= 1.9) {
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
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (item.getId() == MinecraftItemID.CRIMSON_FUNGUS.get(1).getId() && !this.isBaby()) {
            player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), item));
            setInLove();
            return true;
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        boolean hasUpdate = super.entityBaseTick(tickDiff);

        if (this.isInLove()) {
            this.inLoveTicks -= tickDiff;
            if (this.age % 20 == 0) {
                for (int i = 0; i < 3; i++) {
                    this.level.addParticle(new HeartParticle(this.add(Utils.rand(-1.0, 1.0), this.getMountedYOffset() + Utils.rand(-1.0, 1.0), Utils.rand(-1.0, 1.0))));
                }
                for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(10, 5, 10), this)) {
                    if (!entity.isClosed() && this.getClass().isInstance(entity)) {
                        Hoglin hoglin = (Hoglin) entity;
                        if (hoglin.isInLove()) {
                            this.inLoveTicks = 0;
                            hoglin.inLoveTicks = 0;
                            this.spawnBaby();
                            break;
                        }
                    }
                }
            }
        }

        return hasUpdate;
    }

    protected void spawnBaby() {
        try {
            Hoglin hoglin = this.getClass().getConstructor(FullChunk.class, CompoundTag.class).newInstance(this.getChunk(), Entity.getDefaultNBT(this));
            hoglin.setBaby(true);
            hoglin.spawnToAll();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void setInLove() {
        this.inLoveTicks = 600;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_INLOVE);
    }

    public boolean isInLove() {
        return inLoveTicks > 0;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        for (Item item : super.getDrops()) {
            drops.add(item);
        }

        if (this.isBaby()) {
            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                drops.add(Item.get(this.isOnFire() ? Item.COOKED_PORKCHOP : Item.RAW_PORKCHOP, 0, Utils.rand(2, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 4)));
            } else {
                drops.add(Item.get(this.isOnFire() ? Item.COOKED_PORKCHOP : Item.RAW_PORKCHOP, 0, Utils.rand(2, 4)));
            }
            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 1)));
            } else {
                drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, 1)));
            }
        }

        return drops.toArray(new Item[0]);
    }
}
