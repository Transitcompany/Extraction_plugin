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
import org.bukkit.event.player.PlayerInteractEvent;

public class ExtractPointCommand implements CommandExecutor, Listener {
    private final ExtractionPlugin plugin;
    private final ExtractManager extractManager;
    public ExtractPointCommand(ExtractionPlugin plugin, ExtractManager extractManager) {
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
        player.sendMessage("Right-click a banner to set it as an extraction point!");
        player.setMetadata("setting_expoint", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        return true;
    }
    @EventHandler
    public void onExtractSetup(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasMetadata("setting_expoint")) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() == null) return;
        if (block.getType().name().endsWith("_BANNER")) {
            extractManager.registerExtractPoint(block.getLocation());
            player.sendMessage("Extraction point set!");
            player.removeMetadata("setting_expoint", plugin);
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBannerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || !(block.getType().name().endsWith("_BANNER"))) return;
        Player player = event.getPlayer();
        if (extractManager.isExtractPoint(block.getLocation())) {
            extractManager.initiateExtraction(player, block.getLocation());
        }
    }
}
