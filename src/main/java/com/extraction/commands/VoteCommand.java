package com.extraction.commands;

import com.extraction.managers.GovernanceManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteCommand implements CommandExecutor {

    private final GovernanceManager governanceManager;

    public VoteCommand(GovernanceManager governanceManager) {
        this.governanceManager = governanceManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /vote <yes|no>");
            return true;
        }

        boolean yes;
        if (args[0].equalsIgnoreCase("yes")) {
            yes = true;
        } else if (args[0].equalsIgnoreCase("no")) {
            yes = false;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid vote. Use 'yes' or 'no'.");
            return true;
        }

        governanceManager.vote(player, yes);
        return true;
    }
}