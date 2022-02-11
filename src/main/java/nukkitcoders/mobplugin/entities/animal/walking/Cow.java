package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Cow extends WalkingAnimal {

    public static final int NETWORK_ID = 11;

    public Cow(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.45f;
        }
        return 0.9f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.65f;
        }
        return 1.3f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(10);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (item.getId() == Item.BUCKET && item.getDamage() == 0 && !this.isBaby()) {
            Item newBucket = Item.get(Item.BUCKET, 1, 1);
            if (player.getInventory().getItem(player.getInventory().getHeldItemIndex()).count > 1) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                if (player.getInventory().canAddItem(newBucket)) {
                    player.getInventory().addItem(newBucket);
                } else {
                    player.dropItem(newBucket);
                }
            } else {
                player.getInventory().setItemInHand(newBucket);
            }
            this.level.addSound(this, Sound.MOB_COW_MILK);
            return false;
        } else if (item.getId() == Item.WHEAT && !this.isBaby()) {
            if (!player.isCreative()) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            }
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), Item.get(Item.WHEAT)));
            this.setInLove();
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.isAlive() && !player.closed && player.getInventory().getItemInHand().getId() == Item.WHEAT && distance <= 49;
        }
        return false;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        for (Item item : super.getDrops()) {
            drops.add(item);
        }

        if (!this.isBaby()) {
            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 2)));
            } else {
                drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, 2)));
            }
            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                drops.add(Item.get(this.isOnFire() ? Item.STEAK : Item.RAW_BEEF, 0, Utils.rand(1, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 3)));
            } else {
                drops.add(Item.get(this.isOnFire() ? Item.STEAK : Item.RAW_BEEF, 0, Utils.rand(1, 3)));
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }
}
