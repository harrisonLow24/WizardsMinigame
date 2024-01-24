package WizardsGame;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class WizardsPlugin extends JavaPlugin implements Listener {
    SpellCastingManager Cast = new SpellCastingManager();
    CooldownManager Cooldown = new CooldownManager();
    TeleportationManager Teleport = new TeleportationManager();

//    private final Map<UUID, Long> manaLastUpdate = new HashMap<>();
    private final Map<UUID, Double> playerMana = new HashMap<>();
    private final double maxMana = 100.0;
//    private final double manaRegenRate = 1.0;
    final Map<UUID, Double> spellManaCost = new HashMap<>();

    private final Map<UUID, BossBar> manaBossBars = new HashMap<>();
    @Override
    public void onEnable() {
        getLogger().info("WizardsPlugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);
//        getServer().getScheduler().runTaskTimer(this, this::regenerateMana, 0, 20); // Run mana regeneration task every second
        // Schedule a task to update the mana action bar every second
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                updateManaActionBar(onlinePlayer);
            }
        }, 0, 20); // Run the task every second (20 ticks)
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
        manaBossBars.remove(playerId);

    }

    public void setSpellManaCost(UUID playerId, double spellCost) {
        spellManaCost.put(playerId, spellCost);
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ItemStack wand = player.getInventory().getItemInMainHand();

//        double fireballCost = spellManaCost.getOrDefault(playerId, 10.0); // mana cost
//        double teleportCost = spellManaCost.getOrDefault(playerId, 15.0); // mana cost
//        double lightningCost = spellManaCost.getOrDefault(playerId, 15.0); // mana cost
        double fireballCost = 15.0;
        double teleportCost = 20.0;
        double lightningCost = 5.0;


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

//                        updateManaActionBar(player);
//                        player.sendMessage("You have " + getCurrentMana(playerId) + " mana remaining");

                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Fireball.");
                    }
                }else{
                    // player is on fireball cooldown
                    int remainingSeconds = Cooldown.getRemainingFireballCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Fireball on cooldown. Please wait " + remainingSeconds + " seconds before casting again.");
                }
            }
//            if (wand.getType() == Material.BLAZE_ROD) {
//                double spellCost = spellManaCost.getOrDefault(player, 10.0); //mana cost
//                // fireball spell
//
//                if (!Cooldown.isOnFireballCooldown(player)) {
//                    // Implement fireball abilities
//
//                    Cast.castFireball(player);
//
//                    // Set the fireball cooldown for the player
//                    Cooldown.setFireballCooldown(player);
//                } else {
//                    // player is on fireball cooldown
//                    int remainingSeconds = Cooldown.getRemainingFireballCooldownSeconds(player);
//                    player.sendMessage(ChatColor.RED + "Fireball on cooldown. Please wait " + remainingSeconds + " seconds before casting again.");
//                }
//            }
            if (wand.getType() == Material.IRON_SWORD) {
                // teleportation spell
                if (!Cooldown.isOnTeleportCooldown(playerId)) {
                    // teleport
                    if (hasEnoughMana(playerId, teleportCost)){
                        Teleport.teleportSpell(playerId); // Set the teleportation cooldown for the player
                        Cooldown.setTeleportCooldown(playerId);
                        deductMana(playerId, teleportCost);
//                        updateManaActionBar(player);
//                        player.sendMessage("You have " + getCurrentMana(playerId) + " mana remaining");
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
//                        updateManaActionBar(player);
//                        player.sendMessage("You have " + getCurrentMana(playerId) + " mana remaining");
                    }else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Teleport.");
                    }
                } else {
                    // Player is on teleportation cooldown
                    int remainingSeconds = Cooldown.getRemainingLightningCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Lightning on cooldown. Please wait " + remainingSeconds + " seconds.");
                }

            }
        }
    }
    // check if a player has enough mana
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

//    public void updateManaActionBar(Player player) {
//        double currentMana = getCurrentMana(player.getUniqueId());
//        int manaPercentage = (int) ((currentMana / maxMana) * 100);
//        String actionBarMessage = ChatColor.BLUE + "Mana: " + manaPercentage + "%";
//
//        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarMessage));
//    }

    public void updateManaActionBar(Player player) {
        UUID playerId = player.getUniqueId();
        double currentMana = getCurrentMana(playerId);
        double manaPercentage = currentMana / maxMana;

        BossBar bossBar = manaBossBars.computeIfAbsent(playerId, k -> Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID));
        bossBar.setTitle(ChatColor.YELLOW + "Mana: " + (int) (manaPercentage * 100) + "%");
        bossBar.setProgress(manaPercentage);
        bossBar.addPlayer(player);
    }



}