package WizardsGame;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SquidFlight{
    private final Map<UUID, Boolean> flyingMap = new HashMap<>();
    private final Map<UUID, BukkitRunnable> flyingTasks = new HashMap<>();
    private final double flyingManaCostPerTick = 1.5;

    CooldownManager Cooldown = new CooldownManager();

    @EventHandler
    public void onPlayerFall(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if (isFlying(player.getUniqueId())) {
                    event.setCancelled(true); // cancel fall damage if player is flying
                    player.setFallDistance(0); // reset fall distance
                }
            }
        }
    }
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.isFlying()) {
            if (!isFlying(playerId) && canFly(playerId)) {
                startFlyingSpell(player);
            } else {
                event.setCancelled(true); // c ancel flight if player is already flying or can't fly
            }
        } else {
            stopFlyingSpell(player);
        }
    }



    private boolean canFly(UUID playerId) {
        long currentTime = System.currentTimeMillis();
        long lastFlightTime = Cooldown.getOrDefault(playerId, 0L);

        if (currentTime - lastFlightTime >= Cooldown.getRemainingSquidFlyingCooldownSeconds(playerId)) {
            Cooldown.put(playerId, currentTime);
            return true;
        }

        return false;
    }

    public void startFlyingSpell(Player player) {
        UUID playerId = player.getUniqueId();
        if (isFlying(playerId)) {
            return;
        }

        flyingMap.put(playerId, true);
        player.playSound(player.getLocation(), Sound.ENTITY_SQUID_SQUIRT, 1.0F, 1.0F);

        // schedule a task to consume mana over time and move the player
        BukkitRunnable flyingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isFlying(playerId) || !WizardsPlugin.getInstance().Mana.hasEnoughMana(playerId, flyingManaCostPerTick)) {
                    // stop flying if the player is offline, no longer flying, or doesn't have enough mana
                    stopFlyingSpell(player);
                    cancel();
                    return;
                }
                // deduct mana per tick
                WizardsPlugin.getInstance().Mana.deductMana(playerId, flyingManaCostPerTick);
                player.setVelocity(player.getLocation().getDirection().multiply(0.5));
            }
        };
        flyingTask.runTaskTimer(WizardsPlugin.getInstance(), 0, 1); // run every tick

        // save task for later cancel
        flyingTasks.put(playerId, flyingTask);
    }

    public void stopFlyingSpell(Player player) {
        UUID playerId = player.getUniqueId();
        flyingMap.remove(playerId);

        // cancel the flying task if it exists
        BukkitRunnable flyingTask = flyingTasks.remove(playerId);
        if (flyingTask != null) {
            flyingTask.cancel();
            player.setFallDistance(0); // reset fall distance to prevent fall damage
        }
    }

    public boolean isFlying(UUID playerId) {
        return flyingMap.getOrDefault(playerId, false);
    }

}
