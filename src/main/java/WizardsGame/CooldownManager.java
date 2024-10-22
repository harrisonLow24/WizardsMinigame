package WizardsGame;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CooldownManager {


    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public CooldownManager() {

        // schedule the task to run every second
        scheduler.scheduleAtFixedRate(this::clearAllCooldowns, 0, 1, TimeUnit.SECONDS);
    }

    public void toggleCooldowns(UUID playerId) {
        boolean currentStatus = cooldownsDisabledMap.getOrDefault(playerId, false);
        cooldownsDisabledMap.put(playerId, !currentStatus);

        // if cooldowns are disabled, clear cooldowns for the player
        if (hasCooldownsDisabled(playerId)) {
            clearCooldowns(playerId);
        }
    }

    public boolean hasCooldownsDisabled(UUID playerId) {
        return cooldownsDisabledMap.getOrDefault(playerId, false);
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
        for (UUID playerId : squidFlyingCooldowns.keySet()) {
            clearCooldowns(playerId);
        }
        for (UUID playerId : porkchopCooldowns.keySet()) {
            clearCooldowns(playerId);
        }
        for (UUID playerId : charmCooldowns.keySet()) {
            clearCooldowns(playerId);
        }
        for (UUID playerId : GPCooldowns.keySet()) {
            clearCooldowns(playerId);
        }
    }
    // store cooldowns in hashmaps

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Long> minecartCooldowns = new HashMap<>();
    private final Map<UUID, Long> squidFlyingCooldowns = new HashMap<>();
    private final Map<UUID, Long> porkchopCooldowns = new HashMap<>();
    private final Map<UUID, Long> charmCooldowns = new HashMap<>();
    public final Map<UUID, Boolean> cooldownsDisabledMap = new HashMap<>();
    public final Map<UUID, Long> twistedFateSpellCooldowns = new HashMap<>();
    private final Map<UUID, Long> fireballCooldowns = new HashMap<>();
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private final Map<UUID, Long> lightningCooldowns = new HashMap<>();
    private final Map<UUID, Long> gustCooldowns = new HashMap<>();
    private final Map<UUID, Long> GPCooldowns = new HashMap<>();
    private final Map<UUID, Long> MapTeleportCooldowns = new HashMap<>();
    private final Map<UUID, Long> frostBarrierCooldowns = new HashMap<>();

    private final Map<UUID, Long> iceSphereCooldowns = new HashMap<>();

    // cooldown duration in milliseconds
    private final long fireballCooldownDuration = 1 * 1000; // 10
    private final long teleportCooldownDuration = 1 * 1000; // 15
    private final long lightningCooldownDuration = 1 * 1000; // 15
    private final long gustCooldownDuration = 1 * 1000; // 15
    private final long iceSphereCooldownDuration = 1 * 1000; // 20 seconds
    private final long minecartCooldownDuration = 1 * 1000; // 30 seconds
    private final long GPCooldownDuration = 1 * 1000; // 15 seconds
    private final long squidFlyingCooldownDuration = 1 * 1000; // 25 seconds
    private final long MapTeleportCooldownDuration = 1 * 1000; // 25 seconds
    private final long frostBarrierCooldownDuration = 1 * 1000; // 25 seconds
    private final long porkchopCooldownDuration = 1 * 1000; // 12 seconds
    private final long twistedFateSpellDuration = 1 * 1000; // 30 seconds

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
    int getRemainingSquidFlyingCooldownSeconds(UUID playerId) {
        long remainingCooldown = squidFlyingCooldownDuration - (System.currentTimeMillis() - squidFlyingCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingMapTeleportCooldownSeconds(UUID playerId) {
        long remainingCooldown = MapTeleportCooldownDuration - (System.currentTimeMillis() - MapTeleportCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }

    int getRemainingFrostBarrierCooldownSeconds(UUID playerId) {
        long remainingCooldown = frostBarrierCooldownDuration - (System.currentTimeMillis() - frostBarrierCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingPorkchopCooldownSeconds(UUID playerId) {
        long remainingCooldown = porkchopCooldownDuration - (System.currentTimeMillis() - porkchopCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    public int getRemainingCharmCooldownSeconds(UUID playerId) {
        if (!charmCooldowns.containsKey(playerId)) {
            return 0;
        }

        long cooldownEndTime = charmCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();

        int remainingSeconds = (int) Math.max(0, (cooldownEndTime - currentTime) / 1000);
        return remainingSeconds;
    }
    int getRemainingtwistedFateSpellCooldownSeconds(UUID playerId) {
        long remainingCooldown = twistedFateSpellDuration - (System.currentTimeMillis() - twistedFateSpellCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingGPCooldownSeconds(UUID playerId) {
        long remainingCooldown = GPCooldownDuration - (System.currentTimeMillis() - GPCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }


    // check if spells are on cooldown
    boolean isOnFireballCooldown(UUID playerId) {
        return fireballCooldowns.containsKey(playerId) && System.currentTimeMillis() - fireballCooldowns.get(playerId) < fireballCooldownDuration;
    }

    boolean isOnTeleportCooldown(UUID playerId) {
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
    boolean isOnSquidFlyingCooldown(UUID playerId) {
        return squidFlyingCooldowns.containsKey(playerId) && System.currentTimeMillis() - squidFlyingCooldowns.get(playerId) < squidFlyingCooldownDuration;
    }
    boolean isOnMapTeleportCooldown(UUID playerId) {
        return MapTeleportCooldowns.containsKey(playerId) && System.currentTimeMillis() - MapTeleportCooldowns.get(playerId) < MapTeleportCooldownDuration;
    }


    boolean isOnPorkchopCooldown(UUID playerId) {
        return porkchopCooldowns.containsKey(playerId) && System.currentTimeMillis() - porkchopCooldowns.get(playerId) < porkchopCooldownDuration;
    }
    public boolean isOnCharmCooldown(UUID playerId) {
        if (!charmCooldowns.containsKey(playerId)) {
            return false;
        }

        long cooldownEndTime = charmCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();

        return currentTime < cooldownEndTime;
    }
    boolean isOntwistedFateSpellCooldown(UUID playerId) {
        return twistedFateSpellCooldowns.containsKey(playerId) && System.currentTimeMillis() - twistedFateSpellCooldowns.get(playerId) < twistedFateSpellDuration;
    }
    boolean isOnGPCooldown(UUID playerId) {
        return GPCooldowns.containsKey(playerId) && System.currentTimeMillis() - GPCooldowns.get(playerId) < GPCooldownDuration;
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
    void setSquidFlyingCooldown(UUID playerId) {
        squidFlyingCooldowns.put(playerId, System.currentTimeMillis());
    }
    void setFrostBarrierCooldown(UUID playerId) {
        frostBarrierCooldowns.put(playerId, System.currentTimeMillis());
    }
    void setMapTeleportCooldown(UUID playerId) {
        MapTeleportCooldowns.put(playerId, System.currentTimeMillis());
    }
    void setPorkchopCooldown(UUID playerId) {
        porkchopCooldowns.put(playerId, System.currentTimeMillis());
    }
    void setCharmCooldown(UUID playerId) {
        double charmDuration = 10;
        charmCooldowns.put(playerId, (long) (System.currentTimeMillis() + (charmDuration * 1000)));
    }
    void settwistedFateSpellCooldown(UUID playerId) {
        twistedFateSpellCooldowns.put(playerId, System.currentTimeMillis());
    }
    void setGPCooldown(UUID playerId) {
        GPCooldowns.put(playerId, System.currentTimeMillis());
    }



    void clearCooldowns(UUID playerId) {
        fireballCooldowns.remove(playerId);
        teleportCooldowns.remove(playerId);
        lightningCooldowns.remove(playerId);
        gustCooldowns.remove(playerId);
        iceSphereCooldowns.remove(playerId);
        minecartCooldowns.remove(playerId);
        GPCooldowns.remove(playerId);
        squidFlyingCooldowns.remove(playerId);
        porkchopCooldowns.remove(playerId);
        charmCooldowns.remove(playerId);
    }

    public long getOrDefault(UUID playerId, long defaultValue) {
        return cooldowns.getOrDefault(playerId, defaultValue);
    }
    public void put(UUID playerId, long currentTime) {
        cooldowns.put(playerId, currentTime);
    }

}