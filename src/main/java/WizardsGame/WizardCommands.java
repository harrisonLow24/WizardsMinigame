package WizardsGame;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.*;

import static WizardsGame.TeamManager.teamColors;

public class WizardCommands implements CommandExecutor, Listener {

    private final WizardsPlugin plugin;
    CooldownManager Cooldown = new CooldownManager();
    TeamManager Team = new TeamManager();
    WizardsMinigame Mini = new WizardsMinigame(WizardsPlugin.getInstance());
    public WizardCommands(WizardsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("toggleinfinitemana")) {
            if (sender.hasPermission("wizardsplugin.toggleinfinitemana")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;  // cast once to avoid multiple castings
                    UUID playerId = player.getUniqueId();
                    plugin.Mana.toggleInfiniteMana(playerId);
                    sender.sendMessage(ChatColor.GREEN + "Infinite Mana " + (plugin.Mana.hasInfiniteMana(playerId) ? "enabled" : "disabled"));
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("togglecooldowns")) {
            if (sender.hasPermission("wizardsplugin.togglecooldowns")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    UUID playerId = player.getUniqueId();
                    plugin.Cooldown.toggleCooldowns(playerId);
                    sender.sendMessage(ChatColor.GREEN + "Cooldowns " + (plugin.Cooldown.hasCooldownsDisabled(playerId) ? "disabled" : "enabled"));
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("checkmana")) {
            if (sender.hasPermission("wizardsplugin.checkmana")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    UUID playerId = player.getUniqueId();

                    // retrieve and display player's current mana
                    double currentMana = plugin.Mana.getCurrentMana(playerId);
                    sender.sendMessage(ChatColor.BLUE + "Your current mana: " + currentMana);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
        }
        if (command.getName().equalsIgnoreCase("unlockspells")) {
            if (sender instanceof Player player) {
                UUID playerId = player.getUniqueId();
                plugin.unlockAllSpells(playerId);
                player.sendMessage("All spells have been unlocked!");
                return true;
            } else {
                sender.sendMessage("This command can only be executed by a player.");
                return false;
            }
        }

        if (command.getName().equalsIgnoreCase("wizards")) {
            if (args.length == 0) {
                if (sender instanceof Player player) {
                    openMenu(player, MenuType.MAIN);
                    return true;
                }
            }

            switch (args[0].toLowerCase()) {
                case "help":
//                    if (sender instanceof Player player) {
//                        openHelpBook(player);
//                        return true;
//                    } else {
//                        sender.sendMessage(ChatColor.RED + "Only players can use this command.");
//                    }
                    return true;
                case "join":
                    if (sender instanceof Player player) {
                        plugin.Mini.playersInMinigame.add(player.getUniqueId()); // add player to minigame
                        sender.sendMessage(ChatColor.GREEN + "You have joined the Wizards minigame!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Only players can join the minigame.");
                    }
                    return true;

                case "leave":
                    if (sender instanceof Player player) {
                        plugin.Mini.playersInMinigame.remove(player.getUniqueId()); // remove player from minigame
                        sender.sendMessage(ChatColor.GREEN + "You have left the Wizards minigame!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Only players can leave the minigame.");
                    }
                    return true;

                case "start":
                    if (plugin.Mini.playersInMinigame.size() > 2) {
                        sender.sendMessage(ChatColor.RED + "Not enough players to start the game!");
                        return true;
                    }
                    plugin.Mini.startMinigame(); // start minigame
                    return true;

                case "stop":
                    plugin.Mini.stopMinigame(); // stop minigame
                    return true;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown command. Use /wizards join, /wizards leave, /wizards start, or /wizards stop.");
                    return true;
            }
        }

        if (command.getName().equalsIgnoreCase("setspawn")) {
            if (sender instanceof Player player) {
                plugin.Mini.spawnPoints.add(player.getLocation()); // add player's current location as a spawn point
                sender.sendMessage(ChatColor.GREEN + "Spawn point set at your current location!");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can set spawn points.");
            }
        }

        if (command.getName().equalsIgnoreCase("wizteam")) {
            if (args.length < 1) {
                return false; // no subcommand provided
            }

            String subCommand = args[0];

            switch (subCommand.toLowerCase()) {
                case "create":
                    if (sender instanceof Player player) {
                        if (args.length < 2) {
                            sender.sendMessage(ChatColor.RED + "Please specify a team name.");
                            return true;
                        }
                        String createTeamName = args[1];
                        if (plugin.Team.createTeam(createTeamName,player.getUniqueId())) {
                            sender.sendMessage(ChatColor.GREEN + "Team " + createTeamName + " created!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Team " + createTeamName + " already exists.");
                        }
                        return true;
                    }
                case "delete":
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Please specify a team name.");
                        return true;
                    }
                    String deleteTeamName = args[1];
                    if (plugin.Team.deleteTeam(deleteTeamName)) {
                        sender.sendMessage(ChatColor.GREEN + "Team " + deleteTeamName + " deleted!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Team " + deleteTeamName + " does not exist.");
                    }
                    return true;

                case "join":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Only players can join teams.");
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Please specify a team name.");
                        return true;
                    }
                    String joinTeamName = args[1];
                    Player player = (Player) sender;
                    if (plugin.Team.joinTeam(player, joinTeamName)) {
//                        sender.sendMessage(ChatColor.GREEN + "You have joined team " + joinTeamName + "!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Team " + joinTeamName + " does not exist.");
                    }
                    return true;

                case "leave":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Only players can leave teams.");
                        return true;
                    }
                    Player leavingPlayer = (Player) sender;
                    if (plugin.Team.leaveTeam(leavingPlayer)) {
                        sender.sendMessage(ChatColor.GREEN + "You have left the team!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You are not in a team.");
                    }
                    return true;

                case "add":
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Please specify a player name and a team name.");
                        return true;
                    }
                    String playerNameToAdd = args[1];
                    String teamNameToAdd = args[2];
                    if (plugin.Team.addPlayer(playerNameToAdd, teamNameToAdd)) {
                        sender.sendMessage(ChatColor.GREEN + "Player " + playerNameToAdd + " added to team " + teamNameToAdd + "!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player not online or team does not exist.");
                    }
                    return true;

                case "remove":
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Please specify a player name and a team name.");
                        return true;
                    }
                    String playerNameToRemove = args[1];
                    String teamNameToRemove = args[2];
                    if (plugin.Team.removePlayer(playerNameToRemove, teamNameToRemove)) {
                        sender.sendMessage(ChatColor.GREEN + "Player " + playerNameToRemove + " removed from team " + teamNameToRemove + "!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player not online or team does not exist.");
                    }
                    return true;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown team command. Use /Wizteam create, /Wizteam delete, /Wizteam join, /Wizteam leave, /Wizteam add, or /Wizteam remove.");
                    return true;
            }
        }


        if (command.getName().equalsIgnoreCase("map1")) {
            if (sender instanceof Player player) {
                Vector loc1 = player.getLocation().toVector(); // store player's current location
                plugin.Mini.location1Map.put(player.getUniqueId(), loc1);
                player.sendMessage(ChatColor.GREEN + "Location 1 saved!");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("map2")) {
            if (sender instanceof Player player) {
                Vector loc2 = player.getLocation().toVector(); // store player's current location
                plugin.Mini.location2Map.put(player.getUniqueId(), loc2);
                player.sendMessage(ChatColor.GREEN + "Location 2 saved!");
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("mapsave")) {
            if (sender instanceof Player player) {
                Vector loc1 = plugin.Mini.location1Map.get(player.getUniqueId());
                Vector loc2 = plugin.Mini.location2Map.get(player.getUniqueId());

                if (loc1 == null || loc2 == null) {
                    player.sendMessage(ChatColor.RED + "You must save both locations first!");
                    return true;
                }
                plugin.Mini.saveBlocks(loc1, loc2, player);
                player.sendMessage(ChatColor.GREEN + "Blocks saved!");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("mapregen")) {
            if (sender instanceof Player player) {
                Vector loc1 = plugin.Mini.location1Map.get(player.getUniqueId());
                Vector loc2 = plugin.Mini.location2Map.get(player.getUniqueId());

                if (loc1 == null || loc2 == null) {
                    player.sendMessage(ChatColor.RED + "You must save both locations first!");
                    return true;
                }

                plugin.Mini.regenerateBlocks(loc1, loc2, player);
                player.sendMessage(ChatColor.GREEN + "Blocks regenerated!");
                return true;
            }
        }


        if (command.getName().equalsIgnoreCase("fillchests")) {
            if (sender instanceof Player player) {
                UUID playerId = player.getUniqueId();
                Vector loc1 = plugin.Mini.location1Map.get(playerId);
                Vector loc2 = plugin.Mini.location2Map.get(playerId);

                if (loc1 == null || loc2 == null) {
                    player.sendMessage(ChatColor.RED + "You need to set both locations first!");
                    return true;
                }

                // determine coordinates for filling chests
                int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
                int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
                int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
                int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
                int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
                int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
                int count = 0;
                // iterate through all blocks in the defined region
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            // check if block is a chest
                            if (player.getWorld().getBlockAt(x, y, z).getType() == Material.CHEST) {
                                plugin.Mini.fillChest(player.getWorld().getBlockAt(x, y, z));
                                count++;
                            }
                        }
                    }
                }
                player.sendMessage(ChatColor.GREEN + "" + count + " Chests filled!");
                return true;
            }
        }


        return false;
    }
    enum MenuType {
        MAIN,
        ADMIN,
        TEAMS,
        TEAM_MANAGEMENT,
        ADMIN_TEAMS,
        ADMIN_PLAYERS
    }
    void openMenu(Player player, MenuType menuType) {
        switch(menuType) {
            case MAIN:
                openMainMenu(player);
                break;
            case ADMIN:
                openAdminMenu(player);
                break;
            case TEAMS:
                openTeamMenu(player);
                break;
            case TEAM_MANAGEMENT:
                openTeamManagementMenu(player);
                break;
            case ADMIN_TEAMS:
                openAdminTeamsMenu(player);
                break;
            case ADMIN_PLAYERS:
                openAdminPlayersMenu(player);
                break;
        }
    }

    private void openMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 27, ChatColor.BLUE + "Wizards Menu");

        // join/keave Game button
        ItemStack gameStatus = new ItemStack(
                plugin.Mini.playersInMinigame.contains(player.getUniqueId()) ?
                        Material.RED_BED : Material.GREEN_BED
        );
        ItemMeta gameStatusMeta = gameStatus.getItemMeta();
        gameStatusMeta.setDisplayName(
                plugin.Mini.playersInMinigame.contains(player.getUniqueId()) ?
                        ChatColor.RED + "Leave Game" : ChatColor.GREEN + "Join Game"
        );
        gameStatusMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to " +
                        (plugin.Mini.playersInMinigame.contains(player.getUniqueId()) ?
                                "leave the Wizards minigame" : "join the Wizards minigame"
                        )));
        gameStatus.setItemMeta(gameStatusMeta);
        menu.setItem(10, gameStatus);

        // admin commands button
        if (player.hasPermission("wizards.admin")) {
            ItemStack admin = new ItemStack(Material.COMMAND_BLOCK);
            ItemMeta adminMeta = admin.getItemMeta();
            adminMeta.setDisplayName(ChatColor.RED + "Admin Commands");
            adminMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to open admin commands"));
            admin.setItemMeta(adminMeta);
            menu.setItem(12, admin);
        }

        // team management button
        ItemStack teams = new ItemStack(Material.PAPER);
        ItemMeta teamsMeta = teams.getItemMeta();
        teamsMeta.setDisplayName(ChatColor.BLUE + "Team Management");
        teamsMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to manage teams"));
        teams.setItemMeta(teamsMeta);
        menu.setItem(14, teams);

        // help book button
        ItemStack help = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = help.getItemMeta();
        helpMeta.setDisplayName(ChatColor.GREEN + "Help Guide");
        helpMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to open help book"));
        help.setItemMeta(helpMeta);
        menu.setItem(16, help);

        // fill empty slots
        fillEmptySlots(menu);
        player.openInventory(menu);
    }

    // admin commands menu
    private void openAdminMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 36, ChatColor.RED + "Admin Commands");

        // map tools
        menu.setItem(10, createMenuItem(Material.MAP, ChatColor.GREEN + "Set Map Point 1", ChatColor.WHITE + "Click to set first map corner"));
        menu.setItem(11, createMenuItem(Material.MAP, ChatColor.GREEN + "Set Map Point 2", ChatColor.WHITE + "Click to set second map corner"));
        menu.setItem(12, createMenuItem(Material.WRITABLE_BOOK, ChatColor.YELLOW + "Save Map Area", ChatColor.WHITE + "Click to save the selected area"));
        menu.setItem(13, createMenuItem(Material.BOOK, ChatColor.YELLOW + "Regenerate Map", ChatColor.WHITE + "Click to regenerate the saved area"));
        menu.setItem(14, createMenuItem(Material.CHEST, ChatColor.GOLD + "Fill Chests", ChatColor.WHITE + "Click to fill chests in the area"));

        // game control
        menu.setItem(16, createMenuItem(Material.EMERALD, ChatColor.GREEN + "Start Game", ChatColor.WHITE + "Click to start the minigame"));
        menu.setItem(17, createMenuItem(Material.REDSTONE, ChatColor.RED + "Stop Game", ChatColor.WHITE + "Click to stop the minigame"));
        menu.setItem(22, createMenuItem(Material.COMPASS, ChatColor.AQUA + "Set Spawn Point", ChatColor.WHITE + "Click to set a spawn point"));

        // admin team/player management options
        menu.setItem(28, createMenuItem(Material.COMMAND_BLOCK, ChatColor.DARK_PURPLE + "View All Teams",
                ChatColor.WHITE + "Click to view and manage all teams"));
        menu.setItem(29, createMenuItem(Material.PLAYER_HEAD, ChatColor.DARK_PURPLE + "View All Players",
                ChatColor.WHITE + "Click to view and manage all players"));
