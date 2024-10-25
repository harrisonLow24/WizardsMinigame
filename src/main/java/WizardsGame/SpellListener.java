package WizardsGame;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.*;
import java.util.UUID;

public class SpellListener implements Listener {
    private final WizardsPlugin spellManager;
    private final SpellMenu spellMenu;
    SpellMenu Menu = new SpellMenu(WizardsPlugin.getInstance());

    public SpellListener(WizardsPlugin spellManager, SpellMenu spellMenu) {
        this.spellManager = spellManager;
        this.spellMenu = spellMenu;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // check if left click
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                (item.getType() == Material.STICK || isSpellItem(item))) {
            spellMenu.openSpellMenu(player); // open spell menu
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }
        if (event.getView().getTitle().equals("Select a Spell")) {
            if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE || clickedItem.getType() == Material.GRAY_DYE ||
                    clickedItem.getType() == Material.ELYTRA ||
                    clickedItem.getType() == Material.TOTEM_OF_UNDYING ||
                    clickedItem.getType() == Material.NETHERITE_SWORD) {
                //prevent interaction with stained glass
                event.setCancelled(true);
                return;
            }
            UUID playerId = player.getUniqueId();
            Material itemType = clickedItem.getType();
            WizardsPlugin.SpellType selectedSpell = Menu.getSpellByMaterial(itemType);

            if (selectedSpell != null && spellManager.canSelectSpell(playerId, selectedSpell)) {
                player.getInventory().setItemInMainHand(new ItemStack(selectedSpell.getMaterial()));
                player.sendMessage("You have selected the spell: " + selectedSpell.name());
                player.closeInventory();
            }
            event.setCancelled(true);
        }
        if (event.getView().getTitle().equals("Chest")) {
            UUID playerId = player.getUniqueId();
            Material itemType = clickedItem.getType();
            WizardsPlugin.SpellType spellType = spellMenu.getSpellByMaterial(itemType);

            // if the clicked item is a spell item
            if (spellType != null) {
                event.setCancelled(true); // prevent taking item from the chest
                spellManager.addSpellToPlayer(playerId, spellType); // increase spell level
                clickedItem.setAmount(0); // remove the item from the chest
                player.sendMessage(ChatColor.GRAY + "You have acquired the spell: " + ChatColor.GREEN + ChatColor.BOLD + spellType.name() + ChatColor.GRAY + " and its level has been increased!");
            }
        }else {
            Material itemType = clickedItem.getType();
            if (WizardsPlugin.SPELL_NAMES.containsKey(itemType)) {
                event.setCancelled(true);
                event.setCursor(null);
//                player.sendMessage(String.valueOf(WizardsPlugin.SPELL_NAMES.get(itemType)));
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Item itemEntity = event.getItem();
        ItemStack item = event.getItem().getItemStack();
        WizardsPlugin.SpellType spellType = spellMenu.getSpellByMaterial(item.getType());

        if (spellType != null) {
            spellManager.addSpellToPlayer(playerId, spellType);
            player.sendMessage(ChatColor.GRAY + "You have acquired the spell: " + ChatColor.GREEN + ChatColor.BOLD + spellType.name() + ChatColor.GRAY + " and its level has been increased!");
            itemEntity.remove();
        }

    }

    private boolean isSpellItem(ItemStack item) {
        for (WizardsPlugin.SpellType spell : WizardsPlugin.SpellType.values()) {
            if (spell.getMaterial() == item.getType()) {
                return true;
            }
        }
        return false;
    }
}
