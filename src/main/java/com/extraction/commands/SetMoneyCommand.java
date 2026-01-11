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

public class SetMoneyCommand implements CommandExecutor {

    private final EconomyManager economyManager;
    // Define a permission constant for clarity and easy reference
    private static final String REQUIRED_PERMISSION =
        "extraction.admin.givemoney";

    public SetMoneyCommand(EconomyManager economyManager) {
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

        // --- 3. Amount Parsing ---
        String amountStr;
        String input = args[1].toLowerCase().trim();
        if (input.equals("inf") || input.equals("infinity")) {
            amountStr = "∞";
        } else {
            // Allow any text
            amountStr = args[1];
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

        // --- 5. Economy Interaction ---

        try {
            economyManager.setBalance(targetUUID, amountStr);
        } catch (Exception e) {
            // Catch any unexpected exceptions from the economy manager (e.g., DB error)
            sender.sendMessage(
                ChatColor.RED +
                    "CRITICAL ERROR: Failed to set balance due to an internal issue. Check console."
            );
            e.printStackTrace();
            return true;
        }

        // --- 6. Success Messages ---

        String displayAmount = amountStr;

        // Success message to the sender (e.g., console/admin)
        sender.sendMessage(
            ChatColor.GREEN +
                "Success! " +
                ChatColor.GOLD +
                displayAmount +
                ChatColor.GREEN +
                " has been set as " +
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
                        "Your balance has been set to " +
                        ChatColor.GOLD +
                        displayAmount +
                        ChatColor.GREEN +
                        " by an administrator."
                );
        }

        return true;
    }
}
