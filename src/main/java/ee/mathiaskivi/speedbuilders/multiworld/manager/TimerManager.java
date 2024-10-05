package ee.mathiaskivi.speedbuilders.multiworld.manager;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.api.event.ArenaStateChangeEvent;
import ee.mathiaskivi.speedbuilders.api.event.PlayerLoseEvent;
import ee.mathiaskivi.speedbuilders.api.event.PlayerWinEvent;
import ee.mathiaskivi.speedbuilders.api.game.ArenaState;
import ee.mathiaskivi.speedbuilders.multiworld.Arena;
import ee.mathiaskivi.speedbuilders.utility.StatsType;
import org.bukkit.*;
import org.bukkit.block.BlockState;
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
import java.util.concurrent.atomic.AtomicInteger;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

@SuppressWarnings("deprecation")
public class TimerManager {
	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public void startTimer(String arenaName) {
		final Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			arena.setState(ArenaState.STARTING);

			Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.STARTING, arena.getPlayers()));

			arena.setStartTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".start-time"));
			arena.setStartTimerID(new BukkitRunnable() {
				public void run() {
					arena.setStartTime(arena.getStartTime() - 1);
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
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", timeString(arena.getStartTime()))));
							}
							objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
							objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
							objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
							objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
							objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
							objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(p, arena.getName()).toUpperCase())), scoreboard)).setScore(1);
							p.setScoreboard(scoreboard);
						} else {
							ScoreboardManager SBManager = Bukkit.getScoreboardManager();
							Scoreboard scoreboard = SBManager.getNewScoreboard();
							Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
							if (arena.getState() == ArenaState.WAITING) {
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
							}
							if (arena.getState() == ArenaState.STARTING) {
								objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", timeString(arena.getStartTime()))));
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
					if (arena.getStartTime() == 0) {
						this.cancel();
						arena.setStartTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".start-time"));
						gameStartTimer(arena.getName());
						return;
					}
				}
			}.runTaskTimer(plugin, 0L, 20L).getTaskId());
			plugin.getMultiWorld().getSignManager().updateSigns(arena.getName());
		}
	}

	public void gameStartTimer(String arenaName) {
		final Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			for (org.bukkit.entity.Entity entity : Bukkit.getWorld(arena.getName()).getEntities()) {
				if (entity.getType() != EntityType.PLAYER) {
					entity.remove();
				}
			}

			plugin.getMultiWorld().getGuardianManager().spawnGuardian(arena.getName());
			plugin.getMultiWorld().getTemplateManager().resetPlots(arena.getName());

			List<String> plots = new ArrayList<>();
			for (String plot : plugin.getConfigManager().getConfig("arenas.yml").getConfigurationSection("arenas." + arena.getName() + ".plots").getKeys(false)) {
				if (!plot.equals("guardian")) {
					plots.add(plot);
				}
			}

			arena.getPlayers().forEach(p -> {
				if (arena.getGameScoreboard().getPlayerTeam(p) != null) {
					if (arena.getGameScoreboard().getPlayerTeam(p).getName().equals("Players")) {
						String plot = plots.get(0);
						arena.getPlots().put(p.getName(), plot);
						plots.remove(plot);

						Location location = new Location(Bukkit.getWorld(arena.getName()), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.x"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.y"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.z"));
						location.setPitch((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.pitch"));
						location.setYaw((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.yaw"));
						p.teleport(location);
						p.setFallDistance(0F);
					}
				}

				for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("MULTIWORLD.MAIN-GAME_START_DESCRIPTION")) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string));
				}

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

				plugin.getMultiWorld().getKitManager().giveKitItems(p, arena.getName());
				p.updateInventory();

				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
			});

			for (String plot : plots) {
				if (plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".plots." + plot + ".blocks")) {
					for (String block : plugin.getConfigManager().getConfig("arenas.yml").getConfigurationSection("arenas." + arena.getName() + ".plots." + plot + ".blocks").getKeys(false)) {
						String[] blockCoordinates = block.split(",");

						BlockState blockState = Bukkit.getWorld(arena.getName()).getBlockAt(Integer.parseInt(blockCoordinates[0]), Integer.parseInt(blockCoordinates[1]), Integer.parseInt(blockCoordinates[2])).getState();
						blockState.setType(Material.AIR);
						blockState.update(true, false);
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
				if (score.get() >= 0) {
					if (!arena.getPlots().containsKey(p.getName())) {
						arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + p.getName()), arena.getGameScoreboard())).setScore(score.get());
						score.getAndDecrement();
					}
				}
			});
			arena.getPlayers().forEach(p -> {
				p.setScoreboard(arena.getGameScoreboard());
			});

			arena.setState(ArenaState.BEGINNING);

			Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.BEGINNING, arena.getPlots().keySet().stream().map(p -> Bukkit.getPlayer(p)).toList()));

			arena.setGameStartTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".game-start-time"));
			arena.setGameStartTimerID(new BukkitRunnable() {
				public void run() {
					arena.setGameStartTime(arena.getGameStartTime() - 0.1F);
					String gameStartTimeFormat = String.format(Locale.UK, "%.1f", arena.getGameStartTime());
					arena.setGameStartTime(Float.parseFloat(gameStartTimeFormat));

					if (arena.getGameStartTime() <= 0) {
						this.cancel();
						arena.getPlayers().forEach(p -> {
							plugin.getMultiWorld().getNMSManager().showActionBar(p, ChatColor.translateAlternateColorCodes('&', translate("ABAR-GAME_START")) + " &a▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌ &f0.0 " + translate("MAIN-SECONDS"));
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
						});
						showCaseTimer(arena.getName());
						return;
					}

					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(ChatColor.translateAlternateColorCodes('&', translate("ABAR-GAME_START")) + " ");
					int fullDisplay = 24;
					double timeIncompleted = fullDisplay * (arena.getGameStartTime() / plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".game-start-time"));
					double timeCompleted = fullDisplay - timeIncompleted;
					for (int i = 0; i < timeCompleted; i++) {
						stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&a▌"));
					}
					for (int i = 0; i < timeIncompleted; i++) {
						stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&c▌"));
					}
					stringBuilder.append(ChatColor.translateAlternateColorCodes('&', " &f" + arena.getGameStartTime() + " " + translate("MAIN-SECONDS")));

					arena.getPlayers().forEach(p -> {
						plugin.getMultiWorld().getNMSManager().showActionBar(p, stringBuilder.toString());
						if (Math.floor(arena.getGameStartTime()) == arena.getGameStartTime()) {
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
						}
					});
				}
			}.runTaskTimer(plugin, 0L, 2L).getTaskId());
			plugin.getMultiWorld().getSignManager().updateSigns(arena.getName());
		}
	}

	public void showCaseTimer(String arenaName) {
		final Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			List<String> templates = new ArrayList<>();
			for (String templateName : plugin.getConfigManager().getConfig("templates.yml").getConfigurationSection("templates").getKeys(false)) {
				if (!arena.getUsedTemplates().contains(templateName)) {
					templates.add(templateName);
				}
			}
			if (templates.size() == 0) {
				for (String usedTemplate : arena.getUsedTemplates()) {
					templates.add(usedTemplate);
				}
			}
			if (templates.size() > 1) {
				templates.remove(arena.getCurrentBuildRawName());
			}
			try {
				arena.setCurrentBuildRawName(templates.get(new Random().nextInt(templates.size())));
				arena.setCurrentBuildDisplayName(plugin.getConfigManager().getConfig("templates.yml").getString("templates." + arena.getCurrentBuildRawName() + ".display-name"));
				arena.getCurrentBuildBlocks().clear();

				if (!arena.getUsedTemplates().contains(arena.getCurrentBuildRawName())) {
					arena.getUsedTemplates().add(arena.getCurrentBuildRawName());
				} else {
					arena.getUsedTemplates().remove(arena.getCurrentBuildRawName());
				}
			} catch (IllegalArgumentException ex) {
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Templates can not be found!"));
				plugin.getMultiWorld().getArenaManager().endArena(arena.getName());
				return;
			}
			arena.setCurrentRound(arena.getCurrentRound() + 1);

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
				if (score.get() >= 0) {
					if (!arena.getPlots().containsKey(p.getName())) {
						arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + p.getName()), arena.getGameScoreboard())).setScore(score.get());
						score.getAndDecrement();
					}
				}
			});

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

					p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-YOU_WILL_RECREATE_BUILD")));
				}

				p.setScoreboard(arena.getGameScoreboard());
				p.sendTitle("", "&6" + arena.getCurrentBuildDisplayName(), 0, 5*20, 0);
			});

			plugin.getMultiWorld().getGuardianManager().rotateGuardian(0F, arena.getName());

			plugin.getMultiWorld().getTemplateManager().unloadTemplate("guardian", arena.getName());
			for (Entry<String, String> plot : arena.getPlots().entrySet()) {
				plugin.getMultiWorld().getTemplateManager().loadTemplate(plot.getValue(), arena.getName());
			}

			arena.setState(ArenaState.DISPLAYING);

			Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.DISPLAYING, arena.getPlots().keySet().stream().map(p -> Bukkit.getPlayer(p)).toList()));

			arena.setShowCaseTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".showcase-time"));
			arena.setShowCaseTimerID(new BukkitRunnable() {
				public void run() {
					arena.setShowCaseTime(arena.getShowCaseTime() - 1);
					if (arena.getShowCaseTime() == 6) {
						arena.getPlayers().forEach(p -> {
							p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + arena.getShowCaseTime()), 0, 2*20, 0);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
						});
					} else if (arena.getShowCaseTime() == 5) {
						arena.getPlayers().forEach(p -> {
							p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + arena.getShowCaseTime()), 0, 2*20, 0);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
						});
					} else if (arena.getShowCaseTime() == 4) {
						arena.getPlayers().forEach(p -> {
							p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + arena.getShowCaseTime()), 0, 2*20, 0);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.6f);
						});
					} else if (arena.getShowCaseTime() == 3) {
						arena.getPlayers().forEach(p -> {
							p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + arena.getShowCaseTime()), 0, 2*20, 0);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.7f);
						});
					} else if (arena.getShowCaseTime() == 2) {
						arena.getPlayers().forEach(p -> {
							p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + arena.getShowCaseTime()), 0, 2*20, 0);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.8f);
						});
					} else if (arena.getShowCaseTime() == 1) {
						arena.getPlayers().forEach(p -> {
							p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + arena.getShowCaseTime()), 0, 2*20, 0);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.9f);
						});
					} else if (arena.getShowCaseTime() == 0) {
						this.cancel();
						arena.getPlayers().forEach(p -> {
							if (arena.getPlots().containsKey(p.getName())) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-RECREATE_BUILD")));
							}

							p.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-VIEW_TIME_OVER")), 0, 2*20, 10);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
						});
						buildTimer(arena.getName());
					}
				}
			}.runTaskTimer(plugin, 0L, 20L).getTaskId());
			plugin.getMultiWorld().getSignManager().updateSigns(arena.getName());
		}
	}

	public void buildTimer(String arenaName) {
		final Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
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
					for (PotionEffect effect : p.getActivePotionEffects()) {
						p.removePotionEffect(effect.getType());
					}
					p.updateInventory();

					p.playSound(p.getLocation(), Sound.BLOCK_WOOD_BREAK, 1f, 1f);

					arena.getPlayerPercent().put(p.getName(), 0);
				}
			});

			for (Entry<String, String> plot : arena.getPlots().entrySet()) {
				plugin.getMultiWorld().getTemplateManager().unloadTemplate(plot.getValue(), arena.getName());
			}

			arena.setState(ArenaState.BUILDING);

			Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.BUILDING, arena.getPlots().keySet().stream().map(p -> Bukkit.getPlayer(p)).toList()));

			arena.setBuildTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".build-time"));
			if (arena.getCurrentRound() > 2) {
				arena.setBuildTimeSubtractor(arena.getBuildTimeSubtractor() + 1);
				arena.setBuildTime(arena.getBuildTime() - arena.getBuildTimeSubtractor());
			}
			arena.setBuildTimerID(new BukkitRunnable() {
				public void run() {
					arena.setBuildTime(arena.getBuildTime() - 0.1F);
					String buildTimeFormat = String.format(Locale.UK, "%.1f", arena.getBuildTime());
					arena.setBuildTime(Float.parseFloat(buildTimeFormat));

					if (arena.getBuildTime() <= 0) {
						this.cancel();
						arena.getPlayers().forEach(p -> {
							if (arena.getPlots().containsKey(p.getName())) {
								p.getInventory().setArmorContents(null);
								p.getInventory().clear();
								p.setExp(0);
								p.setFireTicks(0);
								p.setFoodLevel(20);
								p.setGameMode(GameMode.SPECTATOR);
								p.setHealth(20);
								p.setLevel(0);
								p.setAllowFlight(true);
								p.setFlying(true);
								for (PotionEffect effect : p.getActivePotionEffects()) {
									p.removePotionEffect(effect.getType());
								}
							}

							plugin.getMultiWorld().getNMSManager().showActionBar(p, ChatColor.translateAlternateColorCodes('&', translate("ABAR-TIME_LEFT")) + " &c▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌ &f0.0 " + translate("MAIN-SECONDS"));
							p.spawnParticle(Particle.ELDER_GUARDIAN, p.getLocation(), 1, 0F, 0F, 0F, 1F);
							p.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-TIME_IS_UP")), 10, 15, 10);

							p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.85f, 1f);
						});
						judgeTimer(arena.getName());
						return;
					}

					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(ChatColor.translateAlternateColorCodes('&', translate("ABAR-TIME_LEFT")) + " ");
					int fullDisplay = 24;
					double timeIncompleted = fullDisplay * (arena.getBuildTime() / (plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".build-time") - arena.getBuildTimeSubtractor()));
					double timeCompleted = fullDisplay - timeIncompleted;
					for (int i = 0; i < timeIncompleted; i++) {
						stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&a▌"));
					}
					for (int i = 0; i < timeCompleted; i++) {
						stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&c▌"));
					}
					stringBuilder.append(ChatColor.translateAlternateColorCodes('&', " &f" + arena.getBuildTime() + " " + translate("MAIN-SECONDS")));

					arena.getPlayers().forEach(p -> {
						plugin.getMultiWorld().getNMSManager().showActionBar(p, stringBuilder.toString());
					});

					if (Math.floor(arena.getBuildTime()) == arena.getBuildTime()) {
						if (arena.getBuildTime() == 5) {
							arena.getPlayers().forEach(p -> {
								p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + String.format(Locale.UK, "%.0f", arena.getBuildTime())), 0, 2*20, 0);
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
							});
						} else if (arena.getBuildTime() == 4) {
							arena.getPlayers().forEach(p -> {
								p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + String.format(Locale.UK, "%.0f", arena.getBuildTime())), 0, 2*20, 0);
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.6f);
							});
						} else if (arena.getBuildTime() == 3) {
							arena.getPlayers().forEach(p -> {
								p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + String.format(Locale.UK, "%.0f", arena.getBuildTime())), 0, 2*20, 0);
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.7f);
							});
						} else if (arena.getBuildTime() == 2) {
							arena.getPlayers().forEach(p -> {
								p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + String.format(Locale.UK, "%.0f", arena.getBuildTime())), 0, 2*20, 0);
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.8f);
							});
						} else if (arena.getBuildTime() == 1) {
							arena.getPlayers().forEach(p -> {
								p.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&a" + String.format(Locale.UK, "%.0f", arena.getBuildTime())), 0, 2*20, 0);
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.9f);
							});
						}
					}
				}
			}.runTaskTimer(plugin, 0L, 2L).getTaskId());
			plugin.getMultiWorld().getSignManager().updateSigns(arena.getName());
		}
	}

	public void judgeTimer(String arenaName) {
		final Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			String tempJudgedPlayerName = null;
			int tempJudgedPlayerPercent = 101;
			for (Entry<String, Integer> entrySet : arena.getPlayerPercent().entrySet()) {
				if (entrySet.getValue() < tempJudgedPlayerPercent) {
					tempJudgedPlayerName = entrySet.getKey();
					tempJudgedPlayerPercent = entrySet.getValue();
				}
			}
			if (tempJudgedPlayerPercent == 100) {
				guardianIsImpressed(arena.getName());
				return;
			}
			arena.setJudgedPlayerName(tempJudgedPlayerName);
			if (Bukkit.getPlayer(arena.getJudgedPlayerName()) != null) {
				Player player = Bukkit.getPlayer(arena.getJudgedPlayerName());
				for (String loserCommand : plugin.getMultiWorld().loserCommands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loserCommand.replaceFirst("/", "").replaceAll("%PLAYER%", player.getName()));
				}

				plugin.getStatsManager().incrementStat(StatsType.LOSSES, player, 1);

				Bukkit.getPluginManager().callEvent(new PlayerLoseEvent(player));
			}

			plugin.getMultiWorld().getTemplateManager().loadTemplate("guardian", arena.getName());

			arena.setState(ArenaState.JUDGING);

			Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(ArenaState.JUDGING, arena.getPlots().keySet().stream().map(p -> Bukkit.getPlayer(p)).toList()));

			arena.setJudgeTime(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".judge-time"));
			arena.setJudgeTimerID1(new BukkitRunnable() {
				public void run() {
					arena.setJudgeTime(arena.getJudgeTime() - 0.05F);
					String judgeTimeFormat = String.format(Locale.UK, "%.2f", arena.getJudgeTime());
					arena.setJudgeTime(Float.parseFloat(judgeTimeFormat));

					if (arena.getJudgeTime() <= 0) {
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID1());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID2());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID3());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID4());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID5());
						Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID6());

						arena.getPlayers().forEach(p -> {
							p.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-PLAYER_WAS_ELIMINATED").replaceAll("%PLAYER%", arena.getJudgedPlayerName())), 0, 20, 10);
						});

						plugin.getMultiWorld().getGuardianManager().laserGuardian(true, arena.getName());

						arena.setJudgeTimerID2(new BukkitRunnable() {
							public void run() {
								arena.getPlayers().forEach(p -> {
									p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1f);

									p.spawnParticle(Particle.EXPLOSION_EMITTER, arena.getJudgedPlayerArmorStand().getLocation(), 1, 0F, 0F, 0F, 1F);
								});

								arena.getJudgedPlayerArmorStand().remove();
								plugin.getMultiWorld().getGuardianManager().laserGuardian(false, arena.getName());

								if (plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".plots." + arena.getPlots().get(arena.getJudgedPlayerName()) + ".area")) {
									plugin.getMultiWorld().getTemplateManager().explodePlot(arena.getPlots().get(arena.getJudgedPlayerName()), arena.getName());
								}

								if (Bukkit.getPlayer(arena.getJudgedPlayerName()) != null) {
									Bukkit.getPlayer(arena.getJudgedPlayerName()).getInventory().setArmorContents(null);
									Bukkit.getPlayer(arena.getJudgedPlayerName()).getInventory().clear();
									Bukkit.getPlayer(arena.getJudgedPlayerName()).setExp(0);
									Bukkit.getPlayer(arena.getJudgedPlayerName()).setFireTicks(0);
									Bukkit.getPlayer(arena.getJudgedPlayerName()).setFoodLevel(20);
									Bukkit.getPlayer(arena.getJudgedPlayerName()).setGameMode(GameMode.SURVIVAL);
									Bukkit.getPlayer(arena.getJudgedPlayerName()).setHealth(20);
									Bukkit.getPlayer(arena.getJudgedPlayerName()).setLevel(0);
									Bukkit.getPlayer(arena.getJudgedPlayerName()).setAllowFlight(true);
									Bukkit.getPlayer(arena.getJudgedPlayerName()).setFlying(true);
									for (PotionEffect potionEffect : Bukkit.getPlayer(arena.getJudgedPlayerName()).getActivePotionEffects()) {
										Bukkit.getPlayer(arena.getJudgedPlayerName()).removePotionEffect(potionEffect.getType());
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

										Bukkit.getPlayer(arena.getJudgedPlayerName()).getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("stats-item-slot") - 1, itemStack2);
									}
									Bukkit.getPlayer(arena.getJudgedPlayerName()).getInventory().setItem(plugin.getConfigManager().getConfig("config.yml").getInt("lobby-item-slot") - 1, itemStack1);
									Bukkit.getPlayer(arena.getJudgedPlayerName()).updateInventory();

									arena.getGameScoreboard().getTeam("Players").removePlayer(Bukkit.getPlayer(arena.getJudgedPlayerName()));
									arena.getGameScoreboard().getTeam("Guardians").addPlayer(Bukkit.getPlayer(arena.getJudgedPlayerName()));

									plugin.getMultiWorld().getNMSManager().setPlayerVisibility(Bukkit.getPlayer(arena.getJudgedPlayerName()), null, false);
								}

								arena.getPlayerPercent().remove(arena.getJudgedPlayerName());
								arena.getPlots().remove(arena.getJudgedPlayerName());

								if (arena.getPlots().size() == 1) {
									arena.setSecondPlace(arena.getJudgedPlayerName());
									arena.setFirstPlace((String) arena.getPlots().keySet().toArray()[0]);

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
										if (score.get() >= 0) {
											if (!arena.getPlots().containsKey(p.getName())) {
												arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + p.getName()), arena.getGameScoreboard())).setScore(score.get());
												score.getAndDecrement();
											}
										}
									});

									arena.getPlayers().forEach(p -> {
										for (String string : plugin.getConfigManager().getConfig("messages.yml").getStringList("MULTIWORLD.MAIN-GAME_END_DESCRIPTION")) {
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + string.replaceAll("%PLAYER1%", arena.getFirstPlace()).replaceAll("%PLAYER2%", arena.getSecondPlace()).replaceAll("%PLAYER3%", arena.getThirdPlace())));
										}

										p.setScoreboard(arena.getGameScoreboard());
										p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&e" + arena.getFirstPlace()), ChatColor.translateAlternateColorCodes('&', "&e" + translate("TITLE-WON_THE_GAME")), 10, 130, 20);

										p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
										if (p.getName().equals(arena.getFirstPlace())) {
											arena.setJudgeTimerID3(new BukkitRunnable() {
												public void run() {
													for (String winnerCommand : plugin.getMultiWorld().winnerCommands) {
														Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerCommand.replaceFirst("/", "").replaceAll("%PLAYER%", p.getName()));
													}

													plugin.getStatsManager().incrementStat(StatsType.WINS, p, 1);

													Bukkit.getPluginManager().callEvent(new PlayerWinEvent(p));
												}
											}.runTaskLater(plugin, 5L).getTaskId());
										}
									});
									arena.setJudgeTimerID4(new BukkitRunnable() {
										public void run() {
											plugin.getMultiWorld().getArenaManager().endArena(arena.getName());
										}
									}.runTaskLater(plugin, 10*20L).getTaskId());
								} else {
									if (arena.getPlots().size() == 2) {
										arena.setThirdPlace(arena.getJudgedPlayerName());
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
										if (score.get() >= 0) {
											if (!arena.getPlots().containsKey(p.getName())) {
												arena.getGameScoreboard().getObjective("SpeedBuilders").getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&8" + p.getName()), arena.getGameScoreboard())).setScore(score.get());
												score.getAndDecrement();
											}
										}
									});
									arena.getPlayers().forEach(p -> {
										p.setScoreboard(arena.getGameScoreboard());
									});

									arena.setJudgeTimerID5(new BukkitRunnable() {
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
											showCaseTimer(arena.getName());
										}
									}.runTaskLater(plugin, 3*20L).getTaskId());
								}
								arena.setJudgedPlayerName(null);
							}
						}.runTaskLater(plugin, 5*20L).getTaskId());
					} else {
						if (Math.floor(arena.getJudgeTime()) == arena.getJudgeTime()) {
							if (arena.getJudgeTime() == 4) {
								arena.getPlayers().forEach(p -> {
									if (arena.getPlots().containsKey(p.getName())) {
										if (arena.getPlayerPercent().get(p.getName()) <= 100 && arena.getPlayerPercent().get(p.getName()) >= 75) {
											p.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_SCORED_PERCENT").replaceAll("%PERCENT_COLOR%", "&b").replaceAll("%PERCENT%", "" + arena.getPlayerPercent().get(p.getName()))), 0, 2*15, 10);
										}
										if (arena.getPlayerPercent().get(p.getName()) <= 74 && arena.getPlayerPercent().get(p.getName()) >= 50) {
											p.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_SCORED_PERCENT").replaceAll("%PERCENT_COLOR%", "&a").replaceAll("%PERCENT%", "" + arena.getPlayerPercent().get(p.getName()))), 0, 2*15, 10);
										}
										if (arena.getPlayerPercent().get(p.getName()) <= 49 && arena.getPlayerPercent().get(p.getName()) >= 25) {
											p.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_SCORED_PERCENT").replaceAll("%PERCENT_COLOR%", "&e").replaceAll("%PERCENT%", "" + arena.getPlayerPercent().get(p.getName()))), 0, 2*15, 10);
										}
										if (arena.getPlayerPercent().get(p.getName()) <= 24 && arena.getPlayerPercent().get(p.getName()) >= 0) {
											p.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_SCORED_PERCENT").replaceAll("%PERCENT_COLOR%", "&c").replaceAll("%PERCENT%", "" + arena.getPlayerPercent().get(p.getName()))), 0, 2*15, 10);
										}
									}
								});
							}
						}

						plugin.getMultiWorld().getGuardianManager().rotateGuardian(7.5F, arena.getName());
					}
				}
			}.runTaskTimer(plugin, 0L, 1L).getTaskId());
			arena.setJudgeTimerID6(new BukkitRunnable() {
				public void run() {
					arena.getPlayers().forEach(p -> {
						p.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-GWEN_IS_JUDGING")), 0, 2*20, 10);
					});
				}
			}.runTaskLater(plugin, 2*20L).getTaskId());
			plugin.getMultiWorld().getSignManager().updateSigns(arena.getName());
		}
	}

	public void guardianIsImpressed(String arenaName) {
		final Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			Bukkit.getScheduler().cancelTask(arena.getGameStartTimerID());
			Bukkit.getScheduler().cancelTask(arena.getShowCaseTimerID());
			Bukkit.getScheduler().cancelTask(arena.getBuildTimerID());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID1());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID2());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID3());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID4());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID5());
			Bukkit.getScheduler().cancelTask(arena.getJudgeTimerID6());

			arena.getPlayers().forEach(p -> {
				if (arena.getPlots().containsKey(p.getName())) {
					p.getInventory().setArmorContents(null);
					p.getInventory().clear();
					p.setExp(0);
					p.setFireTicks(0);
					p.setFoodLevel(20);
					p.setGameMode(GameMode.SPECTATOR);
					p.setHealth(20);
					p.setLevel(0);
					p.setAllowFlight(true);
					p.setFlying(true);
					for (PotionEffect potionEffect : p.getActivePotionEffects()) {
						p.removePotionEffect(potionEffect.getType());
					}
				}

				p.spawnParticle(Particle.ELDER_GUARDIAN, p.getLocation(), 1, 0F, 0F, 0F, 1F);
				p.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-GWEN_IS_IMPRESSED")), 0, 5*20, 0);

				p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.85f, 1f);
			});

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

					showCaseTimer(arena.getName());
				}
			}.runTaskLater(plugin, 3*20L);
		}
	}

	public void cooldownTimer(String arenaName, final String arenaPlayer) {
		final Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			new BukkitRunnable() {
				public void run() {
					if (arena.getPlayers().contains(arenaPlayer)) {
						float cooldownTime = arena.getPlayersDoubleJumpCooldowned().get(arenaPlayer);
						cooldownTime = cooldownTime - 0.1F;
						String cooldownTimeFormat = String.format(Locale.UK, "%.1f", cooldownTime);
						cooldownTime = Float.parseFloat(cooldownTimeFormat);
						arena.getPlayersDoubleJumpCooldowned().put(arenaPlayer, cooldownTime);

						if (cooldownTime <= 0) {
							this.cancel();
							arena.getPlayersDoubleJumpCooldowned().remove(arenaPlayer);
						}
					} else {
						this.cancel();
						arena.getPlayersDoubleJumpCooldowned().remove(arenaPlayer);
					}
				}
			}.runTaskTimer(plugin, 0L, 2L);
		}
	}

	public Location getCenter1(Location location1, Location location2) {
		double x1 = Math.min(location1.getX(), location2.getX());
		double y1 = Math.min(location1.getY(), location2.getY());
		double z1 = Math.min(location1.getZ(), location2.getZ());
		double x2 = Math.max(location1.getX(), location2.getX());
		double y2 = Math.max(location1.getY(), location2.getY());
		double z2 = Math.max(location1.getZ(), location2.getZ());

		return new Location(location1.getWorld(), x1 + (x2 - x1) / 2.0D, y1 + (y2 - y1) / 2.0D, z1 + (z2 - z1) / 2.0D);
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
}
