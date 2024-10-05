package ee.mathiaskivi.speedbuilders.bungeecord.manager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.api.event.ArenaStateChangeEvent;
import ee.mathiaskivi.speedbuilders.api.event.PlayerLoseEvent;
import ee.mathiaskivi.speedbuilders.api.event.PlayerWinEvent;
import ee.mathiaskivi.speedbuilders.api.game.ArenaState;
import ee.mathiaskivi.speedbuilders.utility.StatsType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Random;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

@SuppressWarnings("deprecation")
public class TimerManager {
	public SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public int startTime;
	public int startTimerID;
	public float gameStartTime;
	public int gameStartTimerID;
	public int showCaseTime;
	public int showCaseTimerID;
	public float buildTime;
	public int buildTimerID;
	public float judgeTime;
	public int judgeTimerID1;
	public int judgeTimerID2;
	public int judgeTimerID3;
	public int judgeTimerID4;
	public int judgeTimerID5;
	public int judgeTimerID6;

	public void startTimer() {
		plugin.getBungeeCord().state = ArenaState.STARTING;

		Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.STARTING, Bukkit.getOnlinePlayers()));

		startTime = plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.start-time");
		startTimerID = new BukkitRunnable() {
			public void run() {
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
				if (startTime == 0) {
					this.cancel();
					startTime = plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.start-time");
					gameStartTimer();
					return;
				}
				startTime--;
			}
		}.runTaskTimer(plugin, 0L, 20L).getTaskId();
	}

	public void gameStartTimer() {
		for (Entity entity : Bukkit.getWorld(plugin.getBungeeCord().currentMap).getEntities()) {
			if (entity.getType() != EntityType.PLAYER) {
				entity.remove();
			}
		}

		plugin.getBungeeCord().getGuardianManager().spawnGuardian();
		plugin.getBungeeCord().getTemplateManager().resetPlots(plugin.getBungeeCord().currentMap);

		List<String> plots = new ArrayList<>();
		for (String plot : plugin.getConfigManager().getConfig("maps.yml").getConfigurationSection("maps." + plugin.getBungeeCord().currentMap + ".plots").getKeys(false)) {
			if (!plot.equals("guardian")) {
				plots.add(plot);
			}
		}

		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if (plugin.getBungeeCord().gameScoreboard.getPlayerTeam(onlinePlayer) != null) {
				if (plugin.getBungeeCord().gameScoreboard.getPlayerTeam(onlinePlayer).getName().equals("Players")) {
					String plot = plots.get(0);
					plugin.getBungeeCord().plots.put(onlinePlayer.getName(), plot);
					plots.remove(plot);

					Location location = new Location(Bukkit.getWorld(plugin.getBungeeCord().currentMap), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.x"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.y"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".spawnpoint.yaw"));
					onlinePlayer.teleport(location);
					onlinePlayer.setFallDistance(0F);
				}
			}

			for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("BUNGEECORD.MAIN-GAME_START_DESCRIPTION")) {
				onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string));
			}

			onlinePlayer.getInventory().setArmorContents(null);
			onlinePlayer.getInventory().clear();
			onlinePlayer.setAllowFlight(false);
			onlinePlayer.setExp(0);
			onlinePlayer.setFireTicks(0);
			onlinePlayer.setFlying(false);
			onlinePlayer.setFoodLevel(20);
			onlinePlayer.setGameMode(GameMode.SURVIVAL);
			onlinePlayer.setHealth(20);
			onlinePlayer.setLevel(0);
			for (PotionEffect potionEffect : onlinePlayer.getActivePotionEffects()) {
				onlinePlayer.removePotionEffect(potionEffect.getType());
			}

			plugin.getBungeeCord().getKitManager().giveKitItems(onlinePlayer);
			onlinePlayer.updateInventory();

			onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
		}

		for (String plot : plots) {
			if (plugin.getConfigManager().getConfig("maps.yml").contains("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".blocks")) {
				for (String block : plugin.getConfigManager().getConfig("maps.yml").getConfigurationSection("maps." + plugin.getBungeeCord().currentMap + ".plots." + plot + ".blocks").getKeys(false)) {
					String[] blockCoordinates = block.split(",");

					BlockState blockState = Bukkit.getWorld(plugin.getBungeeCord().currentMap).getBlockAt(Integer.parseInt(blockCoordinates[0]), Integer.parseInt(blockCoordinates[1]), Integer.parseInt(blockCoordinates[2])).getState();
					blockState.setType(Material.AIR);
					blockState.update(true, false);
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
			if (score >= 0) {
				if (!plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
					plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + onlinePlayer.getName()), plugin.getBungeeCord().gameScoreboard)).setScore(score);
					score--;
				}
			}
		}
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			onlinePlayer.setScoreboard(plugin.getBungeeCord().gameScoreboard);
		}

		plugin.getBungeeCord().state = ArenaState.BEGINNING;

		Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.BEGINNING, plugin.getBungeeCord().plots.keySet().stream().map(p -> Bukkit.getPlayer(p)).toList()));

		gameStartTime = (float) plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.game-start-time");
		gameStartTimerID = new BukkitRunnable() {
			public void run() {
				gameStartTime = gameStartTime - 0.1F;
				String gameStartTimeFormat = String.format(Locale.UK, "%.1f", gameStartTime);
				gameStartTime = Float.parseFloat(gameStartTimeFormat);

				if (gameStartTime <= 0) {
					this.cancel();
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', translate("ABAR-GAME_START")) + " &a▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌ &f0.0 " + translate("MAIN-SECONDS")));
						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
					}
					showCaseTimer();
					return;
				}

				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(ChatColor.translateAlternateColorCodes('&', translate("ABAR-GAME_START")) + " ");
				int fullDisplay = 24;
				double timeIncompleted = fullDisplay * (gameStartTime / (float) plugin.getConfigManager().getConfig("config.yml").getDouble("bungeecord.game-start-time"));
				double timeCompleted = fullDisplay - timeIncompleted;
				for (int i = 0; i < timeCompleted; i++) {
					stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&a▌"));
				}
				for (int i = 0; i < timeIncompleted; i++) {
					stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&c▌"));
				}
				stringBuilder.append(ChatColor.translateAlternateColorCodes('&', " &f" + gameStartTime + " " + translate("MAIN-SECONDS")));

				for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
					onlinePlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(stringBuilder.toString()));

					if (Math.floor(gameStartTime) == gameStartTime) {
						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
					}
				}
			}
		}.runTaskTimer(plugin, 0L, 2L).getTaskId();
	}

	public void showCaseTimer() {
		List<String> templates = new ArrayList<>();
		for (String templateName : plugin.getConfigManager().getConfig("templates.yml").getConfigurationSection("templates").getKeys(false)) {
			if (!plugin.getBungeeCord().usedTemplates.contains(templateName)) {
				templates.add(templateName);
			}
		}
		if (templates.size() == 0) {
			for (String usedTemplate : plugin.getBungeeCord().usedTemplates) {
				templates.add(usedTemplate);
			}
		}
		if (templates.size() > 1) {
			templates.remove(plugin.getBungeeCord().currentBuildRawName);
		}
		try {
			plugin.getBungeeCord().currentBuildRawName = templates.get(new Random().nextInt(templates.size()));
			plugin.getBungeeCord().currentBuildDisplayName = plugin.getConfigManager().getConfig("templates.yml").getString("templates." + plugin.getBungeeCord().currentBuildRawName + ".display-name");
			plugin.getBungeeCord().currentBuildBlocks.clear();

			if (!plugin.getBungeeCord().usedTemplates.contains(plugin.getBungeeCord().currentBuildRawName)) {
				plugin.getBungeeCord().usedTemplates.add(plugin.getBungeeCord().currentBuildRawName);
			} else {
				plugin.getBungeeCord().usedTemplates.remove(plugin.getBungeeCord().currentBuildRawName);
			}
		} catch (IllegalArgumentException ex) {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Templates can not be found!"));
			stop();
			return;
		}
		plugin.getBungeeCord().currentRound++;

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
			if (score >= 0) {
				if (!plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
					plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + onlinePlayer.getName()), plugin.getBungeeCord().gameScoreboard)).setScore(score);
					score--;
				}
			}
		}

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

				onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-YOU_WILL_RECREATE_BUILD")));
			}

			onlinePlayer.setScoreboard(plugin.getBungeeCord().gameScoreboard);
			onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&6" + plugin.getBungeeCord().currentBuildDisplayName), 0, 5*20, 0);
		}

		plugin.getBungeeCord().getGuardianManager().rotateGuardian(0F);

		plugin.getBungeeCord().getTemplateManager().unloadTemplate("guardian");
		for (Entry<String, String> plot : plugin.getBungeeCord().plots.entrySet()) {
			plugin.getBungeeCord().getTemplateManager().loadTemplate(plot.getValue());
		}

		plugin.getBungeeCord().state = ArenaState.DISPLAYING;

		Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.DISPLAYING, plugin.getBungeeCord().plots.keySet().stream().map(p -> Bukkit.getPlayer(p)).toList()));

		showCaseTime = plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.showcase-time");
		showCaseTimerID = new BukkitRunnable() {
			public void run() {
				showCaseTime -= 1;
				if (showCaseTime == 6) {
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + showCaseTime), 0, 2*20, 0);
						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
					}
				} else if (showCaseTime == 5) {
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + showCaseTime), 0, 2*20, 0);
						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
					}
				} else if (showCaseTime == 4) {
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + showCaseTime), 0, 2*20, 0);
						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.6f);
					}
				} else if (showCaseTime == 3) {
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + showCaseTime), 0, 2*20, 0);
						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.7f);
					}
				} else if (showCaseTime == 2) {
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + showCaseTime), 0, 2*20, 0);
						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.8f);
					}
				} else if (showCaseTime == 1) {
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + showCaseTime), 0, 2*20, 0);
						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.9f);
					}
				} else if (showCaseTime == 0) {
					this.cancel();
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						if (plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
							onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-RECREATE_BUILD")));
						}

						onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', translate("TITLE-VIEW_TIME_OVER")), 0, 2*20, 10);
						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
					}

					buildTimer();
				}
			}
		}.runTaskTimer(plugin, 0L, 20L).getTaskId();
	}

	public void buildTimer() {
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
				onlinePlayer.updateInventory();

				onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_WOOD_BREAK, 1f, 1f);

				plugin.getBungeeCord().playerPercent.put(onlinePlayer.getName(), 0);
			}
		}

		for (Entry<String, String> plot : plugin.getBungeeCord().plots.entrySet()) {
			plugin.getBungeeCord().getTemplateManager().unloadTemplate(plot.getValue());
		}

		plugin.getBungeeCord().state = ArenaState.BUILDING;

		Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.BUILDING, plugin.getBungeeCord().plots.keySet().stream().map(p -> Bukkit.getPlayer(p)).toList()));

		buildTime = (float) plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.build-time");
		if (plugin.getBungeeCord().currentRound > 2) {
			plugin.getBungeeCord().buildTimeSubtractor++;
			buildTime = buildTime - plugin.getBungeeCord().buildTimeSubtractor;
		}
		buildTimerID = new BukkitRunnable() {
			public void run() {
				buildTime = buildTime - 0.1F;
				String buildTimeFormat = String.format(Locale.UK, "%.1f", buildTime);
				buildTime = Float.parseFloat(buildTimeFormat);

				if (buildTime <= 0) {
					this.cancel();
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						if (plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
							onlinePlayer.getInventory().setArmorContents(null);
							onlinePlayer.getInventory().clear();
							onlinePlayer.setExp(0);
							onlinePlayer.setFireTicks(0);
							onlinePlayer.setFoodLevel(20);
							onlinePlayer.setGameMode(GameMode.SPECTATOR);
							onlinePlayer.setHealth(20);
							onlinePlayer.setLevel(0);
							onlinePlayer.setAllowFlight(true);
							onlinePlayer.setFlying(true);
							for (PotionEffect potionEffect : onlinePlayer.getActivePotionEffects()) {
								onlinePlayer.removePotionEffect(potionEffect.getType());
							}
						}

						onlinePlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', translate("ABAR-TIME_LEFT")) + " &c▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌ &f0.0 " + translate("MAIN-SECONDS")));
						onlinePlayer.spawnParticle(Particle.ELDER_GUARDIAN, onlinePlayer.getLocation(), 1, 0, 0, 0, 1);
						onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', translate("TITLE-TIME_IS_UP")), 10, 15, 10);

						onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.85f, 1f);
					}
					judgeTimer();
					return;
				}

				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(ChatColor.translateAlternateColorCodes('&', translate("ABAR-TIME_LEFT")) + " ");
				int fullDisplay = 24;
				double timeIncompleted = fullDisplay * (buildTime / (float) (plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.build-time") - plugin.getBungeeCord().buildTimeSubtractor));
				double timeCompleted = fullDisplay - timeIncompleted;
				for (int i = 0; i < timeIncompleted; i++) {
					stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&a▌"));
				}
				for (int i = 0; i < timeCompleted; i++) {
					stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&c▌"));
				}
				stringBuilder.append(ChatColor.translateAlternateColorCodes('&', " &f" + buildTime + " " + translate("MAIN-SECONDS")));

				for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
					onlinePlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(stringBuilder.toString()));
				}

				if (Math.floor(buildTime) == buildTime) {
					if (buildTime == 5) {
						for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
							onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + String.format(Locale.UK, "%.0f", buildTime)), 0, 2*20, 0);
							onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
						}
					} else if (buildTime == 4) {
						for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
							onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + String.format(Locale.UK, "%.0f", buildTime)), 0, 2*20, 0);
							onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.6f);
						}
					} else if (buildTime == 3) {
						for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
							onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + String.format(Locale.UK, "%.0f", buildTime)), 0, 2*20, 0);
							onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.7f);
						}
					} else if (buildTime == 2) {
						for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
							onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + String.format(Locale.UK, "%.0f", buildTime)), 0, 2*20, 0);
							onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.8f);
						}
					} else if (buildTime == 1) {
						for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
							onlinePlayer.sendTitle(" ", ChatColor.translateAlternateColorCodes('&', "&a" + String.format(Locale.UK, "%.0f", buildTime)), 0, 2*20, 0);
							onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.9f);
						}
					}
				}
			}
		}.runTaskTimer(plugin, 0L, 2L).getTaskId();
	}

	public void judgeTimer() {
		String tempJudgedPlayerName = null;
		int tempJudgedPlayerPercent = 101;
		for (Entry<String, Integer> entrySet : plugin.getBungeeCord().playerPercent.entrySet()) {
			if (entrySet.getValue() < tempJudgedPlayerPercent) {
				tempJudgedPlayerName = entrySet.getKey();
				tempJudgedPlayerPercent = entrySet.getValue();
			}
		}
		if (tempJudgedPlayerPercent == 100) {
			plugin.getBungeeCord().getTimerManager().guardianIsImpressed();
			return;
		}
		plugin.getBungeeCord().judgedPlayerName = tempJudgedPlayerName;
		if (Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName) != null) {
			Player player = Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName);

			for (String loserCommand : plugin.getBungeeCord().loserCommands) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loserCommand.replaceFirst("/", "").replaceAll("%PLAYER%", player.getName()));
			}

			plugin.getStatsManager().incrementStat(StatsType.LOSSES, player, 1);

			Bukkit.getPluginManager().callEvent(new PlayerLoseEvent(player));
		}

		plugin.getBungeeCord().getTemplateManager().loadTemplate("guardian");

		plugin.getBungeeCord().state = ArenaState.JUDGING;

		Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.JUDGING, plugin.getBungeeCord().plots.keySet().stream().map(p -> Bukkit.getPlayer(p)).toList()));

		judgeTime = (float) plugin.getConfigManager().getConfig("config.yml").getInt("bungeecord.judge-time");
		judgeTimerID1 = new BukkitRunnable() {
			public void run() {
				judgeTime = judgeTime - 0.05F;
				String judgeTimeFormat = String.format(Locale.UK, "%.2f", judgeTime);
				judgeTime = Float.parseFloat(judgeTimeFormat);

				if (judgeTime <= 0) {
					Bukkit.getScheduler().cancelTask(judgeTimerID1);
					Bukkit.getScheduler().cancelTask(judgeTimerID2);
					Bukkit.getScheduler().cancelTask(judgeTimerID3);
					Bukkit.getScheduler().cancelTask(judgeTimerID4);
					Bukkit.getScheduler().cancelTask(judgeTimerID5);
					Bukkit.getScheduler().cancelTask(judgeTimerID6);

					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-PLAYER_WAS_ELIMINATED").replaceAll("%PLAYER%", plugin.getBungeeCord().judgedPlayerName)), 0, 20, 10);
					}

					plugin.getBungeeCord().getGuardianManager().laserGuardian(true);

					judgeTimerID2 = new BukkitRunnable() {
						public void run() {
							for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
								onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1f);

								onlinePlayer.spawnParticle(Particle.EXPLOSION_EMITTER, plugin.getBungeeCord().judgedPlayerArmorStand.getLocation(), 1, 0, 0, 0, 1);
							}

							plugin.getBungeeCord().judgedPlayerArmorStand.remove();
							plugin.getBungeeCord().getGuardianManager().laserGuardian(false);

							if (plugin.getConfigManager().getConfig("maps.yml").contains("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(plugin.getBungeeCord().judgedPlayerName) + ".area")) {
								plugin.getBungeeCord().getTemplateManager().explodePlot(plugin.getBungeeCord().plots.get(plugin.getBungeeCord().judgedPlayerName));
							}

							if (Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName) != null) {
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).getInventory().setArmorContents(null);
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).getInventory().clear();
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).setExp(0);
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).setFireTicks(0);
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).setFoodLevel(20);
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).setGameMode(GameMode.SURVIVAL);
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).setHealth(20);
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).setLevel(0);
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).setAllowFlight(true);
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).setFlying(true);
								for (PotionEffect potionEffect : Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).getActivePotionEffects()) {
									Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).removePotionEffect(potionEffect.getType());
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

									Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("stats-item-slot") - 1, itemStack2);
								}
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("lobby-item-slot") - 1, itemStack1);
								Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName).updateInventory();

								plugin.getBungeeCord().gameScoreboard.getTeam("Players").removePlayer(Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName));
								plugin.getBungeeCord().gameScoreboard.getTeam("Guardians").addPlayer(Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName));

								// plugin.getBungeeCord().getNMSManager().disguise(Bukkit.getPlayer(plugin.getBungeeCord().judgedPlayerName), true);
							}

							plugin.getBungeeCord().playerPercent.remove(plugin.getBungeeCord().judgedPlayerName);
							plugin.getBungeeCord().plots.remove(plugin.getBungeeCord().judgedPlayerName);

							if (plugin.getBungeeCord().plots.size() == 1) {
								plugin.getBungeeCord().secondPlace = plugin.getBungeeCord().judgedPlayerName;
								plugin.getBungeeCord().firstPlace = (String) plugin.getBungeeCord().plots.keySet().toArray()[0];

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
									if (score >= 0) {
										if (!plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
											plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + onlinePlayer.getName()), plugin.getBungeeCord().gameScoreboard)).setScore(score);
											score--;
										}
									}
								}

								for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
									for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("BUNGEECORD.MAIN-GAME_END_DESCRIPTION")) {
										onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", plugin.getBungeeCord().firstPlace).replaceAll("%PLAYER2%", plugin.getBungeeCord().secondPlace).replaceAll("%PLAYER3%", plugin.getBungeeCord().thirdPlace)));
									}

									onlinePlayer.setScoreboard(plugin.getBungeeCord().gameScoreboard);
									onlinePlayer.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + plugin.getBungeeCord().firstPlace), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 10, 130, 20);

									onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
									if (onlinePlayer.getName().equals(plugin.getBungeeCord().firstPlace)) {
										judgeTimerID3 = new BukkitRunnable() {
											public void run() {
												for (String winnerCommand : plugin.getBungeeCord().winnerCommands) {
													Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", onlinePlayer.getName()));
												}

												plugin.getStatsManager().incrementStat(StatsType.WINS, onlinePlayer, 1);

												Bukkit.getPluginManager().callEvent(new PlayerWinEvent(onlinePlayer));
											}
										}.runTaskLater(plugin, 5L).getTaskId();
									}
								}
								judgeTimerID4 = new BukkitRunnable() {
									public void run() {
										stop();
									}
								}.runTaskLater(plugin, 10*20L).getTaskId();
							} else {
								if (plugin.getBungeeCord().plots.size() == 2) {
									plugin.getBungeeCord().thirdPlace = plugin.getBungeeCord().judgedPlayerName;
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
									if (score >= 0) {
										if (!plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
											plugin.getBungeeCord().gameScoreboard.getObjective("SpeedBuilders").getScore(plugin.getBungeeCord().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + onlinePlayer.getName()), plugin.getBungeeCord().gameScoreboard)).setScore(score);
											score--;
										}
									}
								}
								for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
									onlinePlayer.setScoreboard(plugin.getBungeeCord().gameScoreboard);
								}

								judgeTimerID5 = new BukkitRunnable() {
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
										showCaseTimer();
									}
								}.runTaskLater(plugin, 3*20L).getTaskId();
							}
							plugin.getBungeeCord().judgedPlayerName = null;
						}
					}.runTaskLater(plugin, 5*20L).getTaskId();
				} else {
					if (Math.floor(judgeTime) == judgeTime) {
						if (judgeTime == 4) {
							for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
								if (plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
									if (plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()) <= 100 && plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()) >= 75) {
										onlinePlayer.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_SCORED_PERCENT").replaceAll("%PERCENT_COLOR%", "&b").replaceAll("%PERCENT%", "" + plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()))), 0, 2*15, 10);
									}
									if (plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()) <= 74 && plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()) >= 50) {
										onlinePlayer.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_SCORED_PERCENT").replaceAll("%PERCENT_COLOR%", "&a").replaceAll("%PERCENT%", "" + plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()))), 0, 2*15, 10);
									}
									if (plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()) <= 49 && plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()) >= 25) {
										onlinePlayer.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_SCORED_PERCENT").replaceAll("%PERCENT_COLOR%", "&e").replaceAll("%PERCENT%", "" + plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()))), 0, 2*15, 10);
									}
									if (plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()) <= 24 && plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()) >= 0) {
										onlinePlayer.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_SCORED_PERCENT").replaceAll("%PERCENT_COLOR%", "&c").replaceAll("%PERCENT%", "" + plugin.getBungeeCord().playerPercent.get(onlinePlayer.getName()))), 0, 2*15, 10);
									}
								}
							}
						}
					}

					plugin.getBungeeCord().getGuardianManager().rotateGuardian(7.5F);
				}
			}
		}.runTaskTimer(plugin, 0L, 1L).getTaskId();
		judgeTimerID6 = new BukkitRunnable() {
			public void run() {
				for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
					onlinePlayer.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-GWEN_IS_JUDGING")), 0, 2*20, 10);
				}
			}
		}.runTaskLater(plugin, 2*20L).getTaskId();
	}

	public void guardianIsImpressed() {
		Bukkit.getScheduler().cancelTask(gameStartTimerID);
		Bukkit.getScheduler().cancelTask(showCaseTimerID);
		Bukkit.getScheduler().cancelTask(buildTimerID);
		Bukkit.getScheduler().cancelTask(judgeTimerID1);
		Bukkit.getScheduler().cancelTask(judgeTimerID2);
		Bukkit.getScheduler().cancelTask(judgeTimerID3);
		Bukkit.getScheduler().cancelTask(judgeTimerID4);
		Bukkit.getScheduler().cancelTask(judgeTimerID5);
		Bukkit.getScheduler().cancelTask(judgeTimerID6);

		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if (plugin.getBungeeCord().plots.containsKey(onlinePlayer.getName())) {
				onlinePlayer.getInventory().setArmorContents(null);
				onlinePlayer.getInventory().clear();
				onlinePlayer.setExp(0);
				onlinePlayer.setFireTicks(0);
				onlinePlayer.setFoodLevel(20);
				onlinePlayer.setGameMode(GameMode.SPECTATOR);
				onlinePlayer.setHealth(20);
				onlinePlayer.setLevel(0);
				onlinePlayer.setAllowFlight(true);
				onlinePlayer.setFlying(true);
				for (PotionEffect potionEffect : onlinePlayer.getActivePotionEffects()) {
					onlinePlayer.removePotionEffect(potionEffect.getType());
				}
			}

			onlinePlayer.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-GWEN_IS_IMPRESSED")), 0, 5*20, 0);
			onlinePlayer.spawnParticle(Particle.ELDER_GUARDIAN, onlinePlayer.getLocation(), 1, 0, 0, 0, 1);

			onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.85f, 1f);
		}

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

				showCaseTimer();
			}
		}.runTaskLater(plugin, 3*20L);
	}

	public void cooldownTimer(final String player) {
		new BukkitRunnable() {
			public void run() {
				if (Bukkit.getPlayer(player).isOnline()) {
					float cooldownTime = plugin.getBungeeCord().playersDoubleJumpCooldowned.get(player);
					cooldownTime = cooldownTime - 0.1F;
					String cooldownTimeFormat = String.format(Locale.UK, "%.1f", cooldownTime);
					cooldownTime = Float.parseFloat(cooldownTimeFormat);
					plugin.getBungeeCord().playersDoubleJumpCooldowned.put(player, cooldownTime);

					if (cooldownTime <= 0) {
						this.cancel();
						plugin.getBungeeCord().playersDoubleJumpCooldowned.remove(player);
					}
				} else {
					this.cancel();
					plugin.getBungeeCord().playersDoubleJumpCooldowned.remove(player);
				}
			}
		}.runTaskTimer(plugin, 0L, 2L);
	}

	public void stop() {
		Bukkit.getScheduler().cancelTask(startTimerID);
		Bukkit.getScheduler().cancelTask(gameStartTimerID);
		Bukkit.getScheduler().cancelTask(showCaseTimerID);
		Bukkit.getScheduler().cancelTask(buildTimerID);
		Bukkit.getScheduler().cancelTask(judgeTimerID1);
		Bukkit.getScheduler().cancelTask(judgeTimerID2);
		Bukkit.getScheduler().cancelTask(judgeTimerID3);
		Bukkit.getScheduler().cancelTask(judgeTimerID4);
		Bukkit.getScheduler().cancelTask(judgeTimerID5);
		Bukkit.getScheduler().cancelTask(judgeTimerID6);

		if (plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.game-ending-mode").equalsIgnoreCase("reload-1")) {
			new BukkitRunnable() {
				public void run() {
					plugin.getBungeeCord().reload();
				}
			}.runTaskLater(plugin, 10L);
			return;
		} else if (plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.game-ending-mode").equalsIgnoreCase("reload-2")) {
			new BukkitRunnable() {
				public void run() {
					ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
					byteArrayDataOutput.writeUTF("Connect");
					byteArrayDataOutput.writeUTF(plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.lobby-server-name"));
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendPluginMessage(plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
					}
					new BukkitRunnable() {
						public void run() {
							plugin.getBungeeCord().reload();
						}
					}.runTaskLater(plugin, 2*20L);
				}
			}.runTaskLater(plugin, 10L);
			return;
		} else if (plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.game-ending-mode").equalsIgnoreCase("restart")) {
			new BukkitRunnable() {
				public void run() {
					ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
					byteArrayDataOutput.writeUTF("Connect");
					byteArrayDataOutput.writeUTF(plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.lobby-server-name"));
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendPluginMessage(plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
					}
					new BukkitRunnable() {
						public void run() {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
						}
					}.runTaskLater(plugin, 2*20L);
				}
			}.runTaskLater(plugin, 10L);
			return;
		} else if (plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.game-ending-mode").equalsIgnoreCase("stop")) {
			new BukkitRunnable() {
				public void run() {
					ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
					byteArrayDataOutput.writeUTF("Connect");
					byteArrayDataOutput.writeUTF(plugin.getConfigManager().getConfig("config.yml").getString("bungeecord.lobby-server-name"));
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendPluginMessage(plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
					}
					new BukkitRunnable() {
						public void run() {
							Bukkit.shutdown();
						}
					}.runTaskLater(plugin, 2*20L);
				}
			}.runTaskLater(plugin, 10L);
			return;
		}
	}

	public Location getCenter2(Location location1, Location location2) {
		double x1 = Math.min(location1.getX(), location2.getX());
		double y1 = Math.min(location1.getY(), location2.getY());
		double z1 = Math.min(location1.getZ(), location2.getZ());
		double x2 = Math.max(location1.getX(), location2.getX());
		double z2 = Math.max(location1.getZ(), location2.getZ());

		return new Location(location1.getWorld(), x1 + (x2 - x1) / 2.0D, y1, z1 + (z2 - z1) / 2.0D);
	}

	public boolean playerIsInsideAsSpectator(Location location1, Location location2, Location location3) {
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

	public String timeString(int time) {
		String timeString = null;
		if (time >= 60.0) {
			double minutes = time / 60.0;
			timeString = (Math.floor(minutes * 10) / 10) + " " + translate("MAIN-MINUTES");
		} else {
			timeString = time + " " + translate("MAIN-SECONDS");
		}

		return timeString;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getStartTimerID() {
		return startTimerID;
	}

	public float getGameStartTime() {
		return gameStartTime;
	}

	public int getGameStartTimerID() {
		return gameStartTimerID;
	}

	public int getShowCaseTime() {
		return showCaseTime;
	}

	public int getShowCaseTimerID() {
		return showCaseTimerID;
	}

	public float getBuildTime() {
		return buildTime;
	}

	public int getBuildTimerID() {
		return buildTimerID;
	}

	public float getJudgeTime() {
		return judgeTime;
	}

	public int getJudgeTimerID1() {
		return judgeTimerID1;
	}

	public int getJudgeTimerID2() {
		return judgeTimerID2;
	}

	public int getJudgeTimerID3() {
		return judgeTimerID3;
	}

	public int getJudgeTimerID4() {
		return judgeTimerID4;
	}

	public int getJudgeTimerID5() {
		return judgeTimerID5;
	}

	public int getJudgeTimerID6() {
		return judgeTimerID6;
	}
}
