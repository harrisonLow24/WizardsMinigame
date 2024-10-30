package WizardsGame;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportationManager implements Listener {
    private BukkitTask castTimeTask;
    private double castTimeSeconds;


    private boolean isCasting = false;
    private boolean isTeleporting = false;
    private Vector playerVelocity; // store player's velocity before tp


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
//        player.sendMessage("moving");
        if (isCasting) {
            player.sendMessage("casting and moving");
            // prevent jumps
            if (event.getTo().getY() > event.getFrom().getY()) {
                player.setVelocity(new Vector(0, 0, 0));
            }

            // cancel mouse movement while casting
            player.getLocation().setYaw(event.getFrom().getYaw());
            player.getLocation().setPitch(event.getFrom().getPitch());
        }
    }
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        // cancel flight toggling during casting
        if (isCasting) {
            event.setCancelled(true);
        }
    }

    private static class ActionBarUtil {
        static net.md_5.bungee.api.ChatMessageType actionBarType = net.md_5.bungee.api.ChatMessageType.ACTION_BAR;

        static net.md_5.bungee.api.chat.TextComponent createActionBar(String message) {
            return new net.md_5.bungee.api.chat.TextComponent(message);
        }
    }

    // teleportation
    void teleportSpell(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }

        // store player's velocity if in air
        if (player.isFlying()) {
            playerVelocity = player.getVelocity();
        }

        double teleportDistance = 10.0; // teleport distance in blocks
        Vector direction = player.getLocation().getDirection().normalize().multiply(teleportDistance);
        Location destination = player.getLocation().add(direction);
        Location safeLocation = findSafeLocation(player.getLocation(), destination);
        playTeleportSound(safeLocation);

        // reset player's velocity if not in air
        if (!player.isFlying()) {
            player.setVelocity(new Vector(0, 0, 0));
        }

        player.teleport(safeLocation);
        isTeleporting = false; // reset teleporting status
    }

    public boolean isTeleporting() {
        return isTeleporting;
    }


    private void playTeleportSound(Location location) {
        // custom sound effect at teleportation location
        location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    private Location findSafeLocation(Location startLocation, Location destination) {
        Vector direction = destination.toVector().subtract(startLocation.toVector()).normalize();

        // check for collision
        double distanceToCheck = destination.distance(startLocation);
        for (double i = 0; i <= distanceToCheck; i += 0.5) {
            Location checkLocation = startLocation.clone().add(direction.clone().multiply(i));
            if (!checkLocation.getBlock().getType().isAir() && checkLocation.getBlock().getType().isSolid()) {
                // stop before the wall, return the last air block
                return checkLocation.subtract(direction.clone().multiply(0.5));
            }
        }
        // if no wall is hit, return original destination
        return destination;
    }

    public void castTeleportSpell(UUID playerId, double castTimeSeconds) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
//
//        player.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Casting Teleportation spell...");

        this.castTimeSeconds = castTimeSeconds;
        spawnDarkParticles(player.getLocation()); // dark particles at starting location
        isCasting = true; // set casting status to true
        disablePlayerMovement(player); // disable player movement during tp

        // schedule task to update action bar every second until cast complete
        castTimeTask = new BukkitRunnable() {
            double remainingTime = castTimeSeconds;

            @Override
            public void run() {
                if (remainingTime <= 0) {
                    // cast complete, teleporting player
                    teleportSpell(playerId);
                    spawnDarkParticles(player.getLocation());

                    // enable player movement after tp
                    enablePlayerMovement(player);
                    castTimeTask.cancel();
                } else {
                    // update action bar with remaining cast time
                    player.spigot().sendMessage(
                            ActionBarUtil.createActionBar(ChatColor.YELLOW + "Cast Time: "
                                    + ChatColor.RED + remainingTime + "s"));
                    remainingTime -= 0.5;
                }
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0L, 20L); // run every second

    }

    private void spawnDarkParticles(Location location) {
        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 50, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().spawnParticle(Particle.PORTAL, location, 20, 0.5, 0.5, 0.5, 0.1);
    }
    private void resetCastingStatus() {
        isCasting = false;
    }
    private void enablePlayerMovement(Player player) {
        player.setWalkSpeed(0.2f);
        player.setAllowFlight(true);
        resetCastingStatus();
    }
    private void disablePlayerMovement(Player player) {
        player.setWalkSpeed(0);
        player.setAllowFlight(false);
        player.setFlying(false);
    }
}
