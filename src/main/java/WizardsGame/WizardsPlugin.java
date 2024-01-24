package WizardsGame;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class WizardsPlugin extends JavaPlugin implements Listener {
    SpellCastingManager Cast = new SpellCastingManager();
    CooldownManager Cooldown = new CooldownManager();
    TeleportationManager Teleport = new TeleportationManager();
//    Mana Mana = new Mana();

    @Override
    public void onEnable() {
        getLogger().info("WizardsPlugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("WizardsPlugin has been disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Give a default wand to players on join
        Player player = event.getPlayer();
//        Mana.setCurrentMana(player,Mana.maxMana);
        player.getInventory().addItem(new ItemStack(Material.BLAZE_ROD));
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack wand = player.getInventory().getItemInMainHand();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Check if the player used a wand
            if (wand.getType() == Material.IRON_PICKAXE) {
                // Lightning spell
                if (!Cooldown.isOnLightningCooldown(player)) {
                    Cast.castLightningSpell(player);

                    Cooldown.setLightningCooldown(player);
                } else {
                    // Player is on teleportation cooldown
                    int remainingSeconds = Cooldown.getRemainingLightningCooldownSeconds(player);
                    player.sendMessage(ChatColor.RED + "Lightning on cooldownb. Please wait " + remainingSeconds + " seconds.");
                }

            }


            if (wand.getType() == Material.IRON_SWORD) {
                // Teleportation spell
                if (!Cooldown.isOnTeleportCooldown(player)) {
                    // Implement teleportation abilities
                    Teleport.teleportSpell(player);

                    // Set the teleportation cooldown for the player
                    Cooldown.setTeleportCooldown(player);
                } else {
                    // Player is on teleportation cooldown
                    int remainingSeconds = Cooldown.getRemainingTeleportCooldownSeconds(player);
                    player.sendMessage(ChatColor.RED + "Teleportation on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }


            if (wand.getType() == Material.BLAZE_ROD) {
                // Fireball spell
                if (!Cooldown.isOnFireballCooldown(player)) {
                    // Implement fireball abilities
                    Cast.castFireball(player);

                    // Set the fireball cooldown for the player
                    Cooldown.setFireballCooldown(player);
                } else {
                    // Player is on fireball cooldown
                    int remainingSeconds = Cooldown.getRemainingFireballCooldownSeconds(player);
                    player.sendMessage(ChatColor.RED + "Fireball on cooldown. Please wait " + remainingSeconds + " seconds before casting again.");
                }
            }
        }
    }
}