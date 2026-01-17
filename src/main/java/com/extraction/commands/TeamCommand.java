package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.data.PlayerDataManager.PlayerData;
import com.extraction.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class TeamCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;

    public TeamCommand(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use team commands.");
            return true;
        }

        Player player = (Player) sender;
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /team create <name>");
                    return true;
                }
                if (data.getTeamName() != null) {
                    sender.sendMessage(ChatColor.RED + "You are already in a team.");
                    return true;
                }
                String name = args[1];
                if (plugin.getTeamManager().getTeamByName(name) != null) {
                    sender.sendMessage(ChatColor.RED + "Team name already exists.");
                    return true;
                }
                Team team = plugin.getTeamManager().createTeam(name, player);
                data.setTeamName(name);
                plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Created team '" + name + "'.");
                break;

            case "invite":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /team invite <player>");
                    return true;
                }
                Team playerTeam = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
                if (playerTeam == null || !playerTeam.isLeader(player.getUniqueId())) {
                    sender.sendMessage(ChatColor.RED + "You are not the leader of a team.");
                    return true;
                }
                Player invitee = Bukkit.getPlayer(args[1]);
                if (invitee == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                PlayerData inviteeData = plugin.getPlayerDataManager().getPlayerData(invitee);
                if (inviteeData.getTeamName() != null) {
                    sender.sendMessage(ChatColor.RED + "Player is already in a team.");
                    return true;
                }
                if (plugin.getTeamManager().invitePlayer(player, invitee, playerTeam)) {
                    sender.sendMessage(ChatColor.GREEN + "Invited " + invitee.getName() + " to your team.");
                    invitee.sendMessage(ChatColor.GREEN + "You have been invited to team '" + playerTeam.getName() + "'. Use /team accept to join.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to invite.");
                }
                break;

            case "accept":
                if (plugin.getTeamManager().acceptInvite(player)) {
                    Team acceptedTeam = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
                    data.setTeamName(acceptedTeam.getName());
                    plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
                    sender.sendMessage(ChatColor.GREEN + "Joined team '" + acceptedTeam.getName() + "'.");
                    // Notify team
                    for (UUID memberId : acceptedTeam.getMembers()) {
                        if (!memberId.equals(player.getUniqueId())) {
                            Player member = Bukkit.getPlayer(memberId);
                            if (member != null) {
                                member.sendMessage(ChatColor.GREEN + player.getName() + " joined the team.");
                            }
                        }
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "No pending invite.");
                }
                break;

            case "leave":
                if (data.getTeamName() == null) {
                    sender.sendMessage(ChatColor.RED + "You are not in a team.");
                    return true;
                }
                plugin.getTeamManager().leaveTeam(player);
                data.setTeamName(null);
                plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Left the team.");
                break;

            case "chat":
                data.setTeamChatEnabled(!data.isTeamChatEnabled());
                plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Team chat " + (data.isTeamChatEnabled() ? "enabled" : "disabled") + ".");
                break;

            case "stash":
                Team stashTeam = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
                if (stashTeam == null) {
                    sender.sendMessage(ChatColor.RED + "You are not in a team.");
                    return true;
                }
                player.openInventory(stashTeam.getSharedStash());
                break;

            default:
                sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Team Commands:");
        sender.sendMessage(ChatColor.GRAY + "/team create <name> - Create a team");
        sender.sendMessage(ChatColor.GRAY + "/team invite <player> - Invite a player (leader only)");
        sender.sendMessage(ChatColor.GRAY + "/team accept - Accept an invite");
        sender.sendMessage(ChatColor.GRAY + "/team leave - Leave your team");
        sender.sendMessage(ChatColor.GRAY + "/team chat - Toggle team chat");
        sender.sendMessage(ChatColor.GRAY + "/team stash - Open team stash");
    }
}