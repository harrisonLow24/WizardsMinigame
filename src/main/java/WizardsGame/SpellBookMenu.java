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

import java.util.Objects;

public class SpellBookMenu implements Listener {

    public SpellBookMenu(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openSpellBookMenu(Player player) {
        Inventory spellBookMenu = Bukkit.createInventory(null, 9, "Spell Book");

        // spell button creation with custom names
        ItemStack fireballButton = createSpellButton("Fiery Wand", Material.BLAZE_ROD);
        ItemStack teleportButton = createSpellButton("Teleportation Blade", Material.IRON_SWORD);
        ItemStack lightningButton = createSpellButton("Thunder Hammer", Material.IRON_PICKAXE);
        ItemStack minecartButton = createSpellButton("Magical Minecart", Material.MINECART);
        ItemStack gustButton = createSpellButton("Gust Feather", Material.FEATHER);
        ItemStack flyButton = createSpellButton("Winged Shield", Material.SHIELD);
        ItemStack GPButton = createSpellButton("Big Man Slam", Material.IRON_INGOT);

        // spell buttons' positions
        spellBookMenu.setItem(0, fireballButton);
        spellBookMenu.setItem(1, teleportButton);
        spellBookMenu.setItem(2, lightningButton);
        spellBookMenu.setItem(3, minecartButton);
        spellBookMenu.setItem(4, gustButton);
        spellBookMenu.setItem(5, flyButton);
        spellBookMenu.setItem(6, GPButton);

        player.openInventory(spellBookMenu);
    }

    public ItemStack createSpellButton(String spellName, Material material) {
        ItemStack spellButton = new ItemStack(material);

        // customize spell button appearance
        ItemMeta meta = spellButton.getItemMeta();
        assert meta != null;

        // Set custom name based on material
        String customName = getCustomName(material);
        meta.setDisplayName(ChatColor.RESET + customName);

        // Additional customization if needed (lore, enchantments, etc.)
        spellButton.setItemMeta(meta);

        return spellButton;
    }

    public void handleSpellAddition(Player player, Material material) {
        ItemStack spellItem = new ItemStack(material);
        ItemMeta meta = spellItem.getItemMeta();
        String customName = getCustomName(material);

        assert meta != null;
        meta.setDisplayName(ChatColor.RESET + customName);
        spellItem.setItemMeta(meta);

        // check if player's inventory is full
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(spellItem);
            player.sendMessage("You've acquired the spell: " + customName);
        } else {
            player.getWorld().dropItem(player.getLocation(), spellItem);
            player.sendMessage("Your inventory is full. The spell: " + customName + " has been dropped at your location.");
        }
    }



    private String getCustomName(Material material) {
        // custom names for materials
        switch (material) {
            case BLAZE_ROD:
                return "Fiery Wand";
            case IRON_SWORD:
                return "Teleportation Blade";
            case IRON_PICKAXE:
                return "Thunder Hammer";
            case MINECART:
                return "Magical Minecart";
            case FEATHER:
                return "Gust Feather";
            case SHIELD:
                return "Winged Shield";
            case IRON_INGOT:
                return "Big Man Slam";
            default:
                return "Unknown Spell";
        }
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
            if (meta != null) {
                String spellName = ChatColor.stripColor(meta.getDisplayName()); // Get spell name without color codes

                // add spell to player's inventory
                switch (spellName) {
                    case "Fiery Wand":
                    case "Teleportation Blade":
                    case "Thunder Hammer":
                    case "Magical Minecart":
                    case "Gust Feather":
                    case "Winged Shield":
                    case "Big Man Slam":
                        handleSpellAddition(player, clickedItem.getType());

                        // close the inventory
                        // cancel event to prevent item being moved in inventory
                        player.closeInventory();
                        event.setCancelled(true);
                        break;
                }
            }
        }
    }



}
