package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.data.Rank;
import com.extraction.data.PlayerDataManager.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveRankCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;

    public GiveRankCommand(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /giverank <player> <rank>");
            sender.sendMessage(ChatColor.YELLOW + "Ranks: P, D, VIP, H, O");
            return true;
        }

        // Check permission: only Owner or Helper can give ranks
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(playerSender);
            if (data.getRank() != Rank.O && data.getRank() != Rank.H) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to give ranks.");
                return true;
            }
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        Rank newRank = Rank.fromString(args[1]);
        if (newRank == Rank.P && !args[1].equalsIgnoreCase("P")) {
            sender.sendMessage(ChatColor.RED + "Invalid rank. Use: P, D, VIP, H, O");
            return true;
        }

        PlayerData targetData = plugin.getPlayerDataManager().getPlayerData(target);
        targetData.setRank(newRank);
        plugin.getPlayerDataManager().savePlayerData(target.getUniqueId());

        plugin.assignPlayerToTeam(target);

        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s rank to " + newRank.getDisplayName());
        target.sendMessage(ChatColor.GREEN + "Your rank has been set to " + newRank.getDisplayName());

        return true;
    }
}