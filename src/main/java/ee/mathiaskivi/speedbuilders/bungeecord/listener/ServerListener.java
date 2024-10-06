package ee.mathiaskivi.speedbuilders.bungeecord.listener;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.utility.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.server.ServerListPingEvent;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

public class ServerListener implements Listener {
	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerListPing(ServerListPingEvent e) {
		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("bungeecord.custom-motd.enabled")) {
			e.setMotd(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.custom-motd.message").replaceAll("%GAMESTATE%", translate("GAMESTATE-" + plugin.getBungeeCord().gameState.toString()))));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent e) {
		if ((Bukkit.getOnlinePlayers().size() + 1) > plugin.getBungeeCord().maxPlayers) {
			e.disallow(Result.KICK_FULL, ChatColor.translateAlternateColorCodes('&', translate("KICK-THE_SERVER_IS_FULL")));
		} else if (plugin.getBungeeCord().gameState != GameState.WAITING && plugin.getBungeeCord().gameState != GameState.STARTING) {
			e.disallow(Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&', translate("KICK-GAME_RUNNING")));
		}
	}
}
