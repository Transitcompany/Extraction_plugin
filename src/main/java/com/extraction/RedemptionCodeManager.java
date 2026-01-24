package com.extraction;

import org.bukkit.entity.Player;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

public class RedemptionCodeManager {
    
    private final ExtractionPlugin plugin;
    private final String secret = "extractionSecret456"; // Server-side secret
    private final Set<String> usedCodes = new HashSet<>();
    private final double UNIT_MULTIPLIER = 1000.0; // Amount multiplier for encoding
    
    public RedemptionCodeManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }
    
    // Generate a redemption code
    public String generateCode(String action, double actualAmount) {
        if (!action.equals("D") && !action.equals("W")) {
            throw new IllegalArgumentException("Action must be 'D' or 'W'");
        }
        
        long scaledAmount = Math.round(actualAmount * UNIT_MULTIPLIER);
        String amountStr = Long.toString(scaledAmount, 36).toUpperCase();
        String data = secret + action + scaledAmount;
        String checksum = hash(data).substring(0, 4).toUpperCase();
        
        return action + amountStr + checksum;
    }
    
    // Redeem a code for a player
    public boolean redeemCode(Player player, String code) {
        if (code.length() < 6) return false; // Minimum length check
        
        try {
            String action = code.substring(0, 1);
            String checksum = code.substring(code.length() - 4);
            String amountStr = code.substring(1, code.length() - 4);
            
            long scaledAmount = Long.parseLong(amountStr, 36);
            double actualAmount = scaledAmount / UNIT_MULTIPLIER;
            
            String data = secret + action + scaledAmount;
            String expectedChecksum = hash(data).substring(0, 4).toUpperCase();
            
            if (!checksum.equals(expectedChecksum)) return false;
            if (usedCodes.contains(code)) return false;
            
            // Mark as used
            usedCodes.add(code);
            
            // Process the transaction
            if (action.equals("D")) { // Deposit
                plugin.getEconomyManager().addBalance(player.getUniqueId(), actualAmount);
                player.sendMessage("§aDeposited $" + String.format("%.2f", actualAmount) + " to your account!");
            } else if (action.equals("W")) { // Withdraw
                if (plugin.getEconomyManager().getBalanceAsDouble(player.getUniqueId()) >= actualAmount) {
                    plugin.getEconomyManager().takeBalance(player.getUniqueId(), actualAmount);
                    player.sendMessage("§aWithdrew $" + String.format("%.2f", actualAmount) + " from your account!");
                } else {
                    usedCodes.remove(code); // Revert if insufficient funds
                    player.sendMessage("§cInsufficient funds for withdrawal!");
                    return false;
                }
            } else {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}