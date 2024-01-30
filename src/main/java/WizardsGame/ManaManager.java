package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManaManager {
    public final Map<UUID, Double> playerMana = new HashMap<>(); // hashmap of all players' current mana
    public final Map<UUID, Boolean> infiniteManaMap = new HashMap<>();
    public final Map<UUID, BossBar> manaBossBars = new HashMap<>(); // hashmap of all players' mana bars
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