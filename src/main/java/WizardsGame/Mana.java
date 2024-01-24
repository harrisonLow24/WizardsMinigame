//package WizardsGame;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Material;
//import org.bukkit.entity.Player;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class Mana {
//    private final Map<Player, Long> manaLastUpdate = new HashMap<>();
//    private final Map<Player, Double> playerMana = new HashMap<>();
//    private final double maxMana = 100.0;
//    private final double manaRegenRate = 1.0;
//    private final Map<Player, Double> spellManaCost = new HashMap<>(); // Map to store mana costs for each spell
//
//    double fireballCost = spellManaCost.getOrDefault(player, 10.0); // Example mana cost for Fireball
//    WizardsPlugin Wiz = new WizardsPlugin();
//
//
//    public void regenerateMana() {
//        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
//            double currentMana = playerMana.getOrDefault(player, maxMana);
//            double newMana = Math.min(currentMana + manaRegenRate, maxMana);
//            playerMana.put(player, newMana);
//            manaLastUpdate.put(player, System.currentTimeMillis());
//        }
//    }
//
//    // Method to check if a player has enough mana for a spell
//    private boolean hasEnoughMana(Player player, double spellCost) {
//        double currentMana = playerMana.getOrDefault(player, maxMana);
//        return currentMana >= spellCost;
//    }
//
//    // Method to deduct mana for casting a spell
//    private void deductMana(Player player, double spellCost) {
//        double currentMana = playerMana.getOrDefault(player, maxMana);
//        double newMana = Math.max(currentMana - spellCost, 0);
//        playerMana.put(player, newMana);
//    }
//}