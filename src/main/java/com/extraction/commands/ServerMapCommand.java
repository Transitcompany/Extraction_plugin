package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.ServerMapManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServerMapCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final ServerMapManager serverMapManager;

    public ServerMapCommand(ExtractionPlugin plugin, ServerMapManager serverMapManager) {
        this.plugin = plugin;
        this.serverMapManager = serverMapManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        String mapUrl = serverMapManager.getMapUrl();
        if (mapUrl.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Server map URL is not set.");
            return true;
        }

        // Create clickable message
        TextComponent message = new TextComponent(ChatColor.GREEN + "Click here to open the server map!");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, mapUrl));
        player.spigot().sendMessage(message);

        return true;
    }
}