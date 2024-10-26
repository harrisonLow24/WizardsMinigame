package WizardsGame;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WandManager {
    static CooldownManager Cooldown = new CooldownManager();
    static SpellCastingManager Cast = new SpellCastingManager();
    SpellMenu Menu = new SpellMenu(WizardsPlugin.getInstance());

    public static ItemStack createWand(Material material, Player player) {
        UUID playerId = player.getUniqueId();
        ItemStack wand = new ItemStack(material);
        ItemMeta meta = wand.getItemMeta();

        if (meta != null) {
            String spellInfo = getSpellInfo(material,playerId, 0);
            String dmg = getSpellInfo(material,playerId, 1);
            meta.setDisplayName(spellInfo);
            String spellName = getSpellName(material);
            List<String> lore = new ArrayList<>();

            lore.add(String.format("%s", spellName));
            lore.add(String.format(ChatColor.YELLOW + dmg));
            // set lore based on the material
            switch (material) {
                case STICK:
                    lore.removeFirst();
                    lore.removeFirst();
                    lore.add("§gA basic wand with no special powers.");
                    break;
                case BLAZE_ROD:
                    lore.add("§gCast powerful fireballs.");
                    break;
                case IRON_SWORD:
                    lore.add("§gTeleport to a targeted location.");
                    break;
                case IRON_PICKAXE:
                    lore.add("§gSummon lightning with a mighty hammer swing.");
                    break;
                case MINECART:
                    lore.add("§gRide a magical minecart to travel quickly.");
                    break;
                case FEATHER:
                    lore.add("§gCreate a gust of wind to push away enemies.");
                    break;
                case SHIELD:
                    lore.add("§gSoar through the skies!");
                    break;
                case IRON_INGOT:
                    lore.add("§gUnleash a powerful ground slam as the BIG MAN you are.");
                    break;
                case RECOVERY_COMPASS:
                    lore.add("§gSneak into another dimension and reach new heights.");
                    break;
                case HONEYCOMB:
                    lore.add("§gCall upon the stars to rain down on your enemies!");
                    break;
                case TIPPED_ARROW:
                    lore.add("§gBless yourself and allies with a circle of heal!");
                    break;
                case MUSIC_DISC_5:
                    lore.add("§gGet out of trouble in a pinch!");
                    break;
                case HEART_OF_THE_SEA:
                    lore.add("§gSend a ball of void energy at your opponents!");
                    break;
                case AMETHYST_SHARD:
                    lore.add("§gSend a ball of spit at your opponents!");
                    break;
                case NAUTILUS_SHELL:
                    lore.add("§gKnock your enemies back with the power of fish!");
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
            case NAUTILUS_SHELL:
                return "§e§lCod Shooter";
            default:
                return "";
        }
    }
    private static String getSpellInfo(Material material, UUID playerId, int hasDmg) {
        String spellName = "";
        int manaCost = 0;
        long cooldown = 0;
        double damage = 0;
        double heal = 0;
        double radius = 0;
        double knockback = 0;
        double fishCount = 0;
        int basic = 0;

        switch (material) {
            case BLAZE_ROD:
                spellName = "§c§lFiery Wand";
                manaCost = (int) WizardsPlugin.FIREBALL_COST;
                cooldown = Cooldown.fireballCooldownDuration;
                damage = Cast.getFireballDamage(playerId);
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
                damage = Cast.getLightningDamage(playerId);
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
                damage = Cast.getGPDamage(playerId);
                radius = Cast.getGPRadius(playerId);
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
                damage = Cast.getMeteorDamage(playerId);
                radius = Cast.getMeteorRadius(playerId);
                break;
            case TIPPED_ARROW:
                spellName = "§d§lHeal Cloud";
                manaCost = (int) WizardsPlugin.HEALCLOUD_COST;
                cooldown = Cooldown.HealCloudCooldownDuration;
                heal = Cast.getHealAmount(playerId);
                radius = Cast.HEAL_BASE_RADIUS;
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
                damage = Cast.getVoidOrbDamage(playerId);
                break;
            case AMETHYST_SHARD:
                manaCost = (int) WizardsPlugin.MANABOLT_COST;
                cooldown = Cooldown.manaBoltCooldownDuration;
                damage = Cast.getManaBoltDamage(playerId);
                break;
            case NAUTILUS_SHELL:
                manaCost = (int) WizardsPlugin.COD_COST;
                cooldown = Cooldown.CodCooldownDuration;
                knockback = Cast.getFishKnockback(playerId);
                fishCount = Cast.getFishCount(playerId);
                break;
            default:
                spellName = "§i§lBasic Wand";
                basic = 1;
                break;
        }
        if (basic == 1){
            return ("Basic Wand");
        }
        if (hasDmg == 0){
            return String.format("§e§lMana §r§f%d          §e§lCooldown §r§f%.1fs", manaCost, cooldown / 1000.0);
        }if (radius > 1 && heal == 0){
            return ("§cDamage: " + damage / 2 + " §c❤ |" + " §cRadius: " + radius);
        }if (damage > 0) {
            return("§cDamage: " + damage / 2 + " §c❤");
        }if (heal > 0) {
            return("§cHeal: " + heal / 2 + " §c❤/s |" + " §cRadius: " + radius);
        }if (knockback > 0) {
            return("§cKnockback: " + knockback +" | " + " §cCount: " + fishCount);
        } else{
            return("");
        }
    }

    // check if an item is a wand
    public static boolean isWand(ItemStack item) {
        return item != null && item.getItemMeta() != null &&
                (item.getType() == Material.STICK ||
                        item.getType() == Material.BLAZE_ROD ||
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
                        item.getType() == Material.AMETHYST_SHARD||
                        item.getType() == Material.NAUTILUS_SHELL);
    }
}
