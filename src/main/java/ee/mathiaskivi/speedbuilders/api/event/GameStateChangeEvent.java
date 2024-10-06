package ee.mathiaskivi.speedbuilders.api.event;

import ee.mathiaskivi.speedbuilders.utility.GameState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStateChangeEvent extends Event {

    private GameState gameState;
    private int playerCount;
    private static HandlerList handlers = new HandlerList();

    public GameStateChangeEvent(GameState gameState, int playerCount) {
        this.gameState = gameState;
        this.playerCount = playerCount;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
