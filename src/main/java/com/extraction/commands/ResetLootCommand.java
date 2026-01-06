package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.loot.LootContainerManager;
import org.bukkit.Bukkit; // Import Bukkit to access all players
import org.bukkit.ChatColor; // Import ChatColor for red messaging
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ResetLootCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final LootContainerManager lootContainerManager;

    public ResetLootCommand(
        ExtractionPlugin plugin,
        LootContainerManager lootContainerManager
    ) {
        this.plugin = plugin;
        this.lootContainerManager = lootContainerManager;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!sender.hasPermission("extraction.admin")) {
            sender.sendMessage(
                ChatColor.RED + "You do not have permission to reset loot."
            );
            return true;
        }

        int containerCount = lootContainerManager.getAllLocations().size();

        // 1. Send a global warning message in red
        String broadcastMessage =
            ChatColor.DARK_RED.toString() +
            ChatColor.BOLD +
            "[EXTRACTION SYSTEM] " +
            ChatColor.RED +
            "Initiating Global Loot Reset! " +
            ChatColor.YELLOW +
            "(Relooting " +
            containerCount +
            " containers)";

        String freezeWarning =
            ChatColor.RED +
            "WARNING: The server MAY experience a minor freeze or lag spike during this process.";

        Bukkit.broadcastMessage(broadcastMessage);
        Bukkit.broadcastMessage(freezeWarning);

        // 2. Execute the restock operation
        lootContainerManager.restockAll();

        // 3. Send confirmation to the command sender
        sender.sendMessage(
            ChatColor.GREEN +
                "Successfully scheduled restock for all " +
                containerCount +
                " loot containers."
        );
        return true;
    }
}
