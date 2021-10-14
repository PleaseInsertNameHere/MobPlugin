package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.entity.data.Vector3fEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.MinecraftItemID;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.SetEntityLinkPacket;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.utils.Utils;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Strider extends WalkingAnimal implements EntityRideable {

    public final static int NETWORK_ID = 125;

    private boolean saddled;

    public Strider(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.fireProof = true;
        this.setMaxHealth(20);

        if (this.namedTag.contains("Saddle")) {
            this.setSaddled(this.namedTag.getBoolean("Saddle"));
        }
    }

    @Override
    public float getWidth() {
        return this.isBaby() ? 0.45f : 0.9f;
    }

    @Override
    public float getHeight() {
        return this.isBaby() ? 0.85f : 1.7f;
    }

    public boolean mountEntity(Entity entity, byte mode) {
        Objects.requireNonNull(entity, "The target of the mounting entity can't be null");

        if (entity.riding != null) {
            dismountEntity(entity);
            entity.resetFallDistance();
            this.motionX = 0;
            this.motionZ = 0;
            this.stayTime = 20;
        } else {
            if (isPassenger(entity)) {
                return false;
            }

            broadcastLinkPacket(entity, SetEntityLinkPacket.TYPE_RIDE);

            entity.riding = this;
            entity.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, true);
            entity.setDataProperty(new Vector3fEntityData(DATA_RIDER_SEAT_POSITION, new Vector3f(0, 2.8f, 0)));
            entity.setDataProperty(new FloatEntityData(DATA_RIDER_MAX_ROTATION, 181));
            passengers.add(entity);
        }

        return true;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        Iterator<Entity> linkedIterator = this.passengers.iterator();

        while (linkedIterator.hasNext()) {
            Entity linked = linkedIterator.next();

            if (!linked.isAlive()) {
                if (linked.riding == this) {
                    linked.riding = null;
                }

                linkedIterator.remove();
            }
        }

        return super.onUpdate(currentTick);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putBoolean("Saddle", this.isSaddled());
    }

    public boolean isSaddled() {
        return this.saddled;
    }

    public void setSaddled(boolean saddled) {
        this.saddled = saddled;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_SADDLED, saddled);
    }

    public void onPlayerInput(Player player, double strafe, double forward) {
        if (player.getInventory().getItemInHand().getId() == Item.WARPED_FUNGUS_ON_A_STICK) {
            this.stayTime = 0;
            this.moveTime = 10;
            this.route = null;
            this.target = null;
            this.yaw = player.yaw;

            strafe *= 0.4;

            double f = strafe * strafe + forward * forward;
            double friction = 0.3;

            if (f >= 1.0E-4) {
                f = Math.sqrt(f);

                if (f < 1) {
                    f = 1;
                }

                f = friction / f;
                strafe *= f;
                forward *= f;
                double f1 = FastMath.sin(this.yaw * 0.017453292);
                double f2 = FastMath.cos(this.yaw * 0.017453292);
                this.motionX = (strafe * f2 - forward * f1);
                this.motionZ = (forward * f2 + strafe * f1);
            } else {
                this.motionX = 0;
                this.motionZ = 0;
            }
        }
    }

    @Override
    protected void checkTarget() {
        if (this.passengers.isEmpty() || !(this.getPassengers().get(0) instanceof Player) || ((Player) this.getPassengers().get(0)).getInventory().getItemInHand().getId() != Item.WARPED_FUNGUS_ON_A_STICK) {
            super.checkTarget();
        }
    }

    @Override
    public boolean canDespawn() {
        if (this.isSaddled()) {
            return false;
        }

        return super.canDespawn();
    }

    @Override
    public void updatePassengers() {
        if (this.passengers.isEmpty()) {
            return;
        }

        for (Entity passenger : new ArrayList<>(this.passengers)) {
            if (!passenger.isAlive() || Utils.entityInsideWaterFast(this)) {
                dismountEntity(passenger);
                passenger.resetFallDistance();
                continue;
            }

            updatePassengerPosition(passenger);
        }
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                drops.add(Item.get(Item.STRING, 0, Utils.rand(2, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 5)));
            } else {
                drops.add(Item.get(Item.STRING, 0, Utils.rand(2, 5)));
            }

            if (this.isSaddled()) {
                drops.add(Item.get(Item.SADDLE));
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (item.getId() == Item.SADDLE && !this.isSaddled() && !this.isBaby()) {
            player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            this.level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_SADDLE);
            this.setSaddled(true);
        } else if (this.isSaddled() && this.passengers.isEmpty() && !this.isBaby() && !player.isSneaking()) {
            if (player.riding == null) {
                this.mountEntity(player);
            }
        } else if (!this.isBaby() && item.getId() == MinecraftItemID.WARPED_FUNGUS.get(1).getId()) {
            if (!player.isCreative() || player.isSpectator()) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            }
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), item));
            this.setInLove();
            return true;
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && distance <= 40
                    && player.getInventory().getItemInHand().getId() == MinecraftItemID.WARPED_FUNGUS.get(1).getId();
        }
        return false;
    }
}
