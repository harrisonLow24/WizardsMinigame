package WizardsGame;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CharmSpell implements Listener {

    private Map<UUID, Location> charmTargets = new HashMap<>();
    private Map<UUID, Long> charmCooldowns = new HashMap<>();

    // in seconds
    private int charmCooldownDuration = 30;
    private double charmRadius = 5.0;
    private int charmDuration = 4;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // check if player right-clicked with beetroot
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.BEETROOT) {
                // check charm cooldown
                if (charmCooldowns.containsKey(player.getUniqueId())) {
                    long lastTime = charmCooldowns.get(player.getUniqueId());
                    long currentTime = System.currentTimeMillis() / 1000;
                    if (currentTime - lastTime < charmCooldownDuration) {
                        player.sendMessage(ChatColor.RED + "Charm spell is on cooldown!");
                        return;
                    }
                }

                // cast charm spell
                castCharmSpell(player.getUniqueId());
                charmCooldowns.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
            }
        }
    }

    void castCharmSpell(UUID playerId) {
        Player caster = Bukkit.getPlayer(playerId);

        if (caster != null) {
            Location casterLocation = caster.getEyeLocation();
            caster.getWorld().spawnParticle(Particle.HEART, casterLocation, 10, 0.2, 0.2, 0.2, 0.1);

            charmTargets.put(playerId, casterLocation);

            // schedule charm effect
            Bukkit.getScheduler().runTaskTimer(WizardsPlugin.getInstance(), () -> applyCharmEffect(playerId), 0L, 1L);

            // schedule charm nullification task
            new BukkitRunnable() {
                @Override
                public void run() {
                    cancelCharmEffect(playerId);
                }
            }.runTaskLater(WizardsPlugin.getInstance(), charmDuration * 20);
        }
    }
    void castCharmProjectile(Player caster) {
        Location casterLocation = caster.getEyeLocation();
        Vector direction = casterLocation.getDirection().normalize();

        // create particle projectile
        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                if (timer >= 20 * charmDuration) {
                    this.cancel();
                    return;
                }

                // update projectile location
                casterLocation.add(direction);
                caster.getWorld().spawnParticle(Particle.HEART, casterLocation, 1, 0, 0, 0, 0.1);

                // check for entities within charm radius
                for (Entity entity : casterLocation.getWorld().getNearbyEntities(casterLocation, charmRadius, charmRadius, charmRadius)) {
                    if (entity instanceof LivingEntity && !(entity instanceof Player && entity.getUniqueId().equals(caster.getUniqueId()))) {
                        // ap[ply charm effect to entity
                        applyCharmEffect(entity.getUniqueId());
                        this.cancel();
                        return;
                    }
                }

                timer++;
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0L, 1L);
    }

    void applyCharmEffect(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);

        if (player != null && charmTargets.containsKey(player.getUniqueId())) {
            Location targetLocation = charmTargets.get(player.getUniqueId());

            // check if player is within charm radius
            if (player.getLocation().distance(targetLocation) <= charmRadius) {
                // set player's velocity to zero to prevent floating
                player.setVelocity(new Vector(0, 0, 0));
            } else {
                // force player to walk towards caster
                Vector direction = targetLocation.toVector().subtract(player.getLocation().toVector()).normalize();
                player.setVelocity(direction.multiply(0.1));
            }
        } else {
            // remove charm effect player is no longer online
            charmTargets.remove(playerId);
        }
    }
    void cancelCharmEffect(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Charm effect nullified!");
        }
        charmTargets.remove(playerId);
    }
}
