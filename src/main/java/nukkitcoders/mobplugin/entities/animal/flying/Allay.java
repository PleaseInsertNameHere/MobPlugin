package nukkitcoders.mobplugin.entities.animal.flying;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import nukkitcoders.mobplugin.entities.animal.FlyingAnimal;

public class Allay extends FlyingAnimal {

    public static final int NETWORK_ID = 105;

    public Allay(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.addEffect(Effect.getEffect(Effect.LEVITATION).setAmplifier(1).setDuration(Integer.MAX_VALUE));
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if(creature instanceof Player) {
            Player player = (Player) creature;

            return player.isAlive() && !player.closed && distance > 30 && player.isSurvival() || player.isCreative();
        }
        return false;
    }

    @Override
    public Item[] getDrops() {
        return null;
    }

    @Override
    public double getSpeed() {
        return 0.8;
    }

    @Override
    public int getKillExperience() {
        return 0;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }
}
