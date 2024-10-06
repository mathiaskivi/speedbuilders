package ee.mathiaskivi.speedbuilders.multiworld.manager;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.multiworld.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KitManager {
	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public String getKit(Player player, String arenaName) {
		Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			if (arena.getPlayersKit().containsKey(player.getName())) {
				return arena.getPlayersKit().get(player.getName());
			}
		}
		return null;
	}

	public void setKit(Player player, String kit, String arenaName) {
		Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			arena.getPlayersKit().put(player.getName(), kit);
		}
	}

	public void giveKitItems(Player player, String arenaName) {
		Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			String kit = arena.getPlayersKit().get(player.getName());
			if (kit.equals("None")) {}
		}
	}
}
