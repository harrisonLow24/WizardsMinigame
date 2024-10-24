package WizardsGame;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import java.util.*;
import org.bukkit.Particle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static WizardsGame.WizardsPlugin.getPlayerById;

public class SpellCastingManager implements Listener {
    public final Map<UUID, Integer> lightningEffectDuration = new HashMap<>();
    private final Map<UUID, Boolean> lightningEffectTriggered = new HashMap<>();
    double lightningDamage = 1.0;

    //map teleport
    private static final int TELEPORT_DURATION = 5; // duration player stays in the air (in seconds)
    private static final int PLATFORM_SIZE = 150; // size of the platform (size x size)
    private static final int TELEPORT_UP_HEIGHT = 60; // height to teleport up
    private static final double SPEED_BOOST = 2.0; // speed while on barrier platform

    //clone
//    private static final int CLONE_DURATION = 100;

    //meteor
    private static final int METEOR_COUNT = 10; // number of meteors in a cast 10
    private static final int METEOR_DAMAGE = 6; // damage dealt by each meteor 8
    private static final double METEOR_RADIUS = 4.0; // radius for meteors 3
    private static final int METEOR_DELAY = 10; // delay between each meteor in ticks 10
    private static final int MAX_CAST_RANGE = 25; // 25
    private static final double RANDOM_SPAWN_RADIUS = 8.0; // 5
    private final HashMap<UUID, Player> projectileCasterMap = new HashMap<>();
    private Map<UUID, UUID> spellCasters = new HashMap<>();

    private boolean isIgnoredBlock(Material material) {
        // list of materials to ignore
        return material == Material.SHORT_GRASS ||
                material == Material.TALL_GRASS ||
                material == Material.DANDELION ||
                material == Material.POPPY ||
                material == Material.BLUE_ORCHID ||
                material == Material.ALLIUM ||
                material == Material.AZURE_BLUET ||
                material == Material.OXEYE_DAISY ||
                material == Material.LILY_OF_THE_VALLEY ||
                material == Material.ROSE_BUSH ||
                material == Material.PEONY;
    }
    static final double SWORD_SPEED = 1; // adjust speed as needed
    static final double SWORD_DAMAGE = 3.0; // damage dealt by sword 2 hearts
    static final int SWORD_LIFETIME = 100; // time in ticks before the sword disappears (5 seconds)
    static final double AIM_RADIUS = 2; // aim detection
    final Map<UUID, ArmorStand> activeSwords = new HashMap<>();

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getEntity();
            Player caster = projectileCasterMap.get(projectile.getUniqueId());
            if (caster != null && event.getHitEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getHitEntity();
                target.damage(5, caster); // Damage tied to the caster
            }
            projectileCasterMap.remove(projectile.getUniqueId());
        }
    }

    //event handler for entity deaths

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // fheck if entity that died is a Player
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // get last damage cause and verify if it's from another entity
            if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent lastDamageEvent = (EntityDamageByEntityEvent) player.getLastDamageCause();

                // check if the damager is a player
                if (lastDamageEvent.getDamager() instanceof Player) {
                    Player killer = (Player) lastDamageEvent.getDamager();
                    if (event instanceof PlayerDeathEvent) {
                        PlayerDeathEvent playerDeathEvent = (PlayerDeathEvent) event;
                        playerDeathEvent.setDeathMessage(player.getName() + " was slain by " + killer.getName() + "'s spell.");
                    }
                }
            }
        }
    }
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof ArmorStand) {
            ArmorStand swordStand = (ArmorStand) event.getDamager();
            UUID spellId = swordStand.getUniqueId();
            if (spellCasters.containsKey(spellId)) {
                UUID casterId = spellCasters.get(spellId);
                Player caster = Bukkit.getPlayer(casterId);

                if (caster != null && event.getEntity() instanceof Player) {
                    Player victim = (Player) event.getEntity();
                    victim.sendMessage("You were killed by " + caster.getName() + "'s spell!");
                }
            }
        }
    }



    //    void updateActionBar(Player player) {
