package ee.mathiaskivi.speedbuilders.multiworld.manager;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.api.event.ArenaStateChangeEvent;
import ee.mathiaskivi.speedbuilders.api.event.PlayerLoseEvent;
import ee.mathiaskivi.speedbuilders.api.event.PlayerWinEvent;
import ee.mathiaskivi.speedbuilders.api.game.ArenaState;
import ee.mathiaskivi.speedbuilders.multiworld.Arena;
import ee.mathiaskivi.speedbuilders.utility.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

@SuppressWarnings("deprecation")
public class ArenaManager {
	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public Arena getArena(String arenaName) {
		for (Arena arena : Arena.arenaObjects) {
			if (arena.getName().equals(arenaName)) {
				return arena;
			}
		}
		return null;
	}

	public void addPlayer(Player player, String arenaName) {
		Arena arena = getArena(arenaName);
		arena.getPlayers().add(player);

		if (arena.getState() == ArenaState.WAITING) {
			if (plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".lobby.spawn.world") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".lobby.spawn.x") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".lobby.spawn.y") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".lobby.spawn.z")) {
				Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("arenas.yml").getString("arenas." + arena.getName() + ".lobby.spawn.world")), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".lobby.spawn.x"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".lobby.spawn.y"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".lobby.spawn.z"));
				location.setPitch((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".lobby.spawn.pitch"));
				location.setYaw((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".lobby.spawn.yaw"));
				player.teleport(location);
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
			}

			arena.getGameScoreboard().getTeam("Players").addPlayer(player);
			plugin.getMultiWorld().getKitManager().setKit(player, "None", arena.getName());

			player.getInventory().setArmorContents(null);
			player.getInventory().clear();
			player.setExp(0);
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(20);
			player.setLevel(0);
			player.setAllowFlight(false);
			player.setFlying(false);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
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

				player.getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("stats-item-slot") - 1, itemStack2);
			}

			player.getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("lobby-item-slot") - 1, itemStack1);
			player.updateInventory();

			arena.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_JOIN")).replaceAll("%PLAYER%", player.getName()));

			arena.getPlayers().forEach(p -> {
				if (arena.getPlayerStartScoreboard().get(p.getName()) != null) {
					Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(p.getName());
					Objective objective = scoreboard.getObjective("SpeedBuilders");
					for (String entry : scoreboard.getEntries()) {
						scoreboard.resetScores(entry);
					}
					if (arena.getState() == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (arena.getState() == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
					}
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(p, arena.getName()).toUpperCase())), scoreboard)).setScore(1);
					p.setScoreboard(scoreboard);
				} else {
					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
					Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
					Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
					if (arena.getState() == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (arena.getState() == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
					}
					objective.setDisplaySlot(DisplaySlot.SIDEBAR);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(p, arena.getName()).toUpperCase())), scoreboard)).setScore(1);
					p.setScoreboard(scoreboard);
					arena.getPlayerStartScoreboard().put(p.getName(), scoreboard);
				}
			});

			if (arena.getPlayers().size() == arena.getNeededPlayers()) {
				plugin.getMultiWorld().getTimerManager().startTimer(arena.getName());
			}
		} else if (arena.getState() == ArenaState.STARTING) {
			if (plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".lobby.spawn.world") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".lobby.spawn.x") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".lobby.spawn.y") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".lobby.spawn.z")) {
				Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("arenas.yml").getString("arenas." + arena.getName() + ".lobby.spawn.world")), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".lobby.spawn.x"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".lobby.spawn.y"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".lobby.spawn.z"));
				location.setPitch((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".lobby.spawn.pitch"));
				location.setYaw((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".lobby.spawn.yaw"));
				player.teleport(location);
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
			}

			arena.getGameScoreboard().getTeam("Players").addPlayer(player);
			plugin.getMultiWorld().getKitManager().setKit(player, "None", arena.getName());

			player.getInventory().setArmorContents(null);
			player.getInventory().clear();
			player.setExp(0);
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(20);
			player.setLevel(0);
			player.setAllowFlight(false);
			player.setFlying(false);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
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

				player.getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("stats-item-slot") - 1, itemStack2);
			}

			player.getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("lobby-item-slot") - 1, itemStack1);
			player.updateInventory();

			arena.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_JOIN")).replaceAll("%PLAYER%", player.getName()));

			arena.getPlayers().forEach(p -> {
				if (arena.getPlayerStartScoreboard().get(p.getName()) != null) {
					Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(p.getName());
					Objective objective = scoreboard.getObjective("SpeedBuilders");
					for (String entry : scoreboard.getEntries()) {
						scoreboard.resetScores(entry);
					}
					if (arena.getState() == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (arena.getState() == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
					}
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(p, arena.getName()).toUpperCase())), scoreboard)).setScore(1);
					p.setScoreboard(scoreboard);
				} else {
					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
					Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
					Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
					if (arena.getState() == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (arena.getState() == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
					}
					objective.setDisplaySlot(DisplaySlot.SIDEBAR);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(p, arena.getName()).toUpperCase())), scoreboard)).setScore(1);
					p.setScoreboard(scoreboard);
					arena.getPlayerStartScoreboard().put(p.getName(), scoreboard);
				}
			});
		}
		plugin.getMultiWorld().getSignManager().updateSigns(arena.getName());
	}

	public void removePlayer(Player player, String arenaName) {
		Arena arena = getArena(arenaName);
		arena.getPlayers().remove(player.getName());

		if (arena.getState() == ArenaState.WAITING) {
			arena.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			arena.getPlayers().forEach(p -> {
				if (arena.getPlayerStartScoreboard().get(p.getName()) != null) {
					Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(p.getName());
					Objective objective = scoreboard.getObjective("SpeedBuilders");
					for (String entry : scoreboard.getEntries()) {
						scoreboard.resetScores(entry);
					}
					if (arena.getState() == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (arena.getState() == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
					}
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(p, arena.getName()).toUpperCase())), scoreboard)).setScore(1);
					p.setScoreboard(scoreboard);
				} else {
					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
					Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
					Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
					if (arena.getState() == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (arena.getState() == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
					}
					objective.setDisplaySlot(DisplaySlot.SIDEBAR);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(p, arena.getName()).toUpperCase())), scoreboard)).setScore(1);
					p.setScoreboard(scoreboard);
					arena.getPlayerStartScoreboard().put(p.getName(), scoreboard);
				}
			});

			if (arena.getGameScoreboard().getTeam("Players").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Players").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (arena.getGameScoreboard().getTeam("Guardians").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Guardians").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			}

			player.getInventory().setArmorContents(null);
			player.getInventory().clear();
			player.setExp(0);
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			player.setAllowFlight(false);
			player.setFlying(false);
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}

			if (plugin.getMultiWorld().playerTempHealth.containsKey(player.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(player.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempGameMode.containsKey(player.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(player.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(player.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(player.getName())) {
				plugin.getMultiWorld().loadTempInfo(player);
			}
			player.updateInventory();
		} else if (arena.getState() == ArenaState.STARTING) {
			arena.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			if (arena.getPlayers().size() < arena.getNeededPlayers()) {
				Bukkit.getScheduler().cancelTask(arena.getStartTimerID());

				arena.setState(ArenaState.WAITING);

				Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.WAITING, arena.getPlayers()));
			}

			arena.getPlayers().forEach(p -> {
				if (arena.getPlayerStartScoreboard().get(p.getName()) != null) {
					Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(p.getName());
					Objective objective = scoreboard.getObjective("SpeedBuilders");
					for (String entry : scoreboard.getEntries()) {
						scoreboard.resetScores(entry);
					}
					if (arena.getState() == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (arena.getState() == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
					}
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(p, arena.getName()).toUpperCase())), scoreboard)).setScore(1);
					p.setScoreboard(scoreboard);
				} else {
					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
					Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
					Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
					if (arena.getState() == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (arena.getState() == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
					}
					objective.setDisplaySlot(DisplaySlot.SIDEBAR);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(p, arena.getName()).toUpperCase())), scoreboard)).setScore(1);
					p.setScoreboard(scoreboard);
					arena.getPlayerStartScoreboard().put(p.getName(), scoreboard);
				}
			});

			if (arena.getGameScoreboard().getTeam("Players").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Players").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (arena.getGameScoreboard().getTeam("Guardians").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Guardians").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			}

			player.getInventory().setArmorContents(null);
			player.getInventory().clear();
			player.setExp(0);
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			player.setAllowFlight(false);
			player.setFlying(false);
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}

			if (plugin.getMultiWorld().playerTempHealth.containsKey(player.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(player.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempGameMode.containsKey(player.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(player.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(player.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(player.getName())) {
				plugin.getMultiWorld().loadTempInfo(player);
			}
			player.updateInventory();
		} else if (arena.getState() == ArenaState.BEGINNING) {
			arena.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			String plot = arena.getPlots().get(player.getName());
			if (plot != null) {
				arena.getPlayerPercent().remove(player.getName());
				arena.getPlots().remove(player.getName());

				if (arena.getJudgedPlayerArmorStand() != null) {
					arena.getJudgedPlayerArmorStand().remove();
				}

				plugin.getMultiWorld().getGuardianManager().laserGuardian(false, arena.getName());

				if (arena.getPlots().size() == 1) {
					Bukkit.getScheduler().cancelTask(arena.getGameStartTimerID());
					Bukkit.getScheduler().cancelTask(arena.getShowCaseTimerID());
					Bukkit.getScheduler().cancelTask(arena.getBuildTimerID());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID1());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID2());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID3());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID4());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID5());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID6());

					arena.setSecondPlace(player.getName());
					arena.setFirstPlace((String) arena.getPlots().keySet().toArray()[0]);

					arena.getPlayers().forEach(p -> {
						for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("MULTIWORLD.MAIN-GAME_END_DESCRIPTION")) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", arena.getFirstPlace()).replaceAll("%PLAYER2%", arena.getSecondPlace()).replaceAll("%PLAYER3%", arena.getThirdPlace())));
						}

						p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + arena.getFirstPlace()), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 0, 7*20, 20);

						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
						if (p.getName().equals(arena.getFirstPlace())) {
							new BukkitRunnable() {
								public void run() {
									for (String winnerCommand : plugin.getMultiWorld().winnerCommands) {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", p.getName()));
									}

									plugin.getStatsManager().incrementStat(StatsType.WINS, p, 1);

									Bukkit.getPluginManager().callEvent(new PlayerWinEvent(p));
								}
							}.runTaskLater(plugin, 5L);
						}
					});

					plugin.getMultiWorld().getTemplateManager().explodePlot(plot, arena.getName());

					new BukkitRunnable() {
						public void run() {
							endArena(arena.getName());
						}
					}.runTaskLater(plugin, 10*20L);
				} else {
					if (arena.getPlots().size() == 2) {
						arena.setThirdPlace(player.getName());
					}

					plugin.getMultiWorld().getTemplateManager().explodePlot(plot, arena.getName());
				}

				for (String loserCommand : plugin.getMultiWorld().loserCommands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loserCommand.replaceFirst("/", "").replaceAll("%PLAYER%", player.getName()));
				}

				plugin.getStatsManager().incrementStat(StatsType.LOSSES, player, 1);

				Bukkit.getPluginManager().callEvent(new PlayerLoseEvent(player));
			}

			for (String entry : arena.getGameScoreboard().getEntries()) {
				arena.getGameScoreboard().resetScores(entry);
			}
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), arena.getGameScoreboard())).setScore(15);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-BUILD")), arena.getGameScoreboard())).setScore(14);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getCurrentBuildDisplayName()), arena.getGameScoreboard())).setScore(13);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), arena.getGameScoreboard())).setScore(12);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-ROUND")), arena.getGameScoreboard())).setScore(11);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "" + arena.getCurrentRound()), arena.getGameScoreboard())).setScore(10);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&3"), arena.getGameScoreboard())).setScore(9);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), arena.getGameScoreboard())).setScore(8);
			AtomicInteger score = new AtomicInteger(7);
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[0]), arena.getGameScoreboard())).setScore(7);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[1]), arena.getGameScoreboard())).setScore(6);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[2]), arena.getGameScoreboard())).setScore(5);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[3]), arena.getGameScoreboard())).setScore(4);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[4]), arena.getGameScoreboard())).setScore(3);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[5]), arena.getGameScoreboard())).setScore(2);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[6]), arena.getGameScoreboard())).setScore(1);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			arena.getPlayers().forEach(p -> {
				if (p != player) {
					if (score.get() >= 0) {
						if (!arena.getPlots().containsKey(p.getName())) {
							arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + p.getName()), arena.getGameScoreboard())).setScore(score.get());
							score.getAndDecrement();
						}
					}
				}

				p.setScoreboard(arena.getGameScoreboard());
			});

			if (arena.getGameScoreboard().getTeam("Players").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Players").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (arena.getGameScoreboard().getTeam("Guardians").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Guardians").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			}

			player.getInventory().setArmorContents(null);
			player.getInventory().clear();
			player.setExp(0);
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			player.setAllowFlight(false);
			player.setFlying(false);
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}

			if (plugin.getMultiWorld().playerTempHealth.containsKey(player.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(player.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempGameMode.containsKey(player.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(player.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(player.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(player.getName())) {
				plugin.getMultiWorld().loadTempInfo(player);
			}
			player.updateInventory();
		} else if (arena.getState() == ArenaState.DISPLAYING) {
			arena.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			String plot = arena.getPlots().get(player.getName());
			if (plot != null) {
				arena.getPlayerPercent().remove(player.getName());
				arena.getPlots().remove(player.getName());

				if (arena.getJudgedPlayerArmorStand() != null) {
					arena.getJudgedPlayerArmorStand().remove();
				}

				plugin.getMultiWorld().getGuardianManager().laserGuardian(false, arena.getName());

				if (arena.getPlots().size() == 1) {
					Bukkit.getScheduler().cancelTask(arena.getGameStartTimerID());
					Bukkit.getScheduler().cancelTask(arena.getShowCaseTimerID());
					Bukkit.getScheduler().cancelTask(arena.getBuildTimerID());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID1());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID2());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID3());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID4());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID5());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID6());

					arena.setSecondPlace(player.getName());
					arena.setFirstPlace((String) arena.getPlots().keySet().toArray()[0]);

					arena.getPlayers().forEach(p -> {
						for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("MULTIWORLD.MAIN-GAME_END_DESCRIPTION")) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", arena.getFirstPlace()).replaceAll("%PLAYER2%", arena.getSecondPlace()).replaceAll("%PLAYER3%", arena.getThirdPlace())));
						}

						p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + arena.getFirstPlace()), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 0, 7*20, 20);

						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
						if (p.getName().equals(arena.getFirstPlace())) {
							new BukkitRunnable() {
								public void run() {
									for (String winnerCommand : plugin.getMultiWorld().winnerCommands) {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", p.getName()));
									}

									plugin.getStatsManager().incrementStat(StatsType.WINS, p, 1);

									Bukkit.getPluginManager().callEvent(new PlayerWinEvent(p));
								}
							}.runTaskLater(plugin, 5L);
						}
					});

					plugin.getMultiWorld().getTemplateManager().explodePlot(plot, arena.getName());

					new BukkitRunnable() {
						public void run() {
							endArena(arena.getName());
						}
					}.runTaskLater(plugin, 10*20L);
				} else {
					if (arena.getPlots().size() == 2) {
						arena.setThirdPlace(player.getName());
					}

					plugin.getMultiWorld().getTemplateManager().explodePlot(plot, arena.getName());
				}

				for (String loserCommand : plugin.getMultiWorld().loserCommands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loserCommand.replaceFirst("/", "").replaceAll("%PLAYER%", player.getName()));
				}

				plugin.getStatsManager().incrementStat(StatsType.LOSSES, player, 1);

				Bukkit.getPluginManager().callEvent(new PlayerLoseEvent(player));
			}

			for (String entry : arena.getGameScoreboard().getEntries()) {
				arena.getGameScoreboard().resetScores(entry);
			}
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), arena.getGameScoreboard())).setScore(15);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-BUILD")), arena.getGameScoreboard())).setScore(14);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getCurrentBuildDisplayName()), arena.getGameScoreboard())).setScore(13);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), arena.getGameScoreboard())).setScore(12);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-ROUND")), arena.getGameScoreboard())).setScore(11);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "" + arena.getCurrentRound()), arena.getGameScoreboard())).setScore(10);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&3"), arena.getGameScoreboard())).setScore(9);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), arena.getGameScoreboard())).setScore(8);
			AtomicInteger score = new AtomicInteger(7);
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[0]), arena.getGameScoreboard())).setScore(7);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[1]), arena.getGameScoreboard())).setScore(6);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[2]), arena.getGameScoreboard())).setScore(5);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[3]), arena.getGameScoreboard())).setScore(4);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[4]), arena.getGameScoreboard())).setScore(3);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[5]), arena.getGameScoreboard())).setScore(2);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[6]), arena.getGameScoreboard())).setScore(1);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			arena.getPlayers().forEach(p -> {
				if (p != player) {
					if (score.get() >= 0) {
						if (!arena.getPlots().containsKey(p.getName())) {
							arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + p.getName()), arena.getGameScoreboard())).setScore(score.get());
							score.getAndDecrement();
						}
					}
				}

				p.setScoreboard(arena.getGameScoreboard());
			});

			if (arena.getGameScoreboard().getTeam("Players").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Players").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (arena.getGameScoreboard().getTeam("Guardians").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Guardians").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			}

			player.getInventory().setArmorContents(null);
			player.getInventory().clear();
			player.setExp(0);
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			player.setAllowFlight(false);
			player.setFlying(false);
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}

			if (plugin.getMultiWorld().playerTempHealth.containsKey(player.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(player.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempGameMode.containsKey(player.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(player.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(player.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(player.getName())) {
				plugin.getMultiWorld().loadTempInfo(player);
			}
			player.updateInventory();
		} else if (arena.getState() == ArenaState.BUILDING) {
			arena.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			String plot = arena.getPlots().get(player.getName());
			if (plot != null) {
				arena.getPlayerPercent().remove(player.getName());
				arena.getPlots().remove(player.getName());

				if (arena.getJudgedPlayerArmorStand() != null) {
					arena.getJudgedPlayerArmorStand().remove();
				}

				plugin.getMultiWorld().getGuardianManager().laserGuardian(false, arena.getName());

				if (arena.getPlots().size() == 1) {
					Bukkit.getScheduler().cancelTask(arena.getGameStartTimerID());
					Bukkit.getScheduler().cancelTask(arena.getShowCaseTimerID());
					Bukkit.getScheduler().cancelTask(arena.getBuildTimerID());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID1());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID2());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID3());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID4());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID5());
					Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID6());

					arena.setSecondPlace(player.getName());
					arena.setFirstPlace((String) arena.getPlots().keySet().toArray()[0]);

					arena.getPlayers().forEach(p -> {
						for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("MULTIWORLD.MAIN-GAME_END_DESCRIPTION")) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", arena.getFirstPlace()).replaceAll("%PLAYER2%", arena.getSecondPlace()).replaceAll("%PLAYER3%", arena.getThirdPlace())));
						}

						p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + arena.getFirstPlace()), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 0, 7*20, 20);

						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
						if (p.getName().equals(arena.getFirstPlace())) {
							new BukkitRunnable() {
								public void run() {
									for (String winnerCommand : plugin.getMultiWorld().winnerCommands) {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", p.getName()));
									}

									plugin.getStatsManager().incrementStat(StatsType.WINS, p, 1);

									Bukkit.getPluginManager().callEvent(new PlayerWinEvent(p));
								}
							}.runTaskLater(plugin, 5L);
						}
					});

					plugin.getMultiWorld().getTemplateManager().explodePlot(plot, arena.getName());

					new BukkitRunnable() {
						public void run() {
							endArena(arena.getName());
						}
					}.runTaskLater(plugin, 10*20L);
				} else {
					if (arena.getPlayerPercent().size() >= 2 && Collections.min(arena.getPlayerPercent().values()) == 100) {
						plugin.getMultiWorld().getTimerManager().guardianIsImpressed(arena.getName());
					}

					if (arena.getPlots().size() == 2) {
						arena.setThirdPlace(player.getName());
					}

					plugin.getMultiWorld().getTemplateManager().explodePlot(plot, arena.getName());
				}

				for (String loserCommand : plugin.getMultiWorld().loserCommands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loserCommand.replaceFirst("/", "").replaceAll("%PLAYER%", player.getName()));
				}

				plugin.getStatsManager().incrementStat(StatsType.LOSSES, player, 1);

				Bukkit.getPluginManager().callEvent(new PlayerLoseEvent(player));
			}

			for (String entry : arena.getGameScoreboard().getEntries()) {
				arena.getGameScoreboard().resetScores(entry);
			}
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), arena.getGameScoreboard())).setScore(15);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-BUILD")), arena.getGameScoreboard())).setScore(14);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getCurrentBuildDisplayName()), arena.getGameScoreboard())).setScore(13);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), arena.getGameScoreboard())).setScore(12);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-ROUND")), arena.getGameScoreboard())).setScore(11);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "" + arena.getCurrentRound()), arena.getGameScoreboard())).setScore(10);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&3"), arena.getGameScoreboard())).setScore(9);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), arena.getGameScoreboard())).setScore(8);
			AtomicInteger score = new AtomicInteger(7);
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[0]), arena.getGameScoreboard())).setScore(7);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[1]), arena.getGameScoreboard())).setScore(6);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[2]), arena.getGameScoreboard())).setScore(5);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[3]), arena.getGameScoreboard())).setScore(4);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[4]), arena.getGameScoreboard())).setScore(3);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[5]), arena.getGameScoreboard())).setScore(2);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[6]), arena.getGameScoreboard())).setScore(1);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			arena.getPlayers().forEach(p -> {
				if (p != player) {
					if (score.get() >= 0) {
						if (!arena.getPlots().containsKey(p.getName())) {
							arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + p.getName()), arena.getGameScoreboard())).setScore(score.get());
							score.getAndDecrement();
						}
					}
				}

				p.setScoreboard(arena.getGameScoreboard());
			});

			if (arena.getGameScoreboard().getTeam("Players").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Players").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (arena.getGameScoreboard().getTeam("Guardians").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Guardians").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			}

			player.getInventory().setArmorContents(null);
			player.getInventory().clear();
			player.setExp(0);
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			player.setAllowFlight(false);
			player.setFlying(false);
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}

			if (plugin.getMultiWorld().playerTempHealth.containsKey(player.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(player.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempGameMode.containsKey(player.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(player.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(player.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(player.getName())) {
				plugin.getMultiWorld().loadTempInfo(player);
			}
			player.updateInventory();
		} else if (arena.getState() == ArenaState.JUDGING) {
			arena.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			String plot = arena.getPlots().get(player.getName());
			if (plot != null) {
				arena.getPlayerPercent().remove(player.getName());
				arena.getPlots().remove(player.getName());

				if (arena.getJudgedPlayerArmorStand() != null) {
					arena.getJudgedPlayerArmorStand().remove();
				}

				plugin.getMultiWorld().getGuardianManager().laserGuardian(false, arena.getName());

				if (player.getName().equalsIgnoreCase(arena.getJudgedPlayerName())) {
					if (arena.getPlots().size() == 1) {
						Bukkit.getScheduler().cancelTask(arena.getGameStartTimerID());
						Bukkit.getScheduler().cancelTask(arena.getShowCaseTimerID());
						Bukkit.getScheduler().cancelTask(arena.getBuildTimerID());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID1());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID2());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID3());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID4());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID5());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID6());

						arena.setSecondPlace(player.getName());
						arena.setFirstPlace((String) arena.getPlots().keySet().toArray()[0]);

						arena.getPlayers().forEach(p -> {
							for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("MULTIWORLD.MAIN-GAME_END_DESCRIPTION")) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", arena.getFirstPlace()).replaceAll("%PLAYER2%", arena.getSecondPlace()).replaceAll("%PLAYER3%", arena.getThirdPlace())));
							}

							p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + arena.getFirstPlace()), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 0, 7*20, 20);

							p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
							if (p.getName().equals(arena.getFirstPlace())) {
								new BukkitRunnable() {
									public void run() {
										for (String winnerCommand : plugin.getMultiWorld().winnerCommands) {
											Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", p.getName()));
										}

										plugin.getStatsManager().incrementStat(StatsType.WINS, p, 1);

										Bukkit.getPluginManager().callEvent(new PlayerWinEvent(p));
									}
								}.runTaskLater(plugin, 5L);
							}
						});

						plugin.getMultiWorld().getTemplateManager().explodePlot(plot, arena.getName());

						new BukkitRunnable() {
							public void run() {
								endArena(arena.getName());
							}
						}.runTaskLater(plugin, 10*20L);
					} else {
						Bukkit.getScheduler().cancelTask(arena.getGameStartTimerID());
						Bukkit.getScheduler().cancelTask(arena.getShowCaseTimerID());
						Bukkit.getScheduler().cancelTask(arena.getBuildTimerID());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID1());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID2());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID3());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID4());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID5());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID6());

						if (arena.getPlots().size() == 2) {
							arena.setThirdPlace(player.getName());
						}

						plugin.getMultiWorld().getTemplateManager().explodePlot(plot, arena.getName());

						new BukkitRunnable() {
							public void run() {
								arena.getPlayers().forEach(p -> {
									if (arena.getPlots().containsKey(p.getName())) {
										p.getInventory().setArmorContents(null);
										p.getInventory().clear();
										p.setExp(0);
										p.setFireTicks(0);
										p.setFoodLevel(20);
										p.setGameMode(GameMode.SURVIVAL);
										p.setHealth(20);
										p.setLevel(0);
										p.setAllowFlight(false);
										p.setFlying(false);
										for (PotionEffect potionEffect : p.getActivePotionEffects()) {
											p.removePotionEffect(potionEffect.getType());
										}

										if (arena.getGameScoreboard().getPlayerTeam(p) != null) {
											if (arena.getGameScoreboard().getPlayerTeam(p).getName().equals("Players")) {
												String plot = arena.getPlots().get(p.getName());
												Location location = new Location(Bukkit.getWorld(arena.getName()), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.x"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.y"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.z"));
												location.setPitch((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.pitch"));
												location.setYaw((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.yaw"));
												p.teleport(location);
												p.setFallDistance(0F);
											}
										}
									}
								});

								plugin.getMultiWorld().getTimerManager().showCaseTimer(arena.getName());
							}
						}.runTaskLater(plugin, 5L);
					}
				} else {
					if (arena.getPlots().size() == 1) {
						Bukkit.getScheduler().cancelTask(arena.getGameStartTimerID());
						Bukkit.getScheduler().cancelTask(arena.getShowCaseTimerID());
						Bukkit.getScheduler().cancelTask(arena.getBuildTimerID());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID1());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID2());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID3());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID4());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID5());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID6());

						arena.setSecondPlace((String) arena.getPlots().keySet().toArray()[0]);
						arena.setFirstPlace(player.getName());

						arena.getPlayers().forEach(p -> {
							for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("MULTIWORLD.MAIN-GAME_END_DESCRIPTION")) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", arena.getFirstPlace()).replaceAll("%PLAYER2%", arena.getSecondPlace()).replaceAll("%PLAYER3%", arena.getThirdPlace())));
							}

							p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + arena.getFirstPlace()), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 0, 7*20, 20);

							p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
							if (p.getName().equals(arena.getFirstPlace())) {
								new BukkitRunnable() {
									public void run() {
										for (String winnerCommand : plugin.getMultiWorld().winnerCommands) {
											Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", p.getName()));
										}

										plugin.getStatsManager().incrementStat(StatsType.WINS, p, 1);

										Bukkit.getPluginManager().callEvent(new PlayerWinEvent(p));
									}
								}.runTaskLater(plugin, 5L);
							}
						});

						plugin.getMultiWorld().getTemplateManager().explodePlot(plot, arena.getName());

						new BukkitRunnable() {
							public void run() {
								endArena(arena.getName());
							}
						}.runTaskLater(plugin, 10*20L);
					} else {
						if (arena.getPlots().size() == 2) {
							arena.setThirdPlace(player.getName());
						}

						plugin.getMultiWorld().getTemplateManager().explodePlot(plot, arena.getName());
					}
				}
			}

			for (String entry : arena.getGameScoreboard().getEntries()) {
				arena.getGameScoreboard().resetScores(entry);
			}
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), arena.getGameScoreboard())).setScore(15);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-BUILD")), arena.getGameScoreboard())).setScore(14);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getCurrentBuildDisplayName()), arena.getGameScoreboard())).setScore(13);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), arena.getGameScoreboard())).setScore(12);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-ROUND")), arena.getGameScoreboard())).setScore(11);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "" + arena.getCurrentRound()), arena.getGameScoreboard())).setScore(10);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&3"), arena.getGameScoreboard())).setScore(9);
			arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), arena.getGameScoreboard())).setScore(8);
			AtomicInteger score = new AtomicInteger(7);
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[0]), arena.getGameScoreboard())).setScore(7);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[1]), arena.getGameScoreboard())).setScore(6);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[2]), arena.getGameScoreboard())).setScore(5);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[3]), arena.getGameScoreboard())).setScore(4);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[4]), arena.getGameScoreboard())).setScore(3);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[5]), arena.getGameScoreboard())).setScore(2);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) arena.getPlots().keySet().toArray()[6]), arena.getGameScoreboard())).setScore(1);
				score.getAndDecrement();
			} catch (ArrayIndexOutOfBoundsException ex) {}
			arena.getPlayers().forEach(p -> {
				if (p != player) {
					if (score.get() >= 0) {
						if (!arena.getPlots().containsKey(p.getName())) {
							arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + p.getName()), arena.getGameScoreboard())).setScore(score.get());
							score.getAndDecrement();
						}
					}
				}

				p.setScoreboard(arena.getGameScoreboard());
			});

			if (arena.getGameScoreboard().getTeam("Players").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Players").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (arena.getGameScoreboard().getTeam("Guardians").hasPlayer(player)) {
				arena.getGameScoreboard().getTeam("Guardians").removePlayer(player);
				plugin.getMultiWorld().getKitManager().setKit(player, null, arena.getName());

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			}

			player.getInventory().setArmorContents(null);
			player.getInventory().clear();
			player.setExp(0);
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			player.setAllowFlight(false);
			player.setFlying(false);
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}

			if (plugin.getMultiWorld().playerTempHealth.containsKey(player.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(player.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempGameMode.containsKey(player.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(player.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(player.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(player.getName())) {
				plugin.getMultiWorld().loadTempInfo(player);
			}
			player.updateInventory();
		}

		plugin.getMultiWorld().getSignManager().updateSigns(arena.getName());
	}

	public void endArena(String arenaName) {
		Arena arena = getArena(arenaName);

		if (arena != null) {
			arena.getPlayers().forEach(p -> {
				plugin.getMultiWorld().getNMSManager().setPlayerVisibility(p, null, true);

				if (arena.getGameScoreboard().getTeam("Players").hasPlayer(p)) {
					arena.getGameScoreboard().getTeam("Players").removePlayer(p);
					plugin.getMultiWorld().getKitManager().setKit(p, null, arena.getName());

					if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
						Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
						location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
						location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
						p.teleport(location);
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
					}
				} else if (arena.getGameScoreboard().getTeam("Guardians").hasPlayer(p)) {
					arena.getGameScoreboard().getTeam("Guardians").removePlayer(p);
					plugin.getMultiWorld().getKitManager().setKit(p, null, arena.getName());

					if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
						Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
						location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
						location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
						p.teleport(location);
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
					}
				}

				p.getInventory().setArmorContents(null);
				p.getInventory().clear();
				p.setExp(0);
				p.setFireTicks(0);
				p.setFoodLevel(20);
				p.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
				p.setHealth(20);
				p.setLevel(0);
				p.setAllowFlight(false);
				p.setFlying(false);
				p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				for (PotionEffect potionEffect : p.getActivePotionEffects()) {
					p.removePotionEffect(potionEffect.getType());
				}

				if (plugin.getMultiWorld().playerTempHealth.containsKey(p.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(p.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(p.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(p.getName()) && plugin.getMultiWorld().playerTempGameMode.containsKey(p.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(p.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(p.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(p.getName())) {
					plugin.getMultiWorld().loadTempInfo(p);
				}
				p.updateInventory();
			});

			Bukkit.getScheduler().cancelTask(arena.getStartTimerID());
			Bukkit.getScheduler().cancelTask(arena.getGameStartTimerID());
			Bukkit.getScheduler().cancelTask(arena.getShowCaseTimerID());
			Bukkit.getScheduler().cancelTask(arena.getBuildTimerID());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID1());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID2());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID3());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID4());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID5());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID6());

			plugin.getMultiWorld().getTemplateManager().resetPlots(arena.getName());

			arena.getPlayers().clear();
			arena.setStartTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaName + ".start-time"));
			arena.setGameStartTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaName + ".game-start-time"));
			arena.setShowCaseTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaName + ".showcase-time"));
			arena.setBuildTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaName + ".build-time"));
			arena.setJudgeTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaName + ".judge-time"));
			arena.getUnusedTemplates().clear();
			arena.getUsedTemplates().clear();
			arena.getPlayersDoubleJumpCooldowned().clear();
			arena.getPlayerPercent().clear();
			arena.getPlayerStartScoreboard().clear();
			arena.getPlayersKit().clear();
			arena.getPlots().clear();
			arena.setBuildTimeSubtractor(0);
			arena.setCurrentRound(0);

			int maxPlayers = 0;
			try {
				for (String plot : plugin.getConfigManager().getConfig("arenas.yml").getConfigurationSection("arenas." + arena.getName() + ".plots").getKeys(false)) {
					if (plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".plots." + plot + ".area") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".plots." + plot + ".laser-beam") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".plots." + plot + ".build-area") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".plots." + plot + ".blocks")) {
						maxPlayers++;
					}
				}
			} catch (NullPointerException ex) {}
			arena.setMaxPlayers(maxPlayers);
			arena.setCurrentBuildDisplayName(translate("MAIN-NONE"));
			arena.setCurrentBuildRawName(translate("MAIN-NONE"));
			arena.setFirstPlace(translate("MAIN-NONE"));
			arena.setSecondPlace(translate("MAIN-NONE"));
			arena.setThirdPlace(translate("MAIN-NONE"));
			arena.setState(ArenaState.WAITING);

			Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.WAITING, arena.getPlayers()));

			plugin.getMultiWorld().getSignManager().updateSigns(arena.getName());
		}
	}

	public void loadArenas() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (plugin.getConfigManager().getConfig("arenas.yml").contains("arenas")) {
					Arena.arenaObjects.clear();
					for (String arenaSection : plugin.getConfigManager().getConfig("arenas.yml").getConfigurationSection("arenas").getKeys(false)) {
						int startTime = plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaSection + ".start-time");
						int gameStartTime = plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaSection + ".game-start-time");
						int showCaseTime = plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaSection + ".showcase-time");
						int buildTime = plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaSection + ".build-time");
						int judgeTime = plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaSection + ".judge-time");
						int neededPlayers = plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arenaSection + ".needed-players");

						int maxPlayers = 0;
						try {
							for (String plot : plugin.getConfigManager().getConfig("arenas.yml").getConfigurationSection("arenas." + arenaSection + ".plots").getKeys(false)) {
								if (plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arenaSection + ".plots." + plot + ".spawnpoint") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arenaSection + ".plots." + plot + ".area") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arenaSection + ".plots." + plot + ".laser-beam") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arenaSection + ".plots." + plot + ".build-area") && plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arenaSection + ".plots." + plot + ".blocks")) {
									maxPlayers++;
								}
							}
						} catch (NullPointerException ex) {}

						WorldCreator worldCreator = new WorldCreator(arenaSection);
						worldCreator.generator(new VoidGenerator());
						Bukkit.createWorld(worldCreator);

						plugin.getMultiWorld().getTemplateManager().resetPlots(arenaSection);

						Arena arena = new Arena(arenaSection, startTime, gameStartTime, showCaseTime, buildTime, judgeTime, neededPlayers);
						arena.getUnusedTemplates().clear();
						arena.getUsedTemplates().clear();
						arena.getPlayersDoubleJumpCooldowned().clear();
						arena.getPlayerPercent().clear();
						arena.getPlayerStartScoreboard().clear();
						arena.getPlayersKit().clear();
						arena.getPlots().clear();
						arena.setBuildTimeSubtractor(0);
						arena.setCurrentRound(0);
						arena.setMaxPlayers(maxPlayers);
						arena.setCurrentBuildDisplayName(translate("MAIN-NONE"));
						arena.setCurrentBuildRawName(translate("MAIN-NONE"));
						arena.setFirstPlace(translate("MAIN-NONE"));
						arena.setSecondPlace(translate("MAIN-NONE"));
						arena.setThirdPlace(translate("MAIN-NONE"));
						arena.setState(ArenaState.WAITING);

						Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.WAITING, arena.getPlayers()));

						ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
						arena.gameScoreboard = scoreboardManager.getNewScoreboard();
						Objective objective = arena.gameScoreboard.registerNewObjective("SpeedBuilders", "dummy");
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-SPEEDBUILDERS")));
						objective.setDisplaySlot(DisplaySlot.SIDEBAR);

						Team playersTeam = arena.gameScoreboard.registerNewTeam("Players");
						Team guardiansTeam = arena.gameScoreboard.registerNewTeam("Guardians");

						playersTeam.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e"));
						guardiansTeam.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e"));

						plugin.getMultiWorld().getSignManager().updateSigns(arena.getName());
					}
				}
			}
		}, 0L);
	}

	public void createArena(String arenaName, int startTime, int gameStartTime, int showCaseTime, int buildTime, int judgeTime, int neededPlayers) {
		Arena arena = new Arena(arenaName, startTime, gameStartTime, showCaseTime, buildTime, judgeTime, neededPlayers);

		plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName(), null);
		plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".start-time", startTime);
		plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".game-start-time", gameStartTime);
		plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".showcase-time", showCaseTime);
		plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".build-time", buildTime);
		plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".judge-time", judgeTime);
		plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".needed-players", neededPlayers);
		new BukkitRunnable() {
			public void run() {
				plugin.getConfigManager().saveConfig("arenas.yml");

				loadArenas();
			}
		}.runTaskAsynchronously(plugin);
	}

	public void deleteArena(String arenaName) {
		plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName, null);
		new BukkitRunnable() {
			public void run() {
				plugin.getConfigManager().saveConfig("arenas.yml");

				loadArenas();
			}
		}.runTaskAsynchronously(plugin);
	}
}
