package WizardsGame;

import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.*;

import org.bukkit.Particle;

import static WizardsGame.WizardsPlugin.getPlayerById;

public class SpellCastingManager {
    public final Map<UUID, Integer> lightningEffectDuration = new HashMap<>();
    private final Map<UUID, Boolean> lightningEffectTriggered = new HashMap<>();

    //map teleport
    private static final int TELEPORT_DURATION = 5; // duration player stays in the air (in seconds)
    private static final int PLATFORM_SIZE = 150; // size of the platform (size x size)
    private static final int TELEPORT_UP_HEIGHT = 60; // height to teleport up
    private static final double SPEED_BOOST = 2.0; // speed while on barrier platform

    // fireball cast
    void castFireball(UUID playerId) {
        final double[] speed = {1};
        Player player = getPlayerById(playerId);
        if (player != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    speed[0] += 1; // speed of fireball
                }
            }.runTaskTimer(WizardsPlugin.getInstance(), 0L, 1L);
            Vector direction = player.getLocation().getDirection().multiply(speed[0]);
            player.launchProjectile(org.bukkit.entity.Fireball.class, direction);
            player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "" + "You cast the Fireball spell!");
            player.getWorld().playSound(player, Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
        }
    }

    public void castLightningSpell(UUID playerId) {
        Player player = getPlayerById(playerId);
        if (player != null) {
            double particleDistance = 1000;
            Vector direction = player.getLocation().getDirection().multiply(particleDistance);
            Location destination = player.getLocation().add(direction);
            Location blockLocation = findSolidBlockInPath(player.getLocation().add(0, 1.5, 0), destination.add(0, 1, 0));

            if (blockLocation != null) {
                destination = blockLocation;
                spawnAndMoveParticleTrailWithEntityCheck(player.getLocation().add(0, 1.5, 0), destination.add(0, 1, 0), playerId);
                scheduleLightningStrike(destination, playerId); // pass playerId to the method

                // check if lightning effect has been triggered already
                if (!lightningEffectTriggered.getOrDefault(playerId, false)) {
                    // mark lightning effect as triggered
                    lightningEffectTriggered.put(playerId, true);

                    // schedule exaggerated lightning effect with a delay
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            startLightningEffect(playerId);
                        }
                    }.runTaskLater(WizardsPlugin.getInstance(), 20); // 20 ticks = 1 second
                }

                player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "You cast the Lightning spell!");
            }
        }
    }
    private void spawnAndMoveParticleTrailWithEntityCheck(Location startLocation, Location endLocation, UUID playerId) {
        int particleCount = 100; // number of generated trail particles
        Vector direction = endLocation.toVector().subtract(startLocation.toVector()).normalize();
        double distanceBetweenParticles = startLocation.distance(endLocation) / particleCount;

        boolean entityHit = false; // flag to track if entity was hit by particle trail

        for (int i = 0; i < particleCount; i++) {
            Location particleLocation = startLocation.clone().add(direction.clone().multiply(distanceBetweenParticles * i));
            startLocation.getWorld().spawnParticle(Particle.CRIT, particleLocation, 1, 0, 0, 0, 0);

            // check for entities in particle's location
            for (Entity entity : particleLocation.getWorld().getNearbyEntities(particleLocation, 1, 1, 1)) {
                if (entity.getType() != EntityType.PLAYER) { // exclude players from being struck . . . change for PVP
                    entityHit = true;
                    break; // stop checking for entities
                }
            }

            if (entityHit) {
                break; // stop particle trail loop if entity is hit
            }
        }

        // handle lightning strike logic after particle trail loop completes
        if (entityHit) {
            strikeLightning(endLocation, playerId); // pass playerId to the method
        }
    }

    private void strikeLightning(Location location, UUID playerId) {
        // set a delay (in ticks) before striking lightning
        int delayTicks = 0; // set delay to 0 for immediate lightning strike

        new BukkitRunnable() {
            @Override
            public void run() {
                location.getWorld().strikeLightning(location);
                startLightningEffect(playerId); // start the lightning effect
            }
        }.runTaskLater(WizardsPlugin.getInstance(), delayTicks);
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

    private void scheduleLightningStrike(Location location, UUID playerId) {
        // set delay (in ticks) before striking lightning
        int delayTicks = 20; // (20 ticks = 1 second)

        new BukkitRunnable() {
            @Override
            public void run() {
                strikeLightning(location, playerId); // pass playerId
            }
        }.runTaskLater(WizardsPlugin.getInstance(), delayTicks);
    }


    // lightning effect

    void startLightningEffect(UUID playerId) {
        Player player = getPlayerById(playerId);
        if (player == null) {
            return;
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
        player.getWorld().strikeLightningEffect(player.getLocation());
        new BukkitRunnable() {
            double radius = 3.0;
            double height = 5.0;
            double angle = 0;

            @Override
            public void run() {
                if (angle >= 360) {
                    this.cancel();
                    return;
                }

                double x = radius * Math.cos(Math.toRadians(angle));
                double z = radius * Math.sin(Math.toRadians(angle));

                Location particleLocation = player.getLocation().clone().add(x, height, z);
                player.getWorld().spawnParticle(Particle.CRIT, particleLocation, 10, 0.2, 0.2, 0.2, 0.1);

                angle += 10;
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0, 1);

        // schedule task to reset lightning effect trigger after a duration
        int maxLightningEffectDuration = 100;
        lightningEffectDuration.put(playerId, maxLightningEffectDuration);

        Bukkit.getScheduler().runTaskTimer(WizardsPlugin.getInstance(), () -> {
            int remainingDuration = lightningEffectDuration.getOrDefault(playerId, 0);

            if (remainingDuration <= 0) {
                lightningEffectDuration.remove(playerId);

                // reset lightning effect trigger
                lightningEffectTriggered.put(playerId, false);

                return;
            }

            // decrease remaining duration
            lightningEffectDuration.put(playerId, remainingDuration - 1);
        }, 0, 1);
    }



    void castGustSpell(UUID playerId) {
        Player player = getPlayerById(playerId);
        double gustRadius = 5.0; // radius of gust spell
        double gustStrength = 2.0; // strength of gust spell

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



    public static void launchMinecart(Player player) {
        // create and launch minecart with player inside
        Minecart minecart = player.getWorld().spawn(player.getLocation(), Minecart.class);

        // calculate launch direction based on player's pitch and yaw
        double pitch = Math.toRadians(player.getLocation().getPitch());
        double yaw = Math.toRadians(player.getLocation().getYaw());

        double cosPitch = Math.cos(pitch); // precalculate cos of pitch for optimization

        // calculate launch direction using spherical coordinates
        double x = -Math.sin(yaw) * cosPitch;
        double y = -Math.sin(pitch);
        double z = -Math.cos(yaw) * cosPitch;

        Vector launchDirection = new Vector(x, y, z).normalize().multiply(5); // adjust the launch speed

        minecart.setVelocity(launchDirection);

        minecart.setPassenger(player); // sets player as a passenger

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
        Player player = getPlayerById(playerId);
        if (player != null) {
            groundPound(player);
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Ground Pound spell cast!");
        }
    }
    private void groundPound(Player player) {
        var delay = player.isOnGround() ? 10L : 0; // set delay to 1 if player is on ground, otherwise set to 0
        World world = player.getWorld();
        Location initialLocation = player.getLocation();

        // particle effects at initial location
        world.spawnParticle(Particle.EXPLOSION_LARGE, initialLocation, 1, 0, 0, 0, 0);

        if (player.isOnGround()) {  // && player.getVelocity().getY() > 0 && !player.isFlying() && !player.isGliding()
            // launch the player up before bringing them down if not already in the air
            player.setVelocity(new Vector(0, 1.5, 0)); // adjust upward velocity
        }

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
                    player.setFallDistance(0); // reset fall distance to prevent fall damage

                    // launch blocks upward when player touches ground
                    this.cancel();
                    playGPSound(landingLocation);
                    dealDamageToEntities(player, player.getWorld(), landingLocation);

                    // entity launch
                    double launchRadius = 5.0;
                    double launchVelocity = 1.2;
                    launchEntitiesExcludingCaster(player, initialLocation, launchRadius, launchVelocity);

                } else {
                    // apply upward velocity to cancel fall velocity
                    player.setVelocity(new Vector(0, -1.5, 0));
                    player.setFallDistance(0);
                }
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), delay, 1L); // delay 1 second = 20 ticks
    }


    private void dealDamageToEntities(Player player, World world, Location landingLocation) {
        double damageRadius = 5.0;
        double damageAmount = 2.0; // hearts of damage
        for (Entity entity : landingLocation.getWorld().getNearbyEntities(landingLocation, damageRadius, damageRadius, damageRadius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity livingEntity = (LivingEntity) entity;
                EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(player, livingEntity, DamageCause.ENTITY_ATTACK, damageAmount);
                Bukkit.getPluginManager().callEvent(damageEvent);

                if (!damageEvent.isCancelled()) {
                    livingEntity.damage(damageAmount);
                }
            }
        }
    }
    private void launchEntitiesExcludingCaster(Player caster, Location location, double launchRadius, double launchVelocity) {
        World world = location.getWorld();
        for (Entity entity : location.getWorld().getNearbyEntities(location, launchRadius, launchRadius, launchRadius)) {
            if (entity instanceof LivingEntity && !entity.equals(caster)) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // calculate direction away from caster
                Vector awayDirection = entity.getLocation().toVector().subtract(caster.getLocation().toVector()).normalize();

                // launch entities upward and away
                Vector launchVector = new Vector(awayDirection.getX(), launchVelocity, awayDirection.getZ());
                livingEntity.setVelocity(launchVector);
            }
        }
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
                            Vector velocity = getRandomVelocity(minVelocity, maxVelocity); // use random velocity
                            fallingBlock.setVelocity(velocity);

                            // remove original block
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    private static final HashMap<UUID, List<Block>> barrierBlocksMap = new HashMap<>(); // Store barrier blocks for each player
    public void createBarrierPlatform(int x, int z, int y, Player player) {
        List<Block> barrierBlocks = new ArrayList<>();
        for (int i = -PLATFORM_SIZE / 2; i <= PLATFORM_SIZE / 2; i++) {
            for (int j = -PLATFORM_SIZE / 2; j <= PLATFORM_SIZE / 2; j++) {
                Block block = player.getWorld().getBlockAt(x + i, y, z + j);
                block.setType(Material.BARRIER); // set block to barrier
                barrierBlocks.add(block); // store block in the list
            }
        }
        barrierBlocksMap.put(player.getUniqueId(), barrierBlocks); // store list in the map
    }
    public void removeBarrierPlatform(UUID playerId) {
        List<Block> barrierBlocks = barrierBlocksMap.get(playerId);
        if (barrierBlocks != null) {
            for (Block block : barrierBlocks) {
                block.setType(Material.AIR); // set blocks to air
            }
            barrierBlocksMap.remove(playerId); // remove barriers from hashmap
        }
    }
    private void startTeleportationEffects(Player player) {
        int playerY = player.getLocation().getBlockY();
        Vector playerDirection = player.getLocation().getDirection();

        // Check blocks below player's current position
        for (int y = playerY - 1; y >= 0; y--) {
            Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());

            // Check if the block is not air and not a barrier
            if (block.getType() != Material.AIR && block.getType() != Material.BARRIER) {
                // Start fancy teleportation effects
                Location teleportLocation = block.getLocation().add(0, 1, 0);
                player.getWorld().spawnParticle(Particle.PORTAL, teleportLocation, 30, 1, 1, 1, 0.1);
                player.getWorld().playSound(teleportLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.getWorld().playSound(teleportLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

                // Delay for one second before teleporting
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(teleportLocation.setDirection(playerDirection)); // Teleport player
                        // Remove all barrier blocks associated with player
                        removeBarrierPlatform(player.getUniqueId());
                    }
                }.runTaskLater(WizardsPlugin.getInstance(), 20); // 20 ticks = 1 second
                return;
            }
        }
    }
    public void teleportPlayerUp(Player player) {
        // get player's current location
        Vector playerLocation = player.getLocation().toVector();
        // create platform at the player's current Y position + TELEPORT_UP_HEIGHT
        createBarrierPlatform(playerLocation.getBlockX(), playerLocation.getBlockZ(), playerLocation.getBlockY() + TELEPORT_UP_HEIGHT, player);

        // teleport player up
        player.teleport(player.getLocation().add(0, TELEPORT_UP_HEIGHT + 1, 0));
        player.setVelocity(new Vector(0, 0, 0)); // reset velocity

        // increase speed
        player.setWalkSpeed((float) (player.getWalkSpeed() * SPEED_BOOST));

        BossBar bossBar = Bukkit.createBossBar("Move quickly!", BarColor.BLUE, BarStyle.SEGMENTED_20);
        bossBar.addPlayer(player);

        new BukkitRunnable() {
            int timeLeft = TELEPORT_DURATION;
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    teleportBackDown(player);
                    bossBar.removePlayer(player);
                    cancel();
                } else {
                    bossBar.setProgress((double) timeLeft / TELEPORT_DURATION);
                    player.sendMessage("You have " + timeLeft + " seconds to move!");
                    timeLeft--;
                }
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0,20);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setWalkSpeed(0.2f); // reset to default walk speed
            }
        }.runTaskLater(WizardsPlugin.getInstance(), TELEPORT_DURATION * 20);
    }

    public void teleportBackDown(Player player) {
        int playerY = player.getLocation().getBlockY();
        // gets players direction they are looking
        Vector playerDirection = player.getLocation().getDirection();

        // check blocks below player's current position
        for (int y = playerY - 1; y >= 0; y--) {
            Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());

            // check if the block != air and != barrier
            if (block.getType() != Material.AIR && block.getType() != Material.BARRIER) {
                // teleport player to the block above it
                Location teleportLocation = block.getLocation().add(0, 1, 0);
                player.teleport(teleportLocation.setDirection(playerDirection));
                // remove all barrier blocks associated with player
                removeBarrierPlatform(player.getUniqueId());

                // sound effect
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                return;
            }
        }
    }

// WizardsPlugin.getInstance()



    int porkchopSpeed = 2;
    public void castPorkchopSpell(Player player) {
        // create and launch porkchop
        ItemStack porkchopItem = new ItemStack(Material.PORKCHOP);
        Item porkchopEntity = player.getWorld().dropItem(player.getEyeLocation(), porkchopItem);
        porkchopEntity.setVelocity(player.getLocation().getDirection().multiply(porkchopSpeed)); // porkchop speed

        // store player's UUID in item's metadata
        ItemMeta itemMeta = porkchopItem.getItemMeta();
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(String.valueOf(this), "caster"), PersistentDataType.STRING, player.getUniqueId().toString());
        porkchopItem.setItemMeta(itemMeta);
        player.getWorld().playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
    }
}