//        ItemStack itemInHand = player.getInventory().getItemInMainHand();
//        String itemName;
//
//        // Define wand materials
//        Material[] wandMaterials = {Material.STICK, Material.BLAZE_ROD}; // Add other materials as needed
//
//        // Check if the item is not null, is of a material type, and is a wand
//        if (itemInHand != null && itemInHand.getType() != Material.AIR) {
//            for (Material wandMaterial : wandMaterials) {
//                if (itemInHand.getType() == wandMaterial) {
//                    // Get the item's custom name or fallback to the material name
//                    if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName()) {
//                        itemName = itemInHand.getItemMeta().getDisplayName();
//                    } else {
//                        itemName = itemInHand.getType().toString().replace("_", " ").toLowerCase();
//                        itemName = ChatColor.GOLD + ChatColor.BOLD.toString() + itemName.substring(0, 1).toUpperCase() + itemName.substring(1); // Capitalize first letter
//                    }
//
//                    // Create action bar message
//                    StringBuilder manaBar = new StringBuilder(itemName);
//                    TextComponent actionBar = new TextComponent(manaBar.toString());
//                    actionBar.setColor(ChatColor.DARK_PURPLE.asBungee()); // Set color
//
//                    // Send action bar message to player
//                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBar);
//                    return; // Exit method after sending the action bar
//                }
//            }
//        }
//
//        // Clear the action bar or show a default message when not holding a wand
//        TextComponent clearActionBar = new TextComponent("");
//        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, clearActionBar); // Optionally clear the action bar
//    }
    // fireball cast
    public void castFireball(Player caster) {
        Fireball fireball = caster.launchProjectile(Fireball.class);
        fireball.setYield(1); // remove block-breaking ability
        fireball.setIsIncendiary(false); // avoid setting fires
        // track the caster of this fireball
        projectileCasterMap.put(fireball.getUniqueId(), caster);
    }


    public void castLightningSpell(Player caster) {
        World world = caster.getWorld();
        Vector direction = caster.getEyeLocation().getDirection();
        Location eyeLocation = caster.getEyeLocation();

        // variable to hold target entity if found
        LivingEntity targetEntity = null;

        // line of sight
        BlockIterator blockIterator = new BlockIterator(world, eyeLocation.toVector(), direction, 0, Integer.MAX_VALUE);

        // track block hit
        Block targetBlock = null;

        // iterate over blocks in player's line of sight
        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();

            // check if there's a solid block in sight
            if (block.getType() != Material.AIR) {
                targetBlock = block; // found a block
                break; // stop checking
            }

            // check if there's an entity in line of sight
            for (Entity entity : world.getNearbyEntities(block.getLocation(), 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity && entity != caster) {
                    targetEntity = (LivingEntity) entity; // found an entity
                    break; // stop checking
                }
            }

            // if found a target entity, stop checking
            if (targetEntity != null) {
                break; // exit the blockIterator loop
            }
        }

        // determine strike location
        Location strikeLocation = (targetEntity != null) ? targetEntity.getLocation() : (targetBlock != null) ? targetBlock.getLocation() : null;

        // if valid strike location is found, show particles and schedule lightning strike
        if (strikeLocation != null) {
            // start a repeating task to show particles
            new BukkitRunnable() {
                private int count = 0;

                @Override
                public void run() {
                    // show particles in a circle
                    showLightningParticles(strikeLocation);

                    // increment count and check if we should stop
                    if (count >= 40) { // show particles for 2 seconds (40 ticks)
                        cancel();
                        return;
                    }
                    count++;
                }
            }.runTaskTimer(WizardsPlugin.getInstance(), 0L, 1L); // start immediately and repeat every tick

            // schedule task to strike lightning after a delay
            LivingEntity finalTargetEntity = targetEntity;
            Bukkit.getScheduler().runTaskLater(WizardsPlugin.getInstance(), () -> {
                if (finalTargetEntity != null) {
                    finalTargetEntity.damage(lightningDamage, caster); // damage entity directly
                    registerPlayerKill(caster, finalTargetEntity); // register damage attribution to caster
                }
                world.strikeLightning(strikeLocation); // strike lightning at determined location
            }, 30L); // 1.5 sec delay (20 ticks per second)
        }
    }

    private void showLightningParticles(Location location) {
        // create a circle of particles around location
        for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 8) { // 16 particles
            double x = Math.cos(angle) * 1; // radius of 1 block
            double z = Math.sin(angle) * 1;
            Location particleLocation = location.clone().add(x, 1, z);
            particleLocation.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, particleLocation, 1, 0, 0, 0, 1,null,true); // particle effect
        }
        location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
    }

    void registerPlayerKill(Player caster, Entity target) {
        // set last damage cause for target entity
        EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(caster, target, DamageCause.CUSTOM, 2.0);
        target.setLastDamageCause(damageEvent);
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
                player.getWorld().spawnParticle(Particle.CRIT, particleLocation, 10, 0.2, 0.2, 0.2, 0.1, null, false);

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

    private double calculateFallDamage(Entity entity, double initialHeight) {
        double currentHeight = entity.getLocation().getY();
        double fallDistance = initialHeight - currentHeight;
        if (fallDistance > 3) {
            return fallDistance - 3;
        }
        return 0.0;
    }

    void castGustSpell(UUID playerId) {
        Player caster = getPlayerById(playerId);
        double gustRadius = 5.0; // radius of gust spell
        double gustStrength = 2.0; // strength of gust spell

        // push all nearby entities
        assert caster != null;

        int particleCount = 30;
        double innerCircleRadius = 1.0;
        double outerCircleRadius = 2.0;

        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
//        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation(), 30, 1, 1, 1, 0.1);
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            // coordinates for inner circle
            double xInner = Math.cos(angle) * innerCircleRadius;
            double zInner = Math.sin(angle) * innerCircleRadius;
            Location innerParticleLocation = caster.getLocation().add(xInner, 0, zInner);
            caster.getWorld().spawnParticle(Particle.CLOUD, innerParticleLocation, 1, 0, 0, 0, 1);
        }
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            // coordinates for outer circle
            double xOuter = Math.cos(angle) * outerCircleRadius;
            double zOuter = Math.sin(angle) * outerCircleRadius;
            Location outerParticleLocation = caster.getLocation().add(xOuter, 0, zOuter);
            caster.getWorld().spawnParticle(Particle.CLOUD, outerParticleLocation, 1, 0, 0, 0, 1);
        }

        for (Entity entity : caster.getNearbyEntities(gustRadius, gustRadius, gustRadius)) {
            double initialHeight = entity.getLocation().getY();

            // calculate direction vector from caster to entity
            org.bukkit.util.Vector direction = entity.getLocation().toVector().subtract(caster.getLocation().toVector()).normalize();

            entity.setVelocity(direction.multiply(gustStrength));

            // task to check for fall damage
            new BukkitRunnable() {
                @Override
                public void run() {
                    // check if entity is still valid and has fallen
                    if (!entity.isValid() || entity.getLocation().getY() >= initialHeight) {
                        return;
                    }

                    // calculate fall damage based on how far entity has fallen
                    double fallDamage = calculateFallDamage(entity, initialHeight);

                    // tie it to the caster
                    if (fallDamage > 0 && entity instanceof LivingEntity livingEntity) {
                        livingEntity.damage(fallDamage, caster);
                    }
                }
            }.runTaskLater(WizardsPlugin.getInstance(), 1L);

        }

