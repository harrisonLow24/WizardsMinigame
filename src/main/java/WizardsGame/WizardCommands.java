package WizardsGame;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WizardCommands implements CommandExecutor {

    private final WizardsPlugin plugin;
    CooldownManager Cooldown = new CooldownManager();

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
        return false;
    }
}
