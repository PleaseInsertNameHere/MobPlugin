package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.utils.Utils;

import java.lang.reflect.InvocationTargetException;

public class Axolotl extends Fish {

    public static final int NETWORK_ID = 130;
    public int color = 0;
    protected int inLoveTicks = 0;

    public Axolotl(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.3f;
    }

    int getBucketMeta() {
        return 12;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(14);

        if (!this.namedTag.containsInt("Variant")) {
            randomColor();
        } else {
            setColor(this.namedTag.getInt("Variant"));
        }
    }

    private int randomColor() {
        setColor(Utils.random.nextInt(5));

        return this.color;
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && (player.getInventory().getItemInHand().getId() == Item.BUCKET && player.getInventory().getItemInHand().getDamage() == 4) && distance <= 40;
        }
        return false;
    }

    public int getColor() {
        return namedTag.getInt("Variant");
    }

    public void setColor(int color) {
        this.color = color;
        this.namedTag.putInt("Variant", color);
        this.setDataProperty(new IntEntityData(DATA_VARIANT, color));
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 7);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
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
                        Axolotl axolotl = (Axolotl) entity;
                        if (axolotl.isInLove()) {
                            this.inLoveTicks = 0;
                            axolotl.inLoveTicks = 0;
                            this.spawnBaby();
                            break;
                        }
                    }
                }
            }
        }

        return hasUpdate;
    }

    public void setInLove() {
        this.inLoveTicks = 600;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_INLOVE);
    }

    public boolean isInLove() {
        return inLoveTicks > 0;
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if ((item.getId() == Item.BUCKET && item.getDamage() == 4) && !this.isBaby()) {
            if (!player.isCreative() || !player.isSpectator()) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            }
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), item));
            this.setInLove();
            return true;
        }
        return super.onInteract(player, item);
    }

    protected void spawnBaby() {
        Axolotl axolotl = null;
        try {
            axolotl = this.getClass().getConstructor(FullChunk.class, CompoundTag.class).newInstance(this.getChunk(), Entity.getDefaultNBT(this));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        axolotl.setBaby(true);
        axolotl.spawnToAll();
        this.getLevel().dropExpOrb(this, Utils.rand(1, 7));
    }

}
