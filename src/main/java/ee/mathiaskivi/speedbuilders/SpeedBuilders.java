package ee.mathiaskivi.speedbuilders;

import ee.mathiaskivi.speedbuilders.multiworld.MultiWorld;
import ee.mathiaskivi.speedbuilders.utility.Configuration;
import ee.mathiaskivi.speedbuilders.utility.Statistics;
import ee.mathiaskivi.speedbuilders.utility.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class SpeedBuilders extends JavaPlugin {
	public MultiWorld multiWorld;

	public Configuration configuration;
	public Statistics statistics;

	public void onEnable() {
		new Metrics(this, 8034);

		configuration = new Configuration();
		statistics = new Statistics();

		configuration.loadConfig("config.yml");
		if (configuration.getConfig("config.yml").getString("plugin.version").equalsIgnoreCase("multiworld")) {
			multiWorld = new MultiWorld();
			multiWorld.onEnable();
		} else {
			getLogger().info("");
			getLogger().severe("[SpeedBuilders] Version for SpeedBuilders plugin has not been set!");
			getLogger().severe("[SpeedBuilders] Follow this instruction to fix this problem:");
			getLogger().severe("[SpeedBuilders] Change plugin version to \"multiworld\" in config.yml file.");
			getLogger().info("");
			Bukkit.getPluginManager().disablePlugin(this);
		}

		getLogger().info("[SpeedBuilders] SpeedBuilders v" + getDescription().getVersion() + " is enabled!");
	}

	public void onDisable() {
		if (multiWorld != null) {
			if (configuration.getConfig("config.yml").getString("plugin.version").equalsIgnoreCase("multiworld")) {
				multiWorld.onDisable();
			}

			if (statistics.getConnection() != null) {
				try {
					statistics.getConnection().close();
				} catch (SQLException ignored) {}
			}
		}

		getLogger().info("[SpeedBuilders] SpeedBuilders v" + getDescription().getVersion() + " is disabled!");
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
