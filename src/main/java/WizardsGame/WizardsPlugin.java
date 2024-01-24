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

public class WizardsPlugin extends JavaPlugin implements Listener {
    SpellCastingManager Cast = new SpellCastingManager();
    CooldownManager Cooldown = new CooldownManager();
    TeleportationManager Teleport = new TeleportationManager();
//    Mana Mana = new Mana();


private final Map<Player, Long> manaLastUpdate = new HashMap<>();
    private final Map<Player, Double> playerMana = new HashMap<>();
    private final double maxMana = 100.0;
    private final double manaRegenRate = 1.0;
    final Map<Player, Double> spellManaCost = new HashMap<>(); // Map to store mana costs for each spell

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
        player.getInventory().addItem(new ItemStack(Material.BLAZE_ROD));
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
    }






    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack wand = player.getInventory().getItemInMainHand();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Check if the player used a wand

            if (wand.getType() == Material.BLAZE_ROD) {
                double spellCost = spellManaCost.getOrDefault(player, 10.0); //mana cost
                // Fireball spell

                if (!Cooldown.isOnFireballCooldown(player)) {
                    // Implement fireball abilities
                    if (hasEnoughMana(player, spellCost)) {
                        Cast.castFireball(player);
                        Cooldown.setFireballCooldown(player);
                        deductMana(player, spellCost);
                        player.sendMessage("You have " + playerMana + " mana remaining");
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Fireball.");
                    }
                }
            } else{
                // Player is on fireball cooldown
                int remainingSeconds = Cooldown.getRemainingFireballCooldownSeconds(player);
                player.sendMessage(ChatColor.RED + "Fireball on cooldown. Please wait " + remainingSeconds + " seconds before casting again.");
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
                if (!Cooldown.isOnTeleportCooldown(player)) {
                    // Implement teleportation abilities
                    Teleport.teleportSpell(player);

                    // Set the teleportation cooldown for the player
                    Cooldown.setTeleportCooldown(player);
                } else {
                    // Player is on teleportation cooldown
                    int remainingSeconds = Cooldown.getRemainingTeleportCooldownSeconds(player);
                    player.sendMessage(ChatColor.RED + "Teleportation on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
            if (wand.getType() == Material.IRON_PICKAXE) {
                // Lightning spell
                if (!Cooldown.isOnLightningCooldown(player)) {
                    Cast.castLightningSpell(player);

                    Cooldown.setLightningCooldown(player);
                } else {
                    // Player is on teleportation cooldown
                    int remainingSeconds = Cooldown.getRemainingLightningCooldownSeconds(player);
                    player.sendMessage(ChatColor.RED + "Lightning on cooldownb. Please wait " + remainingSeconds + " seconds.");
                }

            }
        }
    }
    // Method to check if a player has enough mana for a spell
    private boolean hasEnoughMana(Player player, double spellCost) {
        double currentMana = playerMana.getOrDefault(player, maxMana);
        return currentMana >= spellCost;
    }
    // Method to deduct mana for casting a spell
    private void deductMana(Player player, double spellCost) {
        double currentMana = playerMana.getOrDefault(player, maxMana);
        double newMana = Math.max(currentMana - spellCost, 0);
        playerMana.put(player, newMana);
    }
}