//        player.sendMessage( ChatColor.WHITE.toString() + ChatColor.BOLD +"Gust spell cast!");
    }

    public static void launchMinecart(Player player) {
        // create and launch minecart with player inside
        Minecart minecart = player.getWorld().spawn(player.getLocation(), Minecart.class);

        // calculate launch direction based on player's pitch and yaw
        double pitch = Math.toRadians(player.getLocation().getPitch());
        double yaw = Math.toRadians(player.getLocation().getYaw());
        Vector launchDirection = player.getEyeLocation().getDirection().normalize().multiply(5); // adjust the launch speed

        minecart.setVelocity(launchDirection);
        Location minecartLocation = player.getEyeLocation().add(0, 1, 0); // positioning it slightly above the player's eye location
        minecart.teleport(minecartLocation);

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
//            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Ground Pound spell cast!");
        }
    }
    private void groundPound(Player player) {
        var delay = player.isOnGround() ? 10L : 0; // set delay to 1 if player is on ground, otherwise set to 0
        World world = player.getWorld();
        Location initialLocation = player.getLocation();

        // particle effects at initial location
        world.spawnParticle(Particle.EXPLOSION_LARGE, initialLocation, 1, 0, 0, 0, 0, null, true);

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
                        world.spawnParticle(Particle.CLOUD, landingLocation.clone().add(0, y, 0), 1, 0, 0, 0, 0, null, true);
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

    private static final HashMap<UUID, List<Block>> barrierBlocksMap = new HashMap<>(); // store barrier blocks for each player
    private static final HashMap<UUID, List<Block>> barrierBlocksMap2 = new HashMap<>();

    Map<UUID, Boolean> playerTeleportationState = new HashMap<>();

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
    private void removeBarrierPlatform(UUID playerId) {
        List<Block> barrierBlocks = barrierBlocksMap.get(playerId);
        List<Block> barrierBlocks2 = barrierBlocksMap2.get(playerId);
        if (barrierBlocks != null) {
            for (Block block : barrierBlocks) {
                block.setType(Material.AIR); // set blocks to air
            }
            for (Block block : barrierBlocks2) {
                block.setType(Material.AIR); // set blocks to air
            }
            barrierBlocksMap.remove(playerId); // remove barriers from map
            barrierBlocksMap2.remove(playerId);
        }
    }
    private void surroundPlayerWithBarriers(Player player) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        List<Block> barrierBlocks = new ArrayList<>(); // create a list to track barrier blocks

        // create barrier blocks surrounding player
        for (int y = 0; y <= 1; y++) { // two blocks tall
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    // create barrier block only in the 1x2x1 area
                    if (x == 0 && z == 0) continue; // skip center (where player stands)
                    Block block = world.getBlockAt(playerLocation.getBlockX() + x, playerLocation.getBlockY() + y, playerLocation.getBlockZ() + z);
                    block.setType(Material.BARRIER); // set the block to barrier
                    barrierBlocks.add(block); // store the barrier block
                }
            }
        }

        // Store the barrier blocks in the barrierBlocksMap
        barrierBlocksMap2.put(player.getUniqueId(), barrierBlocks);
    }

    private void spawnTeleportParticles(Location location) {
        // track the current phase
        final boolean[] isInward = {true};

        // create a runnable task to manage the particle spawning
        new BukkitRunnable() {
            @Override
            public void run() {
                // check if the player is still at the teleport location
                Player player = Bukkit.getPlayer(location.getWorld().getNearbyEntities(location, 1, 1, 1)
                        .stream()
                        .filter(entity -> entity instanceof Player)
                        .findFirst()
                        .orElse(null).getUniqueId()
                );

                if (player != null && player.getLocation().distance(location) < 1) {
                    // fetermine particle effect
                    double radius = 1.5; // radius of  sphere effect
                    int particles = 10; // number of particles to spawn

                    if (isInward[0]) {
                        // spawn particles moving inward towards the teleport location
                        for (int i = 0; i < particles; i++) {
                            double angle = Math.random() * 2 * Math.PI; // random angle
                            double x = radius * Math.cos(angle); // x coordinate based on angle
                            double z = radius * Math.sin(angle); // z coordinate based on angle
                            double y = Math.random() * 0.5; // random height offset

                            // create particle location
                            Location particleLocation = location.clone().add(x, y, z);
                            particleLocation.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 1, 0, 0, 0, 0, null, true); // Spawn inward particle
                        }

                        // switch to outward phase after a certain time
                        if (System.currentTimeMillis() % 1000 < 50) { // timing
                            isInward[0] = false;
                        }
                    } else {
                        // spawn particles exploding outward from the teleport location
                        for (int i = 0; i < particles; i++) {
                            double angle = Math.random() * 2 * Math.PI; // random angle
                            double radiusOffset = Math.random(); // random distance from the center
                            double x = radius * radiusOffset * Math.cos(angle); // x coordinate based on angle
                            double z = radius * radiusOffset * Math.sin(angle); // z coordinate based on angle
                            double y = Math.random() * 0.5; // random height offset

                            // create particle location
                            Location particleLocation = location.clone().add(x, y, z);
                            particleLocation.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, particleLocation, 1, 0, 0, 0, 0, null, true);
                        }
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0, 1);
    }

    public void teleportPlayerUp(Player player) {
        UUID playerId = player.getUniqueId();

        // set teleportation state to true to disable spell casting
        playerTeleportationState.put(playerId, true);

        // get player's current location
        Vector playerLocation = player.getLocation().toVector();
        // create platform at player's current Y position + TELEPORT_UP_HEIGHT
        createBarrierPlatform(playerLocation.getBlockX(), playerLocation.getBlockZ(), playerLocation.getBlockY() + TELEPORT_UP_HEIGHT, player);

        // teleport player up
        player.teleport(player.getLocation().add(0, TELEPORT_UP_HEIGHT + 1, 0));
        player.setVelocity(new Vector(0, 0, 0)); // Reset velocity

        // increase speed
        player.setWalkSpeed((float) (player.getWalkSpeed() * SPEED_BOOST));

        BossBar bossBar = Bukkit.createBossBar("Move quickly!", BarColor.BLUE, BarStyle.SEGMENTED_20);
        bossBar.addPlayer(player);

        new BukkitRunnable() {
            int timeLeft = TELEPORT_DURATION;
            int soundIndex = -1;


            @Override
            public void run() {
                if (timeLeft <= 0) {
                    teleportBackDown(player);
                    bossBar.removePlayer(player);
                    cancel();
                } else {
//                    player.sendMessage("You have " + timeLeft + " seconds to move!");
                    bossBar.setProgress((double) timeLeft / TELEPORT_DURATION);
                    // progressive sound effect
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f + (soundIndex * 0.075f));
                    soundIndex++;
                    timeLeft--;
                }
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0, 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setWalkSpeed(0.2f); // reset to default walk speed
            }
        }.runTaskLater(WizardsPlugin.getInstance(), TELEPORT_DURATION * 20);
    }

    private BukkitRunnable particleRunnable; // declare a variable to hold the particle runnable

    public void teleportBackDown(Player player) {
        UUID playerId = player.getUniqueId();

        // set teleportation state to false
        playerTeleportationState.put(playerId, false);

        // get player's current position
        int playerY = player.getLocation().getBlockY();
        // get player's direction they are looking
        Vector playerDirection = player.getLocation().getDirection();

        // check blocks below player's current position for teleportation
        for (int y = playerY - 1; y >= 0; y--) {
            Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());

            // check if block is not air and not barrier
            if (block.getType() != Material.AIR && block.getType() != Material.BARRIER) {
                // teleport the player in place to prevent being stuck in blocks
                player.teleport(player.getLocation());

                // surround player with barrier blocks
                surroundPlayerWithBarriers(player);

                // create a particle effect at teleport location
                Location teleportLocation = block.getLocation().add(0, 1, 0);
                spawnTeleportParticles(teleportLocation); // spawn particles

                // create a runnable to spawn constant particles
                particleRunnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        // spawn particles at teleport location
                        teleportLocation.getWorld().spawnParticle(Particle.END_ROD, teleportLocation, 5, 0.5, 0.5, 0.5, 0.1, null, true);
                    }
                };
                particleRunnable.runTaskTimer(WizardsPlugin.getInstance(), 0, 1); // every tick (20 times per second)

                // delayed teleportation
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(teleportLocation.setDirection(playerDirection)); // teleport player to ground
                        removeBarrierPlatform(player.getUniqueId()); // remove barriers after teleport
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

                        // cancel the particle spawning runnable
                        particleRunnable.cancel();
                    }
                }.runTaskLater(WizardsPlugin.getInstance(), 20); // 20 ticks = 1 second
                return;
            }
        }
    }




    private void spawnMeteor(Player player, Location targetLocation) {
        Location meteorSpawnLocation = targetLocation.clone().add(
                ThreadLocalRandom.current().nextDouble(-5, 5), // random horizontal offset
                40, // fixed height above target
                ThreadLocalRandom.current().nextDouble(-5, 5)
        );

        Vector direction = targetLocation.toVector().subtract(meteorSpawnLocation.toVector()).normalize();

        SmallFireball meteor = player.getWorld().spawn(meteorSpawnLocation, SmallFireball.class);
        meteor.setDirection(direction);
        meteor.setYield(0); // Set explosion power to 0 to handle custom damage
        meteor.setIsIncendiary(false); // Prevent setting blocks on fire

        // Meteor sound
        meteorSpawnLocation.getWorld().playSound(meteorSpawnLocation, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.5f);

        // Schedule a crater creation when the meteor lands
        new BukkitRunnable() {
            @Override
            public void run() {
                if (meteor.isDead()) {
                    createCrater(targetLocation);
                    applyMeteorDamage(targetLocation, METEOR_DAMAGE);
                    playImpactSound(targetLocation);
                    cancel();
                }
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0, 2);
    }

    private void spawnWarningParticles(Location center) {
        new BukkitRunnable() {
            private int count = 0;
            @Override
            public void run() {
                if (count >= 100) { // show particles for 5 seconds (100 ticks)
                    cancel();
                    return;
                }
                for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / 15) {
                    double x = Math.cos(theta) * METEOR_RADIUS * 2;
                    double z = Math.sin(theta) * METEOR_RADIUS * 2;
                    Location particleLocation = center.clone().add(x, 1, z);
                    center.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, new Particle.DustOptions(Color.RED, 2) );
                }
                count++;
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0, 1); // adjust frequency
    }
    public void castMeteorShower(Player player, Location targetLocation) {
        // start particles in the target area
        spawnWarningParticles(targetLocation);

        new BukkitRunnable() {
            int meteorCount = 0;

            @Override
            public void run() {
                if (meteorCount < METEOR_COUNT) {
                    // randomize meteor's impact location around the target
                    Location randomizedLocation = getRandomizedLocation(targetLocation);
                    spawnMeteor(player, randomizedLocation);
                    meteorCount++;
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0, METEOR_DELAY);
    }
// WizardsPlugin.getInstance()

    Location getRandomizedLocation(Location targetLocation) {
        double offsetX = ThreadLocalRandom.current().nextDouble(-RANDOM_SPAWN_RADIUS, RANDOM_SPAWN_RADIUS);
        double offsetZ = ThreadLocalRandom.current().nextDouble(-RANDOM_SPAWN_RADIUS, RANDOM_SPAWN_RADIUS);
        Location randomizedLocation = targetLocation.clone().add(offsetX, 0, offsetZ);

        // find first solid block below the randomized location if it's air
        Block blockBelow = randomizedLocation.getBlock();
        while (blockBelow.getType() == Material.AIR && blockBelow.getY() > 0) {
            blockBelow = blockBelow.getRelative(0, -1, 0);
        }

        // return solid block location or original if it's air
        return (blockBelow.getType() != Material.AIR) ? blockBelow.getLocation() : targetLocation;
    }
    Location getTargetLocation(Player player) {
        // perform ray trace to find the nearest entity in player's line of sight
        RayTraceResult entityTrace = player.getWorld().rayTrace(
                player.getEyeLocation(), // start trace from player's eye level
                player.getLocation().getDirection(), // direction player is looking
                MAX_CAST_RANGE, // max distance of trace
                FluidCollisionMode.NEVER, // ignore fluids
                true, // stop at any entity
                1.0, // radius to consider for entities
                entity -> entity instanceof LivingEntity && !entity.equals(player)
        );

        // if an entity is found
        if (entityTrace != null && entityTrace.getHitEntity() != null) {
            return entityTrace.getHitEntity().getLocation();
        }

        // if no entity is found
        RayTraceResult blockTrace = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(), // start trace from player's eye level
                player.getLocation().getDirection(), // direction player is looking
                MAX_CAST_RANGE, // max distance of the trace
                FluidCollisionMode.NEVER // ignore fluids
        );

        // if a block is found
        if (blockTrace != null && blockTrace.getHitBlock() != null) {
            return blockTrace.getHitBlock().getLocation();
        }

        // if no block is found
        Location finalLocation = player.getEyeLocation().add(player.getLocation().getDirection().multiply(MAX_CAST_RANGE));

        // find first solid block below 25th block if it's air
        Block blockBelow = finalLocation.getBlock();
        while (blockBelow.getType() == Material.AIR && blockBelow.getY() > 0) {
            blockBelow = blockBelow.getRelative(0, -1, 0);
        }

        // return location of the block or null if no valid block
        return (blockBelow.getType() != Material.AIR) ? blockBelow.getLocation() : null;
    }

    private void createCrater(Location impactLocation) {
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 0; y++) {
                for (int z = -3; z <= 3; z++) {
                    Location craterLocation = impactLocation.clone().add(x, y, z);
                    double distance = craterLocation.distance(impactLocation);
                    if (distance <= METEOR_RADIUS) {
                        Block block = craterLocation.getBlock();
                        if (block.getType() != Material.BEDROCK && block.getType() != Material.WATER) {
                            block.setType(Material.AIR); // create crater by removing blocks
                        }
                    }
                }
            }
        }
        impactLocation.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, impactLocation, 1);
    }

    private void applyMeteorDamage(Location impactLocation, int damage) {
        for (Entity entity : impactLocation.getWorld().getNearbyEntities(impactLocation, 4, 4, 4)) {
//            if (entity instanceof Player) {
//                Player target = (Player) entity;
//                target.damage(damage);
//            }
            if (entity instanceof LivingEntity) {
                // apply damage to any living entity
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.damage(damage);

                // knockback effect
                Vector knockback = livingEntity.getLocation().toVector().subtract(impactLocation.toVector()).normalize();
                knockback.multiply(0.5); // knockback strength
                livingEntity.setVelocity(knockback);
            }
        }
    }

    void playImpactSound(Location location) {
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }
    
