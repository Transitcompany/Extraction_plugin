package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AtmCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;

    public AtmCommand(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can access the ATM.");
            return true;
        }

        Player player = (Player) sender;

        // Send clickable link to ATM website
        player.sendMessage("§8§l[§cATM§8§l]§7 Access your account online:");

        net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent("§b§l§nVISIT ATM.XORG.DEV");
        message.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, "http://atm.xorg.dev"));
        player.spigot().sendMessage(message);

        return true;
    }

}
