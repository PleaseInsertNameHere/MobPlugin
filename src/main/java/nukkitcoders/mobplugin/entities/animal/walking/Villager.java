package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntityWitch;
import cn.nukkit.event.entity.CreatureSpawnEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.entities.monster.walking.Zombie;
import nukkitcoders.mobplugin.entities.monster.walking.ZombieVillager;
import nukkitcoders.mobplugin.utils.Utils;

import static cn.nukkit.entity.passive.EntityVillagerV1.PROFESSION_GENERIC;

public class Villager extends WalkingAnimal {

    public static final int NETWORK_ID = 15;

    public Villager(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.3f;
        }
        return 0.6f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.9f;
        }
        return 1.9f;
    }

    @Override
    public double getSpeed() {
        return 1.1;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(10);

        if (!this.namedTag.contains("Profession")) {
            this.setProfession(PROFESSION_GENERIC);
        }
    }

    public int getProfession() {
        return this.namedTag.getInt("Profession");
    }

    public void setProfession(int profession) {
        this.namedTag.putInt("Profession", profession);
    }

    @Override
    public int getKillExperience() {
        return 0;
    }

    @Override
    public void onStruckByLightning(Entity entity) {
        Entity ent = Entity.createEntity("Witch", this);
        if (ent != null) {
            CreatureSpawnEvent cse = new CreatureSpawnEvent(EntityWitch.NETWORK_ID, this, ent.namedTag, CreatureSpawnEvent.SpawnReason.LIGHTNING);
            this.getServer().getPluginManager().callEvent(cse);

            if (cse.isCancelled()) {
                ent.close();
                return;
            }

            ent.yaw = this.yaw;
            ent.pitch = this.pitch;
            ent.setImmobile(this.isImmobile());
            if (this.hasCustomName()) {
                ent.setNameTag(this.getNameTag());
                ent.setNameTagVisible(this.isNameTagVisible());
                ent.setNameTagAlwaysVisible(this.isNameTagAlwaysVisible());
            }

            this.close();
            ent.spawnToAll();
        } else {
            super.onStruckByLightning(entity);
        }
    }

    @Override
    public void kill() {
        if (this.isAlive()) {
            if (this.lastDamageCause instanceof EntityDamageByEntityEvent) {
                Entity killer = ((EntityDamageByEntityEvent) this.lastDamageCause).getDamager();
                if (killer instanceof Zombie || killer instanceof ZombieVillager) {
                    if (Server.getInstance().getDifficulty() > 1) {
                        if (Utils.rand(1, 2 / (Server.getInstance().getDifficulty() - 1)) == 1) {
                            ZombieVillager zombieVillager = new ZombieVillager(this.getChunk(), this.getDefaultNBT(this));
                            zombieVillager.spawnToAll();
                        }
                    }
                }
            }

        }
        super.kill();
    }
}
