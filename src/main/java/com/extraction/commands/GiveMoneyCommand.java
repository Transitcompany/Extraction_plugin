package com.extraction.commands;

import com.extraction.economy.EconomyManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveMoneyCommand implements CommandExecutor {

    private final EconomyManager economyManager;
    // Define a permission constant for clarity and easy reference
    private static final String REQUIRED_PERMISSION =
        "extraction.admin.givemoney";

    public GiveMoneyCommand(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    /**
     * Utility method to format the currency amount to two decimal places.
     * This uses String.format, which is inherently thread-safe.
     * @param amount The double value to format.
     * @return The formatted string (e.g., "1,234.56").
     */
    private String formatCurrency(double amount) {
        // Formats to two decimal places with comma grouping (based on default locale)
        return String.format("%,.2f", amount);
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        // --- 1. Permission Check (Only needed if sender is not console) ---
        // Console does not have a permission, so we skip the check for console.
        if (
            sender instanceof Player &&
            !sender.hasPermission(REQUIRED_PERMISSION)
        ) {
            sender.sendMessage(
                ChatColor.RED +
                    "You do not have permission to use this command."
            );
            return true;
        }

        // --- 2. Argument Count Check ---
        if (args.length != 2) {
            sender.sendMessage(
                ChatColor.YELLOW +
                    "Usage: " +
                    ChatColor.WHITE +
                    "/" +
                    label +
                    " <player> <amount>"
            );
            return true;
        }

        // --- 3. Amount Validation ---
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(
                ChatColor.RED +
                    "Error: " +
                    ChatColor.GRAY +
                    "'" +
                    args[1] +
                    "'" +
                    ChatColor.RED +
                    " is not a valid numerical amount."
            );
            return true;
        }

        // Check if the amount is positive
        if (amount <= 0) {
            sender.sendMessage(
                ChatColor.RED +
                    "Error: The amount must be a positive value (greater than 0)."
            );
            return true;
        }

        // --- 4. Target Player Resolution (Using OfflinePlayer for robustness) ---

        // Suppress warning for deprecated method, as it's the simplest way for a name lookup.
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        // Check if a player with that name has ever logged in (to ensure UUID can be retrieved)
        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage(
                ChatColor.RED +
                    "Error: Could not find player '" +
                    args[0] +
                    "'. Have they logged in before?"
            );
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        String targetName = target.getName();

        // Fallback for name if it's null (rare, but possible with OfflinePlayer)
        if (targetName == null) {
            targetName = args[0];
        }

        // --- 5. Economy Interaction (FIXED: Calling the void method directly) ---

        try {
            // This is the call that was causing the error, now called directly.
            economyManager.addBalance(targetUUID, amount);
        } catch (Exception e) {
            // Catch any unexpected exceptions from the economy manager (e.g., DB error)
            sender.sendMessage(
                ChatColor.RED +
                    "CRITICAL ERROR: Failed to add money due to an internal issue. Check console."
            );
            e.printStackTrace();
            return true;
        }

        // --- 6. Success Messages ---

        String formattedAmount = formatCurrency(amount);

        // Success message to the sender (e.g., console/admin)
        sender.sendMessage(
            ChatColor.GREEN +
                "Success! " +
                ChatColor.GOLD +
                "$" +
                formattedAmount +
                ChatColor.GREEN +
                " has been added to " +
                ChatColor.YELLOW +
                targetName +
                ChatColor.GREEN +
                "'s balance."
        );

        // Message to the target player if they are online
        if (target.isOnline() && target.getPlayer() != null) {
            target
                .getPlayer()
                .sendMessage(
                    ChatColor.GREEN +
                        "You received a deposit of " +
                        ChatColor.GOLD +
                        "$" +
                        formattedAmount +
                        ChatColor.GREEN +
                        " from an administrator."
                );
        }

        return true;
    }
}
