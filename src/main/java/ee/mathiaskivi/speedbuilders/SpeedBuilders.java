package ee.mathiaskivi.speedbuilders;

import ee.mathiaskivi.speedbuilders.bungeecord.BungeeCord;
import ee.mathiaskivi.speedbuilders.multiworld.MultiWorld;
import ee.mathiaskivi.speedbuilders.utility.Configuration;
import ee.mathiaskivi.speedbuilders.utility.Statistics;
import ee.mathiaskivi.speedbuilders.utility.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class SpeedBuilders extends JavaPlugin {

	public BungeeCord bungeeCord;
	public MultiWorld multiWorld;

	public Configuration configuration;
	public Statistics statistics;

	public void onEnable() {
		Bukkit.getLogger().info("[SpeedBuilders] SpeedBuilders v" + getDescription().getVersion() + " is enabled!");

		new Metrics(this, 8034);

		configuration = new Configuration();
		statistics = new Statistics();

		configuration.loadConfig("config.yml");
		if (configuration.getConfig("config.yml").getString("plugin.version").equalsIgnoreCase("bungeecord")) {
			bungeeCord = new BungeeCord();
			bungeeCord.onEnable();
		} else if (configuration.getConfig("config.yml").getString("plugin.version").equalsIgnoreCase("multiworld")) {
			multiWorld = new MultiWorld();
			multiWorld.onEnable();
		} else {
			Bukkit.getLogger().info("");
			Bukkit.getLogger().severe("[SpeedBuilders] Version for SpeedBuilders plugin has not been set!");
			Bukkit.getLogger().severe("[SpeedBuilders] Follow this instruction to fix this problem:");
			Bukkit.getLogger().severe("[SpeedBuilders] Change plugin version to \"bungeecord\" or \"multiworld\" in config.yml file.");
			Bukkit.getLogger().info("");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	public void onDisable() {
		if (bungeeCord != null || multiWorld != null) {
			if (configuration.getConfig("config.yml").getString("plugin.version").equalsIgnoreCase("bungeecord")) {
				bungeeCord.onDisable();
			} else if (configuration.getConfig("config.yml").getString("plugin.version").equalsIgnoreCase("multiworld")) {
				multiWorld.onDisable();
			}

			if (statistics.getConnection() != null) {
				try {
					statistics.getConnection().close();
				} catch (SQLException ignored) {}
			}
		}

		Bukkit.getLogger().info("[SpeedBuilders] SpeedBuilders v" + getDescription().getVersion() + " is disabled!");
	}

	public BungeeCord getBungeeCord() {
		return bungeeCord;
	}

	public MultiWorld getMultiWorld() {
		return multiWorld;
	}

	public Configuration getConfigManager() {
		return configuration;
	}

	public Statistics getStatsManager() {
		return statistics;
	}
}
