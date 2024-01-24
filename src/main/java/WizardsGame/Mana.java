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
//    WizardsPlugin Wiz = new WizardsPlugin();
//    private final Map<Player, Double> playerMana = new HashMap<>();
//    final double maxMana = 100.0; // Set the maximum mana value
//    private final double manaRegenRate = 2.0; // Set the mana regeneration rate per second
//
//    public double getManaCost(Material wandType) {
//        // You can adjust these values based on your balancing needs
//        switch (wandType) {
//            case IRON_PICKAXE:
//                return 20.0;
//            case IRON_SWORD:
//                return 30.0;
//            case BLAZE_ROD:
//                return 15.0;
//            // Add more cases for other wand types if needed
//            default:
//                return 0.0;
//        }
//    }
//    public double getCurrentMana(Player player) {
//        return playerMana.getOrDefault(player, maxMana);
//    }
//    public void setCurrentMana(Player player, double newMana) {
//        if (newMana >= 0 && newMana <= maxMana) {
//            playerMana.put(player, newMana);
//        } else {
//            Bukkit.getLogger().warning("Invalid mana value set for player " + player.getName());
//        }
//    }
//    public boolean hasEnoughMana(Player player, double manaCost) {
//        return playerMana.getOrDefault(player, maxMana) >= manaCost;
//    }
//
////    void regenerateMana(Player player) {
////        double currentMana = playerMana.getOrDefault((Object) player, 0);
////        if (currentMana < maxMana) {
////            double newMana = Math.min(currentMana + manaRegenRate, maxMana);
////            playerMana.put(player, newMana);
////        }
////    }
//
//}
