package ee.mathiaskivi.speedbuilders.multiworld.listener;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

@SuppressWarnings("deprecation")
public class SetupListener2 implements Listener {
	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Action action = e.getAction();

		if (!player.hasPermission("sb.command.setup")) {
			return;
		} else if (!player.getItemInHand().hasItemMeta()) {
			return;
		} else if (!player.getItemInHand().getItemMeta().hasDisplayName()) {
			return;
		} else if (!player.getItemInHand().getItemMeta().hasLore()) {
			return;
		} else if (!player.getItemInHand().getItemMeta().getLore().contains("§3Setup")) {
			return;
		}

		if (player.getItemInHand().getItemMeta().getDisplayName().equals("Set spawnpoint location for main lobby")) {
			e.setCancelled(true);

			if (action == Action.LEFT_CLICK_BLOCK) {
				double x = e.getClickedBlock().getLocation().getBlockX() + 0.5;
				double y = e.getClickedBlock().getLocation().getBlockY() + 1;
				double z = e.getClickedBlock().getLocation().getBlockZ() + 0.5;

				plugin.getConfigManager().getConfig("lobby.yml").set("lobby.spawn.world", e.getClickedBlock().getLocation().getWorld().getName());
				plugin.getConfigManager().getConfig("lobby.yml").set("lobby.spawn.x", x);
				plugin.getConfigManager().getConfig("lobby.yml").set("lobby.spawn.y", y);
				plugin.getConfigManager().getConfig("lobby.yml").set("lobby.spawn.z", z);
				plugin.getConfigManager().getConfig("lobby.yml").set("lobby.spawn.pitch", 0.0);
				plugin.getConfigManager().getConfig("lobby.yml").set("lobby.spawn.yaw", 0.0);
				new BukkitRunnable() {
					public void run() {
						plugin.getConfigManager().saveConfig("lobby.yml");
					}
				}.runTaskAsynchronously(plugin);

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Spawnpoint location is successfully saved into configuration file for &fmain &7lobby!"));
			}

			return;
		}

		if (!plugin.getMultiWorld().setup.containsKey(player.getName())) {
			return;
		}

