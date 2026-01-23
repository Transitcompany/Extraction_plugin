package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.economy.EconomyManager;
import com.extraction.leveling.LevelingManager;
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
import org.bukkit.entity.HumanEntity;
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

public class SellCommand implements CommandExecutor, Listener {

    private final ExtractionPlugin plugin;
    private final EconomyManager economyManager;
    private final LevelingManager levelingManager;
    private final Map<Material, Double> basePrices = new HashMap<>();

    // --- GUI Constants ---
    private static final String GUI_TITLE =
        ChatColor.DARK_GRAY + "Sell Items - Drop & Click";
    private static final int INVENTORY_SIZE = 54;
    private static final int SELL_ALL_SLOT = 49;
    private static final Material FILLER_MATERIAL =
        Material.BLACK_STAINED_GLASS_PANE;

    public SellCommand(ExtractionPlugin plugin, EconomyManager economyManager, LevelingManager levelingManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.levelingManager = levelingManager;
        setupBasePrices();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // --- Price Setup (kept as is) ---
    private void setupBasePrices() {
        // [Existing setupBasePrices content here]
        // Resources - Common
        basePrices.put(Material.COAL, 0.5);
        basePrices.put(Material.CHARCOAL, 0.5);
        basePrices.put(Material.RAW_IRON, 1.5);
        basePrices.put(Material.IRON_INGOT, 2.0);
        basePrices.put(Material.RAW_GOLD, 2.0);
        basePrices.put(Material.GOLD_INGOT, 3.0);
        basePrices.put(Material.RAW_COPPER, 0.8);
        basePrices.put(Material.COPPER_INGOT, 1.0);
        basePrices.put(Material.DIAMOND, 15.0);
        basePrices.put(Material.EMERALD, 10.0);
        basePrices.put(Material.NETHERITE_SCRAP, 50.0);
        basePrices.put(Material.NETHERITE_INGOT, 250.0);
        basePrices.put(Material.LAPIS_LAZULI, 0.5);
        basePrices.put(Material.REDSTONE, 0.3);
        basePrices.put(Material.QUARTZ, 0.5);
        basePrices.put(Material.AMETHYST_SHARD, 1.0);

        // Mob drops
        basePrices.put(Material.ROTTEN_FLESH, 0.1);
        basePrices.put(Material.BONE, 0.3);
        basePrices.put(Material.STRING, 0.3);
        basePrices.put(Material.SPIDER_EYE, 0.5);
        basePrices.put(Material.GUNPOWDER, 0.8);
        basePrices.put(Material.ENDER_PEARL, 15.0);
        basePrices.put(Material.BLAZE_ROD, 3.0);
        basePrices.put(Material.GHAST_TEAR, 8.0);
        basePrices.put(Material.SLIME_BALL, 0.5);
        basePrices.put(Material.MAGMA_CREAM, 1.0);
        basePrices.put(Material.PHANTOM_MEMBRANE, 2.0);
        basePrices.put(Material.LEATHER, 0.5);
        basePrices.put(Material.RABBIT_HIDE, 0.3);
        basePrices.put(Material.RABBIT_FOOT, 2.0);
        basePrices.put(Material.FEATHER, 0.2);
        basePrices.put(Material.INK_SAC, 0.3);
        basePrices.put(Material.GLOW_INK_SAC, 1.0);
        basePrices.put(Material.PRISMARINE_SHARD, 1.0);
        basePrices.put(Material.PRISMARINE_CRYSTALS, 1.5);
        basePrices.put(Material.NAUTILUS_SHELL, 5.0);
        basePrices.put(Material.HEART_OF_THE_SEA, 50.0);
        basePrices.put(Material.NETHER_STAR, 500.0);
        basePrices.put(Material.SHULKER_SHELL, 75.0);
        basePrices.put(Material.TOTEM_OF_UNDYING, 100.0);
        basePrices.put(Material.TRIDENT, 50.0);
        basePrices.put(Material.ELYTRA, 500.0);

        // Food - Raw
        basePrices.put(Material.BEEF, 0.5);
        basePrices.put(Material.PORKCHOP, 0.5);
        basePrices.put(Material.CHICKEN, 0.4);
        basePrices.put(Material.MUTTON, 0.4);
        basePrices.put(Material.RABBIT, 0.6);
        basePrices.put(Material.COD, 0.3);
        basePrices.put(Material.SALMON, 0.4);
        basePrices.put(Material.TROPICAL_FISH, 1.0);

        // Food - Cooked
        basePrices.put(Material.COOKED_BEEF, 1.0);
        basePrices.put(Material.COOKED_PORKCHOP, 1.0);
        basePrices.put(Material.COOKED_CHICKEN, 0.8);
        basePrices.put(Material.COOKED_MUTTON, 0.8);
        basePrices.put(Material.COOKED_RABBIT, 1.2);
        basePrices.put(Material.COOKED_COD, 0.6);
        basePrices.put(Material.COOKED_SALMON, 0.8);

        // Food - Other
        basePrices.put(Material.APPLE, 0.5);
        basePrices.put(Material.BREAD, 0.4);
        basePrices.put(Material.CARROT, 0.2);
        basePrices.put(Material.POTATO, 0.2);
        basePrices.put(Material.BAKED_POTATO, 0.4);
        basePrices.put(Material.BEETROOT, 0.2);
        basePrices.put(Material.MELON_SLICE, 0.1);
        basePrices.put(Material.PUMPKIN_PIE, 1.0);
        basePrices.put(Material.COOKIE, 0.2);
        basePrices.put(Material.CAKE, 3.0);
        basePrices.put(Material.GOLDEN_CARROT, 5.0);
        basePrices.put(Material.GOLDEN_APPLE, 25.0);
        basePrices.put(Material.ENCHANTED_GOLDEN_APPLE, 200.0);
        basePrices.put(Material.SWEET_BERRIES, 0.2);
        basePrices.put(Material.GLOW_BERRIES, 0.5);
        basePrices.put(Material.HONEY_BOTTLE, 2.0);

        // Plants & Farming
        basePrices.put(Material.WHEAT, 0.1);
        basePrices.put(Material.WHEAT_SEEDS, 0.05);
        basePrices.put(Material.SUGAR_CANE, 0.1);
        basePrices.put(Material.BAMBOO, 0.1);
        basePrices.put(Material.CACTUS, 0.1);
        basePrices.put(Material.KELP, 0.1);
        basePrices.put(Material.DRIED_KELP, 0.2);
        basePrices.put(Material.COCOA_BEANS, 0.3);
        basePrices.put(Material.PUMPKIN, 0.5);
        basePrices.put(Material.MELON, 0.5);
        basePrices.put(Material.NETHER_WART, 0.5);
        basePrices.put(Material.CHORUS_FRUIT, 1.0);

        // Misc items
        basePrices.put(Material.FLINT, 0.2);
        basePrices.put(Material.CLAY_BALL, 0.2);
        basePrices.put(Material.BRICK, 0.3);
        basePrices.put(Material.PAPER, 0.1);
        basePrices.put(Material.BOOK, 0.5);
        basePrices.put(Material.ARROW, 0.2);
        basePrices.put(Material.SPECTRAL_ARROW, 0.5);
        basePrices.put(Material.TORCH, 0.1);
        basePrices.put(Material.LANTERN, 0.5);
        basePrices.put(Material.GLOWSTONE_DUST, 0.5);
        basePrices.put(Material.COBWEB, 1.0);
        basePrices.put(Material.EXPERIENCE_BOTTLE, 3.0);
        basePrices.put(Material.ENCHANTED_BOOK, 20.0);
        basePrices.put(Material.NAME_TAG, 5.0);
        basePrices.put(Material.SADDLE, 10.0);
        basePrices.put(Material.LEAD, 2.0);
        basePrices.put(Material.MILK_BUCKET, 2.0);
        basePrices.put(Material.EGG, 0.2);
        basePrices.put(Material.SNOWBALL, 0.1);
        basePrices.put(Material.ENDER_EYE, 20.0);

        // Tools & Weapons - Iron
        basePrices.put(Material.IRON_SWORD, 8.0);
        basePrices.put(Material.IRON_PICKAXE, 10.0);
        basePrices.put(Material.IRON_AXE, 10.0);
        basePrices.put(Material.IRON_SHOVEL, 5.0);
        basePrices.put(Material.IRON_HOE, 6.0);

        // Tools & Weapons - Diamond
        basePrices.put(Material.DIAMOND_SWORD, 40.0);
        basePrices.put(Material.DIAMOND_PICKAXE, 50.0);
        basePrices.put(Material.DIAMOND_AXE, 50.0);
        basePrices.put(Material.DIAMOND_SHOVEL, 25.0);
        basePrices.put(Material.DIAMOND_HOE, 30.0);

        // Tools & Weapons - Netherite
        basePrices.put(Material.NETHERITE_SWORD, 300.0);
        basePrices.put(Material.NETHERITE_PICKAXE, 350.0);
        basePrices.put(Material.NETHERITE_AXE, 350.0);
        basePrices.put(Material.NETHERITE_SHOVEL, 280.0);
        basePrices.put(Material.NETHERITE_HOE, 280.0);

        // Tools - Other
        basePrices.put(Material.BOW, 5.0);
        basePrices.put(Material.CROSSBOW, 8.0);
        basePrices.put(Material.FISHING_ROD, 3.0);
        basePrices.put(Material.SHEARS, 4.0);
        basePrices.put(Material.FLINT_AND_STEEL, 3.0);
        basePrices.put(Material.SHIELD, 10.0);
        basePrices.put(Material.COMPASS, 3.0);
        basePrices.put(Material.CLOCK, 5.0);
        basePrices.put(Material.SPYGLASS, 8.0);

        // Armor - Iron
        basePrices.put(Material.IRON_HELMET, 15.0);
        basePrices.put(Material.IRON_CHESTPLATE, 25.0);
        basePrices.put(Material.IRON_LEGGINGS, 22.0);
        basePrices.put(Material.IRON_BOOTS, 12.0);

        // Armor - Diamond
        basePrices.put(Material.DIAMOND_HELMET, 70.0);
        basePrices.put(Material.DIAMOND_CHESTPLATE, 120.0);
        basePrices.put(Material.DIAMOND_LEGGINGS, 100.0);
        basePrices.put(Material.DIAMOND_BOOTS, 60.0);

        // Armor - Netherite
        basePrices.put(Material.NETHERITE_HELMET, 400.0);
        basePrices.put(Material.NETHERITE_CHESTPLATE, 600.0);
        basePrices.put(Material.NETHERITE_LEGGINGS, 550.0);
        basePrices.put(Material.NETHERITE_BOOTS, 350.0);

        // Armor - Other
        basePrices.put(Material.LEATHER_HELMET, 2.0);
        basePrices.put(Material.LEATHER_CHESTPLATE, 4.0);
        basePrices.put(Material.LEATHER_LEGGINGS, 3.0);
        basePrices.put(Material.LEATHER_BOOTS, 2.0);
        basePrices.put(Material.CHAINMAIL_HELMET, 8.0);
        basePrices.put(Material.CHAINMAIL_CHESTPLATE, 15.0);
        basePrices.put(Material.CHAINMAIL_LEGGINGS, 12.0);
        basePrices.put(Material.CHAINMAIL_BOOTS, 6.0);
        basePrices.put(Material.GOLDEN_HELMET, 10.0);
        basePrices.put(Material.GOLDEN_CHESTPLATE, 18.0);
        basePrices.put(Material.GOLDEN_LEGGINGS, 15.0);
        basePrices.put(Material.GOLDEN_BOOTS, 8.0);

        // Building blocks
        basePrices.put(Material.COBBLESTONE, 0.05);
        basePrices.put(Material.STONE, 0.1);
        basePrices.put(Material.STONE_BRICKS, 0.15);
        basePrices.put(Material.DEEPSLATE, 0.1);
        basePrices.put(Material.OBSIDIAN, 3.0);
        basePrices.put(Material.CRYING_OBSIDIAN, 5.0);
        basePrices.put(Material.GLASS, 0.2);
        basePrices.put(Material.GLASS_PANE, 0.1);

        // Storage
        basePrices.put(Material.CHEST, 2.0);
        basePrices.put(Material.BARREL, 2.0);
        basePrices.put(Material.ENDER_CHEST, 30.0);
        basePrices.put(Material.SHULKER_BOX, 100.0);

        // Redstone
        basePrices.put(Material.REDSTONE_BLOCK, 3.0);
        basePrices.put(Material.PISTON, 3.0);
        basePrices.put(Material.STICKY_PISTON, 5.0);
        basePrices.put(Material.OBSERVER, 5.0);
        basePrices.put(Material.HOPPER, 8.0);
        basePrices.put(Material.DISPENSER, 4.0);
        basePrices.put(Material.DROPPER, 3.0);
        basePrices.put(Material.TNT, 5.0);
        basePrices.put(Material.REPEATER, 2.0);
        basePrices.put(Material.COMPARATOR, 3.0);
    }

    // --- Price Calculation (kept as is) ---
    private double getItemPrice(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;

        // Check for unsellable items (GUI fillers, etc.)
        Material type = item.getType();
        if (
            type == Material.BLACK_STAINED_GLASS_PANE ||
            type == Material.WHITE_STAINED_GLASS_PANE ||
            type == Material.GRAY_STAINED_GLASS_PANE ||
            type == Material.LIGHT_GRAY_STAINED_GLASS_PANE ||
            type.name().endsWith("_STAINED_GLASS_PANE")
        ) {
            // Check if it's a GUI filler (no real name or blank name)
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String name = meta.getDisplayName();
                    if (name.trim().isEmpty() || name.equals(" ")) {
                        return -1; // Mark as unsellable
                    }
                }
            }
        }

