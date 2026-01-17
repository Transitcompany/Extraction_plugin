package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.ServerMapManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetServerMapCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final ServerMapManager serverMapManager;

    public SetServerMapCommand(ExtractionPlugin plugin, ServerMapManager serverMapManager) {
        this.plugin = plugin;
        this.serverMapManager = serverMapManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /setservermap <url>");
            return true;
        }

        String url = args[0];

        // Basic validation
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            sender.sendMessage(ChatColor.RED + "Invalid URL. Must start with http:// or https://");
            return true;
        }

        serverMapManager.setMapUrl(url);
        sender.sendMessage(ChatColor.GREEN + "Server map URL set successfully!");
        return true;
    }
}