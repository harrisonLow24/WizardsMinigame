package WizardsGame;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class WizardsPlugin extends JavaPlugin implements Listener {
    SpellCastingManager Cast = new SpellCastingManager();
    CooldownManager Cooldown = new CooldownManager();
    TeleportationManager Teleport = new TeleportationManager();
    private final Map<UUID, Integer> lightningEffectDuration = new HashMap<>();

    private final Map<UUID, BossBar> manaBossBars = new HashMap<>(); // hashmap of all players' mana bars
    private final Map<UUID, Double> playerMana = new HashMap<>(); // hashmap of all players' current mana



    private final Map<UUID, Boolean> infiniteManaMap = new HashMap<>();
    private final Map<UUID, Boolean> cooldownsDisabledMap = new HashMap<>();

    final Map<UUID, Double> spellManaCost = new HashMap<>(); // hashmap of all spells' mana costs
    private final double maxMana = 100.0; // mana pool

    @Override
    public void onEnable() {
        getLogger().info("WizardsPlugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);

        // commands
        Objects.requireNonNull(getCommand("toggleinfinitemana")).setExecutor(new WizardCommands(this));
        Objects.requireNonNull(getCommand("togglecooldowns")).setExecutor(new WizardCommands(this));


        // mana bar updated every 20 ticks / 1 second

        // note to self: may remove this scheduler, as the proceeding one updates the bar every second
        // however, this makes sure the mana bar will ALWAYS update when a spell is used for consistency

        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                updateManaActionBar(onlinePlayer);
            }
        }, 0, 20);

        // mana bar updated every 20 ticks / 1 second for MANA REGEN
        getServer().getScheduler().runTaskTimer(this, this::regenerateMana, 0, 20); // Run mana regeneration task every second
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                updateManaActionBar(onlinePlayer);
            }
        }, 0, 20);
    }

    @Override
    public void onDisable() {
        getLogger().info("WizardsPlugin has been disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // get player and UUID
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        player.sendMessage("Welcome!");

        // give all players all the "wands"
        player.getInventory().addItem(new ItemStack(Material.BLAZE_ROD));
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));

        //set players' mana to max on join
        playerMana.put(playerId, maxMana);
        manaBossBars.remove(playerId);

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
        double teleportCost = 10.0;
        double lightningCost = 20.0;
        double gustCost = 25.0;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            // fireball cast
            if (wand.getType() == Material.BLAZE_ROD) {

                if (!Cooldown.isOnFireballCooldown(playerId)) { // if fireball is not on cooldown
                    if (hasEnoughMana(playerId, fireballCost)) { // AND if player has enough mana
                        Cast.castFireball(playerId);            // fireball is cast, and a cooldown + mana reduction is set
                        Cooldown.setFireballCooldown(playerId);
                        deductMana(playerId, fireballCost);
//                        updateManaActionBar(player); // old mana bar update before scheduler
//                        player.sendMessage("You have " + getCurrentMana(playerId) + " mana remaining"); // old mana bar before action bar
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Fireball.");
                    }
                }else{
                    int remainingSeconds = Cooldown.getRemainingFireballCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Fireball on cooldown. Please wait " + remainingSeconds + " seconds before casting again.");
                }
            }
            if (wand.getType() == Material.IRON_SWORD) {
                // teleport cast
                if (!Cooldown.isOnTeleportCooldown(playerId)) { // if teleport is not on cooldown
                    if (hasEnoughMana(playerId, teleportCost)){ // AND if player has enough mana
                        Teleport.teleportSpell(playerId);       // teleport is cast, and a cooldown + mana reduction is set
                        Cooldown.setTeleportCooldown(playerId);
                        deductMana(playerId, teleportCost);
                    }else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Teleport.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingTeleportCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Teleportation on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
            if (wand.getType() == Material.IRON_PICKAXE) {
                // lightning cast
                if (!Cooldown.isOnLightningCooldown(playerId)) { // if teleport is not on cooldown
                    if (hasEnoughMana(playerId, lightningCost)) { // AND if player has enough mana
                        Cast.castLightningSpell(playerId);      // lightning is cast, and a cooldown + mana reduction is set
                        Cooldown.setLightningCooldown(playerId);
                        deductMana(playerId, lightningCost);
                        startLightningEffect(playerId);
                    }else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Lightning.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingLightningCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Lightning on cooldown. Please wait " + remainingSeconds + " seconds.");
                }

            }
            if (wand.getType() == Material.FEATHER) {
                if (!Cooldown.isOnGustCooldown(playerId)) {  // Check if gust spell is not on cooldown
                    if (hasEnoughMana(playerId, gustCost)) { // AND if player has enough mana
                        Cast.castGustSpell(player);          // gust is cast, and a cooldown + mana reduction is set
                        Cooldown.setGustCooldown(playerId);
                        deductMana(playerId, gustCost);
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Gust.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingGustCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Gust spell on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
        }
    }
    // check if player has enough mana
    public boolean hasEnoughMana(UUID playerId, double spellCost) {
        double currentMana = playerMana.getOrDefault(playerId, maxMana);
        return currentMana >= spellCost;
    }
    // deduct mana for spell cast
    private void deductMana(UUID playerId, double spellCost) {
        double currentMana = playerMana.getOrDefault(playerId, maxMana);
        double newMana = Math.max(currentMana - spellCost, 0);
        playerMana.put(playerId, newMana);
    }
    // get current mana value
    public double getCurrentMana(UUID playerId) {
        return playerMana.getOrDefault(playerId, maxMana);
    }
    // get UUID of player
    public static Player getPlayerById(UUID playerId) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(playerId)) {
                return onlinePlayer;
            }
        }
        return null; // player with the specified UUID not found
    }
    // action boss bar to display mana %
    public void updateManaActionBar(Player player) {
        UUID playerId = player.getUniqueId();
        double currentMana = getCurrentMana(playerId);
        double manaPercentage = currentMana / maxMana;

        BossBar bossBar = manaBossBars.computeIfAbsent(playerId, k -> Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID));
        bossBar.setTitle(ChatColor.YELLOW + "Mana: " + (int) (manaPercentage * 100) + "%");
        bossBar.setProgress(manaPercentage);
        bossBar.addPlayer(player);
    }
    // regenerate mana over time
    private void regenerateMana() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            UUID playerId = onlinePlayer.getUniqueId();
            double currentMana = getCurrentMana(playerId);
            if (hasInfiniteMana(playerId)) {
                // Set the player's mana to the maximum value
                setPlayerMana(playerId, maxMana);
            }else {
                // regen rate
                double manaRegenRate = 5.0;
                double newMana = Math.min(currentMana + manaRegenRate, maxMana);
                playerMana.put(playerId, newMana);
            }
        }
    }

    private void startLightningEffect(UUID playerId) {
        // Set the initial duration
        int maxLightningEffectDuration = 100;
        lightningEffectDuration.put(playerId, maxLightningEffectDuration);

        // Schedule a task to decrease the duration every tick
        getServer().getScheduler().runTaskTimer(this, () -> {
            int remainingDuration = lightningEffectDuration.getOrDefault(playerId, 0);

            if (remainingDuration <= 0) {
                // Remove the player's UUID from the map when the duration is over
                lightningEffectDuration.remove(playerId);
                return;
            }

            // Simulate the longer-lasting lightning effect here
            // You can implement your custom lightning effect logic

            // Decrease the remaining duration
            lightningEffectDuration.put(playerId, remainingDuration - 1);

        }, 0, 1);
    }
    public void toggleInfiniteMana(UUID playerId) {
        boolean currentStatus = infiniteManaMap.getOrDefault(playerId, false);
        infiniteManaMap.put(playerId, !currentStatus);

        // If infinite mana is toggled, set the player's mana to the maximum value
        if (hasInfiniteMana(playerId)) {
            setPlayerMana(playerId, maxMana);
        }
    }

    public boolean hasInfiniteMana(UUID playerId) {
        return infiniteManaMap.getOrDefault(playerId, false);
    }
    public void toggleCooldowns(UUID playerId) {
        boolean currentStatus = cooldownsDisabledMap.getOrDefault(playerId, false);
        cooldownsDisabledMap.put(playerId, !currentStatus);

        // If cooldowns are disabled, clear existing cooldowns for the player
        if (hasCooldownsDisabled(playerId)) {
            Cooldown.clearCooldowns(playerId);
        }
    }

    public boolean hasCooldownsDisabled(UUID playerId) {
        return cooldownsDisabledMap.getOrDefault(playerId, false);
    }
    public void setPlayerMana(UUID playerId, double newMana) {
        // Set the mana for the specified player
        playerMana.put(playerId, Math.min(newMana, maxMana));

        // Update the mana display for the player
        Player player = getPlayerById(playerId);
        if (player != null) {
            updateManaActionBar(player);
        }
    }
}