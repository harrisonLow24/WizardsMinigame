package WizardsGame;

import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;


public class WizardsPlugin extends JavaPlugin implements Listener {
    private static WizardsPlugin instance;
    SpellCastingManager Cast = new SpellCastingManager();
    CooldownManager Cooldown = new CooldownManager();
    TeleportationManager Teleport = new TeleportationManager();
    SquidFlight Squid = new SquidFlight();
    ManaManager Mana = new ManaManager();
    CharmSpell Charm = new CharmSpell();

//    TwistedFateSpell Twist = new TwistedFateSpell();

//    // porkchop variables
//    private final double porkchopDamage = 5.0;
//    private final double healAmount = 4.0;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("WizardsPlugin has been enabled!");
        registerEvents();
        registerCommands();
        startManaBarUpdateTask();
        startManaRegenTask();


    }
    void registerEvents() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new TeleportationManager(), this);
        getServer().getPluginManager().registerEvents(new TwistedFateSpell(), this);
        new SpellBookMenu(this);
    }

    // commands
    void registerCommands() {

        Objects.requireNonNull(getCommand("toggleinfinitemana")).setExecutor(new WizardCommands(this));
        Objects.requireNonNull(getCommand("togglecooldowns")).setExecutor(new WizardCommands(this));
        Objects.requireNonNull(getCommand("checkmana")).setExecutor(new WizardCommands(this));
    }
    void startManaBarUpdateTask() {
        // mana bar updated every 20 ticks / 1 second

        // note to self: may remove this scheduler, as the proceeding one updates the bar every second
        // however, this makes sure the mana bar will ALWAYS update when a spell is used for consistency
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Mana.updateManaActionBar(onlinePlayer);
            }
        }, 0, 10);
    }

    void startManaRegenTask() {
        // mana bar updated every 20 ticks / 1 second for MANA REGEN
        getServer().getScheduler().runTaskTimer(this, this::regenerateMana, 0, 20);
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Mana.updateManaActionBar(onlinePlayer);
            }
        }, 0, 10);
    }
    @Override
    public void onDisable() {
        getLogger().info("WizardsPlugin has been disabled!");
    }

    public static WizardsPlugin getInstance() {
        return instance;
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

    // regenerate mana over time
    void regenerateMana() {
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
    private final double FIREBALL_COST = 15.0;
    private final double TELEPORT_COST = 10.0;
    private final double LIGHTNING_COST = 20.0;
    private final double GUST_COST = 25.0;
    private final double MINECART_COST = 30.0;
    private final double GP_COST = 20.0;
    private final double FLYING_MANA_COST_PER_TICK = 1.5;
    private final double CHARM_COST = 15.0;
    private final double PORKCHOP_COST = 10.0;
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ItemStack wand = player.getInventory().getItemInMainHand();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleSpellCast(player, playerId, wand);
        }
    }

    private void handleSpellCast(Player player, UUID playerId, ItemStack wand) {
        switch (wand.getType()) {
            case BLAZE_ROD -> handleFireballCast(player, playerId);
            case IRON_SWORD -> handleTeleportCast(player, playerId);
            case IRON_PICKAXE -> handleLightningCast(player, playerId);
            case FEATHER -> handleGustCast(player, playerId);
            case MINECART -> handleMinecartCast(player, playerId);
            case IRON_INGOT -> handleBigManSlamCast(player, playerId);
            case SHIELD -> handleFlyingSpellCast(player, playerId);
            case IRON_SHOVEL -> handlePorkchopCast(player, playerId);
            case BEETROOT -> handleCharmCast(player, playerId);
        }
    }

    void handleFireballCast(Player player, UUID playerId) {
        if (!Cooldown.isOnFireballCooldown(playerId) && Mana.hasEnoughMana(playerId, FIREBALL_COST)) {
            Cast.castFireball(playerId);
            Cooldown.setFireballCooldown(playerId);
            Mana.deductMana(playerId, FIREBALL_COST);
        } else {
            handleCooldownMessage(player, "Fireball", Cooldown.getRemainingFireballCooldownSeconds(playerId));
        }
    }

    void handleTeleportCast(Player player, UUID playerId) {
        if (!Cooldown.isOnTeleportCooldown(playerId) && Mana.hasEnoughMana(playerId, TELEPORT_COST)) {
            Teleport.castTeleportSpell(playerId, 0);
            Cooldown.setTeleportCooldown(playerId);
            Mana.deductMana(playerId, TELEPORT_COST);
        } else {
            handleCooldownMessage(player, "Teleport", Cooldown.getRemainingTeleportCooldownSeconds(playerId));
        }
    }

    void handleLightningCast(Player player, UUID playerId) {
        if (!Cooldown.isOnLightningCooldown(playerId) && Mana.hasEnoughMana(playerId, LIGHTNING_COST)) {
            Cast.castLightningSpell(playerId);
            Cooldown.setLightningCooldown(playerId);
            Mana.deductMana(playerId, LIGHTNING_COST);
            Cast.startLightningEffect(playerId);
        } else {
            handleCooldownMessage(player, "Lightning", Cooldown.getRemainingLightningCooldownSeconds(playerId));
        }
    }

    void handleGustCast(Player player, UUID playerId) {
        if (!Cooldown.isOnGustCooldown(playerId) && Mana.hasEnoughMana(playerId, GUST_COST)) {
            Cast.castGustSpell(playerId);
            Cooldown.setGustCooldown(playerId);
            Mana.deductMana(playerId, GUST_COST);
        } else {
            handleCooldownMessage(player, "Gust", Cooldown.getRemainingGustCooldownSeconds(playerId));
        }
    }

    void handleFlyingSpellCast(Player player, UUID playerId) {
        if (!Cooldown.isOnSquidFlyingCooldown(playerId) && Mana.hasEnoughMana(playerId, FLYING_MANA_COST_PER_TICK)) {
            Squid.startFlyingSpell(player);
            Mana.deductMana(playerId, FLYING_MANA_COST_PER_TICK);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !Squid.isFlying(playerId) || !Mana.hasEnoughMana(playerId, FLYING_MANA_COST_PER_TICK)) {
                        Squid.stopFlyingSpell(player);
                        this.cancel();
                        return;
                    }
                    Mana.deductMana(playerId, FLYING_MANA_COST_PER_TICK);
                }
            }.runTaskTimer(WizardsPlugin.getInstance(), 0, 20);

            player.playSound(player.getLocation(), Sound.ENTITY_SQUID_SQUIRT, 1.0F, 1.0F);
        } else {
            handleCooldownMessage(player, "Flying", Cooldown.getRemainingSquidFlyingCooldownSeconds(playerId));
        }
    }

    void handleMinecartCast(Player player, UUID playerId) {
        if (!Cooldown.isOnMinecartCooldown(playerId) && Mana.hasEnoughMana(playerId, MINECART_COST)) {
            Cast.launchMinecart(player);
            Cooldown.setMinecartCooldown(playerId);
            Mana.deductMana(playerId, MINECART_COST);
        } else {
            handleCooldownMessage(player, "Minecart", Cooldown.getRemainingMinecartCooldownSeconds(playerId));
        }
    }

    void handleBigManSlamCast(Player player, UUID playerId) {
        if (!Cooldown.isOnGPCooldown(playerId) && Mana.hasEnoughMana(playerId, MINECART_COST)) {
            Cast.castGroundPoundSpell(playerId);
            Cooldown.setGPCooldown(playerId);
            Mana.deductMana(playerId, GP_COST);
        } else {
            handleCooldownMessage(player, "Big Man Slam", Cooldown.getRemainingGPCooldownSeconds(playerId));
        }
    }

    void handlePorkchopCast(Player player, UUID playerId) {
        if (!Cooldown.isOnPorkchopCooldown(playerId) && Mana.hasEnoughMana(playerId, PORKCHOP_COST)) {
            Cast.castPorkchopSpell(player);
            Cooldown.setPorkchopCooldown(playerId);
            Mana.deductMana(playerId, PORKCHOP_COST);
        } else {
            handleCooldownMessage(player, "Porkchop", Cooldown.getRemainingPorkchopCooldownSeconds(playerId));
        }
    }

    void handleCharmCast(Player player, UUID playerId) {
        if (!Cooldown.isOnCharmCooldown(playerId) && Mana.hasEnoughMana(playerId, CHARM_COST)) {
            Charm.castCharmSpell(playerId);
            Cooldown.setCharmCooldown(playerId);
            Mana.deductMana(playerId, CHARM_COST);
        } else {
            handleCooldownMessage(player, "Charm", Cooldown.getRemainingCharmCooldownSeconds(playerId));
        }
    }

    void handleCooldownMessage(Player player, String spellName, int remainingSeconds) {
        player.sendMessage(ChatColor.RED + spellName + " on cooldown. Please wait " + remainingSeconds + " seconds before casting again.");
    }

}

