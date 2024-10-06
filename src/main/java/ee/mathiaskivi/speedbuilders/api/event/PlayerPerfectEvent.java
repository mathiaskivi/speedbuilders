package ee.mathiaskivi.speedbuilders.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerPerfectEvent extends Event {

    private Player player;
    private float time;
    private static HandlerList handlers = new HandlerList();

    public PlayerPerfectEvent(Player player, float time) {
        this.player = player;
        this.time = time;
    }

    public Player getPlayer() {
        return player;
    }

    public float getTime() {
        return time;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
