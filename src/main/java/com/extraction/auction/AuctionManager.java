package com.extraction.auction;

import com.extraction.ExtractionPlugin;
import com.extraction.economy.EconomyManager;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class AuctionManager {

    private final ExtractionPlugin plugin;
    private final EconomyManager economyManager;
    private final Map<UUID, AuctionListing> listings =
        new ConcurrentHashMap<>();
    private File auctionFile;
    private YamlConfiguration auctionConfig;

    // 24 hours in milliseconds
    private static final long LISTING_DURATION = 24 * 60 * 60 * 1000;

    public AuctionManager(
        ExtractionPlugin plugin,
        EconomyManager economyManager
    ) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        loadListings();
        startExpiryChecker();
    }

    private void loadListings() {
        auctionFile = new File(plugin.getDataFolder(), "auctions.yml");
        if (!auctionFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                auctionFile.createNewFile();
            } catch (IOException ignored) {}
        }
        auctionConfig = YamlConfiguration.loadConfiguration(auctionFile);

        if (auctionConfig.getConfigurationSection("listings") != null) {
            for (String key : auctionConfig
                .getConfigurationSection("listings")
                .getKeys(false)) {
                try {
                    UUID listingId = UUID.fromString(key);
                    String path = "listings." + key;

                    UUID sellerId = UUID.fromString(
                        auctionConfig.getString(path + ".seller")
                    );
                    String sellerName = auctionConfig.getString(
                        path + ".sellerName"
                    );
                    ItemStack item = auctionConfig.getItemStack(path + ".item");
                    double price = auctionConfig.getDouble(path + ".price");
                    long listedAt = auctionConfig.getLong(path + ".listedAt");

                    if (item != null) {
                        AuctionListing listing = new AuctionListing(
                            listingId,
                            sellerId,
                            sellerName,
                            item,
                            price,
                            listedAt
                        );
                        listings.put(listingId, listing);
                    }
                } catch (Exception e) {
                    plugin
                        .getLogger()
                        .warning("Failed to load auction listing: " + key);
                }
            }
        }
        plugin
            .getLogger()
            .info("Loaded " + listings.size() + " auction listings.");
    }

    public void saveListings() {
        auctionConfig.set("listings", null);

        for (Map.Entry<UUID, AuctionListing> entry : listings.entrySet()) {
            String path = "listings." + entry.getKey().toString();
            AuctionListing listing = entry.getValue();

            auctionConfig.set(
                path + ".seller",
                listing.getSellerId().toString()
            );
            auctionConfig.set(path + ".sellerName", listing.getSellerName());
            auctionConfig.set(path + ".item", listing.getItem());
            auctionConfig.set(path + ".price", listing.getPrice());
            auctionConfig.set(path + ".listedAt", listing.getListedAt());
        }

        try {
            auctionConfig.save(auctionFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save auction listings!");
        }
    }

    private void startExpiryChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkExpiredListings();
            }
        }
            .runTaskTimer(plugin, 20L * 60, 20L * 60); // Check every minute
    }

    private void checkExpiredListings() {
        long currentTime = System.currentTimeMillis();
        List<UUID> expiredIds = new ArrayList<>();

        for (Map.Entry<UUID, AuctionListing> entry : listings.entrySet()) {
            AuctionListing listing = entry.getValue();
            if (currentTime - listing.getListedAt() > LISTING_DURATION) {
                expiredIds.add(entry.getKey());
            }
        }

        for (UUID id : expiredIds) {
            AuctionListing listing = listings.remove(id);
            if (listing != null) {
                // Return item to seller if online, otherwise store for later
                Player seller = Bukkit.getPlayer(listing.getSellerId());
                if (seller != null && seller.isOnline()) {
                    seller.getInventory().addItem(listing.getItem());
                    seller.sendMessage(
                        ChatColor.YELLOW +
                            "Your auction listing expired! Item returned to your inventory."
                    );
                } else {
                    // Store expired item for later retrieval
                    storeExpiredItem(listing);
                }
            }
        }

        if (!expiredIds.isEmpty()) {
            saveListings();
        }
    }

    private void storeExpiredItem(AuctionListing listing) {
        String path =
            "expired." +
            listing.getSellerId().toString() +
            "." +
            UUID.randomUUID().toString();
        auctionConfig.set(path + ".item", listing.getItem());
        auctionConfig.set(path + ".expiredAt", System.currentTimeMillis());
        try {
            auctionConfig.save(auctionFile);
        } catch (IOException ignored) {}
    }

    public List<ItemStack> getExpiredItems(UUID playerId) {
        List<ItemStack> items = new ArrayList<>();
        String path = "expired." + playerId.toString();

        if (auctionConfig.getConfigurationSection(path) != null) {
            for (String key : auctionConfig
                .getConfigurationSection(path)
                .getKeys(false)) {
                ItemStack item = auctionConfig.getItemStack(
                    path + "." + key + ".item"
                );
                if (item != null) {
                    items.add(item);
                }
            }
            // Clear after retrieval
            auctionConfig.set(path, null);
            try {
                auctionConfig.save(auctionFile);
            } catch (IOException ignored) {}
        }

        return items;
    }

    public boolean createListing(Player seller, ItemStack item, double price) {
        if (item == null || price <= 0) {
            return false;
        }

        UUID listingId = UUID.randomUUID();
        AuctionListing listing = new AuctionListing(
            listingId,
            seller.getUniqueId(),
            seller.getName(),
            item.clone(),
            price,
            System.currentTimeMillis()
        );

        listings.put(listingId, listing);
        saveListings();
        return true;
    }

    public boolean cancelListing(UUID listingId, Player player) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return false;
        }

        // Only seller or admin can cancel
        if (
            !listing.getSellerId().equals(player.getUniqueId()) &&
            !player.hasPermission("extraction.admin")
        ) {
            return false;
        }

        listings.remove(listingId);
        player.getInventory().addItem(listing.getItem());
        saveListings();
        return true;
    }

    public boolean purchaseListing(UUID listingId, Player buyer) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            buyer.sendMessage(ChatColor.RED + "This listing no longer exists!");
            return false;
        }

        // Can't buy your own listing
        if (listing.getSellerId().equals(buyer.getUniqueId())) {
            buyer.sendMessage(
                ChatColor.RED + "You can't buy your own listing!"
            );
            return false;
        }

        double price = listing.getPrice();

        // Check if buyer has enough money
        if (!economyManager.takeBalance(buyer.getUniqueId(), price)) {
            buyer.sendMessage(
                ChatColor.RED +
                    "You don't have enough money! Need $" +
                    String.format("%.2f", price)
            );
            return false;
        }

        // Give money to seller (90% - 10% tax)
        double sellerAmount = price * 0.90;
        economyManager.addBalance(listing.getSellerId(), sellerAmount);

        // Give item to buyer
        buyer.getInventory().addItem(listing.getItem());

        // Notify seller if online
        Player seller = Bukkit.getPlayer(listing.getSellerId());
        if (seller != null && seller.isOnline()) {
            seller.sendMessage(
                ChatColor.GREEN +
                    buyer.getName() +
                    " bought your " +
                    getItemName(listing.getItem()) +
                    " for $" +
                    String.format("%.2f", price) +
                    "! (You received $" +
                    String.format("%.2f", sellerAmount) +
                    " after 10% tax)"
            );
        }

        // Remove listing
        listings.remove(listingId);
        saveListings();

        return true;
    }

    public List<AuctionListing> getAllListings() {
        return new ArrayList<>(listings.values());
    }

    public List<AuctionListing> getPlayerListings(UUID playerId) {
        List<AuctionListing> playerListings = new ArrayList<>();
        for (AuctionListing listing : listings.values()) {
            if (listing.getSellerId().equals(playerId)) {
                playerListings.add(listing);
            }
        }
        return playerListings;
    }

    public AuctionListing getListing(UUID listingId) {
        return listings.get(listingId);
    }

    public long getTimeRemaining(AuctionListing listing) {
        long elapsed = System.currentTimeMillis() - listing.getListedAt();
        return Math.max(0, LISTING_DURATION - elapsed);
    }

    public String formatTimeRemaining(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ").toLowerCase();
    }

    public int getListingCount() {
        return listings.size();
    }

    // Inner class for auction listings
    public static class AuctionListing {

        private final UUID listingId;
        private final UUID sellerId;
        private final String sellerName;
        private final ItemStack item;
        private final double price;
        private final long listedAt;

        public AuctionListing(
            UUID listingId,
            UUID sellerId,
            String sellerName,
            ItemStack item,
            double price,
            long listedAt
        ) {
            this.listingId = listingId;
            this.sellerId = sellerId;
            this.sellerName = sellerName;
            this.item = item;
            this.price = price;
            this.listedAt = listedAt;
        }

        public UUID getListingId() {
            return listingId;
        }

        public UUID getSellerId() {
            return sellerId;
        }

        public String getSellerName() {
            return sellerName;
        }

        public ItemStack getItem() {
            return item;
        }

        public double getPrice() {
            return price;
        }

        public long getListedAt() {
            return listedAt;
        }
    }
}
