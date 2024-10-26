package WizardsGame;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.util.Vector;


public class WizardsPlugin extends JavaPlugin implements Listener {
    private static WizardsPlugin instance;
    SpellCastingManager Cast = new SpellCastingManager();
    SpellMenu Menu = new SpellMenu(this);
    CooldownManager Cooldown = new CooldownManager();
    TeleportationManager Teleport = new TeleportationManager();
    SquidFlight Squid = new SquidFlight();
    ManaManager Mana = new ManaManager();
    CharmSpell Charm = new CharmSpell();

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("WizardsPlugin has been enabled!");
        registerEvents();
        registerCommands();
        startCooldownBarUpdateTask();
        startManaBarUpdateTask();
        startManaRegenTask();
        getServer().getPluginManager().registerEvents(new SpellCastingManager(), this);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkAndUpdateWand(player);
                }
            }
        }.runTaskTimer(this, 0, 5);
    }
    @Override
    public void onDisable() {
        getLogger().info("WizardsPlugin has been disabled!");
        Mana.clearManaBars();
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // get player and UUID
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        startLocationTracking(player);
        player.sendMessage("Welcome!");
        updateActionBar(player);
        //set players' mana to max on join
        Mana.playerMana.put(playerId, Mana.maxMana);
        Mana.manaBossBars.remove(playerId);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        playerLocations.remove(playerId); // remove the player's location record on quit
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ItemStack wand = player.getInventory().getItemInMainHand();

        // cant break blocks
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleSpellCast(player, playerId, wand);
//            event.setCancelled(true);
        }

        // cant place blocks
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
//            if(wand != SPELL_NAMES){
//            event.setCancelled(true);
//            }
        }
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // check if damage is dealt by a player & cast spell if so
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (!(event.getDamager() instanceof ArmorStand) || !isSpellProjectile(player)) {
//                 handleSpellCast(player, playerId, wand);
            }
        }
    }
    private static final int NUMBER_OF_DROPS = 5;
    private static final double RADIUS = 1.0;
    static final Map<UUID, SpellInfo> lastDamager = new HashMap<>();

    static class SpellInfo {
        private final UUID casterId;
        private final String spellName;

        public SpellInfo(UUID casterId, String spellName) {
            this.casterId = casterId;
            this.spellName = spellName;
        }

        public UUID getCasterId() {
            return casterId;
        }

        public String getSpellName() {
            return spellName;
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }
    private boolean isSpell(ItemStack item) {
        return SpellListener.isSpellItem(item);
    }
    private List<ItemStack> getPlayerSpells(Player player) {
        List<ItemStack> spells = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isSpell(item)) {
                spells.add(item.clone()); // clone the item
            }
        }
        return spells;
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

