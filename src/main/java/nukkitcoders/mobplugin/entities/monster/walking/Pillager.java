package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityShootBowEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Pillager extends WalkingMonster {

    public static final int NETWORK_ID = 114;

    public Pillager(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
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
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(24);
        this.setDamage(new float[]{0, 2, 3, 5});
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 80 && Utils.rand(1, 32) < 4 && this.distanceSquared(player) <= 100) {
            this.attackDelay = 0;

            double f = 1.5;
            double yaw = this.yaw + Utils.rand(-12.0, 12.0);
            double pitch = this.pitch + Utils.rand(-7.0, 7.0);
            Location pos = new Location(this.x - Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5, this.y + this.getHeight() - 0.18,
                    this.z + Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5, yaw, pitch, this.level);
            if (this.getLevel().getBlockIdAt((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()) == Block.AIR) {
                Entity k = Entity.createEntity("Arrow", pos, this);
                if (!(k instanceof EntityArrow)) {
                    return;
                }

                EntityArrow arrow = (EntityArrow) k;
                arrow.setMotion(new Vector3(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f, -Math.sin(Math.toRadians(pitch)) * f * f,
                        Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f));

                EntityShootBowEvent ev = new EntityShootBowEvent(this, Item.get(Item.ARROW, 0, 1), arrow, f);
                this.server.getPluginManager().callEvent(ev);

                EntityProjectile projectile = ev.getProjectile();
                if (ev.isCancelled()) {
                    projectile.close();
                } else {
                    ProjectileLaunchEvent launch = new ProjectileLaunchEvent(projectile);
                    this.server.getPluginManager().callEvent(launch);
                    if (launch.isCancelled()) {
                        projectile.close();
                    } else {
                        if (this.server.getDifficulty() == 0) {
                            projectile.namedTag.putDouble("damage", 0);
                        } else if (this.server.getDifficulty() == 1) {
                            projectile.namedTag.putDouble("damage", Utils.rand(3, 4));
                        } else if (this.server.getDifficulty() == 2) {
                            projectile.namedTag.putDouble("damage", 4);
                        } else if (this.server.getDifficulty() == 3) {
                            projectile.namedTag.putDouble("damage", Utils.rand(4, 5));
                        }

                        projectile.spawnToAll();
                        ((EntityArrow) projectile).setPickupMode(EntityArrow.PICKUP_NONE);
                        this.level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_CROSSBOW_SHOOT);
                    }
                }
            }
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
            drops.add(Item.get(Item.ARROW, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 2)));
        } else {
            drops.add(Item.get(Item.ARROW, 0, Utils.rand(0, 2)));
        }

        if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
            if (Utils.rand(1, 200) <= 17 + ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() * 2) {
                drops.add(Item.get(Item.CROSSBOW, Utils.rand(1, Item.get(Item.CROSSBOW).getMaxDurability()), 1));
            }
        } else {
            if (Utils.rand(1, 200) <= 17) {
                drops.add(Item.get(Item.CROSSBOW, Utils.rand(1, Item.get(Item.CROSSBOW).getMaxDurability()), 1));
            }
        }

        if (this.getDataFlag(DATA_FLAGS, DATA_FLAG_IS_ILLAGER_CAPTAIN)) {
            Item illagerBanner = Item.get(Item.BANNER, 15);
            illagerBanner.setCompoundTag(Base64.getDecoder().decode("CgAAAwQAVHlwZQEAAAAA"));
            drops.add(illagerBanner);
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return 5;
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);

        MobEquipmentPacket pk = new MobEquipmentPacket();
        pk.eid = this.getId();
        pk.item = Item.get(471, 0, 1);
        pk.hotbarSlot = 0;
        player.dataPacket(pk);
    }

    @Override
    public int nearbyDistanceMultiplier() {
        return 20;
    }
}