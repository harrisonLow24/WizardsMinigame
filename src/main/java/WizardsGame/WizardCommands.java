package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WizardCommands implements CommandExecutor {

    private final WizardsPlugin plugin;
    CooldownManager Cooldown = new CooldownManager();
    TeamManager Team = new TeamManager();
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
        return false;
    }
}
