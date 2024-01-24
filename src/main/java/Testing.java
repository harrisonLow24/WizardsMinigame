import WizardsGame.CooldownManager;
import WizardsGame.SpellCastingManager;
import WizardsGame.TeleportationManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Testing extends JavaPlugin implements Listener {
    SpellCastingManager Cast = new SpellCastingManager();
    CooldownManager Cooldown = new CooldownManager();
    TeleportationManager Teleport = new TeleportationManager();


    @Override
    public void onEnable() {
        getLogger().info("WizardsPlugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("WizardsPlugin has been disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Give a default wand to players on join
        Player player = event.getPlayer();
        player.getInventory().addItem(new ItemStack(Material.BLAZE_ROD));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack wand = player.getInventory().getItemInMainHand();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Check if the player used a wand
            if (wand.getType() == Material.IRON_PICKAXE) {
                // Lightning spell
                if (!isOnLightningCooldown(player)) {
                    castLightningSpell(player);

                    setLightningCooldown(player);
                } else {
                    // Player is on teleportation cooldown
                    int remainingSeconds = getRemainingLightningCooldownSeconds(player);
                    player.sendMessage(ChatColor.RED + "Lightning on cooldownb. Please wait " + remainingSeconds + " seconds.");
                }

            }
            if (wand.getType() == Material.IRON_SWORD) {
                // Teleportation spell
                if (!isOnTeleportCooldown(player)) {
                    // Implement teleportation abilities
                    teleportSpell(player);

                    // Set the teleportation cooldown for the player
                    setTeleportCooldown(player);
                } else {
                    // Player is on teleportation cooldown
                    int remainingSeconds = getRemainingTeleportCooldownSeconds(player);
                    player.sendMessage(ChatColor.RED + "Teleportation on  Please wait " + remainingSeconds + " seconds.");
                }
            }
            if (wand.getType() == Material.BLAZE_ROD) {
                // Fireball spell
                if (!isOnFireballCooldown(player)) {
                    // Implement fireball abilities
                    castFireball(player);

                    // Set the fireball cooldown for the player
                    setFireballCooldown(player);
                } else {
                    // Player is on fireball cooldown
                    int remainingSeconds = getRemainingFireballCooldownSeconds(player);
                    player.sendMessage(ChatColor.RED + "Fireball on  Please wait " + remainingSeconds + " seconds before casting again.");
                }
            }
        }
    }
    // fireball cast
    void castFireball(Player player) {
        double speed = 1;
        Vector direction = player.getLocation().getDirection().multiply(speed);
        player.launchProjectile(org.bukkit.entity.Fireball.class, direction);
        player.sendMessage(ChatColor.GREEN + "You cast the Fireball spell!");
    }
    ///using a fireball as the lightning strike spot
//    private void castLightningSpell(Player player) {
//        double projectileSpeed = 0.5; // Adjust the speed as needed
//        Vector direction = player.getLocation().getDirection().multiply(projectileSpeed);
//        player.launchProjectile(org.bukkit.entity.Fireball.class, direction);
//    }

//    @EventHandler
//    public void onProjectileHit(ProjectileHitEvent event) {
//        if (event.getEntity() instanceof Fireball) {
//            Fireball fireball = (Fireball) event.getEntity();
//            Location hitLocation = fireball.getLocation();
//
//            // Perform lightning strike at the hit location
//            strikeLightning(hitLocation);
//        }
//    }

    void strikeLightning(Location location) {
        location.getWorld().strikeLightning(location); // Summon lightning at the location
    }

    void castLightningSpell(Player player) {
        double particleDistance = 1000;
        Vector direction = player.getLocation().getDirection().multiply(particleDistance);
        Location destination = player.getLocation().add(direction);

        // Find the first block in the spell's path
        Location blockLocation = findSolidBlockInPath(player.getLocation().add(0, 1.5, 0), destination.add(0, 1, 0));

        // if a solid block is found, strike lightning where particle ends
        if (blockLocation != null) {
            destination = blockLocation;
            spawnAndMoveParticleTrail(player.getLocation().add(0, 1.5, 0), destination.add(0, 1, 0)); // particle adjustment to look better
            strikeLightning(destination);
        }
    }
    private void spawnAndMoveParticleTrail(Location startLocation, Location endLocation) {
        int particleCount = 100; // number of generated trail particles
        Vector direction = endLocation.toVector().subtract(startLocation.toVector()).normalize();
        double distanceBetweenParticles = startLocation.distance(endLocation) / particleCount;

        for (int i = 0; i < particleCount; i++) {
            Location particleLocation = startLocation.clone().add(direction.clone().multiply(distanceBetweenParticles * i));
            startLocation.getWorld().spawnParticle(Particle.CRIT, particleLocation, 1, 0, 0, 0, 0);
        }
    }
    private Location findSolidBlockInPath(Location startLocation, Location endLocation) {
        // Check for the first non-air block in spell's path
        RayTraceResult result = startLocation.getWorld().rayTraceBlocks(startLocation, endLocation.toVector().subtract(startLocation.toVector()).normalize(),
                startLocation.distance(endLocation), FluidCollisionMode.NEVER, true);

        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlock().getLocation(); //returns location of block in spells path
        }

        return null; // No solid block found
    }


    void teleportSpell(Player player) {

        double teleportDistance = 10.0; // Set the teleportation distance in blocks
        Vector direction = player.getLocation().getDirection().multiply(teleportDistance);
        Location destination = player.getLocation().add(direction);
        Location safeLocation = findSafeLocation(player.getLocation(), destination);// find nearest safe teleportation location
        playTeleportSound(safeLocation);// teleportation sound effect
        player.teleport(safeLocation); // teleport player to safe location
        player.sendMessage(ChatColor.BLUE + "You cast the Teleportation spell!");
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

        return startLocation; // if no safe location is found, return the original starting location
    }

    // store cooldowns in hashmaps
    private final Map<Player, Long> fireballCooldowns = new HashMap<>();
    private final Map<Player, Long> teleportCooldowns = new HashMap<>();
    private final Map<Player, Long> lightningCooldowns = new HashMap<>();


    // cooldown duration in milliseconds
    private final long fireballCooldownDuration = 1 * 1000; //10
    private final long teleportCooldownDuration = 1 * 1000; //15
    private final long lightningCooldownDuration = 1 * 1000; //15


    // Returns the remaining cooldown left
    int getRemainingFireballCooldownSeconds(Player player) {
        // remaining fireball cooldown
        long remainingCooldown = fireballCooldownDuration - (System.currentTimeMillis() - fireballCooldowns.getOrDefault(player, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingTeleportCooldownSeconds(Player player) {
        // remaining teleportation cooldown
        long remainingCooldown = teleportCooldownDuration - (System.currentTimeMillis() - teleportCooldowns.getOrDefault(player, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingLightningCooldownSeconds(Player player) {
        // remaining teleportation cooldown
        long remainingCooldown = lightningCooldownDuration - (System.currentTimeMillis() - lightningCooldowns.getOrDefault(player, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }

    // Check if spells are on cooldowm
    boolean isOnFireballCooldown(Player player) {
        // Check if player is on fireball cooldown
        return fireballCooldowns.containsKey(player) && System.currentTimeMillis() - fireballCooldowns.get(player) < fireballCooldownDuration;
    }
    boolean isOnTeleportCooldown(Player player) {
        // Check if player is on teleportation cooldown
        return teleportCooldowns.containsKey(player) && System.currentTimeMillis() - teleportCooldowns.get(player) < teleportCooldownDuration;
    }
    boolean isOnLightningCooldown(Player player) {
        return lightningCooldowns.containsKey(player) && System.currentTimeMillis() - lightningCooldowns.get(player) < lightningCooldownDuration;
    }


    // Sets the cooldown of spells
    void setFireballCooldown(Player player) {
        fireballCooldowns.put(player, System.currentTimeMillis());
    }
    void setTeleportCooldown(Player player) {
        teleportCooldowns.put(player, System.currentTimeMillis());
    }
    void setLightningCooldown(Player player) {
        lightningCooldowns.put(player, System.currentTimeMillis());
    }
}