package WizardsGame;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class WandManager {
    static CooldownManager Cooldown = new CooldownManager();

    public static ItemStack createWand(Material material) {
        ItemStack wand = new ItemStack(material);
        ItemMeta meta = wand.getItemMeta();

        if (meta != null) {
            String spellInfo = getSpellInfo(material);
            meta.setDisplayName(spellInfo);

            // set lore based on the material
            switch (material) {
                case STICK:
                    meta.setLore(List.of("§gA basic wand with no special powers."));
                    break;
                case BLAZE_ROD:
                    meta.setLore(List.of("§gCast powerful fireballs.",
                            "§gRight click to shoot a fireball"));
                    break;
                case IRON_SWORD:
                    meta.setLore(List.of("§gTeleport to a targeted location.",
                            "§gUse it wisely!"));
                    break;
                case IRON_PICKAXE:
                    meta.setLore(List.of("§gSummon lightning with a mighty hammer swing.",
                            "§gStrike your enemies down!"));
                    break;
                case MINECART:
                    meta.setLore(List.of("§gRide a magical minecart to travel quickly.",
                            "§gRide off into the sunset!"));
                    break;
                case FEATHER:
                    meta.setLore(List.of("§gCreate a gust of wind to push away enemies.",
                            "§gUse it to push enemies back!"));
                    break;
                case SHIELD:
                    meta.setLore(List.of("§gRide away into the sunset.",
                            "§gSoar through the skies!"));
                    break;
                case IRON_INGOT:
                    meta.setLore(List.of("§gUnleash a powerful ground slam as the BIG MAN you are.",
                            "§gSmash your opponents!"));
                    break;
                case RECOVERY_COMPASS:
                    meta.setLore(List.of("§gSneak into another dimension and reach new heights.",
                            "§gFollow the map!"));
                    break;
                case HONEYCOMB:
                    meta.setLore(List.of("§gCall upon the stars to rain down on your enemies!",
                            "§gUnleash destruction!"));
                    break;
                case TIPPED_ARROW:
                    meta.setLore(List.of("§gBless yourself and allies with a circle of heal!",
                            "§gHeal your allies!"));
                    break;
                case MUSIC_DISC_5:
                    meta.setLore(List.of("§gGet out of trouble in a pinch!",
                            "§gHeal your allies!"));
                    break;
                case HEART_OF_THE_SEA:
                    meta.setLore(List.of("§gSend a ball of void energy at your opponents!",
                            "§gWatch this!"));
                    break;
                default:
                    meta.setLore(List.of("§gA basic wand with no special powers."));
                    break;
            }

            wand.setItemMeta(meta);
        }

        return wand;
    }

    // spell information
    private static String getSpellInfo(Material material) {
        String spellName;
        int manaCost = 0;
        long cooldown = 0;

        switch (material) {
            case BLAZE_ROD:
                spellName = "§c§lFiery Wand";
                manaCost = (int) WizardsPlugin.FIREBALL_COST;
                cooldown = Cooldown.fireballCooldownDuration;
                break;
            case IRON_SWORD:
                spellName = "§9§lShrouded Step";
                manaCost = (int) WizardsPlugin.TELEPORT_COST;
                cooldown = Cooldown.teleportCooldownDuration;
                break;
            case IRON_PICKAXE:
                spellName = "§b§lMjölnir";
                manaCost = (int) WizardsPlugin.LIGHTNING_COST;
                cooldown = Cooldown.lightningCooldownDuration;
                break;
            case MINECART:
                spellName = "§a§lThe Great Escape";
                manaCost = (int) WizardsPlugin.MINECART_COST;
                cooldown = Cooldown.minecartCooldownDuration;
                break;
            case FEATHER:
                spellName = "§lGust Feather";
                manaCost = (int) WizardsPlugin.GUST_COST;
                cooldown = Cooldown.gustCooldownDuration;
                break;
            case SHIELD:
                spellName = "§6§lWinged Shield";
                manaCost = (int) WizardsPlugin.FLYING_MANA_COST_PER_TICK;
                cooldown = Cooldown.squidFlyingCooldownDuration;
                break;
            case IRON_INGOT:
                spellName = "§8§lBig Man Slam";
                manaCost = (int) WizardsPlugin.GP_COST;
                cooldown = Cooldown.GPCooldownDuration;
                break;
            case RECOVERY_COMPASS:
                spellName = "§5§lVoidwalker";
                manaCost = (int) WizardsPlugin.VOIDWALKER_COST;
                cooldown = Cooldown.MapTeleportCooldownDuration;
                break;
            case HONEYCOMB:
                spellName = "§4§lStarfall Barrage";
                manaCost = (int) WizardsPlugin.METEOR_COST;
                cooldown = Cooldown.MeteorCooldownDuration;
                break;
            case TIPPED_ARROW:
                spellName = "§d§lHeal Cloud";
                manaCost = (int) WizardsPlugin.HEALCLOUD_COST;
                cooldown = Cooldown.HealCloudCooldownDuration;
                break;
            case MUSIC_DISC_5:
                spellName = "§a§lRecall";
                manaCost = (int) WizardsPlugin.Recall_Cost;
                cooldown = Cooldown.RecallCooldownDuration;
                break;
            case HEART_OF_THE_SEA:
                spellName = "§e§lVoid Orb";
                manaCost = (int) WizardsPlugin.VoidOrb_Cost;
                cooldown = Cooldown.VoidOrbCooldownDuration;
                break;
            default:
                spellName = "§i§lGeneric Wand";
                manaCost = 0;
                cooldown = 0;
                break;
        }

        return String.format("§e§lMana §r§f%d          §e§lCooldown §r§f%.1fs", manaCost, cooldown / 1000.0);

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
