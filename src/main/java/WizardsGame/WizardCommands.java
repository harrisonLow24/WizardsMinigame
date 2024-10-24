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
    TeamManager teamManager = new TeamManager();
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
                    Cooldown.toggleCooldowns(playerId);
                    sender.sendMessage(ChatColor.GREEN + "Cooldowns " + (Cooldown.hasCooldownsDisabled(playerId) ? "disabled" : "enabled"));
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

        if (command.getName().equalsIgnoreCase("add")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /add <player> <team>");
                return true;
            }

            Player targetPlayer = plugin.getServer().getPlayer(args[0]);
            String teamName = args[1];

            if (targetPlayer != null) {
                teamManager.addPlayerToTeam(targetPlayer, teamName);
                sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " has been added to " + teamName);
                // update player's name in the tab list
                teamManager.updatePlayerListName(targetPlayer);
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("togglefriendlyfire")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                teamManager.toggleDamage(player);
                boolean canDamage = teamManager.damageToggle.get(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "You have " + (canDamage ? "enabled" : "disabled") + " damage to teammates.");
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            }
            return true;
        }



        return false;
    }
}
