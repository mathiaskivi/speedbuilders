package ee.mathiaskivi.speedbuilders.bungeecord;

import com.google.common.base.Splitter;
import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.api.event.GameStateChangeEvent;
import ee.mathiaskivi.speedbuilders.bungeecord.command.SBCommand;
import ee.mathiaskivi.speedbuilders.bungeecord.listener.*;
import ee.mathiaskivi.speedbuilders.bungeecord.manager.*;
import ee.mathiaskivi.speedbuilders.utility.GameState;
import ee.mathiaskivi.speedbuilders.utility.Translations;
import ee.mathiaskivi.speedbuilders.utility.VoidGenerator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

@SuppressWarnings("deprecation")
public class BungeeCord {
	public SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public String currentMap;

	public GuardianManager guardianManager;
	public KitManager kitManager;
	public TemplateManager templateManager;
	public TimerManager timerManager;

	public Scoreboard gameScoreboard;

	public ArmorStand judgedPlayerArmorStand = null;
	public ArrayList<String> unusedTemplates = new ArrayList<>();
	public ArrayList<String> usedTemplates = new ArrayList<>();
	public HashMap<Integer, String> currentBuildBlocks = new HashMap<>();
	public HashMap<String, Float> playersDoubleJumpCooldowned = new HashMap<>();
	public HashMap<String, Integer> playerPercent = new HashMap<>();
	public HashMap<String, Scoreboard> playerStartScoreboard = new HashMap<>();
	public HashMap<String, String> playersKit = new HashMap<>();
	public HashMap<String, String> plots = new HashMap<>();
	public int buildTimeSubtractor = 0;
	public int currentRound = 0;
	public int maxPlayers = 0;
	public ElderGuardian elderGuardian = null;
	public String currentBuildDisplayName = null;
	public String currentBuildRawName = null;
	public String judgedPlayerName = null;
	public String firstPlace = null;
	public String secondPlace = null;
	public String thirdPlace = null;
	public GameState gameState;

	public HashMap<String, String> setup = new HashMap<>();
	public Location location1 = null;
	public Location location2 = null;
	public Location location3 = null;
	public HashMap<String, List<Block>> blocks = new HashMap<>();

	public List<String> winnerCommands = new ArrayList<>();
	public List<String> loserCommands = new ArrayList<>();