//    void createClone(Player player) {
//        // get player's current location and direction
//        Location spawnLocation = player.getLocation();
//        Vector direction = spawnLocation.getDirection().normalize();
//
//        // create clone
//        ArmorStand clone = (ArmorStand) player.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);
//        clone.setCustomNameVisible(true);
//        clone.setSilent(true); // make the clone silent
//
//        // make the clone move forward in a straight line
//        new BukkitRunnable() {
//            int ticks = 0;
//
//            @Override
//            public void run() {
//                if (ticks < CLONE_DURATION) {
//                    // move the clone forward
//                    Location currentLocation = clone.getLocation();
//                    currentLocation.add(direction);
//                    clone.teleport(currentLocation);
//                    ticks++;
//                } else {
//                    // remove the clone
//                    clone.remove();
//                    cancel();
//                }
//            }
//        }.runTaskTimer(WizardsPlugin.getInstance(), 0, 1); // run every tick
//    }


    private static final int HEAL_RADIUS = 5;     // radius of the healing circle
    private static final int HEAL_AMOUNT = 1;     // amount of health restored per heal tick
    private static final int HEAL_DURATION = 6;  // duration of healing in seconds
    private static final int HEAL_TICK_INTERVAL = 20; // heal interval (in ticks), 20 ticks = 1 second

    private void drawParticleCircle(Location center, double radius) {
        // number of particles around the circle
        int particleCount = 100; // density of circle
        double increment = (2 * Math.PI) / particleCount;

        // dust options for yellow particles
        Particle.DustOptions yellowDust = new Particle.DustOptions(Color.YELLOW, 2.0f);

        for (int i = 0; i < particleCount; i++) {
            double angle = i * increment;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            // spawn yellow particles at calculated position
            center.getWorld().spawnParticle(Particle.REDSTONE, center.clone().add(x, 1, z), 2, yellowDust);
        }
    }

    // spawns a healing circle at specified location
    void spawnHealingCircle(Player caster, Location center) {
//        caster.sendMessage("Healing Circle cast at " + center.toString() + "!");

        // display particle effect for healing circle
        center.getWorld().spawnParticle(Particle.HEART, center, 100, HEAL_RADIUS, 2, HEAL_RADIUS, 0.1);

        // create Boss Bar for players in healing radius
        BossBar healBossBar = Bukkit.createBossBar(
                "§e§lYou are being healed!",
                BarColor.GREEN,
                BarStyle.SOLID

        );
        healBossBar.setProgress(1.0);
        new BukkitRunnable() {
            int elapsedTime = 0;

            @Override
            public void run() {
                if (elapsedTime >= HEAL_DURATION) {
                    cancel();
                    healBossBar.removeAll(); // remove Boss Bar from all players
                    return;
                }
                healPlayersInCircle(center);

                // update Boss Bar for players in healing radius
                for (Entity entity : center.getWorld().getNearbyEntities(center, HEAL_RADIUS, 2, HEAL_RADIUS)) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        healBossBar.addPlayer(player);
                        double progress = 1.0 - ((double) elapsedTime / HEAL_DURATION);
                        healBossBar.setProgress(progress);
                        healBossBar.setTitle("§e§lYou are being healed! Time Remaining: " + (HEAL_DURATION - elapsedTime) + "s");
                    }
                }
                // remove players who walked out of healing radius
                for (Player player : healBossBar.getPlayers()) {
                    if (player.getLocation().distance(center) > HEAL_RADIUS) {
                        healBossBar.removePlayer(player); // Remove from Boss Bar
                    }
                }

                // particles every heal tick
                center.getWorld().spawnParticle(Particle.HEART, center, 60, HEAL_RADIUS, 1, HEAL_RADIUS, 0.1);
                drawParticleCircle(center, HEAL_RADIUS);
                elapsedTime++;
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0, HEAL_TICK_INTERVAL); // Heal every second
    }


    // heal all players within the circle
    private void healPlayersInCircle(Location center) {
        // expand the vertical range for healing by using a cuboid check
        for (Entity entity : center.getWorld().getNearbyEntities(center, HEAL_RADIUS, 2, HEAL_RADIUS)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;

                // check if player is within the vertical range (up to 2 blocks above)
                if (player.getLocation().getY() >= center.getY() - 2 && player.getLocation().getY() <= center.getY() + 2) {
                    double newHealth = Math.min(player.getHealth() + HEAL_AMOUNT, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                    player.setHealth(newHealth); // heal player without exceeding max health
//                    player.sendMessage("§l§eYou are being healed!");
                }
            }
        }
    }

    private void spawnProjectileTrail(Location location, Vector direction) {
        double spiralRadius = 0.25; // radius of the spiral around the sword
        double spiralHeight = 0.2; // height change per iteration
        int spiralTurns = 1; // number of turns in the spiral
        int particlesPerTurn = 1; // number of particles per turn

        // calculate the number of particles to spawn based on the number of turns and particles per turn
        for (int turn = 0; turn < spiralTurns; turn++) {
            for (int i = 0; i < particlesPerTurn; i++) {
                // calculate the angle for this particle
                double angle = 2 * Math.PI * (turn + (i / (double) particlesPerTurn));
                double heightOffset = turn * spiralHeight;

                // calculate the x and z coordinates for the spiral
                double xOffset = spiralRadius * Math.cos(angle);
                double zOffset = spiralRadius * Math.sin(angle);

                // create the particle location
                Location particleLocation = location.clone()
                        .add(xOffset + 0.25, heightOffset + 0.25, zOffset);

                // spawn the particle effect
                particleLocation.getWorld().spawnParticle(Particle.FLASH, particleLocation, 1, 0, 0, 0, 0.1,null,true);
            }
        }
    }
    private boolean isSolidBlock(Location location) {
        Block block = location.getBlock();
        Material blockType = block.getType();
        // return true if the block is solid and not one of the ignored types
        return blockType.isSolid() && !isIgnoredBlock(blockType);
    }
    void VoidOrbCast(Player player, UUID playerId) {
        Location spawnLocation = player.getEyeLocation().clone();
        // armor stand relative to player
        spawnLocation.setY(spawnLocation.getY() - 0.35);
        spawnLocation.setX(spawnLocation.getX() - 0.35);
        ArmorStand swordStand = (ArmorStand) player.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);

