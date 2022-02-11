package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.EntitySmite;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemSkull;
import cn.nukkit.item.ItemSwordStone;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.potion.Effect;
import nukkitcoders.mobplugin.entities.animal.swimming.Turtle;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.route.WalkerRouteFinder;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WitherSkeleton extends WalkingMonster implements EntitySmite {

    public static final int NETWORK_ID = 48;

    public WitherSkeleton(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.route = new WalkerRouteFinder(this);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Wither Skeleton";
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.fireProof = true;
        this.setMaxHealth(20);
        this.setDamage(new float[]{0, 3, 4, 6});
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (followTarget == null || followTarget.isClosed()) {
            for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(64, 64, 64), this)) {
                if (entity instanceof SnowGolem || entity instanceof IronGolem || (entity instanceof Turtle && ((Turtle) entity).isBaby()) || entity instanceof Piglin || entity instanceof PiglinBrute) {
                    setFollowTarget(entity, true);
                    setTarget(entity);
                    break;
                }
            }
        }
        return super.entityBaseTick(tickDiff);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return !player.closed && player.spawned && player.isAlive() && (player.isSurvival() || player.isAdventure()) && distance <= 144;
        }
        return creature.isAlive() && !creature.closed && distance <= 144;
    }

    @Override
    public float getWidth() {
        return 0.7f;
    }

    @Override
    public float getHeight() {
        return 2.4f;
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && player.distanceSquared(this) <= 1) {
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
            if (player.attack(new EntityDamageByEntityEvent(this, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage))) {
                player.addEffect(Effect.getEffect(Effect.WITHER).setDuration(200));
            }
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);

        MobEquipmentPacket pk = new MobEquipmentPacket();
        pk.eid = this.getId();
        pk.item = new ItemSwordStone();
        pk.hotbarSlot = 0;
        player.dataPacket(pk);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        for (Item item : super.getDrops()) {
            drops.add(item);
        }

        if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
            drops.add(Item.get(Item.BONE, 0, Utils.rand(0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 2)));
            if (Utils.rand(1, 3) == 1) {
                drops.add(Item.get(Item.COAL, 0, ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() + 1));
            }

            if (Utils.rand(1, 200) <= 5 + ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() * 2) {
                drops.add(Item.get(Item.SKULL, ItemSkull.WITHER_SKELETON_SKULL, 1));
            }

            if (Utils.rand(1, 200) <= 17 + ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() * 2) {
                drops.add(Item.get(Item.STONE_SWORD, Utils.rand(0, Item.get(Item.STONE_SWORD).getMaxDurability()), 1));
            }
        } else {
            drops.add(Item.get(Item.BONE, 0, Utils.rand(0, 2)));


            if (Utils.rand(1, 3) == 1) {
                drops.add(Item.get(Item.COAL, 0, 1));
            }

            if (Utils.rand(1, 200) <= 5) {
                drops.add(Item.get(Item.SKULL, 1, 1));
            }

            if (Utils.rand(1, 200) <= 17) {
                drops.add(Item.get(Item.STONE_SWORD, Utils.rand(0, 131), 1));
            }
        }

        if (this.lastDamageCause instanceof EntityDamageByEntityEvent) {
            Entity killer = ((EntityDamageByEntityEvent) this.lastDamageCause).getDamager();
            if (killer instanceof Creeper) {
                if (((Creeper) killer).isPowered()) {
                    drops.add(Item.get(Item.SKULL, ItemSkull.WITHER_SKELETON_SKULL, 1));
                }
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return 5;
    }
}
