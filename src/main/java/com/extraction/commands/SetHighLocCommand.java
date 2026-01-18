package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.HighLocManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class SetHighLocCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final HighLocManager highLocManager;

    public SetHighLocCommand(ExtractionPlugin plugin, HighLocManager highLocManager) {
        this.plugin = plugin;
        this.highLocManager = highLocManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /sethighloc <skeletons|zombies>");
            return true;
        }

        String type = args[0].toLowerCase();
        if (!type.equals("skeletons") && !type.equals("zombies")) {
            player.sendMessage(ChatColor.RED + "Type must be 'skeletons' or 'zombies'.");
            return true;
        }

        // Store the type for the next interaction
        // Since no persistent storage per player, perhaps use a map in the manager or player metadata.

        // For simplicity, set a temporary type for the player.
        player.setMetadata("highloc_type", new org.bukkit.metadata.FixedMetadataValue(plugin, type));
        player.sendMessage(ChatColor.GREEN + "Click on a block to set a high location for " + type + ".");

        return true;
    }

    // Note: The actual setting will be handled in a listener for PlayerInteractEvent
}