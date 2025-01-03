package WizardsGame;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ComponentBuilder; // Import for action bar
import net.md_5.bungee.api.chat.TextComponent; // Import for action bar
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManaManager {
    public final Map<UUID, Double> playerMana = new HashMap<>(); // hashmap of all players' current mana
    public final Map<UUID, Boolean> infiniteManaMap = new HashMap<>();
    public final Map<UUID, BossBar> manaBossBars = new HashMap<>(); // hashmap of all players' mana bars
    SpellCastingManager Cast = new SpellCastingManager();
    CooldownManager Cooldown = new CooldownManager();
    public final double maxMana = 100.0; // mana pool


    // check if player has enough mana
    public boolean hasEnoughMana(UUID playerId, double spellCost) {
        double currentMana = playerMana.getOrDefault(playerId, maxMana);
        return currentMana >= spellCost || hasInfiniteMana(playerId);
    }

    // deduct mana for spell cast
    public void deductMana(UUID playerId, double spellCost) {
        if (!hasInfiniteMana(playerId)) {
            double currentMana = playerMana.getOrDefault(playerId, maxMana);
            double newMana = Math.max(currentMana - spellCost, 0);
            playerMana.put(playerId, newMana);

            // update mana display for the player
            Player player = WizardsPlugin.getPlayerById(playerId);
            if (player != null) {
                updateManaActionBar(player);
            }
        }
    }
    // get current mana value
    public double getCurrentMana(UUID playerId) {
        if (hasInfiniteMana(playerId)) {
            return maxMana;
        } else {
            return playerMana.getOrDefault(playerId, maxMana);
        }
    }
    public boolean hasInfiniteMana(UUID playerId) {
        return infiniteManaMap.getOrDefault(playerId, false);
    }
    public void setPlayerMana(UUID playerId, double newMana) {
        // set mana for the specified player
        playerMana.put(playerId, Math.min(newMana, maxMana));

        // opdate  mana display for the player
        Player player = WizardsPlugin.getPlayerById(playerId);
        if (player != null) {
            updateManaActionBar(player);
        }
    }
    public void toggleInfiniteMana(UUID playerId) {
        boolean currentStatus = infiniteManaMap.getOrDefault(playerId, false);
        infiniteManaMap.put(playerId, !currentStatus);

        // if infinite mana is toggled, set player's mana to maximum value
        if (hasInfiniteMana(playerId)) {
            setPlayerMana(playerId, maxMana);
        }
    }

    public void clearManaBars() {
        for (BossBar bossBar : manaBossBars.values()) {
            bossBar.removeAll(); // remove players
        }
        manaBossBars.clear(); // clear hashmap
    }

    // mana bar as boss bar
    public void updateManaActionBar(Player player) {
        UUID playerId = player.getUniqueId();
        double currentMana = getCurrentMana(playerId);
        double manaPercentage = currentMana / maxMana;

        BossBar bossBar = manaBossBars.computeIfAbsent(playerId, k -> Bukkit.createBossBar("", BarColor.PURPLE, BarStyle.SOLID));
        bossBar.setTitle(ChatColor.YELLOW + "§lMana: " + (int) (manaPercentage * 100) + "§l%");
        bossBar.setProgress(manaPercentage);
        bossBar.addPlayer(player);
    }


    // mana bar as boss bar
//    public void updateManaActionBar(Player player) {
//        UUID playerId = player.getUniqueId();
//        double currentMana = getCurrentMana(playerId);
//        double manaPercentage = currentMana / maxMana;
//
//        BossBar bossBar = manaBossBars.computeIfAbsent(playerId, k -> Bukkit.createBossBar("", BarColor.PURPLE, BarStyle.SOLID));
//        bossBar.setTitle(ChatColor.YELLOW + "§lMana: " + (int) (manaPercentage * 100) + "§l%");
//        bossBar.setProgress(manaPercentage);
//        bossBar.addPlayer(player);
//    }


    //mana bar as action bar

//    public void updateManaActionBar(Player player) {
//    UUID playerId = player.getUniqueId();
//    double mana = getCurrentMana(playerId);
//    StringBuilder manaBar = new StringBuilder("Mana: [");
//
//    int filledLength = (int) ((double) mana / maxMana * 20); // length of the filled portion
//    for (int i = 0; i < filledLength; i++) {
//        manaBar.append("█");
//    }
//    for (int i = filledLength; i < 20; i++) {
//        manaBar.append("░");
//    }
//    manaBar.append("] ").append(mana).append("/").append(maxMana); // display current and max mana
//
//// determine spell name and mana cost if the player holds a valid item
//// add item to action bar
////    ItemStack itemInHand = player.getInventory().getItemInMainHand();
////    String spellInfo = getSpellInfo(itemInHand);
////    if (spellInfo != null) {
////        manaBar.append(" | ").append(spellInfo); // add spell info to the action bar
////    }
//
//
//    // create action bar
//    TextComponent actionBar = new TextComponent(manaBar.toString());
//    actionBar.setColor(ChatColor.DARK_PURPLE); // set color
//
//    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBar);
//}


}