//        menu.setItem(32, createMenuItem(Material.ANVIL, ChatColor.DARK_RED + "Force Create Team",
//                "Click to forcibly create a team"));
//        menu.setItem(33, createMenuItem(Material.BARRIER, ChatColor.DARK_RED + "Force Delete Team",
//                "Click to forcibly delete a team"));

        // back button
        menu.setItem(31, createMenuItem(Material.ARROW, ChatColor.GRAY + "Back", "Return to main menu"));

        fillEmptySlots(menu);
        player.openInventory(menu);
    }

    // team selection menu
    private void openTeamMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 27, ChatColor.BLUE + "Team Selection");

        // create team button
        ItemStack createTeam = createMenuItem(Material.ANVIL, ChatColor.GREEN + "Create Team",
                ChatColor.WHITE + "Click to create a new team");

        // existing teams
        int slot = 10;
        for (String teamName : Team.getTeams()) {
            ItemStack team = createMenuItem(Material.PAPER, ChatColor.YELLOW + teamName,
                    ChatColor.WHITE + "Click to join this team");
            menu.setItem(slot++, team);
            if (slot == 17) slot = 19; // skip to next row
        }

        // leave team button if in a team
        if (Team.isPlayerOnTeam(player.getUniqueId())) {
            ItemStack leaveTeam = createMenuItem(Material.BARRIER, ChatColor.RED + "Leave Team",
                    ChatColor.WHITE + "Click to leave your current team");
            menu.setItem(22, leaveTeam);
            menu.setItem(22, leaveTeam);
        }

        // team management button (for team leaders/admins)
        if (player.hasPermission("wizards.admin") || Team.isTeamLeader(player.getUniqueId())) {
            ItemStack manageTeam = createMenuItem(Material.WRITABLE_BOOK, ChatColor.GOLD + "Manage Team",
                    ChatColor.WHITE + "Click to manage team settings");
            menu.setItem(23, manageTeam);
        }

        // back button
        ItemStack back = createMenuItem(Material.ARROW, ChatColor.GRAY + "Back",
                ChatColor.WHITE + "Return to main menu");
        menu.setItem(26, back);

        fillEmptySlots(menu);
        player.openInventory(menu);
    }

    // team management menu
    private void openTeamManagementMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 36, ChatColor.GOLD + "Team Management");

        // create team button (only if not in a team)
        if (!plugin.Team.isPlayerOnTeam(player.getUniqueId())) {
            ItemStack createTeam = createMenuItem(Material.ANVIL, ChatColor.GREEN + "Create Team",
                    ChatColor.WHITE + "Click to create your own team");
            menu.setItem(10, createTeam);
        }

        // delete team button (only for team leaders)
        if (plugin.Team.isTeamLeader(player.getUniqueId())) {
            ItemStack deleteTeam = createMenuItem(Material.BARRIER, ChatColor.RED + "Disband Team",
                    ChatColor.WHITE + "Click to delete your team");
            menu.setItem(16, deleteTeam);
        }

        // admin options
        if (player.hasPermission("wizards.admin")) {
            ItemStack adminTeams = createMenuItem(Material.COMMAND_BLOCK, ChatColor.DARK_PURPLE + "Admin: View All Teams",
                    ChatColor.WHITE + "Click to view all teams");
            menu.setItem(21, adminTeams);

            ItemStack adminPlayers = createMenuItem(Material.PLAYER_HEAD, ChatColor.DARK_PURPLE + "Admin: View All Players",
                    ChatColor.WHITE + "Click to view all players");
            menu.setItem(23, adminPlayers);
        }

        // Back button
        ItemStack back = createMenuItem(Material.ARROW, ChatColor.GRAY + "Back",
                ChatColor.WHITE + "Return to team selection");
        menu.setItem(31, back);

        fillEmptySlots(menu);
        player.openInventory(menu);
    }

    private void openAdminTeamsMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 54, ChatColor.DARK_PURPLE + "Admin: All Teams");

        int slot = 0;
        for (String teamName : plugin.Team.getTeams()) {
            Set<UUID> members = plugin.Team.getTeamMembers(teamName);
            int memberCount = members.size();

            ItemStack teamItem = createMenuItem(Material.PAPER,
                    teamColors.get(teamName) + teamName,
                    ChatColor.GRAY + "Members: " + memberCount,
                    ChatColor.GRAY + "Leader: " + Bukkit.getOfflinePlayer(plugin.Team.getTeamLeader(teamName)).getName(),
                    "",
                    ChatColor.RED + "Left-click to delete",
                    ChatColor.YELLOW + "Right-click to teleport to team");

            menu.setItem(slot++, teamItem);
            if (slot >= 45) break; // Limit to first page
        }

        // Back button
        ItemStack back = createMenuItem(Material.ARROW, ChatColor.GRAY + "Back",
                ChatColor.WHITE + "Return to team management");
        menu.setItem(49, back);

        fillEmptySlots(menu);
        player.openInventory(menu);
    }

    private void openAdminPlayersMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 54, ChatColor.DARK_PURPLE + "Admin: All Players");

        int slot = 0;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String teamName = plugin.Team.getPlayerTeam(onlinePlayer.getUniqueId());
            String teamStatus = teamName != null ?
                    ChatColor.GREEN + "In team: " + teamName :
                    ChatColor.RED + "No team";

            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + onlinePlayer.getName());
            meta.setOwningPlayer(onlinePlayer);
            meta.setLore(Arrays.asList(
                    teamStatus,
                    ChatColor.GRAY + "Game status: " +
                            (plugin.Mini.playersInMinigame.contains(onlinePlayer.getUniqueId()) ?
                                    ChatColor.GREEN + "In game" : ChatColor.RED + "Not in game"),
                    "",
                    ChatColor.RED + "Left-click to kick from team",
                    ChatColor.YELLOW + "Right-click to teleport"
//                    ChatColor.BLUE + "Shift-click to force join game"
            ));
            playerHead.setItemMeta(meta);

            menu.setItem(slot++, playerHead);
            if (slot >= 45) break; // limit to first page
        }

        // back button
        ItemStack back = createMenuItem(Material.ARROW, ChatColor.GRAY + "Back",
                "Return to team management");
        menu.setItem(49, back);

        fillEmptySlots(menu);
        player.openInventory(menu);
    }

    // helper method to create menu items
    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    // helper method to fill empty slots
    private void fillEmptySlots(Inventory menu) {
        ItemStack filler = createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i, filler);
            }
        }
    }


    @EventHandler
    public void onAdminMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.BLUE + "Wizards Admin Menu")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (clicked.getType()) {
            case MAP:
                if (clicked.getItemMeta().getDisplayName().contains("Point 1")) {
                    player.performCommand("map1");
                } else {
                    player.performCommand("map2");
                }
                break;

            case WRITABLE_BOOK:
                player.performCommand("mapsave");
                break;

            case BOOK:
                player.performCommand("mapregen");
                break;

            case CHEST:
                player.performCommand("fillchests");
                break;

            case EMERALD:
                player.performCommand("wizards start");
                break;

            case REDSTONE:
                player.performCommand("wizards stop");
                break;
        }

        player.closeInventory();
    }
    private void openHelpBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        // set book title/author
        meta.setTitle("Wizards Guide");
        meta.setAuthor("WizardsPlugin");

        // create pages
        List<BaseComponent[]> pages = new ArrayList<>();

        // table of contents (2)
        TextComponent toc = new TextComponent("= WIZARDS HELP =\n\n");
        toc.setColor(net.md_5.bungee.api.ChatColor.DARK_PURPLE);
        toc.setBold(true);

        // clickable TOC entries
        addTocEntry(pages, toc, "Table of Contents", 2);
        addTocEntry(pages, toc, "", 2);
        addTocEntry(pages, toc, "Rules", 3);
        addTocEntry(pages, toc, "Teams", 4);
        addTocEntry(pages, toc, "Minigame", 5);
        addTocEntry(pages, toc, "Spells", 6);
        addTocEntry(pages, toc, "Commands", 7);

        pages.add(new ComponentBuilder(toc).create());

        // rules (3)
        pages.add(new ComponentBuilder(
                "       = RULES =     \n\n" +
                        "• You need a" + net.md_5.bungee.api.ChatColor.DARK_GREEN + " basic wand " + net.md_5.bungee.api.ChatColor.BLACK + "(stick) to start off!\n" +
                        "• Picking up a spell will level the spell up!\n" +
                        net.md_5.bungee.api.ChatColor.DARK_GREEN + "• Left click " + net.md_5.bungee.api.ChatColor.BLACK + "wands to cast spells\n" +
                        net.md_5.bungee.api.ChatColor.DARK_GREEN + "• Right click " + net.md_5.bungee.api.ChatColor.BLACK + "wands to open the spell menu"
        ).create());

        // teams (4)
        pages.add(new ComponentBuilder(
                "       = TEAMS =     \n\n" +
                        "• Create teams with /wizteam or through the Wizards Menu book!\n" +
                        "• Friendly fire is disabled\n"
//                        "Team chat is automatic\n" +
        ).create());

        // minigame (5)
        pages.add(new ComponentBuilder(
                "     = MINIGAME =     \n\n" +
                        "• Loot chests and players to acquire and upgrade spells!\n" +
                        "• Eliminating other players will spawn their tombstone to loot!\n" +
                        "• Last one standing wins!"
        ).create());

        // spells (6)
