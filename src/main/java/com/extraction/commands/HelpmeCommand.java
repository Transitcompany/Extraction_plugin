package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.TutorialManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpmeCommand implements CommandExecutor {

    private final TutorialManager tutorialManager;

    public HelpmeCommand(TutorialManager tutorialManager) {
        this.tutorialManager = tutorialManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player) sender;
        tutorialManager.startTutorial(player);
        return true;
    }
}