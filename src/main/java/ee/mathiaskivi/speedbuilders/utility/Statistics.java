package ee.mathiaskivi.speedbuilders.utility;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

public class Statistics {
	public SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");
	public Connection connection = null;

	public void openConnection() {
		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
			if (plugin.getConfigManager().getConfig("config.yml").getString("stats.system-type").equalsIgnoreCase("sqlite")) {
				new BukkitRunnable() {
					public void run() {
						try {
							File file = new File(plugin.getDataFolder(), "stats.db");
							if (!file.exists()) {
								file.createNewFile();
							}

							Class.forName("org.sqlite.JDBC");
							connection = DriverManager.getConnection("jdbc:sqlite:" + file);
							Statement statement = connection.createStatement();
							statement.executeUpdate("CREATE TABLE IF NOT EXISTS `speedbuilders` (`uuid` varchar(36) NOT NULL, `username` varchar(16) NOT NULL, `wins` int(16) NOT NULL, `pbuilds` int(16) NOT NULL, `losses` int(16) NOT NULL, PRIMARY KEY (`uuid`));");
							statement.close();

							new BukkitRunnable() {
								public void run() {
									try {
										Statement statement = connection.createStatement();
										statement.executeQuery("SELECT 1 FROM `speedbuilders`");
										statement.close();
									} catch (SQLException ex) {}
								}
							}.runTaskTimerAsynchronously(plugin, 30*20L, 30*20L);
						} catch (ClassNotFoundException|IOException|SQLException ex) {
							ex.printStackTrace();
						}
					}
				}.runTaskAsynchronously(plugin);
			} else if (plugin.getConfigManager().getConfig("config.yml").getString("stats.system-type").equalsIgnoreCase("mysql")) {
				new BukkitRunnable() {
					public void run() {
						try {
							String host = plugin.getConfigManager().getConfig("config.yml").getString("mysql.host");
							int port = plugin.getConfigManager().getConfig("config.yml").getInt("mysql.port");
							String database = plugin.getConfigManager().getConfig("config.yml").getString("mysql.database");
							String username = plugin.getConfigManager().getConfig("config.yml").getString("mysql.username");
							String password = plugin.getConfigManager().getConfig("config.yml").getString("mysql.password");

							Class.forName("com.mysql.jdbc.Driver");
							connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
							Statement statement = connection.createStatement();
							statement.executeUpdate("CREATE TABLE IF NOT EXISTS `speedbuilders` (`uuid` varchar(36) NOT NULL, `username` varchar(16) NOT NULL, `wins` int(16) NOT NULL, `pbuilds` int(16) NOT NULL, `losses` int(16) NOT NULL, UNIQUE KEY `uuid` (`uuid`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
							statement.close();

							new BukkitRunnable() {
								public void run() {
									try {
										Statement statement = connection.createStatement();
										statement.executeQuery("SELECT 1 FROM `speedbuilders`");
										statement.close();
									} catch (SQLException ex) {}
								}
							}.runTaskTimerAsynchronously(plugin, 30*20L, 30*20L);
						} catch (ClassNotFoundException|SQLException ex) {
							ex.printStackTrace();
						}
					}
				}.runTaskAsynchronously(plugin);
			}
		}
	}

