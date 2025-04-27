package WizardsGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class TeamManager {
    static final Map<String, Set<UUID>> teams = new HashMap<>(); // store teams and their members
    static final Map<String, UUID> teamLeaders = new HashMap<>(); // store team leaders
    static final Map<String, ChatColor> teamColors = new HashMap<>(); // store team colors
    private final Scoreboard scoreboard; // scoreboard for team prefixes
    static Scoreboard sidebarScoreboard; // scoreboard for sidebar
    static Objective sidebarObjective; // objective for sidebar
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

    // get all team names
    public Set<String> getTeams() {
        return teams.keySet();
    }

    // check if player is team leader
    public boolean isTeamLeader(UUID playerId) {
        return teamLeaders.containsValue(playerId);
    }

    // get team by leader UUID
    public String getTeamByLeader(UUID leaderId) {
        for (Map.Entry<String, UUID> entry : teamLeaders.entrySet()) {
            if (entry.getValue().equals(leaderId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // set team leader
    public void setTeamLeader(String teamName, UUID playerId) {
        teamLeaders.put(teamName, playerId);
    }
    public UUID getTeamLeader(String teamName) {
        // check if team exists and has a leader
        if (teamLeaders.containsKey(teamName)) {
            return teamLeaders.get(teamName);
        }
        return null; // return null if team doesn't exist
    }

    // get team members
    public Set<UUID> getTeamMembers(String teamName) {
        return teams.getOrDefault(teamName, new HashSet<>());
    }

    private ChatColor getRandomColor() {
        if (teamColors.isEmpty()) {
            return generateRandomColor();
        }
        Random random = new Random();
        ChatColor[] colors = teamColors.values().toArray(new ChatColor[0]);
        return colors[random.nextInt(colors.length)];
    }

    private ChatColor generateRandomColor() {
        ChatColor[] possibleColors = ChatColor.values();
        return possibleColors[new Random().nextInt(possibleColors.length)];
    }

    // updated createTeam to include leader
    public boolean createTeam(String teamName, UUID leaderId) {
        if (teams.containsKey(teamName)) {
            return false;
        }
        teams.put(teamName, new HashSet<>());
        teamLeaders.put(teamName, leaderId);
        ChatColor color = getRandomColor();
        teamColors.put(teamName, color);
        teams.get(teamName).add(leaderId); // add leader to members

        WizardsMinigame.playersInMinigame.add(leaderId);
        updateSidebar();

        Team scoreboardTeam = scoreboard.registerNewTeam(teamName);
        scoreboardTeam.setPrefix(color + "" + ChatColor.BOLD + "[" + teamName + "] " + ChatColor.RESET);

        Player leader = Bukkit.getPlayer(leaderId);
        if (leader != null) {
            setPlayerTeamPrefix(leader, teamName);
            leader.setScoreboard(sidebarScoreboard);
                leader.sendMessage(ChatColor.GREEN + "Team " + teamName + " created!");
        }
        for (UUID playerId : WizardsMinigame.playersInMinigame) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                WizardsMinigame.sendTitle(player, "Game Started!", "Good luck!");
                WizardsPlugin.playerAliveStatus.put(playerId, true);
                // set the player's scoreboard
                player.setScoreboard(sidebarScoreboard);
                updateSidebar();
            }
        }
        return true;
    }

    // updated deleteTeam to handle leaders
    public boolean deleteTeam(String teamName) {
        teamLeaders.remove(teamName);
        teams.remove(teamName);
        Team scoreboardTeam = scoreboard.getTeam(teamName);
        if (scoreboardTeam != null) {
            scoreboardTeam.unregister();
        }
        updateSidebar();

        return true;
    }

    // updated joinTeam
    public boolean joinTeam(Player player, String teamName) {
        Set<UUID> teamMembers = teams.get(teamName);
        if (teamMembers == null) {
            return false;
        }
        teamMembers.add(player.getUniqueId());
        setPlayerTeamPrefix(player, teamName);
        player.setScoreboard(sidebarScoreboard);

        WizardsMinigame.playersInMinigame.add(player.getUniqueId());
        updateSidebar();
        player.sendMessage(ChatColor.GREEN + "You've joined team " + teamName + " and entered the minigame!");

        return true;
    }

    // updated leaveTeam to handle leaders
    public boolean leaveTeam(Player player) {
        String teamName = getPlayerTeam(player.getUniqueId());
        if (teamName != null) {
            UUID playerId = player.getUniqueId();
            teams.get(teamName).remove(playerId);

            // if leader leaves, disband team or assign new leader
            if (player.getUniqueId().equals(teamLeaders.get(teamName))) {
                if (teams.get(teamName).isEmpty()) {
                    deleteTeam(teamName);
                } else {
                    // assign new leader (first member found)
                    UUID newLeader = teams.get(teamName).iterator().next();
                    teamLeaders.put(teamName, newLeader);
                    Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
                    if (newLeaderPlayer != null) {
                        newLeaderPlayer.sendMessage(ChatColor.YELLOW + "You are now the leader of " + teamName);
                    }
                }
            }

            Team scoreboardTeam = scoreboard.getTeam(teamName);
            if (scoreboardTeam != null) {
                scoreboardTeam.removeEntry(player.getName());
            }
            resetPlayerName(player);
            resetPlayerScoreboard(player);
            WizardsMinigame.clearSidebar();
            updateSidebar();
            return true;
        }
        updateSidebar();
        return false;
    }

    // updated addPlayer
    public boolean addPlayer(String playerName, String teamName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !teams.containsKey(teamName)) {
            return false;
        }
        teams.get(teamName).add(player.getUniqueId());
        setPlayerTeamPrefix(player, teamName);
        player.setScoreboard(sidebarScoreboard);
        return true;
    }

    // updated removePlayer
    public boolean removePlayer(String playerName, String teamName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !teams.containsKey(teamName)) {
            return false;
        }
        UUID playerId = player.getUniqueId();
        teams.get(teamName).remove(playerId);

        // handle if removing leader
        if (playerId.equals(teamLeaders.get(teamName))) {
            if (teams.get(teamName).isEmpty()) {
                deleteTeam(teamName);
            } else {
                // assign new leader
                UUID newLeader = teams.get(teamName).iterator().next();
                setTeamLeader(teamName, newLeader);
            }
        }

        Team scoreboardTeam = scoreboard.getTeam(teamName);
        if (scoreboardTeam != null) {
            scoreboardTeam.removeEntry(player.getName());
        }
        resetPlayerName(player);
        resetPlayerScoreboard(player);
        return true;
    }

    // rename team
    public boolean renameTeam(String oldName, String newName) {
        if (teams.containsKey(newName) || !teams.containsKey(oldName)) {
            return false;
        }

        // move all data to new name
        Set<UUID> members = teams.remove(oldName);
        UUID leader = teamLeaders.remove(oldName);
        ChatColor color = teamColors.remove(oldName);

        teams.put(newName, members);
        teamLeaders.put(newName, leader);
        teamColors.put(newName, color);

        // update scoreboard team
        Team oldTeam = scoreboard.getTeam(oldName);
        if (oldTeam != null) {
            oldTeam.unregister();
        }

        Team newTeam = scoreboard.registerNewTeam(newName);
        newTeam.setPrefix(color + "" + ChatColor.BOLD + "[" + newName + "] " + ChatColor.RESET);
        for (UUID memberId : members) {
            Player player = Bukkit.getPlayer(memberId);
            if (player != null) {
                newTeam.addEntry(player.getName());
            }
        }

        return true;
    }

    private void setPlayerTeamPrefix(Player player, String teamName) {
        ChatColor color = teamColors.get(teamName);
        String prefix = color + "" + ChatColor.BOLD  +"[" + teamName + "] " + ChatColor.RESET;

        player.setDisplayName(prefix + player.getName());
        player.setPlayerListName(prefix + player.getName());

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
            return color + "" + ChatColor.BOLD + "[" + teamName + "]";
        }
        return "";
    }

    private void resetPlayerName(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public static boolean isPlayerOnTeam(UUID playerId) {
        for (Set<UUID> teamMembers : teams.values()) {
            if (teamMembers.contains(playerId)) {
                return true;
            }
        }
        return false;
    }

    public static String getPlayerTeam(UUID playerId) {
        for (Map.Entry<String, Set<UUID>> entry : teams.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Set<String> getTeamNames() {
        return teams.keySet();
    }

    public void clearTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (String teamName : teams.keySet()) {
            Team scoreboardTeam = scoreboard.getTeam(teamName);
            if (scoreboardTeam != null) {
                for (UUID playerId : teams.get(teamName)) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        player.setDisplayName(player.getName());
                        player.setPlayerListName(player.getName());
                        resetPlayerScoreboard(player);
                    }
                }
                scoreboardTeam.unregister();
            }
        }
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
        teams.clear();
        teamColors.clear();
        teamLeaders.clear();
    }

    void updateSidebar() {
        sidebarObjective.getScore(" ").setScore(16);
        for (String entry : sidebarScoreboard.getEntries()) {
            sidebarScoreboard.resetScores(entry);
        }

        String title = ChatColor.GOLD + "" + ChatColor.BOLD + "Wizards";
        sidebarObjective.getScore(title).setScore(15);

        String status = WizardsMinigame.isMinigameActive ?
                ChatColor.GREEN + "Game: Running" :
                ChatColor.YELLOW + "Game: Waiting";
        sidebarObjective.getScore(status).setScore(14);

        String line = ChatColor.GRAY + "--------------------";
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
                        break;
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

        String line11 = "";
        sidebarObjective.getScore(line11).setScore(10);

        int score = 9;
        if (!isSoloGame) {
            for (Map.Entry<String, Set<UUID>> entry : teams.entrySet()) {
                String teamName = entry.getKey();
                Set<UUID> members = entry.getValue();
                int aliveCount = 0;

                for (UUID memberId : members) {
                    Player player = Bukkit.getPlayer(memberId);
                    if (player != null && player.isOnline() && WizardsPlugin.playerAliveStatus.getOrDefault(memberId, true)) {
                        aliveCount++;
                    }
                }
                if (aliveCount > 0) {
                    String displayName = teamColors.get(teamName) + teamName + ": " + ChatColor.WHITE + aliveCount;
                    sidebarObjective.getScore(displayName).setScore(score);
                    score--;
                }
            }
        } else {
            for (UUID playerId : WizardsPlugin.playerAliveStatus.keySet()) {
                if (WizardsPlugin.playerAliveStatus.getOrDefault(playerId, true)) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        String displayName = ChatColor.GREEN + player.getName();
                        sidebarObjective.getScore(displayName).setScore(score);
                        score--;
                    }
                }
            }
        }
    }

    void resetPlayerScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}