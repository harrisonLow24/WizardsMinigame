package WizardsGame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WizTeamTabCompleter implements TabCompleter {
    private final TeamManager Team;

    public WizTeamTabCompleter(TeamManager Team) {
        this.Team = Team;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Check if the base command is "wizteam"
        if (command.getName().equalsIgnoreCase("wizteam")) {
            if (args.length == 1) {
                // subcommands for first argument
                return Arrays.asList("create", "delete", "join", "leave", "add", "remove");
            } else if (args.length == 2) {
                List<String> teamNames = new ArrayList<>(Team.getTeamNames());
                switch (args[0].toLowerCase()) {
                    case "create":
                        return null;
                    case "delete":
                    case "join":
                        return teamNames; // team name
                    case "add":
                    case "remove":
                        return null; // player name
                }
            }
        }
        return null;
    }
}
