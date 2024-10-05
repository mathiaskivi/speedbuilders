package ee.mathiaskivi.speedbuilders.bungeecord.listener;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.api.game.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class WorldListener implements Listener {
	private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockCanBuild(BlockCanBuildEvent e) {
		if (plugin.getBungeeCord().state == ArenaState.BUILDING) {
			Player player = e.getPlayer();
			Location location = e.getBlock().getLocation().add(0.5, 1, 0.5);

			for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 1, 0.5)) { // Find all entities that are located in the usual block placing BoundingBox (X1: -1.3; Y1: -1.8; Z1: -1.3, X2: 1.3; Y2: 2; Z2: 1.3)
				if (entity != player) {
					e.setBuildable(true);
					break;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason() != SpawnReason.CUSTOM) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityChangeBlock(EntityChangeBlockEvent e) {
		if (plugin.getBungeeCord().state == ArenaState.BEGINNING || plugin.getBungeeCord().state == ArenaState.DISPLAYING || plugin.getBungeeCord().state == ArenaState.BUILDING || plugin.getBungeeCord().state == ArenaState.JUDGING) {
			if (e.getEntityType() == EntityType.FALLING_BLOCK) {
				e.getEntity().getWorld().playEffect(e.getEntity().getLocation(), Effect.STEP_SOUND, e.getTo());
				e.getEntity().remove();
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPhysics(BlockPhysicsEvent e) {
		if (plugin.getBungeeCord().state == ArenaState.BEGINNING || plugin.getBungeeCord().state == ArenaState.DISPLAYING || plugin.getBungeeCord().state == ArenaState.BUILDING || plugin.getBungeeCord().state == ArenaState.JUDGING) {
			if (e.getChangedType().toString().endsWith("_BARS")
					|| e.getChangedType().toString().endsWith("_FENCE")
					|| e.getChangedType().toString().endsWith("_GATE")
					|| e.getChangedType().toString().endsWith("_PANE")
					|| e.getChangedType().toString().endsWith("_STAIRS")
					|| e.getChangedType().toString().endsWith("_WALL")
					|| e.getChangedType().toString().endsWith("CHEST")
					|| e.getChangedType().toString().equals("REDSTONE_WIRE")) {
				return;
			}

			e.setCancelled(true);
		}
	}
}
