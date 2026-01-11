package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.data.PlayerDataManager;
import com.extraction.economy.EconomyManager;
import com.extraction.stash.StashManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WipeCommand implements CommandExecutor, TabCompleter {
    private final ExtractionPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final EconomyManager economyManager;
    private final StashManager stashManager;

    public WipeCommand(ExtractionPlugin plugin, PlayerDataManager playerDataManager, 
                      EconomyManager economyManager, StashManager stashManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.economyManager = economyManager;
        this.stashManager = stashManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("extraction.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /wipe <player>");
            return true;
        }

        String playerName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cPlayer '" + playerName + "' has never played on this server.");
            return true;
        }

        UUID uuid = target.getUniqueId();
        
        sender.sendMessage("§eWiping data for player: §a" + target.getName());
        
        economyManager.setBalance(uuid, "0");
        playerDataManager.wipePlayerData(uuid);
        
        File stashFile = new File(new File(plugin.getDataFolder(), "stashes"), uuid + ".yml");
        if (stashFile.exists()) {
            stashFile.delete();
        }
        
        if (target.isOnline()) {
            Player onlinePlayer = target.getPlayer();
            if (onlinePlayer != null) {
                stashManager.getStash(onlinePlayer).clear();
                onlinePlayer.sendMessage("§c§lYour data has been wiped by an administrator!");
            }
        }
        
        sender.sendMessage("§aSuccessfully wiped all data for: §e" + target.getName());
        plugin.getLogger().info("Administrator " + sender.getName() + " wiped data for player " + target.getName());
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("extraction.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}