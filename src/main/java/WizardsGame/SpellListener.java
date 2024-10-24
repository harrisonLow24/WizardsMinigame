package WizardsGame;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SpellListener implements Listener {
    private final WizardsPlugin spellManager;
    private final SpellMenu spellMenu;

    public SpellListener(WizardsPlugin spellManager, SpellMenu spellMenu) {
        this.spellManager = spellManager;
        this.spellMenu = spellMenu;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
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
        if (event.getView().getTitle().equals("Select a Spell")) {
            Player player = (Player) event.getWhoClicked();
            spellMenu.handleMenuClick(event, player);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ItemStack item = event.getItem().getItemStack();
        WizardsPlugin.SpellType spellType = spellMenu.getSpellByMaterial(item.getType());

        if (spellType != null) {
            spellManager.addSpellToPlayer(playerId, spellType); // increase spell level when picked up
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