//    @EventHandler
//    public void onProjectileHit(ProjectileHitEvent event) {
//        if (event.getEntity() instanceof Snowball) {
//            Location hitLocation = event.getEntity().getLocation();
//
//            // check if snowball hit a block
//            if (hitLocation.getBlock().getType() != Material.AIR) {
//                // create a temporary sphere of ice
//                createIceSphere(hitLocation);
//            }
//        }
//    }

//    private void createIceSphere(Location location) {
//        World world = location.getWorld();
//        double radius = 3.0; //radius of ice sphere
//        for (double x = -radius; x <= radius; x++) {
//            for (double y = -radius; y <= radius; y++) {
//                for (double z = -radius; z <= radius; z++) {
//                    if (Math.sqrt(x * x + y * y + z * z) <= radius) {
//                        Location iceBlockLocation = location.clone().add(x, y, z);
//                        world.getBlockAt(iceBlockLocation).setType(Material.ICE);
//                    }
//                }
//            }
//        }
//    }


// porkchop hit event
//    @EventHandler
//    public void onPorkchopHit(EntityDamageByEntityEvent event) {
//        if (event.getDamager() instanceof Item && event.getEntity() instanceof LivingEntity) {
//            Item porkchopItem = (Item) event.getDamager();
//            LivingEntity targetEntity = (LivingEntity) event.getEntity();
//
//            if (porkchopItem.getItemStack().getType() == Material.PORKCHOP) {
//                PersistentDataContainer container = porkchopItem.getItemStack().getItemMeta().getPersistentDataContainer();
//                if (container.has(new NamespacedKey(this, "caster"), PersistentDataType.STRING)) {
//                    UUID casterId = UUID.fromString(container.get(new NamespacedKey(this, "caster"), PersistentDataType.STRING));
//                    Player caster = getPlayerById(casterId);
//                    if (caster != null) {
//                        double damage = 10.0;
//                        targetEntity.damage(damage, caster);
//
//                        // remove the cooked Porkchop from ground
//                        porkchopItem.remove();
//                    }
//                }
//            }
//        }
//    }