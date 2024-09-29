package ee.mathiaskivi.speedbuilders.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerWinEvent extends Event {

    private Player player;
    private static HandlerList handlers = new HandlerList();

    public PlayerWinEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
