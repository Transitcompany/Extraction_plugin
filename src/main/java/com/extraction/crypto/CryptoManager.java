package com.extraction.crypto;

import com.extraction.ExtractionPlugin;
import com.extraction.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CryptoManager {
    
    private final ExtractionPlugin plugin;
    private final EconomyManager economyManager;
    private final Map<String, CryptoWallet> wallets = new HashMap<>();
    
    // Cryptocurrency data
    public static class CryptoCurrency {
        private final String name;
        private final String symbol;
        private final Material icon;
        private final ChatColor color;
        private final double buyPrice;  // Price in dollars
        private final double sellPrice; // Price in dollars
        
        public CryptoCurrency(String name, String symbol, Material icon, ChatColor color, double buyPrice, double sellPrice) {
            this.name = name;
            this.symbol = symbol;
            this.icon = icon;
            this.color = color;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }
        
        public String getName() { return name; }
        public String getSymbol() { return symbol; }
        public Material getIcon() { return icon; }
        public ChatColor getColor() { return color; }
        public double getBuyPrice() { return buyPrice; }
        public double getSellPrice() { return sellPrice; }
    }
    
    public static final Map<String, CryptoCurrency> CRYPTOCURRENCIES = new HashMap<>();
    
    static {
        CRYPTOCURRENCIES.put("BTC", new CryptoCurrency("BitBlock", "BTC", Material.GOLD_INGOT, ChatColor.GOLD, 45000.0, 44000.0));
        CRYPTOCURRENCIES.put("XRP", new CryptoCurrency("X-RP", "XRP", Material.LAPIS_LAZULI, ChatColor.BLUE, 25.0, 24.0));
        CRYPTOCURRENCIES.put("DOGE", new CryptoCurrency("DogeOre", "DOGE", Material.COAL, ChatColor.GRAY, 0.08, 0.07));
        CRYPTOCURRENCIES.put("XMR", new CryptoCurrency("MonOre", "XMR", Material.IRON_INGOT, ChatColor.DARK_GRAY, 150.0, 145.0));
        CRYPTOCURRENCIES.put("CREEPER", new CryptoCurrency("CreeperCoin", "CREEPER", Material.GREEN_DYE, ChatColor.GREEN, 500.0, 480.0));
    }
    
    public static class CryptoWallet {
        private final String walletId;
        private final Map<String, Double> balances = new HashMap<>();
        private final long createdTime;
        
        public CryptoWallet(String walletId) {
            this.walletId = walletId;
            this.createdTime = System.currentTimeMillis();
            // Initialize with 0 balance for all currencies
            for (String symbol : CRYPTOCURRENCIES.keySet()) {
                balances.put(symbol, 0.0);
            }
        }
        
        public String getWalletId() { return walletId; }
        public Map<String, Double> getBalances() { return balances; }
        public double getBalance(String symbol) { return balances.getOrDefault(symbol, 0.0); }
        public long getCreatedTime() { return createdTime; }
        
        public void setBalance(String symbol, double amount) {
            balances.put(symbol, Math.max(0.0, amount));
        }
        
        public void addBalance(String symbol, double amount) {
            double current = getBalance(symbol);
            setBalance(symbol, current + amount);
        }
        
        public boolean removeBalance(String symbol, double amount) {
            double current = getBalance(symbol);
            if (current >= amount) {
                setBalance(symbol, current - amount);
                return true;
            }
            return false;
        }
        
        public double getTotalValueUSD() {
            double total = 0.0;
            for (Map.Entry<String, Double> entry : balances.entrySet()) {
                CryptoCurrency crypto = CRYPTOCURRENCIES.get(entry.getKey());
                if (crypto != null) {
                    total += entry.getValue() * crypto.getSellPrice();
                }
            }
            return total;
        }
    }
    
    public CryptoManager(ExtractionPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }
    
    public CryptoWallet getWallet(String walletId) {
        return wallets.computeIfAbsent(walletId, CryptoWallet::new);
    }
    
    public String getWalletId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "wallet_id");
        return container.get(key, PersistentDataType.STRING);
    }
    
    public boolean isCryptoWallet(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "crypto_wallet");
        return container.has(key, PersistentDataType.BYTE);
    }
    
    public void openCryptoWallet(Player player, ItemStack walletItem) {
        String walletId = getWalletId(walletItem);
        if (walletId == null) {
            player.sendMessage(ChatColor.RED + "Invalid wallet item!");
            return;
        }
        
        CryptoWallet wallet = getWallet(walletId);
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.YELLOW + "" + ChatColor.BOLD + "Crypto Wallet - " + walletId);
        
        // Fill with glass panes
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, filler);
        }
        
        // Add crypto currency displays (slots 10-14)
        int slot = 10;
        for (Map.Entry<String, CryptoCurrency> entry : CRYPTOCURRENCIES.entrySet()) {
            String symbol = entry.getKey();
            CryptoCurrency crypto = entry.getValue();
            double balance = wallet.getBalance(symbol);
            
            ItemStack cryptoItem = new ItemStack(crypto.getIcon());
            ItemMeta cryptoMeta = cryptoItem.getItemMeta();
            if (cryptoMeta != null) {
                cryptoMeta.setDisplayName(crypto.getColor() + "" + ChatColor.BOLD + crypto.getName() + " (" + symbol + ")");
                
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add(ChatColor.GRAY + "Current Balance: " + crypto.getColor() + String.format("%.8f", balance) + " " + symbol);
                lore.add(ChatColor.GRAY + "USD Value: $" + String.format("%.2f", balance * crypto.getSellPrice()));
                lore.add("");
                lore.add(ChatColor.GREEN + "Buy Price: $" + String.format("%.2f", crypto.getBuyPrice()));
                lore.add(ChatColor.RED + "Sell Price: $" + String.format("%.2f", crypto.getSellPrice()));
                lore.add("");
                lore.add(ChatColor.YELLOW + "Left Click: Buy with $" + (int)crypto.getBuyPrice());
                lore.add(ChatColor.YELLOW + "Right Click: Sell for $" + (int)crypto.getSellPrice());
                
                cryptoMeta.setLore(lore);
                cryptoItem.setItemMeta(cryptoMeta);
            }
            
            gui.setItem(slot, cryptoItem);
            slot += 2; // Space between items
        }
        
        // Add wallet info (slot 31)
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Wallet Information");
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + "Wallet ID: " + ChatColor.WHITE + walletId);
            lore.add(ChatColor.GRAY + "Created: " + ChatColor.WHITE + new java.util.Date(wallet.getCreatedTime()).toString());
            lore.add("");
            lore.add(ChatColor.GOLD + "Total Portfolio Value:");
            lore.add(ChatColor.GREEN + "$" + String.format("%.2f", wallet.getTotalValueUSD()));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to withdraw all to balance");
            
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(31, infoItem);
        
        // Add balance display (slot 49)
        double playerBalance = economyManager.getBalanceAsDouble(player.getUniqueId());
        ItemStack balanceItem = new ItemStack(Material.EMERALD);
        ItemMeta balanceMeta = balanceItem.getItemMeta();
        if (balanceMeta != null) {
            balanceMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Your Balance");
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.WHITE + "$" + String.format("%,.2f", playerBalance));
            lore.add("");
            lore.add(ChatColor.GRAY + "Use this balance to buy crypto");
            
            balanceMeta.setLore(lore);
            balanceItem.setItemMeta(balanceMeta);
        }
        gui.setItem(49, balanceItem);
        
        player.openInventory(gui);
    }
    
    public boolean buyCrypto(Player player, String walletId, String symbol, double amount) {
        CryptoWallet wallet = getWallet(walletId);
        CryptoCurrency crypto = CRYPTOCURRENCIES.get(symbol);
        
        if (crypto == null) {
            player.sendMessage(ChatColor.RED + "Unknown cryptocurrency: " + symbol);
            return false;
        }
        
        double cost = amount * crypto.getBuyPrice();
        
        if (economyManager.takeBalance(player.getUniqueId(), cost)) {
            wallet.addBalance(symbol, amount);
            player.sendMessage(ChatColor.GREEN + "Bought " + String.format("%.8f", amount) + " " + symbol + " for $" + String.format("%.2f", cost));
            return true;
        } else {
            double balance = economyManager.getBalanceAsDouble(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Insufficient balance! Need $" + String.format("%.2f", cost) + " (you have $" + String.format("%.2f", balance) + ")");
            return false;
        }
    }
    
    public boolean sellCrypto(Player player, String walletId, String symbol, double amount) {
        CryptoWallet wallet = getWallet(walletId);
        CryptoCurrency crypto = CRYPTOCURRENCIES.get(symbol);
        
        if (crypto == null) {
            player.sendMessage(ChatColor.RED + "Unknown cryptocurrency: " + symbol);
            return false;
        }
        
        if (wallet.getBalance(symbol) < amount) {
            player.sendMessage(ChatColor.RED + "Insufficient " + symbol + " balance! You have " + String.format("%.8f", wallet.getBalance(symbol)));
            return false;
        }
        
        if (wallet.removeBalance(symbol, amount)) {
            double revenue = amount * crypto.getSellPrice();
            economyManager.addBalance(player.getUniqueId(), revenue);
            player.sendMessage(ChatColor.GREEN + "Sold " + String.format("%.8f", amount) + " " + symbol + " for $" + String.format("%.2f", revenue));
            return true;
        }
        return false;
    }
    
    public boolean withdrawAll(Player player, String walletId) {
        CryptoWallet wallet = getWallet(walletId);
        double totalValue = wallet.getTotalValueUSD();
        
        if (totalValue <= 0) {
            player.sendMessage(ChatColor.RED + "No crypto to withdraw!");
            return false;
        }
        
        // Sell all crypto
        for (Map.Entry<String, Double> entry : wallet.getBalances().entrySet()) {
            String symbol = entry.getKey();
            double balance = entry.getValue();
            if (balance > 0) {
                CryptoCurrency crypto = CRYPTOCURRENCIES.get(symbol);
                if (crypto != null) {
                    economyManager.addBalance(player.getUniqueId(), balance * crypto.getSellPrice());
                    wallet.setBalance(symbol, 0.0);
                }
            }
        }
        
        player.sendMessage(ChatColor.GREEN + "Withdrew all crypto for $" + String.format("%.2f", totalValue));
        return true;
    }
}