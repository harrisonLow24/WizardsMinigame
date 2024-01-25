package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public class TeleportationManager {
    // teleportation
    void teleportSpell(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }

        double teleportDistance = 10.0; // teleport distance in blocks
        Vector direction = player.getLocation().getDirection().multiply(teleportDistance);
        Location destination = player.getLocation().add(direction);
        Location safeLocation = findSafeLocation(player.getLocation(), destination);// find nearest safe teleportation location
        playTeleportSound(safeLocation);// teleportation sound effect
        player.teleport(safeLocation); // teleport player to safe location
        player.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "You cast the Teleportation spell!");
    }

    private void playTeleportSound(Location location) {
        // custom sound effect at teleportation location
        location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    private Location findSafeLocation(Location startLocation, Location destination) {
        // nearest air block around destination
        for (int y = 0; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location checkLocation = destination.clone().add(x, y, z);
                    if (checkLocation.getBlock().getType().isAir()) {
                        return checkLocation;
                    }
                }
            }
        }

        return startLocation; // if no safe location is found, return original starting location
    }
}
