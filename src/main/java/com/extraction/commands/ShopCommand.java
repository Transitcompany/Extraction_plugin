package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.economy.EconomyManager;
import com.extraction.shop.ShopManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ShopCommand implements CommandExecutor, Listener {

    private final ExtractionPlugin plugin;
    private final ShopManager shopManager;
    private final EconomyManager economyManager;

    // --- GUI Constants ---
    private static final String CATEGORY_TITLE =
        ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Extraction Shop";
    private static final String SHOP_TITLE_PREFIX =
        ChatColor.DARK_AQUA + "Shop - ";

    // Fillers
    private static final Material FILLER_MATERIAL =
        Material.BLACK_STAINED_GLASS_PANE;
    private static final Material NAV_FILLER_MATERIAL =
        Material.GRAY_STAINED_GLASS_PANE; // Contrasting filler

    // Layout Sizes and Slots
    private static final int INVENTORY_SIZE = 54;
    private static final int CATEGORY_MENU_SIZE = 54; // Main menu is now 54 slots
    private static final int ITEMS_PER_PAGE = 36;

    // Utility Slot Constants (Used in both shop and category menu)
    private static final int PREV_PAGE_SLOT = 36;
    private static final int BACK_BUTTON_SLOT = 40;
    private static final int NEXT_PAGE_SLOT = 44;
    private static final int INFO_ICON_SLOT = 46;
    private static final int BALANCE_SLOT = 49;
    private static final int SELL_REMINDER_SLOT = 52;

    // New/Repositioned Slots for Category Menu
    private static final int HEADER_INFO_SLOT = 4; // Center of the new top row
    private static final int WELCOME_BANNER_SLOT = 13; // Center of the second row
    private static final int CATEGORY_COMBAT_SLOT = 19;
    private static final int CATEGORY_TRAPS_SLOT = 21;
    private static final int CATEGORY_FOOD_SLOT = 23;
    private static final int CATEGORY_BUILDING_SLOT = 25;
    private static final int CATEGORY_WEAPONS_SLOT = 28;
    private static final int CATEGORY_SPECIAL_SLOT = 31; // Center of row 4

    // Track player's current view state
    private final Map<UUID, ShopState> playerStates = new HashMap<>();

    private static class ShopState {

        String category = "menu";
        int page = 0;
        List<Map.Entry<ItemStack, Integer>> currentItems = new ArrayList<>();
    }

    public ShopCommand(
        ExtractionPlugin plugin,
        ShopManager shopManager,
        EconomyManager economyManager
    ) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.economyManager = economyManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player) sender;
        openCategoryMenu(player);
        return true;
    }

    // --- Utility Methods ---

    private ItemStack createGuiItem(
        final Material material,
        final String name,
        final String... lore
    ) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            final List<String> finalLore = new ArrayList<>();
            for (final String line : lore) {
                finalLore.add(line);
            }
            meta.setLore(finalLore);
            item.setItemMeta(meta);
        }

        return item;
    }

    // Utility for creating filler panes
    private ItemStack createFiller(Material material) {
        return createGuiItem(material, " ", "");
    }

    // --- GUI Creation Logic (Remade) ---

    private void openCategoryMenu(Player player) {
        Inventory gui = Bukkit.createInventory(
            null,
            CATEGORY_MENU_SIZE,
            CATEGORY_TITLE
        );

        ItemStack navFiller = createFiller(NAV_FILLER_MATERIAL);
        ItemStack darkFiller = createFiller(FILLER_MATERIAL);

        // 1. Fill the entire inventory with the dark filler initially
        for (int i = 0; i < CATEGORY_MENU_SIZE; i++) {
            gui.setItem(i, darkFiller);
        }

        // 2. Build the NEW Top Row (Row 1: Slots 0-8) - Using contrasting filler
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, navFiller);
        }
        ItemStack headerInfo = createGuiItem(
            Material.COMPASS,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Shop Navigation",
            ChatColor.GRAY + "Select a button below to explore categories."
        );
        gui.setItem(HEADER_INFO_SLOT, headerInfo);

        // 3. Welcome Banner (Row 2: Slot 13 - Center)
        int balance = (int) economyManager.getBalanceAsDouble(player.getUniqueId());
        ItemStack welcomeBanner = createGuiItem(
            Material.BOOK,
            ChatColor.AQUA +
                "" +
                ChatColor.BOLD +
                "Welcome to the Extraction Shop",
            ChatColor.GRAY +
                "Items purchased here are vital for a successful raid.",
            ChatColor.GRAY +
                "Your current balance is: " +
                ChatColor.YELLOW +
                "$" +
                String.format("%,d", balance)
        );
        gui.setItem(WELCOME_BANNER_SLOT, welcomeBanner);

        // 4. Category Buttons (Repositioned to Rows 3 & 4)

        // Row 3 (Slots 18-26)
        ItemStack combatItem = createGuiItem(
            Material.DIAMOND_SWORD,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Combat & Gear",
            ChatColor.GRAY + "Armor, weapons, and tools"
        );
        gui.setItem(CATEGORY_COMBAT_SLOT, combatItem);

        ItemStack trapItem = createGuiItem(
            Material.REDSTONE,
            ChatColor.RED + "" + ChatColor.BOLD + "Traps & Redstone",
            ChatColor.GRAY + "Redstone, TNT, pistons,",
            ChatColor.GRAY + "and trap components"
        );
        gui.setItem(CATEGORY_TRAPS_SLOT, trapItem);

        ItemStack foodItem = createGuiItem(
            Material.GOLDEN_APPLE,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Food & Consumables",
            ChatColor.GRAY + "Food, crops, and healing"
        );
        gui.setItem(CATEGORY_FOOD_SLOT, foodItem);

        ItemStack buildItem = createGuiItem(
            Material.CHEST,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Building & Storage",
            ChatColor.GRAY + "Blocks, chests, and utilities"
        );
        gui.setItem(CATEGORY_BUILDING_SLOT, buildItem);

        // Weapons Category (Row 4)
        ItemStack weaponsItem = createGuiItem(
            Material.IRON_SWORD,
            ChatColor.RED + "" + ChatColor.BOLD + "Weapons",
            ChatColor.GRAY + "Custom weapons and",
            ChatColor.GRAY + "special ammunition!",
            "",
            ChatColor.YELLOW + "Advanced combat gear!"
        );
        gui.setItem(CATEGORY_WEAPONS_SLOT, weaponsItem);

        // Row 4 (Slots 27-35)
        ItemStack specialItem = createGuiItem(
            Material.NETHER_STAR,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Special Items",
            ChatColor.GRAY + "Tactical gear, gadgets,",
            ChatColor.GRAY + "weapons, and extraction items!",
            "",
            ChatColor.YELLOW + "50+ unique items!"
        );
        gui.setItem(CATEGORY_SPECIAL_SLOT, specialItem); // Slot 31

        // 5. Build the NEW Bottom Row (Row 6: Slots 45-53) - Using contrasting filler
        for (int i = 45; i < CATEGORY_MENU_SIZE; i++) {
            gui.setItem(i, navFiller);
        }

        // NEW: Info Icon (slot 46)
        ItemStack infoIcon = createGuiItem(
            Material.PAPER,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Shop Information",
            ChatColor.GRAY + "All purchases are final.",
            ChatColor.GRAY + "Items go directly to your inventory."
        );
        gui.setItem(INFO_ICON_SLOT, infoIcon);

        // Info item showing balance (slot 49 - Center Bottom Row)
        ItemStack balanceItem = createGuiItem(
            Material.GOLD_INGOT,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Your Balance",
            ChatColor.WHITE + "$" + String.format("%,d", balance),
            "",
            ChatColor.GRAY + "Earn money by extracting!"
        );
        gui.setItem(BALANCE_SLOT, balanceItem);

        // NEW: Sell Reminder (slot 52)
        ItemStack sellReminder = createGuiItem(
            Material.EMERALD_BLOCK,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Need Money?",
            ChatColor.GRAY +
                "Use " +
                ChatColor.YELLOW +
                "/sell" +
                ChatColor.GRAY +
                " to access the sell shop.",
            ChatColor.GRAY +
                "Or use " +
                ChatColor.YELLOW +
                "/sell all" +
                ChatColor.GRAY +
                " to quickly sell items."
        );
        gui.setItem(SELL_REMINDER_SLOT, sellReminder);

        player.openInventory(gui);

        // Set state
        ShopState state = new ShopState();
        state.category = "menu";
        state.page = 0;
        playerStates.put(player.getUniqueId(), state);
    }

    private void openCategoryShop(Player player, String category, int page) {
        // ... (This method remains largely the same as the previous full version,
        // as the structure of the sub-shop pages was already 54 slots with navigation)

        Map<ItemStack, Integer> allItems = shopManager.getItemsForPage(
            getCategoryPage(category)
        );
        List<Map.Entry<ItemStack, Integer>> itemList = new ArrayList<>(
            allItems.entrySet()
        );

        int totalItems = itemList.size();
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        // Clamp page number
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        String title = SHOP_TITLE_PREFIX + getCategoryTitle(category);
        if (totalPages > 1) {
            title += " (" + (page + 1) + "/" + totalPages + ")";
        }

        Inventory gui = Bukkit.createInventory(null, INVENTORY_SIZE, title);

        // Calculate item range for this page
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);

        // Store current page items for click handling
        List<Map.Entry<ItemStack, Integer>> pageItems = new ArrayList<>();

        int slot = 0;
        for (int i = startIndex; i < endIndex && slot < ITEMS_PER_PAGE; i++) {
            Map.Entry<ItemStack, Integer> entry = itemList.get(i);
            pageItems.add(entry);

            ItemStack display = entry.getKey().clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "Price: $" + entry.getValue());
                
                int stock = shopManager.getStockForItem(entry.getKey());
                ChatColor stockColor;
                if (stock == 0) {
                    stockColor = ChatColor.RED;
                } else if (stock <= 5) {
                    stockColor = ChatColor.YELLOW;
                } else {
                    stockColor = ChatColor.GREEN;
                }
                lore.add(stockColor + "Stock: " + stock);
                
                if (stock > 0) {
                    lore.add(ChatColor.YELLOW + "Click to purchase");
                } else {
                    lore.add(ChatColor.RED + "Out of stock!");
                }
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            gui.setItem(slot++, display);
        }

        // --- Navigation and Info Bar (Rows 5 & 6) ---
        ItemStack navFiller = createFiller(NAV_FILLER_MATERIAL);

        // Fill rows 5 and 6 (slots 36-53) with gray filler
        for (int i = 36; i < INVENTORY_SIZE; i++) {
            gui.setItem(i, navFiller);
        }

        // The rest of the nav bar items:

        // Previous page button (slot 36)
        if (page > 0) {
            ItemStack prevButton = createGuiItem(
                Material.ARROW,
                ChatColor.YELLOW + "« Previous Page",
                ChatColor.GRAY + "Go to page " + page
            );
            gui.setItem(PREV_PAGE_SLOT, prevButton);
        }

        // Back to categories button (slot 40)
        ItemStack backButton = createGuiItem(
            Material.BARRIER,
            ChatColor.RED + "" + ChatColor.BOLD + "« Back to Categories",
            ChatColor.GRAY + "Return to category selection"
        );
        gui.setItem(BACK_BUTTON_SLOT, backButton);

        // Next page button (slot 44)
        if (page < totalPages - 1) {
            ItemStack nextButton = createGuiItem(
                Material.ARROW,
                ChatColor.YELLOW + "Next Page »",
                ChatColor.GRAY + "Go to page " + (page + 2)
            );
            gui.setItem(NEXT_PAGE_SLOT, nextButton);
        }

        // Info Icon (slot 46)
        ItemStack infoIcon = createGuiItem(
            Material.PAPER,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Shop Information",
            ChatColor.GRAY + "All purchases are final.",
            ChatColor.GRAY + "Items go directly to your inventory.",
            ChatColor.GRAY + "Ensure you have space before buying!"
        );
        gui.setItem(INFO_ICON_SLOT, infoIcon);

        // Info item showing balance (slot 49 - Center Bottom Row)
        int balance = (int) economyManager.getBalanceAsDouble(player.getUniqueId());
        ItemStack balanceItem = createGuiItem(
            Material.GOLD_INGOT,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Your Balance",
            ChatColor.WHITE + "$" + String.format("%,d", balance),
            "",
            ChatColor.GRAY + "Earn money by extracting!"
        );
        gui.setItem(BALANCE_SLOT, balanceItem);

        // Sell Reminder (slot 52)
        ItemStack sellReminder = createGuiItem(
            Material.EMERALD_BLOCK,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Need Money?",
            ChatColor.GRAY +
                "Use " +
                ChatColor.YELLOW +
                "/sell" +
                ChatColor.GRAY +
                " to access the sell shop.",
            ChatColor.GRAY +
                "Or use " +
                ChatColor.YELLOW +
                "/sell all" +
                ChatColor.GRAY +
                " to quickly sell an item."
        );
        gui.setItem(SELL_REMINDER_SLOT, sellReminder);

        player.openInventory(gui);

        // Set state
        ShopState state = new ShopState();
        state.category = category;
        state.page = page;
        state.currentItems = pageItems;
        playerStates.put(player.getUniqueId(), state);
    }

    // --- Helper Methods (Category Logic Kept) ---

    private int getCategoryPage(String category) {
        switch (category) {
            case "combat":
                return 1;
            case "traps":
                return 2;
            case "food":
                return 3;
            case "building":
                return 4;
            case "weapons":
                return 5;
            case "special":
                return 6;
            default:
                return 1;
        }
    }

    private String getCategoryTitle(String category) {
        switch (category) {
            case "combat":
                return "Combat & Gear";
            case "traps":
                return "Traps & Redstone";
            case "food":
                return "Food & Consumables";
            case "building":
                return "Building & Storage";
            case "weapons":
                return "Weapons";
            case "special":
                return "Special Items";
            default:
                return "Shop";
        }
    }

    // --- Event Handlers (Updated for new icons) ---

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (
            !title.startsWith(SHOP_TITLE_PREFIX) &&
            !title.equals(CATEGORY_TITLE)
        ) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        int rawSlot = event.getRawSlot();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Ignore all filler and utility icons in all GUIs
        if (
            rawSlot == HEADER_INFO_SLOT ||
            rawSlot == WELCOME_BANNER_SLOT ||
            rawSlot == INFO_ICON_SLOT ||
            rawSlot == BALANCE_SLOT ||
            rawSlot == SELL_REMINDER_SLOT ||
            clicked.getType() == FILLER_MATERIAL ||
            clicked.getType() == NAV_FILLER_MATERIAL
        ) return;

        ShopState state = playerStates.get(player.getUniqueId());
        if (state == null) {
            state = new ShopState();
            playerStates.put(player.getUniqueId(), state);
        }

        // Category menu handling
        if (state.category.equals("menu") || title.equals(CATEGORY_TITLE)) {
            handleCategoryClick(player, clicked, rawSlot);
            return;
        }

        // Shop item handling
        handleShopClick(player, clicked, state, rawSlot);
    }

    private void handleCategoryClick(
        Player player,
        ItemStack clicked,
        int rawSlot
    ) {
        // Only respond to clicks on the category buttons
        if (rawSlot == CATEGORY_COMBAT_SLOT) {
            openCategoryShop(player, "combat", 0);
        } else if (rawSlot == CATEGORY_TRAPS_SLOT) {
            openCategoryShop(player, "traps", 0);
        } else if (rawSlot == CATEGORY_FOOD_SLOT) {
            openCategoryShop(player, "food", 0);
        } else if (rawSlot == CATEGORY_BUILDING_SLOT) {
            openCategoryShop(player, "building", 0);
        } else if (rawSlot == CATEGORY_WEAPONS_SLOT) {
            openCategoryShop(player, "weapons", 0);
        } else if (rawSlot == CATEGORY_SPECIAL_SLOT) {
            openCategoryShop(player, "special", 0);
        }
    }

    private void handleShopClick(
        Player player,
        ItemStack clicked,
        ShopState state,
        int rawSlot
    ) {
        // Navigation Icons
        if (rawSlot == BACK_BUTTON_SLOT) {
            openCategoryMenu(player);
            return;
        }
        if (rawSlot == PREV_PAGE_SLOT && state.page > 0) {
            openCategoryShop(player, state.category, state.page - 1);
            return;
        }
        // Next page check needs to be conditional on having more pages
        if (rawSlot == NEXT_PAGE_SLOT) {
            // Recalculate total pages to ensure valid click
            Map<ItemStack, Integer> allItems = shopManager.getItemsForPage(
                getCategoryPage(state.category)
            );
            int totalPages = (int) Math.ceil(
                (double) allItems.size() / ITEMS_PER_PAGE
            );
            if (state.page < totalPages - 1) {
                openCategoryShop(player, state.category, state.page + 1);
            }
            return;
        }

        // Only process clicks in the item area (slots 0-35)
        if (rawSlot < 0 || rawSlot >= ITEMS_PER_PAGE) {
            return;
        }

        // Find the item in current page items
        if (rawSlot < state.currentItems.size()) {
            Map.Entry<ItemStack, Integer> entry = state.currentItems.get(
                rawSlot
            );
            purchaseItem(player, entry.getKey(), entry.getValue(), state);
            return;
        }
    }

    private void purchaseItem(
        Player player,
        ItemStack shopItem,
        int price,
        ShopState state
    ) {
        if (!shopManager.hasStock(shopItem)) {
            player.sendMessage(
                ChatColor.RED +
                    "This item is out of stock!"
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_VILLAGER_NO,
                1.0f,
                1.0f
            );
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(
                ChatColor.RED +
                    "Your inventory is full! Make space before buying."
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_VILLAGER_NO,
                1.0f,
                1.0f
            );
            return;
        }

        if (shopManager.buyItem(player, shopItem, price)) {
            player.sendMessage(
                ChatColor.GREEN +
                    "Purchased " +
                    getItemName(shopItem) +
                    " for $" +
                    price +
                    "!"
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                1.0f,
                1.0f
            );

            // Refresh the shop to update stock and balance display
            if (state.category.equals("menu")) {
                openCategoryMenu(player);
            } else {
                openCategoryShop(player, state.category, state.page);
            }
        } else {
        int balance = (int) economyManager.getBalanceAsDouble(player.getUniqueId());
            player.sendMessage(
                ChatColor.RED +
                    "Not enough money! Need $" +
                    price +
                    " (You have $" +
                    balance +
                    ")"
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_VILLAGER_NO,
                1.0f,
                1.0f
            );
        }
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return ChatColor.stripColor(item.getItemMeta().getDisplayName());
        }
        // Convert material name to readable format
        String name = item.getType().name().toLowerCase().replace("_", " ");
        // Capitalize first letter of each word
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (sb.length() > 0) sb.append(" ");
            sb
                .append(Character.toUpperCase(word.charAt(0)))
                .append(word.substring(1));
        }
        if (item.getAmount() > 1) {
            sb.append(" x").append(item.getAmount());
        }
        return sb.toString();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (
            title.startsWith(SHOP_TITLE_PREFIX) || title.equals(CATEGORY_TITLE)
        ) {
            // Small delay before removing to handle inventory transitions
            Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    Player player = (Player) event.getPlayer();
                    // Only remove if they're not still in a shop
                    if (
                        player.getOpenInventory().getTopInventory() == null ||
                        (!player
                                .getOpenInventory()
                                .getTitle()
                                .startsWith(SHOP_TITLE_PREFIX) &&
                            !player
                                .getOpenInventory()
                                .getTitle()
                                .equals(CATEGORY_TITLE))
                    ) {
                        playerStates.remove(player.getUniqueId());
                    }
                },
                1L
            );
        }
    }
}
