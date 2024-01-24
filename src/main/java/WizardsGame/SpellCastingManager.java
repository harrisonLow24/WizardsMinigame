package WizardsGame;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.util.BlockIterator;

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



    void launchMinecart(Player player) {
        // create and launch the minecart with the player inside
        Minecart minecart = player.getWorld().spawn(player.getLocation(), Minecart.class);

        // Set the minecart's velocity to a high value
        Vector direction = player.getLocation().getDirection().multiply(5);  // adjust the launch speed
        direction.setX(direction.getX() * 100);  // adjust the x velocity
        minecart.setVelocity(direction);

        // Set the player as the passenger
        minecart.setPassenger(player);

        // Schedule a task to check for the minecart landing
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!minecart.isValid() || minecart.isOnGround()) {
                    // Eject the player from the minecart when it lands
                    if (minecart.getPassenger() != null && minecart.getPassenger() instanceof Player) {
                        Player passenger = (Player) minecart.getPassenger();
                        passenger.leaveVehicle();
                    }

                    // Optionally, you can add more effects or actions when the minecart lands
                    minecart.remove();
                    this.cancel(); // Stop the task once the minecart lands
                }
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0L, 1L);
    }
    private void playGPSound(Location location) {
        // custom sound effect at teleportation location
        location.getWorld().playSound(location, Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 1.0f);
    }
    void castGroundPoundSpell(UUID playerId) {
        Player player = WizardsPlugin.getPlayerById(playerId);
        if (player != null) {
            groundPound(player);
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Ground Pound spell cast!");
        }
    }

    private void groundPound(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();

        // Play particle effects
        world.spawnParticle(Particle.EXPLOSION_LARGE, location, 1, 0, 0, 0, 0);

        // Launch the player up before bringing them down
        player.setVelocity(new Vector(0, 1, 0)); // Adjust the upward velocity

        // Wait for a moment before starting the descent
        new BukkitRunnable() {
            @Override
            public void run() {
                // Create a circular impact on the terrain
                double radius = 5.0;
                for (double x = -radius; x <= radius; x += 0.5) {
                    for (double z = -radius; z <= radius; z += 0.5) {
                        if (Math.sqrt(x * x + z * z) <= radius) {
                            Block block = world.getBlockAt(location.clone().add(x, -1, z));
                            if (block.getType() != Material.BEDROCK) {
                                // Create a falling block entity for each block
                                FallingBlock fallingBlock = world.spawnFallingBlock(block.getLocation(), block.getBlockData());

                                // Set the falling block's velocity to simulate scattering
                                Vector velocity = new Vector(0, 2, 0); // Adjust the velocity as needed
                                fallingBlock.setVelocity(velocity);

                                // Remove the original block
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }

                // Make the player fall faster without taking fall damage
                player.setFallDistance(0); // Reset fall distance to prevent fall damage

                // Check if the player is on the ground
                if (player.isOnGround()) {
                    // Launch blocks upward when the player touches the ground
                    scatterBlocks(location);
                    this.cancel(); // Stop the task once the blocks are launched
                } else {
                    // Apply upward velocity to cancel fall velocity
                    player.setVelocity(new Vector(0, -1, 0));
                }
                playGPSound(location);
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 10L, 1L); // Delay for 1 second (20 ticks) before starting the descent
    }




    private void scatterBlocks(Location location) {
        World world = location.getWorld();
        double scatterRadius = 5.0;

        // Iterate through blocks in a spherical pattern
        for (double x = -scatterRadius; x <= scatterRadius; x += 3) {
            for (double y = -scatterRadius; y <= scatterRadius; y += 2) {
                for (double z = -scatterRadius; z <= scatterRadius; z += 2) {
                    if (Math.sqrt(x * x + y * y + z * z) <= scatterRadius) {
                        Block block = world.getBlockAt(location.clone().add(x, y, z));
                        if (block.getType() != Material.BEDROCK) {
                            // Create a falling block entity for each block
                            FallingBlock fallingBlock = world.spawnFallingBlock(block.getLocation(), block.getBlockData());

                            // Set the falling block's velocity to simulate launching blocks upward
                            Vector velocity = new Vector(.75, 1, .75); // Adjust the velocity as needed
                            fallingBlock.setVelocity(velocity);

                            // Remove the original block
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
    int porkchopSpeed = 2;
    public void castPorkchopSpell(Player player) {
        // create and launch porkchop
        ItemStack porkchopItem = new ItemStack(Material.PORKCHOP);
        Item porkchopEntity = player.getWorld().dropItem(player.getEyeLocation(), porkchopItem);
        porkchopEntity.setVelocity(player.getLocation().getDirection().multiply(porkchopSpeed)); // Porkchop speed

        // store player's UUID in item's metadata
        ItemMeta itemMeta = porkchopItem.getItemMeta();
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(String.valueOf(this), "caster"), PersistentDataType.STRING, player.getUniqueId().toString());
        porkchopItem.setItemMeta(itemMeta);
    }

}
