package ee.mathiaskivi.speedbuilders.multiworld.listener;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.multiworld.Arena;
import ee.mathiaskivi.speedbuilders.api.game.ArenaState;
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
import org.bukkit.util.Vector;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {
	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		if (Arena.arenaObjects.contains(plugin.getMultiWorld().getArenaManager().getArena(player.getWorld().getName()))) {
			if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
				Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
				location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
				location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
				player.teleport(location);
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
			}

			if (plugin.getMultiWorld().playerTempHealth.containsKey(player.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(player.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(player.getName()) && plugin.getMultiWorld().playerTempGameMode.containsKey(player.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(player.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(player.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(player.getName())) {
				player.getInventory().setArmorContents(null);
				player.getInventory().clear();
				player.setAllowFlight(false);
				player.setExp(0);
				player.setFireTicks(0);
				player.setFlying(false);
				player.setFoodLevel(20);
				player.setGameMode(GameMode.SURVIVAL);
				player.setHealth(20);
				player.setLevel(0);
				for (PotionEffect potionEffect : player.getActivePotionEffects()) {
					player.removePotionEffect(potionEffect.getType());
				}

				plugin.getMultiWorld().loadTempInfo(player);
				player.updateInventory();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		Arena tempArena = null;
		for (Arena arenaObject : Arena.arenaObjects) {
			if (arenaObject.getPlayers().contains(player.getName())) {
				tempArena = arenaObject;
			}
		}
		Arena arena = tempArena;
		if (arena != null) {
			plugin.getMultiWorld().getArenaManager().removePlayer(player, arena.getName());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Action action = e.getAction();
		Arena tempArena = null;
		for (Arena arenaObject : Arena.arenaObjects) {
			if (arenaObject.getPlayers().contains(player.getName())) {
				tempArena = arenaObject;
			}
		}
		Arena arena = tempArena;
		if (arena != null) {
			if (arena.getState() == ArenaState.WAITING) {
				if (player.getItemInHand().getType() == Material.CLOCK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")))) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						e.setCancelled(true);
					}
					if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
						e.setCancelled(true);

						plugin.getMultiWorld().getArenaManager().removePlayer(player, arena.getName());
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT").replaceAll("%PLAYER%", player.getName())));
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
			} else if (arena.getState() == ArenaState.STARTING) {
				if (player.getItemInHand().getType() == Material.CLOCK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")))) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						e.setCancelled(true);
					}
					if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
						e.setCancelled(true);

						plugin.getMultiWorld().getArenaManager().removePlayer(player, arena.getName());
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT").replaceAll("%PLAYER%", player.getName())));
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
			} else if (arena.getState() == ArenaState.DISPLAYING) {
				if (player.getItemInHand().getType() == Material.CLOCK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")))) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						e.setCancelled(true);
					}
					if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
						e.setCancelled(true);

						plugin.getMultiWorld().getArenaManager().removePlayer(player, arena.getName());
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT").replaceAll("%PLAYER%", player.getName())));
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
			} else if (arena.getState() == ArenaState.BUILDING) {
				if (player.getItemInHand().getType() == Material.CLOCK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")))) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						e.setCancelled(true);
					}
					if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
						e.setCancelled(true);

						plugin.getMultiWorld().getArenaManager().removePlayer(player, arena.getName());
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT").replaceAll("%PLAYER%", player.getName())));
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

				if (arena.getPlots().containsKey(player.getName())) {
					if (arena.getPlayerPercent().containsKey(player.getName())) {
						if (arena.getPlayerPercent().get(player.getName()) < 100) {
							if (action == Action.LEFT_CLICK_BLOCK) {
								e.setCancelled(true);

								Block block = e.getClickedBlock();
								Location location1 = block.getLocation();
								Location location2 = new Location(Bukkit.getWorld(arena.getName()), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.z1"));
								Location location3 = new Location(Bukkit.getWorld(arena.getName()), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.x2"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.y2"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.z2"));

								if (isBlockInside(location1, location2, location3)) {
									BlockState blockState = block.getState();
									BlockData blockData = blockState.getBlockData();

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

									block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());

									blockState.setType(Material.AIR);
									blockState.update(true, false);

									plugin.getMultiWorld().getNMSManager().updateBlockConnections(block);

									plugin.getMultiWorld().getTemplateManager().check(arena.getPlots().get(player.getName()), player, arena.getName());
								}
							}
						}
					}
				}
			} else if (arena.getState() == ArenaState.JUDGING) {
				if (player.getItemInHand().getType() == Material.CLOCK && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', translate("MAIN-LOBBY_ITEM")))) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						e.setCancelled(true);
					}
					if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
						e.setCancelled(true);

						plugin.getMultiWorld().getArenaManager().removePlayer(player, arena.getName());
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT").replaceAll("%PLAYER%", player.getName())));
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
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		Arena tempArena = null;
		for (Arena arenaObject : Arena.arenaObjects) {
			if (arenaObject.getPlayers().contains(player.getName())) {
				tempArena = arenaObject;
			}
		}
		Arena arena = tempArena;
		if (arena != null) {
			if (arena.getState() == ArenaState.BEGINNING) {
				if (arena.getPlots().containsKey(player.getName())) {
					if (((e.getTo().getX() != e.getFrom().getX()) || (e.getTo().getZ() != e.getFrom().getZ()))) {
						Location location = e.getFrom();
						location.setPitch(e.getTo().getPitch());
						location.setYaw(e.getTo().getYaw());
						e.setTo(location);
					}
				}
			} else if (arena.getState() == ArenaState.DISPLAYING) {
				if (arena.getPlots().containsKey(player.getName())) {
					if (!isPlayerInsideAsPlayer(e.getTo(), new Location(player.getWorld(), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.z1")), new Location(player.getWorld(), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.x2"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.y2"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.z2")))) {
						String plot = arena.getPlots().get(player.getName());

						Location location = new Location(Bukkit.getWorld(arena.getName()), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.x"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.y"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.z"));
						location.setPitch((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.pitch"));
						location.setYaw((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.yaw"));
						e.setTo(location);
						player.setFallDistance(0F);

						player.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_CANNOT_LEAVE")), 0, 2*15, 10);
					}
				}
			} else if (arena.getState() == ArenaState.BUILDING) {
				if (arena.getPlots().containsKey(player.getName())) {
					if (!isPlayerInsideAsPlayer(e.getTo(), new Location(player.getWorld(), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.z1")), new Location(player.getWorld(), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.x2"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.y2"), plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".area.z2")))) {
						String plot = arena.getPlots().get(player.getName());

						Location location = new Location(Bukkit.getWorld(arena.getName()), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.x"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.y"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.z"));
						location.setPitch((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.pitch"));
						location.setYaw((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + plot + ".spawnpoint.yaw"));
						e.setTo(location);
						player.setFallDistance(0F);

						player.sendTitle("", ChatColor.translateAlternateColorCodes('&', translate("TITLE-YOU_CANNOT_LEAVE")), 0, 2*15, 10);
					}
					if (!arena.getPlayersDoubleJumpCooldowned().containsKey(player.getName())) {
						player.setAllowFlight(true);
					} else {
						player.setAllowFlight(false);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		Player player = e.getPlayer();
		Arena tempArena = null;
		for (Arena arenaObject : Arena.arenaObjects) {
			if (arenaObject.getPlayers().contains(player.getName())) {
				tempArena = arenaObject;
			}
		}
		Arena arena = tempArena;
		if (arena != null) {
			if (e.getCause() == TeleportCause.SPECTATE) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			Arena tempArena = null;
			for (Arena arenaObject : Arena.arenaObjects) {
				if (arenaObject.getPlayers().contains(player.getName())) {
					tempArena = arenaObject;
				}
			}
			Arena arena = tempArena;
			if (arena != null) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			Arena tempArena = null;
			for (Arena arenaObject : Arena.arenaObjects) {
				if (arenaObject.getPlayers().contains(player.getName())) {
					tempArena = arenaObject;
				}
			}
			Arena arena = tempArena;
			if (arena != null) {
				e.setFoodLevel(20);
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
		final Player player = e.getPlayer();
		Arena tempArena = null;
		for (Arena arenaObject : Arena.arenaObjects) {
			if (arenaObject.getPlayers().contains(player.getName())) {
				tempArena = arenaObject;
			}
		}
		Arena arena = tempArena;
		if (arena != null) {
			if (arena.getState() == ArenaState.BUILDING) {
				if (!arena.getPlayersDoubleJumpCooldowned().containsKey(player.getName())) {
					if (arena.getPlots().containsKey(player.getName())) {
						e.setCancelled(true);

						player.setVelocity(new Vector(0, 1, 0).multiply(1.05));
						player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);

						arena.getPlayersDoubleJumpCooldowned().put(player.getName(), 1.5F);
						plugin.getMultiWorld().getTimerManager().cooldownTimer(arena.getName(), player.getName());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		InventoryView inventoryView = e.getView();
		ItemStack itemStack1 = e.getCurrentItem();
		Arena tempArena = null;
		for (Arena arenaObject : Arena.arenaObjects) {
			if (arenaObject.getPlayers().contains(player.getName())) {
				tempArena = arenaObject;
			}
		}
		Arena arena = tempArena;
		if (arena != null) {
			if (itemStack1 != null) {
				if (inventoryView.getTitle().startsWith(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY")))) {
					e.setCancelled(true);
					player.updateInventory();
				} else {
					if (player.hasPermission("sb.command.setup")) {
						if (arena.getState() != ArenaState.WAITING && arena.getState() != ArenaState.BUILDING) {
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
						if (arena.getState() != ArenaState.BUILDING) {
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
		} else if (inventoryView.getTitle().startsWith(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-INVENTORY")))) {
			if (itemStack1 != null) {
				e.setCancelled(true);
				player.updateInventory();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		Player player = e.getPlayer();
		ItemStack itemStack1 = e.getItemDrop().getItemStack();
		Arena tempArena = null;
		for (Arena arenaObject : Arena.arenaObjects) {
			if (arenaObject.getPlayers().contains(player.getName())) {
				tempArena = arenaObject;
			}
		}
		Arena arena = tempArena;
		if (arena != null) {
			if (itemStack1 != null) {
				if (player.hasPermission("sb.command.setup")) {
					if (arena.getState() != ArenaState.WAITING) {
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
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		Arena tempArena = null;
		for (Arena arenaObject : Arena.arenaObjects) {
			if (arenaObject.getPlayers().contains(player.getName())) {
				tempArena = arenaObject;
			}
		}
		Arena arena = tempArena;
		if (arena != null) {
			e.getBlock().getRelative(BlockFace.UP).getState().update();
			e.getBlock().getState().update();
			e.getBlock().getRelative(BlockFace.DOWN).getState().update();
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player player = e.getPlayer();
		Arena tempArena = null;
		for (Arena arenaObject : Arena.arenaObjects) {
			if (arenaObject.getPlayers().contains(player.getName())) {
				tempArena = arenaObject;
			}
		}
		Arena arena = tempArena;
		if (arena != null) {
			if (arena.getState() == ArenaState.BUILDING) {
				Location location1 = e.getBlock().getLocation();
				Location location2 = new Location(Bukkit.getWorld(arena.getName()), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.x1"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.y1"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.z1"));
				Location location3 = new Location(Bukkit.getWorld(arena.getName()), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.x2"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.y2"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots." + arena.getPlots().get(player.getName()) + ".build-area.z2"));
				if (isBlockInside(location1, location2, location3)) {
					new BukkitRunnable() {
						public void run() {
							plugin.getMultiWorld().getTemplateManager().check(arena.getPlots().get(player.getName()), player, arena.getName());
						}
					}.runTaskLater(plugin, 1L);
				} else {
					e.setCancelled(true);
				}
			} else {
				e.getBlock().getRelative(BlockFace.UP).getState().update();
				e.getBlock().getState().update();
				e.getBlock().getRelative(BlockFace.DOWN).getState().update();
				e.setCancelled(true);
			}
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
