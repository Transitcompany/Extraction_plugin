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

public class GiveItemCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;

    public GiveItemCommand(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("extraction.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /giveitem <player> <gps|medkit|cyanide_pill> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        String itemType = args[1].toLowerCase();
        if (!itemType.equals("gps") && !itemType.equals("medkit") && !itemType.equals("cyanide_pill")) {
            sender.sendMessage(ChatColor.RED + "Invalid item type! Use 'gps', 'medkit', or 'cyanide_pill'");
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0 || amount > 64) {
                    sender.sendMessage(ChatColor.RED + "Amount must be between 1 and 64.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[2]);
                return true;
            }
        }

        // Create items one by one since they're unstackable
        for (int i = 0; i < amount; i++) {
            ItemStack item = createItem(itemType);
            target.getInventory().addItem(item);
        }

        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " " + itemType.toUpperCase() + " to " + target.getName());
        target.sendMessage(ChatColor.AQUA + "You received " + amount + " " + itemType.toUpperCase() + "!");

        return true;
    }

    private ItemStack createItem(String itemType) {
        Material material;
        String displayName;
        String loreText;
        String keyName;
        int value;

        if (itemType.equals("gps")) {
            material = Material.TRIAL_KEY;
            displayName = ChatColor.AQUA + "" + ChatColor.BOLD + "GPS";
            loreText = "Left or right-click to see your current location";
            keyName = "gps_trail_key";
            value = 150;
        } else if (itemType.equals("medkit")) {
            material = Material.OMINOUS_TRIAL_KEY;
            displayName = ChatColor.RED + "" + ChatColor.BOLD + "Medkit";
            loreText = "Left or right-click to heal yourself";
            keyName = "medkit";
            value = 200;
        } else { // cyanide_pill
            material = Material.BLAZE_ROD;
            displayName = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Cyanide Pill";
            loreText = "Right-click to consume this deadly poison";
            keyName = "cyanide_pill";
            value = 0;
        }

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "A " + (itemType.equals("gps") ? "device that reveals your coordinates" : itemType.equals("medkit") ? "device that heals your wounds" : "deadly poison that will kill you"),
                ChatColor.GRAY + loreText,
                "",
                ChatColor.YELLOW + "Value: $" + value
            ));

            // Set custom key to identify this item
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, keyName);
            container.set(key, PersistentDataType.BYTE, (byte) 1);

            // Set initial uses for medkit
            if (itemType.equals("medkit")) {
                NamespacedKey usesKey = new NamespacedKey(plugin, "medkit_uses");
                container.set(usesKey, PersistentDataType.INTEGER, 3);

                // Update lore to show uses
                java.util.List<String> lore = meta.getLore();
                if (lore != null && lore.size() >= 3) {
                    lore.add(ChatColor.GRAY + "Uses remaining: 3/3");
                    meta.setLore(lore);
                }
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}