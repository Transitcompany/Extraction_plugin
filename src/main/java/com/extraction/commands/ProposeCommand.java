package com.extraction.commands;

import com.extraction.managers.GovernanceManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProposeCommand implements CommandExecutor {

    private final GovernanceManager governanceManager;

    public ProposeCommand(GovernanceManager governanceManager) {
        this.governanceManager = governanceManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /propose <rule description>");
            return true;
        }

        String description = String.join(" ", args);
        if (description.length() > 200) {
            player.sendMessage(ChatColor.RED + "Proposal too long (max 200 characters).");
            return true;
        }

        if (governanceManager.proposeRule(player, description)) {
            player.sendMessage(ChatColor.GREEN + "Proposal submitted!");
        }
        return true;
    }
}