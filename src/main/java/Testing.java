package WizardsGame;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class Testing extends JavaPlugin implements Listener {
    SpellCastingManager Cast = new SpellCastingManager();
    CooldownManager Cooldown = new CooldownManager();
    TeleportationManager Teleport = new TeleportationManager();

    //    private final Map<UUID, Long> manaLastUpdate = new HashMap<>();
    private final Map<UUID, Double> playerMana = new HashMap<>();
    private final double maxMana = 100.0;
    //    private final double manaRegenRate = 1.0;
    final Map<UUID, Double> spellManaCost = new HashMap<>();
    @Override
    public void onEnable() {
        getLogger().info("WizardsPlugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);
//        getServer().getScheduler().runTaskTimer(this, this::regenerateMana, 0, 20); // Run mana regeneration task every second
    }

    @Override
    public void onDisable() {
        getLogger().info("WizardsPlugin has been disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        player.sendMessage("Welcome!");
        player.getInventory().addItem(new ItemStack(Material.BLAZE_ROD));
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
        // Initialize player-specific data on join
        playerMana.put(playerId, maxMana);
        spellManaCost.put(playerId, 10.0); // Set default spell mana cost
    }




    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ItemStack wand = player.getInventory().getItemInMainHand();

        double fireballCost = spellManaCost.getOrDefault(playerId, 10.0); // mana cost
        double teleportCost = spellManaCost.getOrDefault(playerId, 15.0); // mana cost
        double lightningCost = spellManaCost.getOrDefault(playerId, 15.0); // mana cost


        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Check if the player used a wandz
            if (wand.getType() == Material.BLAZE_ROD) {
                // Fireball spell
                if (!Cooldown.isOnFireballCooldown(playerId)) {
                    // Implement fireball abilities
                    if (hasEnoughMana(playerId, fireballCost)) {

                        Cast.castFireball(playerId);
                        Cooldown.setFireballCooldown(playerId);
                        deductMana(playerId, fireballCost);
                        player.sendMessage("You have " + getCurrentMana(playerId) + " mana remaining");

                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Fireball.");
                    }
                }else{
                    // Player is on fireball cooldown
                    int remainingSeconds = Cooldown.getRemainingFireballCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Fireball on cooldown. Please wait " + remainingSeconds + " seconds before casting again.");
                }
            }
//            if (wand.getType() == Material.BLAZE_ROD) {
//                double spellCost = spellManaCost.getOrDefault(player, 10.0); //mana cost
//                // Fireball spell
//
//                if (!Cooldown.isOnFireballCooldown(player)) {
//                    // Implement fireball abilities
//
//                    Cast.castFireball(player);
//
//                    // Set the fireball cooldown for the player
//                    Cooldown.setFireballCooldown(player);
//                } else {
//                    // Player is on fireball cooldown
//                    int remainingSeconds = Cooldown.getRemainingFireballCooldownSeconds(player);
//                    player.sendMessage(ChatColor.RED + "Fireball on cooldown. Please wait " + remainingSeconds + " seconds before casting again.");
//                }
//            }
            if (wand.getType() == Material.IRON_SWORD) {
                // Teleportation spell
                if (!Cooldown.isOnTeleportCooldown(playerId)) {
                    // Implement teleportation abilities
                    if (hasEnoughMana(playerId, teleportCost)){
                        Teleport.teleportSpell(playerId); // Set the teleportation cooldown for the player
                        Cooldown.setTeleportCooldown(playerId);
                        deductMana(playerId, teleportCost);
                        player.sendMessage("You have " + getCurrentMana(playerId) + " mana remaining");
                    }else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Teleport.");
                    }
                } else {
                    // Player is on teleport cooldown
                    int remainingSeconds = Cooldown.getRemainingTeleportCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Teleportation on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
            if (wand.getType() == Material.IRON_PICKAXE) {
                // Lightning spell
                if (!Cooldown.isOnLightningCooldown(playerId)) {
                    if (hasEnoughMana(playerId, lightningCost)) {
                        Cast.castLightningSpell(playerId);
                        Cooldown.setLightningCooldown(playerId);
                        deductMana(playerId, lightningCost);
                        player.sendMessage("You have " + getCurrentMana(playerId) + " mana remaining");
                    }
                } else {
                    // Player is on teleportation cooldown
                    int remainingSeconds = Cooldown.getRemainingLightningCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Lightning on cooldown. Please wait " + remainingSeconds + " seconds.");
                }

            }
        }
    }
    // Method to check if a player has enough mana for a spell
    public boolean hasEnoughMana(UUID playerId, double spellCost) {
        double currentMana = playerMana.getOrDefault(playerId, maxMana);
        return currentMana >= spellCost;
    }
    // Method to deduct mana for casting a spell
    private void deductMana(UUID playerId, double spellCost) {
        double currentMana = playerMana.getOrDefault(playerId, maxMana);
        double newMana = Math.max(currentMana - spellCost, 0);
        playerMana.put(playerId, newMana);
    }
    public double getCurrentMana(UUID playerId) {
        return playerMana.getOrDefault(playerId, maxMana);
    }
    public static Player getPlayerById(UUID playerId) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(playerId)) {
                return onlinePlayer;
            }
        }
        return null; // Player with the specified UUID not found
    }

    // teleportation
    void teleportSpell(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            // Player not online or not found, handle accordingly
            return;
        }

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
    // fireball cast
    void castFireball(UUID playerId) {
        Player player = WizardsPlugin.getPlayerById(playerId);
        if (player != null) {
            double speed = 1;
            Vector direction = player.getLocation().getDirection().multiply(speed);
            player.launchProjectile(org.bukkit.entity.Fireball.class, direction);
            player.sendMessage(ChatColor.GREEN + "You cast the Fireball spell!");
        }
    }


    void castLightningSpell(UUID playerId) {
        Player player = WizardsPlugin.getPlayerById(playerId);
        if (player != null) {
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
    }

    // Adjusted methods to use UUID instead of Player
    private void strikeLightning(Location location) {
        location.getWorld().strikeLightning(location); // Summon lightning at the location
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

    // store cooldowns in hashmaps
    private final Map<UUID, Long> fireballCooldowns = new HashMap<>();
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private final Map<UUID, Long> lightningCooldowns = new HashMap<>();

    // cooldown duration in milliseconds
    private final long fireballCooldownDuration = 1 * 1000; //10
    private final long teleportCooldownDuration = 1 * 1000; //15
    private final long lightningCooldownDuration = 1 * 1000; //15

    // Returns the remaining cooldown left
    int getRemainingFireballCooldownSeconds(UUID playerId) {
        // remaining fireball cooldown
        long remainingCooldown = fireballCooldownDuration - (System.currentTimeMillis() - fireballCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }

    int getRemainingTeleportCooldownSeconds(UUID playerId) {
        // remaining teleportation cooldown
        long remainingCooldown = teleportCooldownDuration - (System.currentTimeMillis() - teleportCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }

    int getRemainingLightningCooldownSeconds(UUID playerId) {
        // remaining teleportation cooldown
        long remainingCooldown = lightningCooldownDuration - (System.currentTimeMillis() - lightningCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }

    // Check if spells are on cooldown
    boolean isOnFireballCooldown(UUID playerId) {
        // Check if player is on fireball cooldown
        return fireballCooldowns.containsKey(playerId) && System.currentTimeMillis() - fireballCooldowns.get(playerId) < fireballCooldownDuration;
    }

    boolean isOnTeleportCooldown(UUID playerId) {
        // Check if player is on teleportation cooldown
        return teleportCooldowns.containsKey(playerId) && System.currentTimeMillis() - teleportCooldowns.get(playerId) < teleportCooldownDuration;
    }

    boolean isOnLightningCooldown(UUID playerId) {
        return lightningCooldowns.containsKey(playerId) && System.currentTimeMillis() - lightningCooldowns.get(playerId) < lightningCooldownDuration;
    }

    // Sets the cooldown of spells
    void setFireballCooldown(UUID playerId) {
        fireballCooldowns.put(playerId, System.currentTimeMillis());
    }

    void setTeleportCooldown(UUID playerId) {
        teleportCooldowns.put(playerId, System.currentTimeMillis());
    }

    void setLightningCooldown(UUID playerId) {
        lightningCooldowns.put(playerId, System.currentTimeMillis());
    }

}