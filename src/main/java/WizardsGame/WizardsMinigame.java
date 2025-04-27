package WizardsGame;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

import static WizardsGame.SpellListener.isSpellItem;
import static WizardsGame.TeamManager.teams;

public class WizardsMinigame implements Listener{
    private final WizardsPlugin plugin;
    SpellMenu Menu = new SpellMenu(WizardsPlugin.getInstance());
    TeamManager Team = new TeamManager();

    public WizardsMinigame(WizardsPlugin plugin) {
        this.plugin = plugin;
    }



    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------MINIGAME--------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    final Set<Location> spawnPoints = new HashSet<>(); // store spawn points
    static final Set<UUID> playersInMinigame = new HashSet<>(); // store players in the minigame
    static boolean isMinigameActive = false; // track if the minigame is active
    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        if (isMinigameActive) { // check if the minigame is active
            event.setCancelled(true); // cancel the block drop event
        }
    }
    static void sendTitle(Player player, String title, String subtitle) {
        // show title
        player.sendTitle(ChatColor.YELLOW + title, ChatColor.GREEN + subtitle, 10, 70, 20);
    }
    public void startMinigame() {
        Team.isSoloGame = true;
        for (Set<UUID> members : teams.values()) {
            if (!members.isEmpty()) {
                Team.isSoloGame = false; // if any team has members, it's not a solo game
                break; // no need to check further
            }
        }
        if (!Team.isSoloGame) {
            // convert solo players to teams of one
            for (UUID playerId : playersInMinigame) {
                if (!TeamManager.isPlayerOnTeam(playerId)) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        String teamName = player.getName() + "'s Team";
                        plugin.Team.createTeam(teamName, playerId);
                        plugin.Team.joinTeam(player, teamName);
                    }
                }
            }
            Team.isSoloGame = false;
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Starting a team game! Solo players converted to teams.");
        }else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Starting a solo game!");
        }
        isMinigameActive = true;
        countdownStart();
    }

    private void countdownStart() {
        new BukkitRunnable() {
            int countdown = 3; // countdown length

            @Override
            public void run() {
                if (countdown > 0) {
                    for (UUID playerId : playersInMinigame) {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player != null) {
                            sendTitle(player, countdown + "", "The game is starting soon!");
                        }
                    }
                    countdown--;
                } else {
                    // after countdown ends
                    for (UUID playerId : playersInMinigame) {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player != null) {
                            sendTitle(player, "Game Started!", "Good luck!");
                            WizardsPlugin.playerAliveStatus.put(playerId, true);
                            // set the player's scoreboard
                            player.setScoreboard(Team.sidebarScoreboard);
                            Team.updateSidebar();
                        }
                        teleportToRandomSpawn();
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
        for (UUID playerId : playersInMinigame) {
            Player player = Bukkit.getPlayer(playerId);
            assert player != null;
            player.getInventory().clear(); // main inventory
            player.getInventory().setArmorContents(null); // armor slots
            player.getInventory().setExtraContents(null); // offhand, etc.
        }

    }
    static void clearSidebar() {
        // reset all entries on the sidebar scoreboard
        for (String entry : TeamManager.sidebarScoreboard.getEntries()) {
            TeamManager.sidebarScoreboard.resetScores(entry);
        }

        // clear the sidebar title
        TeamManager.sidebarObjective.setDisplayName("");
    }
    private void teleportToRandomSpawn() {
        Map<String, List<UUID>> teamsMap = new HashMap<>();

        // group players by team or individually for solo games
        for (UUID playerId : playersInMinigame) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
//                String playerTeam = getPlayerTeam(playerId); // get player's team name
                String teamName = TeamManager.getPlayerTeam(playerId);
                if (teamName == null) {
                    // no team and it's a team game, this shouldn't happen due to above
                    teamName = player.getName(); // fallback to player name
                }
                teamsMap.computeIfAbsent(teamName, k -> new ArrayList<>()).add(playerId);
                // treat each player as their own team in solo games
                if (Team.isSoloGame) {
                    teamsMap.put(playerId.toString(), Collections.singletonList(playerId));
                } else {
//                    teamsMap.computeIfAbsent(playerTeam, k -> new ArrayList<>()).add(playerId);
                }
            }
        }

        // shuffle spawn points
        List<Location> spawnPointList = new ArrayList<>(spawnPoints);
        Collections.shuffle(spawnPointList);

        // assign spawn points to teams
        int index = 0;
        for (List<UUID> teamPlayers : teamsMap.values()) {
            if (index < spawnPointList.size()) {
                Location spawnLocation = spawnPointList.get(index);
                // teleport all players in the team to the same spawn point
                for (UUID playerId : teamPlayers) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        player.teleport(spawnLocation);
                        player.sendMessage(ChatColor.GREEN + "You've been teleported to a spawn point!");
                    }
                }
                index++; // move to the next spawn point for the next team
            } else {
                // handle the case where there are more teams than spawn points
                Bukkit.broadcastMessage(ChatColor.RED + "Not enough spawn points for all teams!");
                break; // stop assigning spawn points if none are left
            }
        }
    }
    private String getPlayerTeam(UUID playerId) {
        for (Map.Entry<String, Set<UUID>> entry : teams.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                return entry.getKey(); // return team name if the player is part of the team
            }
        }
        return ""; // return an empty string if the player is not part of any team
    }

    public void hasGameEnded() {
        int aliveTeams = 0;
        boolean soloGame = (teams.isEmpty());
        if (!soloGame) {
            for (String teamName : teams.keySet()) {
                Set<UUID> members = teams.get(teamName);
                boolean hasAlivePlayer = false;

                // check if any player in the team is alive
                for (UUID memberId : members) {
                    if (WizardsPlugin.playerAliveStatus.getOrDefault(memberId, true)) {
                        hasAlivePlayer = true;
                        break; // at least one player is alive
                    }
                }

                // increment count if this team has alive players
                if (hasAlivePlayer) {
                    aliveTeams++;
                }
            }
//                // more than one team still has alive players, game continues
//                if (aliveTeams > 1) {
//                    return false;
//                }
            if (aliveTeams == 1) {
                String winningTeamName = "";
                for (String teamName : teams.keySet()) {
                    Set<UUID> members = teams.get(teamName);
                    for (UUID memberId : members) {
                        if (WizardsPlugin.playerAliveStatus.getOrDefault(memberId, true)) {
                            winningTeamName = teamName; // set winning team name
                            break;
                        }
                    }
                    if (!winningTeamName.isEmpty()) {
                        break; // found winning team
                    }
                }
                announceWinningTeam(winningTeamName); // announce winning team
                endGame();
            }
        } else { // solo game
            int alivePlayers = 0; // Count of alive players
            for (UUID playerId : WizardsPlugin.playerAliveStatus.keySet()) {
                if (WizardsPlugin.playerAliveStatus.getOrDefault(playerId, true)) {
                    alivePlayers++; // count alive players
                }
            }
            if (alivePlayers == 1) {
                UUID winningPlayerId = null;
                for (UUID playerId : WizardsPlugin.playerAliveStatus.keySet()) {
                    if (WizardsPlugin.playerAliveStatus.getOrDefault(playerId, true)) {
                        winningPlayerId = playerId; // set winning player ID
                        break;
                    }
                }
                announceWinningPlayer(Bukkit.getPlayer(winningPlayerId)); // announce winning player
                endGame(); // end the game
            }
            if (alivePlayers == 0) {
                Bukkit.broadcastMessage(ChatColor.GOLD + "It's a tie!");
                for (UUID playerId : playersInMinigame) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        sendTitle(player, "Tie!", ChatColor.GOLD + "" + ChatColor.BOLD +"Congratulations! No one is the chosen ones!");
                    }
                }
                endGame(); // end the game
            }
        }
    }

    private void announceWinningTeam(String winningTeamName) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "Team " + winningTeamName + " has won the game!");
        for (UUID playerId : playersInMinigame) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                sendTitle(player, winningTeamName, ChatColor.GOLD + "" + ChatColor.BOLD +"Congratulations! Your team are the chosen ones!");
            }
        }
    }
    private void announceWinningPlayer(Player winningPlayer) {
        String playerName = winningPlayer.getName();
        Bukkit.broadcastMessage(ChatColor.GOLD + playerName + " has won the game!");
        for (UUID playerId : playersInMinigame) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                sendTitle(player, playerName, ChatColor.GOLD + "" + ChatColor.BOLD +"Congratulations! You are the chosen one!");
            }
        }
        // can add extra rewards etc.
    }
    public void clearAllTombstones() {
        // remove all armor stand tombstones
        for (ArmorStand tombstone : playerTombstones.values()) {
            if (tombstone != null && !tombstone.isDead()) {
                tombstone.remove();
            }
        }
        playerTombstones.clear();
        deathInventories.clear();
    }


    public void endGame() {
        ItemStack book = new ItemStack(Material.BOOK);
        Bukkit.broadcastMessage(ChatColor.GREEN + "Game over!");
        isMinigameActive = false;
        Team.clearTeams();
        teams.clear();
        playerTombstones.clear();
        clearAllTombstones();
        for (UUID playerId : playersInMinigame) {
            Player player = Bukkit.getPlayer(playerId);
            Team.resetPlayerScoreboard(player);
            WizardsPlugin.resetSpellLevel(playerId);
            player.getInventory().setItem(8, book);
        }
        clearSidebar();
        resetGame(); // clear all players from minigame
    }

    //manually stopping game
    void stopMinigame() {
        isMinigameActive = false;
        playerTombstones.clear();
        clearAllTombstones();
        for (UUID playerId : playersInMinigame) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.RED + "The minigame has been stopped.");
            }
        }
        playersInMinigame.clear(); // clear all players from minigame
        endGame();
    }

    private void resetGame() {
        Team.clearTeams(); // clear all teams and reset scoreboard

        // reset player alive status
        for (UUID playerId : WizardsPlugin.playerAliveStatus.keySet()) {
            WizardsPlugin.playerAliveStatus.put(playerId, true); // mark all players as alive
        }
    }
    private final Map<UUID, Inventory> deathInventories = new HashMap<>();
    private Map<UUID, ArmorStand> playerTombstones = new HashMap<>();
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();

        if (!playersInMinigame.contains(playerId)) return;

        // store ALL items before they're cleared
        Inventory deathInventory = Bukkit.createInventory(null, 54,
                ChatColor.RED + player.getName() + "'s Tombstone");

        // store inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                deathInventory.addItem(item.clone());
            }
        }

        // store armor
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) {
                deathInventory.addItem(armor.clone());
            }
        }

        // store offhand item
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && offhand.getType() != Material.AIR) {
            deathInventory.addItem(offhand.clone());
        }

        // save the inventory before clearing
        deathInventories.put(playerId, deathInventory);

        // clear drops
        event.getDrops().clear();
        event.setKeepInventory(false); // ensure keep inv is overriden

        // create tombstone
        createTombstone(player);

        // set player to spectator
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.RED + "You died! Right-click your tombstone to retrieve items.");
        }, 1L);

        // check game end conditions
        hasGameEnded();
    }

    private void createTombstone(Player player) {
        Location deathLoc = player.getLocation().clone();
        deathLoc.setYaw(0); // tombstone facing direction

        // remove any existing tombstone for this player
        // note to self: may remove in the case of a resurrection spell
        if (playerTombstones.containsKey(player.getUniqueId())) {
            playerTombstones.get(player.getUniqueId()).remove();
        }

        ArmorStand tombstone = deathLoc.getWorld().spawn(deathLoc, ArmorStand.class);
        tombstone.setVisible(false);
        tombstone.setGravity(false);
        tombstone.setCustomNameVisible(true);
        tombstone.setCustomName(ChatColor.RED + player.getName() + "'s Tombstone");

        // set player head
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        skull.setItemMeta(meta);

        tombstone.getEquipment().setHelmet(skull);
        tombstone.setHeadPose(new EulerAngle(Math.toRadians(20), 0, 0));

        // tombstone properties
        tombstone.setInvulnerable(true);
        tombstone.setCollidable(false);
        tombstone.setPersistent(true);

        playerTombstones.put(player.getUniqueId(), tombstone);
        // store the inventory with spell items
        Inventory deathInventory = Bukkit.createInventory(null, 54,
                ChatColor.RED + player.getName() + "'s Tombstone");

        // store spells with their levels
        for (WizardsPlugin.SpellType spellType : WizardsPlugin.SpellType.values()) {
            int level = WizardsPlugin.getSpellLevel(player.getUniqueId(), spellType);
            if (level > 0) {
                ItemStack spellItem = new ItemStack(spellType.getMaterial());

                // set display name and lore to show level
                String spellName = WizardsPlugin.SPELL_NAMES.get(spellType.getMaterial());
                meta.setDisplayName(ChatColor.YELLOW + spellName);
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Level: " + ChatColor.GREEN + level,
                        ChatColor.GRAY + "Click to absorb this spell"
                ));

                spellItem.setItemMeta(meta);
                deathInventory.addItem(spellItem);
            }
        }

        // store other items normally
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR &&
                    !isSpellItem(item)) {
                deathInventory.addItem(item.clone());
            }
        }

        deathInventories.put(player.getUniqueId(), deathInventory);
    }

    @EventHandler
    public void onTombstoneClick(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;

        ArmorStand stand = (ArmorStand) event.getRightClicked();
        for (Map.Entry<UUID, ArmorStand> entry : playerTombstones.entrySet()) {
            if (entry.getValue().equals(stand)) {
                UUID deadPlayerId = entry.getKey();
                if (deathInventories.containsKey(deadPlayerId)) {
                    event.setCancelled(true);
                    event.getPlayer().openInventory(deathInventories.get(deadPlayerId));

                    // tombstone sound
                    event.getPlayer().playSound(event.getPlayer().getLocation(),
                            Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                }
                break;
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onTombstoneInventoryClick(InventoryClickEvent event) {
        // check if this is a tombstone inventory
        String title = event.getView().getTitle();
        if (!title.contains("'s Tombstone")) return; // proceed if it's a tombstone inventory (contains "'s Tombstone")
        if (!(event.getWhoClicked() instanceof Player player)) return;
//        event.setCancelled(true); // cancel the event

        ItemStack clickedItem = event.getCurrentItem();

        // cancel all events in tombstone inventory
        event.setCancelled(true);

        // handle null or air items
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        try {
            // get dead player's name from inventory title
            String playerName = event.getView().getTitle()
                    .replace("'s Tombstone", "")
                    .replace(ChatColor.RED.toString(), "");

            // find dead player's UUID
            UUID deadPlayerId = null;
            for (Map.Entry<UUID, ArmorStand> entry : playerTombstones.entrySet()) {
                Player deadPlayer = Bukkit.getPlayer(entry.getKey());
                if (deadPlayer != null && deadPlayer.getName().equals(playerName)) {
                    deadPlayerId = entry.getKey();
                    break;
                }
            }

            if (deadPlayerId == null) return;

            // basic wand (stick)
            if (clickedItem.getType() == Material.STICK) {
                ItemStack basicWand = new ItemStack(Material.STICK);
                ItemMeta meta = basicWand.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW + "Basic Wand");
                basicWand.setItemMeta(meta);

                player.getInventory().addItem(basicWand);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                // remove one stick
                if (clickedItem.getAmount() > 1) {
                    clickedItem.setAmount(clickedItem.getAmount() - 1);
                    event.getInventory().setItem(event.getSlot(), clickedItem);
                } else {
                    event.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                }
                return;
            }

            // handle spell items
            WizardsPlugin.SpellType spellType = SpellMenu.getSpellByMaterial(clickedItem.getType());
            if (spellType != null) {
                // get dead player's spell level
                int deadPlayerLevel = WizardsPlugin.getSpellLevel(deadPlayerId, spellType);

                if (deadPlayerLevel > 0) {
                    // increase player's spell level
                    for (int i = 0; i < deadPlayerLevel; i++) {
                        plugin.addSpellToPlayer(player.getUniqueId(), spellType);
                    }

                    // get spell name (use display name if available, otherwise default name)
                    String spellName = clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()
                            ? clickedItem.getItemMeta().getDisplayName()
                            : WizardsPlugin.SPELL_NAMES.get(clickedItem.getType());

                    // sound and message
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                    int newLevel = WizardsPlugin.getSpellLevel(player.getUniqueId(), spellType);

                    if (newLevel == deadPlayerLevel) {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + spellName + "> " +
                                ChatColor.GREEN + "Spell Acquired (Level " + newLevel + ")");
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + spellName + "> " +
                                ChatColor.GREEN + "Level +" + deadPlayerLevel + " -> " +
                                ChatColor.GREEN + "" + ChatColor.BOLD + newLevel);
                    }

                    // remove item from the tombstone
                    if (clickedItem.getAmount() > 1) {
                        clickedItem.setAmount(clickedItem.getAmount() - 1);
                        event.getInventory().setItem(event.getSlot(), clickedItem);
                    } else {
                        event.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                    }
                }
            }
        } catch (Exception e) {
            // logs
            plugin.getLogger().warning("Error handling tombstone click: " + e.getMessage());
        }
    }



    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------MAP-------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    private final Map<UUID, Map<BlockVector, BlockState>> savedBlocksMap = new HashMap<>();
    private static final int CHUNK_SIZE = 16; // define chunk size for saving and regenerating blocks

    // save blocks in chunks
    void saveBlocks(Vector loc1, Vector loc2, Player player) {
        UUID playerId = player.getUniqueId();
        Map<BlockVector, BlockState> blockMap = new HashMap<>();
        int blocksSaved = 0;

        // bounds for saving
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        // iterate through chunks ( 16x16x16 blocks )
        for (int x = minX; x <= maxX; x += CHUNK_SIZE) {
            for (int y = minY; y <= maxY; y += CHUNK_SIZE) {
                for (int z = minZ; z <= maxZ; z += CHUNK_SIZE) {
                    // process from (x, y, z) to (x + CHUNK_SIZE - 1, y + CHUNK_SIZE - 1, z + CHUNK_SIZE - 1)
                    for (int dx = x; dx < Math.min(x + CHUNK_SIZE, maxX + 1); dx++) {
                        for (int dy = y; dy < Math.min(y + CHUNK_SIZE, maxY + 1); dy++) {
                            for (int dz = z; dz < Math.min(z + CHUNK_SIZE, maxZ + 1); dz++) {
                                Block block = player.getWorld().getBlockAt(dx, dy, dz);
                                if (block.getType() != Material.AIR) {
                                    blockMap.put(new BlockVector(dx, dy, dz), block.getState());
                                    blocksSaved++;
                                }
                            }
                        }
                    }
                }
            }
        }

        savedBlocksMap.put(playerId, blockMap); // store saved blocks
        player.sendMessage(ChatColor.GREEN + "Saved " + blocksSaved + " blocks!");
    }

    // regenerate blocks in chunks
    void regenerateBlocks(Vector loc1, Vector loc2, Player player) {
        UUID playerId = player.getUniqueId();
        Map<BlockVector, BlockState> blockMap = savedBlocksMap.get(playerId);

        if (blockMap == null) {
            player.sendMessage(ChatColor.RED + "No blocks have been saved!");
            return;
        }

        // Define regeneration bounds
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        // bukkit runnable to process each layer
        new BukkitRunnable() {
            int y = minY; // start with lowest y layer
            int blocksRegenerated = 0;

            @Override
            public void run() {
                // process current layer at height y
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Block block = player.getWorld().getBlockAt(x, y, z);
                        BlockVector vector = new BlockVector(x, y, z);

                        if (blockMap.containsKey(vector)) {
                            BlockState savedState = blockMap.get(vector);

                            // check if the current block already matches saved state
                            if (block.getType() != savedState.getType() ||
                                    !block.getBlockData().matches(savedState.getBlockData())) {

                                // restore saved block state
                                block.setType(savedState.getType(), false);
                                block.getState().setBlockData(savedState.getBlockData());
                                block.getState().update(true, false);
                                blocksRegenerated++;
                            }
                        } else {
                            // set to air if not in saved state and not already air
                            if (block.getType() != Material.AIR) {
                                block.setType(Material.AIR, false);
                                blocksRegenerated++;
                            }
                        }
                    }
                }

                // move to next layer
                player.sendMessage(ChatColor.GREEN + "Layer " + y +"/" + maxY + " regenerated!");
                y++;
                // stop task when all layers are processed
                if (y > maxY) {
                    player.sendMessage(ChatColor.GREEN + "Regenerated " + blocksRegenerated + " blocks!");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // run every x ticks ( 20 ticks in 1 sec - 2L = 10x per sec )
    }


    // -----------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------CHESTS-----------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    final Map<UUID, Vector> location1Map = new HashMap<>(); // store location 1
    final Map<UUID, Vector> location2Map = new HashMap<>(); // store location 2
    private final Map<WizardsPlugin.SpellType, Integer> spellRarityWeights = new HashMap<>();
    private final Random random = new Random();

    private void initializeRarityWeights() {
        // higher number = more common
        spellRarityWeights.put(WizardsPlugin.SpellType.Basic_Wand, 100); // common
        spellRarityWeights.put(WizardsPlugin.SpellType.Fiery_Wand, 50);   // uncommon
        spellRarityWeights.put(WizardsPlugin.SpellType.Shrouded_Step, 15); // rare
        spellRarityWeights.put(WizardsPlugin.SpellType.Mjölnir, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.Gust, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.The_Great_Escape, 10);
        spellRarityWeights.put(WizardsPlugin.SpellType.Big_Man_Slam, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.Winged_Shield, 0);
        spellRarityWeights.put(WizardsPlugin.SpellType.VoidWalker, 10); // legendary
        spellRarityWeights.put(WizardsPlugin.SpellType.Starfall_Barrage, 25);
        spellRarityWeights.put(WizardsPlugin.SpellType.Heal_Cloud, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.Recall, 10);
        spellRarityWeights.put(WizardsPlugin.SpellType.Void_Orb, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.Dragon_Spit, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.Cod_Shooter, 25);
    }

    void fillChest(Block chestBlock) {
        // check if the block is a chest
        if (chestBlock.getState() instanceof Chest chest) {
            Inventory inventory = chest.getInventory();
            inventory.clear(); // clear existing items in the chest

            // randomly fill the chest with spells and armor
            int numSpells = random.nextInt(5); // random number of spells (0-4)
            int numArmor = random.nextInt(4); // random number of armor (0-3)

            // add random spells
            for (int i = 0; i < numSpells; i++) {
                ItemStack spellItem = getRandomSpell();
                if (spellItem != null) {
                    placeItemRandomly(inventory, spellItem);
                }
            }

            // add random armor
            for (int i = 0; i < numArmor; i++) {
                ItemStack armorItem = getRandomArmor();
                if (armorItem != null) {
                    placeItemRandomly(inventory, armorItem);
                }
            }
        }
    }
    private ItemStack getRandomSpell() {
        // initialize rarity weights if not already done
        if (spellRarityWeights.isEmpty()) {
            initializeRarityWeights();
        }

        // list to hold spells based on rarity weights
        List<WizardsPlugin.SpellType> weightedSpellList = new ArrayList<>();

        // populate the list according to weights
        for (Map.Entry<WizardsPlugin.SpellType, Integer> entry : spellRarityWeights.entrySet()) {
            WizardsPlugin.SpellType spellType = entry.getKey();
            int weight = entry.getValue();

            // add the spell to the list based on weight
            for (int i = 0; i < weight; i++) {
                weightedSpellList.add(spellType);
            }
        }

        // if there are no spells, return null
        if (weightedSpellList.isEmpty()) {
            return null;
        }

        // select a random spell from weighted list
        WizardsPlugin.SpellType randomSpellType = weightedSpellList.get(random.nextInt(weightedSpellList.size()));

        // create the spell
        ItemStack spellItem = new ItemStack(randomSpellType.getMaterial());
        ItemMeta meta = spellItem.getItemMeta();

        if (meta != null) {
            String formattedName = ChatColor.YELLOW + "" + ChatColor.BOLD + Menu.formatSpellName(randomSpellType.name());
            meta.setDisplayName(formattedName);
            spellItem.setItemMeta(meta);
        }

        return spellItem;
    }

    private ItemStack getRandomArmor() {
        Material[] armorMaterials = {Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
                Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
                Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS};
        return new ItemStack(armorMaterials[random.nextInt(armorMaterials.length)]);
    }

    private void placeItemRandomly(Inventory inventory, ItemStack item) {
        // get a random empty slot in the chest inventory
        int randomSlot;
        do {
            randomSlot = random.nextInt(inventory.getSize());
        } while (inventory.getItem(randomSlot) != null);

        inventory.setItem(randomSlot, item); // place the item in the random slot
    }
}
