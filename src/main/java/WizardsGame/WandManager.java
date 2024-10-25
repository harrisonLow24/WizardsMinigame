package WizardsGame;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WandManager {
    static CooldownManager Cooldown = new CooldownManager();

    public static ItemStack createWand(Material material) {
        ItemStack wand = new ItemStack(material);
        ItemMeta meta = wand.getItemMeta();

        if (meta != null) {
            String spellInfo = getSpellInfo(material);
            meta.setDisplayName(spellInfo);
            String spellName = getSpellName(material);
            List<String> lore = new ArrayList<>();

            lore.add(String.format("§eSpell: %s", spellName));
            // set lore based on the material
            switch (material) {
                case STICK:
                    lore.add("§gA basic wand with no special powers.");
                    break;
                case BLAZE_ROD:
                    lore.add("§gCast powerful fireballs.");
                    lore.add("§gRight click to shoot a fireball.");
                    break;
                case IRON_SWORD:
                    lore.add("§gTeleport to a targeted location.");
                    lore.add("§gUse it wisely!");
                    break;
                case IRON_PICKAXE:
                    lore.add("§gSummon lightning with a mighty hammer swing.");
                    lore.add("§gStrike your enemies down!");
                    break;
                case MINECART:
                    lore.add("§gRide a magical minecart to travel quickly.");
                    lore.add("§gRide off into the sunset!");
                    break;
                case FEATHER:
                    lore.add("§gCreate a gust of wind to push away enemies.");
                    lore.add("§gUse it to push enemies back!");
                    break;
                case SHIELD:
                    lore.add("§gRide away into the sunset.");
                    lore.add("§gSoar through the skies!");
                    break;
                case IRON_INGOT:
                    lore.add("§gUnleash a powerful ground slam as the BIG MAN you are.");
                    lore.add("§gSmash your opponents!");
                    break;
                case RECOVERY_COMPASS:
                    lore.add("§gSneak into another dimension and reach new heights.");
                    lore.add("§gFollow the map!");
                    break;
                case HONEYCOMB:
                    lore.add("§gCall upon the stars to rain down on your enemies!");
                    lore.add("§gUnleash destruction!");
                    break;
                case TIPPED_ARROW:
                    lore.add("§gBless yourself and allies with a circle of heal!");
                    lore.add("§gHeal your allies!");
                    break;
                case MUSIC_DISC_5:
                    lore.add("§gGet out of trouble in a pinch!");
                    lore.add("§gHeal your allies!");
                    break;
                case HEART_OF_THE_SEA:
                    lore.add("§gSend a ball of void energy at your opponents!");
                    lore.add("§gWatch this!");
                    break;
                case AMETHYST_SHARD:
                    lore.add("§gtest");
                    lore.add("§gtest");
                    break;
                default:
                    lore.add("§gA basic wand with no special powers.");
                    break;
            }
            meta.setLore(lore);

            wand.setItemMeta(meta);
        }

        return wand;
    }


    // spell information
    private static String getSpellName(Material material) {
        switch (material) {
            case BLAZE_ROD:
                return "§c§lFiery Wand";
            case IRON_SWORD:
                return "§9§lShrouded Step";
            case IRON_PICKAXE:
                return "§b§lMjölnir";
            case MINECART:
                return "§a§lThe Great Escape";
            case FEATHER:
                return "§lGust Feather";
            case SHIELD:
                return "§6§lWinged Shield";
            case IRON_INGOT:
                return "§8§lBig Man Slam";
            case RECOVERY_COMPASS:
                return "§5§lVoidwalker";
            case HONEYCOMB:
                return "§4§lStarfall Barrage";
            case TIPPED_ARROW:
                return "§d§lHeal Cloud";
            case MUSIC_DISC_5:
                return "§a§lRecall";
            case HEART_OF_THE_SEA:
                return "§e§lVoid Orb";
            case AMETHYST_SHARD:
                return "§e§lDragon Spit";
            default:
                return "§i§lGeneric Wand";
        }
    }
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
            case AMETHYST_SHARD:
                manaCost = (int) WizardsPlugin.MANABOLT_COST;
                cooldown = Cooldown.manaBoltCooldownDuration;
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
                        item.getType() == Material.HEART_OF_THE_SEA||
                        item.getType() == Material.AMETHYST_SHARD);
    }
}
