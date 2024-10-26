package WizardsGame;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
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

    private String formatSpellName(WizardsPlugin.SpellType spellType) {
        String name = spellType.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return formattedName.toString().trim();
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        ItemStack clickedItem = event.getCurrentItem();
        Material itemType = clickedItem.getType();
        WizardsPlugin.SpellType selectedSpell = Menu.getSpellByMaterial(itemType);
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
//        if (event.isShiftClick()) {
//            event.setCancelled(true);
//            return;
//        }
        if (event.getView().getTitle().equals("Select a Spell")) {
            if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE || clickedItem.getType() == Material.GRAY_DYE ||
                    clickedItem.getType() == Material.ELYTRA ||
                    clickedItem.getType() == Material.TOTEM_OF_UNDYING ||
                    clickedItem.getType() == Material.NETHERITE_SWORD) {
                //prevent interaction with stained glass
                event.setCancelled(true);
                return;
            }
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
            if (selectedSpell != null && spellManager.canSelectSpell(playerId, selectedSpell)) {
                player.getInventory().setItemInMainHand(new ItemStack(selectedSpell.getMaterial()));
                String formattedName = formatSpellName(selectedSpell);
                player.sendMessage(ChatColor.GRAY + "You have selected the spell: " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + formattedName);
                player.closeInventory();
            }
            event.setCancelled(true);
        }
        if (event.getView().getTitle().equals("Chest") ) {
            if(event.getClickedInventory().equals(player.getOpenInventory().getTopInventory())){

                if (selectedSpell != null) {
                    event.setCancelled(true); // prevent taking item from the chest
                    spellManager.addSpellToPlayer(playerId, selectedSpell); // increase spell level
                    clickedItem.setAmount(0); // remove item from the chest
                    if(WizardsPlugin.getSpellLevel(playerId, selectedSpell) == 1 ){
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 3f);
                        player.sendMessage(ChatColor.LIGHT_PURPLE  + "" + ChatColor.BOLD + selectedSpell.name() + "> " + ChatColor.GREEN
                                + " Spell Acquired" );
                    }else {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 3f);
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + selectedSpell.name() + "> " + ChatColor.GREEN
                                + " Level +1 -> " + ChatColor.GREEN + "" + ChatColor.BOLD + WizardsPlugin.getSpellLevel(playerId, selectedSpell));
                    }
                }
            }if (!event.getClickedInventory().equals(player.getOpenInventory().getTopInventory())){
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED +"" + ChatColor.BOLD + "You cannot move this spell!");
            }
        }

    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Item itemEntity = event.getItem();
        ItemStack item = event.getItem().getItemStack();
        String spellName = WizardsPlugin.getSpellInfo(item);
        WizardsPlugin.SpellType spellType = spellMenu.getSpellByMaterial(item.getType());

        if (spellType != null) {
            spellManager.addSpellToPlayer(playerId, spellType);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 3f);

            if(WizardsPlugin.getSpellLevel(playerId, spellType) == 1 ){
                player.sendMessage(ChatColor.LIGHT_PURPLE  + "" + ChatColor.BOLD + spellName + "> " + ChatColor.GREEN
                        + " Spell Acquired" );
            }else {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "" + spellName + "> " + ChatColor.GREEN
                        + " Level +1 -> " + ChatColor.GREEN + "" + ChatColor.BOLD + WizardsPlugin.getSpellLevel(playerId, spellType));
            }
            itemEntity.remove();
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        long currentTime = System.currentTimeMillis();

        // check if dropped item is a spell
        if (isSpellItem(droppedItem)) {
            if (WizardsPlugin.lastDropMessage.containsKey(player) && (currentTime - WizardsPlugin.lastDropMessage.get(player) < WizardsPlugin.MESSAGE_COOLDOWN)) {
                event.setCancelled(true); // cancel drop
                return;
            }
            // cancel the drop & set cooldown
            event.setCancelled(true);
            WizardsPlugin.lastDropMessage.put(player, currentTime);
            player.sendMessage(ChatColor.RED + "You cannot drop spells!");
        }
    }

    static boolean isSpellItem(ItemStack item) {
        for (WizardsPlugin.SpellType spell : WizardsPlugin.SpellType.values()) {
            if (spell.getMaterial() == item.getType()) {
                return true;
            }
        }
        return false;
    }
}
