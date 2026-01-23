package com.extraction.commands;

import com.extraction.managers.TermsOfServiceManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TosCommand implements CommandExecutor {

    private final TermsOfServiceManager tosManager;

    public TosCommand(TermsOfServiceManager tosManager) {
        this.tosManager = tosManager;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player) sender;
        tosManager.displayTermsOfService(player);
        return true;
    }
}