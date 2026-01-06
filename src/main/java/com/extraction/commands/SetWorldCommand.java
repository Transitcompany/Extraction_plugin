package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.extract.ExtractManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetWorldCommand implements CommandExecutor {
    private final ExtractionPlugin plugin;
    private final ExtractManager extractManager;
    public SetWorldCommand(ExtractionPlugin plugin, ExtractManager extractManager) {
        this.plugin = plugin;
        this.extractManager = extractManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("extraction.admin")) {
            sender.sendMessage("No permission.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage("Usage: /setworld <extract|lobby> <World>");
            return true;
        }
        String which = args[0];
        String world = args[1];
        extractManager.setWorld(which, world);
        sender.sendMessage("Set " + which + " world to " + world + ".");
        return true;
    }
}