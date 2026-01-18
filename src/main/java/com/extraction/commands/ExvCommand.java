package com.extraction.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ExvCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "=== Extraction Plugin v1.0 ===");
        sender.sendMessage(ChatColor.YELLOW + "Latest Updates:");
        sender.sendMessage(ChatColor.WHITE + "- Added /pay command with shorthand amounts (e.g., 1k, 10b)");
        sender.sendMessage(ChatColor.WHITE + "- Enhanced proximity chat: 50-block range, feedback in chat and actionbar");
        sender.sendMessage(ChatColor.WHITE + "- Fixed plugin.yml YAML errors for proper loading");
        sender.sendMessage(ChatColor.WHITE + "- Removed governance system (voting on rules)");
        sender.sendMessage(ChatColor.WHITE + "- Disabled /msg and /tell commands to enforce proximity chat only");
        sender.sendMessage(ChatColor.GREEN + "Plugin is up to date!");
        return true;
    }
}