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
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MagmaCube extends JumpingMonster {

    public static final int NETWORK_ID = 42;

    public static final int SIZE_SMALL = 1;
    public static final int SIZE_MEDIUM = 2;
    public static final int SIZE_BIG = 3;

    protected int size;

    public MagmaCube(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (size == SIZE_BIG) {
            return 2.08f;
        } else if (size == SIZE_MEDIUM) {
            return 0.78f;
        } else if (size == SIZE_SMALL) {
            return 0.52f;
        }
        return 0.52f;
    }

    @Override
    public float getHeight() {
        if (size == SIZE_BIG) {
            return 2.08f;
        } else if (size == SIZE_MEDIUM) {
            return 0.78f;
        } else if (size == SIZE_SMALL) {
            return 0.52f;
        }
        return 0.52f;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.fireProof = true;
        if (this.namedTag.contains("Size")) {
            this.size = this.namedTag.getInt("Size");
        } else {
            this.size = Utils.rand(1, 3);
        }

        if (size == SIZE_BIG) {
            this.setScale(this.getHeight());
            this.setMaxHealth(16);
        } else if (size == SIZE_MEDIUM) {
            this.setScale(this.getHeight());
            this.setMaxHealth(4);
        } else if (size == SIZE_SMALL) {
            this.setScale(this.getHeight());
            this.setMaxHealth(1);
        }

        if (size == SIZE_BIG) {
            this.setDamage(new float[]{0, 4, 6, 9});
        } else if (size == SIZE_MEDIUM) {
            this.setDamage(new float[]{0, 3, 4, 6});
        } else {
            this.setDamage(new float[]{0, 2.5f, 3, 4.5f});
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
        List<Item> drops = new ArrayList<>();
        for (Item item : super.getDrops()) {
            drops.add(item);
        }
        if (this.size == SIZE_BIG) {
            CreatureSpawnEvent ev = new CreatureSpawnEvent(NETWORK_ID, this, this.namedTag, CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
            level.getServer().getPluginManager().callEvent(ev);

            if (ev.isCancelled()) {
                if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                    drops.add(Item.get(Item.MAGMA_CREAM, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 1)));
                } else {
                    drops.add(Item.get(Item.MAGMA_CREAM, 0, Utils.rand(0, 1)));
                }
            }

            for (int i = 1; i <= Utils.rand(2, 4); i++) {
                MagmaCube entity = new MagmaCube(this.getChunk(), Entity.getDefaultNBT(this).putInt("Size", SIZE_MEDIUM));
                entity.spawnToAll();
            }

            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                drops.add(Item.get(Item.MAGMA_CREAM, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 1)));
            } else {
                drops.add(Item.get(Item.MAGMA_CREAM, 0, Utils.rand(0, 1)));
            }
        } else if (this.size == SIZE_MEDIUM) {
            CreatureSpawnEvent ev = new CreatureSpawnEvent(NETWORK_ID, this, this.namedTag, CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
            level.getServer().getPluginManager().callEvent(ev);

            if (ev.isCancelled()) {
                if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                    drops.add(Item.get(Item.MAGMA_CREAM, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 1)));
                } else {
                    drops.add(Item.get(Item.MAGMA_CREAM, 0, Utils.rand(0, 1)));
                }
            }


            for (int i = 1; i <= Utils.rand(2, 4); i++) {
                MagmaCube entity = new MagmaCube(this.getChunk(), Entity.getDefaultNBT(this).putInt("Size", SIZE_SMALL));
                entity.spawnToAll();
            }

            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                drops.add(Item.get(Item.MAGMA_CREAM, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 1)));
            } else {
                drops.add(Item.get(Item.MAGMA_CREAM, 0, Utils.rand(0, 1)));
            }
        }
        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        if (this.size == SIZE_BIG) return 4;
        else if (this.size == SIZE_MEDIUM) return 2;
        else if (this.size == SIZE_SMALL) return 1;
        return 0;
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Magma Cube";
    }
}