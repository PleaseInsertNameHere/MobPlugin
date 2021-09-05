package nukkitcoders.mobplugin.event.entity;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Position;

public class SpawnGolemEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Position golemPosition;
    private final Player player;
    private final GolemType golemType;

    public SpawnGolemEvent(Player player, Position golemPosition, GolemType golemType) {
        this.player = player;
        this.golemPosition = golemPosition;
        this.golemType = golemType;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Position getGolemPosition() {
        return this.golemPosition;
    }

    public GolemType getGolemType() {
        return this.golemType;
    }

    public enum GolemType {
        IRON_GOLEM,
        SNOW_GOLEM
    }
}