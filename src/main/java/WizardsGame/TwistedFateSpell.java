package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TwistedFateSpell implements Listener {
    private final CooldownManager cooldownManager = new CooldownManager();
    private final ManaManager manaManager = new ManaManager();
    private final double teleportCost = 30.0;
    private final Map<UUID, Location> teleportLocations = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ItemStack wand = player.getInventory().getItemInMainHand();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (wand.getType() == Material.END_PORTAL_FRAME) {
                if (!cooldownManager.isOntwistedFateSpellCooldown(playerId)) {
                    if (manaManager.hasEnoughMana(playerId, teleportCost)) {
                        makePlayersGlow(player);
                        player.sendMessage(ChatColor.YELLOW + "Choose a location within 10 seconds...");
                        teleportLocations.put(playerId, player.getLocation());
                        player.setInvisible(true);
                        Bukkit.getScheduler().runTaskLater(WizardsPlugin.getInstance(), () -> {
                            Location selectedLocation = teleportLocations.get(playerId);

                            if (selectedLocation != null) {
                                player.teleport(selectedLocation);
                                manaManager.deductMana(playerId, teleportCost);
                                cooldownManager.settwistedFateSpellCooldown(playerId);
                            } else {
                                player.sendMessage(ChatColor.RED + "Teleportation cancelled.");
                            }

                            removePlayersGlow(player);
                            teleportLocations.remove(playerId);
                        }, 200L); // 200L - 10s
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Twisted Fate Teleport.");
                    }
                    player.setInvisible(false);
                } else {
                    int remainingSeconds = cooldownManager.getRemainingtwistedFateSpellCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Twisted Fate Teleport on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (teleportLocations.containsKey(playerId)) {
            Location selectedLocation = getSelectedLocation(player);

            if (selectedLocation != null) {
                teleportLocations.put(playerId, selectedLocation);
                player.sendMessage(ChatColor.GREEN + "Location selected!");
            }
        }
    }
    private Location getSelectedLocation(Player player) {
        Block block = player.getTargetBlockExact(5);

        if (block != null && block.getType() != Material.AIR) {
            return block.getLocation().add(0.5, 1, 0.5);
        } else {
            player.sendMessage(ChatColor.RED + "No valid block selected.");
            return null;
        }
    }

    private void makePlayersGlow(Player caster) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(caster)) {
                onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
            }
        }
    }

    private void removePlayersGlow(Player caster) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(caster)) {
                onlinePlayer.removePotionEffect(PotionEffectType.GLOWING);
            }
        }
    }
}
