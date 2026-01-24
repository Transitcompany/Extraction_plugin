package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RedeemCommand implements CommandExecutor {
    
    private final ExtractionPlugin plugin;
    
    public RedeemCommand(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can redeem codes.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length != 1) {
            player.sendMessage("§cUsage: /redeem <code>");
            return true;
        }
        
        String code = args[0].toUpperCase(); // Codes are case-insensitive
        
        if (plugin.getRedemptionCodeManager().redeemCode(player, code)) {
            player.sendMessage("§a§lCode redeemed successfully!");
        } else {
            player.sendMessage("§c§lInvalid or already used code.");
        }
        
        return true;
    }
}