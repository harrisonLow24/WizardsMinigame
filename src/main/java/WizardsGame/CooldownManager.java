package WizardsGame;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {

    // store cooldowns in hashmaps
    private final Map<Player, Long> fireballCooldowns = new HashMap<>();
    private final Map<Player, Long> teleportCooldowns = new HashMap<>();
    private final Map<Player, Long> lightningCooldowns = new HashMap<>();


    // cooldown duration in milliseconds
    private final long fireballCooldownDuration = 1 * 1000; //10
    private final long teleportCooldownDuration = 1 * 1000; //15
    private final long lightningCooldownDuration = 1 * 1000; //15


    // Returns the remaining cooldown left
    int getRemainingFireballCooldownSeconds(Player player) {
        // remaining fireball cooldown
        long remainingCooldown = fireballCooldownDuration - (System.currentTimeMillis() - fireballCooldowns.getOrDefault(player, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingTeleportCooldownSeconds(Player player) {
        // remaining teleportation cooldown
        long remainingCooldown = teleportCooldownDuration - (System.currentTimeMillis() - teleportCooldowns.getOrDefault(player, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingLightningCooldownSeconds(Player player) {
        // remaining teleportation cooldown
        long remainingCooldown = lightningCooldownDuration - (System.currentTimeMillis() - lightningCooldowns.getOrDefault(player, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }

    // Check if spells are on cooldowm
    boolean isOnFireballCooldown(Player player) {
        // Check if player is on fireball cooldown
        return fireballCooldowns.containsKey(player) && System.currentTimeMillis() - fireballCooldowns.get(player) < fireballCooldownDuration;
    }
    boolean isOnTeleportCooldown(Player player) {
        // Check if player is on teleportation cooldown
        return teleportCooldowns.containsKey(player) && System.currentTimeMillis() - teleportCooldowns.get(player) < teleportCooldownDuration;
    }
    boolean isOnLightningCooldown(Player player) {
        return lightningCooldowns.containsKey(player) && System.currentTimeMillis() - lightningCooldowns.get(player) < lightningCooldownDuration;
    }


    // Sets the cooldown of spells
    void setFireballCooldown(Player player) {
        fireballCooldowns.put(player, System.currentTimeMillis());
    }
    void setTeleportCooldown(Player player) {
        teleportCooldowns.put(player, System.currentTimeMillis());
    }
    void setLightningCooldown(Player player) {
        lightningCooldowns.put(player, System.currentTimeMillis());
    }

}