//        TextComponent spellsPage = new TextComponent("=== SPELLS ===\n\n");
//        for (WizardsPlugin.SpellType spell : WizardsPlugin.SpellType.values()) {
//            spellsPage.addExtra(createSpellText(spell) + "\n");
//        }
//        pages.add(new ComponentBuilder(spellsPage).create());
        // spells section
        List<BaseComponent[]> spellPages = new ArrayList<>();
        TextComponent currentSpellPage = new TextComponent("      = SPELLS =     \n\n");
        int lineCount = 2;

        for (WizardsPlugin.SpellType spell : WizardsPlugin.SpellType.values()) {
            TextComponent spellLine = new TextComponent(createSpellText(spell) + "\n");

            // exceed page limit (14 lines per page)
            if (lineCount >= 14) {
                spellPages.add(new ComponentBuilder(currentSpellPage).create());
                // start new page
                currentSpellPage = new TextComponent("  = SPELLS (cont.) =  \n\n");
                lineCount = 2; // reset counter
            }

            currentSpellPage.addExtra(spellLine);
            lineCount++;
        }

        // add the last page if it has content
        if (lineCount > 2) {
            spellPages.add(new ComponentBuilder(currentSpellPage).create());
        }

        // add spell pages to the pages list
        pages.addAll(spellPages);

        // commands (7)
        pages.add(new ComponentBuilder(
                "     = COMMANDS =     \n\n" +
                        "/wizards - Open Wizards Menu\n" +
                        "/wizards help - This book\n" +
                        "/wizards join - Join game\n" +
                        "/wizards leave - Quit game\n" +
                        "/wizards start - Begin match\n" +
                        "/wizteam - Team management\n" +
                        "/cooldowns - Toggle CDs\n" +
                        "/mana - Toggle mana"
        ).create());

        meta.spigot().setPages(pages);
        book.setItemMeta(meta);

        player.openBook(book);
    }
    private void addTocEntry(List<BaseComponent[]> pages, TextComponent toc, String text, int page) {
        TextComponent entry = new TextComponent(text + "\n");
        entry.setColor(net.md_5.bungee.api.ChatColor.BLUE);
        entry.setUnderlined(true);
        entry.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, String.valueOf(page-1)));
        toc.addExtra(entry);
    }

    private String createSpellText(WizardsPlugin.SpellType spell) {
        return spell.name().replace("_", " ");
//                + ChatColor.GRAY + " - " + getSpellDescription(spell);
    }

