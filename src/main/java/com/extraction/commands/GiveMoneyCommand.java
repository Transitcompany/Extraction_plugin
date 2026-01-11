package com.extraction.commands;

import com.extraction.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveMoneyCommand implements CommandExecutor {

    private final EconomyManager economyManager;
    private static final String REQUIRED_PERMISSION = "extraction.admin.givemoney";

    public GiveMoneyCommand(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission(REQUIRED_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + label + " <player> <amount>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage(ChatColor.RED + "Error: Could not find player '" + args[0] + "'.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Error: '" + args[1] + "' is not a valid number.");
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "Error: Amount must be positive.");
            return true;
        }

        try {
            economyManager.addBalance(target.getUniqueId(), amount);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "CRITICAL ERROR: Failed to add money.");
            e.printStackTrace();
            return true;
        }

        String formattedAmount = String.format("%,.2f", amount);
        sender.sendMessage(ChatColor.GREEN + "Success! $" + formattedAmount + " added to " + target.getName() + "'s balance.");

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(ChatColor.GREEN + "You received $" + formattedAmount + " from an administrator.");
        }

        return true;
    }
}