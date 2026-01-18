package com.extraction.commands;

import com.extraction.managers.GovernanceManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RulesCommand implements CommandExecutor {

    private final GovernanceManager governanceManager;

    public RulesCommand(GovernanceManager governanceManager) {
        this.governanceManager = governanceManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player) sender;
        governanceManager.displayRules(player);
        return true;
    }
}