package WizardsGame;

import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.UUID;

public class SpellCastingManager {
    // fireball cast
    void castFireball(UUID playerId) {
        Player player = WizardsPlugin.getPlayerById(playerId);
        if (player != null) {
            double speed = 1; // speed of fireball
            Vector direction = player.getLocation().getDirection().multiply(speed);
            player.launchProjectile(org.bukkit.entity.Fireball.class, direction);
            player.sendMessage(ChatColor.GREEN + "You cast the Fireball spell!");
        }
    }


    void castLightningSpell(UUID playerId) {
        Player player = WizardsPlugin.getPlayerById(playerId);
        if (player != null) {
            double particleDistance = 500; // length of the particle trail
            Vector direction = player.getLocation().getDirection().multiply(particleDistance);
            Location destination = player.getLocation().add(direction);

            // find first block in spell's path
            Location blockLocation = findSolidBlockInPath(player.getLocation().add(0, 1.5, 0), destination.add(0, 1, 0));

            // if solid block is found, strike lightning where particle ends
            if (blockLocation != null) {
                destination = blockLocation;
                spawnAndMoveParticleTrail(player.getLocation().add(0, 1.5, 0), destination.add(0, 1, 0)); // particle adjustment to look better
                strikeLightning(destination);
            }
        }
    }

    private void strikeLightning(Location location) {
        location.getWorld().strikeLightning(location); // Summon lightning at the location
    }

    // particle generation between player and strike area
    private void spawnAndMoveParticleTrail(Location startLocation, Location endLocation) {
        int particleCount = 100000; // number of generated trail particles
        Vector direction = endLocation.toVector().subtract(startLocation.toVector()).normalize();
        double distanceBetweenParticles = startLocation.distance(endLocation) / particleCount;

        for (int i = 0; i < particleCount; i++) {
            Location particleLocation = startLocation.clone().add(direction.clone().multiply(distanceBetweenParticles * i));
            startLocation.getWorld().spawnParticle(Particle.CRIT, particleLocation, 1, 0, 0, 0, 0);
        }
    }
    // check for blocks in path of the particles
    private Location findSolidBlockInPath(Location startLocation, Location endLocation) {
        // check for first non-air block in spell's path
        RayTraceResult result = startLocation.getWorld().rayTraceBlocks(startLocation, endLocation.toVector().subtract(startLocation.toVector()).normalize(),
                startLocation.distance(endLocation), FluidCollisionMode.NEVER, true);

        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlock().getLocation(); //returns location of block in spells path
        }

        return null; // no solid block found
    }
}
