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
            double particleDistance = 1000; // length of particle trail
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
        location.getWorld().strikeLightning(location);
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
        assert player != null;
        for (Entity entity : player.getNearbyEntities(gustRadius, gustRadius, gustRadius)) {
//            if (entity instanceof Player) {
//                // can add case for if player is detected
//                continue;
//            }

            // calculate direction vector from player to entity
            org.bukkit.util.Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();

            // apply gust by pushing entities away
            entity.setVelocity(direction.multiply(gustStrength));
        }

        player.sendMessage( ChatColor.WHITE.toString() + ChatColor.BOLD +"Gust spell cast!");
    }



    void launchMinecart(Player player) {
        // create and launch the minecart with player inside
        Minecart minecart = player.getWorld().spawn(player.getLocation(), Minecart.class);

        // set the minecart's velocity
        Vector direction = player.getLocation().getDirection().multiply(5);  // adjust the launch speed
        direction.setX(direction.getX() * 100);  // adjust the x velocity
        minecart.setVelocity(direction);


        minecart.setPassenger(player); // sets player as passenger

        // schedule task to check for minecart landing
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!minecart.isValid() || minecart.isOnGround()) {
                    // eject the player from the minecart when landing
                    if (minecart.getPassenger() != null && minecart.getPassenger() instanceof Player) {
                        Player passenger = (Player) minecart.getPassenger();
                        passenger.leaveVehicle();
                    }

                    // note to self: can add effects or actions for minecart landing
                    minecart.remove();
                    this.cancel();
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
        Location initialLocation = player.getLocation();

        // particle effects at initial location
        world.spawnParticle(Particle.EXPLOSION_LARGE, initialLocation, 1, 0, 0, 0, 0);

        // launch the player up before bringing them down
        player.setVelocity(new Vector(0, 1, 0)); // Adjust the upward velocity

        // delay before descent
        new BukkitRunnable() {
            @Override
            public void run() {
                // check if the player is on a block
                if (player.isOnGround()) {
                    // create a circular impact
                    double radius = 5.0;
                    Location landingLocation = player.getLocation();

                    // particle effects for descent
                    for (double y = 0; y <= 10; y += 0.5) {
                        world.spawnParticle(Particle.CLOUD, landingLocation.clone().add(0, y, 0), 1, 0, 0, 0, 0);
                    }

                    scatterBlocks(landingLocation); // scatter blocks relative to landing location

                    for (double x = -radius; x <= radius; x += 0.5) {
                        for (double z = -radius; z <= radius; z += 0.5) {
                            if (Math.sqrt(x * x + z * z) <= radius) {
                                Block block = world.getBlockAt(initialLocation.clone().add(x, -1, z));
                                if (block.getType() != Material.BEDROCK) {
                                    // create a falling block entity for each block
                                    FallingBlock fallingBlock = world.spawnFallingBlock(block.getLocation(), block.getBlockData());

                                    // set the falling block's velocity
                                    Vector velocity = new Vector(0, 2, 0);
                                    fallingBlock.setVelocity(velocity);

                                    block.setType(Material.AIR); // remove original block
                                }
                            }
                        }
                    }

                    // make player fall faster without taking fall damage
                    player.setFallDistance(0); // Reset fall distance to prevent fall damage

                    // launch blocks upward when player touches ground
                    this.cancel();
                    playGPSound(landingLocation);
                } else {
                    // apply upward velocity to cancel fall velocity
                    player.setVelocity(new Vector(0, -5, 0));
                    player.setFallDistance(0);
                }
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 10L, 1L); // delay1 second = 20 ticks
    }





    private Vector getRandomVelocity(double minVelocity, double maxVelocity) {
        double randomX = minVelocity + Math.random() * (maxVelocity - minVelocity);
        double randomY = minVelocity + Math.random() * (maxVelocity - minVelocity);
        double randomZ = minVelocity + Math.random() * (maxVelocity - minVelocity);

        return new Vector(randomX, randomY, randomZ);
    }



    private void scatterBlocks(Location location) {
        World world = location.getWorld();
        double scatterRadius = 5.0;
        double minVelocity = 0.5;
        double maxVelocity = 1.5;

        // iterate through blocks in a sphere
        for (double x = -scatterRadius; x <= scatterRadius; x += 3) {
            for (double y = -scatterRadius; y <= scatterRadius; y += 2) {
                for (double z = -scatterRadius; z <= scatterRadius; z += 2) {
                    if (Math.sqrt(x * x + y * y + z * z) <= scatterRadius) {
                        assert world != null;
                        Block block = world.getBlockAt(location.clone().add(x, y, z));
                        if (block.getType() != Material.BEDROCK) {
                            // create falling block for each block
                            FallingBlock fallingBlock = world.spawnFallingBlock(block.getLocation(), block.getBlockData());

                            // set random velocity
                            Vector velocity = getRandomVelocity(minVelocity, maxVelocity); // Use the random velocity
                            fallingBlock.setVelocity(velocity);

                            // remove original block
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
        player.getWorld().playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
    }
}
