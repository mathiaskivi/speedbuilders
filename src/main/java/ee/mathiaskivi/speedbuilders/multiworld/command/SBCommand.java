package ee.mathiaskivi.speedbuilders.multiworld.command;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.multiworld.Arena;
import ee.mathiaskivi.speedbuilders.utility.GameState;
import ee.mathiaskivi.speedbuilders.utility.Translations;
import ee.mathiaskivi.speedbuilders.utility.VoidGenerator;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

@SuppressWarnings("deprecation")
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
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb arena &8- &7" + translate("HMENU-MANAGE_ARENAS")));
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb autojoin &8- &7" + translate("HMENU-AUTOJOIN_ARENA")));
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb join <arenaName> &8- &7" + translate("HMENU-JOIN_ARENA")));
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb leave &8- &7" + translate("HMENU-LEAVE_ARENA")));
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb reload &8- &7" + translate("HMENU-RELOAD")));
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb setup &8- &7" + translate("HMENU-SETUP_THE_GAME")));
			if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
				commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb stats &8- &7" + translate("HMENU-SHOW_STATS")));
			}
			return true;
		} else if (arguments.length == 1) {
			if (arguments[0].equalsIgnoreCase("arena")) {
				commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lSpeedBuilders v" + plugin.getDescription().getVersion()));
				commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb arena create <arenaName> <startTime> <gameStartTime> <showCaseTime> <buildTime> <judgeTime> <neededPlayers>"));
				commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb arena delete <arenaName>"));
				return true;
			} else if (arguments[0].equalsIgnoreCase("autojoin")) {
				if (commandSender instanceof Player) {
					Player player = (Player) commandSender;

					boolean found = false;
					for (Arena arena : Arena.arenaObjects) {
						if (arena.getPlayers().contains(player.getName())) {
							found = true;
							break;
						}
					}
					if (!found) {
						boolean foundJoinableArena = false;
						for (Arena arena : Arena.arenaObjects) {
							String[] folders = new File(".").list(new FilenameFilter() {
								public boolean accept(File current, String name) {
									return new File(current, name).isDirectory();
								}
							});

							found = false;
							for (String folder : folders) {
								if (folder.equals(arena.getName())) {
									found = true;
									break;
								}
							}

							if (found) {
								if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) {
									if (!(arena.getPlayers().size() >= arena.getMaxPlayers())) {
										foundJoinableArena = true;
										plugin.getMultiWorld().saveTempInfo(player);
										plugin.getMultiWorld().getArenaManager().addPlayer(player, arena.getName());
										break;
									} else if (player.hasPermission("sb.server.joinfullgame")) {
										plugin.getMultiWorld().saveTempInfo(player);
										plugin.getMultiWorld().getArenaManager().addPlayer(player, arena.getName());

										List<Player> noPermUsers = new ArrayList<>();
										for (String arenaPlayer : arena.getPlayers()) {
											if (Bukkit.getPlayer(arenaPlayer) != player && !Bukkit.getPlayer(arenaPlayer).hasPermission("sb.server.joinfullgame")) {
												noPermUsers.add(Bukkit.getPlayer(arenaPlayer));
											}
										}

										if (!noPermUsers.isEmpty()) {
											Player kickPlayer = noPermUsers.get(new Random().nextInt(noPermUsers.size()));
											arena.getPlayers().remove(kickPlayer.getName());

											if (arena.getGameState() == GameState.WAITING) {
												for (String arenaPlayer : arena.getPlayers()) {
													if (arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName()) != null) {
														Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName());
														Objective objective = scoreboard.getObjective("SpeedBuilders");
														for (String entry : scoreboard.getEntries()) {
															scoreboard.resetScores(entry);
														}
														if (arena.getGameState() == GameState.WAITING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
														}
														if (arena.getGameState() == GameState.STARTING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
														}
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
														Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
													} else {
														ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
														Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
														Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
														if (arena.getGameState() == GameState.WAITING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
														}
														if (arena.getGameState() == GameState.STARTING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
														}
														objective.setDisplaySlot(DisplaySlot.SIDEBAR);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
														Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
														arena.getPlayerStartScoreboard().put(Bukkit.getPlayer(arenaPlayer).getName(), scoreboard);
													}
												}

												arena.getGameScoreboard().getPlayerTeam(kickPlayer).removePlayer(kickPlayer);
												plugin.getMultiWorld().getKitManager().setKit(kickPlayer, null, arena.getName());

												if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
													Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
													location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
													location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
													kickPlayer.teleport(location);
												} else {
													kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
												}

												kickPlayer.getInventory().setArmorContents(null);
												kickPlayer.getInventory().clear();
												kickPlayer.setAllowFlight(false);
												kickPlayer.setExp(0);
												kickPlayer.setFireTicks(0);
												kickPlayer.setFlying(false);
												kickPlayer.setFoodLevel(20);
												kickPlayer.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
												kickPlayer.setHealth(20);
												kickPlayer.setLevel(0);
												for (PotionEffect potionEffect : kickPlayer.getActivePotionEffects()) {
													kickPlayer.removePotionEffect(potionEffect.getType());
												}

												if (plugin.getMultiWorld().playerTempHealth.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(kickPlayer.getName())) {
													plugin.getMultiWorld().loadTempInfo(kickPlayer);
												}
												kickPlayer.updateInventory();
											} else if (arena.getGameState() == GameState.STARTING) {
												for (String arenaPlayer : arena.getPlayers()) {
													if (arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName()) != null) {
														Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName());
														Objective objective = scoreboard.getObjective("SpeedBuilders");
														for (String entry : scoreboard.getEntries()) {
															scoreboard.resetScores(entry);
														}
														if (arena.getGameState() == GameState.WAITING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
														}
														if (arena.getGameState() == GameState.STARTING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
														}
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
														Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
													} else {
														ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
														Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
														Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
														if (arena.getGameState() == GameState.WAITING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
														}
														if (arena.getGameState() == GameState.STARTING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
														}
														objective.setDisplaySlot(DisplaySlot.SIDEBAR);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
														Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
														arena.getPlayerStartScoreboard().put(Bukkit.getPlayer(arenaPlayer).getName(), scoreboard);
													}
												}

												arena.getGameScoreboard().getPlayerTeam(kickPlayer).removePlayer(kickPlayer);
												plugin.getMultiWorld().getKitManager().setKit(kickPlayer, null, arena.getName());

												if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
													Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
													location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
													location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
													kickPlayer.teleport(location);
												} else {
													kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
												}

												kickPlayer.getInventory().setArmorContents(null);
												kickPlayer.getInventory().clear();
												kickPlayer.setAllowFlight(false);
												kickPlayer.setExp(0);
												kickPlayer.setFireTicks(0);
												kickPlayer.setFlying(false);
												kickPlayer.setFoodLevel(20);
												kickPlayer.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
												kickPlayer.setHealth(20);
												kickPlayer.setLevel(0);
												for (PotionEffect potionEffect : kickPlayer.getActivePotionEffects()) {
													kickPlayer.removePotionEffect(potionEffect.getType());
												}

												if (plugin.getMultiWorld().playerTempHealth.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(kickPlayer.getName())) {
													plugin.getMultiWorld().loadTempInfo(kickPlayer);
												}
												kickPlayer.updateInventory();
											}
										}
										noPermUsers.clear();
										break;
									}
								}
							}
						}
						if (!foundJoinableArena) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_JOINABLE_ARENAS")));
						}
					} else {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYING")));
					}
				} else {
					commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
				}
				return true;
			} else if (arguments[0].equalsIgnoreCase("leave")) {
				if (commandSender instanceof Player) {
					Player player = (Player) commandSender;
					boolean found = false;
					for (Arena arena : Arena.arenaObjects) {
						if (arena.getPlayers().contains(player.getName())) {
							found = true;
							plugin.getMultiWorld().getArenaManager().removePlayer(player, arena.getName());
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT").replaceAll("%PLAYER%", player.getName())));
							break;
						}
					}
					if (!found) {
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NOT_PLAYING")));
					}
				} else {
					commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
				}
				return true;
			} else if (arguments[0].equalsIgnoreCase("reload")) {
				if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.reload")) {
					plugin.getConfigManager().reloadConfig("arenas.yml");
					plugin.getConfigManager().reloadConfig("config.yml");
					plugin.getConfigManager().reloadConfig("lobby.yml");
					plugin.getConfigManager().reloadConfig("messages.yml");
					plugin.getConfigManager().reloadConfig("signs.yml");
					plugin.getConfigManager().reloadConfig("templates.yml");
					plugin.getConfigManager().reloadConfig("spigot.yml");

					for (String message : plugin.getConfigManager().getConfig("messages.yml").getConfigurationSection("MULTIWORLD").getKeys(false)) {
						Translations.messages.put(message, plugin.getConfigManager().getConfig("messages.yml").getString("MULTIWORLD." + message));
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

						if (plugin.getMultiWorld().setup.containsKey(player.getName())) {
							Inventory inventory = Bukkit.createInventory(player, InventoryType.CHEST, ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY") + "&8Setup"));

							ItemStack itemStack1 = new ItemStack(Material.BEACON);
							ItemMeta itemMeta1 = itemStack1.getItemMeta();
							itemMeta1.setDisplayName("§r§fSet spawnpoint location for arena lobby");
							itemStack1.setItemMeta(itemMeta1);

							ItemStack itemStack2 = new ItemStack(Material.GUARDIAN_SPAWN_EGG);
							ItemMeta itemMeta2 = itemStack2.getItemMeta();
							itemMeta2.setDisplayName("§r§fSet spawnpoint location for guardian plot");
							itemStack2.setItemMeta(itemMeta2);

							ItemStack itemStack3 = new ItemStack(Material.PAPER);
							ItemMeta itemMeta3 = itemStack3.getItemMeta();
							itemMeta3.setDisplayName("§r§fSet template area positions for guardian plot");
							itemStack3.setItemMeta(itemMeta3);

							ItemStack itemStack4 = new ItemStack(Material.WHITE_BED);
							ItemMeta itemMeta4 = itemStack4.getItemMeta();
							itemMeta4.setDisplayName("§r§fSet spawnpoint location for each player plot");
							itemStack4.setItemMeta(itemMeta4);

							ItemStack itemStack5 = new ItemStack(Material.WOODEN_AXE);
							ItemMeta itemMeta5 = itemStack5.getItemMeta();
							itemMeta5.setDisplayName("§r§fSet plot area positions for each player plot");
							itemStack5.setItemMeta(itemMeta5);

							ItemStack itemStack6 = new ItemStack(Material.ARMOR_STAND);
							ItemMeta itemMeta6 = itemStack6.getItemMeta();
							itemMeta6.setDisplayName("§r§fSet laser location for each player plot");
							itemStack6.setItemMeta(itemMeta6);

							ItemStack itemStack7 = new ItemStack(Material.OAK_FENCE);
							ItemMeta itemMeta7 = itemStack7.getItemMeta();
							itemMeta7.setDisplayName("§r§fSet build area positions for each player plot");
							itemStack7.setItemMeta(itemMeta7);

							ItemStack itemStack8 = new ItemStack(Material.WRITABLE_BOOK);
							ItemMeta itemMeta8 = itemStack8.getItemMeta();
							itemMeta8.setDisplayName("§r§fAdd build template(s)");
							itemStack8.setItemMeta(itemMeta8);

							ItemStack itemStack9 = new ItemStack(Material.CLOCK);
							ItemMeta itemMeta9 = itemStack9.getItemMeta();
							itemMeta9.setDisplayName("§r§fReturn to lobby");
							itemStack9.setItemMeta(itemMeta9);

							inventory.addItem(itemStack1, itemStack2, itemStack3, itemStack4, itemStack5, itemStack6, itemStack7, itemStack8);
							inventory.setItem(22, itemStack9);
							player.openInventory(inventory);
						} else {
							Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY") + "&8Setup"));

							ItemStack itemStack1 = new ItemStack(Material.NETHER_STAR);
							ItemMeta itemMeta1 = itemStack1.getItemMeta();
							itemMeta1.setDisplayName("§r§fSet spawnpoint location for main lobby");
							itemStack1.setItemMeta(itemMeta1);

							ItemStack itemStack2 = new ItemStack(Material.PODZOL);
							ItemMeta itemMeta2 = itemStack2.getItemMeta();
							itemMeta2.setDisplayName("§r§fEdit arena");
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
			if (arguments[0].equalsIgnoreCase("join")) {
				final Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arguments[1]);

				if (arena != null) {
					if (commandSender instanceof Player) {
						Player player = (Player) commandSender;
						boolean found = false;
						for (Arena arenaObject : Arena.arenaObjects) {
							if (arenaObject.getPlayers().contains(player.getName())) {
								found = true;
							}
						}
						if (!found) {
							String[] folders = new File(".").list(new FilenameFilter() {
								public boolean accept(File current, String name) {
									return new File(current, name).isDirectory();
								}
							});

							found = false;
							for (String folder : folders) {
								if (folder.equals(arena.getName())) {
									found = true;
									break;
								}
							}

							if (found) {
								if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) {
									if (!(arena.getPlayers().size() >= arena.getMaxPlayers())) {
										plugin.getMultiWorld().saveTempInfo(player);
										plugin.getMultiWorld().getArenaManager().addPlayer(player, arena.getName());
									} else if (player.hasPermission("sb.server.joinfullgame")) {
										plugin.getMultiWorld().saveTempInfo(player);
										plugin.getMultiWorld().getArenaManager().addPlayer(player, arena.getName());

										List<Player> noPermUsers = new ArrayList<>();
										for (String arenaPlayer : arena.getPlayers()) {
											if (Bukkit.getPlayer(arenaPlayer) != player && !Bukkit.getPlayer(arenaPlayer).hasPermission("sb.server.joinfullgame")) {
												noPermUsers.add(Bukkit.getPlayer(arenaPlayer));
											}
										}

										if (!noPermUsers.isEmpty()) {
											Player kickPlayer = noPermUsers.get(new Random().nextInt(noPermUsers.size()));
											arena.getPlayers().remove(kickPlayer.getName());

											if (arena.getGameState() == GameState.WAITING) {
												for (String arenaPlayer : arena.getPlayers()) {
													if (arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName()) != null) {
														Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName());
														Objective objective = scoreboard.getObjective("SpeedBuilders");
														for (String entry : scoreboard.getEntries()) {
															scoreboard.resetScores(entry);
														}
														if (arena.getGameState() == GameState.WAITING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
														}
														if (arena.getGameState() == GameState.STARTING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
														}
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
														Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
													} else {
														ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
														Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
														Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
														if (arena.getGameState() == GameState.WAITING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
														}
														if (arena.getGameState() == GameState.STARTING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
														}
														objective.setDisplaySlot(DisplaySlot.SIDEBAR);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
														Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
														arena.getPlayerStartScoreboard().put(Bukkit.getPlayer(arenaPlayer).getName(), scoreboard);
													}
												}

												arena.getGameScoreboard().getPlayerTeam(kickPlayer).removePlayer(kickPlayer);
												plugin.getMultiWorld().getKitManager().setKit(kickPlayer, null, arena.getName());

												if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
													Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
													location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
													location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
													kickPlayer.teleport(location);
												} else {
													kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
												}

												kickPlayer.getInventory().setArmorContents(null);
												kickPlayer.getInventory().clear();
												kickPlayer.setAllowFlight(false);
												kickPlayer.setExp(0);
												kickPlayer.setFireTicks(0);
												kickPlayer.setFlying(false);
												kickPlayer.setFoodLevel(20);
												kickPlayer.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
												kickPlayer.setHealth(20);
												kickPlayer.setLevel(0);
												for (PotionEffect potionEffect : kickPlayer.getActivePotionEffects()) {
													kickPlayer.removePotionEffect(potionEffect.getType());
												}

												if (plugin.getMultiWorld().playerTempHealth.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(kickPlayer.getName())) {
													plugin.getMultiWorld().loadTempInfo(kickPlayer);
												}
												kickPlayer.updateInventory();
											} else if (arena.getGameState() == GameState.STARTING) {
												for (String arenaPlayer : arena.getPlayers()) {
													if (arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName()) != null) {
														Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName());
														Objective objective = scoreboard.getObjective("SpeedBuilders");
														for (String entry : scoreboard.getEntries()) {
															scoreboard.resetScores(entry);
														}
														if (arena.getGameState() == GameState.WAITING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
														}
														if (arena.getGameState() == GameState.STARTING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
														}
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
														Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
													} else {
														ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
														Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
														Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
														if (arena.getGameState() == GameState.WAITING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
														}
														if (arena.getGameState() == GameState.STARTING) {
															objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
														}
														objective.setDisplaySlot(DisplaySlot.SIDEBAR);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
														objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
														Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
														arena.getPlayerStartScoreboard().put(Bukkit.getPlayer(arenaPlayer).getName(), scoreboard);
													}
												}

												arena.getGameScoreboard().getPlayerTeam(kickPlayer).removePlayer(kickPlayer);
												plugin.getMultiWorld().getKitManager().setKit(kickPlayer, null, arena.getName());

												if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
													Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
													location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
													location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
													kickPlayer.teleport(location);
												} else {
													kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
												}

												kickPlayer.getInventory().setArmorContents(null);
												kickPlayer.getInventory().clear();
												kickPlayer.setAllowFlight(false);
												kickPlayer.setExp(0);
												kickPlayer.setFireTicks(0);
												kickPlayer.setFlying(false);
												kickPlayer.setFoodLevel(20);
												kickPlayer.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
												kickPlayer.setHealth(20);
												kickPlayer.setLevel(0);
												for (PotionEffect potionEffect : kickPlayer.getActivePotionEffects()) {
													kickPlayer.removePotionEffect(potionEffect.getType());
												}

												if (plugin.getMultiWorld().playerTempHealth.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(kickPlayer.getName())) {
													plugin.getMultiWorld().loadTempInfo(kickPlayer);
												}
												kickPlayer.updateInventory();
											}
										}
										noPermUsers.clear();
									} else {
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-ARENA_IS_FULL")));
									}
								} else {
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-GAME_RUNNING")));
								}
							} else {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Arena &f" + arena.getName() + " &7is missing a world folder!"));
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Shut down the server and upload the world to this server!"));
							}
						} else {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYING")));
						}
					} else {
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYER_COMMAND")));
					}
				} else {
					commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-ARENA_DOES_NOT_EXIST")));
				}
				return true;
			} else if (arguments[0].equalsIgnoreCase("stats")) {
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
			if (arguments[0].equalsIgnoreCase("arena")) {
				if (arguments[1].equalsIgnoreCase("delete")) {
					if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.arena.*") || commandSender.hasPermission("sb.command.arena.delete")) {
						plugin.getMultiWorld().getArenaManager().deleteArena(arguments[2]);

						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Arena &f" + arguments[2] + " &7is successfully deleted from configuration file!"));
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Checking for arena world folder. Please wait..."));

						String[] folders = new File(".").list(new FilenameFilter() {
							public boolean accept(File current, String name) {
								return new File(current, name).isDirectory();
							}
						});

						boolean found = false;
						for (String folder : folders) {
							if (folder.equals(arguments[2])) {
								found = true;
								break;
							}
						}

						if (found) {
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Arena &f" + arguments[2] + " &7has a world folder!"));
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Shut down the server and delete the world folder from this server!"));
						} else {
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Arena &f" + arguments[2] + " &7does not have a world folder."));
						}
					} else {
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
					}
					return true;
				}
			} else if (arguments[0].equalsIgnoreCase("setup")) {
				if (arguments[1].equalsIgnoreCase("spawnpoint")) {
					if (commandSender instanceof Player) {
						if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.setup")) {
							Player player = (Player) commandSender;
							Arena arena = plugin.getMultiWorld().getArenaManager().getArena(plugin.getMultiWorld().setup.get(player.getName()));

							if (arena != null) {
								if (plugin.getMultiWorld().location1 != null) {
									double x = plugin.getMultiWorld().location1.getBlockX() + 0.5;
									double y = plugin.getMultiWorld().location1.getBlockY() + 1;
									double z = plugin.getMultiWorld().location1.getBlockZ() + 0.5;

									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".spawnpoint.x", x);
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".spawnpoint.y", y);
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".spawnpoint.z", z);
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".spawnpoint.pitch", 0.0);
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".spawnpoint.yaw", 0.0);
									new BukkitRunnable() {
										public void run() {
											plugin.getConfigManager().saveConfig("arenas.yml");
										}
									}.runTaskAsynchronously(plugin);

									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Plot spawnpoint location is successfully saved into configuration file for &f" + arguments[2] + " &7plot!"));

									plugin.getMultiWorld().location1 = null;
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
							Arena arena = plugin.getMultiWorld().getArenaManager().getArena(plugin.getMultiWorld().setup.get(player.getName()));

							if (arena != null) {
								if (plugin.getMultiWorld().location2 != null && plugin.getMultiWorld().location3 != null) {
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".area.x1", plugin.getMultiWorld().location2.getBlockX());
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".area.y1", plugin.getMultiWorld().location2.getBlockY());
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".area.z1", plugin.getMultiWorld().location2.getBlockZ());
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".area.x2", plugin.getMultiWorld().location3.getBlockX());
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".area.y2", plugin.getMultiWorld().location3.getBlockY());
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".area.z2", plugin.getMultiWorld().location3.getBlockZ());
									new BukkitRunnable() {
										public void run() {
											plugin.getConfigManager().saveConfig("arenas.yml");
										}
									}.runTaskAsynchronously(plugin);

									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Plot area positions are successfully saved into configuration file for &f" + arguments[2] + " &7plot!"));

									plugin.getMultiWorld().location2 = null;
									plugin.getMultiWorld().location3 = null;
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
							Arena arena = plugin.getMultiWorld().getArenaManager().getArena(plugin.getMultiWorld().setup.get(player.getName()));

							if (arena != null) {
								if (plugin.getMultiWorld().location1 != null) {
									double x = plugin.getMultiWorld().location1.getBlockX() + 0.5;
									double y = plugin.getMultiWorld().location1.getBlockY();
									double z = plugin.getMultiWorld().location1.getBlockZ() + 0.5;

									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".laser-beam.x", x);
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".laser-beam.y", y);
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".laser-beam.z", z);
									new BukkitRunnable() {
										public void run() {
											plugin.getConfigManager().saveConfig("arenas.yml");
										}
									}.runTaskAsynchronously(plugin);

									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Laser spawnpoint location is successfully saved into configuration file for &f" + arguments[2] + " &7plot!"));

									plugin.getMultiWorld().location1 = null;
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
							Arena arena = plugin.getMultiWorld().getArenaManager().getArena(plugin.getMultiWorld().setup.get(player.getName()));

							if (arena != null) {
								if (plugin.getMultiWorld().location2 != null && plugin.getMultiWorld().location3 != null) {
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".build-area.x1", plugin.getMultiWorld().location2.getBlockX());
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".build-area.y1", plugin.getMultiWorld().location2.getBlockY());
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".build-area.z1", plugin.getMultiWorld().location2.getBlockZ());
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".build-area.x2", plugin.getMultiWorld().location3.getBlockX());
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".build-area.y2", plugin.getMultiWorld().location3.getBlockY());
									plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + arguments[2] + ".build-area.z2", plugin.getMultiWorld().location3.getBlockZ());
									new BukkitRunnable() {
										public void run() {
											plugin.getConfigManager().saveConfig("arenas.yml");
										}
									}.runTaskAsynchronously(plugin);

									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Build area positions are successfully saved into configuration file for &f" + arguments[2] + " &7plot!"));

									plugin.getMultiWorld().location2 = null;
									plugin.getMultiWorld().location3 = null;
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
		} else if (arguments.length == 9) {
			if (arguments[0].equalsIgnoreCase("arena")) {
				if (arguments[1].equalsIgnoreCase("create")) {
					if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.arena.*") || commandSender.hasPermission("sb.command.arena.create")) {
						boolean containsArena = false;
						if (Arena.arenaObjects.contains(plugin.getMultiWorld().getArenaManager().getArena(arguments[2]))) {
							containsArena = true;
						}

						if (!containsArena) {
							String[] folders = new File(".").list(new FilenameFilter() {
								public boolean accept(File current, String name) {
									return new File(current, name).isDirectory();
								}
							});

							boolean found = false;
							for (String folder : folders) {
								if (folder.equals(arguments[2])) {
									found = true;
									break;
								}
							}

							if (found) {
								WorldCreator worldCreator = new WorldCreator(arguments[2]);
								worldCreator.generator(new VoidGenerator());
								Bukkit.createWorld(worldCreator);

								String arenaName = arguments[2];
								int startTime = Integer.parseInt(arguments[3]);
								int gameStartTime = Integer.parseInt(arguments[4]);
								int showCaseTime = Integer.parseInt(arguments[5]);
								int buildTime = Integer.parseInt(arguments[6]);
								int judgeTime = Integer.parseInt(arguments[7]);
								int neededPlayers = Integer.parseInt(arguments[8]);

								plugin.getMultiWorld().getArenaManager().createArena(arenaName, startTime, gameStartTime, showCaseTime, buildTime, judgeTime, neededPlayers);
								commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Arena is successfully saved into configuration file!"));
								commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Type &f/sb setup &7to start setting up your arena."));
							} else {
								commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7World folder for this arena can not be found!"));
								commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Make sure that arena name matches with the world name!"));
								commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Shut down the server and upload the world to this server!"));
							}
						} else {
							commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7That arena already exists!"));
						}
					} else {
						commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
					}
					return true;
				}
			}
		} else if (arguments.length >= 4) {
			if (arguments[0].equalsIgnoreCase("setup")) {
				if (arguments[1].equalsIgnoreCase("template")) {
					if (commandSender instanceof Player) {
						if (commandSender.hasPermission("sb.command.*") || commandSender.hasPermission("sb.command.setup")) {
							Player player = (Player) commandSender;
							Arena arena = plugin.getMultiWorld().getArenaManager().getArena(plugin.getMultiWorld().setup.get(player.getName()));

							if (arena != null) {
								if (plugin.getMultiWorld().blocks.containsKey(player.getName())) {
									String raw = arguments[2];
									String display = "";
									for (int i = 3; i < arguments.length; i++) {
										display = display + " " + arguments[i];
									}
									display = display.replaceFirst(" ", "");

									plugin.getMultiWorld().getTemplateManager().saveTemplate(raw, display, plugin.getMultiWorld().blocks.get(player.getName()));

									player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Blocks for template are successfully saved into configuration file."));
									plugin.getMultiWorld().blocks.remove(player.getName());
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
		commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb arena &8- &7" + translate("HMENU-MANAGE_ARENAS")));
		commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb autojoin &8- &7" + translate("HMENU-AUTOJOIN_ARENA")));
		commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb join <arenaName> &8- &7" + translate("HMENU-JOIN_ARENA")));
		commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb leave &8- &7" + translate("HMENU-LEAVE_ARENA")));
		commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb reload &8- &7" + translate("HMENU-RELOAD")));
		commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb setup &8- &7" + translate("HMENU-SETUP_THE_GAME")));
		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f- &7/sb stats &8- &7" + translate("HMENU-SHOW_STATS")));
		}
		return true;
	}
}
