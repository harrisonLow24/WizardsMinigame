package WizardsGame;

import org.bukkit.Bukkit;
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

    public enum SpellCategory {
        COMBAT,
        MOVEMENT,
        MISC
    }

    public SpellMenu(WizardsPlugin spellManager) {
        this.spellManager = spellManager;
    }

    public void openSpellMenu(Player player) {
        UUID playerId = player.getUniqueId();
        Inventory menu = Bukkit.createInventory(null, 54, "Select a Spell");

        // slots for each section
        int[] combatSlots = {18, 19, 20, 27, 28, 29, 38, 39, 40};
        int[] movementSlots = {24, 25, 26, 33, 34, 35, 42, 43, 44};
        int[] miscSlots = {22, 31, 40};

        int[] dividerSlots = {3, 12, 21, 30, 39, 48, 5, 14, 23, 32, 41, 50};

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
            int spellLevel = spellManager.getSpellLevel(playerId, spellType);
            ItemStack spellItem = new ItemStack(spellType.getMaterial());
            ItemMeta meta = spellItem.getItemMeta();

            // spell name with level
            meta.setDisplayName("§a§l" + spellType.name() + " §7(Level " + spellLevel + ")");

            // spell details as lore
            List<String> spellLore = getSpellDetails(spellType, spellLevel);
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
            case FIERY_WAND, MJOLNIR, STARFALL_BARRAGE, BIG_MAN_SLAM, VOID_ORB, DRAGON_SPIT-> {
                return SpellCategory.COMBAT;
            }
            case SHROUDED_STEP, THE_GREAT_ESCAPE, GUST_FEATHER, WINGED_SHIELD, VOIDWALKER, RECALL -> {
                return SpellCategory.MOVEMENT;
            }
            case HEAL_CLOUD -> {
                return SpellCategory.MISC;
            }
            default -> {
                return SpellCategory.MISC; // default category
            }
        }
    }

    // spell details
    private List<String> getSpellDetails(WizardsPlugin.SpellType spellType, int spellLevel) {
        List<String> details = new ArrayList<>();
        switch (spellType) {
            case FIERY_WAND -> {
                details.add("§eMana: " + WizardsPlugin.FIREBALL_COST);
                details.add("§eCooldown: " + Cooldown.fireballCooldownDuration / 1000 + "s");
                details.add("§7Launches a fireball at your enemies.");
            }
            case SHROUDED_STEP -> {
                details.add("§eMana: " + WizardsPlugin.TELEPORT_COST);
                details.add("§eCooldown: " + Cooldown.teleportCooldownDuration / 1000 + "s");
                details.add("§7Teleport a short distance.");
            }
            case MJOLNIR -> {
                details.add("§eMana: " + WizardsPlugin.LIGHTNING_COST);
                details.add("§eCooldown: " + Cooldown.lightningCooldownDuration / 1000 + "s");
                details.add("§7Summons a lightning strike.");
            }
            case THE_GREAT_ESCAPE -> {
                details.add("§eMana: " + WizardsPlugin.MINECART_COST);
                details.add("§eCooldown: " + Cooldown.minecartCooldownDuration / 1000 + "s");
                details.add("§7Send you a short distance in a minecart.");
            }
            case GUST_FEATHER -> {
                details.add("§eMana: " + WizardsPlugin.GUST_COST);
                details.add("§eCooldown: " + Cooldown.gustCooldownDuration / 1000 + "s");
                details.add("§7Pushes enemies away.");
            }
            case WINGED_SHIELD -> {
                details.add("§eMana: " + WizardsPlugin.FLYING_MANA_COST_PER_TICK);
                details.add("§eCooldown: " + Cooldown.squidFlyingCooldownDuration / 1000 + "s");
                details.add("§7Enables flight for a short duration.");
            }
            case BIG_MAN_SLAM -> {
                details.add("§eMana: " + WizardsPlugin.GP_COST);
                details.add("§eCooldown: " + Cooldown.GPCooldownDuration / 1000 + "s");
                details.add("§7Crushes enemies with a slam.");
            }
            case VOIDWALKER -> {
                details.add("§eMana: " + WizardsPlugin.VOIDWALKER_COST);
                details.add("§eCooldown: " + Cooldown.MapTeleportCooldownDuration / 1000 + "s");
                details.add("§7Teleports you using an alternate dimension.");
            }
            case STARFALL_BARRAGE -> {
                details.add("§eMana: " + WizardsPlugin.METEOR_COST);
                details.add("§eCooldown: " + Cooldown.MeteorCooldownDuration / 1000 + "s");
                details.add("§7Calls down a barrage of meteors.");
            }
            case HEAL_CLOUD -> {
                details.add("§eMana: " + WizardsPlugin.HEALCLOUD_COST);
                details.add("§eCooldown: " + Cooldown.HealCloudCooldownDuration / 1000 + "s");
                details.add("§7Heals you and nearby allies over time.");
            }
            case RECALL -> {
                details.add("§eMana: " + WizardsPlugin.Recall_Cost);
                details.add("§eCooldown: " + Cooldown.RecallCooldownDuration / 1000 + "s");
                details.add("§7Teleport backwards 5 seconds.");
            }
            case VOID_ORB -> {
                details.add("§eMana: " + WizardsPlugin.VoidOrb_Cost);
                details.add("§eCooldown: " + Cooldown.VoidOrbCooldownDuration / 1000 + "s");
                details.add("§7Sends a ball of void energy at your enemies.");
            }
            case DRAGON_SPIT -> {
                details.add("§eMana: " + WizardsPlugin.MANABOLT_COST);
                details.add("§eCooldown: " + Cooldown.MapTeleportCooldownDuration / 1000 + "s");
                details.add("§7Sends a ball of spit at your enemies.");
            }
        }

        if (spellLevel > 0) {
            details.add("§aLevel: " + spellLevel);
        } else {
            details.add("§cNot owned!");
        }

        return details;
    }
}
