package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.HighLocManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RespawnHighLocCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final HighLocManager highLocManager;

    public RespawnHighLocCommand(ExtractionPlugin plugin, HighLocManager highLocManager) {
        this.plugin = plugin;
        this.highLocManager = highLocManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Respawn all high locs
        plugin.respawnHighLocs();
        sender.sendMessage(ChatColor.GREEN + "Respawning all high location mobs!");
        return true;
    }
}