        // Check for special extraction items with NBT data first
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                PersistentDataContainer container =
                    meta.getPersistentDataContainer();

                // Special extraction items
                NamespacedKey bannerKey = new NamespacedKey(
                    plugin,
                    "extraction_banner"
                );
                NamespacedKey flareKey = new NamespacedKey(
                    plugin,
                    "extraction_flare"
                );
                NamespacedKey teleporterKey = new NamespacedKey(
                    plugin,
                    "emergency_teleporter"
                );
                NamespacedKey smokeBombKey = new NamespacedKey(
                    plugin,
                    "smoke_bomb"
                );
                NamespacedKey grapplingKey = new NamespacedKey(
                    plugin,
                    "grappling_hook"
                );
                NamespacedKey trackerKey = new NamespacedKey(
                    plugin,
                    "tracker_compass"
                );
                NamespacedKey medKitKey = new NamespacedKey(plugin, "med_kit");
                NamespacedKey ammoBoxKey = new NamespacedKey(
                    plugin,
                    "ammo_box"
                );
                NamespacedKey moneyPrinterKey = new NamespacedKey(
                    plugin,
                    "money_printer"
                );
                NamespacedKey jetpackKey = new NamespacedKey(plugin, "jetpack");
                NamespacedKey landmineKey = new NamespacedKey(
                    plugin,
                    "landmine"
                );
                NamespacedKey adrenalineKey = new NamespacedKey(
                    plugin,
                    "adrenaline_shot"
                );
                NamespacedKey cashMoneyKey = new NamespacedKey(
                    plugin,
                    "cash_money"
                );
                NamespacedKey cashValueKey = new NamespacedKey(
                    plugin,
                    "cash_value"
                );
                NamespacedKey gpsTrailKey = new NamespacedKey(
                    plugin,
                    "gps_trail_key"
                );

