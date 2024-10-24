package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class SpellBookMenu implements Listener {

    public SpellBookMenu(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openSpellBookMenu(Player player) {
        Inventory spellBookMenu = Bukkit.createInventory(null, 54, "Spell Book"); // Use 54 slots for a full chest

        // fill empty slots with stained glass
        ItemStack stainedGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1, (short) 15); // Use white stained glass
        for (int i = 0; i < spellBookMenu.getSize(); i++) {
            // set stained glass in specific slots, leaving gaps for spoells
            if (i != 10 && i != 12 && i != 14 && i != 16 && i != 28 && i != 30 && i != 32 && i != 34 && i != 46) {
                spellBookMenu.setItem(i, stainedGlass);
            }
        }

        // spell button creation with custom names
        ItemStack fireballButton = createSpellButton("Fiery Wand", Material.BLAZE_ROD);
        ItemStack teleportButton = createSpellButton("Shrouded Step", Material.IRON_SWORD);
        ItemStack lightningButton = createSpellButton("Mjölnir", Material.IRON_PICKAXE);
        ItemStack minecartButton = createSpellButton("The Great Escape", Material.MINECART);
        ItemStack gustButton = createSpellButton("Gust Feather", Material.FEATHER);
        ItemStack flyButton = createSpellButton("Winged Shield", Material.SHIELD);
        ItemStack GPButton = createSpellButton("Big Man Slam", Material.IRON_INGOT);
        ItemStack mapTPButton = createSpellButton("Voidwalker", Material.RECOVERY_COMPASS);
        ItemStack mapMeteorButton = createSpellButton("Starfall Barrage", Material.HONEYCOMB);
        ItemStack mapHealCloudButton = createSpellButton("Heal Cloud", Material.TIPPED_ARROW);
        ItemStack mapRecallButton = createSpellButton("Recall", Material.MUSIC_DISC_5);

        // spell buttons' positions, starting from the 9th slot (index 8)
        spellBookMenu.setItem(10, fireballButton);
        spellBookMenu.setItem(12, teleportButton);
        spellBookMenu.setItem(14, lightningButton);
        spellBookMenu.setItem(16, minecartButton);
        spellBookMenu.setItem(28, gustButton);
        spellBookMenu.setItem(30, flyButton);
        spellBookMenu.setItem(32, GPButton);
        spellBookMenu.setItem(34, mapTPButton);
        spellBookMenu.setItem(46, mapMeteorButton);
        spellBookMenu.setItem(48, mapHealCloudButton);
        spellBookMenu.setItem(50, mapRecallButton);

        player.openInventory(spellBookMenu);
    }


    public ItemStack createSpellButton(String spellName, Material material) {
        ItemStack spellButton = new ItemStack(material);

        // customize spell button appearance
        ItemMeta meta = spellButton.getItemMeta();
        assert meta != null;

        // set custom name
        ChatColor nameColor = getSpellNameColor(material);
        String customName = nameColor + spellName;
        meta.setDisplayName(customName);

        // set custom description
        List<String> lore = getSpellLore(spellName);
        meta.setLore(lore);

        // note: can add additional customization(enchantments, etc.)
        spellButton.setItemMeta(meta);

        return spellButton;
    }

    public void handleSpellAddition(Player player, Material material) {
        ItemStack spellItem = new ItemStack(material);
        ItemMeta meta = spellItem.getItemMeta();
        String customName = getCustomName(material);

        assert meta != null;
        meta.setDisplayName(getSpellNameColor(material) + customName); // Set custom name color
        spellItem.setItemMeta(meta);

        // set lore
        List<String> lore = getSpellLore(customName);
        meta.setLore(lore);
        spellItem.setItemMeta(meta);

        // check if player already has the spell
        if (!hasSpell(player, customName)) {
            // check if player's inventory is full
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(spellItem);
                player.sendMessage("You've acquired the spell: " + customName);
            } else {
                player.getWorld().dropItem(player.getLocation(), spellItem);
                player.sendMessage("Your inventory is full. The spell: " + customName + " has been dropped at your location.");
            }
        } else {
            player.sendMessage("You already have the spell: " + customName);
        }
    }



    private String getCustomName(Material material) {
        // custom names for materials
        switch (material) {
            case BLAZE_ROD:
                return "Fiery Wand";
            case IRON_SWORD:
                return "Shrouded Step";
            case IRON_PICKAXE:
                return "Mjölnir";
            case MINECART:
                return "The Great Escape";
            case FEATHER:
                return "Gust Feather";
            case SHIELD:
                return "Winged Shield";
            case IRON_INGOT:
                return "Big Man Slam";
            case RECOVERY_COMPASS:
                return "Voidwalker";
            case HONEYCOMB:
                return "Starfall Barrage";
            case TIPPED_ARROW:
                return "Heal Cloud";
            case MUSIC_DISC_5:
                return "Recall";

            default:
                return "Unknown Spell";
        }
    }

    private boolean hasSpell(Player player, String spellName) {
        // check if player's inventory has item already
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String itemName = ChatColor.stripColor(meta.getDisplayName());
                    if (spellName.equals(itemName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ChatColor getSpellNameColor(Material material) {
        // custom colors for spells
        switch (material) {
            case BLAZE_ROD:
                return ChatColor.RED;
            case IRON_SWORD:
                return ChatColor.BLUE;
            case IRON_PICKAXE:
                return ChatColor.YELLOW;
            case MINECART:
                return ChatColor.GREEN;
            case FEATHER:
                return ChatColor.WHITE;
            case SHIELD:
                return ChatColor.GOLD;
            case IRON_INGOT:
                return ChatColor.DARK_GRAY;
            case RECOVERY_COMPASS:
                return ChatColor.DARK_PURPLE;
            case HONEYCOMB:
                return ChatColor.DARK_RED;
            case TIPPED_ARROW:
                return ChatColor.LIGHT_PURPLE;
            case MUSIC_DISC_5:
                return ChatColor.DARK_GREEN;
            default:
                return ChatColor.GRAY;
        }
    }

    public List<String> getSpellLore(String spellName) {
        // custom description for spells
        List<String> lore = new ArrayList<>();
        switch (spellName) {
            case "Fiery Wand":
                lore.add("Cast powerful fireballs." +
                        "Right click to shoot a fireball");
                break;
            case "Shrouded Step":
                lore.add("Teleport to a targeted location.");
                break;
            case "Mjölnir":
                lore.add("Summon lightning with a mighty hammer swing.");
                break;
            case "The Great Escape":
                lore.add("Ride a magical minecart to travel quickly.");
                break;
            case "Gust Feather":
                lore.add("Create a gust of wind to push away enemies.");
                break;
            case "Winged Shield":
                lore.add("Ride away into the sunset.");
                break;
            case "Big Man Slam":
                lore.add("Unleash a powerful ground slam as the BIG MAN you are.");
                break;
            case "Voidwalker":
                lore.add("Sneak into another dimension and reach new heights.");
                break;
            case "Starfall Barrage":
                lore.add("Call upon the stars to rain down on your enemies.");
                break;
            case "Heal Cloud":
                lore.add("Bless yourself and allies with a circle of heal.");
                break;
            case "Recall":
                lore.add("Get out of trouble in a pinch!");
                break;
            default:
                lore.add("Unknown Spell");
                break;
        }
        return lore;
    }




    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // check if player right-clicks with  spell book
        if (event.getAction().name().contains("RIGHT") && itemInHand.getType() == Material.BOOK) {
            openSpellBookMenu(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // check if clicked item is a spell button
        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                //prevent interaction with stained glass
                event.setCancelled(true);
                return;
            }
            if (meta != null) {
                String spellName = ChatColor.stripColor(meta.getDisplayName()); // Get spell name without color codes

                // check if the player has a spell book in hand
                if (player.getInventory().getItemInMainHand().getType() == Material.BOOK) {
                    // add spell to player's inventory only if the spell book is in hand
                    switch (spellName) {
                        case "Fiery Wand":
                        case "Shrouded Step":
                        case "Mjölnir":
                        case "The Great Escape":
                        case "Gust Feather":
                        case "Winged Shield":
                        case "Big Man Slam":
                        case "Voidwalker":
                        case "Starfall Barrage":
                        case "Heal Cloud":
                        case "Recall":
                            handleSpellAddition(player, clickedItem.getType());

                            // close  inventory
                            // cancel event to prevent item from being moved in the inventory
                            player.closeInventory();
                            event.setCancelled(true);
                            break;
                    }
                } else {
                    player.closeInventory();
                    event.setCancelled(true);
                }
            }
        }
    }
}
