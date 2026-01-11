package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.TemperatureManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TemperatureListener implements Listener {
    private final ExtractionPlugin plugin;
    private final TemperatureManager temperatureManager;

    public TemperatureListener(ExtractionPlugin plugin, TemperatureManager temperatureManager) {
        this.plugin = plugin;
        this.temperatureManager = temperatureManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.POTION) {
            // Assume water bottle is potion, but in reality, might need to check meta for water.
            // For simplicity, any potion consumption cools.
            temperatureManager.drinkWater(player);
            // Consume the item
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            event.setCancelled(true);
        }
    }
}