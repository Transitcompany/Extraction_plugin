package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.TradeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptTradeCommand implements CommandExecutor {
    private final ExtractionPlugin plugin;
    private final TradeManager tradeManager;

    public AcceptTradeCommand(ExtractionPlugin plugin, TradeManager tradeManager) {
        this.plugin = plugin;
        this.tradeManager = tradeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        tradeManager.acceptTrade(player);
        return true;
    }
}