                if (container.has(bannerKey, PersistentDataType.BYTE)) {
                    return 50000.0; // Extraction Banner
                }
                if (container.has(flareKey, PersistentDataType.BYTE)) {
                    return 50000.0; // Extraction Flare
                }
                if (container.has(teleporterKey, PersistentDataType.BYTE)) {
                    return 30000.0; // Emergency Teleporter
                }
                if (container.has(smokeBombKey, PersistentDataType.BYTE)) {
                    return 300.0; // Smoke Bomb
                }
                if (container.has(grapplingKey, PersistentDataType.BYTE)) {
                    return 450.0; // Grappling Hook
                }
                if (container.has(trackerKey, PersistentDataType.BYTE)) {
                    return 600.0; // Tracker Compass
                }
                if (container.has(gpsTrailKey, PersistentDataType.BYTE)) {
                    return 150.0; // GPS Trail Key
                }
                if (container.has(medKitKey, PersistentDataType.BYTE)) {
                    return 300.0; // Med Kit
                }
                if (container.has(ammoBoxKey, PersistentDataType.BYTE)) {
                    return 450.0; // Ammo Box
                }
                if (container.has(moneyPrinterKey, PersistentDataType.BYTE)) {
                    return 30000.0; // Money Printer
                }
                if (container.has(jetpackKey, PersistentDataType.BYTE)) {
                    return 1500.0; // Jetpack
                }
                if (container.has(landmineKey, PersistentDataType.BYTE)) {
                    return 240.0; // Landmine
                }
                if (container.has(adrenalineKey, PersistentDataType.BYTE)) {
                    return 360.0; // Adrenaline Shot
                }
                NamespacedKey speedPowderKey = new NamespacedKey(
                    plugin,
                    "speed_powder"
                );
                NamespacedKey invisibilityCloakKey = new NamespacedKey(
                    plugin,
                    "invisibility_cloak"
                );
                NamespacedKey reviveSyringeKey = new NamespacedKey(
                    plugin,
                    "revive_syringe"
                );
                NamespacedKey empGrenadeKey = new NamespacedKey(
                    plugin,
                    "emp_grenade"
                );
                if (container.has(speedPowderKey, PersistentDataType.BYTE)) {
                    return 900.0; // Speed Powder
                }
                if (
                    container.has(invisibilityCloakKey, PersistentDataType.BYTE)
                ) {
                    return 2400.0; // Invisibility Cloak
                }
                if (container.has(reviveSyringeKey, PersistentDataType.BYTE)) {
                    return 1500.0; // Revive Syringe
                }
                if (container.has(empGrenadeKey, PersistentDataType.BYTE)) {
                    return 750.0; // EMP Grenade
                }
                // Cash money - return its stored value (not sellable, deposit instead)
                if (container.has(cashMoneyKey, PersistentDataType.BYTE)) {
                    return -1; // Cash should be deposited, not sold
                }
            }
        }

        // Check for defined base price
        Double basePrice = basePrices.get(item.getType());
        if (basePrice != null) {
            return basePrice;
        }

        // Auto-calculate price based on material type
        return calculateAutoPrice(item.getType());
    }

    private double calculateAutoPrice(Material material) {
        String name = material.name();

        // Netherite items
        if (name.contains("NETHERITE")) return 200.0;

        // Diamond items
        if (name.contains("DIAMOND")) return 30.0;

        // Gold items
        if (name.contains("GOLD") || name.contains("GOLDEN")) return 5.0;

        // Iron items
        if (name.contains("IRON")) return 3.0;

        // Stone items
        if (name.contains("STONE") && !name.contains("REDSTONE")) return 0.5;

        // Wooden items
        if (
            name.contains("WOODEN") ||
            name.contains("OAK") ||
            name.contains("BIRCH") ||
            name.contains("SPRUCE") ||
            name.contains("JUNGLE") ||
            name.contains("ACACIA") ||
            name.contains("DARK_OAK") ||
            name.contains("MANGROVE") ||
            name.contains("CHERRY") ||
            name.contains("PLANKS") ||
            name.contains("LOG") ||
            name.contains("WOOD")
        ) {
            return 0.2;
        }

        // Wool and dyes
        if (name.contains("WOOL") || name.contains("DYE")) return 0.3;

        // Concrete and terracotta
        if (
            name.contains("CONCRETE") || name.contains("TERRACOTTA")
        ) return 0.3;

        // Glass types
        if (name.contains("GLASS")) return 0.2;

        // Ores
        if (name.contains("ORE")) return 2.0;

        // Potions
        if (name.contains("POTION")) return 5.0;

        // Spawn eggs
        if (name.contains("SPAWN_EGG")) return 50.0;

        // Banners
        if (name.contains("BANNER")) return 1.0;

        // Shulker boxes
        if (name.contains("SHULKER_BOX")) return 100.0;

        // Default fallback - small value for anything else
        return 0.1;
    }

    // --- Command Executor ---
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

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("hand")) {
                sellHandItem(player);
            } else {
                player.sendMessage(ChatColor.YELLOW + "Please use the sell GUI to sell items!");
                return true;
            }
            return true;
        }

        // Open sell GUI
        player.openInventory(createSellGUI());
        return true;
    }

    // --- GUI Creation Logic (New and Improved) ---
    private Inventory createSellGUI() {
        Inventory sellGUI = Bukkit.createInventory(
            null,
            INVENTORY_SIZE,
            GUI_TITLE
        );

        // Add sell all button
        ItemStack sellAllButton = createGuiItem(
            Material.EMERALD_BLOCK,
            ChatColor.GREEN + "" + ChatColor.BOLD + "SELL ALL ITEMS",
            ChatColor.GRAY + "Click to sell all sellable items",
            ChatColor.GRAY + "currently placed in the chest slots!",
            "",
            ChatColor.YELLOW + "Drop items above, then click."
        );
        sellGUI.setItem(SELL_ALL_SLOT, sellAllButton);

        // Add filler
        ItemStack filler = createGuiItem(FILLER_MATERIAL, " ", "");
        for (int i = 45; i < INVENTORY_SIZE; i++) {
            if (i != SELL_ALL_SLOT) {
                sellGUI.setItem(i, filler);
            }
        }

        // Add a visual divider/info panel above the sell button (slots 48 and 50)
        ItemStack infoPanel = createGuiItem(
            Material.GREEN_STAINED_GLASS_PANE,
            ChatColor.AQUA + "Drop Area",
            ChatColor.GRAY + "Place items in the top 45 slots",
            ChatColor.GRAY + "to sell them."
        );
        sellGUI.setItem(48, infoPanel);
        sellGUI.setItem(50, infoPanel);

        return sellGUI;
    }

    // Utility method for cleaner item creation
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

    // --- Sell Logic Methods (Consolidated/Simplified) ---

    private void performSell(
        Player player,
        ItemStack item,
        Inventory inv,
        boolean isGUI
    ) {
        double pricePerItem = getItemPrice(item);

        if (pricePerItem <= 0) {
            player.sendMessage(ChatColor.RED + "This item cannot be sold!");
            return;
        }

        int amount = item.getAmount();
        double totalPrice = pricePerItem * amount;

        double multiplier = levelingManager.getSellMultiplier(player);
        double finalPrice = totalPrice * multiplier;
        
        economyManager.addBalance(player.getUniqueId(), finalPrice);
        levelingManager.addSellXp(player, finalPrice);

        // Remove item from inventory (either player or GUI)
        if (isGUI) {
            inv.removeItem(item);
        } else {
            // For /sell hand or /sell all (player inventory)
            if (inv.equals(player.getInventory())) {
                player.getInventory().setItemInMainHand(null);
            } else {
                inv.removeItem(item);
            }
        }

        String itemName = item.getType().name().replace("_", " ").toLowerCase();
        if (
            item.hasItemMeta() &&
            item.getItemMeta() != null &&
            item.getItemMeta().hasDisplayName()
        ) {
            itemName = ChatColor.stripColor(
                item.getItemMeta().getDisplayName()
            ); // Strip colors for message clarity
        }

        player.sendMessage(
            ChatColor.GREEN +
                "Sold " +
                amount +
                "x " +
                itemName +
                " for $" +
                String.format("%.2f", finalPrice) +
                (multiplier > 1.0 ? " §e(+" + String.format("%.0f", (multiplier - 1.0) * 100) + "% bonus)" : "")
        );
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            1.0f,
            1.0f
        );
    }

    private void sellHandItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You're not holding anything!");
            return;
        }

        // The core logic is now in performSell
        performSell(player, item, player.getInventory(), false);
    }

    private void sellAllFromInventory(Player player) {
        double totalEarned = 0.0;
        int itemsSold = 0;
        Inventory playerInventory = player.getInventory();

        // Iterate through all slots, skipping armor, off-hand, and creative/hotbar if needed,
        // but for a simple /sell all, iterating all contents is fine.
        ItemStack[] contents = playerInventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;

            double pricePerItem = getItemPrice(item);
            if (pricePerItem > 0) {
                int amount = item.getAmount();
                double price = pricePerItem * amount;
                totalEarned += price;
                itemsSold += amount;
                playerInventory.setItem(i, null); // Remove item
            }
        }

        if (itemsSold > 0) {
            economyManager.addBalance(player.getUniqueId(), totalEarned);
            player.sendMessage(
                ChatColor.GREEN +
                    "Sold " +
                    itemsSold +
                    " items for $" +
                    String.format("%.2f", totalEarned)
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                1.0f,
                1.0f
            );
        } else {
            player.sendMessage(
                ChatColor.RED + "No sellable items found in your inventory!"
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_VILLAGER_NO,
                1.0f,
                1.0f
            );
        }
    }

    private void sellAllFromGUI(Player player, Inventory sellGUI) {
        double totalEarned = 0.0;
        int itemsSold = 0;
        List<Integer> slotsToRemove = new ArrayList<>();

        // Only check the top 45 slots (the chest area)
        for (int i = 0; i < 45; i++) {
            ItemStack item = sellGUI.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            double pricePerItem = getItemPrice(item);
            if (pricePerItem > 0) {
                int amount = item.getAmount();
                double price = pricePerItem * amount;
                totalEarned += price;
                itemsSold += amount;
                slotsToRemove.add(i);
            }
        }

        // Remove sold items
        for (int slot : slotsToRemove) {
            sellGUI.setItem(slot, null);
        }

        if (itemsSold > 0) {
            double multiplier = levelingManager.getSellMultiplier(player);
            double finalPrice = totalEarned * multiplier;
            
            economyManager.addBalance(player.getUniqueId(), finalPrice);
            levelingManager.addSellXp(player, finalPrice);
            
            player.sendMessage(
                ChatColor.GREEN +
                    "Sold " +
                    itemsSold +
                    " items for $" +
                    String.format("%.2f", finalPrice) +
                    (multiplier > 1.0 ? " §e(+" + String.format("%.0f", (multiplier - 1.0) * 100) + "% bonus)" : "")
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                1.0f,
                1.0f
            );
        } else {
            player.sendMessage(
                ChatColor.RED + "No sellable items in the drop area!"
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_VILLAGER_NO,
                1.0f,
                1.0f
            );
        }
    }

    // --- Event Handlers (Cleaner) ---

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) return;
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        Player player = (Player) clicker;
        ItemStack clicked = event.getCurrentItem();
        int slot = event.getRawSlot();

        // 1. Prevent moving filler items or command buttons
        if (slot >= 45 && slot < INVENTORY_SIZE) {
            event.setCancelled(true);
        }

        // 2. Handle sell all button click
        if (slot == SELL_ALL_SLOT) {
            event.setCancelled(true);
            if (
                clicked != null && clicked.getType() == Material.EMERALD_BLOCK
            ) {
                sellAllFromGUI(player, event.getInventory());
            }
            return;
        }

        // 3. Handle clicking an item in the drop area (top 45 slots) to sell it individually
        if (slot < 45 && clicked != null && clicked.getType() != Material.AIR) {
            // Check if the click action is one that would sell the item
            if (event.isLeftClick() || event.isRightClick()) {
                event.setCancelled(true);
                performSell(player, clicked, event.getInventory(), true);
            }
        }

        // 4. Prevent users from taking items out of the GUI (except to shift-click into their inventory which is handled by default)
        // If the click is in the top inventory (the sell area)
        if (
            event.getClickedInventory() != null &&
            event.getClickedInventory().equals(event.getInventory())
        ) {
            // Cancel taking items out of the drop area if not shift-clicking to their inventory
            if (event.isShiftClick() && event.getRawSlot() < 45) {
                // Allow shift-click into player inventory (handled by bukkit default) - do nothing
            } else if (
                event.getAction().toString().contains("PICKUP") ||
                event.getAction().toString().contains("COLLECT_TO_CURSOR")
            ) {
                // Prevent normal click pickup from the drop area
                if (event.getRawSlot() < 45) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();
        UUID playerUUID = player.getUniqueId();

        // Check for any remaining items in the drop area (slots 0-44)
        for (int i = 0; i < 45; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                // Return unsold items to player
                HashMap<Integer, ItemStack> leftover = player
                    .getInventory()
                    .addItem(item);

                // Drop items that don't fit
                for (ItemStack drop : leftover.values()) {
                    player
                        .getWorld()
                        .dropItemNaturally(player.getLocation(), drop);
                }
                inv.setItem(i, null); // Clear the slot in the sell GUI
            }
        }
    }
}
