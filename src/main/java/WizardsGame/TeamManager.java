package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class TeamManager {
    static final Map<String, Set<UUID>> teams = new HashMap<>(); // store teams and their members
    static final Map<String, ChatColor> teamColors = new HashMap<>(); // store team colors
    private final Scoreboard scoreboard; // scoreboard for team prefixes
    final Scoreboard sidebarScoreboard; // scoreboard for sidebar
    final Objective sidebarObjective; // objective for sidebar
    boolean isSoloGame = (teams.isEmpty());

    public TeamManager() {
        // predefined colors that can be used for teams
        for (ChatColor color : ChatColor.values()) {
            if (color.isColor()) {
                teamColors.put(color.name(), color);
            }
        }

        // set up the scoreboard
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getMainScoreboard();
        sidebarScoreboard = manager.getNewScoreboard();
        sidebarObjective = sidebarScoreboard.registerNewObjective("alivePlayers", "dummy", ChatColor.AQUA + "" + ChatColor.BOLD + "Hareesun's Wizards");
        sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private ChatColor getRandomColor() {
        // check if teamColors map is empty
        if (teamColors.isEmpty()) {
            // generate a random color if no team colors are available
            return generateRandomColor(); // generate a random ChatColor
        }

        Random random = new Random();
        ChatColor[] colors = teamColors.values().toArray(new ChatColor[0]);
        return colors[random.nextInt(colors.length)]; // return a random color from available team colors
    }

    private ChatColor generateRandomColor() {
        // generate a random color
        ChatColor[] possibleColors = ChatColor.values();
        return possibleColors[new Random().nextInt(possibleColors.length)];
    }
    // create a new team
    public boolean createTeam(String teamName) {
        if (teams.containsKey(teamName)) {
            return false; // team already exists
        }
        teams.put(teamName, new HashSet<UUID>()); // create new team
        ChatColor color = getRandomColor(); // assign random color to team
        teamColors.put(teamName, color);

        // new scoreboard team
        Team scoreboardTeam = scoreboard.registerNewTeam(teamName);
        scoreboardTeam.setPrefix(color + "" + ChatColor.BOLD + "[" + teamName + "] " + ChatColor.RESET);

        return true;
    }

    // delete a team
    public boolean deleteTeam(String teamName) {
        teams.remove(teamName);
        Team scoreboardTeam = scoreboard.getTeam(teamName);
        if (scoreboardTeam != null) {
            scoreboardTeam.unregister();
        }
        return true;
    }

    // join a team
    public boolean joinTeam(Player player, String teamName) {
        Set<UUID> teamMembers = teams.get(teamName);
        if (teamMembers == null) {
            return false; // team does not exist
        }
        teamMembers.add(player.getUniqueId());

        // set player's display name, tab list name, and add to scoreboard team
        setPlayerTeamPrefix(player, teamName);
        player.setScoreboard(sidebarScoreboard);
        return true;
    }

    // leave a team
    public boolean leaveTeam(Player player) {
        String teamName = getPlayerTeam(player.getUniqueId());
        if (teamName != null) {
            teams.get(teamName).remove(player.getUniqueId());

            // remove from scoreboard team and reset player's names
            Team scoreboardTeam = scoreboard.getTeam(teamName);
            if (scoreboardTeam != null) {
                scoreboardTeam.removeEntry(player.getName());
            }
            resetPlayerName(player);
            resetPlayerScoreboard(player);
            return true;
        }
        return false;
    }

    // add a player to a team
    public boolean addPlayer(String playerName, String teamName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !teams.containsKey(teamName)) {
            return false; // player not online or team does not exist
        }
        teams.get(teamName).add(player.getUniqueId());

        // set player's display name, tab list name, and add to scoreboard team
        setPlayerTeamPrefix(player, teamName);

        player.setScoreboard(sidebarScoreboard);

        return true;
    }

    // remove a player from a team
    public boolean removePlayer(String playerName, String teamName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !teams.containsKey(teamName)) {
            return false; // player not online or team does not exist
        }
        teams.get(teamName).remove(player.getUniqueId());

        // remove from scoreboard team and reset player's names
        Team scoreboardTeam = scoreboard.getTeam(teamName);
        if (scoreboardTeam != null) {
            scoreboardTeam.removeEntry(player.getName());
        }
        resetPlayerName(player);
        resetPlayerScoreboard(player);

        return true;
    }

    // func to set player's display name, tab list name, and add to scoreboard team
    private void setPlayerTeamPrefix(Player player, String teamName) {
        ChatColor color = teamColors.get(teamName);
        String prefix = color + "" + ChatColor.BOLD  +"[" + teamName + "] " + ChatColor.RESET;

        // set display name for chat
        player.setDisplayName(prefix + player.getName());

        // set name in the tab list
        player.setPlayerListName(prefix + player.getName());

        // add to the scoreboard team for name display
        Team scoreboardTeam = scoreboard.getTeam(teamName);
        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(teamName);
            scoreboardTeam.setPrefix(prefix);
        }
        scoreboardTeam.addEntry(player.getName());
        player.setScoreboard(scoreboard);
    }

    public static String getTeamPrefix(UUID playerId) {
        String teamName = getPlayerTeam(playerId);
        if (teamName != null) {
            ChatColor color = teamColors.get(teamName);
            return color + "" + ChatColor.BOLD + "[" + teamName + "]"; // prefix:  [TeamName]
        }
        return ""; // no prefix if player is not on a team
    }

    // reset player's names when leaving a team
    private void resetPlayerName(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    // check if a player is on a team
    public boolean isPlayerOnTeam(UUID playerId) {
        for (Set<UUID> teamMembers : teams.values()) {
            if (teamMembers.contains(playerId)) {
                return true; // player is on a team
            }
        }
        return false; // player is not on any team
    }

    // get the team of a player
    public static String getPlayerTeam(UUID playerId) {
        for (Map.Entry<String, Set<UUID>> entry : teams.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                return entry.getKey(); // return team name
            }
        }
        return null; // player is not on a team
    }

    public Set<String> getTeamNames() {
        return teams.keySet();
    }

    public void clearTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        // Iterate through each team to remove players from teams and reset prefixes
        for (String teamName : teams.keySet()) {
            Team scoreboardTeam = scoreboard.getTeam(teamName);

            if (scoreboardTeam != null) {
                for (UUID playerId : teams.get(teamName)) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        // reset player's display name to remove the prefix
                        player.setDisplayName(player.getName());
                        player.setPlayerListName(player.getName());
                        resetPlayerScoreboard(player);
                    }
                }
                scoreboardTeam.unregister(); // remove team from scoreboard
            }
            }
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
        teams.clear(); // remove all teams and members
        teamColors.clear();
    }
    void updateSidebar() {
        sidebarObjective.getScore(" ").setScore(15);
        for (String entry : sidebarScoreboard.getEntries()) {
            sidebarScoreboard.resetScores(entry);
        }
        // display title with a line underneath
        String title = ChatColor.GOLD + "" + ChatColor.BOLD + "Wizards";
        sidebarObjective.getScore(title).setScore(14); // title

        // add a line under the title
        String line = ChatColor.GRAY + "--------------------"; // line under the title
        sidebarObjective.getScore(line).setScore(13);

        String placeholderText = ChatColor.YELLOW + "" + ChatColor.BOLD + (isSoloGame ? "Wizards alive" : "Teams left");
        sidebarObjective.getScore(placeholderText).setScore(12);

        if (!isSoloGame) {
            int teamsLeft = 0;
            for (Set<UUID> members : teams.values()) {
                for (UUID memberId : members) {
                    Player player = Bukkit.getPlayer(memberId);
                    if (WizardsPlugin.playerAliveStatus.getOrDefault(memberId, true)) {
                        teamsLeft++;
                        break; // break after finding one alive player in the team
                    }
                }
            }
            String teamsLeftText = ChatColor.WHITE + "" + teamsLeft;
            sidebarObjective.getScore(teamsLeftText).setScore(11);
        } else {
            int wizardsAlive = 0;
            for (UUID playerId : WizardsPlugin.playerAliveStatus.keySet()) {
                if (WizardsPlugin.playerAliveStatus.getOrDefault(playerId, true)) {
                    wizardsAlive++;
                }
            }
            String wizardsAliveText = ChatColor.WHITE + "" + wizardsAlive;
            sidebarObjective.getScore(wizardsAliveText).setScore(11);
        }
        String line11 =  ("");
        sidebarObjective.getScore(line11).setScore(10);
        int score = 9;
        if (!isSoloGame) {
            for (Map.Entry<String, Set<UUID>> entry : teams.entrySet()) {
                String teamName = entry.getKey();
                Set<UUID> members = entry.getValue();
                int aliveCount = 0;

                // count alive players in teams
                for (UUID memberId : members) {
                    Player player = Bukkit.getPlayer(memberId);
                    if (player != null && player.isOnline() && WizardsPlugin.playerAliveStatus.getOrDefault(memberId, true)) {
                        aliveCount++;
                    }
                }
                if (aliveCount > 0) {
                    String displayName = teamColors.get(teamName) + teamName + ": " + ChatColor.WHITE + aliveCount;
                    sidebarObjective.getScore(displayName).setScore(score);
                    score--; // decrement score for the next team
                }
            }
        } else {
            // solo mode: display all alive players
            for (UUID playerId : WizardsPlugin.playerAliveStatus.keySet()) {
                if (WizardsPlugin.playerAliveStatus.getOrDefault(playerId, true)) { // only display if player is alive
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        String displayName = ChatColor.GREEN + player.getName();
                        sidebarObjective.getScore(displayName).setScore(score);
                        score--; // decrement score for the next player
                    }
                }
            }
        }
    }

    void resetPlayerScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
