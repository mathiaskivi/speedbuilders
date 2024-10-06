package ee.mathiaskivi.speedbuilders.multiworld.manager;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.multiworld.Arena;
import ee.mathiaskivi.speedbuilders.utility.GameState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

public class SignManager implements Listener {
    private final SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        if (e.getBlock().getState() instanceof org.bukkit.block.Sign) {
            org.bukkit.block.Sign blockState = (org.bukkit.block.Sign) e.getBlock().getState();

            if (blockState.getLine(0).equals(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-SIGN")))) {
                if (player.hasPermission("sb.command.setup")) {
                    if (plugin.getConfigManager().getConfig("signs.yml").contains("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ())) {
                        if (blockState.getBlockData() instanceof org.bukkit.block.data.type.Sign) {
                            Block relativeBlock = e.getBlock().getRelative(BlockFace.DOWN);
                            Material previousType = Material.getMaterial(plugin.getConfigManager().getConfig("signs.yml").getString("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".previous-type"));

                            relativeBlock.setType(previousType);

                            plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ(), null);
                            new BukkitRunnable() {
                                public void run() {
                                    plugin.getConfigManager().saveConfig("signs.yml");
                                }
                            }.runTaskAsynchronously(plugin);

                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7SpeedBuilders sign is successfully removed!"));
                        } else if (blockState.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                            org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) blockState.getBlockData();

                            Block relativeBlock = e.getBlock().getRelative(wallSign.getFacing().getOppositeFace());
                            Material previousType = Material.getMaterial(plugin.getConfigManager().getConfig("signs.yml").getString("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".previous-type"));

                            relativeBlock.setType(previousType);

                            plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ(), null);
                            new BukkitRunnable() {
                                public void run() {
                                    plugin.getConfigManager().saveConfig("signs.yml");
                                }
                            }.runTaskAsynchronously(plugin);

                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7SpeedBuilders sign is successfully removed!"));
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
                    e.setCancelled(true);
                }
            }
        } else {
            Block block = e.getBlock();

            if (block.getRelative(BlockFace.UP).getState().getBlockData() instanceof org.bukkit.block.data.type.Sign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getRelative(BlockFace.UP).getState();

                if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-SIGN")))) {
                    e.setCancelled(true);
                }
            } else if (block.getRelative(BlockFace.NORTH).getState().getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getRelative(BlockFace.NORTH).getState();
                org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) sign.getBlockData();

                if (wallSign.getFacing().getOppositeFace() == block.getRelative(BlockFace.NORTH).getFace(block)) {
                    if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-SIGN")))) {
                        e.setCancelled(true);
                    }
                }
            } else if (block.getRelative(BlockFace.SOUTH).getState().getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getRelative(BlockFace.SOUTH).getState();
                org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) sign.getBlockData();

                if (wallSign.getFacing().getOppositeFace() == block.getRelative(BlockFace.SOUTH).getFace(block)) {
                    if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-SIGN")))) {
                        e.setCancelled(true);
                    }
                }
            } else if (block.getRelative(BlockFace.EAST).getState().getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getRelative(BlockFace.EAST).getState();
                org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) sign.getBlockData();

                if (wallSign.getFacing().getOppositeFace() == block.getRelative(BlockFace.EAST).getFace(block)) {
                    if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-SIGN")))) {
                        e.setCancelled(true);
                    }
                }
            } else if (block.getRelative(BlockFace.WEST).getState().getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getRelative(BlockFace.WEST).getState();
                org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) sign.getBlockData();

                if (wallSign.getFacing().getOppositeFace() == block.getRelative(BlockFace.WEST).getFace(block)) {
                    if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-SIGN")))) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent e) {
        Player player = e.getPlayer();
        if (e.getLine(0).equalsIgnoreCase("[SB]") || e.getLine(0).equalsIgnoreCase("[SpeedBuilders]")) {
            if (player.hasPermission("sb.command.setup")) {
                Block block = e.getBlock();
                org.bukkit.block.Sign blockState = (org.bukkit.block.Sign) block.getState();

                e.setLine(0, ChatColor.translateAlternateColorCodes('&', translate("PREFIX-SIGN")));
                if (e.getLine(1).equalsIgnoreCase("[AutoJoin]")) {
                    if (blockState.getBlockData() instanceof org.bukkit.block.data.type.Sign) {
                        Block relativeBlock = blockState.getBlock().getRelative(BlockFace.DOWN);

                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".world", blockState.getWorld().getName());
                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".previous-type", relativeBlock.getType().toString());
                        new BukkitRunnable() {
                            public void run() {
                                plugin.getConfigManager().saveConfig("signs.yml");
                            }
                        }.runTaskAsynchronously(plugin);

                        e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&4[AutoJoin]"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7SpeedBuilders sign is successfully created!"));
                    } else if (blockState.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                        org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) blockState.getBlockData();
                        Block relativeBlock = blockState.getBlock().getRelative(wallSign.getFacing().getOppositeFace());

                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".world", blockState.getWorld().getName());
                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".previous-type", relativeBlock.getType().toString());
                        new BukkitRunnable() {
                            public void run() {
                                plugin.getConfigManager().saveConfig("signs.yml");
                            }
                        }.runTaskAsynchronously(plugin);

                        e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&4[AutoJoin]"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7SpeedBuilders sign is successfully created!"));
                    }
                } else if (e.getLine(1).equalsIgnoreCase("[Leave]")) {
                    if (blockState.getBlockData() instanceof org.bukkit.block.data.type.Sign) {
                        Block relativeBlock = blockState.getBlock().getRelative(BlockFace.DOWN);

                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".world", blockState.getWorld().getName());
                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".previous-type", relativeBlock.getType().toString());
                        new BukkitRunnable() {
                            public void run() {
                                plugin.getConfigManager().saveConfig("signs.yml");
                            }
                        }.runTaskAsynchronously(plugin);

                        e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&5[Leave]"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7SpeedBuilders sign is successfully created!"));
                    } else if (blockState.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                        org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) blockState.getBlockData();
                        Block relativeBlock = blockState.getBlock().getRelative(wallSign.getFacing().getOppositeFace());

                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".world", blockState.getWorld().getName());
                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".previous-type", relativeBlock.getType().toString());
                        new BukkitRunnable() {
                            public void run() {
                                plugin.getConfigManager().saveConfig("signs.yml");
                            }
                        }.runTaskAsynchronously(plugin);

                        e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&5[Leave]"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7SpeedBuilders sign is successfully created!"));
                    }
                } else if (e.getLine(1).equalsIgnoreCase("[Stats]")) {
                    if (blockState.getBlockData() instanceof org.bukkit.block.data.type.Sign) {
                        Block relativeBlock = blockState.getBlock().getRelative(BlockFace.DOWN);

                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".world", blockState.getWorld().getName());
                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".previous-type", relativeBlock.getType().toString());
                        new BukkitRunnable() {
                            public void run() {
                                plugin.getConfigManager().saveConfig("signs.yml");
                            }
                        }.runTaskAsynchronously(plugin);

                        e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&2[Stats]"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7SpeedBuilders sign is successfully created!"));
                    } else if (blockState.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                        org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) blockState.getBlockData();
                        Block relativeBlock = blockState.getBlock().getRelative(wallSign.getFacing().getOppositeFace());

                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".world", blockState.getWorld().getName());
                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".previous-type", relativeBlock.getType().toString());
                        new BukkitRunnable() {
                            public void run() {
                                plugin.getConfigManager().saveConfig("signs.yml");
                            }
                        }.runTaskAsynchronously(plugin);

                        e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&2[Stats]"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7SpeedBuilders sign is successfully created!"));
                    }
                } else if (Arena.arenaObjects.contains(plugin.getMultiWorld().getArenaManager().getArena(e.getLine(1)))) {
                    if (blockState.getBlockData() instanceof org.bukkit.block.data.type.Sign) {
                        Block relativeBlock = blockState.getBlock().getRelative(BlockFace.DOWN);

                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".arena", e.getLine(1));
                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".world", blockState.getWorld().getName());
                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".previous-type", relativeBlock.getType().toString());
                        new BukkitRunnable() {
                            public void run() {
                                plugin.getConfigManager().saveConfig("signs.yml");
                            }
                        }.runTaskAsynchronously(plugin);

                        Arena arena = plugin.getMultiWorld().getArenaManager().getArena(e.getLine(1));
                        e.setLine(1, arena.getName());
                        if (arena.getGameState() == GameState.WAITING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-WAITING")));

                            relativeBlock.setType(Material.LIME_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.STARTING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-STARTING")));

                            relativeBlock.setType(Material.ORANGE_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.GAME_STARTING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-GAME_STARTING")));

                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.SHOWCASING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-SHOWCASING")));

                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.BUILDING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-BUILDING")));

                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.JUDGING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-JUDGING")));

                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        }
                        e.setLine(3, arena.getPlayers().size() + "/" + arena.getMaxPlayers());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7SpeedBuilders sign is successfully created!"));
                    } else if (blockState.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                        org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) blockState.getBlockData();
                        Block relativeBlock = blockState.getBlock().getRelative(wallSign.getFacing().getOppositeFace());

                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".arena", e.getLine(1));
                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".world", blockState.getWorld().getName());
                        plugin.getConfigManager().getConfig("signs.yml").set("signs." + blockState.getBlock().getX() + "," + blockState.getBlock().getY() + "," + blockState.getBlock().getZ() + ".previous-type", relativeBlock.getType().toString());
                        new BukkitRunnable() {
                            public void run() {
                                plugin.getConfigManager().saveConfig("signs.yml");
                            }
                        }.runTaskAsynchronously(plugin);

                        Arena arena = plugin.getMultiWorld().getArenaManager().getArena(e.getLine(1));
                        e.setLine(1, arena.getName());
                        if (arena.getGameState() == GameState.WAITING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-WAITING")));

                            relativeBlock.setType(Material.LIME_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.STARTING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-STARTING")));

                            relativeBlock.setType(Material.ORANGE_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.GAME_STARTING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-GAME_STARTING")));

                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.SHOWCASING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-SHOWCASING")));

                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.BUILDING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-BUILDING")));

                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.JUDGING) {
                            e.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-JUDGING")));

                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        }
                        e.setLine(3, arena.getPlayers().size() + "/" + arena.getMaxPlayers());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&7SpeedBuilders sign is successfully created!"));
                    }
                } else {
                    e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&cArena does"));
                    e.setLine(2, ChatColor.translateAlternateColorCodes('&', "&cnot exist"));
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_PERMISSION")));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getState() instanceof org.bukkit.block.Sign) {
                org.bukkit.block.Sign blockState = (org.bukkit.block.Sign) e.getClickedBlock().getState();

                if (blockState.getLine(0).equals(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-SIGN")))) {
                    if (blockState.getLine(1).equals(ChatColor.translateAlternateColorCodes('&', "&4[AutoJoin]"))) {
                        if (!plugin.getMultiWorld().playersSignCooldowned.contains(player.getName())) {
                            plugin.getMultiWorld().playersSignCooldowned.add(player.getName());
                            boolean found = false;
                            for (Arena arena : Arena.arenaObjects) {
                                if (arena.getPlayers().contains(player.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                boolean foundJoinableArena = false;
                                for (Arena arena : Arena.arenaObjects) {
                                    String[] folders = new File(".").list(new FilenameFilter() {
                                        public boolean accept(File current, String name) {
                                            return new File(current, name).isDirectory();
                                        }
                                    });

                                    found = false;
                                    for (String folder : folders) {
                                        if (folder.equals(arena.getName())) {
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (found) {
                                        if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) {
                                            if (!(arena.getPlayers().size() >= arena.getMaxPlayers())) {
                                                foundJoinableArena = true;
                                                plugin.getMultiWorld().saveTempInfo(player);
                                                plugin.getMultiWorld().getArenaManager().addPlayer(player, arena.getName());
                                                plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                                                break;
                                            } else if (player.hasPermission("sb.server.joinfullgame")) {
                                                plugin.getMultiWorld().saveTempInfo(player);
                                                plugin.getMultiWorld().getArenaManager().addPlayer(player, arena.getName());

                                                List<Player> noPermUsers = new ArrayList<>();
                                                for (String arenaPlayer : arena.getPlayers()) {
                                                    if (Bukkit.getPlayer(arenaPlayer) != player && !Bukkit.getPlayer(arenaPlayer).hasPermission("sb.server.joinfullgame")) {
                                                        noPermUsers.add(Bukkit.getPlayer(arenaPlayer));
                                                    }
                                                }

                                                if (!noPermUsers.isEmpty()) {
                                                    Player kickPlayer = noPermUsers.get(new Random().nextInt(noPermUsers.size()));
                                                    arena.getPlayers().remove(kickPlayer.getName());

                                                    if (arena.getGameState() == GameState.WAITING) {
                                                        for (String arenaPlayer : arena.getPlayers()) {
                                                            if (arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName()) != null) {
                                                                Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName());
                                                                Objective objective = scoreboard.getObjective("SpeedBuilders");
                                                                for (String entry : scoreboard.getEntries()) {
                                                                    scoreboard.resetScores(entry);
                                                                }
                                                                if (arena.getGameState() == GameState.WAITING) {
                                                                    objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
                                                                }
                                                                if (arena.getGameState() == GameState.STARTING) {
                                                                    objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
                                                                }
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
                                                                Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
                                                            } else {
                                                                ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
                                                                Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
                                                                Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
                                                                if (arena.getGameState() == GameState.WAITING) {
                                                                    objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
                                                                }
                                                                if (arena.getGameState() == GameState.STARTING) {
                                                                    objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
                                                                }
                                                                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
                                                                Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
                                                                arena.getPlayerStartScoreboard().put(Bukkit.getPlayer(arenaPlayer).getName(), scoreboard);
                                                            }
                                                        }

                                                        arena.getGameScoreboard().getPlayerTeam(kickPlayer).removePlayer(kickPlayer);
                                                        plugin.getMultiWorld().getKitManager().setKit(kickPlayer, null, arena.getName());

                                                        if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
                                                            Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
                                                            location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
                                                            location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
                                                            kickPlayer.teleport(location);
                                                        } else {
                                                            kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
                                                        }

                                                        kickPlayer.getInventory().setArmorContents(null);
                                                        kickPlayer.getInventory().clear();
                                                        kickPlayer.setAllowFlight(false);
                                                        kickPlayer.setExp(0);
                                                        kickPlayer.setFireTicks(0);
                                                        kickPlayer.setFlying(false);
                                                        kickPlayer.setFoodLevel(20);
                                                        kickPlayer.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
                                                        kickPlayer.setHealth(20);
                                                        kickPlayer.setLevel(0);
                                                        for (PotionEffect potionEffect : kickPlayer.getActivePotionEffects()) {
                                                            kickPlayer.removePotionEffect(potionEffect.getType());
                                                        }

                                                        if (plugin.getMultiWorld().playerTempHealth.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(kickPlayer.getName())) {
                                                            plugin.getMultiWorld().loadTempInfo(kickPlayer);
                                                        }
                                                        kickPlayer.updateInventory();
                                                    } else if (arena.getGameState() == GameState.STARTING) {
                                                        for (String arenaPlayer : arena.getPlayers()) {
                                                            if (arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName()) != null) {
                                                                Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName());
                                                                Objective objective = scoreboard.getObjective("SpeedBuilders");
                                                                for (String entry : scoreboard.getEntries()) {
                                                                    scoreboard.resetScores(entry);
                                                                }
                                                                if (arena.getGameState() == GameState.WAITING) {
                                                                    objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
                                                                }
                                                                if (arena.getGameState() == GameState.STARTING) {
                                                                    objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
                                                                }
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
                                                                Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
                                                            } else {
                                                                ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
                                                                Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
                                                                Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
                                                                if (arena.getGameState() == GameState.WAITING) {
                                                                    objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
                                                                }
                                                                if (arena.getGameState() == GameState.STARTING) {
                                                                    objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
                                                                }
                                                                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
                                                                objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
                                                                Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
                                                                arena.getPlayerStartScoreboard().put(Bukkit.getPlayer(arenaPlayer).getName(), scoreboard);
                                                            }
                                                        }

                                                        arena.getGameScoreboard().getPlayerTeam(kickPlayer).removePlayer(kickPlayer);
                                                        plugin.getMultiWorld().getKitManager().setKit(kickPlayer, null, arena.getName());

                                                        if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
                                                            Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
                                                            location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
                                                            location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
                                                            kickPlayer.teleport(location);
                                                        } else {
                                                            kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
                                                        }

                                                        kickPlayer.getInventory().setArmorContents(null);
                                                        kickPlayer.getInventory().clear();
                                                        kickPlayer.setAllowFlight(false);
                                                        kickPlayer.setExp(0);
                                                        kickPlayer.setFireTicks(0);
                                                        kickPlayer.setFlying(false);
                                                        kickPlayer.setFoodLevel(20);
                                                        kickPlayer.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
                                                        kickPlayer.setHealth(20);
                                                        kickPlayer.setLevel(0);
                                                        for (PotionEffect potionEffect : kickPlayer.getActivePotionEffects()) {
                                                            kickPlayer.removePotionEffect(potionEffect.getType());
                                                        }

                                                        if (plugin.getMultiWorld().playerTempHealth.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(kickPlayer.getName())) {
                                                            plugin.getMultiWorld().loadTempInfo(kickPlayer);
                                                        }
                                                        kickPlayer.updateInventory();
                                                    }
                                                }
                                                noPermUsers.clear();
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!foundJoinableArena) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_JOINABLE_ARENAS")));
                                    plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                                } else {
                                    plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                                }
                            } else {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYING")));
                                plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                            }
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-WAIT_BEFORE_CLICKING_AGAIN")));
                        }
                    } else if (blockState.getLine(1).equals(ChatColor.translateAlternateColorCodes('&', "&5[Leave]"))) {
                        if (!plugin.getMultiWorld().playersSignCooldowned.contains(player.getName())) {
                            plugin.getMultiWorld().playersSignCooldowned.add(player.getName());
                            boolean found = false;
                            for (Arena arena : Arena.arenaObjects) {
                                if (arena.getPlayers().contains(player.getName())) {
                                    found = true;
                                    plugin.getMultiWorld().getArenaManager().removePlayer(player, arena.getName());
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("MAIN-PLAYER_QUIT").replaceAll("%PLAYER%", player.getName())));
                                    plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                                    break;
                                }
                            }
                            if (!found) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NOT_PLAYING")));
                                plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                            }
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-WAIT_BEFORE_CLICKING_AGAIN")));
                        }
                    } else if (blockState.getLine(1).equals(ChatColor.translateAlternateColorCodes('&', "&2[Stats]"))) {
                        if (!plugin.getMultiWorld().playersSignCooldowned.contains(player.getName())) {
                            plugin.getStatsManager().showStats(player, player);
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-WAIT_BEFORE_CLICKING_AGAIN")));
                        }
                    } else if (Arena.arenaObjects.contains(plugin.getMultiWorld().getArenaManager().getArena(blockState.getLine(1)))) {
                        if (!plugin.getMultiWorld().playersSignCooldowned.contains(player.getName())) {
                            plugin.getMultiWorld().playersSignCooldowned.add(player.getName());
                            boolean found = false;
                            for (Arena arenaObject : Arena.arenaObjects) {
                                if (arenaObject.getPlayers().contains(player.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                Arena arena = plugin.getMultiWorld().getArenaManager().getArena(blockState.getLine(1));

                                String[] folders = new File(".").list(new FilenameFilter() {
                                    public boolean accept(File current, String name) {
                                        return new File(current, name).isDirectory();
                                    }
                                });

                                found = false;
                                for (String folder : folders) {
                                    if (folder.equals(arena.getName())) {
                                        found = true;
                                        break;
                                    }
                                }

                                if (found) {
                                    if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) {
                                        if (!(arena.getPlayers().size() >= arena.getMaxPlayers())) {
                                            plugin.getMultiWorld().saveTempInfo(player);

                                            plugin.getMultiWorld().getArenaManager().addPlayer(player, arena.getName());
                                            plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                                        } else if (player.hasPermission("sb.server.joinfullgame")) {
                                            plugin.getMultiWorld().saveTempInfo(player);
                                            plugin.getMultiWorld().getArenaManager().addPlayer(player, arena.getName());

                                            List<Player> noPermUsers = new ArrayList<>();
                                            for (String arenaPlayer : arena.getPlayers()) {
                                                if (Bukkit.getPlayer(arenaPlayer) != player && !Bukkit.getPlayer(arenaPlayer).hasPermission("sb.server.joinfullgame")) {
                                                    noPermUsers.add(Bukkit.getPlayer(arenaPlayer));
                                                }
                                            }

                                            if (!noPermUsers.isEmpty()) {
                                                Player kickPlayer = noPermUsers.get(new Random().nextInt(noPermUsers.size()));
                                                arena.getPlayers().remove(kickPlayer.getName());

                                                if (arena.getGameState() == GameState.WAITING) {
                                                    for (String arenaPlayer : arena.getPlayers()) {
                                                        if (arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName()) != null) {
                                                            Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName());
                                                            Objective objective = scoreboard.getObjective("SpeedBuilders");
                                                            for (String entry : scoreboard.getEntries()) {
                                                                scoreboard.resetScores(entry);
                                                            }
                                                            if (arena.getGameState() == GameState.WAITING) {
                                                                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
                                                            }
                                                            if (arena.getGameState() == GameState.STARTING) {
                                                                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
                                                            }
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
                                                            Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
                                                        } else {
                                                            ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
                                                            Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
                                                            Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
                                                            if (arena.getGameState() == GameState.WAITING) {
                                                                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
                                                            }
                                                            if (arena.getGameState() == GameState.STARTING) {
                                                                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
                                                            }
                                                            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
                                                            Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
                                                            arena.getPlayerStartScoreboard().put(Bukkit.getPlayer(arenaPlayer).getName(), scoreboard);
                                                        }
                                                    }

                                                    arena.getGameScoreboard().getPlayerTeam(kickPlayer).removePlayer(kickPlayer);
                                                    plugin.getMultiWorld().getKitManager().setKit(kickPlayer, null, arena.getName());

                                                    if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
                                                        Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
                                                        location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
                                                        location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
                                                        kickPlayer.teleport(location);
                                                    } else {
                                                        kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
                                                    }

                                                    kickPlayer.getInventory().setArmorContents(null);
                                                    kickPlayer.getInventory().clear();
                                                    kickPlayer.setAllowFlight(false);
                                                    kickPlayer.setExp(0);
                                                    kickPlayer.setFireTicks(0);
                                                    kickPlayer.setFlying(false);
                                                    kickPlayer.setFoodLevel(20);
                                                    kickPlayer.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
                                                    kickPlayer.setHealth(20);
                                                    kickPlayer.setLevel(0);
                                                    for (PotionEffect potionEffect : kickPlayer.getActivePotionEffects()) {
                                                        kickPlayer.removePotionEffect(potionEffect.getType());
                                                    }

                                                    if (plugin.getMultiWorld().playerTempHealth.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(kickPlayer.getName())) {
                                                        plugin.getMultiWorld().loadTempInfo(kickPlayer);
                                                    }
                                                    kickPlayer.updateInventory();
                                                } else if (arena.getGameState() == GameState.STARTING) {
                                                    for (String arenaPlayer : arena.getPlayers()) {
                                                        if (arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName()) != null) {
                                                            Scoreboard scoreboard = arena.getPlayerStartScoreboard().get(Bukkit.getPlayer(arenaPlayer).getName());
                                                            Objective objective = scoreboard.getObjective("SpeedBuilders");
                                                            for (String entry : scoreboard.getEntries()) {
                                                                scoreboard.resetScores(entry);
                                                            }
                                                            if (arena.getGameState() == GameState.WAITING) {
                                                                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
                                                            }
                                                            if (arena.getGameState() == GameState.STARTING) {
                                                                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
                                                            }
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
                                                            Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
                                                        } else {
                                                            ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
                                                            Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
                                                            Objective objective = scoreboard.registerNewObjective("SpeedBuilders", "dummy");
                                                            if (arena.getGameState() == GameState.WAITING) {
                                                                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-WAITING_FOR_PLAYERS")));
                                                            }
                                                            if (arena.getGameState() == GameState.STARTING) {
                                                                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-STARTING_IN").replaceAll("%TIME%", plugin.getMultiWorld().getTimerManager().timeString(arena.getStartTime()))));
                                                            }
                                                            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&1"), scoreboard)).setScore(6);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-PLAYERS")), scoreboard)).setScore(5);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', arena.getPlayers().size() + "/" + arena.getMaxPlayers()), scoreboard)).setScore(4);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', "&2"), scoreboard)).setScore(3);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("SBOARD-KIT")), scoreboard)).setScore(2);
                                                            objective.getScore(plugin.getMultiWorld().scoreboardScore(ChatColor.translateAlternateColorCodes('&', translate("KITS-" + plugin.getMultiWorld().getKitManager().getKit(Bukkit.getPlayer(arenaPlayer), arena.getName()).toUpperCase())), scoreboard)).setScore(1);
                                                            Bukkit.getPlayer(arenaPlayer).setScoreboard(scoreboard);
                                                            arena.getPlayerStartScoreboard().put(Bukkit.getPlayer(arenaPlayer).getName(), scoreboard);
                                                        }
                                                    }

                                                    arena.getGameScoreboard().getPlayerTeam(kickPlayer).removePlayer(kickPlayer);
                                                    plugin.getMultiWorld().getKitManager().setKit(kickPlayer, null, arena.getName());

                                                    if (plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.world") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.x") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.y") && plugin.getConfigManager().getConfig("lobby.yml").contains("lobby.spawn.z")) {
                                                        Location location = new Location(Bukkit.getWorld(plugin.getConfigManager().getConfig("lobby.yml").getString("lobby.spawn.world")), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.x"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.y"), plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.z"));
                                                        location.setPitch((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.pitch"));
                                                        location.setYaw((float) plugin.getConfigManager().getConfig("lobby.yml").getDouble("lobby.spawn.yaw"));
                                                        kickPlayer.teleport(location);
                                                    } else {
                                                        kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-NO_LOBBY_SPAWNPOINT")));
                                                    }

                                                    kickPlayer.getInventory().setArmorContents(null);
                                                    kickPlayer.getInventory().clear();
                                                    kickPlayer.setAllowFlight(false);
                                                    kickPlayer.setExp(0);
                                                    kickPlayer.setFireTicks(0);
                                                    kickPlayer.setFlying(false);
                                                    kickPlayer.setFoodLevel(20);
                                                    kickPlayer.setGameMode(GameMode.valueOf(plugin.getConfigManager().getConfig("config.yml").getString("gamemode").toUpperCase()));
                                                    kickPlayer.setHealth(20);
                                                    kickPlayer.setLevel(0);
                                                    for (PotionEffect potionEffect : kickPlayer.getActivePotionEffects()) {
                                                        kickPlayer.removePotionEffect(potionEffect.getType());
                                                    }

                                                    if (plugin.getMultiWorld().playerTempHealth.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempFoodLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempExp.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempLevel.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempArmor.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempItems.containsKey(kickPlayer.getName()) && plugin.getMultiWorld().playerTempEffects.containsKey(kickPlayer.getName())) {
                                                        plugin.getMultiWorld().loadTempInfo(kickPlayer);
                                                    }
                                                    kickPlayer.updateInventory();
                                                }
                                            }
                                            noPermUsers.clear();
                                        } else {
                                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-ARENA_IS_FULL")));
                                            plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-GAME_RUNNING")));
                                        plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                                    }
                                } else {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&cWorld folder can not be found for that arena!"));
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + "&cShut down the server and upload the world to this server!"));
                                    plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                                }
                            } else {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-PLAYING")));
                                plugin.getMultiWorld().playersSignCooldowned.remove(player.getName());
                            }
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate("PREFIX-CHAT") + translate("ERROR-WAIT_BEFORE_CLICKING_AGAIN")));
                        }
                    }
                }
            }
        }
    }

    public void updateSigns(String arenaName) {
        for (String signSection : plugin.getConfigManager().getConfig("signs.yml").getConfigurationSection("signs").getKeys(false)) {
            String[] signCoordinates = signSection.split(",");

            if (plugin.getConfigManager().getConfig("signs.yml").contains("signs." + signCoordinates[0] + "," + signCoordinates[1] + "," + signCoordinates[2])) {
                if (plugin.getConfigManager().getConfig("signs.yml").getString("signs." + signCoordinates[0] + "," + signCoordinates[1] + "," + signCoordinates[2] + ".arena").equals(arenaName)) {
                    Arena arena = plugin.getMultiWorld().getArenaManager().getArena(plugin.getConfigManager().getConfig("signs.yml").getString("signs." + signCoordinates[0] + "," + signCoordinates[1] + "," + signCoordinates[2] + ".arena"));

                    World world = Bukkit.getWorld(plugin.getConfigManager().getConfig("signs.yml").getString("signs." + signCoordinates[0] + "," + signCoordinates[1] + "," + signCoordinates[2] + ".world"));
                    Location location = new Location(world, Integer.parseInt(signCoordinates[0]), Integer.parseInt(signCoordinates[1]), Integer.parseInt(signCoordinates[2]));

                    location.getChunk().load();
                    Block block = world.getBlockAt(location);
                    BlockState blockState = block.getState();
                    if (blockState.getBlockData() instanceof org.bukkit.block.data.type.Sign) {
                        org.bukkit.block.Sign sign = (org.bukkit.block.Sign) blockState;

                        sign.setLine(0, ChatColor.translateAlternateColorCodes('&', translate("PREFIX-SIGN")));
                        sign.setLine(1, arena.getName());
                        if (arena.getGameState() == GameState.WAITING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-WAITING")));

                            Block relativeBlock = block.getRelative(BlockFace.DOWN);
                            relativeBlock.setType(Material.LIME_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.STARTING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-STARTING")));

                            Block relativeBlock = block.getRelative(BlockFace.DOWN);
                            relativeBlock.setType(Material.ORANGE_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.GAME_STARTING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-GAME_STARTING")));

                            Block relativeBlock = block.getRelative(BlockFace.DOWN);
                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.SHOWCASING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-SHOWCASING")));

                            Block relativeBlock = block.getRelative(BlockFace.DOWN);
                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.BUILDING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-BUILDING")));

                            BlockState relativeBlock = block.getRelative(BlockFace.DOWN).getState();
                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.JUDGING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-JUDGING")));

                            Block relativeBlock = block.getRelative(BlockFace.DOWN);
                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        }
                        sign.setLine(3, arena.getPlayers().size() + "/" + arena.getMaxPlayers());
                        sign.update(true, false);
                    } else if (blockState.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                        org.bukkit.block.Sign sign = (org.bukkit.block.Sign) blockState;
                        org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) blockState.getBlockData();

                        sign.setLine(0, ChatColor.translateAlternateColorCodes('&', translate("PREFIX-SIGN")));
                        sign.setLine(1, arena.getName());
                        if (arena.getGameState() == GameState.WAITING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-WAITING")));

                            Block relativeBlock = block.getRelative(wallSign.getFacing().getOppositeFace());
                            relativeBlock.setType(Material.LIME_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.STARTING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-STARTING")));

                            Block relativeBlock = block.getRelative(wallSign.getFacing().getOppositeFace());
                            relativeBlock.setType(Material.ORANGE_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.GAME_STARTING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-GAME_STARTING")));

                            Block relativeBlock = block.getRelative(wallSign.getFacing().getOppositeFace());
                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.SHOWCASING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-SHOWCASING")));

                            Block relativeBlock = block.getRelative(wallSign.getFacing().getOppositeFace());
                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.BUILDING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-BUILDING")));

                            Block relativeBlock = block.getRelative(wallSign.getFacing().getOppositeFace());
                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        } else if (arena.getGameState() == GameState.JUDGING) {
                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', translate("GAMESTATE-JUDGING")));

                            Block relativeBlock = block.getRelative(wallSign.getFacing().getOppositeFace());
                            relativeBlock.setType(Material.RED_TERRACOTTA);
                        }
                        sign.setLine(3, arena.getPlayers().size() + "/" + arena.getMaxPlayers());
                        sign.update(true, false);
                    }
                }
            }
        }
    }
}
