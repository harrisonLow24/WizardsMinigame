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

import java.util.UUID;

public class SpellListener implements Listener {
    private final WizardsPlugin spellManager;
    private final SpellMenu spellMenu;
    private final WizardCommands wizardCommands;

//    SpellMenu Menu = new SpellMenu(WizardsPlugin.getInstance());

    public SpellListener(WizardsPlugin spellManager, SpellMenu spellMenu, WizardCommands wizardCommands) {
        this.spellManager = spellManager;
        this.spellMenu = spellMenu;
        this.wizardCommands = wizardCommands;
    }
    WizardCommands Comm;

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
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                (item.getType() == Material.BOOK)) {
            event.setCancelled(true); // cancel default behavior
            wizardCommands.openMenu(player, WizardCommands.MenuType.MAIN); // open wizards menu
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
        String title = event.getView().getTitle();
//        if (event.getView().getTitle().contains("'s Tombstone")) return;
        if (event.getClickedInventory() == null) return;
        // skip if not our custom inventories
        if (!title.equals("Select a Spell") && !title.equals("Chest") && !title.contains("'s Tombstone")) return;

        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        ItemStack clickedItem = event.getCurrentItem();
        Material itemType = clickedItem.getType();
        WizardsPlugin.SpellType selectedSpell = SpellMenu.getSpellByMaterial(itemType);
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
        if (event.getView().getTitle().equals("Chest")) {
            if(event.getClickedInventory().equals(player.getOpenInventory().getTopInventory())){

                if (selectedSpell != null) {
                    event.setCancelled(true); // prevent taking item from the chest
                    if (selectedSpell == WizardsPlugin.SpellType.Basic_Wand) {
                        ItemStack basicWandItem = new ItemStack(Material.STICK, 1);
                        ItemMeta meta = basicWandItem.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(ChatColor.ITALIC + "Basic Wand");
                            meta.setCustomModelData((int) System.currentTimeMillis());
                            basicWandItem.setItemMeta(meta);
                        }
                        player.getInventory().addItem(basicWandItem); // add the basic wand to player's inventory
                        clickedItem.setAmount(0); // remove item from the chest
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 3f);
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + selectedSpell.name() + "> " + ChatColor.GREEN + " Basic Wand Acquired");

                    }else{
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
                }
            }if (!event.getClickedInventory().equals(player.getOpenInventory().getTopInventory())){
                if (isSpellItem(clickedItem)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You cannot move this spell!");
                }
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
        WizardsPlugin.SpellType spellType = SpellMenu.getSpellByMaterial(item.getType());

        if (spellType != null) {
            if (spellType == WizardsPlugin.SpellType.Basic_Wand) {
                ItemStack basicWandItem = new ItemStack(Material.STICK);
                ItemMeta meta = basicWandItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Basic Wand");
                    meta.setCustomModelData((int) System.currentTimeMillis());
                    basicWandItem.setItemMeta(meta);
                }
                player.getInventory().addItem(basicWandItem); // add basic wand to the player's inventory
                player.sendMessage(ChatColor.GREEN + " Basic Wand Acquired");

            }else {
                spellManager.addSpellToPlayer(playerId, spellType);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 3f);

                if(WizardsPlugin.getSpellLevel(playerId, spellType) == 1 ){
                    player.sendMessage(ChatColor.LIGHT_PURPLE  + "" + ChatColor.BOLD + spellName + "> " + ChatColor.GREEN
                            + " Spell Acquired" );
                }else {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + spellName + "> " + ChatColor.GREEN
                            + " Level +1 -> " + ChatColor.GREEN + ChatColor.BOLD + WizardsPlugin.getSpellLevel(playerId, spellType));
                }
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