	public int getStats(StatsType statsType, Player player) {
		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
			if (plugin.getConfigManager().getConfig("config.yml").getString("stats.system-type").equalsIgnoreCase("sqlite")) {
				try {
					int stat = 0;

					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery("SELECT * FROM `speedbuilders` WHERE `uuid`='" + player.getUniqueId().toString() + "'");

					while (resultSet.next()) {
						stat = resultSet.getInt(statsType.toString().toLowerCase());
					}

					statement.close();
					return stat;
				} catch (SQLException ex) {
					return 0;
				}
			} else if (plugin.getConfigManager().getConfig("config.yml").getString("stats.system-type").equalsIgnoreCase("mysql")) {
				try {
					int stat = 0;

					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery("SELECT * FROM `speedbuilders` WHERE `uuid`='" + player.getUniqueId().toString() + "'");

					while (resultSet.next()) {
						stat = resultSet.getInt(statsType.toString().toLowerCase());
					}

					statement.close();
					return stat;
				} catch (SQLException ex) {
					return 0;
				}
			}
		}
		return 0;
	}

	public void showStats(final Player player1, final Player player2) {
		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
			if (plugin.getConfigManager().getConfig("config.yml").getString("stats.system-type").equalsIgnoreCase("sqlite")) {
				Inventory inventory = Bukkit.createInventory(player1, 9, ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY") + "&8" + translate("STATS")));

				ItemStack itemStack1 = new ItemStack(Material.DIAMOND);
				ItemMeta itemMeta1 = itemStack1.getItemMeta();
				itemMeta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-WINS") + translate("STATS-LOADING")));
				List<String> lore1 = new ArrayList<>();
				lore1.add("");
				itemMeta1.setLore(lore1);
				itemStack1.setItemMeta(itemMeta1);

				ItemStack itemStack2 = new ItemStack(Material.BRICK);
				ItemMeta itemMeta2 = itemStack2.getItemMeta();
				itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-PBUILDS") + translate("STATS-LOADING")));
				List<String> lore2 = new ArrayList<>();
				lore2.add("");
				itemMeta2.setLore(lore2);
				itemStack2.setItemMeta(itemMeta2);

				ItemStack itemStack3 = new ItemStack(Material.COAL);
				ItemMeta itemMeta3 = itemStack3.getItemMeta();
				itemMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-LOSSES") + translate("STATS-LOADING")));
				List<String> lore3 = new ArrayList<>();
				lore3.add("");
				itemMeta3.setLore(lore3);
				itemStack3.setItemMeta(itemMeta3);

				inventory.setItem(1, itemStack1);
				inventory.setItem(4, itemStack2);
				inventory.setItem(7, itemStack3);

				new BukkitRunnable() {
					public void run() {
						ItemStack itemStack1 = new ItemStack(Material.DIAMOND);
						ItemMeta itemMeta1 = itemStack1.getItemMeta();
						itemMeta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-WINS") + getStats(StatsType.WINS, player2)));
						List<String> lore1 = new ArrayList<>();
						lore1.add("");
						itemMeta1.setLore(lore1);
						itemStack1.setItemMeta(itemMeta1);

						ItemStack itemStack2 = new ItemStack(Material.BRICK);
						ItemMeta itemMeta2 = itemStack2.getItemMeta();
						itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-PBUILDS") + getStats(StatsType.PBUILDS, player2)));
						List<String> lore2 = new ArrayList<>();
						lore2.add("");
						itemMeta2.setLore(lore2);
						itemStack2.setItemMeta(itemMeta2);

						ItemStack itemStack3 = new ItemStack(Material.COAL);
						ItemMeta itemMeta3 = itemStack3.getItemMeta();
						itemMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-LOSSES") + getStats(StatsType.LOSSES, player2)));
						List<String> lore3 = new ArrayList<>();
						lore3.add("");
						itemMeta3.setLore(lore3);
						itemStack3.setItemMeta(itemMeta3);

						inventory.setItem(1, itemStack1);
						inventory.setItem(4, itemStack2);
						inventory.setItem(7, itemStack3);

						player1.updateInventory();
					}
				}.runTaskAsynchronously(plugin);

				player1.openInventory(inventory);
			} else if (plugin.getConfigManager().getConfig("config.yml").getString("stats.system-type").equalsIgnoreCase("mysql")) {
				Inventory inventory = Bukkit.createInventory(player1, 9, ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY") + "&8" + translate("STATS")));

				ItemStack itemStack1 = new ItemStack(Material.DIAMOND);
				ItemMeta itemMeta1 = itemStack1.getItemMeta();
				itemMeta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-WINS") + translate("STATS-LOADING")));
				List<String> lore1 = new ArrayList<>();
				lore1.add("");
				itemMeta1.setLore(lore1);
				itemStack1.setItemMeta(itemMeta1);

				ItemStack itemStack2 = new ItemStack(Material.BRICK);
				ItemMeta itemMeta2 = itemStack2.getItemMeta();
				itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-PBUILDS") + translate("STATS-LOADING")));
				List<String> lore2 = new ArrayList<>();
				lore2.add("");
				itemMeta2.setLore(lore2);
				itemStack2.setItemMeta(itemMeta2);

				ItemStack itemStack3 = new ItemStack(Material.COAL);
				ItemMeta itemMeta3 = itemStack3.getItemMeta();
				itemMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-LOSSES") + translate("STATS-LOADING")));
				List<String> lore3 = new ArrayList<>();
				lore3.add("");
				itemMeta3.setLore(lore3);
				itemStack3.setItemMeta(itemMeta3);

				inventory.setItem(1, itemStack1);
				inventory.setItem(4, itemStack2);
				inventory.setItem(7, itemStack3);

				new BukkitRunnable() {
					public void run() {
						ItemStack itemStack1 = new ItemStack(Material.DIAMOND);
						ItemMeta itemMeta1 = itemStack1.getItemMeta();
						itemMeta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-WINS") + getStats(StatsType.WINS, player2)));
						List<String> lore1 = new ArrayList<>();
						lore1.add("");
						itemMeta1.setLore(lore1);
						itemStack1.setItemMeta(itemMeta1);

						ItemStack itemStack2 = new ItemStack(Material.BRICK);
						ItemMeta itemMeta2 = itemStack2.getItemMeta();
						itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-PBUILDS") + getStats(StatsType.PBUILDS, player2)));
						List<String> lore2 = new ArrayList<>();
						lore2.add("");
						itemMeta2.setLore(lore2);
						itemStack2.setItemMeta(itemMeta2);

						ItemStack itemStack3 = new ItemStack(Material.COAL);
						ItemMeta itemMeta3 = itemStack3.getItemMeta();
						itemMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("STATS-LOSSES") + getStats(StatsType.LOSSES, player2)));
						List<String> lore3 = new ArrayList<>();
						lore3.add("");
						itemMeta3.setLore(lore3);
						itemStack3.setItemMeta(itemMeta3);

						inventory.setItem(1, itemStack1);
						inventory.setItem(4, itemStack2);
						inventory.setItem(7, itemStack3);

						player1.updateInventory();
					}
				}.runTaskAsynchronously(plugin);

				player1.openInventory(inventory);
			}
		}
	}

	public void setValue(final StatsType statsType, final Player player, final int value) {
		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
			if (plugin.getConfigManager().getConfig("config.yml").getString("stats.system-type").equalsIgnoreCase("sqlite")) {
				new BukkitRunnable() {
					public void run() {
						try {
							Statement statement = connection.createStatement();
							statement.executeUpdate("INSERT OR IGNORE INTO `speedbuilders` (`uuid`, `username`, `wins`, `pbuilds`, `losses`) VALUES ('" + player.getUniqueId().toString() + "', '" + player.getName() + "', '" + getStats(StatsType.WINS, player) + "', '" + getStats(StatsType.PBUILDS, player) + "', '" + getStats(StatsType.LOSSES, player) + "');");
							statement.executeUpdate("UPDATE `speedbuilders` SET `" + statsType.toString().toLowerCase() + "` = '" + value + "' WHERE `uuid` ='" + player.getUniqueId().toString() + "';");
							statement.close();
						} catch (SQLException ex) {}
					}
				}.runTaskAsynchronously(plugin);
			} else if (plugin.getConfigManager().getConfig("config.yml").getString("stats.system-type").equalsIgnoreCase("mysql")) {
				new BukkitRunnable() {
					public void run() {
						try {
							Statement statement = connection.createStatement();
							statement.executeUpdate("INSERT IGNORE INTO `speedbuilders` (`uuid`, `username`, `wins`, `pbuilds`, `losses`) VALUES ('" + player.getUniqueId().toString() + "', '" + player.getName() + "', '" + getStats(StatsType.WINS, player) + "', '" + getStats(StatsType.PBUILDS, player) + "', '" + getStats(StatsType.LOSSES, player) + "');");
							statement.executeUpdate("UPDATE `speedbuilders` SET `" + statsType.toString().toLowerCase() + "` = '" + value + "' WHERE `uuid` ='" + player.getUniqueId().toString() + "';");
							statement.close();
						} catch (SQLException ex) {}
					}
				}.runTaskAsynchronously(plugin);
			}
		}
	}

	public void incrementStat(final StatsType statsType, final Player player, final int value) {
		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
			if (plugin.getConfigManager().getConfig("config.yml").getString("stats.system-type").equalsIgnoreCase("sqlite")) {
				new BukkitRunnable() {
					public void run() {
						try {
							Statement statement = connection.createStatement();
							statement.executeUpdate("INSERT OR IGNORE INTO `speedbuilders` (`uuid`, `username`, `wins`, `pbuilds`, `losses`) VALUES ('" + player.getUniqueId().toString() + "', '" + player.getName() + "', '" + getStats(StatsType.WINS, player) + "', '" + getStats(StatsType.PBUILDS, player) + "', '" + getStats(StatsType.LOSSES, player) + "');");
							statement.executeUpdate("UPDATE `speedbuilders` SET `" + statsType.toString().toLowerCase() + "` = '" + (getStats(statsType, player) + value) + "' WHERE `uuid` ='" + player.getUniqueId().toString() + "';");
							statement.close();
						} catch (SQLException ex) {}
					}
				}.runTaskAsynchronously(plugin);
			} else if (plugin.getConfigManager().getConfig("config.yml").getString("stats.system-type").equalsIgnoreCase("mysql")) {
				new BukkitRunnable() {
					public void run() {
						try {
							Statement statement = connection.createStatement();
							statement.executeUpdate("INSERT IGNORE INTO `speedbuilders` (`uuid`, `username`, `wins`, `pbuilds`, `losses`) VALUES ('" + player.getUniqueId().toString() + "', '" + player.getName() + "', '" + getStats(StatsType.WINS, player) + "', '" + getStats(StatsType.PBUILDS, player) + "', '" + getStats(StatsType.LOSSES, player) + "');");
							statement.executeUpdate("UPDATE `speedbuilders` SET `" + statsType.toString().toLowerCase() + "` = '" + (getStats(statsType, player) + value) + "' WHERE `uuid` ='" + player.getUniqueId().toString() + "';");
							statement.close();
						} catch (SQLException ex) {}
					}
				}.runTaskAsynchronously(plugin);
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}
}
