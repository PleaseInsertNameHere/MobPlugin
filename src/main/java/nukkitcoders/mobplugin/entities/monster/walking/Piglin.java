package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityShootBowEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemCrossbow;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemRawGold;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.AnimateEntityPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.MobArmorEquipmentPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import nukkitcoders.mobplugin.MobPlugin;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.utils.PiglinDrops;
import nukkitcoders.mobplugin.utils.PiglinInventory;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Piglin extends WalkingMonster implements InventoryHolder {

    public final static int NETWORK_ID = 123;

    public Item itemoffhand;
    public Item itemhand;
    protected boolean isRunning = false;
    protected PiglinInventory inventory;
    private boolean trading = false;
    private int angry = 0;
    private int lookingTicks = 0;
    private Player lookingPlayer;

    public Piglin(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    private static boolean isWearingGold(Player p) {
        if (p.getInventory() == null) return false;
        PlayerInventory i = p.getInventory();
        return i.getHelmet().getId() == ItemID.GOLD_HELMET || i.getChestplate().getId() == ItemID.GOLD_CHESTPLATE || i.getLeggings().getId() == ItemID.GOLD_LEGGINGS || i.getBoots().getId() == ItemID.GOLD_BOOTS;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public int getKillExperience() {
        int xp = 0;
        xp += this.isBaby() ? 1 : 5;
        if (!this.isBaby()) {
            xp += Utils.rand(1, 3) * armor.length;
        }
        return xp;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(16);
        this.setDamage(new float[]{0, 3, 5, 7});


        if (namedTag.containsCompound("ItemHand")) {
            this.setItemhand(NBTIO.getItemHelper(namedTag.getCompound("ItemHand")));
        } else {
            if (!this.isBaby()) {
                this.setItemhand(Utils.rand(1, 2) == 1 ? Item.get(Item.GOLDEN_SWORD) : Item.get(Item.CROSSBOW));
            }
        }

        if (namedTag.containsCompound("ItemOffHand")) {
            this.setItemoffhand(NBTIO.getItemHelper(namedTag.getCompound("ItemOffHand")));
        }

        this.inventory = new PiglinInventory(this);
        if (!namedTag.contains("Items") || !(this.namedTag.get("Items") instanceof ListTag)) {
            this.namedTag.putList(new ListTag<CompoundTag>("Items"));
        }
        ListTag<CompoundTag> list = this.namedTag.getList("Items", CompoundTag.class);
        for (CompoundTag tag : list.getAll()) {
            Item item = NBTIO.getItemHelper(tag);
            this.inventory.slots.put(tag.getByte("Slot"), item);
        }

        if (!namedTag.contains("Armor") || !(this.namedTag.get("Armor") instanceof ListTag)) {
            setArmor(getRandomGoldArmor());
            this.namedTag.putList(new ListTag<CompoundTag>("Armor"));
            return;
        }
        ListTag<CompoundTag> armor = this.namedTag.getList("Armor", CompoundTag.class);
        Item[] armorItems = new Item[4];
        for (CompoundTag tag : armor.getAll()) {
            Item item = NBTIO.getItemHelper(tag);
            armorItems[tag.getByte("Slot")] = item;
        }
        this.setArmor(armorItems);
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
    public void attackEntity(Entity player) {
        if (itemhand instanceof ItemCrossbow) {
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
                            if (this.server.getDifficulty() != 0) {
                                projectile.namedTag.putDouble("damage", this.server.getDifficulty() <= 2 ? Utils.rand(2, 4) : Utils.rand(2, 5));
                            } else {
                                projectile.namedTag.putDouble("damage", 0);

                            }
                            projectile.spawnToAll();
                            ((EntityArrow) projectile).setPickupMode(EntityArrow.PICKUP_NONE);
                            this.level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_CROSSBOW_SHOOT);
                        }
                    }
                }
            }
        } else {
            if (this.attackDelay > 30 && player.distanceSquared(this) <= 1) {
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

    }

    @Override
    public void jumpEntity(Entity player) {

    }

    public boolean isAngry() {
        return this.angry > 0;
    }

    public void setAngry(int val) {
        this.angry = val;
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        super.attack(ev);

        if (!ev.isCancelled() && ev instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) ev).getDamager() instanceof Player) {
                this.setAngry(600);
            }
        }

        return true;
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (distance <= 100 && this.isAngry() && creature instanceof Piglin && !((Piglin) creature).isAngry()) {
            ((Piglin) creature).setAngry(600);
        }
        return creature instanceof Player && (this.isAngry() || !isWearingGold((Player) creature)) && super.targetOption(creature, distance);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        /*if (item.getId() == Item.get(Item.GOLD_INGOT).getId() && !this.isTrading()) {
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_ADMIRING, true);
            setTrading(true);
            Item cloneitem = item.clone();
            cloneitem.count = 1;
            setItemoffhand(cloneitem);
            this.stayTime = 20 * 7;
            this.noRotateTicks = 20 * 7;
            // Todo: Fix Animation (Animation doesnt stop)
            AnimateEntityPacket packet = new AnimateEntityPacket();
            packet.setAnimation("animation.piglin.admire");
            packet.setNextState("default");
            packet.setBlendOutTime(6f);
            packet.setStopExpression("");
            packet.setController("");
            List<Long> entityruntimeids = new ArrayList<>();
            entityruntimeids.add(this.getId());
            packet.setEntityRuntimeIds(entityruntimeids);
            for (Player all : Server.getInstance().getOnlinePlayers().values()) {
                all.dataPacket(packet);
            }
            this.lookingPlayer = player;
            this.lookingTicks = 120;

            Server.getInstance().getScheduler().scheduleDelayedTask(MobPlugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    if (!Piglin.this.isClosed()) {
                        Piglin.this.spawnToAll();
                        setTrading(false);
                        setItemoffhand(Item.get(Item.AIR));
                        if (player.isAlive() && !player.isClosed() && player.getLevel().equals(Piglin.this.getLevel()) && Piglin.this.distance(player) <= 7) {
                            Vector3 motion = player.subtract(Piglin.this.add(0, 1, 0)).multiply(0.1D);
                            motion.y += Math.sqrt(player.distance(Piglin.this.add(0, 1, 0))) * 0.12D;
                            Piglin.this.getLevel().dropItem(Piglin.this.add(0, 1, 0), PiglinDrops.getResult(), motion);
                        } else {
                            Piglin.this.getLevel().dropItem(Piglin.this, PiglinDrops.getResult());

                        }
                    }
                }
            }, 20 * 6);
            return true;
        }*/
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);

        if (this.itemoffhand != null) {
            MobEquipmentPacket pk = new MobEquipmentPacket();
            pk.eid = this.getId();
            pk.inventorySlot = 1;
            pk.item = this.itemoffhand;
            for (Player all : Server.getInstance().getOnlinePlayers().values())
                all.dataPacket(pk);
        }

        if (this.itemhand != null) {
            MobEquipmentPacket pk = new MobEquipmentPacket();
            pk.eid = this.getId();
            pk.hotbarSlot = 1;
            pk.item = this.itemhand;
            for (Player all : Server.getInstance().getOnlinePlayers().values())
                all.dataPacket(pk);
        }
        if (armor != null) {
            MobArmorEquipmentPacket pk = new MobArmorEquipmentPacket();
            pk.eid = this.getId();
            pk.slots = this.armor;
            for (Player all : Server.getInstance().getOnlinePlayers().values())
                all.dataPacket(pk);
        }
    }

    private Item[] getRandomGoldArmor() {
        Item[] armorgold = new Item[4];
        int helmet = Utils.rand(1, 4);
        int chestplate = Utils.rand(1, 4);
        int leggings = Utils.rand(1, 4);
        int boots = Utils.rand(1, 4);
        if (helmet == 1) {
            armorgold[0] = Item.get(Item.GOLD_HELMET);
        }
        if (chestplate == 1) {
            armorgold[1] = Item.get(Item.GOLD_CHESTPLATE);
        }
        if (leggings == 1) {
            armorgold[2] = Item.get(Item.GOLD_LEGGINGS);
        }
        if (boots == 1) {
            armorgold[3] = Item.get(Item.GOLD_BOOTS);
        }
        return armorgold;
    }

    public Item[] getArmor() {
        return this.armor;
    }

    public void setArmor(Item[] newarmor) {
        this.armor = newarmor;
        this.spawnToAll();
    }

    public Item getItemhand() {
        return itemhand;
    }

    public void setItemhand(Item itemhand) {
        this.itemhand = itemhand;
        this.spawnToAll();
    }

    public Item getItemoffhand() {
        return itemoffhand;
    }

    public void setItemoffhand(Item itemoffhand) {
        this.itemoffhand = itemoffhand;
        this.spawnToAll();
    }

    public boolean isTrading() {
        return trading;
    }

    public void setTrading(boolean trading) {
        this.trading = trading;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) { // Todo: Turn into zombified piglin in overworld or end
        if (lookingTicks > 0) {
            if (lookingPlayer != null && !lookingPlayer.isClosed() && lookingPlayer.isAlive() && lookingPlayer.getLevel().equals(this.getLevel())) {
                double xdiff = lookingPlayer.x - this.x;
                double zdiff = lookingPlayer.z - this.z;
                double angle = Math.atan2(zdiff, xdiff);
                double yaw = angle * 180.0D / Math.PI - 90.0D;

                double ydiff = lookingPlayer.y - this.y;
                Vector2 v = new Vector2(this.x, this.z);
                double dist = v.distance(lookingPlayer.x, lookingPlayer.z);
                angle = Math.atan2(dist, ydiff);
                double pitch = angle * 180.0D / Math.PI - 90.0D;
                Piglin.this.setRotation(yaw, pitch);
                lookingTicks--;
            } else {
                lookingTicks = 0;
                lookingPlayer = null;
            }
        }


        /*if (!this.isTrading() && this.stayTime <= 0) {
            if (target == null || (target instanceof Entity && ((Entity) target).closed) || !(target instanceof Entity)) {
                for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(16, 16, 16), this)) {
                    if (entity instanceof EntityItem) {
                        Item item = ((EntityItem) entity).getItem();
                        if (item.getId() != Item.GOLD_INGOT) {
                            if ((isItemValid(item) && this.inventory.canAddItem(item)) || item.isArmor()) {
                                isRunning = true;
                                setTarget(entity);
                            }
                        } else {
                            isRunning = true;
                            setTarget(entity);
                        }

                    }
                }
            }
        }

        for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(1, 0, 1), this)) {
            if (entity instanceof EntityItem) {
                Item item = ((EntityItem) entity).getItem();
                if (!this.isTrading()) {
                    if (item.getId() == Item.GOLD_INGOT) {
                        if (!this.isBaby()) {
                            animate(item, entity);
                            Server.getInstance().getScheduler().scheduleDelayedTask(MobPlugin.getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    if (!Piglin.this.isClosed()) {
                                        setTrading(false);
                                        setItemoffhand(Item.get(Item.AIR));
                                        Piglin.this.setDataFlag(DATA_FLAGS, DATA_FLAG_ADMIRING, false);
                                        Piglin.this.getLevel().dropItem(Piglin.this, PiglinDrops.getResult());
                                    }
                                }
                            }, 20 * 6);
                            break;
                        } else {
                            setItemhand(item);
                        }
                    } else if (isItemValid(item)) {
                        if (this.inventory.canAddItem(item)) {
                            animate(item, entity);

                            Server.getInstance().getScheduler().scheduleDelayedTask(MobPlugin.getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    if (!Piglin.this.isClosed()) {
                                        setTrading(false);
                                        Piglin.this.setDataFlag(DATA_FLAGS, DATA_FLAG_ADMIRING, false);
                                        setItemoffhand(Item.get(Item.AIR));
                                        if (item.isHelmet() && (armor[0] == null || armor[0].getId() != Item.GOLD_HELMET)) {
                                            Item[] armorHelmet = getArmor();
                                            if (armorHelmet[0] != null) {
                                                Piglin.this.getLevel().dropItem(Piglin.this, armorHelmet[0]);
                                            }
                                            armorHelmet[0] = item;
                                            setArmor(armorHelmet);
                                        } else if (item.isChestplate() && (armor[1] == null || armor[1].getId() != Item.GOLD_CHESTPLATE)) {
                                            Item[] armorChestplate = getArmor();
                                            if (armorChestplate[1] != null) {
                                                Piglin.this.getLevel().dropItem(Piglin.this, armorChestplate[1]);
                                            }
                                            armorChestplate[1] = item;
                                            setArmor(armorChestplate);

                                        } else if (item.isLeggings() && (armor[2] == null || armor[2].getId() != Item.GOLD_LEGGINGS)) {
                                            Item[] armorLeggings = getArmor();
                                            if (armorLeggings[2] != null) {
                                                Piglin.this.getLevel().dropItem(Piglin.this, armorLeggings[2]);
                                            }
                                            armorLeggings[2] = item;
                                            setArmor(armorLeggings);
                                        } else if (item.isBoots() && (armor[3] == null || armor[3].getId() != Item.GOLD_BOOTS)) {
                                            Item[] armorBoots = getArmor();
                                            if (armorBoots[3] != null) {
                                                Piglin.this.getLevel().dropItem(Piglin.this, armorBoots[3]);
                                            }
                                            armorBoots[3] = item;
                                            setArmor(armorBoots);
                                        } else if (item.isSword() && itemhand == null) {
                                            setItemhand(item);
                                        } else {
                                            if (Piglin.this.inventory.canAddItem(item)) {
                                                Piglin.this.inventory.addItem(item);
                                            }
                                        }
                                    }
                                }
                            }, 20 * 6);
                            break;
                        }
                    } else {
                        if ((item.isHelmet() && (armor[0] == null || armor[0].getId() != Item.GOLD_HELMET && item.getTier() > armor[0].getTier())) || (item.isChestplate() && (armor[1] == null || armor[1].getId() != Item.GOLD_CHESTPLATE && item.getTier() > armor[1].getTier())) || (item.isLeggings() && (armor[2] == null || armor[2].getId() != Item.GOLD_LEGGINGS && item.getTier() > armor[2].getTier())) || (item.isBoots() && (armor[3] == null || armor[3].getId() != Item.GOLD_BOOTS && item.getTier() > armor[3].getTier()))) {
                            if (item.isHelmet() && (armor[0] == null || armor[0].getId() != Item.GOLD_HELMET)) {
                                Item[] armorHelmet = getArmor();
                                if (armorHelmet[0] != null) {
                                    this.getLevel().dropItem(this, armorHelmet[0]);
                                }
                                armorHelmet[0] = item;
                                setArmor(armorHelmet);
                            } else if (item.isChestplate() && (armor[1] == null || armor[1].getId() != Item.GOLD_CHESTPLATE)) {
                                Item[] armorChestplate = getArmor();
                                if (armorChestplate[1] != null) {
                                    this.getLevel().dropItem(this, armorChestplate[1]);
                                }
                                armorChestplate[1] = item;
                                setArmor(armorChestplate);

                            } else if (item.isLeggings() && (armor[2] == null || armor[2].getId() != Item.GOLD_LEGGINGS)) {
                                Item[] armorLeggings = getArmor();
                                if (armorLeggings[2] != null) {
                                    this.getLevel().dropItem(this, armorLeggings[2]);
                                }
                                armorLeggings[2] = item;
                                setArmor(armorLeggings);
                            } else if (item.isBoots() && (armor[3] == null || armor[3].getId() != Item.GOLD_BOOTS)) {
                                Item[] armorBoots = getArmor();
                                if (armorBoots[3] != null) {
                                    this.getLevel().dropItem(this, armorBoots[3]);
                                }
                                armorBoots[3] = item;
                                setArmor(armorBoots);
                            }
                            if (item.getCount() > 1) {
                                item.setCount(item.getCount() - 1);
                                entity.spawnToAll();
                            } else {
                                entity.close();
                            }
                        }
                    }
                }
            }
        }*/

        if (isRunning) {
            this.stayTime = 0;
        }
        return super.entityBaseTick(tickDiff);
    }

    private boolean isItemValid(Item item) {
        switch (item.getId()) {
            case 255 - BlockID.BELL:
            case BlockID.GOLD_BLOCK:
            case 255 - BlockID.RAW_GOLD_BLOCK:
            case Item.CLOCK:
            case 255 - BlockID.DEEPSLATE_GOLD_ORE:
            case Item.GOLDEN_APPLE_ENCHANTED:
            case 255 - BlockID.GILDED_BLACKSTONE:
            case Item.GLISTERING_MELON:
            case Item.GOLD_INGOT:
            case Item.GOLD_NUGGET:
            case BlockID.GOLD_ORE:
            case Item.GOLDEN_APPLE:
            case Item.GOLDEN_AXE:
            case Item.GOLD_BOOTS:
            case Item.GOLDEN_CARROT:
            case Item.GOLD_CHESTPLATE:
            case Item.GOLD_HELMET:
            case Item.GOLDEN_HOE:
            case Item.GOLD_HORSE_ARMOR:
            case Item.GOLD_LEGGINGS:
            case Item.GOLD_PICKAXE:
            case Item.GOLD_SHOVEL:
            case Item.GOLD_SWORD:
            case Item.LIGHT_WEIGHTED_PRESSURE_PLATE:
            case 255 - BlockID.NETHER_GOLD_ORE:
            case BlockID.POWERED_RAIL:
            case ItemID.RAW_PORKCHOP:
                return true;

            default:
                return item instanceof ItemRawGold;
        }
    }

    private void animate(Item item, Entity entity) {
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_ADMIRING, true);
        this.stayTime = 20 * 7;
        this.lookingTicks = 20 * 7;
        setTrading(true);
        this.isRunning = false;
        if (item.getCount() > 1) {
            item.setCount(item.getCount() - 1);
            entity.spawnToAll();
        } else {
            entity.close();
        }
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_ADMIRING, true);
        this.stayTime = 20 * 7;
        this.lookingTicks = 20 * 7;
        setTrading(true);
        Item offhandItem = item.clone();
        offhandItem.setCount(1);
        setItemoffhand(offhandItem);
        this.isRunning = false;


        // Todo: Fix Animation (Animation doesnt stop)
        AnimateEntityPacket packet = new AnimateEntityPacket();
        packet.setAnimation("animation.piglin.admire");
        packet.setNextState("default");
        packet.setBlendOutTime(6f);
        packet.setStopExpression("");
        packet.setController("");
        List<Long> entityruntimeids = new ArrayList<>();
        entityruntimeids.add(this.getId());
        packet.setEntityRuntimeIds(entityruntimeids);
        for (Player all : Server.getInstance().getOnlinePlayers().values())
            all.dataPacket(packet);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>(this.inventory.getContents().values());

        for (Item item : getArmor()) {
            if (this.getLastDamageCause() != null && this.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() >= 1) {
                if (Utils.rand(1, 200) <= 17 + ((EntityDamageByEntityEvent) this.getLastDamageCause()).getLootingLevel() * 2) {
                    drops.add(item);
                }
            } else {
                if (Utils.rand(1, 200) <= 17) {
                    drops.add(item);
                }
            }
        }
        if (Utils.rand(1, 200) <= 17) {

            drops.add(getItemhand() == null ? Item.get(Item.AIR) : itemhand);
        }

        drops.add(getItemoffhand() == null ? Item.get(Item.AIR) : itemoffhand);
        return drops.toArray(new Item[0]);
    }

    @Override
    public void saveNBT() {
        this.namedTag.putList(new ListTag<CompoundTag>("Items"));
        for (int index = 0; index < this.inventory.getSize(); index++) {
            Item item = this.inventory.getItem(index);
            int i = getSlotIndex(index);
            if (item.getId() == Item.AIR || item.getCount() <= 0) {
                if (i >= 0) {
                    this.namedTag.getList("Items").remove(i);
                }
            } else if (i < 0) {
                this.namedTag.getList("Items", CompoundTag.class).add(NBTIO.putItemHelper(item, index));
            } else {
                this.namedTag.getList("Items", CompoundTag.class).add(i, NBTIO.putItemHelper(item, index));
            }
        }

        this.namedTag.putList(new ListTag<CompoundTag>("Armor"));
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null) {
                this.namedTag.getList("Armor", CompoundTag.class).add(NBTIO.putItemHelper(armor[i], i));
            }
        }
        if (itemhand != null) {
            this.namedTag.putCompound("ItemHand", NBTIO.putItemHelper(itemhand));
        }

        if (itemoffhand != null) {
            this.namedTag.putCompound("ItemOffHand", NBTIO.putItemHelper(itemoffhand));
        }


        super.saveNBT();
    }

    protected int getSlotIndex(int index) {
        ListTag<CompoundTag> list = this.namedTag.getList("Items", CompoundTag.class);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getByte("Slot") == index) {
                return i;
            }
        }
        return -1;
    }
}
