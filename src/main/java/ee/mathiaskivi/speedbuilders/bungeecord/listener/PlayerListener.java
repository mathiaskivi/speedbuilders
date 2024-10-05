package ee.mathiaskivi.speedbuilders.bungeecord.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.api.event.ArenaStateChangeEvent;
import ee.mathiaskivi.speedbuilders.api.event.PlayerLoseEvent;
import ee.mathiaskivi.speedbuilders.api.event.PlayerWinEvent;
import ee.mathiaskivi.speedbuilders.api.game.ArenaState;
import ee.mathiaskivi.speedbuilders.utility.StatsType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import java.util.Collections;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {
	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		if (plugin.getBungeeCord().state == ArenaState.WAITING) {
			if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
				Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
				location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
				location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
				player.teleport(location);
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
			}

			plugin.getBungeeCord().gameScoreboard.getTeam("Players").addPlayer(player);
			plugin.getBungeeCord().getKitManager().setKit(player, "None");

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

			e.setJoinMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_JOIN")).replaceAll("%PLAYER%", player.getName()));

			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (plugin.getBungeeCord().playerStartScoreboard.get(onlinePlayer.getName()) != null) {
					Scoreboard scoreboard = plugin.getBungeeCord().playerStartScoreboard.get(onlinePlayer.getName());
					Objective objective = scoreboard.getObjective("SpeedBuilders");
					for (String entry : scoreboard.getEntries()) {
						scoreboard.resetScores(entry);
					}
					if (plugin.getBungeeCord().state == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (plugin.getBungeeCord().state == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getBungeeCord().getTimerManager().timeString(plugin.getBungeeCord().getTimerManager().getStartTime()))));
					}
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', Bukkit.getOnlinePlayers().size() + "/" + plugin.getBungeeCord().maxPlayers), scoreboard)).setScore(4);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getBungeeCord().getKitManager().getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
					onlinePlayer.setScoreboard(scoreboard);
				} else {
					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
					Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
					Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
					if (plugin.getBungeeCord().state == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (plugin.getBungeeCord().state == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getBungeeCord().getTimerManager().timeString(plugin.getBungeeCord().getTimerManager().getStartTime()))));
					}
					objective.setDisplaySlot(DisplaySlot.SIDEBAR);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', Bukkit.getOnlinePlayers().size() + "/" + plugin.getBungeeCord().maxPlayers), scoreboard)).setScore(4);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getBungeeCord().getKitManager().getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
					onlinePlayer.setScoreboard(scoreboard);
					plugin.getBungeeCord().playerStartScoreboard.put(onlinePlayer.getName(), scoreboard);
				}
			}

			int neededPlayers = plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.needed-players");
			if (Bukkit.getOnlinePlayers().size() == neededPlayers) {
				plugin.getBungeeCord().getTimerManager().startTimer();
			}
		} else if (plugin.getBungeeCord().state == ArenaState.STARTING) {
			if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
				Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
				location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
				location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
				player.teleport(location);
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
			}

			plugin.getBungeeCord().gameScoreboard.getTeam("Players").addPlayer(player);
			plugin.getBungeeCord().getKitManager().setKit(player, "None");

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

			e.setJoinMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_JOIN")).replaceAll("%PLAYER%", player.getName()));

			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (plugin.getBungeeCord().playerStartScoreboard.get(onlinePlayer.getName()) != null) {
					Scoreboard scoreboard = plugin.getBungeeCord().playerStartScoreboard.get(onlinePlayer.getName());
					Objective objective = scoreboard.getObjective("SpeedBuilders");
					for (String entry : scoreboard.getEntries()) {
						scoreboard.resetScores(entry);
					}
					if (plugin.getBungeeCord().state == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (plugin.getBungeeCord().state == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getBungeeCord().getTimerManager().timeString(plugin.getBungeeCord().getTimerManager().getStartTime()))));
					}
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', Bukkit.getOnlinePlayers().size() + "/" + plugin.getBungeeCord().maxPlayers), scoreboard)).setScore(4);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getBungeeCord().getKitManager().getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
					onlinePlayer.setScoreboard(scoreboard);
				} else {
					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
					Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
					Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
					if (plugin.getBungeeCord().state == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (plugin.getBungeeCord().state == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getBungeeCord().getTimerManager().timeString(plugin.getBungeeCord().getTimerManager().getStartTime()))));
					}
					objective.setDisplaySlot(DisplaySlot.SIDEBAR);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', Bukkit.getOnlinePlayers().size() + "/" + plugin.getBungeeCord().maxPlayers), scoreboard)).setScore(4);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getBungeeCord().getKitManager().getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
					onlinePlayer.setScoreboard(scoreboard);
					plugin.getBungeeCord().playerStartScoreboard.put(onlinePlayer.getName(), scoreboard);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();

		if (plugin.getBungeeCord().state == ArenaState.WAITING) {
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (plugin.getBungeeCord().playerStartScoreboard.get(onlinePlayer.getName()) != null) {
					Scoreboard scoreboard = plugin.getBungeeCord().playerStartScoreboard.get(onlinePlayer.getName());
					Objective objective = scoreboard.getObjective("SpeedBuilders");
					for (String entry : scoreboard.getEntries()) {
						scoreboard.resetScores(entry);
					}
					if (plugin.getBungeeCord().state == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (plugin.getBungeeCord().state == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getBungeeCord().getTimerManager().timeString(plugin.getBungeeCord().getTimerManager().getStartTime()))));
					}
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', (Bukkit.getOnlinePlayers().size() - 1) + "/" + plugin.getBungeeCord().maxPlayers), scoreboard)).setScore(4);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getBungeeCord().getKitManager().getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
					onlinePlayer.setScoreboard(scoreboard);
				} else {
					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
					Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
					Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
					if (plugin.getBungeeCord().state == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (plugin.getBungeeCord().state == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getBungeeCord().getTimerManager().timeString(plugin.getBungeeCord().getTimerManager().getStartTime()))));
					}
					objective.setDisplaySlot(DisplaySlot.SIDEBAR);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', (Bukkit.getOnlinePlayers().size() - 1) + "/" + plugin.getBungeeCord().maxPlayers), scoreboard)).setScore(4);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getBungeeCord().getKitManager().getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
					onlinePlayer.setScoreboard(scoreboard);
					plugin.getBungeeCord().playerStartScoreboard.put(onlinePlayer.getName(), scoreboard);
				}
			}

			if (plugin.getBungeeCord().gameScoreboard.getTeam("Players").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Players").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

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
			player.setAllowFlight(false);
			player.setExp(0);
			player.setFireTicks(0);
			player.setFlying(false);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}
			player.updateInventory();
		} else if (plugin.getBungeeCord().state == ArenaState.STARTING) {
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			int neededPlayers = plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.needed-players");
			if ((Bukkit.getOnlinePlayers().size() - 1) < neededPlayers) {
				Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getStartTimerID());

				plugin.getBungeeCord().state = ArenaState.WAITING;

				Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.WAITING, Bukkit.getOnlinePlayers()));
			}

			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (plugin.getBungeeCord().playerStartScoreboard.get(onlinePlayer.getName()) != null) {
					Scoreboard scoreboard = plugin.getBungeeCord().playerStartScoreboard.get(onlinePlayer.getName());
					Objective objective = scoreboard.getObjective("SpeedBuilders");
					for (String entry : scoreboard.getEntries()) {
						scoreboard.resetScores(entry);
					}
					if (plugin.getBungeeCord().state == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (plugin.getBungeeCord().state == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getBungeeCord().getTimerManager().timeString(plugin.getBungeeCord().getTimerManager().getStartTime()))));
					}
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', (Bukkit.getOnlinePlayers().size() - 1) + "/" + plugin.getBungeeCord().maxPlayers), scoreboard)).setScore(4);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getBungeeCord().getKitManager().getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
					onlinePlayer.setScoreboard(scoreboard);
				} else {
					ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
					Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
					Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
					if (plugin.getBungeeCord().state == ArenaState.WAITING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
					}
					if (plugin.getBungeeCord().state == ArenaState.STARTING) {
						objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getBungeeCord().getTimerManager().timeString(plugin.getBungeeCord().getTimerManager().getStartTime()))));
					}
					objective.setDisplaySlot(DisplaySlot.SIDEBAR);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', (Bukkit.getOnlinePlayers().size() - 1) + "/" + plugin.getBungeeCord().maxPlayers), scoreboard)).setScore(4);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
					objective.getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getBungeeCord().getKitManager().getKit(onlinePlayer).toUpperCase())), scoreboard)).setScore(1);
					onlinePlayer.setScoreboard(scoreboard);
					plugin.getBungeeCord().playerStartScoreboard.put(onlinePlayer.getName(), scoreboard);
				}
			}

			if (plugin.getBungeeCord().gameScoreboard.getTeam("Players").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Players").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

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
			player.setAllowFlight(false);
			player.setExp(0);
			player.setFireTicks(0);
			player.setFlying(false);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}
			player.updateInventory();
		} else if (plugin.getBungeeCord().state == ArenaState.BEGINNING) {
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			String plot = plugin.getBungeeCord().plots.get(player.getName());
			if (plot != null) {
				plugin.getBungeeCord().playerPercent.remove(player.getName());
				plugin.getBungeeCord().plots.remove(player.getName());

				if (plugin.getBungeeCord().judgedPlayerArmorStand != null) {
					plugin.getBungeeCord().judgedPlayerArmorStand.remove();
				}

				plugin.getBungeeCord().getGuardianManager().laserGuardian(false);

				if (plugin.getBungeeCord().plots.size() == 1) {
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getGameStartTimerID());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getShowCaseTimerID());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getBuildTimerID());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID1());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID2());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID3());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID4());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID5());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID6());

					plugin.getBungeeCord().secondPlace = player.getName();
					plugin.getBungeeCord().firstPlace = (String) plugin.getBungeeCord().plots.keySet().toArray()[0];

					for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("BUNGEECORD.MAIN-GAME_END_DESCRIPTION")) {
							onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", plugin.getBungeeCord().firstPlace).replaceAll("%PLAYER2%", plugin.getBungeeCord().secondPlace).replaceAll("%PLAYER3%", plugin.getBungeeCord().thirdPlace)));
						}

						onlinePlayer.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + plugin.getBungeeCord().firstPlace), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 0, 7*20, 20);

						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
						if (onlinePlayer.getName().equals(plugin.getBungeeCord().firstPlace)) {
							new BukkitRunnable() {
								public void run() {
									for (String winnerCommand : plugin.getBungeeCord().winnerCommands) {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", onlinePlayer.getName()));
									}

									plugin.getStatsManager().incrementStat(StatsType.WINS, onlinePlayer, 1);

									Bukkit.getPluginManager().callEvent(new PlayerWinEvent(onlinePlayer));
								}
							}.runTaskLater(plugin, 5L);
						}
					}

					plugin.getBungeeCord().getTemplateManager().explodePlot(plot);

					new BukkitRunnable() {
						public void run() {
							plugin.getBungeeCord().getTimerManager().stop();
						}
					}.runTaskLater(plugin, 10*20L);
				} else {
					if (plugin.getBungeeCord().plots.size() == 2) {
						plugin.getBungeeCord().thirdPlace = player.getName();
					}

					plugin.getBungeeCord().getTemplateManager().explodePlot(plot);
				}

				for (String loserCommand : plugin.getBungeeCord().loserCommands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loserCommand.replaceFirst("/", "").replaceAll("%PLAYER%", player.getName()));
				}

				plugin.getStatsManager().incrementStat(StatsType.LOSSES, player, 1);

				Bukkit.getPluginManager().callEvent(new PlayerLoseEvent(player));
			}

			for (String entry : plugin.getBungeeCord().gameScoreboard.getEntries()) {
				plugin.getBungeeCord().gameScoreboard.resetScores(entry);
			}
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), plugin.getBungeeCord().gameScoreboard)).setScore(15);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-BUILD")), plugin.getBungeeCord().gameScoreboard)).setScore(14);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', plugin.getBungeeCord().currentBuildDisplayName), plugin.getBungeeCord().gameScoreboard)).setScore(13);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), plugin.getBungeeCord().gameScoreboard)).setScore(12);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-ROUND")), plugin.getBungeeCord().gameScoreboard)).setScore(11);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "" + plugin.getBungeeCord().currentRound), plugin.getBungeeCord().gameScoreboard)).setScore(10);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&3"), plugin.getBungeeCord().gameScoreboard)).setScore(9);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), plugin.getBungeeCord().gameScoreboard)).setScore(8);
			int score = 7;
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[0]), plugin.getBungeeCord().gameScoreboard)).setScore(7);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[1]), plugin.getBungeeCord().gameScoreboard)).setScore(6);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[2]), plugin.getBungeeCord().gameScoreboard)).setScore(5);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[3]), plugin.getBungeeCord().gameScoreboard)).setScore(4);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[4]), plugin.getBungeeCord().gameScoreboard)).setScore(3);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[5]), plugin.getBungeeCord().gameScoreboard)).setScore(2);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[6]), plugin.getBungeeCord().gameScoreboard)).setScore(1);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (onlinePlayer != player) {
					if (score >= 0) {
						if (!plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
							plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + onlinePlayer.getName()), plugin.getBungeeCord().gameScoreboard)).setScore(score);
							score--;
						}
					}
				}
			}
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				onlinePlayer.setScoreboard(plugin.getBungeeCord().gameScoreboard);
			}

			if (plugin.getBungeeCord().gameScoreboard.getTeam("Players").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Players").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

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
			player.setAllowFlight(false);
			player.setExp(0);
			player.setFireTicks(0);
			player.setFlying(false);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}
			player.updateInventory();
		} else if (plugin.getBungeeCord().state == ArenaState.DISPLAYING) {
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			String plot = plugin.getBungeeCord().plots.get(player.getName());
			if (plot != null) {
				plugin.getBungeeCord().playerPercent.remove(player.getName());
				plugin.getBungeeCord().plots.remove(player.getName());

				if (plugin.getBungeeCord().judgedPlayerArmorStand != null) {
					plugin.getBungeeCord().judgedPlayerArmorStand.remove();
				}

				plugin.getBungeeCord().getGuardianManager().laserGuardian(false);

				if (plugin.getBungeeCord().plots.size() == 1) {
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getGameStartTimerID());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getShowCaseTimerID());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getBuildTimerID());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID1());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID2());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID3());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID4());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID5());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID6());

					plugin.getBungeeCord().secondPlace = player.getName();
					plugin.getBungeeCord().firstPlace = (String) plugin.getBungeeCord().plots.keySet().toArray()[0];

					for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("BUNGEECORD.MAIN-GAME_END_DESCRIPTION")) {
							onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", plugin.getBungeeCord().firstPlace).replaceAll("%PLAYER2%", plugin.getBungeeCord().secondPlace).replaceAll("%PLAYER3%", plugin.getBungeeCord().thirdPlace)));
						}

						onlinePlayer.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + plugin.getBungeeCord().firstPlace), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 0, 7*20, 20);

						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
						if (onlinePlayer.getName().equals(plugin.getBungeeCord().firstPlace)) {
							new BukkitRunnable() {
								public void run() {
									for (String winnerCommand : plugin.getBungeeCord().winnerCommands) {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", onlinePlayer.getName()));
									}

									plugin.getStatsManager().incrementStat(StatsType.WINS, onlinePlayer, 1);

									Bukkit.getPluginManager().callEvent(new PlayerWinEvent(onlinePlayer));
								}
							}.runTaskLater(plugin, 5L);
						}
					}

					plugin.getBungeeCord().getTemplateManager().explodePlot(plot);

					new BukkitRunnable() {
						public void run() {
							plugin.getBungeeCord().getTimerManager().stop();
						}
					}.runTaskLater(plugin, 10*20L);
				} else {
					if (plugin.getBungeeCord().plots.size() == 2) {
						plugin.getBungeeCord().thirdPlace = player.getName();
					}

					plugin.getBungeeCord().getTemplateManager().explodePlot(plot);
				}

				for (String loserCommand : plugin.getBungeeCord().loserCommands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loserCommand.replaceFirst("/", "").replaceAll("%PLAYER%", player.getName()));
				}

				plugin.getStatsManager().incrementStat(StatsType.LOSSES, player, 1);

				Bukkit.getPluginManager().callEvent(new PlayerLoseEvent(player));
			}

			for (String entry : plugin.getBungeeCord().gameScoreboard.getEntries()) {
				plugin.getBungeeCord().gameScoreboard.resetScores(entry);
			}
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), plugin.getBungeeCord().gameScoreboard)).setScore(15);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-BUILD")), plugin.getBungeeCord().gameScoreboard)).setScore(14);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', plugin.getBungeeCord().currentBuildDisplayName), plugin.getBungeeCord().gameScoreboard)).setScore(13);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), plugin.getBungeeCord().gameScoreboard)).setScore(12);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-ROUND")), plugin.getBungeeCord().gameScoreboard)).setScore(11);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "" + plugin.getBungeeCord().currentRound), plugin.getBungeeCord().gameScoreboard)).setScore(10);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&3"), plugin.getBungeeCord().gameScoreboard)).setScore(9);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), plugin.getBungeeCord().gameScoreboard)).setScore(8);
			int score = 7;
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[0]), plugin.getBungeeCord().gameScoreboard)).setScore(7);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[1]), plugin.getBungeeCord().gameScoreboard)).setScore(6);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[2]), plugin.getBungeeCord().gameScoreboard)).setScore(5);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[3]), plugin.getBungeeCord().gameScoreboard)).setScore(4);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[4]), plugin.getBungeeCord().gameScoreboard)).setScore(3);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[5]), plugin.getBungeeCord().gameScoreboard)).setScore(2);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[6]), plugin.getBungeeCord().gameScoreboard)).setScore(1);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (onlinePlayer != player) {
					if (score >= 0) {
						if (!plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
							plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + onlinePlayer.getName()), plugin.getBungeeCord().gameScoreboard)).setScore(score);
							score--;
						}
					}
				}
			}
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				onlinePlayer.setScoreboard(plugin.getBungeeCord().gameScoreboard);
			}

			if (plugin.getBungeeCord().gameScoreboard.getTeam("Players").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Players").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

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
			player.setAllowFlight(false);
			player.setExp(0);
			player.setFireTicks(0);
			player.setFlying(false);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}
			player.updateInventory();
		} else if (plugin.getBungeeCord().state == ArenaState.BUILDING) {
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			String plot = plugin.getBungeeCord().plots.get(player.getName());
			if (plot != null) {
				plugin.getBungeeCord().playerPercent.remove(player.getName());
				plugin.getBungeeCord().plots.remove(player.getName());

				if (plugin.getBungeeCord().judgedPlayerArmorStand != null) {
					plugin.getBungeeCord().judgedPlayerArmorStand.remove();
				}

				plugin.getBungeeCord().getGuardianManager().laserGuardian(false);

				if (plugin.getBungeeCord().plots.size() == 1) {
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getGameStartTimerID());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getShowCaseTimerID());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getBuildTimerID());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID1());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID2());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID3());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID4());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID5());
					Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID6());

					plugin.getBungeeCord().secondPlace = player.getName();
					plugin.getBungeeCord().firstPlace = (String) plugin.getBungeeCord().plots.keySet().toArray()[0];

					for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("BUNGEECORD.MAIN-GAME_END_DESCRIPTION")) {
							onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", plugin.getBungeeCord().firstPlace).replaceAll("%PLAYER2%", plugin.getBungeeCord().secondPlace).replaceAll("%PLAYER3%", plugin.getBungeeCord().thirdPlace)));
						}

						onlinePlayer.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + plugin.getBungeeCord().firstPlace), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 0, 7*20, 20);

						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
						if (onlinePlayer.getName().equals(plugin.getBungeeCord().firstPlace)) {
							new BukkitRunnable() {
								public void run() {
									for (String winnerCommand : plugin.getBungeeCord().winnerCommands) {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", onlinePlayer.getName()));
									}

									plugin.getStatsManager().incrementStat(StatsType.WINS, onlinePlayer, 1);

									Bukkit.getPluginManager().callEvent(new PlayerWinEvent(onlinePlayer));
								}
							}.runTaskLater(plugin, 5L);
						}
					}

					plugin.getBungeeCord().getTemplateManager().explodePlot(plot);

					new BukkitRunnable() {
						public void run() {
							plugin.getBungeeCord().getTimerManager().stop();
						}
					}.runTaskLater(plugin, 10*20L);
				} else {
					if (plugin.getBungeeCord().playerPercent.size() >= 2 && Collections.min(plugin.getBungeeCord().playerPercent.values()) == 100) {
						plugin.getBungeeCord().getTimerManager().guardianIsImpressed();
					}

					if (plugin.getBungeeCord().plots.size() == 2) {
						plugin.getBungeeCord().thirdPlace = player.getName();
					}

					plugin.getBungeeCord().getTemplateManager().explodePlot(plot);
				}

				for (String loserCommand : plugin.getBungeeCord().loserCommands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loserCommand.replaceFirst("/", "").replaceAll("%PLAYER%", player.getName()));
				}

				plugin.getStatsManager().incrementStat(StatsType.LOSSES, player, 1);

				Bukkit.getPluginManager().callEvent(new PlayerLoseEvent(player));
			}

			for (String entry : plugin.getBungeeCord().gameScoreboard.getEntries()) {
				plugin.getBungeeCord().gameScoreboard.resetScores(entry);
			}
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), plugin.getBungeeCord().gameScoreboard)).setScore(15);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-BUILD")), plugin.getBungeeCord().gameScoreboard)).setScore(14);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', plugin.getBungeeCord().currentBuildDisplayName), plugin.getBungeeCord().gameScoreboard)).setScore(13);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), plugin.getBungeeCord().gameScoreboard)).setScore(12);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-ROUND")), plugin.getBungeeCord().gameScoreboard)).setScore(11);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "" + plugin.getBungeeCord().currentRound), plugin.getBungeeCord().gameScoreboard)).setScore(10);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&3"), plugin.getBungeeCord().gameScoreboard)).setScore(9);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), plugin.getBungeeCord().gameScoreboard)).setScore(8);
			int score = 7;
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[0]), plugin.getBungeeCord().gameScoreboard)).setScore(7);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[1]), plugin.getBungeeCord().gameScoreboard)).setScore(6);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[2]), plugin.getBungeeCord().gameScoreboard)).setScore(5);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[3]), plugin.getBungeeCord().gameScoreboard)).setScore(4);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[4]), plugin.getBungeeCord().gameScoreboard)).setScore(3);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[5]), plugin.getBungeeCord().gameScoreboard)).setScore(2);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[6]), plugin.getBungeeCord().gameScoreboard)).setScore(1);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (onlinePlayer != player) {
					if (score >= 0) {
						if (!plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
							plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + onlinePlayer.getName()), plugin.getBungeeCord().gameScoreboard)).setScore(score);
							score--;
						}
					}
				}
			}
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				onlinePlayer.setScoreboard(plugin.getBungeeCord().gameScoreboard);
			}

			if (plugin.getBungeeCord().gameScoreboard.getTeam("Players").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Players").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

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
			player.setAllowFlight(false);
			player.setExp(0);
			player.setFireTicks(0);
			player.setFlying(false);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}
			player.updateInventory();
		} else if (plugin.getBungeeCord().state == ArenaState.JUDGING) {
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT")).replaceAll("%PLAYER%", player.getName()));

			String plot = plugin.getBungeeCord().plots.get(player.getName());
			if (plot != null) {
				plugin.getBungeeCord().playerPercent.remove(player.getName());
				plugin.getBungeeCord().plots.remove(player.getName());

				if (plugin.getBungeeCord().judgedPlayerArmorStand != null) {
					plugin.getBungeeCord().judgedPlayerArmorStand.remove();
				}

				plugin.getBungeeCord().getGuardianManager().laserGuardian(false);

				if (player.getName().equalsIgnoreCase(plugin.getBungeeCord().judgedPlayerName)) {
					if (plugin.getBungeeCord().plots.size() == 1) {
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getGameStartTimerID());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getShowCaseTimerID());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getBuildTimerID());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID1());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID2());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID3());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID4());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID5());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID6());

						plugin.getBungeeCord().secondPlace = player.getName();
						plugin.getBungeeCord().firstPlace = (String) plugin.getBungeeCord().plots.keySet().toArray()[0];

						for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
							for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("BUNGEECORD.MAIN-GAME_END_DESCRIPTION")) {
								onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", plugin.getBungeeCord().firstPlace).replaceAll("%PLAYER2%", plugin.getBungeeCord().secondPlace).replaceAll("%PLAYER3%", plugin.getBungeeCord().thirdPlace)));
							}

							onlinePlayer.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + plugin.getBungeeCord().firstPlace), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 0, 7*20, 20);

							onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
							if (onlinePlayer.getName().equals(plugin.getBungeeCord().firstPlace)) {
								new BukkitRunnable() {
									public void run() {
										for (String winnerCommand : plugin.getBungeeCord().winnerCommands) {
											Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", onlinePlayer.getName()));
										}

										plugin.getStatsManager().incrementStat(StatsType.WINS, onlinePlayer, 1);

										Bukkit.getPluginManager().callEvent(new PlayerWinEvent(onlinePlayer));
									}
								}.runTaskLater(plugin, 5L);
							}
						}

						plugin.getBungeeCord().getTemplateManager().explodePlot(plot);

						new BukkitRunnable() {
							public void run() {
								plugin.getBungeeCord().getTimerManager().stop();
							}
						}.runTaskLater(plugin, 10*20L);
					} else {
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getGameStartTimerID());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getShowCaseTimerID());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getBuildTimerID());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID1());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID2());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID3());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID4());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID5());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID6());

						if (plugin.getBungeeCord().plots.size() == 2) {
							plugin.getBungeeCord().thirdPlace = player.getName();
						}

						plugin.getBungeeCord().getTemplateManager().explodePlot(plot);

						new BukkitRunnable() {
							public void run() {
								for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
									if (plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
										onlinePlayer.getInventory().setArmorContents(null);
										onlinePlayer.getInventory().clear();
										onlinePlayer.setExp(0);
										onlinePlayer.setFireTicks(0);
										onlinePlayer.setFoodLevel(20);
										onlinePlayer.setGameMode(GameMode.SURVIVAL);
										onlinePlayer.setHealth(20);
										onlinePlayer.setLevel(0);
										onlinePlayer.setAllowFlight(false);
										onlinePlayer.setFlying(false);
										for (PotionEffect potionEffect : onlinePlayer.getActivePotionEffects()) {
											onlinePlayer.removePotionEffect(potionEffect.getType());
										}

										if (plugin.getBungeeCord().gameScoreboard.getPlayerTeam(onlinePlayer) != null) {
											if (plugin.getBungeeCord().gameScoreboard.getPlayerTeam(onlinePlayer).getName().equals("Players")) {
												String plot = plugin.getBungeeCord().plots.get(onlinePlayer.getName());
												Location location = new Location(Bukkit.getWorld(plugin.getBungeeCord().currentMap), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.x"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.y"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.z"));
												location.setPitch((float) plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.pitch"));
												location.setYaw((float) plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.yaw"));
												onlinePlayer.teleport(location);
												onlinePlayer.setFallDistance(0F);
											}
										}
									}
								}

								plugin.getBungeeCord().getTimerManager().showCaseTimer();
							}
						}.runTaskLater(plugin, 5L);
					}
				} else {
					if (plugin.getBungeeCord().plots.size() == 1) {
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getGameStartTimerID());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getShowCaseTimerID());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getBuildTimerID());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID1());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID2());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID3());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID4());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID5());
						Bukkit.getScheduler().cancelTask(plugin.getBungeeCord().getTimerManager().getJudgeTimerID6());

						plugin.getBungeeCord().secondPlace = (String) plugin.getBungeeCord().plots.keySet().toArray()[0];
						plugin.getBungeeCord().firstPlace = player.getName();

						for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
							for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("BUNGEECORD.MAIN-GAME_END_DESCRIPTION")) {
								onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", plugin.getBungeeCord().firstPlace).replaceAll("%PLAYER2%", plugin.getBungeeCord().secondPlace).replaceAll("%PLAYER3%", plugin.getBungeeCord().thirdPlace)));
							}

							onlinePlayer.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + plugin.getBungeeCord().firstPlace), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 0, 7*20, 20);

							onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
							if (onlinePlayer.getName().equals(plugin.getBungeeCord().firstPlace)) {
								new BukkitRunnable() {
									public void run() {
										for (String winnerCommand : plugin.getBungeeCord().winnerCommands) {
											Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", onlinePlayer.getName()));
										}

										plugin.getStatsManager().incrementStat(StatsType.WINS, onlinePlayer, 1);

										Bukkit.getPluginManager().callEvent(new PlayerWinEvent(onlinePlayer));
									}
								}.runTaskLater(plugin, 5L);
							}
						}

						plugin.getBungeeCord().getTemplateManager().explodePlot(plot);

						new BukkitRunnable() {
							public void run() {
								plugin.getBungeeCord().getTimerManager().stop();
							}
						}.runTaskLater(plugin, 10*20L);
					} else {
						if (plugin.getBungeeCord().plots.size() == 2) {
							plugin.getBungeeCord().thirdPlace = player.getName();
						}

						plugin.getBungeeCord().getTemplateManager().explodePlot(plot);
					}
				}
			}

			for (String entry : plugin.getBungeeCord().gameScoreboard.getEntries()) {
				plugin.getBungeeCord().gameScoreboard.resetScores(entry);
			}
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), plugin.getBungeeCord().gameScoreboard)).setScore(15);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-BUILD")), plugin.getBungeeCord().gameScoreboard)).setScore(14);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', plugin.getBungeeCord().currentBuildDisplayName), plugin.getBungeeCord().gameScoreboard)).setScore(13);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), plugin.getBungeeCord().gameScoreboard)).setScore(12);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-ROUND")), plugin.getBungeeCord().gameScoreboard)).setScore(11);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "" + plugin.getBungeeCord().currentRound), plugin.getBungeeCord().gameScoreboard)).setScore(10);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&3"), plugin.getBungeeCord().gameScoreboard)).setScore(9);
			plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), plugin.getBungeeCord().gameScoreboard)).setScore(8);
			int score = 7;
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[0]), plugin.getBungeeCord().gameScoreboard)).setScore(7);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[1]), plugin.getBungeeCord().gameScoreboard)).setScore(6);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[2]), plugin.getBungeeCord().gameScoreboard)).setScore(5);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[3]), plugin.getBungeeCord().gameScoreboard)).setScore(4);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[4]), plugin.getBungeeCord().gameScoreboard)).setScore(3);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[5]), plugin.getBungeeCord().gameScoreboard)).setScore(2);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			try {
				plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&f" + (String) plugin.getBungeeCord().plots.keySet().toArray()[6]), plugin.getBungeeCord().gameScoreboard)).setScore(1);
				score--;
			} catch (ArrayIndexOutOfBoundsException ex) {}
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (onlinePlayer != player) {
					if (score >= 0) {
						if (!plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
							plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + onlinePlayer.getName()), plugin.getBungeeCord().gameScoreboard)).setScore(score);
							score--;
						}
					}
				}
			}
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				onlinePlayer.setScoreboard(plugin.getBungeeCord().gameScoreboard);
			}

			if (plugin.getBungeeCord().gameScoreboard.getTeam("Players").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Players").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					player.teleport(location);
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}
			} else if (plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").hasPlayer(player)) {
				plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").removePlayer(player);
				plugin.getBungeeCord().getKitManager().setKit(player, null);

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
			player.setAllowFlight(false);
			player.setExp(0);
			player.setFireTicks(0);
			player.setFlying(false);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
			player.setHealth(20);
			player.setLevel(0);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}
			player.updateInventory();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Action action = e.getAction();
		if (plugin.getBungeeCord().state == ArenaState.WAITING) {
			if (player.getItemInHand().getType() == Material.CLOCK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")))) {
				if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
					e.setCancelled(true);
				}
				if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
					e.setCancelled(true);

					ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
					byteArrayDataOutput.writeUTF("Connect");
					byteArrayDataOutput.writeUTF(plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.lobby-server-name"));
					player.sendPluginMessage(plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
				}
			}
			if (player.getItemInHand().getType() == Material.BOOK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-STATS_ITEM")))) {
				if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						e.setCancelled(true);
					}
					if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
						e.setCancelled(true);

						plugin.getStatsManager().showStats(player, player);
					}
				}
			}
		} else if (plugin.getBungeeCord().state == ArenaState.STARTING) {
			if (player.getItemInHand().getType() == Material.CLOCK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")))) {
				if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
					e.setCancelled(true);
				}
				if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
					e.setCancelled(true);

					ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
					byteArrayDataOutput.writeUTF("Connect");
					byteArrayDataOutput.writeUTF(plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.lobby-server-name"));
					player.sendPluginMessage(plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
				}
			}
			if (player.getItemInHand().getType() == Material.BOOK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-STATS_ITEM")))) {
				if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						e.setCancelled(true);
					}
					if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
						e.setCancelled(true);

						plugin.getStatsManager().showStats(player, player);
					}
				}
			}
		} else if (plugin.getBungeeCord().state == ArenaState.DISPLAYING) {
			if (player.getItemInHand().getType() == Material.CLOCK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")))) {
				if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
					e.setCancelled(true);
				}
				if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
					e.setCancelled(true);

					ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
					byteArrayDataOutput.writeUTF("Connect");
					byteArrayDataOutput.writeUTF(plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.lobby-server-name"));
					player.sendPluginMessage(plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
				}
			}
			if (player.getItemInHand().getType() == Material.BOOK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-STATS_ITEM")))) {
				if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						e.setCancelled(true);
					}
					if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
						e.setCancelled(true);

						plugin.getStatsManager().showStats(player, player);
					}
				}
			}
		} else if (plugin.getBungeeCord().state == ArenaState.BUILDING) {
			if (player.getItemInHand().getType() == Material.CLOCK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")))) {
				if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
					e.setCancelled(true);
				}
				if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
					e.setCancelled(true);

					ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
					byteArrayDataOutput.writeUTF("Connect");
					byteArrayDataOutput.writeUTF(plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.lobby-server-name"));
					player.sendPluginMessage(plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
				}
			}
			if (player.getItemInHand().getType() == Material.BOOK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-STATS_ITEM")))) {
				if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						e.setCancelled(true);
					}
					if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
						e.setCancelled(true);

						plugin.getStatsManager().showStats(player, player);
					}
				}
			}

			if (plugin.getBungeeCord().plots.containsKey(player.getName())) {
				if (plugin.getBungeeCord().playerPercent.containsKey(player.getName())) {
					if (plugin.getBungeeCord().playerPercent.get(player.getName()) < 100) {
						if (action == Action.LEFT_CLICK_BLOCK) {
							e.setCancelled(true);

							Block block = e.getClickedBlock();
							Location location1 = block.getLocation();
							Location location2 = new Location(Bukkit.getWorld(plugin.getBungeeCord().currentMap), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.x1"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.y1"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.z1"));
							Location location3 = new Location(Bukkit.getWorld(plugin.getBungeeCord().currentMap), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.x2"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.y2"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.z2"));

							if (isBlockInside(location1, location2, location3)) {
								BlockState blockState = block.getState();
								BlockData blockData = blockState.getBlockData();

								if (blockData.getMaterial() == Material.WALL_TORCH) {
									player.getInventory().addItem(new ItemStack(Material.TORCH));
								} if (blockData instanceof Slab) {
									Slab slab = (Slab) blockData;

									if (slab.getType() == Slab.Type.DOUBLE) {
										player.getInventory().addItem(new ItemStack(Material.getMaterial(blockData.getMaterial().toString()), 2));
									} else {
										player.getInventory().addItem(new ItemStack(Material.getMaterial(blockData.getMaterial().toString())));
									}
								} else {
									player.getInventory().addItem(new ItemStack(Material.getMaterial(blockData.getMaterial().toString())));
								}

								block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());

								blockState.setType(Material.AIR);
								blockState.update(true, false);

								// plugin.getBungeeCord().getNMSManager().updateBlockConnections(block);

								plugin.getBungeeCord().getTemplateManager().check(plugin.getBungeeCord().plots.get(player.getName()), player);
							}
						}
					}
				}
			}
		} else if (plugin.getBungeeCord().state == ArenaState.JUDGING) {
			if (player.getItemInHand().getType() == Material.CLOCK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")))) {
				if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
					e.setCancelled(true);
				}
				if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
					e.setCancelled(true);

					ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
					byteArrayDataOutput.writeUTF("Connect");
					byteArrayDataOutput.writeUTF(plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.lobby-server-name"));
					player.sendPluginMessage(plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
				}
			}
			if (player.getItemInHand().getType() == Material.BOOK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-STATS_ITEM")))) {
				if (plugin.getConfigManager().getConfig("config.yml").getBoolean("stats.enabled")) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						e.setCancelled(true);
					}
					if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
						e.setCancelled(true);

						plugin.getStatsManager().showStats(player, player);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		if (plugin.getBungeeCord().state == ArenaState.BEGINNING) {
			if (plugin.getBungeeCord().plots.containsKey(player.getName())) {
				if (((e.getTo().getX() != e.getFrom().getX()) || (e.getTo().getZ() != e.getFrom().getZ()))) {
					Location location = e.getFrom();
					location.setPitch(e.getTo().getPitch());
					location.setYaw(e.getTo().getYaw());
					e.setTo(location);
				}
			}
		} else if (plugin.getBungeeCord().state == ArenaState.DISPLAYING) {
			if (plugin.getBungeeCord().plots.containsKey(player.getName())) {
				if (!isPlayerInsideAsPlayer(e.getTo(), new Location(player.getWorld(), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.x1"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.y1"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.z1")), new Location(player.getWorld(), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.x2"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.y2"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.z2")))) {
					String plot = plugin.getBungeeCord().plots.get(player.getName());
					Location location = new Location(Bukkit.getWorld(plugin.getBungeeCord().currentMap), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.x"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.y"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.yaw"));
					e.setTo(location);
					player.setFallDistance(0F);

					player.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_CANNOT_LEAVE")), 0, 2*15, 10);
				}
			} else {
				if (isPlayerInsideAsSpectator(e.getTo(), new Location(player.getWorld(), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.x1"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.y1"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.z1")), new Location(player.getWorld(), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.x2"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.y2"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.z2")))) {
					//
				} else {
					//
				}
			}
		} else if (plugin.getBungeeCord().state == ArenaState.BUILDING) {
			if (plugin.getBungeeCord().plots.containsKey(player.getName())) {
				if (!isPlayerInsideAsPlayer(e.getTo(), new Location(player.getWorld(), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.x1"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.y1"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.z1")), new Location(player.getWorld(), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.x2"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.y2"), plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".area.z2")))) {
					String plot = plugin.getBungeeCord().plots.get(player.getName());
					Location location = new Location(Bukkit.getWorld(plugin.getBungeeCord().currentMap), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.x"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.y"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.yaw"));
					e.setTo(location);
					player.setFallDistance(0F);

					player.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_CANNOT_LEAVE")), 0, 2*15, 10);
				}
				if (!plugin.getBungeeCord().playersDoubleJumpCooldowned.containsKey(player.getName())) {
					player.setAllowFlight(true);
				} else {
					player.setAllowFlight(false);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (e.getCause() == TeleportCause.SPECTATE) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent e) {
		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		e.setFoodLevel(20);
		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
		final Player player = e.getPlayer();
		if (plugin.getBungeeCord().state == ArenaState.BUILDING) {
			if (!plugin.getBungeeCord().playersDoubleJumpCooldowned.containsKey(player.getName())) {
				if (plugin.getBungeeCord().plots.containsKey(player.getName())) {
					e.setCancelled(true);

					player.setVelocity(new Vector(0, 1, 0).multiply(1.05));
					player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);

					plugin.getBungeeCord().playersDoubleJumpCooldowned.put(player.getName(), 1.5F);
					plugin.getBungeeCord().getTimerManager().cooldownTimer(player.getName());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		InventoryView inventoryView = e.getView();
		ItemStack itemStack1 = e.getCurrentItem();
		if (itemStack1 != null) {
			if (inventoryView.getTitle().startsWith(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY")))) {
				e.setCancelled(true);
				player.updateInventory();
			} else {
				if (player.hasPermission("sb.command.setup")) {
					if (plugin.getBungeeCord().state != ArenaState.WAITING && plugin.getBungeeCord().state != ArenaState.BUILDING) {
						e.setCancelled(true);
						player.updateInventory();
					} else {
						ItemStack itemStack2 = new ItemStack(Material.CLOCK);
						ItemMeta itemMeta2 = itemStack2.getItemMeta();
						itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")));
						itemStack2.setItemMeta(itemMeta2);

						ItemStack itemStack3 = new ItemStack(Material.BOOK);
						ItemMeta itemMeta3 = itemStack3.getItemMeta();
						itemMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-STATS_ITEM")));
						itemStack3.setItemMeta(itemMeta3);

						if (itemStack1.equals(itemStack2) || itemStack1.equals(itemStack3)) {
							e.setCancelled(true);
							player.updateInventory();
						}
					}
				} else {
					if (plugin.getBungeeCord().state != ArenaState.BUILDING) {
						e.setCancelled(true);
						player.updateInventory();
					} else {
						ItemStack itemStack2 = new ItemStack(Material.CLOCK);
						ItemMeta itemMeta2 = itemStack2.getItemMeta();
						itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")));
						itemStack2.setItemMeta(itemMeta2);

						ItemStack itemStack3 = new ItemStack(Material.BOOK);
						ItemMeta itemMeta3 = itemStack3.getItemMeta();
						itemMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-STATS_ITEM")));
						itemStack3.setItemMeta(itemMeta3);

						if (itemStack1.equals(itemStack2) || itemStack1.equals(itemStack3)) {
							e.setCancelled(true);
							player.updateInventory();
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		Player player = e.getPlayer();
		ItemStack itemStack1 = e.getItemDrop().getItemStack();
		if (itemStack1 != null) {
			if (player.hasPermission("sb.command.setup")) {
				if (plugin.getBungeeCord().state != ArenaState.WAITING) {
					e.setCancelled(true);
					player.updateInventory();
				} else {
					ItemStack itemStack2 = new ItemStack(Material.CLOCK);
					ItemMeta itemMeta2 = itemStack2.getItemMeta();
					itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")));
					itemStack2.setItemMeta(itemMeta2);

					ItemStack itemStack3 = new ItemStack(Material.BOOK);
					ItemMeta itemMeta3 = itemStack3.getItemMeta();
					itemMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-STATS_ITEM")));
					itemStack3.setItemMeta(itemMeta3);

					if (itemStack1.equals(itemStack2) || itemStack1.equals(itemStack3)) {
						e.setCancelled(true);
						player.updateInventory();
					}
				}
			} else {
				e.setCancelled(true);
				player.updateInventory();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		if (plugin.getBungeeCord().state != ArenaState.WAITING) {
			e.getBlock().getRelative(BlockFace.UP).getState().update();
			e.getBlock().getState().update();
			e.getBlock().getRelative(BlockFace.DOWN).getState().update();
			e.setCancelled(true);
		} else if (!player.isOp()) {
			e.getBlock().getRelative(BlockFace.UP).getState().update();
			e.getBlock().getState().update();
			e.getBlock().getRelative(BlockFace.DOWN).getState().update();
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player player = e.getPlayer();
		if (plugin.getBungeeCord().state == ArenaState.BUILDING) {
			Location location1 = e.getBlock().getLocation();
			Location location2 = new Location(Bukkit.getWorld(plugin.getBungeeCord().currentMap), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.x1"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.y1"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.z1"));
			Location location3 = new Location(Bukkit.getWorld(plugin.getBungeeCord().currentMap), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.x2"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.y2"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(player.getName()) + ".build-area.z2"));
			if (isBlockInside(location1, location2, location3)) {
				new BukkitRunnable() {
					public void run() {
						plugin.getBungeeCord().getTemplateManager().check(plugin.getBungeeCord().plots.get(player.getName()), player);
					}
				}.runTaskLater(plugin, 1L);
			} else {
				e.setCancelled(true);
			}
		} else if (!player.isOp()) {
			e.getBlock().getRelative(BlockFace.UP).getState().update();
			e.getBlock().getState().update();
			e.getBlock().getRelative(BlockFace.DOWN).getState().update();
			e.setCancelled(true);
		}
	}

	public Location getCenter(Location location1, Location location2) {
		double x1 = Math.min(location1.getX(), location2.getX());
		double y1 = Math.min(location1.getY(), location2.getY());
		double z1 = Math.min(location1.getZ(), location2.getZ());
		double x2 = Math.max(location1.getX(), location2.getX());
		double z2 = Math.max(location1.getZ(), location2.getZ());

		return new Location(location1.getWorld(), x1 + (x2 - x1) / 2.0D, y1, z1 + (z2 - z1) / 2.0D);
	}

	public boolean isBlockInside(Location location1, Location location2, Location location3) {
		double x = location1.getBlockX();
		double y = location1.getBlockY();
		double z = location1.getBlockZ();

		double x1 = Math.min(location2.getBlockX(), location3.getBlockX());
		double y1 = Math.min(location2.getBlockY(), location3.getBlockY()) + 1;
		double z1 = Math.min(location2.getBlockZ(), location3.getBlockZ());
		double x2 = Math.max(location2.getBlockX(), location3.getBlockX());
		double y2 = Math.max(location2.getBlockY(), location3.getBlockY());
		double z2 = Math.max(location2.getBlockZ(), location3.getBlockZ());

		return (x >= x1 && y >= y1 && z >= z1 && x <= x2 && y <= y2 && z <= z2);
	}

	public boolean isPlayerInsideAsPlayer(Location location1, Location location2, Location location3) {
		double x = location1.getBlockX();
		double y = location1.getBlockY();
		double z = location1.getBlockZ();

		double x1 = Math.min(location2.getBlockX(), location3.getBlockX());
		double y1 = Math.min(location2.getBlockY(), location3.getBlockY());
		double z1 = Math.min(location2.getBlockZ(), location3.getBlockZ());
		double x2 = Math.max(location2.getBlockX(), location3.getBlockX());
		double y2 = Math.max(location2.getBlockY(), location3.getBlockY());
		double z2 = Math.max(location2.getBlockZ(), location3.getBlockZ());

		return (x >= x1 && y >= y1 && z >= z1 && x <= x2 && y <= y2 && z <= z2);
	}

	public boolean isPlayerInsideAsSpectator(Location location1, Location location2, Location location3) {
		double x = location1.getBlockX();
		double y = location1.getBlockY();
		double z = location1.getBlockZ();

		double x1 = Math.min(location2.getBlockX(), location3.getBlockX()) - 2;
		double y1 = Math.min(location2.getBlockY(), location3.getBlockY()) + 1;
		double z1 = Math.min(location2.getBlockZ(), location3.getBlockZ()) - 2;
		double x2 = Math.max(location2.getBlockX(), location3.getBlockX()) + 2;
		double y2 = Math.max(location2.getBlockY(), location3.getBlockY());
		double z2 = Math.max(location2.getBlockZ(), location3.getBlockZ()) + 2;

		return (x >= x1 && y >= y1 && z >= z1 && x <= x2 && y <= y2 && z <= z2);
	}
}
