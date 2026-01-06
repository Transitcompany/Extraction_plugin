package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtractGiveCommand implements CommandExecutor, TabCompleter {

    private final ExtractionPlugin plugin;
    private final ShopManager shopManager;
    private final List<String> itemTypes = Arrays.asList("banner", "flare", "teleporter");

    public ExtractGiveCommand(ExtractionPlugin plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("extraction.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /extractgive <player> <item> [amount]");
            sender.sendMessage(ChatColor.GRAY + "Available items:");
            sender.sendMessage(ChatColor.GREEN + "  banner " + ChatColor.GRAY + "- Easy Extraction Banner ($100k value)");
            sender.sendMessage(ChatColor.GREEN + "  flare " + ChatColor.GRAY + "- Extraction Flare ($50k value, faster extraction)");
            sender.sendMessage(ChatColor.GREEN + "  teleporter " + ChatColor.GRAY + "- Emergency Teleporter ($250k value, instant extraction)");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        String itemType = args[1].toLowerCase();
        int amount = 1;

        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) amount = 1;
                if (amount > 64) amount = 64;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[2]);
                return true;
            }
        }

        ItemStack item = null;
        String itemName = "";

        switch (itemType) {
            case "banner":
                item = shopManager.createExtractionBanner();
                itemName = "Easy Extraction Banner";
                break;
            case "flare":
                item = shopManager.createExtractionFlare();
                itemName = "Extraction Flare";
                break;
            case "teleporter":
                item = shopManager.createEmergencyTeleporter();
                itemName = "Emergency Teleporter";
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown item type: " + itemType);
                sender.sendMessage(ChatColor.GRAY + "Available: banner, flare, teleporter");
                return true;
        }

        if (item != null) {
            item.setAmount(amount);
            target.getInventory().addItem(item);

            sender.sendMessage(ChatColor.GREEN + "Gave " + amount + "x " + itemName + " to " + target.getName());
            target.sendMessage(ChatColor.GREEN + "You received " + amount + "x " + ChatColor.GOLD + itemName + ChatColor.GREEN + "!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partial)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            String partial = args[1].toLowerCase();
            for (String type : itemTypes) {
                if (type.startsWith(partial)) {
                    completions.add(type);
                }
            }
        } else if (args.length == 3) {
            completions.addAll(Arrays.asList("1", "4", "8", "16", "32", "64"));
        }

        return completions;
    }
}
