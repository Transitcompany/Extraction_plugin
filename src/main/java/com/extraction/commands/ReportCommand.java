package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.ReportManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final ReportManager reportManager;

    public ReportCommand(ExtractionPlugin plugin, ReportManager reportManager) {
        this.plugin = plugin;
        this.reportManager = reportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /report <player> <reason>");
            return true;
        }

        String reported = args[0];
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        if (reportManager.sendReport(player.getName(), reported, reason)) {
            player.sendMessage(ChatColor.GREEN + "Report sent successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to send report. Webhook may not be set.");
        }

        return true;
    }
}