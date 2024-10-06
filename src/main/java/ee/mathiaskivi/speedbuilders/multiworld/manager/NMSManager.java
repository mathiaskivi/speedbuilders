package ee.mathiaskivi.speedbuilders.multiworld.manager;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class NMSManager {
	public SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public void showActionBar(Player player, String message) {
		BaseComponent[] baseComponent = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', message)).create();
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, baseComponent);
	}

	public void setPlayerVisibility(Player player, Player target, boolean visible) {
		if (visible) {
			if (player.hasMetadata("invisible")) {
				if (target != null) {
					target.showPlayer(plugin, player);
					
					player.removeMetadata("invisible", plugin);
				} else {
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						if (!onlinePlayer.equals(player)) {
							onlinePlayer.showPlayer(plugin, player);
						}
					}
					
					player.removeMetadata("invisible", plugin);
				}
			}
		} else {
			if (target != null) {
				target.hidePlayer(plugin, player);
				
				player.setMetadata("invisible", new FixedMetadataValue(plugin, "true"));
			} else {
				for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
					if (!onlinePlayer.equals(player)) {
						onlinePlayer.hidePlayer(plugin, player);
					}
				}

				player.setMetadata("invisible", new FixedMetadataValue(plugin, "true"));
			}
		}
	}

	public void updateBlockConnections(Block block) {
		if (block.getRelative(BlockFace.DOWN).getBlockData() instanceof Bisected) {
			if (block.getRelative(BlockFace.DOWN).getBlockData() instanceof Stairs) {
				return;
			}

			Bisected bisected = (Bisected) block.getRelative(BlockFace.DOWN).getState().getBlockData();

			if (bisected.getHalf() == Half.BOTTOM) {
				BlockState blockState = block.getRelative(BlockFace.DOWN).getState();
				blockState.setType(Material.AIR);
				blockState.update(true, false);
			}
		}

		if (block.getRelative(BlockFace.UP).getBlockData() instanceof Bisected) {
			if (block.getRelative(BlockFace.UP).getBlockData() instanceof Stairs) {
				return;
			}

			Bisected bisected = (Bisected) block.getRelative(BlockFace.UP).getState().getBlockData();

			if (bisected.getHalf() == Half.TOP) {
				BlockState blockState = block.getRelative(BlockFace.UP).getState();
				blockState.setType(Material.AIR);
				blockState.update(true, false);
			}
		}

		if (block.getRelative(BlockFace.EAST).getState().getBlockData() instanceof Bed) {
			Bed bed = (Bed) block.getRelative(BlockFace.EAST).getState().getBlockData();

			if (bed.getFacing() == BlockFace.EAST) {
				if (bed.getPart() == Bed.Part.HEAD) {
					BlockState blockState = block.getRelative(BlockFace.EAST).getState();
					blockState.setType(Material.AIR);
					blockState.update(true, false);
				}
			} else if (bed.getFacing() == BlockFace.EAST.getOppositeFace()) {
				if (bed.getPart() == Bed.Part.FOOT) {
					BlockState blockState = block.getRelative(BlockFace.EAST).getState();
					blockState.setType(Material.AIR);
					blockState.update(true, false);
				}
			}
		}

		if (block.getRelative(BlockFace.NORTH).getState().getBlockData() instanceof Bed) {
			Bed bed = (Bed) block.getRelative(BlockFace.NORTH).getState().getBlockData();

			if (bed.getFacing() == BlockFace.NORTH) {
				if (bed.getPart() == Bed.Part.HEAD) {
					BlockState blockState = block.getRelative(BlockFace.NORTH).getState();
					blockState.setType(Material.AIR);
					blockState.update(true, false);
				}
			} else if (bed.getFacing() == BlockFace.NORTH.getOppositeFace()) {
				if (bed.getPart() == Bed.Part.FOOT) {
					BlockState blockState = block.getRelative(BlockFace.NORTH).getState();
					blockState.setType(Material.AIR);
					blockState.update(true, false);
				}
			}
		}

		if (block.getRelative(BlockFace.SOUTH).getState().getBlockData() instanceof Bed) {
			Bed bed = (Bed) block.getRelative(BlockFace.SOUTH).getState().getBlockData();

			if (bed.getFacing() == BlockFace.SOUTH) {
				if (bed.getPart() == Bed.Part.HEAD) {
					BlockState blockState = block.getRelative(BlockFace.SOUTH).getState();
					blockState.setType(Material.AIR);
					blockState.update(true, false);
				}
			} else if (bed.getFacing() == BlockFace.SOUTH.getOppositeFace()) {
				if (bed.getPart() == Bed.Part.FOOT) {
					BlockState blockState = block.getRelative(BlockFace.SOUTH).getState();
					blockState.setType(Material.AIR);
					blockState.update(true, false);
				}
			}
		}

		if (block.getRelative(BlockFace.WEST).getState().getBlockData() instanceof Bed) {
			Bed bed = (Bed) block.getRelative(BlockFace.WEST).getState().getBlockData();

			if (bed.getFacing() == BlockFace.WEST) {
				if (bed.getPart() == Bed.Part.HEAD) {
					BlockState blockState = block.getRelative(BlockFace.WEST).getState();
					blockState.setType(Material.AIR);
					blockState.update(true, false);
				}
			} else if (bed.getFacing() == BlockFace.WEST.getOppositeFace()) {
				if (bed.getPart() == Bed.Part.FOOT) {
					BlockState blockState = block.getRelative(BlockFace.WEST).getState();
					blockState.setType(Material.AIR);
					blockState.update(true, false);
				}
			}
		}

		if (block.getRelative(BlockFace.EAST).getState().getBlockData() instanceof MultipleFacing) {
			BlockState blockState = block.getRelative(BlockFace.EAST).getState();

			MultipleFacing multipleFacing = (MultipleFacing) blockState.getBlockData();
			multipleFacing.setFace(BlockFace.WEST, false);

			blockState.setBlockData(multipleFacing);
			blockState.update(true, false);
		}

		if (block.getRelative(BlockFace.NORTH).getState().getBlockData() instanceof MultipleFacing) {
			BlockState blockState = block.getRelative(BlockFace.NORTH).getState();

			MultipleFacing multipleFacing = (MultipleFacing) blockState.getBlockData();
			multipleFacing.setFace(BlockFace.SOUTH, false);

			blockState.setBlockData(multipleFacing);
			blockState.update(true, false);
		}

		if (block.getRelative(BlockFace.SOUTH).getState().getBlockData() instanceof MultipleFacing) {
			BlockState blockState = block.getRelative(BlockFace.SOUTH).getState();

			MultipleFacing multipleFacing = (MultipleFacing) blockState.getBlockData();
			multipleFacing.setFace(BlockFace.NORTH, false);

			blockState.setBlockData(multipleFacing);
			blockState.update(true, false);
		}

		if (block.getRelative(BlockFace.WEST).getState().getBlockData() instanceof MultipleFacing) {
			BlockState blockState = block.getRelative(BlockFace.WEST).getState();

			MultipleFacing multipleFacing = (MultipleFacing) blockState.getBlockData();
			multipleFacing.setFace(BlockFace.EAST, false);

			blockState.setBlockData(multipleFacing);
			blockState.update(true, false);
		}
	}
}
