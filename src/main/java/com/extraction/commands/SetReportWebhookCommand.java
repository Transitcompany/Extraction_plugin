package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.ReportManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetReportWebhookCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final ReportManager reportManager;

    public SetReportWebhookCommand(ExtractionPlugin plugin, ReportManager reportManager) {
        this.plugin = plugin;
        this.reportManager = reportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /setreporthook <webhook_url>");
            return true;
        }

        String webhookUrl = args[0];

        // Basic validation
        if (!webhookUrl.startsWith("https://")) {
            sender.sendMessage(ChatColor.RED + "Invalid webhook URL. Must start with https://");
            return true;
        }

        reportManager.setWebhookUrl(webhookUrl);
        sender.sendMessage(ChatColor.GREEN + "Report webhook set successfully!");
        return true;
    }
}