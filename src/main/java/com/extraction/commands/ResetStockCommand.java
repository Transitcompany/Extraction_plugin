package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetStockCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final ShopManager shopManager;

    public ResetStockCommand(ExtractionPlugin plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("extraction.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        shopManager.resetStock();
        
        String message = ChatColor.GREEN + "" + ChatColor.BOLD + "Shop stock has been reset!";
        
        if (sender instanceof Player) {
            ((Player) sender).sendMessage(message);
        }
        
        plugin.getLogger().info("Shop stock reset by " + sender.getName());
        
        return true;
    }
}