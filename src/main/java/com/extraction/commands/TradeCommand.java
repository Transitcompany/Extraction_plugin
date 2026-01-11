package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.TradeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {
    private final ExtractionPlugin plugin;
    private final TradeManager tradeManager;

    public TradeCommand(ExtractionPlugin plugin, TradeManager tradeManager) {
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
        if (args.length != 1) {
            player.sendMessage("Usage: /trade <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("Player not found.");
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage("You cannot trade with yourself.");
            return true;
        }
        tradeManager.setCurrentTradeTarget(player, target);
        tradeManager.openSenderGUI(player);
        // First, add to TradeManager.
        return true;
    }
}