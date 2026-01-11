package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.TradeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class TradeListener implements Listener {
    private final ExtractionPlugin plugin;
    private final TradeManager tradeManager;

    public TradeListener(ExtractionPlugin plugin, TradeManager tradeManager) {
        this.plugin = plugin;
        this.tradeManager = tradeManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();
        if (title.equals("Trade Amount")) {
            event.setCancelled(true);
            tradeManager.handleSenderClick(player, event.getSlot(), inv);
        }
    }
}