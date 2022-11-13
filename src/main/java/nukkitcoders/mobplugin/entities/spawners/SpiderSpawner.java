package nukkitcoders.mobplugin.entities.spawners;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import nukkitcoders.mobplugin.AutoSpawnTask;
import nukkitcoders.mobplugin.MobPlugin;
import nukkitcoders.mobplugin.entities.autospawn.AbstractEntitySpawner;
import nukkitcoders.mobplugin.entities.monster.walking.Spider;

public class SpiderSpawner extends AbstractEntitySpawner {

    public SpiderSpawner(AutoSpawnTask spawnTask) {
        super(spawnTask);
    }

    public void spawn(Player player, Position pos, Level level) {
        final int biomeId = level.getBiomeId((int) pos.x, (int) pos.z);
        if (biomeId != 14 && biomeId != 15) {
            if (level.getBlockLightAt((int) pos.x, (int) pos.y, (int) pos.z) <= 7) {
                if (MobPlugin.isMobSpawningAllowedByTime(level)) {
                    this.spawnTask.createEntity("Spider", pos.add(0.5, 1, 0.5));
                }
            }
        }
    }

    @Override
    public final int getEntityNetworkId() {
        return Spider.NETWORK_ID;
    }
}
