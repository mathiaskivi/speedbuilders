package ee.mathiaskivi.speedbuilders.bungeecord.command;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.utility.Translations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

public class SBCommand implements CommandExecutor {
	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
		if (!plugin.getConfigManager().getConfig("config.yml").getBoolean("plugin.command-visible")) {
			if (!commandSender.isOp()) {
				commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig("spigot.yml").getString("messages.unknown-command")));
				return true;
			}
		}
		if (arguments.length == 0) {
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lSpeedBuilders v" + plugin.getDescription().getVersion()));
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb reload &8- &7" + translate("HMENU-RELOAD")));
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb setup &8- &7" + translate("HMENU-SETUP_THE_GAME")));
			if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
				commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb stats &8- &7" + translate("HMENU-SHOW_STATS")));
			}
			return true;
		} else if (arguments.length == 1) {
			if (arguments[0].equalsIgnoreCase("reload")) {
				if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.reload")) {
					plugin.getConfigManager().reloadConfig("config.yml");
					plugin.getConfigManager().reloadConfig("lobby.yml");
					plugin.getConfigManager().reloadConfig("maps.yml");
					plugin.getConfigManager().reloadConfig("messages.yml");
					plugin.getConfigManager().reloadConfig("templates.yml");
					plugin.getConfigManager().reloadConfig("spigot.yml");

					for (String message : plugin.getConfigManager().getConfig("messages.yml").getConfigurationSection("BUNGEECORD").getKeys(false)) {
						Translations.messages.put(message, plugin.getConfigManager().getConfig("messages.yml").getString("BUNGEECORD." + message));
					}

					commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Configuration files and messages are successfully reloaded."));
				} else {
					commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
				}
				return true;
			} else if (arguments[0].equalsIgnoreCase("setup")) {
				if (commandSender instanceof Player) {
					if (commandSender.hasPermission("sb.command.setup")) {
						Player player = (Player) commandSender;

						if (plugin.getBungeeCord().setup.containsKey(player.getName())) {
							Inventory inventory = Bukkit.createInventory(player, InventoryType.CHEST, ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY") + "&8Setup"));

							ItemStack itemStack1 = new ItemStack(Material.GUARDIAN_SPAWN_EGG);
							ItemMeta itemMeta1 = itemStack1.getItemMeta();
							itemMeta1.setDisplayName("§r§fSet spawnpoint location for guardian plot");
							itemStack1.setItemMeta(itemMeta1);

							ItemStack itemStack2 = new ItemStack(Material.PAPER);
							ItemMeta itemMeta2 = itemStack2.getItemMeta();
							itemMeta2.setDisplayName("§r§fSet template area positions for guardian plot");
							itemStack2.setItemMeta(itemMeta2);

							ItemStack itemStack3 = new ItemStack(Material.WHITE_BED);
							ItemMeta itemMeta3 = itemStack3.getItemMeta();
							itemMeta3.setDisplayName("§r§fSet spawnpoint location for each player plot");
							itemStack3.setItemMeta(itemMeta3);

							ItemStack itemStack4 = new ItemStack(Material.WOODEN_AXE);
							ItemMeta itemMeta4 = itemStack4.getItemMeta();
							itemMeta4.setDisplayName("§r§fSet plot area positions for each player plot");
							itemStack4.setItemMeta(itemMeta4);

							ItemStack itemStack5 = new ItemStack(Material.ARMOR_STAND);
							ItemMeta itemMeta5 = itemStack5.getItemMeta();
							itemMeta5.setDisplayName("§r§fSet laser location for each player plot");
							itemStack5.setItemMeta(itemMeta5);

							ItemStack itemStack6 = new ItemStack(Material.OAK_FENCE);
							ItemMeta itemMeta6 = itemStack6.getItemMeta();
							itemMeta6.setDisplayName("§r§fSet build area positions for each player plot");
							itemStack6.setItemMeta(itemMeta6);

							ItemStack itemStack7 = new ItemStack(Material.WRITABLE_BOOK);
							ItemMeta itemMeta7 = itemStack7.getItemMeta();
							itemMeta7.setDisplayName("§r§fAdd build template(s)");
							itemStack7.setItemMeta(itemMeta7);

							ItemStack itemStack8 = new ItemStack(Material.CLOCK);
							ItemMeta itemMeta8 = itemStack8.getItemMeta();
							itemMeta8.setDisplayName("§r§fReturn to lobby");
							itemStack8.setItemMeta(itemMeta8);

							inventory.addItem(itemStack1, itemStack2, itemStack3, itemStack4, itemStack5, itemStack6, itemStack7);
							inventory.setItem(22, itemStack8);
							player.openInventory(inventory);
						} else {
							Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY") + "&8Setup"));

							ItemStack itemStack1 = new ItemStack(Material.NETHER_STAR);
							ItemMeta itemMeta1 = itemStack1.getItemMeta();
							itemMeta1.setDisplayName("§r§fSet spawnpoint location for main lobby");
							itemStack1.setItemMeta(itemMeta1);

							ItemStack itemStack2 = new ItemStack(Material.PODZOL);
							ItemMeta itemMeta2 = itemStack2.getItemMeta();
							itemMeta2.setDisplayName("§r§fEdit map");
							itemStack2.setItemMeta(itemMeta2);

							inventory.setItem(0, itemStack1);
							inventory.setItem(8, itemStack2);
							player.openInventory(inventory);
						}
					} else {
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
					}
				} else {
					commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
				}
				return true;
			} else if (arguments[0].equalsIgnoreCase("stats")) {
				commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lSpeedBuilders v" + plugin.getDescription().getVersion()));
				commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb stats show (playerName)"));
				return true;
			}
		} else if (arguments.length == 2) {
			if (arguments[0].equalsIgnoreCase("stats")) {
				if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
					if (arguments[1].equalsIgnoreCase("show")) {
						if (commandSender instanceof Player) {
							final Player player = (Player) commandSender;
							plugin.getStatsManager().showStats(player, player);
						} else {
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
						}
						return true;
					}
				}
			}
		} else if (arguments.length == 3) {
			if (arguments[0].equalsIgnoreCase("setup")) {
				if (arguments[1].equalsIgnoreCase("spawnpoint")) {
					if (commandSender instanceof Player) {
						if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.setup")) {
							Player player = (Player) commandSender;

							if (plugin.getBungeeCord().setup.containsKey(player.getName())) {
								if (plugin.getBungeeCord().location1 != null) {
									double x = plugin.getBungeeCord().location1.getBlockX() + 0.5;
									double y = plugin.getBungeeCord().location1.getBlockY() + 1;
									double z = plugin.getBungeeCord().location1.getBlockZ() + 0.5;

									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".spawnpoint.x", x);
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".spawnpoint.y", y);
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".spawnpoint.z", z);
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".spawnpoint.pitch", 0.0);
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".spawnpoint.yaw", 0.0);
									new BukkitRunnable() {
										public void run() {
											plugin.getConfigManager().saveConfig("maps.yml");
										}
									}.runTaskAsynchronously(plugin);

									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Plot spawnpoint location is successfully saved into configuration file for &f" + arguments[2] + " &7plot!"));

									plugin.getBungeeCord().location1 = null;
								} else {
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for the plot spawnpoint does not exist."));
								}
							}
						} else {
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
						}
					} else {
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
					}
				} else if (arguments[1].equalsIgnoreCase("plot-area")) {
					if (commandSender instanceof Player) {
						if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.setup")) {
							Player player = (Player) commandSender;

							if (plugin.getBungeeCord().setup.containsKey(player.getName())) {
								if (plugin.getBungeeCord().location2 != null && plugin.getBungeeCord().location3 != null) {
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".area.x1", plugin.getBungeeCord().location2.getBlockX());
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".area.y1", plugin.getBungeeCord().location2.getBlockY());
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".area.z1", plugin.getBungeeCord().location2.getBlockZ());
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".area.x2", plugin.getBungeeCord().location3.getBlockX());
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".area.y2", plugin.getBungeeCord().location3.getBlockY());
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".area.z2", plugin.getBungeeCord().location3.getBlockZ());
									new BukkitRunnable() {
										public void run() {
											plugin.getConfigManager().saveConfig("maps.yml");
										}
									}.runTaskAsynchronously(plugin);

									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Plot area positions are successfully saved into configuration file for &f" + arguments[2] + " &7plot!"));

									plugin.getBungeeCord().location2 = null;
									plugin.getBungeeCord().location3 = null;
								} else {
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for one of the plot area positions does not exist."));
								}
							}
						} else {
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
						}
					} else {
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
					}
				} else if (arguments[1].equalsIgnoreCase("laser")) {
					if (commandSender instanceof Player) {
						if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.setup")) {
							Player player = (Player) commandSender;

							if (plugin.getBungeeCord().setup.containsKey(player.getName())) {
								if (plugin.getBungeeCord().location1 != null) {
									double x = plugin.getBungeeCord().location1.getBlockX() + 0.5;
									double y = plugin.getBungeeCord().location1.getBlockY();
									double z = plugin.getBungeeCord().location1.getBlockZ() + 0.5;

									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".laser-beam.x", x);
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".laser-beam.y", y);
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".laser-beam.z", z);
									new BukkitRunnable() {
										public void run() {
											plugin.getConfigManager().saveConfig("maps.yml");
										}
									}.runTaskAsynchronously(plugin);

									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Laser spawnpoint location is successfully saved into configuration file for &f" + arguments[2] + " &7plot!"));

									plugin.getBungeeCord().location1 = null;
								} else {
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for the laser spawnpoint does not exist."));
								}
							}
						} else {
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
						}
					} else {
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
					}
				} else if (arguments[1].equalsIgnoreCase("build-area")) {
					if (commandSender instanceof Player) {
						if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.setup")) {
							Player player = (Player) commandSender;

							if (plugin.getBungeeCord().setup.containsKey(player.getName())) {
								if (plugin.getBungeeCord().location2 != null && plugin.getBungeeCord().location3 != null) {
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".build-area.x1", plugin.getBungeeCord().location2.getBlockX());
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".build-area.y1", plugin.getBungeeCord().location2.getBlockY());
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".build-area.z1", plugin.getBungeeCord().location2.getBlockZ());
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".build-area.x2", plugin.getBungeeCord().location3.getBlockX());
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".build-area.y2", plugin.getBungeeCord().location3.getBlockY());
									plugin.getConfigManager().getConfig("maps.yml").set("maps." + plugin.getBungeeCord().setup.get(player.getName()) + ".plots." + arguments[2] + ".build-area.z2", plugin.getBungeeCord().location3.getBlockZ());
									new BukkitRunnable() {
										public void run() {
											plugin.getConfigManager().saveConfig("maps.yml");
										}
									}.runTaskAsynchronously(plugin);

									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Build area positions are successfully saved into configuration file for &f" + arguments[2] + " &7plot!"));

									plugin.getBungeeCord().location2 = null;
									plugin.getBungeeCord().location3 = null;
								} else {
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for one of the build area positions does not exist."));
								}
							}
						} else {
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
						}
					} else {
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
					}
				}
				return true;
			} else if (arguments[0].equalsIgnoreCase("stats")) {
				if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
					if (arguments[1].equalsIgnoreCase("show")) {
						if (commandSender instanceof Player) {
							final Player player = (Player) commandSender;
							if (Bukkit.getPlayer(arguments[2]) != null) {
								plugin.getStatsManager().showStats(player, Bukkit.getPlayer(arguments[2]));
							} else {
								commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("STATS-PLAYER_IS_NOT_ONLINE").replaceAll("%PLAYER%", arguments[2])));
							}
						} else {
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
						}
						return true;
					}
				}
			}
		} else if (arguments.length >= 4) {
			if (arguments[0].equalsIgnoreCase("setup")) {
				if (arguments[1].equalsIgnoreCase("template")) {
					if (commandSender instanceof Player) {
						if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.setup")) {
							Player player = (Player) commandSender;

							if (plugin.getBungeeCord().setup.containsKey(player.getName())) {
								if (plugin.getBungeeCord().blocks.containsKey(player.getName())) {
									String raw = arguments[2];
									String display = "";
									for (int i = 3; i < arguments.length; i++) {
										display = display + " " + arguments[i];
									}
									display = display.replaceFirst(" ", "");

									plugin.getBungeeCord().getTemplateManager().saveTemplate(raw, display, plugin.getBungeeCord().blocks.get(player.getName()));

									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Blocks for template are successfully saved into configuration file."));
									plugin.getBungeeCord().blocks.remove(player.getName());
								} else {
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Blocks for the template does not exist."));
								}
							}
						} else {
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
						}
					} else {
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
					}
				}
				return true;
			}
		}

		commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lSpeedBuilders v" + plugin.getDescription().getVersion()));
		commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb reload &8- &7" + translate("HMENU-RELOAD")));
		commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb setup &8- &7" + translate("HMENU-SETUP_THE_GAME")));
		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb stats &8- &7" + translate("HMENU-SHOW_STATS")));
		}
		return true;
	}
}
