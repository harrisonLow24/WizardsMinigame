package WizardsGame;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    // store cooldowns by player ID and spell name
    private final Map<UUID, Map<String, Long>> playerCooldowns = new HashMap<>();

    // store cooldown durations for each spell
    private static final Map<String, Long> cooldownDurations = new HashMap<>();

    // initialize spell cooldown durations
    public CooldownManager() {
        cooldownDurations.put("Fiery Wand", 2 * 1000L);
        cooldownDurations.put("Shrouded Step", 1 * 1000L);
        cooldownDurations.put("Mjölnir", 7 * 1000L);
        cooldownDurations.put("Gust", 1 * 1000L);
        cooldownDurations.put("Big Man Slam", 1 * 1000L);
        cooldownDurations.put("The Great Escape", 1 * 1000L);
        cooldownDurations.put("Winged Shield", 1 * 1000L);
        cooldownDurations.put("Voidwalker", 30 * 1000L);
        cooldownDurations.put("Starfall Barrage", 20 * 1000L);
        cooldownDurations.put("Heal Cloud", 1 * 1000L);
        cooldownDurations.put("Void Orb", 1 * 1000L);
        cooldownDurations.put("Dragon Spit", 1 * 1000L);
        cooldownDurations.put("Cod Shooter", 1 * 1000L);
        cooldownDurations.put("Recall", 10 * 1000L);
    }

    // set cooldowns for a spell
    public void setCooldown(UUID playerId, String spellName) {
        playerCooldowns
                .computeIfAbsent(playerId, k -> new HashMap<>())
                .put(spellName, System.currentTimeMillis());
    }

    // check if a spell is on cooldown
    public boolean isOnCooldown(UUID playerId, String spellName) {
        if (!playerCooldowns.containsKey(playerId)) {
            return false;
        }
        Long lastUsed = playerCooldowns.get(playerId).get(spellName);
        if (lastUsed == null) {
            return false;
        }
        long duration = cooldownDurations.getOrDefault(spellName, 0L);
        return System.currentTimeMillis() - lastUsed < duration;
    }

    // get remaining cooldown time in seconds
    public int getRemainingCooldown(UUID playerId, String spellName) {
        if (!playerCooldowns.containsKey(playerId)) {
            return 0;
        }
        Long lastUsed = playerCooldowns.get(playerId).get(spellName);
        if (lastUsed == null) {
            return 0;
        }
        long duration = cooldownDurations.getOrDefault(spellName, 0L);
        long remaining = duration - (System.currentTimeMillis() - lastUsed);
        return (int) Math.ceil(remaining / 1000.0);
    }
    public static long getCooldownDuration(String spellName) {
        return cooldownDurations.getOrDefault(spellName, 0L);
    }

    // clear all cooldowns for a player
    public void clearCooldowns(UUID playerId) {
        playerCooldowns.remove(playerId);
    }

    // toggle cooldowns for player
    private final Map<UUID, Boolean> cooldownsDisabledMap = new HashMap<>();

    public void toggleCooldowns(UUID playerId) {
        boolean currentStatus = cooldownsDisabledMap.getOrDefault(playerId, false);
        cooldownsDisabledMap.put(playerId, !currentStatus);

        if (hasCooldownsDisabled(playerId)) {
            clearCooldowns(playerId);
        }
    }

    public boolean hasCooldownsDisabled(UUID playerId) {
        return cooldownsDisabledMap.getOrDefault(playerId, false);
    }
}
