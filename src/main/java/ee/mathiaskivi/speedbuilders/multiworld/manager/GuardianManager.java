package ee.mathiaskivi.speedbuilders.multiworld.manager;

import ee.mathiaskivi.speedbuilders.SpeedBuilders;
import ee.mathiaskivi.speedbuilders.multiworld.Arena;
import ee.mathiaskivi.speedbuilders.utility.VoidGenerator;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import static ee.mathiaskivi.speedbuilders.utility.Translations.translate;

public class GuardianManager {
    private final SpeedBuilders plugin = (SpeedBuilders) Bukkit.getPluginManager().getPlugin("SpeedBuilders");

    public void spawnGuardian(String arenaName) {
        Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

        if (arena != null) {
            Location location = new Location(Bukkit.getWorld(arena.getName()), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots.guardian.spawnpoint.x"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots.guardian.spawnpoint.y"), plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots.guardian.spawnpoint.z"));
            location.setPitch((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots.guardian.spawnpoint.pitch"));
            location.setYaw((float) plugin.getConfigManager().getConfig("arenas.yml").getDouble("arenas." + arena.getName() + ".plots.guardian.spawnpoint.yaw"));

            WorldCreator worldCreator = new WorldCreator(arena.getName());
            worldCreator.generator(new VoidGenerator());
            Bukkit.createWorld(worldCreator);
            location.getChunk().load();

            new BukkitRunnable() {
                public void run() {
                    arena.setElderGuardian((ElderGuardian) location.getWorld().spawnEntity(location, EntityType.ELDER_GUARDIAN));
                    arena.getElderGuardian().setCustomName(ChatColor.translateAlternateColorCodes('&', translate("MAIN-GWEN_THE_GUARDIAN")));
                    arena.getElderGuardian().setCustomNameVisible(true);

                    arena.getElderGuardian().setAware(false);
                    arena.getElderGuardian().setCollidable(false);
                    arena.getElderGuardian().setGravity(false);
                    arena.getElderGuardian().setSilent(true);
                }
            }.runTaskLater(plugin, 5L);
        }
    }

    public void rotateGuardian(float yaw, String arenaName) {
        Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

        if (arena != null) {
            Location location = arena.getElderGuardian().getLocation();

            if (yaw == 7.5F) {
                location.setYaw(location.getYaw() + 7.5F);
            } else {
                location.setYaw(0F);
            }

            arena.getElderGuardian().teleport(location);
        }
    }

    public void laserGuardian(boolean judgingPlayer, String arenaName) {
        Arena arena = plugin.getMultiWorld().getArenaManager().getArena(arenaName);

        if (arena != null) {
            if (judgingPlayer) {
                int x = plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(arena.getJudgedPlayerName()) + ".laser-beam.x");
                int y = plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(arena.getJudgedPlayerName()) + ".laser-beam.y");
                int z = plugin.getConfigManager().getConfig("arenas.yml").getInt("arenas." + arena.getName() + ".plots." + arena.getPlots().get(arena.getJudgedPlayerName()) + ".laser-beam.z");
                Location location = new Location(arena.getElderGuardian().getWorld(), x, y, z);

                arena.setJudgedPlayerArmorStand(arena.getElderGuardian().getWorld().spawn(location, ArmorStand.class));
                arena.getJudgedPlayerArmorStand().setGravity(false);
                arena.getJudgedPlayerArmorStand().setVisible(false);

                arena.getElderGuardian().setTarget(arena.getJudgedPlayerArmorStand());
                arena.getElderGuardian().playEffect(EntityEffect.GUARDIAN_TARGET);
            } else {
                arena.getElderGuardian().setTarget(null);
                arena.getElderGuardian().playEffect(EntityEffect.GUARDIAN_TARGET);
            }
        }
    }
}
