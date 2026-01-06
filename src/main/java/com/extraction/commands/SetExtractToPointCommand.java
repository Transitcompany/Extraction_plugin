package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.extract.ExtractManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class SetExtractToPointCommand implements CommandExecutor, Listener {
    private final ExtractionPlugin plugin;
    private final ExtractManager extractManager;
    
    public SetExtractToPointCommand(ExtractionPlugin plugin, ExtractManager extractManager) {
        this.plugin = plugin;
        this.extractManager = extractManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player) sender;
        
        if (extractManager.getExtractToPointCount() >= 30) {
            player.sendMessage("Maximum of 30 extract-to points reached!");
            return true;
        }
        
        player.sendMessage("Place a block to set it as an extract-to point!");
        player.sendMessage("Current points: " + extractManager.getExtractToPointCount() + "/30");
        player.setMetadata("setting_extractto", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        return true;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.hasMetadata("setting_extractto")) return;
        
        Block block = event.getBlockPlaced();
        extractManager.registerExtractToPoint(block.getLocation());
        player.sendMessage("Extract-to point set! (" + extractManager.getExtractToPointCount() + "/30)");
        player.removeMetadata("setting_extractto", plugin);
    }
}



