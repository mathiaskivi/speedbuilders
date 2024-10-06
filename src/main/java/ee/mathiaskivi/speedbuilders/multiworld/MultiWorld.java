package ee.mathiaskivi.speedbuilders.multiworld;

import com.google.common.base.Splitter;
import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.multiworld.command.SBCommand;
import ee.mathiaskivi.speedbuilders.multiworld.listener.*;
import ee.mathiaskivi.speedbuilders.multiworld.manager.*;
import ee.mathiaskivi.speedbuilders.utility.Translations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

public class MultiWorld {
	public SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public ArenaManager arenaManager;
	public GuardianManager guardianManager;
	public KitManager kitManager;
	public NMSManager nmsManager;
	public SignManager signManager;
	public TemplateManager templateManager;
	public TimerManager timerManager;

	public ArrayList<String> playersSignCooldowned = new ArrayList<>();
	public HashMap<String, Double> playerTempHealth = new HashMap<>();
	public HashMap<String, Integer> playerTempFoodLevel = new HashMap<>();
	public HashMap<String, Float> playerTempExp = new HashMap<>();
	public HashMap<String, Integer> playerTempLevel = new HashMap<>();
	public HashMap<String, GameMode> playerTempGameMode = new HashMap<>();
	public HashMap<String, org.bukkit.inventory.ItemStack[]> playerTempArmor = new HashMap<>();
	public HashMap<String, org.bukkit.inventory.ItemStack[]> playerTempItems = new HashMap<>();
	public HashMap<String, Collection<PotionEffect>> playerTempEffects = new HashMap<>();

	public HashMap<String, String> setup = new HashMap<>();
	public Location location1 = null;
	public Location location3 = null;
	public Location location2 = null;
	public HashMap<String, List<Block>> blocks = new HashMap<>();

	public List<String> winnerCommands = new ArrayList<>();
	public List<String> loserCommands = new ArrayList<>();

	public void onEnable() {
		File folder = new File("plugins/" + plugin.getDescription().getName());
		if (!folder.exists()) {
			folder.mkdirs();
		}

		plugin.getConfigManager().loadConfig("arenas.yml");
		plugin.getConfigManager().loadConfig("lobby.yml");
		plugin.getConfigManager().loadConfig("messages.yml");
		plugin.getConfigManager().loadConfig("signs.yml");
		plugin.getConfigManager().loadConfig("templates.yml");
		plugin.getConfigManager().loadConfig("spigot.yml", new File("."));

		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
		Bukkit.getPluginCommand("speedbuilders").setExecutor(new SBCommand());
		Bukkit.getPluginManager().registerEvents(new GuardianListener(), plugin);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
		Bukkit.getPluginManager().registerEvents(new SetupListener1(), plugin);
		Bukkit.getPluginManager().registerEvents(new SetupListener2(), plugin);
		Bukkit.getPluginManager().registerEvents(new SignManager(), plugin);
		Bukkit.getPluginManager().registerEvents(new WorldListener(), plugin);

		arenaManager = new ArenaManager();
		guardianManager = new GuardianManager();
		kitManager = new KitManager();
		nmsManager = new NMSManager();
		signManager = new SignManager();
		templateManager = new TemplateManager();
		timerManager = new TimerManager();

		plugin.getStatsManager().openConnection();

		for (String message : plugin.getConfigManager().getConfig("messages.yml").getConfigurationSection("MULTIWORLD").getKeys(false)) {
			Translations.messages.put(message, plugin.getConfigManager().getConfig("messages.yml").getString("MULTIWORLD." + message));
		}

		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("winner-commands.enabled")) {
			winnerCommands = plugin.getConfigManager().getConfig("config.yml").getStringList("winner-commands.commands");
		}
		if (plugin.getConfigManager().getConfig("config.yml").getBoolean("loser-commands.enabled")) {
			loserCommands = plugin.getConfigManager().getConfig("config.yml").getStringList("loser-commands.commands");
		}

		arenaManager.loadArenas();
	}

	public void onDisable() {
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if (playerTempHealth.containsKey(onlinePlayer.getName()) && playerTempFoodLevel.containsKey(onlinePlayer.getName()) && playerTempExp.containsKey(onlinePlayer.getName()) && playerTempLevel.containsKey(onlinePlayer.getName()) && playerTempGameMode.containsKey(onlinePlayer.getName()) && playerTempArmor.containsKey(onlinePlayer.getName()) && playerTempItems.containsKey(onlinePlayer.getName()) && playerTempEffects.containsKey(onlinePlayer.getName())) {
				if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
					Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
					location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
					location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
					onlinePlayer.teleport(location);
				} else {
					onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				}

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
				onlinePlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				for (PotionEffect potionEffect : onlinePlayer.getActivePotionEffects()) {
					onlinePlayer.removePotionEffect(potionEffect.getType());
				}

				loadTempInfo(onlinePlayer);
				onlinePlayer.updateInventory();
			}
		}
	}

	public ArenaManager getArenaManager() {
		return arenaManager;
	}

	public GuardianManager getGuardianManager() {
		return guardianManager;
	}

	public KitManager getKitManager() {
		return kitManager;
	}

	public NMSManager getNMSManager() {
		return nmsManager;
	}

	public SignManager getSignManager() {
		return signManager;
	}

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public TimerManager getTimerManager() {
		return timerManager;
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

	public void saveTempInfo(Player player) {
		playerTempHealth.put(player.getName(), player.getHealth());
		playerTempFoodLevel.put(player.getName(), player.getFoodLevel());
		playerTempExp.put(player.getName(), player.getExp());
		playerTempLevel.put(player.getName(), player.getLevel());
		playerTempGameMode.put(player.getName(), player.getGameMode());
		playerTempArmor.put(player.getName(), player.getInventory().getArmorContents());
		playerTempItems.put(player.getName(), player.getInventory().getContents());
		playerTempEffects.put(player.getName(), player.getActivePotionEffects());
	}

	public void loadTempInfo(Player player) {
		player.setHealth(playerTempHealth.get(player.getName()));
		player.setFoodLevel(playerTempFoodLevel.get(player.getName()));
		player.setExp(playerTempExp.get(player.getName()));
		player.setLevel(playerTempLevel.get(player.getName()));
		player.setGameMode(playerTempGameMode.get(player.getName()));

		player.getInventory().setArmorContents(playerTempArmor.get(player.getName()));
		player.getInventory().setContents(playerTempItems.get(player.getName()));

		player.addPotionEffects(playerTempEffects.get(player.getName()));

		playerTempHealth.remove(player.getName());
		playerTempFoodLevel.remove(player.getName());
		playerTempExp.remove(player.getName());
		playerTempLevel.remove(player.getName());
		playerTempGameMode.remove(player.getName());
		playerTempArmor.remove(player.getName());
		playerTempItems.remove(player.getName());
		playerTempEffects.remove(player.getName());
	}
}