	public void onEnable() {
		File folder = new File("plugins/" + plugin.getDescription().getName());
		if (!folder.exists()) {
			folder.mkdirs();
		}

		plugin.getConfigManager().loadConfig("lobby.yml");
		plugin.getConfigManager().loadConfig("maps.yml");
		plugin.getConfigManager().loadConfig("messages.yml");
		plugin.getConfigManager().loadConfig("templates.yml");
		plugin.getConfigManager().loadConfig("spigot.yml", new File("."));

		File mapsFolder = new File("plugins/" + plugin.getDescription().getName() + "/maps");
		if (!mapsFolder.exists()) {
			mapsFolder.mkdirs();
		}

		String[] allMapFolders = mapsFolder.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		ArrayList<String> allMaps = new ArrayList<>();
		try {
			for (String allMapFolder : allMapFolders) {
				allMaps.add(allMapFolder);
			}

			if (allMaps.size() > 1 && allMaps.contains(plugin.getConfigManager().getConfig("lobby.yml").getString("previous-map"))) {
				allMaps.remove(plugin.getConfigManager().getConfig("lobby.yml").getString("previous-map"));
			}

			currentMap = allMaps.get(new Random().nextInt(allMaps.size()));
		} catch (IllegalArgumentException | NullPointerException ex) {
			Bukkit.getLogger().info("");
			Bukkit.getLogger().severe("[SpeedBuilders] Maps for SpeedBuilders plugin can not be found!");
			Bukkit.getLogger().severe("[SpeedBuilders] Follow this instruction to fix this problem:");
			Bukkit.getLogger().severe("[SpeedBuilders] Copy and paste your maps to the plugin's maps folder (../plugins/SpeedBuilders/maps),");
			Bukkit.getLogger().severe("[SpeedBuilders] and to the server's root folder.");
			Bukkit.getLogger().info("");
			Bukkit.getPluginManager().disablePlugin(plugin);
			return;
		}
		Bukkit.createWorld(new WorldCreator(currentMap).generator(new VoidGenerator()));

		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
		Bukkit.getPluginCommand("speedbuilders").setExecutor(new SBCommand());
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
		Bukkit.getPluginManager().registerEvents(new ServerListener(), plugin);
		Bukkit.getPluginManager().registerEvents(new SetupListener1(), plugin);
		Bukkit.getPluginManager().registerEvents(new SetupListener2(), plugin);
		Bukkit.getPluginManager().registerEvents(new WorldListener(), plugin);

		guardianManager = new GuardianManager();
		kitManager = new KitManager();
		templateManager = new TemplateManager();
		timerManager = new TimerManager();

		plugin.getStatsManager().openConnection();

		for (String message : plugin.getConfigManager().getConfig("messages.yml").getConfigurationSection("BUNGEECORD").getKeys(false)) {
			Translations.messages.put(message, plugin.getConfigManager().getConfig("messages.yml").getString("BUNGEECORD." + message));
		}
		unusedTemplates.clear();
		usedTemplates.clear();
		playersDoubleJumpCooldowned.clear();
		playersKit.clear();
		playerPercent.clear();
		plots.clear();
		playerStartScoreboard.clear();
		buildTimeSubtractor = 0;
		currentRound = 0;
		maxPlayers = 0;
		currentBuildDisplayName = translate("MAIN-NONE");
		currentBuildRawName = translate("MAIN-NONE");
		firstPlace = translate("MAIN-NONE");
		secondPlace = translate("MAIN-NONE");
		thirdPlace = translate("MAIN-NONE");
		gameState = GameState.WAITING;

		Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(GameState.WAITING, Bukkit.getOnlinePlayers().size()));

		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("winner-commands.enabled")) {
			winnerCommands = plugin.getConfigManager().getConfig("config.yml").getStringList("winner-commands.commands");
		}
		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("loser-commands.enabled")) {
			loserCommands = plugin.getConfigManager().getConfig("config.yml").getStringList("loser-commands.commands");
		}

		try {
			for (String plotName : plugin.getConfigManager().getConfig("maps.yml").getConfigurationSection("maps." + currentMap + ".plots").getKeys(false)) {
				if (plugin.getConfigManager().getConfig("maps.yml").contains("maps." + currentMap + ".plots." + plotName + ".spawnpoint") && plugin.getConfigManager().getConfig("maps.yml").contains("maps." + currentMap + ".plots." + plotName + ".area") && plugin.getConfigManager().getConfig("maps.yml").contains("maps." + currentMap + ".plots." + plotName + ".laser-beam") && plugin.getConfigManager().getConfig("maps.yml").contains("maps." + currentMap + ".plots." + plotName + ".build-area") && plugin.getConfigManager().getConfig("maps.yml").contains("maps." + currentMap + ".plots." + plotName + ".blocks")) {
					maxPlayers++;
				}
			}
		} catch (NullPointerException ignored) {}
		if (maxPlayers == 0) {
			maxPlayers = 1;
		}

		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		gameScoreboard = scoreboardManager.getNewScoreboard();
		Objective objective = gameScoreboard.registerNewObjective("SpeedBuilders", "dummy");
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-SPEEDBUILDERS")));
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		Team playersTeam = gameScoreboard.registerNewTeam("Players");
		Team guardiansTeam = gameScoreboard.registerNewTeam("Guardians");

		playersTeam.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e"));
		guardiansTeam.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e"));

		new BukkitRunnable() {
			public void run() {
				if (Bukkit.getOnlinePlayers().size() > 0) {
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
							Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
							location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
							location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
							onlinePlayer.teleport(location);
						} else {
							onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
						}

						gameScoreboard.getTeam("Players").addPlayer(onlinePlayer);
						kitManager.setKit(onlinePlayer, "None");

						onlinePlayer.getInventory().setArmorContents(null);
						onlinePlayer.getInventory().clear();
						onlinePlayer.setAllowFlight(false);
						onlinePlayer.setExp(0);
						onlinePlayer.setFireTicks(0);
						onlinePlayer.setFlying(false);
						onlinePlayer.setFoodLevel(20);
						onlinePlayer.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
						onlinePlayer.setHealth(20);
						onlinePlayer.setLevel(0);
						for (PotionEffect potionEffect : onlinePlayer.getActivePotionEffects()) {
							onlinePlayer.removePotionEffect(potionEffect.getType());
						}

						ItemStack itemStack1 = new ItemStack(Material.CLOCK);
						ItemMeta itemMeta1 = itemStack1.getItemMeta();
						itemMeta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")));
						itemStack1.setItemMeta(itemMeta1);

						if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
							ItemStack itemStack2 = new ItemStack(Material.BOOK);
							ItemMeta itemMeta2 = itemStack2.getItemMeta();
							itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-STATS_ITEM")));
							itemStack2.setItemMeta(itemMeta2);

							onlinePlayer.getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("stats-item-slot") - 1, itemStack2);
						}
						onlinePlayer.getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("lobby-item-slot") - 1, itemStack1);
						onlinePlayer.updateInventory();

						if (playerStartScoreboard.get(onlinePlayer.getName()) != null) {
							Scoreboard scoreboard = playerStartScoreboard.get(onlinePlayer.getName());
							Objective objective = scoreboard.getObjective("SpeedBuilders");
							for (String entry : scoreboard.getEntries()) {
								scoreboard.resetScores(entry);
							}
							if (gameState == GameState.WAITING) {
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
							}
							if (gameState == GameState.STARTING) {
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", timerManager.timeString(timerManager.getStartTime()))));
							}
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', Bukkit.getOnlinePlayers().size() + "/" + maxPlayers), scoreboard)).setScore(4);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + kitManager.getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
							onlinePlayer.setScoreboard(scoreboard);
						} else {
							ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
							Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
							Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
							if (gameState == GameState.WAITING) {
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
							}
							if (gameState == GameState.STARTING) {
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", timerManager.timeString(timerManager.getStartTime()))));
							}
							objective.setDisplaySlot(DisplaySlot.SIDEBAR);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', Bukkit.getOnlinePlayers().size() + "/" + maxPlayers), scoreboard)).setScore(4);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + kitManager.getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
							onlinePlayer.setScoreboard(scoreboard);
							playerStartScoreboard.put(onlinePlayer.getName(), scoreboard);
						}
					}

					int neededPlayers = plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.needed-players");
					if (Bukkit.getOnlinePlayers().size() >= neededPlayers) {
						timerManager.startTimer();
					}
				}

				plugin.getConfigManager().getConfig("lobby.yml").set("previous-map", currentMap);
				new BukkitRunnable() {
					public void run() {
						plugin.getConfigManager().saveConfig("lobby.yml");
					}
				}.runTaskAsynchronously(plugin);
			}
		}.runTaskLater(plugin, 2L);
	}

	public void onDisable() {
	}

	public GuardianManager getGuardianManager() {
		return guardianManager;
	}

	public KitManager getKitManager() {
		return kitManager;
	}

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public TimerManager getTimerManager() {
		return timerManager;
	}

	public void reload() {
		File mapsFolder = new File("plugins/" + plugin.getDescription().getName() + "/maps");
		if (!mapsFolder.exists()) {
			mapsFolder.mkdirs();
		}

		String[] allMapFolders = mapsFolder.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		ArrayList<String> allMaps = new ArrayList<>();
		try {
			for (String allMapFolder : allMapFolders) {
				allMaps.add(allMapFolder);
			}

			if (allMaps.size() > 1 && allMaps.contains(plugin.getConfigManager().getConfig("lobby.yml").getString("previous-map"))) {
				allMaps.remove(plugin.getConfigManager().getConfig("lobby.yml").getString("previous-map"));
			}
			currentMap = allMaps.get(new Random().nextInt(allMaps.size()));
		} catch (IllegalArgumentException | NullPointerException ex) {
			Bukkit.getLogger().info("");
			Bukkit.getLogger().severe("[SpeedBuilders] Maps for SpeedBuilders plugin can not be found!");
			Bukkit.getLogger().severe("[SpeedBuilders] Follow this instruction to fix this problem:");
			Bukkit.getLogger().severe("[SpeedBuilders] Copy and paste your maps to the plugin's maps folder (../plugins/SpeedBuilders/maps),");
			Bukkit.getLogger().severe("[SpeedBuilders] and to the server's root folder.");
			Bukkit.getLogger().info("");
			Bukkit.getPluginManager().disablePlugin(plugin);
			return;
		}
		unusedTemplates.clear();
		usedTemplates.clear();
		playersDoubleJumpCooldowned.clear();
		playersKit.clear();
		playerPercent.clear();
		plots.clear();
		playerStartScoreboard.clear();
		buildTimeSubtractor = 0;
		currentRound = 0;
		maxPlayers = 0;
		currentBuildDisplayName = translate("MAIN-NONE");
		currentBuildRawName = translate("MAIN-NONE");
		firstPlace = translate("MAIN-NONE");
		secondPlace = translate("MAIN-NONE");
		thirdPlace = translate("MAIN-NONE");
		gameState = GameState.WAITING;

		Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(GameState.WAITING, Bukkit.getOnlinePlayers().size()));

		try {
			for (String plotName : plugin.getConfigManager().getConfig("maps.yml").getConfigurationSection("maps." + currentMap + ".plots").getKeys(false)) {
				if (plugin.getConfigManager().getConfig("maps.yml").contains("maps." + currentMap + ".plots." + plotName + ".spawnpoint") && plugin.getConfigManager().getConfig("maps.yml").contains("maps." + currentMap + ".plots." + plotName + ".area") && plugin.getConfigManager().getConfig("maps.yml").contains("maps." + currentMap + ".plots." + plotName + ".laser-beam") && plugin.getConfigManager().getConfig("maps.yml").contains("maps." + currentMap + ".plots." + plotName + ".build-area") && plugin.getConfigManager().getConfig("maps.yml").contains("maps." + currentMap + ".plots." + plotName + ".blocks")) {
					maxPlayers++;
				}
			}
		} catch (NullPointerException ignored) {}
		if (maxPlayers == 0) {
			maxPlayers = 1;
		}

		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		gameScoreboard = scoreboardManager.getNewScoreboard();
		Objective objective = gameScoreboard.registerNewObjective("SpeedBuilders", "dummy");
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-SPEEDBUILDERS")));
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		Team playersTeam = gameScoreboard.registerNewTeam("Players");
		Team guardiansTeam = gameScoreboard.registerNewTeam("Guardians");

		playersTeam.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e"));
		guardiansTeam.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e"));

		new BukkitRunnable() {
			public void run() {
				if (Bukkit.getOnlinePlayers().size() > 0) {
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						// plugin.getBungeeCord().getNMSManager().disguise(onlinePlayer, false);

						if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
							Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
							location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
							location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
							onlinePlayer.teleport(location);
						} else {
							onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
						}

						gameScoreboard.getTeam("Players").addPlayer(onlinePlayer);
						kitManager.setKit(onlinePlayer, "None");

						onlinePlayer.getInventory().setArmorContents(null);
						onlinePlayer.getInventory().clear();
						onlinePlayer.setAllowFlight(false);
						onlinePlayer.setExp(0);
						onlinePlayer.setFireTicks(0);
						onlinePlayer.setFlying(false);
						onlinePlayer.setFoodLevel(20);
						onlinePlayer.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
						onlinePlayer.setHealth(20);
						onlinePlayer.setLevel(0);
						for (PotionEffect potionEffect : onlinePlayer.getActivePotionEffects()) {
							onlinePlayer.removePotionEffect(potionEffect.getType());
						}

						ItemStack itemStack1 = new ItemStack(Material.CLOCK);
						ItemMeta itemMeta1 = itemStack1.getItemMeta();
						itemMeta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")));
						itemStack1.setItemMeta(itemMeta1);

						if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
							ItemStack itemStack2 = new ItemStack(Material.BOOK);
							ItemMeta itemMeta2 = itemStack2.getItemMeta();
							itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-STATS_ITEM")));
							itemStack2.setItemMeta(itemMeta2);

							onlinePlayer.getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("stats-item-slot") - 1, itemStack2);
						}
						onlinePlayer.getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("lobby-item-slot") - 1, itemStack1);
						onlinePlayer.updateInventory();

						if (playerStartScoreboard.get(onlinePlayer.getName()) != null) {
							Scoreboard scoreboard = playerStartScoreboard.get(onlinePlayer.getName());
							Objective objective = scoreboard.getObjective("SpeedBuilders");
							for (String entry : scoreboard.getEntries()) {
								scoreboard.resetScores(entry);
							}
							if (gameState == GameState.WAITING) {
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
							}
							if (gameState == GameState.STARTING) {
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", timerManager.timeString(timerManager.getStartTime()))));
							}
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', Bukkit.getOnlinePlayers().size() + "/" + maxPlayers), scoreboard)).setScore(4);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + kitManager.getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
							onlinePlayer.setScoreboard(scoreboard);
						} else {
							ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
							Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
							Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
							if (gameState == GameState.WAITING) {
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
							}
							if (gameState == GameState.STARTING) {
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", timerManager.timeString(timerManager.getStartTime()))));
							}
							objective.setDisplaySlot(DisplaySlot.SIDEBAR);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', Bukkit.getOnlinePlayers().size() + "/" + maxPlayers), scoreboard)).setScore(4);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
							objective.getScore(scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + kitManager.getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
							onlinePlayer.setScoreboard(scoreboard);
							playerStartScoreboard.put(onlinePlayer.getName(), scoreboard);
						}
					}

					int neededPlayers = plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.needed-players");
					if (Bukkit.getOnlinePlayers().size() >= neededPlayers) {
						timerManager.startTimer();
					}
				}

				plugin.getConfigManager().getConfig("lobby.yml").set("previous-map", currentMap);
				new BukkitRunnable() {
					public void run() {
						plugin.getConfigManager().saveConfig("lobby.yml");
					}
				}.runTaskAsynchronously(plugin);
			}
		}.runTaskLater(plugin, 2L);
	}

	public String scoreboardScore(String text, Scoreboard scoreboard) {
		String result;
		if (text.length() <= 16) {
			return text;
		}
		Team team = scoreboard.registerNewTeam("text-" + scoreboard.getTeams().size());
		Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
		team.setPrefix(iterator.next());
		result = iterator.next();
		if (text.length() > 32) {
			team.setSuffix(iterator.next());
		}
		Map.Entry<Team, String> abstractMap = new AbstractMap.SimpleEntry<>(team, result);
		String value = abstractMap.getValue();
		if (abstractMap.getKey() != null) {
			abstractMap.getKey().addEntry(value);
		}
		return value;
	}

}
