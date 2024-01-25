package WizardsGame;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Item;

import org.bukkit.event.entity.ProjectileHitEvent;


public class WizardsPlugin extends JavaPlugin implements Listener {
    private static WizardsPlugin instance;
    SpellCastingManager Cast = new SpellCastingManager();
    CooldownManager Cooldown = new CooldownManager();
    TeleportationManager Teleport = new TeleportationManager();
    SquidFlight Squid = new SquidFlight();
    ManaManager Mana = new ManaManager();
    CharmSpell Charm = new CharmSpell();


    private final Map<UUID, Integer> lightningEffectDuration = new HashMap<>();

    private final Map<UUID, Boolean> cooldownsDisabledMap = new HashMap<>();

    final Map<UUID, Double> spellManaCost = new HashMap<>(); // hashmap of all spells' mana costs

    // porkchop variables
    private final double porkchopDamage = 5.0;
    private final double healAmount = 4.0;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("WizardsPlugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);
        SpellBookMenu spellBookMenu = new SpellBookMenu(this);
        getServer().getPluginManager().registerEvents(new TeleportationManager(), this);

        // commands
        Objects.requireNonNull(getCommand("toggleinfinitemana")).setExecutor(new WizardCommands(this));
        Objects.requireNonNull(getCommand("togglecooldowns")).setExecutor(new WizardCommands(this));


        // mana bar updated every 20 ticks / 1 second

        // note to self: may remove this scheduler, as the proceeding one updates the bar every second
        // however, this makes sure the mana bar will ALWAYS update when a spell is used for consistency

        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Mana.updateManaActionBar(onlinePlayer);
            }
        }, 0, 10);

        // mana bar updated every 20 ticks / 1 second for MANA REGEN
        getServer().getScheduler().runTaskTimer(this, this::regenerateMana, 0, 20);
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Mana.updateManaActionBar(onlinePlayer);
            }
        }, 0, 10);


    }

    public static WizardsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        getLogger().info("WizardsPlugin has been disabled!");
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // get player and UUID
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        player.sendMessage("Welcome!");

        //set players' mana to max on join
        Mana.playerMana.put(playerId, Mana.maxMana);
        Mana.manaBossBars.remove(playerId);

    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ItemStack wand = player.getInventory().getItemInMainHand();

        //spell mana cost
        double fireballCost = 15.0;
        double teleportCost = 10.0;
        double lightningCost = 20.0;
        double gustCost = 25.0;
        double minecartCost = 30.0;
        double flyingManaCostPerTick = 1.5;
        double charmCost = 15.0;
        double porkchopCost = 10;


        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            // fireball cast
            if (wand.getType() == Material.BLAZE_ROD) {

                if (!Cooldown.isOnFireballCooldown(playerId)) { // if fireball is not on cooldown
                    if (Mana.hasEnoughMana(playerId, fireballCost)) { // AND if player has enough mana
                        Cast.castFireball(playerId);            // fireball is cast, and a cooldown + mana reduction is set
                        Cooldown.setFireballCooldown(playerId);
                        Mana.deductMana(playerId, fireballCost);
//                        updateManaActionBar(player); // old mana bar update before scheduler
//                        player.sendMessage("You have " + getCurrentMana(playerId) + " mana remaining"); // old mana bar before action bar
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Fireball.");
                    }
                }else{
                    int remainingSeconds = Cooldown.getRemainingFireballCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Fireball on cooldown. Please wait " + remainingSeconds + " seconds before casting again.");
                }
            }
            if (wand.getType() == Material.IRON_SWORD) {
                // teleport cast
                if (!Cooldown.isOnTeleportCooldown(playerId)) { // if teleport is not on cooldown
                    if (Mana.hasEnoughMana(playerId, teleportCost)){ // AND if player has enough mana
                        Teleport.castTeleportSpell(playerId, 0); // teleport is cast, and a cooldown + mana reduction is set
                        Cooldown.setTeleportCooldown(playerId);
                        Mana.deductMana(playerId, teleportCost);
                    }else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Teleport.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingTeleportCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Teleportation on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
            if (wand.getType() == Material.IRON_PICKAXE) {
                // lightning cast
                if (!Cooldown.isOnLightningCooldown(playerId)) { // if teleport is not on cooldown
                    if (Mana.hasEnoughMana(playerId, lightningCost)) { // AND if player has enough mana
                        Cast.castLightningSpell(playerId);      // lightning is cast, and a cooldown + mana reduction is set
                        Cooldown.setLightningCooldown(playerId);
                        Mana.deductMana(playerId, lightningCost);
                        startLightningEffect(playerId);
                    }else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Lightning.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingLightningCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Lightning on cooldown. Please wait " + remainingSeconds + " seconds.");
                }

            }
            if (wand.getType() == Material.FEATHER) {
                if (!Cooldown.isOnGustCooldown(playerId)) {  // if gust spell is not on cooldown
                    if (Mana.hasEnoughMana(playerId, gustCost)) { // AND if player has enough mana
                        Cast.castGustSpell(playerId);          // gust is cast, and a cooldown + mana reduction is set
                        Cooldown.setGustCooldown(playerId);
                        Mana.deductMana(playerId, gustCost);
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Gust.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingGustCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Gust spell on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
            if (wand.getType() == Material.MINECART) {
                // minecart spell
                if (!Cooldown.isOnMinecartCooldown(playerId)) {
                    if (Mana.hasEnoughMana(playerId, minecartCost)) {
                        Cast.launchMinecart(player);
                        Cooldown.setMinecartCooldown(playerId);
                        Mana.deductMana(playerId, minecartCost);
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Minecart spell.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingMinecartCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Minecart spell on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
            if (wand.getType() == Material.IRON_INGOT) {
                // minecart spell cast
                if (!Cooldown.isOnMinecartCooldown(playerId)) {
                    if (Mana.hasEnoughMana(playerId, minecartCost)) {
                        Cast.castGroundPoundSpell(playerId);
                        Cooldown.setMinecartCooldown(playerId);
                        Mana.deductMana(playerId, minecartCost);
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Minecart spell.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingMinecartCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Minecart spell on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
            if (wand.getType() == Material.SHIELD) {
                if (!Cooldown.isOnSquidFlyingCooldown(playerId)) {
                    if (Mana.hasEnoughMana(playerId, flyingManaCostPerTick)) {
                        // flying spell
                        Squid.startFlyingSpell(player);

                        // initial mana cost
                        Mana.deductMana(playerId, flyingManaCostPerTick);

                        // schedule a task to consume mana over time
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!player.isOnline() || !Squid.isFlying(playerId) || !Mana.hasEnoughMana(playerId, flyingManaCostPerTick)) {
                                    // stop flying if player is offline, no longer flying, or not enough enough mana
                                    Squid.stopFlyingSpell(player);
                                    this.cancel();
                                    return;
                                }
                                // deduct mana per tick
                                Mana.deductMana(playerId, flyingManaCostPerTick);
                            }
                        }.runTaskTimer(this, 0, 20); // run every 1 second = 20 ticks

                        // squid sound effect
                        player.playSound(player.getLocation(), Sound.ENTITY_SQUID_SQUIRT, 1.0F, 1.0F);
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Flying.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingSquidFlyingCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Flying spell on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
            if (wand.getType() == Material.IRON_SHOVEL) {
                // porkchop spell
                if (!Cooldown.isOnPorkchopCooldown(playerId)) {
                    if (Mana.hasEnoughMana(playerId, porkchopCost)) {
                        Cast.castPorkchopSpell(player);
                        Cooldown.setPorkchopCooldown(playerId);
                        Mana.deductMana(playerId, porkchopCost);
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Porkchop spell.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingPorkchopCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Porkchop spell on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }
            if (wand.getType() == Material.BEETROOT) {
                // charm spell cast
                if (!Cooldown.isOnCharmCooldown(playerId)) {
                    if (Mana.hasEnoughMana(playerId, charmCost)) {
                        Charm.castCharmSpell(playerId);
                        Cooldown.setCharmCooldown(playerId);
                        Mana.deductMana(playerId, charmCost);
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough mana to cast Charm spell.");
                    }
                } else {
                    int remainingSeconds = Cooldown.getRemainingCharmCooldownSeconds(playerId);
                    player.sendMessage(ChatColor.RED + "Charm spell on cooldown. Please wait " + remainingSeconds + " seconds.");
                }
            }

        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) {
            Location hitLocation = event.getEntity().getLocation();

            // check if snowball hit a block
            if (hitLocation.getBlock().getType() != Material.AIR) {
                // create a temporary sphere of ice
                createIceSphere(hitLocation);
            }
        }
    }

    // porkchop hit event
    @EventHandler
    public void onPorkchopHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Item && event.getEntity() instanceof LivingEntity) {
            Item porkchopItem = (Item) event.getDamager();
            LivingEntity targetEntity = (LivingEntity) event.getEntity();

            if (porkchopItem.getItemStack().getType() == Material.PORKCHOP) {
                PersistentDataContainer container = porkchopItem.getItemStack().getItemMeta().getPersistentDataContainer();
                if (container.has(new NamespacedKey(this, "caster"), PersistentDataType.STRING)) {
                    UUID casterId = UUID.fromString(container.get(new NamespacedKey(this, "caster"), PersistentDataType.STRING));
                    Player caster = getPlayerById(casterId);
                    if (caster != null) {
                        double damage = 10.0;
                        targetEntity.damage(damage, caster);

                        // remove the cooked Porkchop from ground
                        porkchopItem.remove();
                    }
                }
            }
        }
    }

    // regenerate mana over time
    public void regenerateMana() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            UUID playerId = onlinePlayer.getUniqueId();
            double currentMana = Mana.getCurrentMana(playerId);
            if (Mana.hasInfiniteMana(playerId)) {
                // set player's mana to maximum value
                Mana.setPlayerMana(playerId, Mana.maxMana);
            }else {
                // regen rate
                double manaRegenRate = 5.0;
                double newMana = Math.min(currentMana + manaRegenRate, Mana.maxMana);
                Mana.playerMana.put(playerId, newMana);
            }
        }
    }



    private void createIceSphere(Location location) {
        World world = location.getWorld();
        double radius = 3.0; //radius of ice sphere
        for (double x = -radius; x <= radius; x++) {
            for (double y = -radius; y <= radius; y++) {
                for (double z = -radius; z <= radius; z++) {
                    if (Math.sqrt(x * x + y * y + z * z) <= radius) {
                        Location iceBlockLocation = location.clone().add(x, y, z);
                        world.getBlockAt(iceBlockLocation).setType(Material.ICE);
                    }
                }
            }
        }
    }
    // get UUID of player
    public static Player getPlayerById(UUID playerId) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(playerId)) {
                return onlinePlayer;
            }
        }
        return null; // player with UUID not found
    }
    // action boss bar to display mana %



    // exaggerated lightning effect
    private void startLightningEffect(UUID playerId) {
        Player player = getPlayerById(playerId);
        if (player == null) {
            return;
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
        player.getWorld().strikeLightningEffect(player.getLocation());
        new BukkitRunnable() {
            double radius = 3.0;
            double height = 5.0;
            double angle = 0;

            @Override
            public void run() {
                if (angle >= 360) {
                    this.cancel();
                    return;
                }

                double x = radius * Math.cos(Math.toRadians(angle));
                double z = radius * Math.sin(Math.toRadians(angle));

                Location particleLocation = player.getLocation().clone().add(x, height, z);
                player.getWorld().spawnParticle(Particle.CRIT, particleLocation, 10, 0.2, 0.2, 0.2, 0.1);

                angle += 10;
            }
        }.runTaskTimer(this, 0, 1);

        // schedule task to simulate longer lightning effect
        int maxLightningEffectDuration = 100;
        lightningEffectDuration.put(playerId, maxLightningEffectDuration);

        getServer().getScheduler().runTaskTimer(this, () -> {
            int remainingDuration = lightningEffectDuration.getOrDefault(playerId, 0);

            if (remainingDuration <= 0) {
                lightningEffectDuration.remove(playerId);
                return;
            }

            // increase remaining duration
            lightningEffectDuration.put(playerId, remainingDuration - 1);
        }, 0, 1);
    }


    public void toggleCooldowns(UUID playerId) {
        boolean currentStatus = cooldownsDisabledMap.getOrDefault(playerId, false);
        cooldownsDisabledMap.put(playerId, !currentStatus);

        // if cooldowns are disabled, clear cooldowns for the player
        if (hasCooldownsDisabled(playerId)) {
            Cooldown.clearCooldowns(playerId);
        }
    }

    public boolean hasCooldownsDisabled(UUID playerId) {
        return cooldownsDisabledMap.getOrDefault(playerId, false);
    }

}