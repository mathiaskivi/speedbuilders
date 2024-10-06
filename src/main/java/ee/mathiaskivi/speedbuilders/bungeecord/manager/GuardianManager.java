package ee.mathiaskivi.speedbuilders.bungeecord.manager;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.utility.VoidGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

public class GuardianManager {

	private final SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	public void spawnGuardian() {
		Location location = new Location(Bukkit.getWorld(plugin.getBungeeCord().currentMap), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots.guardian.spawnpoint.x"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots.guardian.spawnpoint.y"), plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots.guardian.spawnpoint.z"));
		location.setPitch((float) plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots.guardian.spawnpoint.pitch"));
		location.setYaw((float) plugin.getConfigManager().getConfig("maps.yml").getDouble("maps." + plugin.getBungeeCord().currentMap + ".plots.guardian.spawnpoint.yaw"));

		WorldCreator worldCreator = new WorldCreator(plugin.getBungeeCord().currentMap);
		worldCreator.generator(new VoidGenerator());
		Bukkit.createWorld(worldCreator);
		location.getChunk().load();
		for (Entity entity : location.getChunk().getEntities()) {
			if (entity.getType() != EntityType.PLAYER) {
				entity.remove();
			}
		}

		new BukkitRunnable() {
			public void run() {
				plugin.getBungeeCord().elderGuardian = (ElderGuardian) location.getWorld().spawnEntity(location, EntityType.ELDER_GUARDIAN);
				plugin.getBungeeCord().elderGuardian.setCustomName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-GWEN_THE_GUARDIAN")));
				plugin.getBungeeCord().elderGuardian.setCustomNameVisible(true);
				plugin.getBungeeCord().elderGuardian.setAware(false);
				plugin.getBungeeCord().elderGuardian.setCollidable(false);
				plugin.getBungeeCord().elderGuardian.setGravity(false);
				plugin.getBungeeCord().elderGuardian.setSilent(true);
			}
		}.runTaskLater(plugin, 5L);
	}

	public void rotateGuardian(float yaw) {
		Location location = plugin.getBungeeCord().elderGuardian.getLocation();

		if (yaw == 7.5F) {
			location.setYaw(location.getYaw() + 7.5F);
		} else {
			location.setYaw(0F);
		}

		plugin.getBungeeCord().elderGuardian.teleport(location);
	}

	public void laserGuardian(boolean judgingPlayer) {
		if (judgingPlayer) {
			int x = plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(plugin.getBungeeCord().judgedPlayerName) + ".laser-beam.x");
			int y = plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(plugin.getBungeeCord().judgedPlayerName) + ".laser-beam.y");
			int z = plugin.getConfigManager().getConfig("maps.yml").getInt("maps." + plugin.getBungeeCord().currentMap + ".plots." + plugin.getBungeeCord().plots.get(plugin.getBungeeCord().judgedPlayerName) + ".laser-beam.z");
			Location location = new Location(plugin.getBungeeCord().elderGuardian.getWorld(), x, y, z);

			plugin.getBungeeCord().judgedPlayerArmorStand = plugin.getBungeeCord().elderGuardian.getWorld().spawn(location, ArmorStand.class);
			plugin.getBungeeCord().judgedPlayerArmorStand.setGravity(false);
			plugin.getBungeeCord().judgedPlayerArmorStand.setVisible(false);

			plugin.getBungeeCord().elderGuardian.setTarget(plugin.getBungeeCord().judgedPlayerArmorStand);
			plugin.getBungeeCord().elderGuardian.setLaser(true);
		} else {
			plugin.getBungeeCord().elderGuardian.setTarget(null);
			plugin.getBungeeCord().elderGuardian.setLaser(false);
		}
	}

}