//    private String getSpellDescription(WizardsPlugin.SpellType spell) {
//        switch(spell) {
//            default: return "Basic spell";
//        }
//    }

    private final Map<UUID, String> pendingTeamCreations = new HashMap<>();
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // check if player has a pending team creation
        if (pendingTeamCreations.containsKey(playerId)) {
            event.setCancelled(true);
            String teamName = event.getMessage().trim();

            // validate team name
            if (teamName.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Team name cannot be empty!");
                pendingTeamCreations.remove(playerId);
                return;
            }

            if (teamName.length() > 16) {
                player.sendMessage(ChatColor.RED + "Team name must be 16 characters or less!");
                pendingTeamCreations.remove(playerId);
                return;
            }

            if (!teamName.matches("[a-zA-Z0-9_]+")) {
                player.sendMessage(ChatColor.RED + "Team name can only contain letters, numbers and underscores!");
                pendingTeamCreations.remove(playerId);
                return;
            }

            // create the team
            if (plugin.Team.createTeam(teamName, playerId)) {
//                player.sendMessage(ChatColor.GREEN + "Team " + teamName + " created successfully!");
                plugin.Team.joinTeam(player, teamName);
                openMenu(player, MenuType.TEAMS); // return to teams menu
            } else {
                player.sendMessage(ChatColor.RED + "Team " + teamName + " already exists!");
            }

            pendingTeamCreations.remove(playerId);
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        pendingTeamCreations.remove(event.getPlayer().getUniqueId());
    }
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = event.getView().getTitle();
        if (event.getClickedInventory() == null) return;
        // skip if not a custom inventories
        if (!title.equals(ChatColor.BLUE + "Wizards Menu") && !title.equals(ChatColor.RED + "Admin Commands") && !title.equals(ChatColor.BLUE + "Team Selection")
                && !title.equals("Team Management")&& !title.equals(ChatColor.GOLD + "Team Management") && !title.equals(ChatColor.DARK_PURPLE + "Admin: All Teams")
                && !title.equals(ChatColor.DARK_PURPLE + "Admin: All Players")
                && !title.equals("Select a Spell") && !title.equals("Chest") && !title.contains("'s Tombstone")) return;
        if (event.getView().getTitle().contains("'s Tombstone")) return;
        if (event.getView().getTitle().contains("Chest")) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        event.setCancelled(true);

        try {
            // main Menu
            if (title.equals(ChatColor.BLUE + "Wizards Menu")) {
                switch (clicked.getType()) {
                    case GREEN_BED:
                        player.performCommand("wizards join");
                        player.closeInventory();
                        break;
                    case RED_BED:
                        player.performCommand("wizards leave");
                        player.closeInventory();
                        break;
                    case COMMAND_BLOCK:
                        openMenu(player, MenuType.ADMIN);
                        break;
                    case PAPER:
                        openMenu(player, MenuType.TEAMS);
                        break;
                    case BOOK:
                        openHelpBook(player);
                        player.closeInventory();
                        openHelpBook(player);
                        break;
                }
            }
            // admin menu
            else if (title.equals(ChatColor.RED + "Admin Commands")) {
                if (clicked.getType() == Material.ARROW) {
                    openMenu(player, MenuType.MAIN);
                    return;
                }

                if (!player.hasPermission("wizards.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission for this!");
                    return;
                }

                switch (clicked.getType()) {
                    case MAP:
                        if (clicked.getItemMeta().getDisplayName().contains("Point 1")) {
                            player.performCommand("map1");
                            player.closeInventory();
                        } else {
                            player.performCommand("map2");
                            player.closeInventory();
                        }
                        break;
                    case WRITABLE_BOOK:
                        player.performCommand("mapsave");
                        player.closeInventory();
                        break;
                    case BOOK:
                        player.performCommand("mapregen");
                        player.closeInventory();
                        break;
                    case CHEST:
                        player.performCommand("fillchests");
                        player.closeInventory();
                        break;
                    case EMERALD:
                        player.performCommand("wizards start");
                        player.closeInventory();
                        break;
                    case REDSTONE:
                        player.performCommand("wizards stop");
                        player.closeInventory();
                        break;
                    case COMPASS:
                        player.performCommand("setspawn");
                        player.closeInventory();
                        break;
                    case PLAYER_HEAD:
                        openAdminPlayersMenu(player);
                        break;
                    case COMMAND_BLOCK:
                        openAdminTeamsMenu(player);
                        break;
                }
            }
            // team selection menu
            else if (title.equals(ChatColor.BLUE + "Team Selection")) {
                if (clicked.getType() == Material.ARROW) {
                    openMenu(player, MenuType.MAIN);
                    return;
                }

                switch (clicked.getType()) {
                    case PAPER:
                        // join existing team
                        String teamName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                        player.performCommand("wizteam join " + teamName);
                        player.closeInventory();
                        break;
                    case ANVIL:
                        player.closeInventory();
                        pendingTeamCreations.put(player.getUniqueId(), "create");
                        player.sendMessage(ChatColor.YELLOW + "Type the new team name in chat:");
                        player.sendMessage(ChatColor.GRAY + " - Must be 1-16 characters");
                        break;
                    case BARRIER:
                        player.performCommand("wizteam leave");
                        player.closeInventory();
                        break;
                    case WRITABLE_BOOK:
                        openMenu(player, MenuType.TEAM_MANAGEMENT);
                        break;
                }
            }
            // team management menu
            else if (title.equals(ChatColor.GOLD + "Team Management")) {
                if (clicked.getType() == Material.ARROW) {
                    openMenu(player, MenuType.TEAMS);
                    return;
                }

                switch (clicked.getType()) {
                    case BARRIER:
                        String teamName = plugin.Team.getPlayerTeam(player.getUniqueId());
                        if (teamName != null) {
                            player.performCommand("wizteam leave");
                            plugin.Team.deleteTeam(teamName);
                            player.sendMessage(ChatColor.RED + "Team " + teamName + " has been disbanded!");
                            openMenu(player, MenuType.TEAMS);
                        }
                        break;
                    case ANVIL:
                        player.closeInventory();
                        pendingTeamCreations.put(player.getUniqueId(), "create");
                        player.sendMessage(ChatColor.YELLOW + "Type the new team name in chat:");
                        break;
                    case PLAYER_HEAD:
                        openAdminPlayersMenu(player);
                        break;
                    case COMMAND_BLOCK:
                        openAdminTeamsMenu(player);
                        break;
                }
            }
            else if (title.equals(ChatColor.DARK_PURPLE + "Admin: All Teams")) {
                if (clicked.getType() == Material.ARROW) {
                    openMenu(player, MenuType.ADMIN);
                    return;
                }

                switch (clicked.getType()) {
                    case PAPER:
                        String teamName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                        // handle left-click (delete) and right-click (teleport) here
                        if (event.isLeftClick()) {
                            plugin.Team.deleteTeam(teamName);
                            player.sendMessage(ChatColor.RED + "Team " + teamName + " has been deleted!");
                            openAdminTeamsMenu(player);
                        } else if (event.isRightClick()) {
                            // teleport to first team member
                            Set<UUID> members = plugin.Team.getTeamMembers(teamName);
                            if (!members.isEmpty()) {
                                Player firstMember = Bukkit.getPlayer(members.iterator().next());
                                if (firstMember != null) {
                                    player.teleport(firstMember);
                                    player.sendMessage(ChatColor.GREEN + "Teleported to team " + teamName);
                                }
                            }
                        }
                        break;
                }
            }
            else if (title.equals(ChatColor.DARK_PURPLE + "Admin: All Players")) {
                if (clicked.getType() == Material.ARROW) {
                    openMenu(player, MenuType.ADMIN);
                    return;
                }

                switch (clicked.getType()) {
                    case PLAYER_HEAD:
                        String playerName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                        Player target = Bukkit.getPlayer(playerName);
                        if (target != null) {
                            if (event.isLeftClick()) {
                                // kick from team
                                plugin.Team.leaveTeam(target);
                                player.sendMessage(ChatColor.RED + "Kicked " + playerName + " from their team!");
                            } else if (event.isRightClick()) {
                                // teleport to player
                                player.teleport(target);
                                player.sendMessage(ChatColor.GREEN + "Teleported to " + playerName);
                            } else if (event.isShiftClick()) {
                                // force join game
                                plugin.Mini.playersInMinigame.add(target.getUniqueId());
                                player.sendMessage(ChatColor.GREEN + "Forced " + playerName + " to join the game!");
                            }
                            openAdminPlayersMenu(player);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An error occurred. Please try again.");
        }
    }
}
