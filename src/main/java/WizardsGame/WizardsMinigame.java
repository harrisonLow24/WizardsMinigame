package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.*;

public class WizardsMinigame {
    private final WizardsPlugin plugin;
    SpellMenu Menu = new SpellMenu(WizardsPlugin.getInstance());
    final Map<UUID, Vector> location1Map = new HashMap<>(); // store location 1
    final Map<UUID, Vector> location2Map = new HashMap<>(); // store location 2
    private final Random random = new Random();

    public WizardsMinigame(WizardsPlugin plugin) {
        this.plugin = plugin;
    }
    private final Map<WizardsPlugin.SpellType, Integer> spellRarityWeights = new HashMap<>();

    private void initializeRarityWeights() {
        // higher number = more common
        spellRarityWeights.put(WizardsPlugin.SpellType.Basic_Wand, 100); // common
        spellRarityWeights.put(WizardsPlugin.SpellType.Fiery_Wand, 30);   // uncommon
        spellRarityWeights.put(WizardsPlugin.SpellType.Shrouded_Step, 15); // rare
        spellRarityWeights.put(WizardsPlugin.SpellType.Mjölnir, 25);
        spellRarityWeights.put(WizardsPlugin.SpellType.Gust, 30);
        spellRarityWeights.put(WizardsPlugin.SpellType.The_Great_Escape, 15);
        spellRarityWeights.put(WizardsPlugin.SpellType.Big_Man_Slam, 30);
        spellRarityWeights.put(WizardsPlugin.SpellType.Winged_Shield, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.VoidWalker, 5); // legendary
        spellRarityWeights.put(WizardsPlugin.SpellType.Starfall_Barrage, 25);
        spellRarityWeights.put(WizardsPlugin.SpellType.Heal_Cloud, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.Recall, 5);
        spellRarityWeights.put(WizardsPlugin.SpellType.Void_Orb, 15);
        spellRarityWeights.put(WizardsPlugin.SpellType.Dragon_Spit, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.Cod_Shooter, 25);
    }

    void fillChest(Block chestBlock) {
        // check if the block is a chest
        if (chestBlock.getState() instanceof Chest chest) {
            Inventory inventory = chest.getInventory();
            inventory.clear(); // clear existing items in the chest

            // randomly fill the chest with spells and armor
            int numSpells = random.nextInt(5); // random number of spells (0-4)
            int numArmor = random.nextInt(4); // random number of armor (0-3)

            // add random spells
            for (int i = 0; i < numSpells; i++) {
                ItemStack spellItem = getRandomSpell();
                if (spellItem != null) {
                    placeItemRandomly(inventory, spellItem);
                }
            }

            // add random armor
            for (int i = 0; i < numArmor; i++) {
                ItemStack armorItem = getRandomArmor();
                if (armorItem != null) {
                    placeItemRandomly(inventory, armorItem);
                }
            }
        }
    }
    private ItemStack getRandomSpell() {
        // initialize rarity weights if not already done
        if (spellRarityWeights.isEmpty()) {
            initializeRarityWeights();
        }

        // list to hold spells based on rarity weights
        List<WizardsPlugin.SpellType> weightedSpellList = new ArrayList<>();

        // populate the list according to weights
        for (Map.Entry<WizardsPlugin.SpellType, Integer> entry : spellRarityWeights.entrySet()) {
            WizardsPlugin.SpellType spellType = entry.getKey();
            int weight = entry.getValue();

            // add the spell to the list based on weight
            for (int i = 0; i < weight; i++) {
                weightedSpellList.add(spellType);
            }
        }

        // if there are no spells, return null
        if (weightedSpellList.isEmpty()) {
            return null;
        }

        // select a random spell from weighted list
        WizardsPlugin.SpellType randomSpellType = weightedSpellList.get(random.nextInt(weightedSpellList.size()));

        // create the spell
        ItemStack spellItem = new ItemStack(randomSpellType.getMaterial());
        ItemMeta meta = spellItem.getItemMeta();

        if (meta != null) {
            String formattedName = ChatColor.YELLOW + "" + ChatColor.BOLD + Menu.formatSpellName(randomSpellType.name());
            meta.setDisplayName(formattedName);
            spellItem.setItemMeta(meta);
        }

        return spellItem;
    }

    private ItemStack getRandomArmor() {
        Material[] armorMaterials = {Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
                Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
                Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS};
        return new ItemStack(armorMaterials[random.nextInt(armorMaterials.length)]);
    }

    private void placeItemRandomly(Inventory inventory, ItemStack item) {
        // get a random empty slot in the chest inventory
        int randomSlot;
        do {
            randomSlot = random.nextInt(inventory.getSize());
        } while (inventory.getItem(randomSlot) != null);

        inventory.setItem(randomSlot, item); // place the item in the random slot
    }


}
