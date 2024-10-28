package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.UUID;

public class WizardCommands implements CommandExecutor {

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
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /wizards join/leave/start/stop");
                return true;
            }

            switch (args[0].toLowerCase()) {
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
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Please specify a team name.");
                        return true;
                    }
                    String createTeamName = args[1];
                    if (plugin.Team.createTeam(createTeamName)) {
                        sender.sendMessage(ChatColor.GREEN + "Team " + createTeamName + " created!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Team " + createTeamName + " already exists.");
                    }
                    return true;

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
                        sender.sendMessage(ChatColor.GREEN + "You have joined team " + joinTeamName + "!");
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
}
