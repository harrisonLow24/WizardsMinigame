package WizardsGame;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
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
            player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "" + "You cast the Fireball spell!");
        }
    }

    void castLightningSpell(UUID playerId) {
        Player player = WizardsPlugin.getPlayerById(playerId);
        if (player != null) {
            double particleDistance = 1000; // length of the particle trail
            Vector direction = player.getLocation().getDirection().multiply(particleDistance);
            Location destination = player.getLocation().add(direction);

            // find first block in spell's path
            Location blockLocation = findSolidBlockInPath(player.getLocation().add(0, 1.5, 0), destination.add(0, 1, 0));

            // if solid block is found, strike lightning where particle ends
            if (blockLocation != null) {
                destination = blockLocation;
                spawnAndMoveParticleTrail(player.getLocation().add(0, 1.5, 0), destination.add(0, 1, 0)); // particle adjustment to look better
                strikeLightning(destination);
                player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "You cast the Lightning spell!");
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

    void castGustSpell(UUID playerId) {
        Player player = WizardsPlugin.getPlayerById(playerId);
        double gustRadius = 5.0; // radius of the gust spell
        double gustStrength = 2.0; // strength of the gust spell

        // push all nearby entities
        for (Entity entity : player.getNearbyEntities(gustRadius, gustRadius, gustRadius)) {
//            if (entity instanceof Player) {
//                // can add special case for if player is detected
//                continue;
//            }

            // calculate direction vector from player to the entity
            org.bukkit.util.Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();

            // apply gust by pushing entity away
            entity.setVelocity(direction.multiply(gustStrength));
        }

        player.sendMessage( ChatColor.WHITE.toString() + ChatColor.BOLD +"Gust spell cast!");
    }
//    void createMinecart(UUID playerId){
//        Player player = WizardsPlugin.getPlayerById(playerId);
//
//        if (player != null) {
//            double speed = 1; // speed of fireball
//            Vector direction = player.getLocation().getDirection().multiply(speed);
////            player.addPassenger(org.bukkit.entity.Vehicle.Minecart.class, direction);
//
//            player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "" + "You cast the Fireball spell!");
//        }
//    }

//    void castIceSphere(UUID playerId) {
//        Player player = WizardsPlugin.getPlayerById(playerId);
//        double iceSphereCost = 20.0;
//
//        if (player != null) {
//            if (WizardsPlugin.getPlayerMana(player) >= iceSphereCost) {
//                Snowball snowball = player.launchProjectile(Snowball.class);
//                // customize  snowball behavior
//
//                new BukkitRunnable() {
//                    @Override
//                    public void run() {
//                        if (snowball.isValid()) {
//                            Location snowballLocation = snowball.getLocation();
//
//                            // check if snowball is close to the ground
//                            if (snowballLocation.getBlock().getType() != Material.AIR) {
//                                createIceSphere(snowballLocation);
//                                this.cancel(); // Stop the task once the snowball hits a block
//                            }
//                        } else {
//                            this.cancel(); // Stop the task if the snowball is no longer valid
//                        }
//                    }
//                }.runTaskTimer(WizardsPlugin.getInstance(), 0L, 1L);
//            } else {
//                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Not enough mana to cast Ice Sphere!");
//            }
//        }
//    }



    private void createIceSphere(Location location) {
        double sphereRadius = 3.0; // Radius of the ice sphere
        int sphereParticles = 1000; // Number of particles in the sphere

        for (double phi = 0; phi <= Math.PI; phi += Math.PI / sphereParticles) {
            double y = sphereRadius * Math.cos(phi);
            for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / sphereParticles) {
                double x = sphereRadius * Math.sin(phi) * Math.cos(theta);
                double z = sphereRadius * Math.sin(phi) * Math.sin(theta);

                location.getWorld().spawnParticle(Particle.SNOW_SHOVEL, location.clone().add(x, y, z), 1, 0, 0, 0, 0);
            }
        }

        // Optionally, you can add more effects or modify the surroundings based on the ice sphere creation.
    }

}
