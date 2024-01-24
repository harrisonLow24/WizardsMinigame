package WizardsGame;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    // store cooldowns in hashmaps
    private final Map<UUID, Long> fireballCooldowns = new HashMap<>();
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private final Map<UUID, Long> lightningCooldowns = new HashMap<>();

    // cooldown duration in milliseconds
    private final long fireballCooldownDuration = 1 * 1000; //10
    private final long teleportCooldownDuration = 1 * 1000; //15
    private final long lightningCooldownDuration = 1 * 1000; //15

    // Returns the remaining cooldown left
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

    // Check if spells are on cooldown
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

    // Sets the cooldown of spells
    void setFireballCooldown(UUID playerId) {
        fireballCooldowns.put(playerId, System.currentTimeMillis());
    }

    void setTeleportCooldown(UUID playerId) {
        teleportCooldowns.put(playerId, System.currentTimeMillis());
    }

    void setLightningCooldown(UUID playerId) {
        lightningCooldowns.put(playerId, System.currentTimeMillis());
    }

}