//        if (!(event.getEntity() instanceof Player)) return; // toggle only players or all entities
        
        // check if the entity was killed by a spell
        SpellInfo damagerInfo = lastDamager.get(event.getEntity().getUniqueId());
        if (damagerInfo != null) {
            UUID damagerId = damagerInfo.getCasterId();
            String spellName = damagerInfo.getSpellName();
            Player player = Bukkit.getPlayer(damagerId);
            if (player != null) {
                // sound effect for the caster
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1.0f, 1.0f);
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(ChatColor.AQUA +"Death> " + ChatColor.YELLOW + event.getEntity().getName() +
                            ChatColor.GRAY + " killed by " + ChatColor.YELLOW + player.getName() +
                            ChatColor.GRAY + " with " + ChatColor.GREEN + ChatColor.ITALIC + spellName + ChatColor.GRAY + "!");
                }
            }
        }else{
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(ChatColor.AQUA +"Death> " + ChatColor.YELLOW + event.getEntity().getName() +
                        ChatColor.GRAY + " fell into " + ChatColor.YELLOW + ChatColor.ITALIC + "The Void"  + ChatColor.GRAY + "!");
            }
        }

        if (event.getEntity() instanceof Player) {
            Player deadPlayer = (Player) event.getEntity();
            List<ItemStack> playerSpells = getPlayerSpells(deadPlayer); // retrieve dead player's spells

            // drop each spell
            for (ItemStack spellItem : playerSpells) {
                if (spellItem != null && spellItem.getType() != Material.AIR) {
                    // customize display name of the item
                    String spellName = WizardsPlugin.getSpellInfo(spellItem);
                    ItemMeta meta = spellItem.getItemMeta();
                    if (meta != null) {
                        spellItem.setItemMeta(meta);
                    }
                    Location deathLocation = deadPlayer.getLocation();
                    Item droppedItem = deathLocation.getWorld().dropItemNaturally(deathLocation, spellItem);

                    // unpickable for a short time
//                    droppedItem.setPickupDelay(Integer.MAX_VALUE);

//                    droppedItem.setGlowing(true);

                    // custom name to be always visible on the ground
                    droppedItem.setCustomName(ChatColor.YELLOW + "" + ChatColor.BOLD + spellName);
                    droppedItem.setCustomNameVisible(true);
                }
            }
            for (ItemStack armorPiece : deadPlayer.getInventory().getArmorContents()) {
                if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                    Location deathLocation = deadPlayer.getLocation();
                    Item droppedItem = deathLocation.getWorld().dropItemNaturally(deathLocation, armorPiece);

                    // name display
                    String armorName = armorPiece.hasItemMeta() && armorPiece.getItemMeta().hasDisplayName()
                            ? armorPiece.getItemMeta().getDisplayName()
                            : armorPiece.getType().toString().replace('_', ' ');
                    droppedItem.setCustomName(ChatColor.YELLOW + "" + ChatColor.BOLD + armorName);
//                    droppedItem.setCustomNameVisible(true);
                }
            }
        }


        // death location of the entity
        Location deathLocation = event.getEntity().getLocation();
        Random random = new Random();

        // drop red dye at random locations around death location
        for (int i = 0; i < NUMBER_OF_DROPS; i++) {
            // random coordinates within the specified radius
            double xOffset = (random.nextDouble() * 2 - 1) * RADIUS;
            double yOffset = (random.nextDouble() * 2 - 1) * RADIUS;
            double zOffset = (random.nextDouble() * 2 - 1) * RADIUS;

            // create new location for the dye
            Location dyeLocation = deathLocation.clone().add(new Vector(xOffset, yOffset, zOffset));

            // create item stack
            ItemStack dyeItem = new ItemStack(Material.RED_DYE, 1); // 1 red dye

            // spawn item
            Item item = dyeLocation.getWorld().dropItem(dyeLocation, dyeItem);
            item.setPickupDelay(Integer.MAX_VALUE); // prevents picking up

            // velocity to item to make it move outward
            item.setVelocity(new Vector(random.nextDouble() * 0.2 - 0.1, 0.4, random.nextDouble() * 0.2 - 0.1));

            // remove the item after 2 seconds (40 ticks)
            Bukkit.getScheduler().runTaskLater(WizardsPlugin.getInstance(), item::remove, 40);
        }

        // clean up last damager record
        lastDamager.remove(event.getEntity().getUniqueId());
    }

    private boolean isSpellProjectile(Player player) {
        return Cast.activeSwords.containsValue(player.getUniqueId()) || Cast.activeBolts.containsKey(player.getUniqueId());
        // activeBolts.put(player.getUniqueId(), manaBolt);
    }


    @EventHandler
    public void onPlayerItemChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        updateActionBar(player);
    }

    void registerEvents() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new TeleportationManager(), this);
        getServer().getPluginManager().registerEvents(new SpellListener(this, Menu), this);
