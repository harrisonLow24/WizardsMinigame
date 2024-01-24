package WizardsGame;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SquidFlight{
    private final Map<UUID, Boolean> flyingMap = new HashMap<>();
    private final Map<UUID, BukkitRunnable> flyingTasks = new HashMap<>();
    private final double flyingManaCostPerTick = 1.5;
    public void startFlyingSpell(Player player) {
        UUID playerId = player.getUniqueId();
        flyingMap.put(playerId, true);
        player.playSound(player.getLocation(), Sound.ENTITY_SQUID_SQUIRT, 1.0F, 1.0F);
        // schedule a task to consume mana over time and move the player
        BukkitRunnable flyingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isFlying(playerId) || !WizardsPlugin.getInstance().hasEnoughMana(playerId, flyingManaCostPerTick)) {
                    // Stop flying if player is offline, no longer flying, or doesn't have enough mana
                    stopFlyingSpell(player);
                    cancel();
                    return;
                }
                // deduct mana per tick
                WizardsPlugin.getInstance().deductMana(playerId, flyingManaCostPerTick);
                player.setVelocity(player.getLocation().getDirection().multiply(0.5));
            }
        };

        flyingTask.runTaskTimer(WizardsPlugin.getInstance(), 0, 1); // Run every tick

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
        }
    }

    public boolean isFlying(UUID playerId) {
        return flyingMap.getOrDefault(playerId, false);
    }

}
