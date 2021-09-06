package nukkitcoders.mobplugin.event.entity;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Position;

public class SpawnWitherEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Position pos;
    private final Player player;

    public SpawnWitherEvent(Player player, Position pos) {
        this.player = player;
        this.pos = pos;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Position getPosition() {
        return this.pos;
    }
}
