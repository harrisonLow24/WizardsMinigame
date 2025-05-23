package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SpellMenu {
    private final WizardsPlugin spellManager;
    CooldownManager Cooldown = new CooldownManager();
    ManaManager Mana = new ManaManager();
    SpellCastingManager Cast = new SpellCastingManager();

    public enum SpellCategory {
        COMBAT,
        MOVEMENT,
        MISC
    }

    public SpellMenu(WizardsPlugin spellManager) {
        this.spellManager = spellManager;
    }
    String formatSpellName(String spellName) {
        String formattedName = spellName.replace("_", " ").toLowerCase();
        String[] words = formattedName.split(" ");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            formatted.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return formatted.toString().trim();
    }

    public void openSpellMenu(Player player) {
        UUID playerId = player.getUniqueId();
        Inventory menu = Bukkit.createInventory(null, 54, "Select a Spell");

        // slots for each section
        int[] combatSlots = {18, 19, 20, 27, 28, 29}; // 38, 39, 40
        int[] movementSlots = {24, 25, 26, 33, 34, 35, 42}; // 42, 43, 44
        int[] miscSlots = {22, 31};

//        int[] dividerSlots = {3, 12, 21, 30, 39, 48, 5, 14, 23, 32, 41, 50};

        int[] dividerSlots = new int[54];
        for (int i = 0; i < 54; i++) {
            dividerSlots[i] = i;
        }

        // fill divider slots
        ItemStack divider = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta dividerMeta = divider.getItemMeta();
        dividerMeta.setDisplayName(" ");
        divider.setItemMeta(dividerMeta);

        for (int slot : dividerSlots) {
            menu.setItem(slot, divider);
        }

        // xet specific items in defined slots
        ItemStack netheriteSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta swordMeta = netheriteSword.getItemMeta();
        swordMeta.setDisplayName("§aCOMBAT");
        netheriteSword.setItemMeta(swordMeta);
        menu.setItem(1, netheriteSword);

        ItemStack totemOfUndying = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta totemMeta = totemOfUndying.getItemMeta();
        totemMeta.setDisplayName("§aMISC");
        totemOfUndying.setItemMeta(totemMeta);
        menu.setItem(4, totemOfUndying);

        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta elytraMeta = elytra.getItemMeta();
        elytraMeta.setDisplayName("§aMOVEMENT");
        elytra.setItemMeta(elytraMeta);
        menu.setItem(7, elytra);


        // current index for each section
        int combatIndex = 0;
        int movementIndex = 0;
        int miscIndex = 0;

        for (WizardsPlugin.SpellType spellType : WizardsPlugin.SpellType.values()) {
            if (spellType == WizardsPlugin.SpellType.Basic_Wand) {
                continue;
            }
            int spellLevel = spellManager.getSpellLevel(playerId, spellType);
            ItemStack spellItem = new ItemStack(spellType.getMaterial());
            ItemMeta meta = spellItem.getItemMeta();

            // spell name with level
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + formatSpellName(spellType.name()) + ChatColor.BLUE +" Lv " + spellLevel + "");

            // spell details as lore
            List<String> spellLore = getSpellDetails(spellType, spellLevel, playerId, 0);
            meta.setLore(spellLore);

            // update meta
            spellItem.setItemMeta(meta);

            // spell is unowned
            if (spellLevel == 0) {
                meta.setLore(Collections.singletonList("§c§lNot owned!"));
                spellItem.setType(Material.GRAY_DYE);
            }

            // determine categories
            SpellCategory category = getSpellCategory(spellType);
            switch (category) {
                case COMBAT -> {
                    if (combatIndex < combatSlots.length) {
                        menu.setItem(combatSlots[combatIndex], spellItem);
                        combatIndex++;
                    }
                }
                case MOVEMENT -> {
                    if (movementIndex < movementSlots.length) {
                        menu.setItem(movementSlots[movementIndex], spellItem);
                        movementIndex++;
                    }
                }
                case MISC -> {
                    if (miscIndex < miscSlots.length) {
                        menu.setItem(miscSlots[miscIndex], spellItem);
                        miscIndex++;
                    }
                }
            }
        }

        player.openInventory(menu);
    }

    static WizardsPlugin.SpellType getSpellByMaterial(Material material) {
        for (WizardsPlugin.SpellType spell : WizardsPlugin.SpellType.values()) {
            if (spell.getMaterial() == material) {
                return spell;
            }
        }
        return null;
    }

    // determine catergories
    private SpellCategory getSpellCategory(WizardsPlugin.SpellType spellType) {
        switch (spellType) {
            case Fiery_Wand, Mjölnir, Starfall_Barrage, Big_Man_Slam, Void_Orb, Dragon_Spit-> {
                return SpellCategory.COMBAT;
            }
            case Shrouded_Step, The_Great_Escape, Gust , Winged_Shield, VoidWalker, Recall , Leap-> {
                return SpellCategory.MOVEMENT;
            }
            case Heal_Cloud, Cod_Shooter-> {
                return SpellCategory.MISC;
            }
            default -> {
                return SpellCategory.MISC; // default category
            }
        }
    }

    // spell details
    List<String> getSpellDetails(WizardsPlugin.SpellType spellType, int spellLevel, UUID playerId, int a) {
        List<String> details = new ArrayList<>();
        double damage = 0;
        double heal = 0;
        double radius = 0;
        double knockback = 0;
        double fishCount = 0;
        double velocity = 0;
        int hasBoth = 0;
        String mana = "";
        double cooldown = 0;
        String desc = "";
//        if (spellLevel > 0) {
//            details.add("§aLevel: " + spellLevel);
//        }else {
//            details.add("§cNot owned!");
//            return details;
//        }
        if (spellLevel <= 0){
            details.add("§cNot owned!");
            return details;
        }
        String spellName;
        switch (spellType) {
            case Fiery_Wand -> {
                spellName = "Fiery Wand";
                mana = "§eMana: " + WizardsPlugin.FIREBALL_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Launches a fireball at your enemies.";
                damage = Cast.getFireballDamage(playerId);
            }
            case Shrouded_Step -> {
                spellName = "Shrouded Step";
                mana = "§eMana: " + WizardsPlugin.TELEPORT_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Teleport a short distance.";
            }
            case Mjölnir -> {
                spellName = "Mjölnir";
                mana = "§eMana: " + WizardsPlugin.LIGHTNING_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Summons a lightning strike.";
                damage = Cast.getLightningDamage(playerId);
                radius = Cast.getLightningRadius(playerId);
                hasBoth = 1;
            }
            case The_Great_Escape -> {
                spellName = "The Great Escape";
                mana = "§eMana: " + WizardsPlugin.MINECART_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Send you a short distance in a minecart.";
            }
            case Gust -> {
                spellName = "Gust";
                mana = "§eMana: " + WizardsPlugin.GUST_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Pushes enemies away.";
            }
            case Winged_Shield -> {
                spellName = "Winged Shield";
                mana = "§eMana: " + WizardsPlugin.FLYING_MANA_COST_PER_TICK;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Enables flight for a short duration.";
            }
            case Big_Man_Slam -> {
                spellName = "Big Man Slam";
                mana = "§eMana: " + WizardsPlugin.GP_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Crushes enemies with a slam.";
                damage = Cast.getGPDamage(playerId);
                radius = Cast.getGPRadius(playerId);
                hasBoth = 1;
            }
            case VoidWalker -> {
                spellName = "Voidwalker";
                mana = "§eMana: " + WizardsPlugin.VOIDWALKER_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Teleports you using an alternate dimension.";
            }
            case Starfall_Barrage -> {
                spellName = "Starfall Barrage";
                mana = "§eMana: " + WizardsPlugin.METEOR_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Calls down a barrage of meteors.";
                damage = Cast.getMeteorDamage(playerId);
                radius = Cast.getMeteorRadius(playerId);
                hasBoth = 1;
            }
            case Heal_Cloud -> {
                spellName = "Heal Cloud";
                mana = "§eMana: " + WizardsPlugin.HEALCLOUD_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Heals you and nearby allies over time.";
                heal = Cast.getHealAmount(playerId);
                radius = Cast.HEAL_BASE_RADIUS;
            }
            case Recall -> {
                spellName = "Recall";
                mana = "§eMana: " + WizardsPlugin.Recall_Cost;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Teleport backwards 5 seconds.";
            }
            case Void_Orb -> {
                spellName = "Void Orb";
                mana = "§eMana: " + WizardsPlugin.VoidOrb_Cost;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Sends a ball of void energy at your enemies.";
                damage = Cast.getVoidOrbDamage(playerId);
            }
            case Dragon_Spit -> {
                spellName = "Dragon Spit";
                mana = "§eMana: " + WizardsPlugin.MANABOLT_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Sends a ball of spit at your enemies.";
                damage = Cast.getManaBoltDamage(playerId);
            }
            case Cod_Shooter -> {
                spellName = "Cod Shooter";
                mana = "§eMana: " + WizardsPlugin.COD_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Knock your enemies back with the power of fish.";
                knockback = Cast.getFishKnockback(playerId);
                fishCount = Cast.getFishCount(playerId);
            }
            case Leap -> {
                spellName = "Leap";
                mana = "§eMana: " + WizardsPlugin.LEAP_COST;
                cooldown = Cooldown.getCooldownDuration(spellName);
                desc = "§7Leap forward with all your might.";
//                damage = Cast.getLeapDamage(playerId);
                velocity = Cast.getLeapVelocity(playerId);
                hasBoth = 1;
            }
        }
        if (a == 0) {
            details.add(mana);
            details.add("§eCooldown: " + cooldown / 1000 + "s");
        }if (radius > 1 && damage > 0){
            details.add("§cDamage: " + damage / 2 + " §c❤ |" + " §cRadius: " + radius);
        }if (damage > 0 && hasBoth == 0) {
            details.add("§cDamage: " + damage / 2 + " §c❤");
        }if (heal > 0) {
            details.add("§cHeal: " + heal / 2 + " §c❤/s |" + " §cRadius: " + radius);
        }if (knockback > 0) {
            details.add("§cKnockback: " + knockback +" | " + " §cCount: " + fishCount);
        }if (velocity > 0) {
            details.add("§cVelocity: " + velocity);
        }
            details.add(desc);

        return details;
    }
}
