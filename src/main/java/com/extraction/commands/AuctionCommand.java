package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.auction.AuctionManager;
import com.extraction.auction.AuctionManager.AuctionListing;
import com.extraction.economy.EconomyManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AuctionCommand implements CommandExecutor, Listener {

    private final ExtractionPlugin plugin;
    private final AuctionManager auctionManager;
    private final EconomyManager economyManager;

    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, AuctionMode> playerModes = new HashMap<>();
    private final Map<UUID, ItemStack> pendingSellItems = new HashMap<>();
    private final Map<UUID, Double> pendingSellPrices = new HashMap<>();

    private static final int ITEMS_PER_PAGE = 36;

    private enum AuctionMode {
        BROWSE,
        MY_LISTINGS,
        SELL_SELECT,
        SELL_CONFIRM
    }

    public AuctionCommand(ExtractionPlugin plugin, AuctionManager auctionManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.auctionManager = auctionManager;
        this.economyManager = economyManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "sell":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /auction sell <price>");
                    player.sendMessage(ChatColor.GRAY + "Hold the item you want to sell in your hand.");
                    return true;
                }
                try {
                    double price = Double.parseDouble(args[1]);
                    if (price <= 0) {
                        player.sendMessage(ChatColor.RED + "Price must be greater than 0!");
                        return true;
                    }
                    if (price > 10000000) {
                        player.sendMessage(ChatColor.RED + "Maximum price is $10,000,000!");
                        return true;
                    }
                    sellItem(player, price);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid price: " + args[1]);
                }
                return true;

            case "browse":
                openBrowseMenu(player, 0);
                return true;

            case "my":
            case "listings":
            case "mylistings":
                openMyListings(player);
                return true;

            case "collect":
                collectExpired(player);
                return true;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /auction for the menu.");
                return true;
        }
    }

    private void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Auction House");

        // Browse listings
        ItemStack browseItem = new ItemStack(Material.CHEST);
        ItemMeta browseMeta = browseItem.getItemMeta();
        if (browseMeta != null) {
            browseMeta.setDisplayName(ChatColor.GREEN + "Browse Listings");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "View all items for sale");
            lore.add("");
            lore.add(ChatColor.YELLOW + "" + auctionManager.getListingCount() + " active listings");
            browseMeta.setLore(lore);
            browseItem.setItemMeta(browseMeta);
        }
        gui.setItem(11, browseItem);

        // Sell item
        ItemStack sellItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta sellMeta = sellItem.getItemMeta();
        if (sellMeta != null) {
            sellMeta.setDisplayName(ChatColor.GOLD + "Sell Item");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "List an item for sale");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Hold item and use:");
            lore.add(ChatColor.WHITE + "/auction sell <price>");
            sellMeta.setLore(lore);
            sellItem.setItemMeta(sellMeta);
        }
        gui.setItem(13, sellItem);

        // My listings
        ItemStack myItem = new ItemStack(Material.BOOK);
        ItemMeta myMeta = myItem.getItemMeta();
        if (myMeta != null) {
            myMeta.setDisplayName(ChatColor.AQUA + "My Listings");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "View your active listings");
            lore.add(ChatColor.GRAY + "Cancel listings here");
            lore.add("");
            int myCount = auctionManager.getPlayerListings(player.getUniqueId()).size();
            lore.add(ChatColor.YELLOW + "" + myCount + " active listings");
            myMeta.setLore(lore);
            myItem.setItemMeta(myMeta);
        }
        gui.setItem(15, myItem);

        // Fill empty slots with glass
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        playerModes.put(player.getUniqueId(), AuctionMode.BROWSE);
        player.openInventory(gui);
    }

    private void openBrowseMenu(Player player, int page) {
        List<AuctionListing> allListings = auctionManager.getAllListings();
        int totalPages = Math.max(1, (int) Math.ceil(allListings.size() / (double) ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        playerPages.put(player.getUniqueId(), page);
        playerModes.put(player.getUniqueId(), AuctionMode.BROWSE);

        Inventory gui = Bukkit.createInventory(null, 54, "Auction House - Browse (Page " + (page + 1) + "/" + totalPages + ")");

        // Add listings
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allListings.size());

        for (int i = startIndex; i < endIndex; i++) {
            AuctionListing listing = allListings.get(i);
            ItemStack displayItem = createListingDisplay(listing);
            gui.setItem(i - startIndex, displayItem);
        }

        // Navigation bar at bottom
        addNavigationBar(gui, page, totalPages);

        player.openInventory(gui);
    }

    private void openMyListings(Player player) {
        List<AuctionListing> myListings = auctionManager.getPlayerListings(player.getUniqueId());
        playerModes.put(player.getUniqueId(), AuctionMode.MY_LISTINGS);

        Inventory gui = Bukkit.createInventory(null, 54, "Auction House - My Listings");

        for (int i = 0; i < Math.min(myListings.size(), 45); i++) {
            AuctionListing listing = myListings.get(i);
            ItemStack displayItem = createMyListingDisplay(listing);
            gui.setItem(i, displayItem);
        }

        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.YELLOW + "Back to Menu");
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(49, backButton);

        // Fill bottom row
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 45; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    private ItemStack createListingDisplay(AuctionListing listing) {
        ItemStack display = listing.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();

            lore.add("");
            lore.add(ChatColor.GRAY + "─────────────────");
            lore.add(ChatColor.GOLD + "Price: $" + String.format("%.2f", listing.getPrice()));
            lore.add(ChatColor.GRAY + "Seller: " + ChatColor.WHITE + listing.getSellerName());
            lore.add(ChatColor.GRAY + "Time left: " + ChatColor.YELLOW + auctionManager.formatTimeRemaining(auctionManager.getTimeRemaining(listing)));
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to purchase!");

            // Store listing ID in lore for reference
            lore.add(ChatColor.DARK_GRAY + "ID:" + listing.getListingId().toString());

            meta.setLore(lore);
            display.setItemMeta(meta);
        }
        return display;
    }

    private ItemStack createMyListingDisplay(AuctionListing listing) {
        ItemStack display = listing.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();

            lore.add("");
            lore.add(ChatColor.GRAY + "─────────────────");
            lore.add(ChatColor.GOLD + "Price: $" + String.format("%.2f", listing.getPrice()));
            lore.add(ChatColor.GRAY + "Time left: " + ChatColor.YELLOW + auctionManager.formatTimeRemaining(auctionManager.getTimeRemaining(listing)));
            lore.add("");
            lore.add(ChatColor.RED + "Click to CANCEL listing");

            // Store listing ID in lore for reference
            lore.add(ChatColor.DARK_GRAY + "ID:" + listing.getListingId().toString());

            meta.setLore(lore);
            display.setItemMeta(meta);
        }
        return display;
    }

    private void addNavigationBar(Inventory gui, int currentPage, int totalPages) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }

        for (int i = 45; i < 54; i++) {
            gui.setItem(i, filler);
        }

        // Previous page
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.SPECTRAL_ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ChatColor.YELLOW + "« Previous Page");
                prevButton.setItemMeta(prevMeta);
            }
            gui.setItem(45, prevButton);
        }

        // Back to menu
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Back to Menu");
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(49, backButton);

        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = new ItemStack(Material.SPECTRAL_ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page »");
                nextButton.setItemMeta(nextMeta);
            }
            gui.setItem(53, nextButton);
        }
    }

    private void sellItem(Player player, double price) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item to sell!");
            return;
        }

        // Check listing limit
        List<AuctionListing> myListings = auctionManager.getPlayerListings(player.getUniqueId());
        if (myListings.size() >= 10) {
            player.sendMessage(ChatColor.RED + "You can only have 10 active listings at a time!");
            return;
        }

        // Create listing
        ItemStack itemToSell = itemInHand.clone();
        player.getInventory().setItemInMainHand(null);

        if (auctionManager.createListing(player, itemToSell, price)) {
            player.sendMessage(ChatColor.GREEN + "Successfully listed " + ChatColor.WHITE +
                getItemName(itemToSell) + " x" + itemToSell.getAmount() + ChatColor.GREEN +
                " for " + ChatColor.GOLD + "$" + String.format("%.2f", price));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        } else {
            player.getInventory().addItem(itemToSell);
            player.sendMessage(ChatColor.RED + "Failed to create listing!");
        }
    }

    private void collectExpired(Player player) {
        List<ItemStack> expiredItems = auctionManager.getExpiredItems(player.getUniqueId());
        if (expiredItems.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You have no expired items to collect.");
            return;
        }

        for (ItemStack item : expiredItems) {
            player.getInventory().addItem(item);
        }
        player.sendMessage(ChatColor.GREEN + "Collected " + expiredItems.size() + " expired items!");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ").toLowerCase();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("Auction House")) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Main menu handling
        if (title.equals("Auction House")) {
            handleMainMenuClick(player, clicked);
            return;
        }

        // Browse menu handling
        if (title.contains("Browse")) {
            handleBrowseClick(player, clicked, event.getSlot());
            return;
        }

        // My listings handling
        if (title.contains("My Listings")) {
            handleMyListingsClick(player, clicked, event.getSlot());
            return;
        }
    }

    private void handleMainMenuClick(Player player, ItemStack clicked) {
        Material type = clicked.getType();

        if (type == Material.CHEST) {
            openBrowseMenu(player, 0);
        } else if (type == Material.GOLD_INGOT) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Hold the item you want to sell and use:");
            player.sendMessage(ChatColor.WHITE + "/auction sell <price>");
        } else if (type == Material.BOOK) {
            openMyListings(player);
        }
    }

    private void handleBrowseClick(Player player, ItemStack clicked, int slot) {
        Material type = clicked.getType();

        // Navigation
        if (slot >= 45) {
            if (type == Material.SPECTRAL_ARROW) {
                String displayName = clicked.getItemMeta().getDisplayName();
                int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
                if (displayName.contains("Previous")) {
                    openBrowseMenu(player, currentPage - 1);
                } else if (displayName.contains("Next")) {
                    openBrowseMenu(player, currentPage + 1);
                }
            } else if (type == Material.BARRIER) {
                openMainMenu(player);
            }
            return;
        }

        // Try to purchase
        if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
            List<String> lore = clicked.getItemMeta().getLore();
            for (String line : lore) {
                if (line.startsWith(ChatColor.DARK_GRAY + "ID:")) {
                    String idStr = ChatColor.stripColor(line).substring(3);
                    try {
                        UUID listingId = UUID.fromString(idStr);
                        if (auctionManager.purchaseListing(listingId, player)) {
                            player.sendMessage(ChatColor.GREEN + "Purchase successful!");
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            openBrowseMenu(player, playerPages.getOrDefault(player.getUniqueId(), 0));
                        }
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Error processing purchase!");
                    }
                    return;
                }
            }
        }
    }

    private void handleMyListingsClick(Player player, ItemStack clicked, int slot) {
        Material type = clicked.getType();

        // Back button
        if (slot == 49 && type == Material.ARROW) {
            openMainMenu(player);
            return;
        }

        // Cancel listing
        if (slot < 45 && clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
            List<String> lore = clicked.getItemMeta().getLore();
            for (String line : lore) {
                if (line.startsWith(ChatColor.DARK_GRAY + "ID:")) {
                    String idStr = ChatColor.stripColor(line).substring(3);
                    try {
                        UUID listingId = UUID.fromString(idStr);
                        if (auctionManager.cancelListing(listingId, player)) {
                            player.sendMessage(ChatColor.YELLOW + "Listing cancelled! Item returned to your inventory.");
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                            openMyListings(player);
                        }
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Error cancelling listing!");
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (title.startsWith("Auction House")) {
            playerPages.remove(event.getPlayer().getUniqueId());
            playerModes.remove(event.getPlayer().getUniqueId());
        }
    }
}