//        getServer().getPluginManager().registerEvents(new SquidFlight(), this);
    }

    // commands
    void registerCommands() {

        Objects.requireNonNull(getCommand("toggleinfinitemana")).setExecutor(new WizardCommands(this));
        Objects.requireNonNull(getCommand("togglecooldowns")).setExecutor(new WizardCommands(this));
        Objects.requireNonNull(getCommand("checkmana")).setExecutor(new WizardCommands(this));
        Objects.requireNonNull(getCommand("unlockspells")).setExecutor(new WizardCommands(this));
        Objects.requireNonNull(getCommand("add")).setExecutor(new WizardCommands(this));
        Objects.requireNonNull(getCommand("togglefriendlyfire")).setExecutor(new WizardCommands(this));
    }

    static final Map<Material, String> SPELL_NAMES = new HashMap<>();
    static {
//        ChatColor.BOLD +
        SPELL_NAMES.put(Material.STICK, "Generic Wand");
        SPELL_NAMES.put(Material.BLAZE_ROD, "Fiery Wand");
        SPELL_NAMES.put(Material.IRON_SWORD, "Shrouded Step");
        SPELL_NAMES.put(Material.IRON_PICKAXE, "Mjölnir");
        SPELL_NAMES.put(Material.FEATHER, "Gust Feather");
        SPELL_NAMES.put(Material.MINECART, "The Great Escape");
        SPELL_NAMES.put(Material.IRON_INGOT, "Big Man Slam");
        SPELL_NAMES.put(Material.SHIELD, "Winged Shield");
        SPELL_NAMES.put(Material.RECOVERY_COMPASS, "Voidwalker");
        SPELL_NAMES.put(Material.HONEYCOMB, "Starfall Barrage");
        SPELL_NAMES.put(Material.TIPPED_ARROW, "Heal Cloud");
        SPELL_NAMES.put(Material.MUSIC_DISC_5, "Recall");
        SPELL_NAMES.put(Material.HEART_OF_THE_SEA, "Void Orb");
        SPELL_NAMES.put(Material.AMETHYST_SHARD, "Dragon Spit");
        SPELL_NAMES.put(Material.NAUTILUS_SHELL, "Cod Gun");

    }
    static String getSpellInfo(ItemStack itemInHand) {
        if (itemInHand != null) {
            // check if item type has a corresponding spell name
            String spellName = SPELL_NAMES.get(itemInHand.getType());
            if (spellName != null) {
                return spellName;
            }
        }
        return null;
    }
    private void updateActionBar(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        String spellName = getSpellInfo(itemInHand); // get spell name based on the item type
        long cooldownDuration = getCooldownDuration(itemInHand); // get the cooldown duration for that spell
        long remainingCooldown = getRemainingCooldown(player.getUniqueId(), itemInHand); // get the remaining cooldown
        WizardsPlugin.SpellType spellType = SpellMenu.getSpellByMaterial(itemInHand.getType());
        int spellLevel = WizardsPlugin.getSpellLevel(player.getUniqueId(), spellType);

        StringBuilder actionBarMessage = new StringBuilder();

        if (spellName != null) {
            // color based on cooldown state
            ChatColor spellColor = remainingCooldown > 0 ? ChatColor.RED : ChatColor.GREEN;
            actionBarMessage.append(spellColor).append(ChatColor.BOLD + spellName).append(ChatColor.RESET).append(" ");
            actionBarMessage.append(ChatColor.BLUE).append(ChatColor.BOLD + "| Lv ").append(spellLevel).append(" ");


            if (remainingCooldown > 0) {
                int totalBlocks = 24; // total blocks in the bar
                int filledBlocks = (int) Math.floor((cooldownDuration - remainingCooldown) / (double) cooldownDuration * totalBlocks);
                int emptyBlocks = totalBlocks - filledBlocks;
                int remainingSeconds = (int) Math.ceil(remainingCooldown / 1000.0);

                actionBarMessage.append(ChatColor.GREEN).append("█".repeat(Math.max(0, filledBlocks))); // solid section
                actionBarMessage.append(ChatColor.RED).append("█".repeat(Math.max(0, emptyBlocks))); // empty seciton
                actionBarMessage.append(" ").append(ChatColor.WHITE).append("").append(remainingSeconds).append("s");
            }
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage.toString()));
    }


    private long getRemainingCooldown(UUID playerId, ItemStack itemInHand) {
        if (itemInHand.getType() == Material.BLAZE_ROD) {
            return Cooldown.fireballCooldownDuration - (System.currentTimeMillis() - Cooldown.fireballCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.IRON_PICKAXE) {
            return Cooldown.lightningCooldownDuration - (System.currentTimeMillis() - Cooldown.lightningCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.IRON_SWORD) {
            return Cooldown.teleportCooldownDuration - (System.currentTimeMillis() - Cooldown.teleportCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.FEATHER) {
            return Cooldown.gustCooldownDuration - (System.currentTimeMillis() - Cooldown.gustCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.MINECART) {
            return Cooldown.minecartCooldownDuration - (System.currentTimeMillis() - Cooldown.minecartCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.IRON_INGOT) {
            return Cooldown.GPCooldownDuration - (System.currentTimeMillis() - Cooldown.GPCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.HONEYCOMB) {
            return Cooldown.MeteorCooldownDuration - (System.currentTimeMillis() - Cooldown.MeteorCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.TIPPED_ARROW) {
            return Cooldown.HealCloudCooldownDuration - (System.currentTimeMillis() - Cooldown.HealCloudCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.RECOVERY_COMPASS) {
            return Cooldown.MapTeleportCooldownDuration - (System.currentTimeMillis() - Cooldown.MapTeleportCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.MUSIC_DISC_5) {
            return Cooldown.RecallCooldownDuration - (System.currentTimeMillis() - Cooldown.RecallCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.HEART_OF_THE_SEA) {
            return Cooldown.VoidOrbCooldownDuration - (System.currentTimeMillis() - Cooldown.VoidOrbCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.AMETHYST_SHARD) {
            return Cooldown.manaBoltCooldownDuration - (System.currentTimeMillis() - Cooldown.manaBoltCooldowns.getOrDefault(playerId, 0L));
        }
        else if (itemInHand.getType() == Material.NAUTILUS_SHELL) {
            return Cooldown.CodCooldownDuration - (System.currentTimeMillis() - Cooldown.codCooldowns.getOrDefault(playerId, 0L));
        }
        return 0;
    }

    private long getCooldownDuration(ItemStack itemInHand) {
        // Return the cooldown duration based on the item type
        if (itemInHand.getType() == Material.BLAZE_ROD) {
            return Cooldown.fireballCooldownDuration;
        }
        if (itemInHand.getType() == Material.IRON_SWORD) {
            return Cooldown.teleportCooldownDuration;
        }
        if (itemInHand.getType() == Material.IRON_PICKAXE) {
            return Cooldown.lightningCooldownDuration;
        }
        if (itemInHand.getType() == Material.MINECART) {
            return Cooldown.minecartCooldownDuration;
        }
        if (itemInHand.getType() == Material.FEATHER) {
            return Cooldown.gustCooldownDuration;
        }

        if (itemInHand.getType() == Material.IRON_INGOT) {
            return Cooldown.GPCooldownDuration;
        }
        if (itemInHand.getType() == Material.RECOVERY_COMPASS) {
            return Cooldown.MapTeleportCooldownDuration;
        }
        if (itemInHand.getType() == Material.HONEYCOMB) {
            return Cooldown.MeteorCooldownDuration;
        }
        if (itemInHand.getType() == Material.TIPPED_ARROW) {
            return Cooldown.HealCloudCooldownDuration;
        }
        if (itemInHand.getType() == Material.MUSIC_DISC_5) {
            return Cooldown.RecallCooldownDuration;
        }
        if (itemInHand.getType() == Material.HEART_OF_THE_SEA) {
            return Cooldown.VoidOrbCooldownDuration;
        }
        if (itemInHand.getType() == Material.AMETHYST_SHARD) {
            return Cooldown.manaBoltCooldownDuration;
        }
        if (itemInHand.getType() == Material.NAUTILUS_SHELL) {
            return Cooldown.CodCooldownDuration;
        }

        return 0;
    }


    void startCooldownBarUpdateTask() {
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                updateActionBar(onlinePlayer);
            }
        }, 0, 5);
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
        getServer().getScheduler().runTaskTimer(this, this::regenerateMana, 0, 10);
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Mana.updateManaActionBar(onlinePlayer);
            }
        }, 0, 10);
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
                double manaRegenRate = 2.5;
                double newMana = Math.min(currentMana + manaRegenRate, Mana.maxMana);
                Mana.playerMana.put(playerId, newMana);
            }
        }
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


// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ----------------------------------------------- SPELL  HANDLING -----------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

    static final double FIREBALL_COST = 15.0;
    static final double TELEPORT_COST = 10.0;
    static final double LIGHTNING_COST = 20.0;
    static final double GUST_COST = 25.0;
    static final double MINECART_COST = 30.0;
    static final double GP_COST = 20.0;
    static final double FLYING_MANA_COST_PER_TICK = 1.5;
    static final double VOIDWALKER_COST = 80.0;
//    private final int CLONE_COST = 20;
    static final double METEOR_COST = 50.0;
    static final double HEALCLOUD_COST = 15.0;
    static final double Recall_Cost = 25.0;
    static final double VoidOrb_Cost = 10.0;
    static final double MANABOLT_COST = 10.0;
    static final double COD_COST = 15.0;
    static final double CHARM_COST = 15.0;

    // recall spell ----------------------------------------------------------------------------------------------------
    private final int maxRecordedLocations = 5; // number of locations to record ( ho many seconds to teleport back )
    private final Map<UUID, Queue<Location>> playerLocations = new HashMap<>();
    private final int recordInterval = 20; // time in ticks (1 second)
    // -----------------------------------------------------------------------------------------------------------------
    private final Set<UUID> playersWithWands = new HashSet<>();



    public enum SpellType {
        Fiery_Wand(Material.BLAZE_ROD),
        Shrouded_Step(Material.IRON_SWORD),
        Mjölnir(Material.IRON_PICKAXE),
        The_Great_Escape(Material.MINECART),
        Gust(Material.FEATHER),
        Winged_Shield(Material.SHIELD),
        Big_Man_Slam(Material.IRON_INGOT),
        VoidWalker(Material.RECOVERY_COMPASS),
        Starfall_Barrage(Material.HONEYCOMB),
        Heal_Cloud(Material.TIPPED_ARROW),
        Recall(Material.MUSIC_DISC_5),
        Void_Orb(Material.HEART_OF_THE_SEA),
        Dragon_Spit(Material.AMETHYST_SHARD),
        Cod_Gun(Material.NAUTILUS_SHELL);

        private final Material material;

        SpellType(Material material) {
            this.material = material;
        }

        public Material getMaterial() {
            return material;
        }
    }
    private static final Map<UUID, Map<SpellType, Integer>> playerSpells = new HashMap<>(); // each player’s owned spells
    private static final int maxSpellLevel = 5; // max level
    public static void increaseSpellLevel(UUID playerId, SpellType spellType) {
        Map<SpellType, Integer> spells = playerSpells.computeIfAbsent(playerId, k -> new HashMap<>());
        int currentLevel = spells.getOrDefault(spellType, 0);

        // increase level if not at max level
        if (currentLevel < maxSpellLevel) {
            spells.put(spellType, currentLevel + 1);
        }
    }

    private void checkAndUpdateWand(Player player) {
        ItemStack wand = player.getInventory().getItemInMainHand();
        if (WandManager.isWand(wand)) {
            wand = WandManager.createWand(wand.getType(), player); // create item with the correct properties
            player.getInventory().setItemInMainHand(wand); // update item
            playersWithWands.add(player.getUniqueId()); // track player
        } else {
            playersWithWands.remove(player.getUniqueId()); // remove from tracking
        }
    }
    public boolean canSelectSpell(UUID playerId, SpellType spellType) {
        return getSpellLevel(playerId, spellType) >= 1;
    }

    public static int getSpellLevel(UUID playerId, SpellType spellType) {
        return playerSpells.getOrDefault(playerId, new HashMap<>()).getOrDefault(spellType, 0);
    }

    public void addSpellToPlayer(UUID playerId, SpellType spellType) {
        increaseSpellLevel(playerId, spellType);
    }
    void unlockAllSpells(UUID playerId) {
        for (SpellType spellType : SpellType.values()) {
            // increase the spell level to at least 1 to unlock it
            increaseSpellLevel(playerId, spellType);
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
            case RECOVERY_COMPASS -> handleMapTeleportCast(player, playerId);
//            case SHULKER_SHELL -> handleCloneCast(player, playerId);
            case HONEYCOMB -> handleMeteorCast(player, playerId);
            case TIPPED_ARROW -> handleHealCloudCast(player, playerId);
            case MUSIC_DISC_5 -> handleRecallCast(player, playerId);
            case HEART_OF_THE_SEA -> handleVoidOrbCast(player, playerId);
            case AMETHYST_SHARD -> handleManaBoltCast(player, playerId);
            case NAUTILUS_SHELL -> handleCodGunCast(player, playerId);

            case BEETROOT -> handleCharmCast(player, playerId);


        }
    }
    void handleFireballCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnFireballCooldown(playerId) && Mana.hasEnoughMana(playerId, FIREBALL_COST)) {
            Cast.castFireball(player);
            Cooldown.setFireballCooldown(playerId);
            Mana.deductMana(playerId, FIREBALL_COST);
        } else if (Cooldown.isOnFireballCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingFireballCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }

    void handleTeleportCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnTeleportCooldown(playerId) && Mana.hasEnoughMana(playerId, TELEPORT_COST)) {
            Teleport.castTeleportSpell(playerId, 0);
            Cooldown.setTeleportCooldown(playerId);
            Mana.deductMana(playerId, TELEPORT_COST);
        } else if (Cooldown.isOnTeleportCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingRecallCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }

    void handleLightningCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnLightningCooldown(playerId) && Mana.hasEnoughMana(playerId, LIGHTNING_COST)) {
            Cast.castLightningSpell(player);
            Cooldown.setLightningCooldown(playerId);
            Mana.deductMana(playerId, LIGHTNING_COST);
            Cast.startLightningEffect(playerId);
        } else if (Cooldown.isOnLightningCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingLightningCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }

    void handleGustCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnGustCooldown(playerId) && Mana.hasEnoughMana(playerId, GUST_COST)) {
            Cast.castGustSpell(playerId);
            Cooldown.setGustCooldown(playerId);
            Mana.deductMana(playerId, GUST_COST);
        } else if (Cooldown.isOnGustCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingGustCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }

    void handleFlyingSpellCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
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
        } else if (Cooldown.isOnSquidFlyingCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingSquidFlyingCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }

    void handleMinecartCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnMinecartCooldown(playerId) && Mana.hasEnoughMana(playerId, MINECART_COST)) {
            Cast.launchMinecart(player);
            Cooldown.setMinecartCooldown(playerId);
            Mana.deductMana(playerId, MINECART_COST);
        } else if (Cooldown.isOnMinecartCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingMinecartCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }

    void handleBigManSlamCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnGPCooldown(playerId) && Mana.hasEnoughMana(playerId, GP_COST)) {
            Cast.castGroundPoundSpell(playerId);
            Cooldown.setGPCooldown(playerId);
            Mana.deductMana(playerId, GP_COST);
        } else if (Cooldown.isOnGPCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingGPCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }
    void handleMapTeleportCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnMapTeleportCooldown(playerId) && Mana.hasEnoughMana(playerId, TELEPORT_COST)) {
            Cast.teleportPlayerUp(player);
            Cooldown.setMapTeleportCooldown(playerId);
            Mana.deductMana(playerId, VOIDWALKER_COST);
        } else if (Cooldown.isOnMapTeleportCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingMapTeleportCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }

    }

    public void handleMeteorCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnMeteorCooldown(playerId) && Mana.hasEnoughMana(playerId, METEOR_COST)) {
            Location targetLocation = Cast.getTargetLocation(player);
            if (targetLocation != null) {
                Cast.castMeteorShower(player, targetLocation);
                Cooldown.setMeteorCooldown(playerId);
                Mana.deductMana(playerId, METEOR_COST);
            }
        } else if (Cooldown.isOnMeteorCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingMeteorCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }

    void handleHealCloudCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnHealCloudCooldown(playerId) && Mana.hasEnoughMana(playerId, HEALCLOUD_COST)) {
            Location targetLocation = Cast.getTargetLocation(player);
            if (targetLocation != null) {
                targetLocation.setY(targetLocation.getY() + 1);
                Cast.spawnHealingCircle(player, targetLocation); // spawn the healing circle
                Cooldown.setHealCloudCooldown(playerId); // set cooldown after casting
                Mana.deductMana(playerId, HEALCLOUD_COST);
            } else if (Cooldown.isOnHealCloudCooldown(playerId)) {
                handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingHealCloudCooldownSeconds(playerId));
            }else{
                handleManaMessage(player);
            }
        }
    }

    private void handleRecallCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        // check if player can teleport
        if (!Cooldown.isOnRecallCooldown(playerId) && Mana.hasEnoughMana(playerId, Recall_Cost)) {
            Queue<Location> locations = playerLocations.get(playerId);
            if (locations != null && locations.size() >= maxRecordedLocations) {
                // get location from 5 seconds ago ( first recorded location in the queue )
                Location teleportLocation = locations.poll(); // get and remove oldest location
                if (teleportLocation != null) {
                    showTeleportEffect(teleportLocation, player.getLocation());
                    player.teleport(teleportLocation);
                    Mana.deductMana(playerId, Recall_Cost); // Deduct mana
                    applyDarknessEffect(player);
                    player.sendMessage("You have been teleported to your previous location.");
                }
            } else {
                player.sendMessage("No location record found. Please wait a moment.");
            }
        } else if (Cooldown.isOnRecallCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingRecallCooldownSeconds(playerId));
        } else {
            handleManaMessage(player);
        }
    }
    // show teleport effect at the original and new locations
    private void showTeleportEffect(Location from, Location to) {
        // show effect at original location
        createSphereEffect(from, Particle.VILLAGER_HAPPY, 1, 10); // sphere effect going inwards
        createSphereEffect(to, Particle.SONIC_BOOM, 0.01, 10); // sphere effect going outwards

        // play sound effect at original location
        from.getWorld().playSound(from, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    private void applyDarknessEffect(Player player) {
        PotionEffect blindnessEffect = new PotionEffect(PotionEffectType.BLINDNESS, 40, 0); // 20 ticks = 1 second
        player.addPotionEffect(blindnessEffect); // apply effect to the player
    }

    // create a sphere effect with particles
    private void createSphereEffect(Location center, Particle particle, double radius, int particlesCount) {
        double increment = Math.PI / particlesCount;
        for (double theta = 0; theta < Math.PI; theta += increment) {
            for (double phi = 0; phi < 2 * Math.PI; phi += increment) {
                double x = radius * Math.sin(theta) * Math.cos(phi);
                double y = radius * Math.cos(theta);
                double z = radius * Math.sin(theta) * Math.sin(phi);

                // calculate location to spawn the particle
                Location particleLocation = center.clone().add(x, y + 1, z);
                center.getWorld().spawnParticle(particle, particleLocation, 1);
            }
        }
    }

    public void startLocationTracking(Player player) {
        UUID playerId = player.getUniqueId();
        playerLocations.put(playerId, new LinkedList<>()); // initialize location queue for the player

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel(); // stop tracking if the player is no longer online
                    return;
                }

                // record player's current location
                Queue<Location> locations = playerLocations.get(playerId);
                if (locations.size() >= maxRecordedLocations) {
                    locations.poll(); // remove oldest location if maximum size is hit
                }
                locations.offer(player.getLocation()); // add current location to the queue

                //debug
                Bukkit.getLogger().info("Recording location for " + player.getName() + ": " + player.getLocation());
            }
        }.runTaskTimer(WizardsPlugin.getInstance(), 0, recordInterval); // run every second
    }

    void handleVoidOrbCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnVoidOrbCooldown(playerId) && Mana.hasEnoughMana(playerId, VoidOrb_Cost)) {
            Cast.VoidOrbCast(player, playerId);
            Cooldown.setVoidOrbCooldown(playerId);
            Mana.deductMana(playerId, VoidOrb_Cost); // deduct mana cost
        } else if (Cooldown.isOnVoidOrbCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingVoidOrbCooldownSeconds(playerId));
    }
        }

    private void handleManaBoltCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnManaBoltCooldown(playerId) && Mana.hasEnoughMana(playerId, MANABOLT_COST)){
            Mana.deductMana(playerId, MANABOLT_COST);
            Cooldown.setManaBoltCooldown(playerId);
            Cast.launchManaBolt(player);
        }
        else if (Cooldown.isOnManaBoltCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingManaBoltCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }

    private void handleCodGunCast(Player player, UUID playerId) {
        if (Cast.playerTeleportationState.getOrDefault(playerId, false)) {
            sendTeleportWarning(player);
            return;
        }
        if (!Cooldown.isOnCodCooldown(playerId) && Mana.hasEnoughMana(playerId, COD_COST)){
            Mana.deductMana(playerId, COD_COST);
            Cooldown.setCodCooldown(playerId);
            Cast.shootFish(player);
        }
        else if (Cooldown.isOnCodCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingCodCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }


//    void handleCloneCast(Player player, UUID playerId) {
//        if (!Cooldown.isOnCloneCooldown(playerId) && Mana.hasEnoughMana(playerId, CLONE_COST)) {
//            Cast.createClone(player);
//            Cooldown.setCloneCooldown(playerId);
//            Mana.deductMana(playerId, CLONE_COST);
//        } else {
//            handleCooldownMessage(player, "Illusion", Cooldown.getRemainingCloneCooldown(playerId));
//        }
//    }

    void handleCharmCast(Player player, UUID playerId) {
        if (!Cooldown.isOnCharmCooldown(playerId) && Mana.hasEnoughMana(playerId, CHARM_COST)) {
            Charm.castCharmSpell(playerId);
            Cooldown.setCharmCooldown(playerId);
            Mana.deductMana(playerId, CHARM_COST);
        } else if (Cooldown.isOnCharmCooldown(playerId)) {
            handleCooldownMessage(player, getSpellInfo(player.getItemInHand()), Cooldown.getRemainingCharmCooldownSeconds(playerId));
        }else{
            handleManaMessage(player);
        }
    }

    private HashMap<Player, Long> lastTeleportMessage = new HashMap<>();
    private HashMap<Player, Long> lastCooldownMessage = new HashMap<>();
    private HashMap<Player, Long> lastManaMessage = new HashMap<>();
    static final HashMap<Player, Long> lastDropMessage = new HashMap<>();
    static final long MESSAGE_COOLDOWN = 1000;

    private void sendTeleportWarning(Player player) {
        long currentTime = System.currentTimeMillis();
        if (!lastTeleportMessage.containsKey(player) ||
                (currentTime - lastTeleportMessage.get(player)) > MESSAGE_COOLDOWN) {

            player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + ("You cannot cast spells while in a different dimension!"));
            lastTeleportMessage.put(player, currentTime);
        }
    }
    void handleCooldownMessage(Player player, String spellName, int remainingSeconds) {
        long currentTime = System.currentTimeMillis();
        if (!lastCooldownMessage.containsKey(player) || (currentTime - lastCooldownMessage.get(player)) > MESSAGE_COOLDOWN) {

            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + spellName + ChatColor.RED + " on cooldown. Please wait " + ChatColor.BOLD + remainingSeconds + ChatColor.RED + " seconds before casting again.");
            lastCooldownMessage.put(player, currentTime);
        }
    }

    void handleManaMessage(Player player) {
        long currentTime = System.currentTimeMillis();
        if (!lastManaMessage.containsKey(player) || (currentTime - lastManaMessage.get(player)) > MESSAGE_COOLDOWN) {

            player.sendMessage(ChatColor.RED + " You do not have enough mana. Please wait before casting again.");
            lastManaMessage.put(player, currentTime);
        }
    }

}