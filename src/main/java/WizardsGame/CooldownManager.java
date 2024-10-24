package WizardsGame;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CooldownManager {


//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public CooldownManager() {

        // schedule the task to run every second
//        scheduler.scheduleAtFixedRate(this::clearAllCooldowns, 0, 1, TimeUnit.SECONDS);
    }

    public void toggleCooldowns(UUID playerId) {
        boolean currentStatus = cooldownsDisabledMap.getOrDefault(playerId, false);
        cooldownsDisabledMap.put(playerId, !currentStatus);

        // if cooldowns are disabled, clear cooldowns for the player
//        Bukkit.getScheduler().runTaskTimer(WizardsPlugin.getInstance(), new Runnable() {
//            @Override
//            public void run() {
//                clearCooldowns(playerId);
//                // debug
//                Bukkit.getLogger().info("Cooldowns cleared for player " + playerId);
//            }
//        }, 0L, 20L);
        if (hasCooldownsDisabled(playerId)) {
            clearCooldowns(playerId);
        }
    }

    public boolean hasCooldownsDisabled(UUID playerId) {
        return cooldownsDisabledMap.getOrDefault(playerId, false);
    }


    void clearCooldowns(UUID playerId) {
        fireballCooldowns.remove(playerId);
        minecartCooldowns.remove(playerId);
        squidFlyingCooldowns.remove(playerId);
        teleportCooldowns.remove(playerId);
        lightningCooldowns.remove(playerId);
        gustCooldowns.remove(playerId);
        GPCooldowns.remove(playerId);
        VoidOrbCooldowns.remove(playerId);
        MapTeleportCooldowns.remove(playerId);
        MeteorCooldowns.remove(playerId);
        HealCloudCooldowns.remove(playerId);
        twistedFateSpellCooldowns.remove(playerId);
        charmCooldowns.remove(playerId);
        porkchopCooldowns.remove(playerId);
    }
    // store cooldowns in hashmaps

    final Map<UUID, Long> cooldowns = new HashMap<>();
    final Map<UUID, Long> minecartCooldowns = new HashMap<>();
    final Map<UUID, Long> squidFlyingCooldowns = new HashMap<>();
    final Map<UUID, Long> porkchopCooldowns = new HashMap<>();
    final Map<UUID, Long> charmCooldowns = new HashMap<>();
    final Map<UUID, Boolean> cooldownsDisabledMap = new HashMap<>();
    final Map<UUID, Long> twistedFateSpellCooldowns = new HashMap<>();
    final Map<UUID, Long> fireballCooldowns = new HashMap<>();
    final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    final Map<UUID, Long> lightningCooldowns = new HashMap<>();
    final Map<UUID, Long> gustCooldowns = new HashMap<>();
    final Map<UUID, Long> GPCooldowns = new HashMap<>();
    final Map<UUID, Long> VoidOrbCooldowns = new HashMap<>();
    final Map<UUID, Long> MapTeleportCooldowns = new HashMap<>();
    final Map<UUID, Long> RecallCooldowns = new HashMap<>();
