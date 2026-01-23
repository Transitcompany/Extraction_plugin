package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GiveGpsCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;

    public GiveGpsCommand(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("extraction.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /givegps <player> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0 || amount > 64) {
                    sender.sendMessage(ChatColor.RED + "Amount must be between 1 and 64.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[1]);
                return true;
            }
        }

        // Create GPS item
        ItemStack gpsKey = new ItemStack(Material.TRIAL_KEY, amount);
        ItemMeta meta = gpsKey.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "GPS");
            meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "A device that reveals your coordinates",
                ChatColor.GRAY + "Left or right-click to see your current location",
                "",
                ChatColor.YELLOW + "Value: $150"
            ));
            
            // Set custom key to identify this item
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "gps_trail_key");
            container.set(key, PersistentDataType.BYTE, (byte) 1);

            gpsKey.setItemMeta(meta);
        }

        // Give item to player
        target.getInventory().addItem(gpsKey);
        
        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " GPS to " + target.getName());
        target.sendMessage(ChatColor.AQUA + "You received " + amount + " GPS!");
        
        return true;
    }
}