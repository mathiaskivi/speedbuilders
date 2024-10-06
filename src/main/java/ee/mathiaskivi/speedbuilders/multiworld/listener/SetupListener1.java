package ee.mathiaskivi.speedbuilders.multiworld.listener;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.utility.VoidGenerator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

public class SetupListener1 implements Listener {
	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		InventoryView inventoryView = e.getView();
		ItemStack itemStack1 = e.getCurrentItem();

		if (!inventoryView.getTitle().equals(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY") + "&8Setup"))) {
			return;
		} else if (itemStack1 == null) {
			return;
		}

		if (itemStack1.getType() == Material.PODZOL) {
			e.setCancelled(true);

			if (!plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn")) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7You have to set spawnpoint location for main lobby!"));
				return;
			}

			Inventory inventory = Bukkit.createInventory(player, InventoryType.CHEST, ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY") + "&8Setup"));

			int slot = 0;
			for (String arena : plugin.getConfigManager().getConfig("arenas.yml").getConfigurationSection("arenas").getKeys(false)) {
				ItemStack itemStack2 = new ItemStack(Material.GRASS_BLOCK);
				ItemMeta itemMeta2 = itemStack2.getItemMeta();
				itemMeta2.setDisplayName(arena);
				itemStack2.setItemMeta(itemMeta2);

				inventory.setItem(slot, itemStack2);
				slot++;
			}

			player.openInventory(inventory);
		} else if (itemStack1.getType() == Material.GRASS_BLOCK) {
			e.setCancelled(true);

			String[] folders = new File(plugin.getDataFolder() + "/../../").list((parent, child) -> new File(parent, child).isDirectory());
			if (!Arrays.asList(folders).contains(itemStack1.getItemMeta().getDisplayName())) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7World folder can not be found for that arena!"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Shut down the server and upload the world to this server!"));
				return;
			} else if (plugin.getMultiWorld().setup.containsValue(itemStack1.getItemMeta().getDisplayName())) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7That arena is already being set up!"));
				return;
			}

			String arena = itemStack1.getItemMeta().getDisplayName();

			WorldCreator worldCreator = new WorldCreator(arena);
			worldCreator.generator(new VoidGenerator());
			Bukkit.createWorld(worldCreator);
			for (Entity entity : Bukkit.getWorld(arena).getEntities()) {
				if (entity.getType() != EntityType.PLAYER) {
					entity.remove();
				}
			}

			Location location = Bukkit.getWorld(arena).getSpawnLocation();
			location.setY(location.getWorld().getHighestBlockYAt(location));
			player.teleport(location);

			plugin.getMultiWorld().getTemplateManager().resetPlots(arena);

			player.getInventory().clear();
			player.setAllowFlight(true);
			player.setExp(0);
			player.setFireTicks(0);
			player.setFlying(true);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.CREATIVE);
			player.setHealth(20);
			player.setLevel(0);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}
			player.updateInventory();

			player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Type &f/sb setup &7again to start setting up your arena."));
			plugin.getMultiWorld().setup.put(player.getName(), itemStack1.getItemMeta().getDisplayName());
		} else if (itemStack1.getType() == Material.NETHER_STAR) {
			player.closeInventory();
			player.getInventory().clear();

			ItemStack itemStack2 = new ItemStack(Material.NETHER_STAR);
			ItemMeta itemMeta2 = itemStack2.getItemMeta();
			itemMeta2.setDisplayName("Set spawnpoint location for main lobby");
			List<String> lore2 = new ArrayList<>();
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5Left-click to set spawnpoint location."));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&cIt sets the spawnpoint location's &fY-coordinate"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&c1 block &fhigher&c than the clicked block!"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&3Setup"));
			itemMeta2.setLore(lore2);
			itemStack2.setItemMeta(itemMeta2);

			player.getInventory().addItem(itemStack2);
		} else if (itemStack1.getType() == Material.BEACON) {
			player.closeInventory();
			player.getInventory().clear();

			ItemStack itemStack2 = new ItemStack(Material.BEACON);
			ItemMeta itemMeta2 = itemStack2.getItemMeta();
			itemMeta2.setDisplayName("Set spawnpoint location for arena lobby");
			List<String> lore2 = new ArrayList<>();
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5Left-click to set spawnpoint location."));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&cIt sets the spawnpoint location's &fY-coordinate"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&c1 block &fhigher&c than the clicked block!"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&3Setup"));
			itemMeta2.setLore(lore2);
			itemStack2.setItemMeta(itemMeta2);

			player.getInventory().addItem(itemStack2);
		} else if (itemStack1.getType() == Material.GUARDIAN_SPAWN_EGG) {
			player.closeInventory();
			player.getInventory().clear();

			ItemStack itemStack2 = new ItemStack(Material.GUARDIAN_SPAWN_EGG);
			ItemMeta itemMeta2 = itemStack2.getItemMeta();
			itemMeta2.setDisplayName("Set spawnpoint location for guardian plot");
			List<String> lore2 = new ArrayList<>();
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5Left-click to set spawnpoint location."));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&cIt sets the spawnpoint location's &fY-coordinate"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&c10 blocks &fhigher&c than the clicked block!"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&3Setup"));
			itemMeta2.setLore(lore2);
			itemStack2.setItemMeta(itemMeta2);

			player.getInventory().addItem(itemStack2);
		} else if (itemStack1.getType() == Material.PAPER) {
			player.closeInventory();
			player.getInventory().clear();

			ItemStack itemStack2 = new ItemStack(Material.PAPER);
			ItemMeta itemMeta2 = itemStack2.getItemMeta();
			itemMeta2.setDisplayName("Set template area positions for guardian plot");
			List<String> lore2 = new ArrayList<>();
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5Left-click to select corner 1 and"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5right-click to select corner 2."));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&3Setup"));
			itemMeta2.setLore(lore2);
			itemStack2.setItemMeta(itemMeta2);

			player.getInventory().addItem(itemStack2);
		} else if (itemStack1.getType() == Material.WHITE_BED) {
			player.closeInventory();
			player.getInventory().clear();

			ItemStack itemStack2 = new ItemStack(Material.WHITE_BED);
			ItemMeta itemMeta2 = itemStack2.getItemMeta();
			itemMeta2.setDisplayName("Set spawnpoint location for each player plot");
			List<String> lore2 = new ArrayList<>();
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5Left-click to set spawnpoint location."));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&cIt sets the spawnpoint location's &fY-coordinate"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&c1 block &fhigher&c than the clicked block!"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&3Setup"));
			itemMeta2.setLore(lore2);
			itemStack2.setItemMeta(itemMeta2);

			player.getInventory().addItem(itemStack2);
		} else if (itemStack1.getType() == Material.WOODEN_AXE) {
			player.closeInventory();
			player.getInventory().clear();

			ItemStack itemStack2 = new ItemStack(Material.WOODEN_AXE);
			ItemMeta itemMeta2 = itemStack2.getItemMeta();
			itemMeta2.setDisplayName("Set plot area positions for each player plot");
			List<String> lore2 = new ArrayList<>();
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5Left-click to select corner 1 and"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5right-click to select corner 2."));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&3Setup"));
			itemMeta2.setLore(lore2);
			itemStack2.setItemMeta(itemMeta2);

			player.getInventory().addItem(itemStack2);
		} else if (itemStack1.getType() == Material.ARMOR_STAND) {
			player.closeInventory();
			player.getInventory().clear();

			ItemStack itemStack2 = new ItemStack(Material.ARMOR_STAND);
			ItemMeta itemMeta2 = itemStack2.getItemMeta();
			itemMeta2.setDisplayName("Set laser location for each player plot");
			List<String> lore2 = new ArrayList<>();
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5Left-click to set laser location."));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&3Setup"));
			itemMeta2.setLore(lore2);
			itemStack2.setItemMeta(itemMeta2);

			player.getInventory().addItem(itemStack2);
		} else if (itemStack1.getType() == Material.OAK_FENCE) {
			player.closeInventory();
			player.getInventory().clear();

			ItemStack itemStack2 = new ItemStack(Material.OAK_FENCE);
			ItemMeta itemMeta2 = itemStack2.getItemMeta();
			itemMeta2.setDisplayName("Set build area positions for each player plot");
			List<String> lore2 = new ArrayList<>();
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5Left-click to select corner 1 and"));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&5right-click to select corner 2."));
			lore2.add(ChatColor.translateAlternateColorCodes('&', "&3Setup"));
			itemMeta2.setLore(lore2);
			itemStack2.setItemMeta(itemMeta2);

			player.getInventory().addItem(itemStack2);
		} else if (itemStack1.getType() == Material.WRITABLE_BOOK) {
			player.closeInventory();
			player.getInventory().clear();

			String arena = plugin.getMultiWorld().setup.get(player.getName());
			if (!plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena + ".plots.guardian.template-area")) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Location for at least one of the template area positions does not exist."));
				return;
			}

			List<Block> blocks = new ArrayList<>();

			int x1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.x2"));
			int y1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.y2"));
			int z1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.z2"));
			int x2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.x2"));
			int y2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.y2"));
			int z2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena + ".plots.guardian.template-area.z2"));

			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					for (int z = z1; z <= z2; z++) {
						blocks.add(Bukkit.getWorld(arena).getBlockAt(x, y, z));
					}
				}
			}

			plugin.getMultiWorld().blocks.put(player.getName(), blocks);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Blocks for template were successfully saved into cache."));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7Type &f/sb setup template <raw> <display> &7to save the template into configuration file."));
		} else if (itemStack1.getType() == Material.CLOCK) {
			player.closeInventory();
			player.getInventory().clear();

			if (!plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn")) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
				return;
			}

			Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
			location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
			location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
			player.teleport(location);

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

			String arena = plugin.getMultiWorld().setup.get(player.getName());
			plugin.getMultiWorld().getTemplateManager().savePlots(arena);

			Bukkit.getWorld(arena).save();
			plugin.getMultiWorld().setup.remove(player.getName());
		}
	}
}