//        Vector direction1 = player.getEyeLocation().getDirection();
//
//        float yaw = (float) Math.toDegrees(Math.atan2(direction1.getZ(), direction1.getX()));
//        // normalize yaw
//        if (yaw < 0) {
//            yaw += 360;
//        }
//        float pitch = (float) Math.toDegrees(Math.asin(direction1.getY() / direction1.length()));
//        swordStand.setRotation(yaw, pitch);

        swordStand.setInvisible(true);
        swordStand.setGravity(false);
        swordStand.setMarker(true); // marker to avoid collisions
        swordStand.setSmall(false); // smaller hitbox
        swordStand.setArms(true);
//        swordStand.setItemInHand(new ItemStack(Material.SEA_LANTERN));

        // store in swords map
        activeSwords.put(playerId, swordStand);

        // calculate direction and velocity
        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(SWORD_SPEED);
        swordStand.setVelocity(direction);

        // task to move sword and handle collision detection

        new BukkitRunnable() {
            @Override
            public void run() {
                if (swordStand.isDead() || !swordStand.isValid()) {
                    cancel();
                    return;
                }

                // move armor stand in given direction
                Location currentLocation = swordStand.getLocation();
                currentLocation.add(direction);

                // check if sword hits a solid block
                if (isSolidBlock(currentLocation)) {
                    swordStand.remove();
                    activeSwords.remove(playerId);
                    cancel();
                    return;
                }

                // teleport sword to updated location
                swordStand.teleport(currentLocation);

                // play a sound effect at the current location of the armor stand
                currentLocation.getWorld().playSound(currentLocation, Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.0f);

                // spawn particles for trail
                spawnProjectileTrail(currentLocation, direction);

                // get location of sword's hit area
                Location swordTipLocation = currentLocation.clone().add(direction.clone().multiply(0.5));

                // check for nearby entities to detect a hit using held item's location
                for (Entity entity : swordTipLocation.getWorld().getNearbyEntities(swordTipLocation, AIM_RADIUS, AIM_RADIUS, AIM_RADIUS)) {
                    if (entity != player && entity != swordStand && entity instanceof LivingEntity) {

                        // line-of-sight check to ensure no wall is between sword and entity
                        RayTraceResult rayTraceResult = swordTipLocation.getWorld().rayTraceBlocks(
                                swordTipLocation,
                                entity.getLocation().toVector().subtract(swordTipLocation.toVector()).normalize(),
                                swordTipLocation.distance(entity.getLocation())
                        );

                        // if obstruction found, skip damaging the entity
                        if (rayTraceResult != null && rayTraceResult.getHitBlock() != null) {
                            continue;
                        }

                        // damage first entity hit and remove sword
                        ((LivingEntity) entity).damage(SWORD_DAMAGE, player);
                        swordStand.remove();
                        activeSwords.remove(playerId);
                        cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0L, 1L);


        // remove sword after defined lifetime
        Bukkit.getScheduler().runTaskLater(WizardsPlugin.getInstance(), () -> {
            if (swordStand.isValid()) {
                swordStand.remove();
                activeSwords.remove(playerId);
            }
        }, SWORD_LIFETIME);
        spellCasters.put(swordStand.getUniqueId(), playerId);
    }



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
