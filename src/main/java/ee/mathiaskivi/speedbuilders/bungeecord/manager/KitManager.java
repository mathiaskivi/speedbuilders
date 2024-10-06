package ee.mathiaskivi.speedbuilders.bungeecord.manager;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KitManager {
	public SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public String getKit(Player player) {
		if (plugin.getBungeeCord().playersKit.containsKey(player.getName())) {
			return plugin.getBungeeCord().playersKit.get(player.getName());
		}
		return null;
	}

	public void setKit(Player player, String kit) {
		plugin.getBungeeCord().playersKit.put(player.getName(), kit);
	}

	public void giveKitItems(Player player) {
		String kit = plugin.getBungeeCord().playersKit.get(player.getName());

		if (kit.equals("None")) {}
	}
}
