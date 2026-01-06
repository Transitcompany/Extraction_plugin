package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.crypto.CryptoManager;
import com.extraction.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CryptoWalletListener implements Listener {
    
    private final ExtractionPlugin plugin;
    private final CryptoManager cryptoManager;
    private final EconomyManager economyManager;
    
    public CryptoWalletListener(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.cryptoManager = plugin.getCryptoManager();
        this.economyManager = plugin.getEconomyManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.contains("Crypto Wallet")) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Check if clicking on crypto currency (slots 10, 12, 14, 16, 18)
        int slot = event.getSlot();
        if (slot >= 10 && slot <= 18 && slot % 2 == 0) {
            String symbol = getCryptoSymbolFromSlot(slot);
            if (symbol != null) {
                if (event.isLeftClick()) {
                    // Buy crypto
                    buyCrypto(player, symbol);
                } else if (event.isRightClick()) {
                    // Sell crypto
                    sellCrypto(player, symbol);
                }
            }
        }
        
        // Check if clicking on wallet info (slot 31) - withdraw all
        if (slot == 31) {
            // Get wallet from player's held item or main hand
            ItemStack walletItem = player.getInventory().getItemInMainHand();
            if (cryptoManager.isCryptoWallet(walletItem)) {
                String walletId = cryptoManager.getWalletId(walletItem);
                if (walletId != null) {
                    cryptoManager.withdrawAll(player, walletId);
                    // Reopen wallet to refresh display
                    cryptoManager.openCryptoWallet(player, walletItem);
                }
            }
        }
    }
    
    private String getCryptoSymbolFromSlot(int slot) {
        switch (slot) {
            case 10: return "BTC";
            case 12: return "XRP";
            case 14: return "DOGE";
            case 16: return "XMR";
            case 18: return "CREEPER";
            default: return null;
        }
    }
    
    private void buyCrypto(Player player, String symbol) {
        // Get wallet from player's held item
        ItemStack walletItem = player.getInventory().getItemInMainHand();
        if (!cryptoManager.isCryptoWallet(walletItem)) {
            player.sendMessage(ChatColor.RED + "Please hold the crypto wallet in your main hand!");
            return;
        }
        
        String walletId = cryptoManager.getWalletId(walletItem);
        if (walletId == null) {
            player.sendMessage(ChatColor.RED + "Invalid wallet!");
            return;
        }
        
        CryptoManager.CryptoCurrency crypto = CryptoManager.CRYPTOCURRENCIES.get(symbol);
        if (crypto == null) {
            player.sendMessage(ChatColor.RED + "Unknown cryptocurrency!");
            return;
        }
        
        // Buy 1 unit of crypto
        double amount = 1.0;
        if (cryptoManager.buyCrypto(player, walletId, symbol, amount)) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            // Reopen wallet to refresh display
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                cryptoManager.openCryptoWallet(player, walletItem);
            }, 1L);
        }
    }
    
    private void sellCrypto(Player player, String symbol) {
        // Get wallet from player's held item
        ItemStack walletItem = player.getInventory().getItemInMainHand();
        if (!cryptoManager.isCryptoWallet(walletItem)) {
            player.sendMessage(ChatColor.RED + "Please hold the crypto wallet in your main hand!");
            return;
        }
        
        String walletId = cryptoManager.getWalletId(walletItem);
        if (walletId == null) {
            player.sendMessage(ChatColor.RED + "Invalid wallet!");
            return;
        }
        
        CryptoManager.CryptoCurrency crypto = CryptoManager.CRYPTOCURRENCIES.get(symbol);
        if (crypto == null) {
            player.sendMessage(ChatColor.RED + "Unknown cryptocurrency!");
            return;
        }
        
        // Sell 1 unit of crypto
        double amount = 1.0;
        if (cryptoManager.sellCrypto(player, walletId, symbol, amount)) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            // Reopen wallet to refresh display
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                cryptoManager.openCryptoWallet(player, walletItem);
            }, 1L);
        }
    }
}