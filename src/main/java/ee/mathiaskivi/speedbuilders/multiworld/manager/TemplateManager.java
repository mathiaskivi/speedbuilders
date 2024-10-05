package ee.mathiaskivi.speedbuilders.multiworld.manager;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.api.event.PlayerPerfectEvent;
import ee.mathiaskivi.speedbuilders.multiworld.Arena;
import ee.mathiaskivi.speedbuilders.utility.StatsType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

public class TemplateManager {
	public SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public void check(String plot, Player player, String arenaName) {
		Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			HashMap<Integer, String> placedBlocks = new HashMap<>();

			int x1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x2"));
			int y1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y2"));
			int z1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z2"));
			int x2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x2"));
			int y2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y2"));
			int z2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z2"));

			int order = 0;
			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					for (int z = z1; z <= z2; z++) {
						if (y != y1) {
							BlockState blockState = Bukkit.getWorld(arena.getName()).getBlockAt(x, y, z).getState();

							if (!blockState.getType().toString().equals("AIR")) {
								if (blockState.getBlockData().getAsString().equals(arena.getCurrentBuildBlocks().get(order))) {
									placedBlocks.put(order, blockState.getBlockData().getAsString());
								}
							}
						}

						order++;
					}
				}
			}

			int percent = (100 * placedBlocks.size()) / arena.getCurrentBuildBlocks().size();
			if (percent == 100) {
				float time = Math.round(10 * ((plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".build-time") - arena.getBuildTimeSubtractor()) - arena.getBuildTime())) / 10.0f;

				arena.getPlayers().forEach(p -> {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PERFECT_BUILD").replaceAll("%PLAYER%", player.getName()).replaceAll("%TIME%", time + " " + translate("MAIN-SECONDS"))));
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

					if (p.equals(player)) {
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
						player.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-PERFECT_MATCH")), 0, 2*20, 10);

						arena.getPlayerPercent().put(player.getName(), 100);

						plugin.getStatsManager().incrementStat(StatsType.PBUILDS, player, 1);

						Bukkit.getPluginManager().callEvent(new PlayerPerfectEvent(player, time));
					}
				});

				if (Collections.min(arena.getPlayerPercent().values()) >= 100) {
					plugin.getMultiWorld().getTimerManager().guardianIsImpressed(arena.getName());
				}
			} else {
				arena.getPlayerPercent().put(player.getName(), percent);
			}
		}
	}

	public void loadTemplate(String plot, String arenaName) {
		Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			if (plot.equalsIgnoreCase("guardian")) {
				int x1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.x2"));
				int y1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.y2"));
				int z1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.z2"));
				int x2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.x2"));
				int y2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.y2"));
				int z2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.z2"));

				int order = 0;
				for (int x = x1; x <= x2; x++) {
					for (int y = y1; y <= y2; y++) {
						for (int z = z1; z <= z2; z++) {
							BlockState blockState = Bukkit.getWorld(arena.getName()).getBlockAt(x, y, z).getState();
							blockState.setBlockData(Bukkit.createBlockData(plugin.getConfigManager().getConfig("templates.yml").getString("templates." + arena.getCurrentBuildRawName() + ".blocks." + order)));
							blockState.update(true,  false);

							order++;
						}
					}
				}
			} else {
				HashMap<Integer, String> blockData = new HashMap<>();

				int x1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x2"));
				int y1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y2"));
				int z1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z2"));
				int x2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x2"));
				int y2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y2"));
				int z2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z2"));

				int order = 0;
				for (int x = x1; x <= x2; x++) {
					for (int y = y1; y <= y2; y++) {
						for (int z = z1; z <= z2; z++) {
							BlockState blockState = Bukkit.getWorld(arena.getName()).getBlockAt(x, y, z).getState();
							blockState.setBlockData(Bukkit.createBlockData(plugin.getConfigManager().getConfig("templates.yml").getString("templates." + arena.getCurrentBuildRawName() + ".blocks." + order)));
							blockState.update(true,  false);

							if (y != y1) {
								if (!blockState.getType().toString().equals("AIR")) {
									blockData.put(order, blockState.getBlockData().getAsString());
								}
							}

							order++;
						}
					}
				}

				if (arena.getCurrentBuildBlocks().isEmpty()) {
					arena.getCurrentBuildBlocks().putAll(blockData);
				}
			}
		}
	}

	public void unloadTemplate(String plot, String arenaName) {
		Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			if (plot.equalsIgnoreCase("guardian")) {
				for (String block : plugin.getConfigManager().getConfig("arenas.yml").getConfigurationSection("arenas." + arena.getName() + ".plots.guardian.blocks").getKeys(false)) {
					String[] blockCoordinates = block.split(",");

					BlockState blockState = Bukkit.getWorld(arena.getName()).getBlockAt(Integer.parseInt(blockCoordinates[0]), Integer.parseInt(blockCoordinates[1]), Integer.parseInt(blockCoordinates[2])).getState();
					blockState.setBlockData(Bukkit.createBlockData(plugin.getConfigManager().getConfig("arenas.yml").getString("arenas." + arena.getName() + ".plots.guardian.blocks." + block)));
					blockState.update(true,  false);
				}
			} else {
				Player player = null;
				for (Entry<String, String> entrySet : arena.getPlots().entrySet()) {
					if (entrySet.getValue().equalsIgnoreCase(plot)) {
						player = Bukkit.getPlayer(entrySet.getKey());
						break;
					}
				}

				int x1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x2"));
				int y1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y2"));
				int z1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z2"));
				int x2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.x2"));
				int y2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.y2"));
				int z2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".build-area.z2"));

				for (int x = x1; x <= x2; x++) {
					for (int y = y1; y <= y2; y++) {
						for (int z = z1; z <= z2; z++) {
							if (y != y1) {
								BlockState blockState = Bukkit.getWorld(arena.getName()).getBlockAt(x, y, z).getState();
								BlockData blockData = blockState.getBlockData();

								if (blockData.getMaterial() != Material.AIR) {
									if (blockData.getMaterial() == Material.WALL_TORCH) {
										player.getInventory().addItem(new ItemStack(Material.TORCH));
									} else if (blockData instanceof Slab) {
										Slab slab = (Slab) blockData;

										if (slab.getType() == Slab.Type.DOUBLE) {
											player.getInventory().addItem(new ItemStack(Material.getMaterial(blockData.getMaterial().toString()), 2));
										} else {
											player.getInventory().addItem(new ItemStack(Material.getMaterial(blockData.getMaterial().toString())));
										}
									} else {
										player.getInventory().addItem(new ItemStack(Material.getMaterial(blockData.getMaterial().toString())));
									}

									blockState.setType(Material.AIR);
									blockState.update(true, false);

									plugin.getMultiWorld().getNMSManager().updateBlockConnections(blockState.getBlock());
								}
							}
						}
					}
				}

				Location location1 = new Location(Bukkit.getWorld(arena.getName()), x1, y1, z1);
				Location location2 = new Location(Bukkit.getWorld(arena.getName()), x2, y2, z2);
				Location getCenter = getCenter(location1, location2);

				getCenter.getWorld().playEffect(getCenter, Effect.STEP_SOUND, Material.OAK_LOG);
			}
		}
	}

	public void explodePlot(String plot, String arenaName) {
		Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			int x1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.x2"));
			int y1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.y2"));
			int z1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.z2"));
			int x2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.x2"));
			int y2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.y2"));
			int z2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.z2"));

			int fallingBlockNum = 0;
			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					for (int z = z1; z <= z2; z++) {
						BlockState blockState = Bukkit.getWorld(arena.getName()).getBlockAt(x, y, z).getState();

						if ((fallingBlockNum % 7) == 0) {
							if (blockState.getType() != Material.AIR) {
								FallingBlock fallingBlock = blockState.getWorld().spawnFallingBlock(blockState.getLocation().add(0, 1, 0), blockState.getBlockData());
								fallingBlock.setDropItem(false);
								fallingBlock.setVelocity(new Vector(((float) -0.15 + (float) (Math.random() * ((0.15 - -0.15) + 0.15))), 1F, ((float) 0.15 + (float)(Math.random() * ((-0.15 - 0.15) - 0.15)))));

								new BukkitRunnable() {
									public void run() {
										if (!fallingBlock.isDead()) {
											fallingBlock.remove();
										}
									}
								}.runTaskLater(plugin, 15*20L);
							}
						}

						blockState.setType(Material.AIR);
						blockState.update(true, false);
						fallingBlockNum++;
					}
				}
			}
		}
	}

	public void resetPlots(String arenaName) {
		Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			if (plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".plots")) {
				for (String plot : plugin.getConfigManager().getConfig("arenas.yml").getConfigurationSection("arenas." + arena.getName() + ".plots").getKeys(false)) {
					if (plugin.getConfigManager().getConfig("arenas.yml").contains("arenas." + arena.getName() + ".plots." + plot + ".blocks")) {
						for (String block : plugin.getConfigManager().getConfig("arenas.yml").getConfigurationSection("arenas." + arena.getName() + ".plots." + plot + ".blocks").getKeys(false)) {
							String[] blockCoordinates = block.split(",");

							BlockState blockState = Bukkit.getWorld(arena.getName()).getBlockAt(Integer.parseInt(blockCoordinates[0]), Integer.parseInt(blockCoordinates[1]), Integer.parseInt(blockCoordinates[2])).getState();
							blockState.setBlockData(Bukkit.createBlockData(plugin.getConfigManager().getConfig("arenas.yml").getString("arenas." + arena.getName() + ".plots." + plot + ".blocks." + block)));
							blockState.update(true,  false);
						}
					}
				}
			}
		}
	}

	public void savePlots(String arenaName) {
		Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

		if (arena != null) {
			for (String plot : plugin.getConfigManager().getConfig("arenas.yml").getConfigurationSection("arenas." + arena.getName() + ".plots").getKeys(false)) {
				plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + plot + ".blocks", "");

				if (plot.equalsIgnoreCase("guardian")) {
					int x1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.x2"));
					int y1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.y2"));
					int z1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.z2"));
					int x2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.x2"));
					int y2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.y2"));
					int z2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots.guardian.template-area.z2"));

					for (int x = x1; x <= x2; x++) {
						for (int y = y1; y <= y2; y++) {
							for (int z = z1; z <= z2; z++) {
								BlockState blockState = Bukkit.getWorld(arena.getName()).getBlockAt(x, y, z).getState();

								plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots.guardian.blocks." + x + "," + y + "," + z, blockState.getBlockData().getAsString().replaceFirst("minecraft:", ""));
							}
						}
					}
				} else {
					int x1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.x2"));
					int y1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.y2"));
					int z1 = Math.min(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.z2"));
					int x2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.x2"));
					int y2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.y2"));
					int z2 = Math.max(plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.z1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + plot + ".area.z2"));

					for (int x = x1; x <= x2; x++) {
						for (int y = y1; y <= y2; y++) {
							for (int z = z1; z <= z2; z++) {
								BlockState blockState = Bukkit.getWorld(arena.getName()).getBlockAt(x, y, z).getState();

								plugin.getConfigManager().getConfig("arenas.yml").set("arenas." + arena.getName() + ".plots." + plot + ".blocks." + x + "," + y + "," + z, blockState.getBlockData().getAsString().replaceFirst("minecraft:", ""));
							}
						}
					}
				}
			}

			new BukkitRunnable() {
				public void run() {
					plugin.getConfigManager().saveConfig("arenas.yml");
				}
			}.runTaskAsynchronously(plugin);
		}
	}

	public void saveTemplate(String raw, String display, List<Block> blocks) {
		plugin.getConfigManager().getConfig("templates.yml").set("templates." + raw, "");
		plugin.getConfigManager().getConfig("templates.yml").set("templates." + raw + ".display-name", display);

		int order = 0;
		for (Block block : blocks) {
			BlockState blockState = block.getState();

			plugin.getConfigManager().getConfig("templates.yml").set("templates." + raw + ".blocks." + order, blockState.getBlockData().getAsString().replaceFirst("minecraft:", ""));

			order++;
		}

		new BukkitRunnable() {
			public void run() {
				plugin.getConfigManager().saveConfig("templates.yml");
			}
		}.runTaskAsynchronously(plugin);
	}

	public Location getCenter(Location location1, Location location2) {
		double x1 = Math.min(location1.getX(), location2.getX());
		double y1 = Math.min(location1.getY(), location2.getY());
		double z1 = Math.min(location1.getZ(), location2.getZ());
		double x2 = Math.max(location1.getX(), location2.getX());
		double y2 = Math.max(location1.getY(), location2.getY());
		double z2 = Math.max(location1.getZ(), location2.getZ());

		return new Location(location1.getWorld(), x1 + (x2 - x1) / 2.0D, y1 + (y2 - y1) / 2.0D, z1 + (z2 - z1) / 2.0D);
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
}
