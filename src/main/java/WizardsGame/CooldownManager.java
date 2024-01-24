package WizardsGame;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CooldownManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<UUID, Long> minecartCooldowns = new HashMap<>();
    private final long minecartCooldownDuration = 30 * 1000; // 30 seconds
    public CooldownManager() {

        // schedule the task to run every second
        scheduler.scheduleAtFixedRate(this::clearAllCooldowns, 0, 1, TimeUnit.SECONDS);
    }

    private void clearAllCooldowns() {
        // iterate through all players and clear cooldowns
        for (UUID playerId : fireballCooldowns.keySet()) {
            clearCooldowns(playerId);
        }
        for (UUID playerId : teleportCooldowns.keySet()) {
            clearCooldowns(playerId);
        }
        for (UUID playerId : lightningCooldowns.keySet()) {
            clearCooldowns(playerId);
        }
        for (UUID playerId : gustCooldowns.keySet()) {
            clearCooldowns(playerId);
        }
        for (UUID playerId : iceSphereCooldowns.keySet()) {
            clearCooldowns(playerId);
        }
        for (UUID playerId : minecartCooldowns.keySet()) {
            clearCooldowns(playerId);
        }
    }
    // store cooldowns in hashmaps
    private final Map<UUID, Long> fireballCooldowns = new HashMap<>();
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private final Map<UUID, Long> lightningCooldowns = new HashMap<>();
    private final Map<UUID, Long> gustCooldowns = new HashMap<>();
    private final Map<UUID, Long> iceSphereCooldowns = new HashMap<>();

    // cooldown duration in milliseconds
    private final long fireballCooldownDuration = 10 * 1000; //10
    private final long teleportCooldownDuration = 15 * 1000; //15
    private final long lightningCooldownDuration = 15 * 1000; //15
    private final long gustCooldownDuration = 15 * 1000; // 15
    private final long iceSphereCooldownDuration = 20 * 1000; // 20 seconds



    // returns the remaining cooldown left
    int getRemainingFireballCooldownSeconds(UUID playerId) {
        // remaining fireball cooldown
        long remainingCooldown = fireballCooldownDuration - (System.currentTimeMillis() - fireballCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }

    int getRemainingTeleportCooldownSeconds(UUID playerId) {
        // remaining teleportation cooldown
        long remainingCooldown = teleportCooldownDuration - (System.currentTimeMillis() - teleportCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }

    int getRemainingLightningCooldownSeconds(UUID playerId) {
        // remaining teleportation cooldown
        long remainingCooldown = lightningCooldownDuration - (System.currentTimeMillis() - lightningCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingGustCooldownSeconds(UUID playerId) {
        long remainingCooldown = gustCooldownDuration - (System.currentTimeMillis() - gustCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingIceSphereCooldownSeconds(UUID playerId) {
        long remainingCooldown = iceSphereCooldownDuration - (System.currentTimeMillis() - iceSphereCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingMinecartCooldownSeconds(UUID playerId) {
        long remainingCooldown = minecartCooldownDuration - (System.currentTimeMillis() - minecartCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }



    // check if spells are on cooldown
    boolean isOnFireballCooldown(UUID playerId) {
        // Check if player is on fireball cooldown
        return fireballCooldowns.containsKey(playerId) && System.currentTimeMillis() - fireballCooldowns.get(playerId) < fireballCooldownDuration;
    }

    boolean isOnTeleportCooldown(UUID playerId) {
        // Check if player is on teleportation cooldown
        return teleportCooldowns.containsKey(playerId) && System.currentTimeMillis() - teleportCooldowns.get(playerId) < teleportCooldownDuration;
    }

    boolean isOnLightningCooldown(UUID playerId) {
        return lightningCooldowns.containsKey(playerId) && System.currentTimeMillis() - lightningCooldowns.get(playerId) < lightningCooldownDuration;
    }
    boolean isOnGustCooldown(UUID playerId) {
        return gustCooldowns.containsKey(playerId) && System.currentTimeMillis() - gustCooldowns.get(playerId) < gustCooldownDuration;
    }
    boolean isOnIceSphereCooldown(UUID playerId) {
        return iceSphereCooldowns.containsKey(playerId) && System.currentTimeMillis() - iceSphereCooldowns.get(playerId) < iceSphereCooldownDuration;
    }
    boolean isOnMinecartCooldown(UUID playerId) {
        return minecartCooldowns.containsKey(playerId) && System.currentTimeMillis() - minecartCooldowns.get(playerId) < minecartCooldownDuration;
    }


    // sets the cooldown of spells
    void setFireballCooldown(UUID playerId) {
        fireballCooldowns.put(playerId, System.currentTimeMillis());
    }

    void setTeleportCooldown(UUID playerId) {
        teleportCooldowns.put(playerId, System.currentTimeMillis());
    }

    void setLightningCooldown(UUID playerId) {
        lightningCooldowns.put(playerId, System.currentTimeMillis());
    }
    void setGustCooldown(UUID playerId) {
        gustCooldowns.put(playerId, System.currentTimeMillis());
    }
    void setIceSphereCooldown(UUID playerId) {
        iceSphereCooldowns.put(playerId, System.currentTimeMillis());
    }
    void setMinecartCooldown(UUID playerId) {
        minecartCooldowns.put(playerId, System.currentTimeMillis());
    }



    void clearCooldowns(UUID playerId) {
        fireballCooldowns.remove(playerId);
        teleportCooldowns.remove(playerId);
        lightningCooldowns.remove(playerId);
        gustCooldowns.remove(playerId);
        iceSphereCooldowns.remove(playerId);
        minecartCooldowns.remove(playerId);
    }
}
