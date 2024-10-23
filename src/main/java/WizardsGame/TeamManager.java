package WizardsGame;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamManager {
    private final HashMap<UUID, String> playerTeams = new HashMap<>();
    final HashMap<UUID, Boolean> damageToggle = new HashMap<>();
    private final HashMap<String, ChatColor> teamColors = new HashMap<>();

    public TeamManager() {
        // team colors
        teamColors.put("Team1", ChatColor.RED);
        teamColors.put("Team2", ChatColor.BLUE);
        teamColors.put("Team3", ChatColor.GREEN);
        teamColors.put("Team4", ChatColor.YELLOW);
    }

    public void addPlayerToTeam(Player player, String teamName) {
        UUID playerId = player.getUniqueId();
        playerTeams.put(playerId, teamName);
        damageToggle.put(playerId, true); // fefault to allowing damage
        updatePlayerListName(player);
    }

    public String getPlayerTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    public void toggleDamage(Player player) {
        UUID playerId = player.getUniqueId();
        damageToggle.put(playerId, !damageToggle.getOrDefault(playerId, true));
    }

    public boolean canDamage(Player attacker, Player target) {
        return !(playerTeams.get(attacker.getUniqueId()).equals(playerTeams.get(target.getUniqueId())) &&
                !damageToggle.getOrDefault(attacker.getUniqueId(), true));
    }

    public void updatePlayerListName(Player player) {
        String teamName = playerTeams.get(player.getUniqueId());
        ChatColor color = teamColors.getOrDefault(teamName, ChatColor.WHITE);

        // set display name and player list name
        player.setDisplayName(color + player.getName() + ChatColor.WHITE);
        player.setPlayerListName(color + "[" + teamName + "] " + player.getName());
    }
}
