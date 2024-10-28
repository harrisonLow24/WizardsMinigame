package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.*;

import static WizardsGame.TeamManager.teamColors;
import static WizardsGame.TeamManager.teams;

public class WizardsMinigame {
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
    final Set<UUID> playersInMinigame = new HashSet<>(); // store players in the minigame

    private void sendTitle(Player player, String title, String subtitle) {
        // show title
        player.sendTitle(ChatColor.YELLOW + title, ChatColor.GREEN + subtitle, 10, 70, 20);
    }
    public void startMinigame() {
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
                        }
                        teleportToRandomSpawn(player);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void teleportToRandomSpawn(Player player) {
        if (!spawnPoints.isEmpty()) {
            List<Location> spawnPointList = new ArrayList<>(spawnPoints);
            Location randomSpawn = spawnPointList.get(new Random().nextInt(spawnPointList.size()));
            player.teleport(randomSpawn); // teleport to a random spawn point
            player.sendMessage(ChatColor.GREEN + "You've been teleported to a spawn point!");
        }
    }

    public boolean hasGameEnded() {
        int aliveTeams = 0;

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

            // more than one team still has alive players, game continues
            if (aliveTeams > 1) {
                return false;
            }
        }
        endMinigame();
        return aliveTeams <= 1; // game ends if zero or one team is alive
    }

    //temp
    public String getWinningTeam() {
        if (teams.size() > 1) { // check if there are multiple teams
            for (String teamName : teams.keySet()) {
                Set<UUID> members = teams.get(teamName);

                // team mode
                for (UUID memberId : members) {
                    if (WizardsPlugin.playerAliveStatus.getOrDefault(memberId, true)) {
                        return teamName; // return team name if at least one member is alive
                    }
                }
            }
        } else { // free-for-all  mode
            for (UUID playerId : playersInMinigame) {
                if (WizardsPlugin.playerAliveStatus.getOrDefault(playerId, true)) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        return player.getName(); // return the player’s name if they are the last alive
                    }
                }
            }
        }
        return null; // no winning entity if all players/teams are eliminated
    }

    public void endMinigame() {
        String winningTeam = getWinningTeam();
        if (winningTeam != null) {
            ChatColor teamColor = teamColors.getOrDefault(winningTeam, ChatColor.GOLD);
            String winningMessage = teamColor + "Team " + winningTeam + " has won the Wizards Minigame!";
            // announce winning team to all players
            Bukkit.broadcastMessage(winningMessage);
            for (UUID playerId : playersInMinigame) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    sendTitle(player, winningTeam, ChatColor.GOLD + "" + ChatColor.BOLD +"Congratulations! You are the chosen one!");
                }
            }
        } else {
            Bukkit.broadcastMessage(ChatColor.GRAY + "The Wizards Minigame ended in a draw!");
        }
        resetGame(); // clear all players from minigame
    }

    void stopMinigame() {
        for (UUID playerId : playersInMinigame) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.RED + "The minigame has been stopped.");
            }
        }
        playersInMinigame.clear(); // clear all players from minigame
    }

    private void resetGame() {
        Team.clearTeams(); // clear all teams and reset scoreboard

        // reset player alive status
        for (UUID playerId : WizardsPlugin.playerAliveStatus.keySet()) {
            WizardsPlugin.playerAliveStatus.put(playerId, true); // mark all players as alive
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------MAP-------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    private final Map<UUID, Map<BlockVector, Material>> savedBlocksMap = new HashMap<>();
    void saveBlocks(Vector loc1, Vector loc2, Player player) {
        UUID playerId = player.getUniqueId();
        Map<BlockVector, Material> blockMap = new HashMap<>();

        // bounds
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = player.getWorld().getBlockAt(x, y, z);
                    blockMap.put(new BlockVector(x, y, z), block.getType()); // save block type
                }
            }
        }
        savedBlocksMap.put(playerId, blockMap); // store saved blocks
    }

    void regenerateBlocks(Vector loc1, Vector loc2, Player player) {
        UUID playerId = player.getUniqueId();
        Map<BlockVector, Material> blockMap = savedBlocksMap.get(playerId);

        if (blockMap == null) {
            player.sendMessage(ChatColor.RED + "No blocks have been saved!");
            return;
        }

        for (Map.Entry<BlockVector, Material> entry : blockMap.entrySet()) {
            BlockVector vector = entry.getKey();
            Material material = entry.getValue();
            Block block = player.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
            block.setType(material); // replace block with saved type
        }
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
        spellRarityWeights.put(WizardsPlugin.SpellType.Fiery_Wand, 30);   // uncommon
        spellRarityWeights.put(WizardsPlugin.SpellType.Shrouded_Step, 15); // rare
        spellRarityWeights.put(WizardsPlugin.SpellType.Mjölnir, 25);
        spellRarityWeights.put(WizardsPlugin.SpellType.Gust, 30);
        spellRarityWeights.put(WizardsPlugin.SpellType.The_Great_Escape, 15);
        spellRarityWeights.put(WizardsPlugin.SpellType.Big_Man_Slam, 30);
        spellRarityWeights.put(WizardsPlugin.SpellType.Winged_Shield, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.VoidWalker, 5); // legendary
        spellRarityWeights.put(WizardsPlugin.SpellType.Starfall_Barrage, 25);
        spellRarityWeights.put(WizardsPlugin.SpellType.Heal_Cloud, 50);
        spellRarityWeights.put(WizardsPlugin.SpellType.Recall, 5);
        spellRarityWeights.put(WizardsPlugin.SpellType.Void_Orb, 15);
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
