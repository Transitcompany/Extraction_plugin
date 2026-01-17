package com.extraction.managers;

import com.extraction.team.Team;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    private Map<UUID, Team> teams = new HashMap<>();
    private Map<UUID, UUID> pendingInvites = new HashMap<>(); // player -> teamId

    public Team createTeam(String name, Player leader) {
        UUID teamId = UUID.randomUUID();
        Team team = new Team(teamId, name, leader.getUniqueId());
        teams.put(teamId, team);
        return team;
    }

    public boolean invitePlayer(Player inviter, Player invitee, Team team) {
        if (!team.isLeader(inviter.getUniqueId())) return false;
        pendingInvites.put(invitee.getUniqueId(), team.getId());
        return true;
    }

    public boolean acceptInvite(Player player) {
        UUID teamId = pendingInvites.get(player.getUniqueId());
        if (teamId == null) return false;
        Team team = teams.get(teamId);
        if (team == null) return false;
        team.addMember(player.getUniqueId());
        pendingInvites.remove(player.getUniqueId());
        return true;
    }

    public void leaveTeam(Player player) {
        UUID playerId = player.getUniqueId();
        Team team = getPlayerTeam(playerId);
        if (team != null) {
            team.removeMember(playerId);
            if (team.getMembers().isEmpty()) {
                teams.remove(team.getId());
            } else if (team.isLeader(playerId)) {
                // Transfer leadership to first member
                UUID newLeader = team.getMembers().get(0);
                team = new Team(team.getId(), team.getName(), newLeader); // Recreate with new leader
                teams.put(team.getId(), team);
            }
        }
    }

    public Team getPlayerTeam(UUID playerId) {
        for (Team team : teams.values()) {
            if (team.getMembers().contains(playerId)) {
                return team;
            }
        }
        return null;
    }

    public Team getTeamByName(String name) {
        for (Team team : teams.values()) {
            if (team.getName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }

    public Map<UUID, Team> getTeams() { return teams; }
    public Map<UUID, UUID> getPendingInvites() { return pendingInvites; }
}