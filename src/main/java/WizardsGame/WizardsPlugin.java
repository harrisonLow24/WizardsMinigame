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

public class WizardsPlugin extends JavaPlugin implements Listener {
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

}