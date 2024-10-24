package WizardsGame;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class WandManager {
    public static ItemStack createWand(Material material) {
        ItemStack wand = new ItemStack(material);
        ItemMeta meta = wand.getItemMeta();

        if (meta != null) {
            // set display name and lore based on the material
            switch (material) {
                case STICK:
                    meta.setDisplayName("§i§lGeneric Wand");
                    meta.setLore(List.of("§gA basic wand with no special powers."));
                    break;
                case BLAZE_ROD:
                    meta.setDisplayName("§c§lFiery Wand");
                    meta.setLore(List.of("§gCast powerful fireballs.",
                            "§gRight click to shoot a fireball"));
                    break;
                case IRON_SWORD:
                    meta.setDisplayName("§9§lShrouded Step");
                    meta.setLore(List.of("§gTeleport to a targeted location.",
                            "§gUse it wisely!"));
                    break;
                case IRON_PICKAXE:
                    meta.setDisplayName("§b§lMjölnir");
                    meta.setLore(List.of("§gSummon lightning with a mighty hammer swing.",
                            "§gStrike your enemies down!"));
                    break;
                case MINECART:
                    meta.setDisplayName("§a§lThe Great Escape");
                    meta.setLore(List.of("§gRide a magical minecart to travel quickly.",
                            "§gRide off into the sunset!"));
                    break;
                case FEATHER:
                    meta.setDisplayName("§lGust Feather");
                    meta.setLore(List.of("§gCreate a gust of wind to push away enemies.",
                            "§gUse it to push enemies back!"));
                    break;
                case SHIELD:
                    meta.setDisplayName("§6§lWinged Shield");
                    meta.setLore(List.of("§gRide away into the sunset.",
                            "§gSoar through the skies!"));
                    break;
                case IRON_INGOT:
                    meta.setDisplayName("§8§lBig Man Slam");
                    meta.setLore(List.of("§gUnleash a powerful ground slam as the BIG MAN you are.",
                            "§gSmash your opponents!"));
                    break;
                case RECOVERY_COMPASS:
                    meta.setDisplayName("§5§lVoidwalker");
                    meta.setLore(List.of("§gSneak into another dimension and reach new heights.",
                            "§gFollow the map!"));
                    break;
                case HONEYCOMB:
                    meta.setDisplayName("§4§lStarfall Barrage");
                    meta.setLore(List.of("§gCall upon the stars to rain down on your enemies!",
                            "§gUnleash destruction!"));
                    break;
                case TIPPED_ARROW:
                    meta.setDisplayName("§d§lHeal Cloud");
                    meta.setLore(List.of("§gBless yourself and allies with a circle of heal!",
                            "§gHeal your allies!"));
                    break;
                case MUSIC_DISC_5:
                    meta.setDisplayName("§a§lRecall");
                    meta.setLore(List.of("§gGet out of trouble in a pinch!",
                            "§gHeal your allies!"));
                    break;
                case HEART_OF_THE_SEA:
                    meta.setDisplayName("§e§lVoid Orb");
                    meta.setLore(List.of("§gSend a ball of void energy at your opponents!",
                            "§gWatch this!"));
                    break;
                default:
                    meta.setDisplayName("§i§lGeneric Wand");
                    meta.setLore(List.of("§gA basic wand with no special powers."));
                    break;
            }

            wand.setItemMeta(meta);
        }

        return wand;
    }

    // check if an item is a wand
    public static boolean isWand(ItemStack item) {
        return item != null && item.getItemMeta() != null &&
                (item.getType() == Material.BLAZE_ROD ||
                        item.getType() == Material.IRON_SWORD ||
                        item.getType() == Material.IRON_PICKAXE ||
                        item.getType() == Material.MINECART ||
                        item.getType() == Material.FEATHER ||
                        item.getType() == Material.SHIELD ||
                        item.getType() == Material.IRON_INGOT ||
                        item.getType() == Material.RECOVERY_COMPASS ||
                        item.getType() == Material.HONEYCOMB ||
                        item.getType() == Material.TIPPED_ARROW ||
                        item.getType() == Material.MUSIC_DISC_5 ||
                        item.getType() == Material.HEART_OF_THE_SEA);
    }

}

