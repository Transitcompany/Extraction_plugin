package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.TemperatureManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempEventCommand implements CommandExecutor {
    private final ExtractionPlugin plugin;
    private final TemperatureManager temperatureManager;

    public TempEventCommand(ExtractionPlugin plugin, TemperatureManager temperatureManager) {
        this.plugin = plugin;
        this.temperatureManager = temperatureManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("extraction.admin")) {
            sender.sendMessage("No permission.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage("Usage: /tempevent <heatwave|cold|end>");
            return true;
        }
        String event = args[0].toLowerCase();
        if (event.equals("heatwave")) {
            temperatureManager.startHeatwave();
        } else if (event.equals("cold")) {
            temperatureManager.startColdwave();
        } else if (event.equals("end")) {
            temperatureManager.endEvents();
        } else {
            sender.sendMessage("Invalid event.");
        }
        return true;
    }
}