		if (player.getItemInHand().getItemMeta().getDisplayName().equals("Set spawnpoint location for arena lobby")) {
			e.setCancelled(true);

			if (action == Action.LEFT_CLICK_BLOCK) {
				String arenaName = plugin.getMultiWorld().setup.get(player.getName());

				double x = e.getClickedBlock().getLocation().getBlockX() + 0.5;
				double y = e.getClickedBlock().getLocation().getBlockY() + 1;
				double z = e.getClickedBlock().getLocation().getBlockZ() + 0.5;

				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".lobby.spawn.world", e.getClickedBlock().getLocation().getWorld().getName());
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".lobby.spawn.x", x);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".lobby.spawn.y", y);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".lobby.spawn.z", z);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".lobby.spawn.pitch", 0.0);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".lobby.spawn.yaw", 0.0);
				new BukkitRunnable() {
					public void run() {
						plugin.getConfigManager().saveConfig("arenas.yml");
					}
				}.runTaskAsynchronously(plugin);

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Spawnpoint location is successfully saved into configuration file for &farena &7lobby!"));
			}
		} else if (player.getItemInHand().getItemMeta().getDisplayName().equals("Set spawnpoint location for guardian plot")) {
			e.setCancelled(true);

			if (action == Action.LEFT_CLICK_BLOCK) {
				String arenaName = plugin.getMultiWorld().setup.get(player.getName());

				double x = e.getClickedBlock().getLocation().getBlockX() + 0.5;
				double y = e.getClickedBlock().getLocation().getBlockY() + 10;
				double z = e.getClickedBlock().getLocation().getBlockZ() + 0.5;

				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.spawnpoint.world", e.getClickedBlock().getLocation().getWorld().getName());
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.spawnpoint.x", x);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.spawnpoint.y", y);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.spawnpoint.z", z);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.spawnpoint.pitch", 0.0);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.spawnpoint.yaw", 0.0);
				new BukkitRunnable() {
					public void run() {
						plugin.getConfigManager().saveConfig("arenas.yml");
					}
				}.runTaskAsynchronously(plugin);

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Spawnpoint location is successfully saved into configuration file for &fguardian &7plot!"));
			}
		} else if (player.getItemInHand().getItemMeta().getDisplayName().equals("Set template area positions for guardian plot")) {
			e.setCancelled(true);

			if (action == Action.LEFT_CLICK_BLOCK) {
				String arenaName = plugin.getMultiWorld().setup.get(player.getName());

				double x1 = e.getClickedBlock().getLocation().getBlockX();
				double y1 = e.getClickedBlock().getLocation().getBlockY();
				double z1 = e.getClickedBlock().getLocation().getBlockZ();

				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.template-area.x1", x1);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.template-area.y1", y1);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.template-area.z1", z1);
				new BukkitRunnable() {
					public void run() {
						plugin.getConfigManager().saveConfig("arenas.yml");
					}
				}.runTaskAsynchronously(plugin);

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for template area position 1 was successfully saved into configuration file for &fguardian &7plot!"));
			} else if (action == Action.RIGHT_CLICK_BLOCK) {
				String arenaName = plugin.getMultiWorld().setup.get(player.getName());

				double x2 = e.getClickedBlock().getLocation().getBlockX();
				double y2 = e.getClickedBlock().getLocation().getBlockY();
				double z2 = e.getClickedBlock().getLocation().getBlockZ();

				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.template-area.x2", x2);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.template-area.y2", y2);
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arenaName + ".plots.guardian.template-area.z2", z2);
				new BukkitRunnable() {
					public void run() {
						plugin.getConfigManager().saveConfig("arenas.yml");
					}
				}.runTaskAsynchronously(plugin);

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for template area position 2 was successfully saved into configuration file for &fguardian &7plot!"));
			}
		} else if (player.getItemInHand().getItemMeta().getDisplayName().equals("Set spawnpoint location for each player plot")) {
			e.setCancelled(true);

			if (action == Action.LEFT_CLICK_BLOCK) {
				plugin.getMultiWorld().location1 = e.getClickedBlock().getLocation();
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for plot spawnpoint was successfully saved into cache."));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Type &f/sb setup spawnpoint <plot> &7to save the plot spawnpoint into configuration file."));
			}
		} else if (player.getItemInHand().getItemMeta().getDisplayName().equals("Set plot area positions for each player plot")) {
			e.setCancelled(true);

			if (action == Action.LEFT_CLICK_BLOCK) {
				plugin.getMultiWorld().location2 = e.getClickedBlock().getLocation();
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for plot area position 1 was successfully saved into cache."));
				if (plugin.getMultiWorld().location3 != null) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Type &f/sb setup plot-area <plot> &7to save both plot area positions into configuration file."));
				}
			} else if (action == Action.RIGHT_CLICK_BLOCK) {
				plugin.getMultiWorld().location3 = e.getClickedBlock().getLocation();
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for plot area position 2 was successfully saved into cache."));
				if (plugin.getMultiWorld().location2 != null) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Type &f/sb setup plot-area <plot> &7to save both plot area positions into configuration file."));
				}
			}
		} else if (player.getItemInHand().getItemMeta().getDisplayName().equals("Set laser location for each player plot")) {
			e.setCancelled(true);

			if (action == Action.LEFT_CLICK_BLOCK) {
				plugin.getMultiWorld().location1 = e.getClickedBlock().getLocation();
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for laser spawnpoint was successfully saved into cache."));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Type &f/sb setup laser <plot> &7to save the laser spawnpoint into configuration file."));
			}
		} else if (player.getItemInHand().getItemMeta().getDisplayName().equals("Set build area positions for each player plot")) {
			e.setCancelled(true);

			if (action == Action.LEFT_CLICK_BLOCK) {
				plugin.getMultiWorld().location2 = e.getClickedBlock().getLocation();
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for build area position 1 was successfully saved into cache."));
				if (plugin.getMultiWorld().location3 != null) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Type &f/sb setup build-area <plot> &7to save both build area positions into configuration file."));
				}
			} else if (action == Action.RIGHT_CLICK_BLOCK) {
				plugin.getMultiWorld().location3 = e.getClickedBlock().getLocation();
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for build area position 2 was successfully saved into cache."));
				if (plugin.getMultiWorld().location2 != null) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Type &f/sb setup build-area <plot> &7to save both build area positions into configuration file."));
				}
			}
		}
	}
}
