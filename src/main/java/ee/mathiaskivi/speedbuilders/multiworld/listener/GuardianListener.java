package ee.mathiaskivi.speedbuilders.multiworld.listener;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.multiworld.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class GuardianListener implements Listener {
    private SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.getEntity().getType() != EntityType.ELDER_GUARDIAN) {
            return;
        }

        Arena arena = plugin.getMultiWorld().getArenaManager().getArena(e.getEntity().getWorld().getName());
        if (arena == null) {
            return;
        }

        if (e.getTarget() != null && e.getTarget().getType() == EntityType.ARMOR_STAND) {
            return;
        }

        Bukkit.broadcastMessage("WAT");

        e.setCancelled(true);
    }
}
