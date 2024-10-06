package ee.mathiaskivi.speedbuilders.utility;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.TreeMap;

public class Configuration {

	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");
	private Map<String, Config> configurations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	public Configuration() {
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdirs();
		}
	}

	public YamlConfiguration getConfig(String name) {
		return configurations.get(name).get();
	}

	public void loadConfig(String name) {
		loadConfig(name, plugin.getDataFolder());
	}

	public void loadConfig(String name, File folder) {
		File file = new File(folder, name);
		if (!file.exists()) {
			try {
				file.createNewFile();

				InputStream inputStream = plugin.getResource(name);
				if (inputStream != null) {
					OutputStream outputStream = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = inputStream.read(buffer)) > 0) {
						outputStream.write(buffer, 0, length);
					}
					outputStream.close();
					inputStream.close();
				}
			} catch (IOException ignored) {}
		}

		Config config = new Config(file);
		config.load();

		configurations.put(name, config);
	}

	public void reloadConfig(String name) {
		configurations.get(name).load();
	}

	public void saveConfig(String name) {
		configurations.get(name).save();
	}

	private static class Config {
		private File file;
		private YamlConfiguration yamlConfiguration;

		public Config(File file) {
			this.file = file;
			this.yamlConfiguration = new YamlConfiguration();
		}

		public YamlConfiguration get() {
			return yamlConfiguration;
		}

		public void load() {
			try {
				yamlConfiguration.load(file);
			} catch (InvalidConfigurationException | IOException ignored) {}
		}

		public void save() {
			try {
				yamlConfiguration.save(file);
			} catch (ConcurrentModificationException | IOException | NullPointerException ignored) {}
		}
	}
}
