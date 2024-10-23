package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.UUID;

public class SpellMenu {
    private final WizardsPlugin spellManager;

    public SpellMenu(WizardsPlugin spellManager) {
        this.spellManager = spellManager;
    }

    public void openSpellMenu(Player player) {
        UUID playerId = player.getUniqueId();
        Inventory menu = Bukkit.createInventory(null, 27, "Select a Spell");

        // populate menu with spells the player owns
        for (WizardsPlugin.SpellType spellType : WizardsPlugin.SpellType.values()) {
            int spellLevel = spellManager.getSpellLevel(playerId, spellType);
            ItemStack spellItem = new ItemStack(spellType.getMaterial());
            ItemMeta meta = spellItem.getItemMeta();
            meta.setDisplayName(spellType.name() + " (Level " + spellLevel + ")");
            spellItem.setItemMeta(meta);

            // if spell is unowned, make it unselectable
            if (spellLevel == 0) {
                meta.setLore(Collections.singletonList("Not owned!"));
                spellItem.setType(Material.BARRIER); // show as a barrier if not owned
            } else {
                meta.setLore(Collections.singletonList("Level: " + spellLevel));
            }

            menu.addItem(spellItem);
        }

        player.openInventory(menu);
    }

    public void handleMenuClick(InventoryClickEvent event, Player player) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.BARRIER) {
            event.setCancelled(true);
            return; // do not allow interaction if spell is unowned
        }

        UUID playerId = player.getUniqueId();
        Material itemType = clickedItem.getType();
        WizardsPlugin.SpellType selectedSpell = getSpellByMaterial(itemType);

        if (selectedSpell != null && spellManager.canSelectSpell(playerId, selectedSpell)) {
            player.getInventory().setItemInMainHand(new ItemStack(selectedSpell.getMaterial()));
            player.sendMessage("You have selected the spell: " + selectedSpell.name());
            player.closeInventory();
        }

        event.setCancelled(true);
    }

    WizardsPlugin.SpellType getSpellByMaterial(Material material) {
        for (WizardsPlugin.SpellType spell : WizardsPlugin.SpellType.values()) {
            if (spell.getMaterial() == material) {
                return spell;
            }
        }
        return null;
    }
}
