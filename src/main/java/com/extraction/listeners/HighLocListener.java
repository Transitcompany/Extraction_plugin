package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.HighLocManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class HighLocListener implements Listener {

    private final ExtractionPlugin plugin;
    private final HighLocManager highLocManager;

    public HighLocListener(ExtractionPlugin plugin, HighLocManager highLocManager) {
        this.plugin = plugin;
        this.highLocManager = highLocManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (player.hasMetadata("highloc_type")) {
            event.setCancelled(true); // Prevent normal interaction
            String type = player.getMetadata("highloc_type").get(0).asString();
            player.removeMetadata("highloc_type", plugin);

            highLocManager.addHighLoc(event.getClickedBlock().getLocation(), type);
            player.sendMessage(ChatColor.GREEN + "High location set for " + type + "!");
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (plugin.getHighLocManager().isHighLocMob(event.getEntity())) {
            // Add some loot
            event.getDrops().add(new ItemStack(Material.IRON_INGOT, 1 + (int)(Math.random() * 3))); // 1-3 iron
            if (Math.random() < 0.5) {
                event.getDrops().add(new ItemStack(Material.GOLD_INGOT, 1));
            }
            if (Math.random() < 0.2) {
                event.getDrops().add(new ItemStack(Material.DIAMOND, 1));
            }
        }
    }
}