//    private final Map<UUID, Long> cloneCooldowns = new HashMap<>();
    final Map<UUID, Long> MeteorCooldowns = new HashMap<>();
    final Map<UUID, Long> HealCloudCooldowns = new HashMap<>    ();
    final Map<UUID, Long> frostBarrierCooldowns = new HashMap<>();

    final Map<UUID, Long> iceSphereCooldowns = new HashMap<>    ();

    // cooldown duration in milliseconds
    final long fireballCooldownDuration = 2 * 1000; // 10
    final long teleportCooldownDuration = 1 * 1000; // 15
    final long lightningCooldownDuration = 7 * 1000; // 15
    final long gustCooldownDuration = 1 * 1000; // 15
    final long iceSphereCooldownDuration = 1 * 1000; // 20 seconds
    final long minecartCooldownDuration = 1 * 1000; // 30 seconds
    final long GPCooldownDuration = 1 * 1000; // 15 seconds
    final long squidFlyingCooldownDuration = 1 * 1000; // 25 seconds
    final long MapTeleportCooldownDuration = 30 * 1000; // 25 seconds
    final long RecallCooldownDuration = 1 * 1000;
    final long MeteorCooldownDuration = 20 * 1000; // 25 seconds
    final long HealCloudCooldownDuration = 1 * 1000; // 25 seconds
    final long VoidOrbCooldownDuration = 1 * 1000; // 25 seconds
    //    private final long cloneCooldownDuration = 1 * 1000; // 30 seconds
    final long frostBarrierCooldownDuration = 1 * 1000; // 25 seconds
    final long porkchopCooldownDuration = 1 * 1000; // 12 seconds
    final long twistedFateSpellDuration = 1 * 1000; // 30 seconds
    final long charmSpellDuration = 1 * 1000; // 30 seconds

    // returns the remaining cooldown left
    int getRemainingFireballCooldownSeconds(UUID playerId) {
        // remaining fireball cooldown
        long remainingCooldown = fireballCooldownDuration - (System.currentTimeMillis() - fireballCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }

    int getRemainingRecallCooldownSeconds(UUID playerId) {
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
    int getRemainingMeteorCooldownSeconds(UUID playerId) {
        long remainingCooldown = MeteorCooldownDuration - (System.currentTimeMillis() - MeteorCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingHealCloudCooldownSeconds(UUID playerId) {
        long remainingCooldown = HealCloudCooldownDuration - (System.currentTimeMillis() - HealCloudCooldowns.getOrDefault(playerId, 0L));
        return (int) Math.ceil(remainingCooldown / 1000.0);
    }
    int getRemainingVoidOrbCooldownSeconds(UUID playerId) {
        long remainingCooldown = VoidOrbCooldownDuration - (System.currentTimeMillis() - VoidOrbCooldowns.getOrDefault(playerId, 0L));
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
//    int getRemainingCloneCooldown(UUID playerId) {
//        long remainingCooldown = cloneCooldownDuration - (System.currentTimeMillis() - cloneCooldowns.getOrDefault(playerId, 0L));
//        return (int) Math.ceil(remainingCooldown / 1000.0);
//    }


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
    boolean isOnRecallCooldown(UUID playerId) {
        return RecallCooldowns.containsKey(playerId) && System.currentTimeMillis() - RecallCooldowns.get(playerId) < RecallCooldownDuration;
    }
//    public boolean isOnCloneCooldown(UUID playerId) {
//        return cloneCooldowns.containsKey(playerId) && (System.currentTimeMillis() - cloneCooldowns.get(playerId) < cloneCooldownDuration);
//    }
    boolean isOnMeteorCooldown(UUID playerId) {
    return MeteorCooldowns.containsKey(playerId) && System.currentTimeMillis() - MeteorCooldowns.get(playerId) < MeteorCooldownDuration;
    }
    boolean isOnVoidOrbCooldown(UUID playerId) {
        return VoidOrbCooldowns.containsKey(playerId) && System.currentTimeMillis() - VoidOrbCooldowns.get(playerId) < VoidOrbCooldownDuration;
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
    boolean isOnHealCloudCooldown(UUID playerId) {
        return HealCloudCooldowns.containsKey(playerId) && System.currentTimeMillis() - HealCloudCooldowns.get(playerId) < HealCloudCooldownDuration;
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
//    void setCloneCooldown(UUID playerId) {
//        cloneCooldowns.put(playerId, System.currentTimeMillis());
//    }
    void setMeteorCooldown(UUID playerId) {
        MeteorCooldowns.put(playerId, System.currentTimeMillis());
    }
    void setHealCloudCooldown(UUID playerId) {
        HealCloudCooldowns.put(playerId, System.currentTimeMillis());
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
    void setVoidOrbCooldown(UUID playerId) {
        VoidOrbCooldowns.put(playerId, System.currentTimeMillis());
    }

    public long getOrDefault(UUID playerId, long defaultValue) {
        return cooldowns.getOrDefault(playerId, defaultValue);
    }
    public void put(UUID playerId, long currentTime) {
        cooldowns.put(playerId, currentTime);
    }

}