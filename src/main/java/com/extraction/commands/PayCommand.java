package com.extraction.commands;

import com.extraction.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final EconomyManager economyManager;

    public PayCommand(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + label + " <player> <amount>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null) {
            player.sendMessage(ChatColor.RED + "Error: Could not find player '" + args[0] + "'.");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Error: You cannot pay yourself.");
            return true;
        }

        double amount;
        try {
            amount = economyManager.parseAmount(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Error: '" + args[1] + "' is not a valid amount.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Error: Amount must be positive.");
            return true;
        }

        double senderBalance = economyManager.getBalanceAsDouble(player.getUniqueId());
        if (senderBalance < amount) {
            player.sendMessage(ChatColor.RED + "Error: Insufficient funds. You have $" + economyManager.formatBalance(player.getUniqueId()) + ".");
            return true;
        }

        try {
            economyManager.takeBalance(player.getUniqueId(), amount);
            economyManager.addBalance(target.getUniqueId(), amount);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "CRITICAL ERROR: Failed to process payment.");
            e.printStackTrace();
            return true;
        }

        String formattedAmount = economyManager.formatNumber(amount);
        player.sendMessage(ChatColor.GREEN + "Success! You paid $" + formattedAmount + " to " + target.getName() + ".");

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(ChatColor.GREEN + "You received $" + formattedAmount + " from " + player.getName() + ".");
        }

        return true;
    }
}