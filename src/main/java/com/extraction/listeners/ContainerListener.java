package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.loot.LootContainerManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ContainerListener implements Listener {
    private final ExtractionPlugin plugin;
    private final LootContainerManager lootContainerManager;
    public ContainerListener(ExtractionPlugin plugin, LootContainerManager lootContainerManager) {
        this.plugin = plugin;
        this.lootContainerManager = lootContainerManager;
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (lootContainerManager.matchesProtected(block.getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("This loot container cannot be broken.");
        }
    }
}



