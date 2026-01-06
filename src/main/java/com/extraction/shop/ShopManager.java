package com.extraction.shop;

import com.extraction.ExtractionPlugin;
import com.extraction.economy.EconomyManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitScheduler;

public class ShopManager {

    private final ExtractionPlugin plugin;
    private final EconomyManager economyManager;
    private final Map<ItemStack, Integer> mainItems = new LinkedHashMap<>();
    private final Map<ItemStack, Integer> trapItems = new LinkedHashMap<>();
    private final Map<ItemStack, Integer> foodItems = new LinkedHashMap<>();
    private final Map<ItemStack, Integer> buildingItems = new LinkedHashMap<>();
    private final Map<ItemStack, Integer> weaponsItems = new LinkedHashMap<>();
    private final Map<ItemStack, Integer> specialItems = new LinkedHashMap<>();
    
    private final Map<ItemStack, Integer> mainItemsStock = new ConcurrentHashMap<>();
    private final Map<ItemStack, Integer> trapItemsStock = new ConcurrentHashMap<>();
    private final Map<ItemStack, Integer> foodItemsStock = new ConcurrentHashMap<>();
    private final Map<ItemStack, Integer> buildingItemsStock = new ConcurrentHashMap<>();
    private final Map<ItemStack, Integer> weaponsItemsStock = new ConcurrentHashMap<>();
    private final Map<ItemStack, Integer> specialItemsStock = new ConcurrentHashMap<>();
    
    private final Random random = new Random();

    public ShopManager(ExtractionPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        setupShopItems();
        setupWeaponsItems();
        setupSpecialItems();
        initializeStock();
        scheduleStockReset();
    }

    private void setupShopItems() {
        // ============ PAGE 1: MAIN ITEMS ============

        // Armor - Leather - **CHEAP STARTER GEAR**
        mainItems.put(new ItemStack(Material.LEATHER_HELMET), 11);
        mainItems.put(new ItemStack(Material.LEATHER_CHESTPLATE), 17);
        mainItems.put(new ItemStack(Material.LEATHER_LEGGINGS), 14);
        mainItems.put(new ItemStack(Material.LEATHER_BOOTS), 11);

        // Armor - Gold - **CHEAPER MEDIOCRE GEAR**
        mainItems.put(new ItemStack(Material.GOLDEN_HELMET), 22);
        mainItems.put(new ItemStack(Material.GOLDEN_CHESTPLATE), 35);
        mainItems.put(new ItemStack(Material.GOLDEN_LEGGINGS), 28);
        mainItems.put(new ItemStack(Material.GOLDEN_BOOTS), 22);

        // Armor - Chainmail - **MEDIOCRE GEAR**
        mainItems.put(new ItemStack(Material.CHAINMAIL_HELMET), 38);
        mainItems.put(new ItemStack(Material.CHAINMAIL_CHESTPLATE), 61);
        mainItems.put(new ItemStack(Material.CHAINMAIL_LEGGINGS), 54);
        mainItems.put(new ItemStack(Material.CHAINMAIL_BOOTS), 38);

        // Armor - Iron
        mainItems.put(new ItemStack(Material.IRON_HELMET), 55);
        mainItems.put(new ItemStack(Material.IRON_CHESTPLATE), 90);
        mainItems.put(new ItemStack(Material.IRON_LEGGINGS), 75);
        mainItems.put(new ItemStack(Material.IRON_BOOTS), 55);

        // Armor - Diamond
        mainItems.put(new ItemStack(Material.DIAMOND_HELMET), 100);
        mainItems.put(new ItemStack(Material.DIAMOND_CHESTPLATE), 250);
        mainItems.put(new ItemStack(Material.DIAMOND_LEGGINGS), 200);
        mainItems.put(new ItemStack(Material.DIAMOND_BOOTS), 100);

        // Armor - Netherite - **VERY HIGH PRICE INCREASE (40k requested)**
        mainItems.put(new ItemStack(Material.NETHERITE_HELMET), 40001); // 40k+ by the cent (40,001)
        mainItems.put(new ItemStack(Material.NETHERITE_CHESTPLATE), 75005);
        mainItems.put(new ItemStack(Material.NETHERITE_LEGGINGS), 65003);
        mainItems.put(new ItemStack(Material.NETHERITE_BOOTS), 40002);

        // Weapons - Swords
        mainItems.put(new ItemStack(Material.WOODEN_SWORD), 5); // Cheaper shit
        mainItems.put(new ItemStack(Material.STONE_SWORD), 12);
        mainItems.put(new ItemStack(Material.IRON_SWORD), 50);
        mainItems.put(new ItemStack(Material.DIAMOND_SWORD), 150);
        mainItems.put(new ItemStack(Material.NETHERITE_SWORD), 45007); // **VERY HIGH PRICE INCREASE**

        // Weapons - Ranged
        mainItems.put(new ItemStack(Material.BOW), 97); // Realistic price
        mainItems.put(new ItemStack(Material.CROSSBOW), 125);
        mainItems.put(new ItemStack(Material.ARROW, 16), 15);
        mainItems.put(new ItemStack(Material.ARROW, 32), 27); // Realistic price
        mainItems.put(new ItemStack(Material.ARROW, 64), 49);

        // Tools
        mainItems.put(new ItemStack(Material.WOODEN_PICKAXE), 4); // Cheaper shit
        mainItems.put(new ItemStack(Material.STONE_PICKAXE), 11);
        mainItems.put(new ItemStack(Material.IRON_PICKAXE), 40);
        mainItems.put(new ItemStack(Material.DIAMOND_PICKAXE), 120);
        mainItems.put(new ItemStack(Material.NETHERITE_PICKAXE), 40006); // **VERY HIGH PRICE INCREASE**
        mainItems.put(new ItemStack(Material.WOODEN_AXE), 4); // Cheaper shit
        mainItems.put(new ItemStack(Material.STONE_AXE), 11);
        mainItems.put(new ItemStack(Material.IRON_AXE), 40);
        mainItems.put(new ItemStack(Material.DIAMOND_AXE), 120);
        mainItems.put(new ItemStack(Material.NETHERITE_AXE), 40008); // **VERY HIGH PRICE INCREASE**
        mainItems.put(new ItemStack(Material.SHEARS), 11); // Realistic price

        // Special Items
        mainItems.put(new ItemStack(Material.ELYTRA), 10000); // High price increase
        mainItems.put(new ItemStack(Material.ENDER_PEARL, 4), 63); // Realistic price
        mainItems.put(new ItemStack(Material.ENDER_PEARL, 8), 115); // Realistic price
        mainItems.put(new ItemStack(Material.ENDER_PEARL, 16), 200);
        mainItems.put(new ItemStack(Material.FIREWORK_ROCKET, 16), 44);
        mainItems.put(new ItemStack(Material.FIREWORK_ROCKET, 64), 143); // Realistic price

        // Combat Utilities
        mainItems.put(new ItemStack(Material.SHIELD), 45);
        mainItems.put(new ItemStack(Material.TRIDENT), 243); // Realistic price
        mainItems.put(new ItemStack(Material.TOTEM_OF_UNDYING), 1500); // High price increase

        // Utility
        mainItems.put(new ItemStack(Material.COMPASS), 22); // Realistic price
        mainItems.put(new ItemStack(Material.FLINT_AND_STEEL), 17); // Realistic price
        mainItems.put(new ItemStack(Material.FISHING_ROD), 25);

        // ============ PAGE 2: TRAPS ============

        // Redstone Components
        trapItems.put(new ItemStack(Material.REDSTONE, 32), 16); // Realistic price
        trapItems.put(new ItemStack(Material.REDSTONE, 64), 28); // Realistic price
        trapItems.put(new ItemStack(Material.REDSTONE_TORCH, 16), 12); // Realistic price
        trapItems.put(new ItemStack(Material.REDSTONE_TORCH, 32), 20);
        trapItems.put(new ItemStack(Material.REDSTONE_BLOCK, 8), 35);
        trapItems.put(new ItemStack(Material.REDSTONE_BLOCK, 16), 60);
        trapItems.put(new ItemStack(Material.REDSTONE_LAMP, 4), 27); // Realistic price
        trapItems.put(new ItemStack(Material.REDSTONE_LAMP, 8), 50);

        // Buttons & Levers
        trapItems.put(new ItemStack(Material.LEVER, 8), 7);
        trapItems.put(new ItemStack(Material.LEVER, 16), 10);
        trapItems.put(new ItemStack(Material.STONE_BUTTON, 8), 5);
        trapItems.put(new ItemStack(Material.STONE_BUTTON, 16), 7);
        trapItems.put(new ItemStack(Material.OAK_BUTTON, 8), 5);

        // Pressure Plates
        trapItems.put(new ItemStack(Material.STONE_PRESSURE_PLATE, 8), 6);
        trapItems.put(
            new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, 4),
            10
        );
        trapItems.put(
            new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, 4),
            10
        );
        trapItems.put(new ItemStack(Material.OAK_PRESSURE_PLATE, 8), 5);

        // Detectors
        trapItems.put(new ItemStack(Material.TRIPWIRE_HOOK, 4), 10);
        trapItems.put(new ItemStack(Material.TRIPWIRE_HOOK, 8), 18);
        trapItems.put(new ItemStack(Material.STRING, 16), 7);
        trapItems.put(new ItemStack(Material.STRING, 32), 11);
        trapItems.put(new ItemStack(Material.TARGET, 8), 15);
        trapItems.put(new ItemStack(Material.OBSERVER, 4), 38);
        trapItems.put(new ItemStack(Material.OBSERVER, 8), 70);
        trapItems.put(new ItemStack(Material.COMPARATOR, 4), 28);
        trapItems.put(new ItemStack(Material.REPEATER, 8), 17);
        trapItems.put(new ItemStack(Material.REPEATER, 16), 30);

        // Pistons
        trapItems.put(new ItemStack(Material.PISTON, 8), 22);
        trapItems.put(new ItemStack(Material.PISTON, 16), 40);
        trapItems.put(new ItemStack(Material.STICKY_PISTON, 4), 27);
        trapItems.put(new ItemStack(Material.STICKY_PISTON, 8), 50);

        // Dispensers & Hoppers
        trapItems.put(new ItemStack(Material.DISPENSER, 4), 33);
        trapItems.put(new ItemStack(Material.DISPENSER, 8), 60);
        trapItems.put(new ItemStack(Material.DROPPER, 4), 22);
        trapItems.put(new ItemStack(Material.DROPPER, 8), 40);
        trapItems.put(new ItemStack(Material.HOPPER, 4), 45);
        trapItems.put(new ItemStack(Material.HOPPER, 8), 80);

        // TNT
        trapItems.put(new ItemStack(Material.TNT, 8), 35);
        trapItems.put(new ItemStack(Material.TNT, 16), 60);
        trapItems.put(new ItemStack(Material.TNT, 32), 110);

        // Rails
        trapItems.put(new ItemStack(Material.RAIL, 16), 10);
        trapItems.put(new ItemStack(Material.POWERED_RAIL, 8), 25);
        trapItems.put(new ItemStack(Material.DETECTOR_RAIL, 8), 17);
        trapItems.put(new ItemStack(Material.ACTIVATOR_RAIL, 8), 20);

        // ============ PAGE 3: FOOD (RAW) ============

        // Raw Meats
        foodItems.put(new ItemStack(Material.BEEF, 8), 12);
        foodItems.put(new ItemStack(Material.BEEF, 16), 20);
        foodItems.put(new ItemStack(Material.BEEF, 32), 35);
        foodItems.put(new ItemStack(Material.PORKCHOP, 8), 12);
        foodItems.put(new ItemStack(Material.PORKCHOP, 16), 20);
        foodItems.put(new ItemStack(Material.PORKCHOP, 32), 35);
        foodItems.put(new ItemStack(Material.CHICKEN, 8), 10);
        foodItems.put(new ItemStack(Material.CHICKEN, 16), 16);
        foodItems.put(new ItemStack(Material.CHICKEN, 32), 28);
        foodItems.put(new ItemStack(Material.MUTTON, 8), 10);
        foodItems.put(new ItemStack(Material.MUTTON, 16), 16);
        foodItems.put(new ItemStack(Material.MUTTON, 32), 28);
        foodItems.put(new ItemStack(Material.RABBIT, 8), 14);
        foodItems.put(new ItemStack(Material.RABBIT, 16), 25);
        foodItems.put(new ItemStack(Material.COD, 8), 8);
        foodItems.put(new ItemStack(Material.COD, 16), 13);
        foodItems.put(new ItemStack(Material.SALMON, 8), 10);
        foodItems.put(new ItemStack(Material.SALMON, 16), 16);

        // Vegetables & Crops
        foodItems.put(new ItemStack(Material.CARROT, 16), 9);
        foodItems.put(new ItemStack(Material.CARROT, 32), 16);
        foodItems.put(new ItemStack(Material.POTATO, 16), 9);
        foodItems.put(new ItemStack(Material.POTATO, 32), 16);
        foodItems.put(new ItemStack(Material.BEETROOT, 16), 11);
        foodItems.put(new ItemStack(Material.BEETROOT, 32), 20);
        foodItems.put(new ItemStack(Material.WHEAT, 16), 7);
        foodItems.put(new ItemStack(Material.WHEAT, 32), 12);

        // Fruits
        foodItems.put(new ItemStack(Material.APPLE, 8), 12);
        foodItems.put(new ItemStack(Material.APPLE, 16), 20);
        foodItems.put(new ItemStack(Material.MELON_SLICE, 16), 9);
        foodItems.put(new ItemStack(Material.MELON_SLICE, 32), 16);
        foodItems.put(new ItemStack(Material.SWEET_BERRIES, 16), 9);
        foodItems.put(new ItemStack(Material.SWEET_BERRIES, 32), 16);
        foodItems.put(new ItemStack(Material.GLOW_BERRIES, 16), 14);

        // Special Food - **HIGH PRICE INCREASE**
        foodItems.put(new ItemStack(Material.GOLDEN_CARROT, 8), 55);
        foodItems.put(new ItemStack(Material.GOLDEN_CARROT, 16), 95);
        foodItems.put(new ItemStack(Material.GOLDEN_APPLE, 1), 150);
        foodItems.put(new ItemStack(Material.GOLDEN_APPLE, 4), 550);
        foodItems.put(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1), 2500);
        foodItems.put(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2), 4800);

        // Other
        foodItems.put(new ItemStack(Material.EGG, 16), 11);
        foodItems.put(new ItemStack(Material.MILK_BUCKET), 17);
        foodItems.put(new ItemStack(Material.HONEY_BOTTLE, 4), 22);
        foodItems.put(new ItemStack(Material.SUGAR, 16), 7);

        // ============ PAGE 4: BUILDING & STORAGE ============

        // Wood Planks
        buildingItems.put(new ItemStack(Material.OAK_PLANKS, 64), 12);
        buildingItems.put(new ItemStack(Material.SPRUCE_PLANKS, 64), 12);
        buildingItems.put(new ItemStack(Material.BIRCH_PLANKS, 64), 12);
        buildingItems.put(new ItemStack(Material.JUNGLE_PLANKS, 64), 12);
        buildingItems.put(new ItemStack(Material.ACACIA_PLANKS, 64), 12);
        buildingItems.put(new ItemStack(Material.DARK_OAK_PLANKS, 64), 12);

        // Stone & Brick
        buildingItems.put(new ItemStack(Material.COBBLESTONE, 64), 10);
        buildingItems.put(new ItemStack(Material.STONE, 64), 14);
        buildingItems.put(new ItemStack(Material.STONE_BRICKS, 64), 17);
        buildingItems.put(new ItemStack(Material.BRICK, 64), 20);
        buildingItems.put(new ItemStack(Material.DEEPSLATE, 64), 17);
        buildingItems.put(new ItemStack(Material.DEEPSLATE_BRICKS, 64), 22);

        // Special Blocks
        buildingItems.put(new ItemStack(Material.OBSIDIAN, 16), 45);
        buildingItems.put(new ItemStack(Material.OBSIDIAN, 32), 85);
        buildingItems.put(new ItemStack(Material.GLASS, 32), 10);
        buildingItems.put(new ItemStack(Material.GLASS, 64), 18);
        buildingItems.put(new ItemStack(Material.GLASS_PANE, 32), 8);
        buildingItems.put(new ItemStack(Material.GLASS_PANE, 64), 13);

        // Utility Blocks
        buildingItems.put(new ItemStack(Material.LADDER, 16), 7);
        buildingItems.put(new ItemStack(Material.LADDER, 32), 11);
        buildingItems.put(new ItemStack(Material.TORCH, 32), 6);
        buildingItems.put(new ItemStack(Material.TORCH, 64), 9);
        buildingItems.put(new ItemStack(Material.LANTERN, 8), 14);
        buildingItems.put(new ItemStack(Material.CRAFTING_TABLE), 7);
        buildingItems.put(new ItemStack(Material.FURNACE), 10);
        buildingItems.put(new ItemStack(Material.BLAST_FURNACE), 30);
        buildingItems.put(new ItemStack(Material.SMOKER), 25);
        buildingItems.put(new ItemStack(Material.CAMPFIRE), 10);
        buildingItems.put(new ItemStack(Material.ANVIL), 95);

        // Storage - **HIGH PRICE INCREASE**
        buildingItems.put(new ItemStack(Material.CHEST), 17);
        buildingItems.put(new ItemStack(Material.TRAPPED_CHEST), 28);
        buildingItems.put(new ItemStack(Material.BARREL), 15);
        buildingItems.put(new ItemStack(Material.ENDER_CHEST), 300); // High price increase
        buildingItems.put(new ItemStack(Material.SHULKER_BOX), 500); // High price increase
        buildingItems.put(new ItemStack(Material.RED_SHULKER_BOX), 600); // High price increase
        buildingItems.put(new ItemStack(Material.BLUE_SHULKER_BOX), 600); // High price increase
        buildingItems.put(new ItemStack(Material.GREEN_SHULKER_BOX), 600); // High price increase
        buildingItems.put(new ItemStack(Material.YELLOW_SHULKER_BOX), 600); // High price increase
        buildingItems.put(new ItemStack(Material.BLACK_SHULKER_BOX), 600); // High price increase
        buildingItems.put(new ItemStack(Material.WHITE_SHULKER_BOX), 600); // High price increase
    }

    private void setupWeaponsItems() {
        // ============ PAGE 6: WEAPONS ============
        
        weaponsItems.put(createAutomaticBow(), 8500); // High price for auto weapon
        weaponsItems.put(createBurstCrossbow(), 12000); // Very high price for burst weapon
        weaponsItems.put(createRicochetBow(), 6500); // High price for ricochet weapon
        
        // Add some traditional weapons with enhancements
        weaponsItems.put(createEnchantedDiamondSword(), 7500); // Premium sword
        weaponsItems.put(createEnchantedDiamondBow(), 4800); // Premium bow
        weaponsItems.put(createExplosiveArrows(32), 3200); // Special ammunition
        weaponsItems.put(createPoisonDaggers(1), 4100); // Stealth weapon (fixed quantity)
        
        // Add new weapons
        weaponsItems.put(createFlamethrower(), 8900); // Close range weapon
        weaponsItems.put(createRocketLauncher(), 15000); // Heavy weapon
        weaponsItems.put(createLightningStaff(), 11000); // Magic weapon
        weaponsItems.put(createFreezeGun(), 9500); // Crowd control weapon
        weaponsItems.put(createShotgun(), 7200); // Close range spread weapon
    }

    public Map<ItemStack, Integer> getItemsForPage(int page) {
        switch (page) {
            case 1:
                return mainItems;
            case 2:
                return trapItems;
            case 3:
                return foodItems;
            case 4:
                return buildingItems;
            case 5:
                return weaponsItems;
            case 6:
                return specialItems;
            default:
                return mainItems;
        }
    }

    public String getPageTitle(int page) {
        switch (page) {
            case 1:
                return "Extraction Shop - Main";
            case 2:
                return "Extraction Shop - Traps";
            case 3:
                return "Extraction Shop - Food";
            case 4:
                return "Extraction Shop - Building & Storage";
            case 5:
                return "Extraction Shop - Weapons";
            case 6:
                return "Extraction Shop - Special Items";
            default:
                return "Extraction Shop";
        }
    }

    private void setupSpecialItems() {
        // ============ TACTICAL ITEMS ============
        specialItems.put(createSmokeBomb(), 1250); // Higher price
        specialItems.put(createGrapplingHook(), 1750); // Higher price
        specialItems.put(createTrackerCompass(), 2300); // Higher price
        specialItems.put(createMedKit(), 1100); // Higher price
        specialItems.put(createAmmoBox(), 1600); // Higher price
        specialItems.put(createJetpack(), 6000); // Higher price
        specialItems.put(createLandmine(), 950); // Higher price
        specialItems.put(createAdrenalineShot(), 1400); // Higher price
        specialItems.put(createSpeedPowder(), 3499); // Realistic price
        specialItems.put(createInvisibilityCloak(), 9999); // Realistic price
        specialItems.put(createReviveSyringe(), 5500); // Higher price
        specialItems.put(createEMPGrenade(), 2850); // Higher price
        specialItems.put(createMoneyPrinter(), 150000); // **VERY HIGH PRICE INCREASE**

        // ============ EXTRACTION ITEMS ============
        specialItems.put(createExtractionBanner(), 75000); // Higher price
        specialItems.put(createExtractionFlare(), 35000); // Higher price
        specialItems.put(createEmergencyTeleporter(), 99000); // Realistic price

        // ============ NEW CUSTOM ITEMS ============
        specialItems.put(createPlasmaRifle(), 12500); // New high-tech weapon
        specialItems.put(createQuantumShield(), 8800); // Defensive equipment
        specialItems.put(createNanoMedKit(), 3200); // Advanced healing
        specialItems.put(createCryptoWallet(), 5000); // Cryptocurrency wallet
    }

    public ItemStack createExtractionBanner() {
        ItemStack banner = new ItemStack(Material.WHITE_BANNER);
        ItemMeta meta = banner.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Easy Extraction Banner");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Place and break to extract!");
            lore.add(ChatColor.GRAY + "Works anywhere on the map!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $40,000");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "extraction_banner");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            banner.setItemMeta(meta);
        }
        return banner;
    }

    public ItemStack createExtractionFlare() {
        ItemStack flare = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = flare.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Extraction Flare");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click to signal extraction!");
            lore.add(ChatColor.GRAY + "Faster extraction (10 seconds)");
            lore.add(ChatColor.GRAY + "Works anywhere on the map!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $50,000");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "extraction_flare");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            flare.setItemMeta(meta);
        }
        return flare;
    }

    public ItemStack createEmergencyTeleporter() {
        ItemStack teleporter = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = teleporter.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(
                ChatColor.LIGHT_PURPLE + "Emergency Teleporter"
            );
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click for INSTANT extraction!");
            lore.add(ChatColor.GRAY + "No waiting, immediate teleport!");
            lore.add(ChatColor.GRAY + "Works anywhere on the map!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $30,000S");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(
                plugin,
                "emergency_teleporter"
            );
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            teleporter.setItemMeta(meta);
        }
        return teleporter;
    }

    public boolean buyItem(Player player, ItemStack item, int price) {
        if (!hasStock(item)) {
            return false;
        }
        
        if (economyManager.takeBalance(player.getUniqueId(), price)) {
            if (decreaseStock(item)) {
                player.getInventory().addItem(item.clone());
                return true;
            } else {
                economyManager.addBalance(player.getUniqueId(), price);
                return false;
            }
        }
        return false;
    }

    public ExtractionPlugin getPlugin() {
        return plugin;
    }

    private void initializeStock() {
        generateStockForItems(mainItems, mainItemsStock);
        generateStockForItems(trapItems, trapItemsStock);
        generateStockForItems(foodItems, foodItemsStock);
        generateStockForItems(buildingItems, buildingItemsStock);
        generateStockForItems(weaponsItems, weaponsItemsStock);
        generateStockForItems(specialItems, specialItemsStock);
    }

    private void generateStockForItems(Map<ItemStack, Integer> items, Map<ItemStack, Integer> stockMap) {
        for (Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
            ItemStack item = entry.getKey();
            int stock = generateRandomStock(item);
            stockMap.put(item, stock);
        }
    }

    private int generateRandomStock(ItemStack item) {
        Material material = item.getType();
        
        if (material.name().contains("NETHERITE") || material == Material.NETHERITE_SCRAP) {
            return random.nextInt(3) + 1;
        }
        
        if (material == Material.NETHERRACK) {
            return random.nextInt(8) + 1;
        }
        
        if (material.name().contains("DIAMOND") || material == Material.DIAMOND_BLOCK) {
            return random.nextInt(15) + 3;
        }
        
        if (material.name().contains("GOLDEN_APPLE") || material.name().contains("ENCHANTED_GOLDEN_APPLE")) {
            return random.nextInt(8) + 2;
        }
        
        if (material == Material.ELYTRA || material == Material.TOTEM_OF_UNDYING || 
            material.name().contains("SHULKER_BOX") || material == Material.ENDER_CHEST) {
            return random.nextInt(12) + 3;
        }
        
        return random.nextInt(44) + 6;
    }

    public void resetStock() {
        initializeStock();
    }

    private void scheduleStockReset() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, this::resetStock, 20L * 3600L, 20L * 3600L);
    }

    public int getStockForItem(ItemStack item) {
        Integer stock = mainItemsStock.get(item);
        if (stock != null) return stock;
        
        stock = trapItemsStock.get(item);
        if (stock != null) return stock;
        
        stock = foodItemsStock.get(item);
        if (stock != null) return stock;
        
stock = buildingItemsStock.get(item);
        if (stock != null) return stock;
        
        stock = weaponsItemsStock.get(item);
        if (stock != null) return stock;
        
        stock = specialItemsStock.get(item);
        if (stock != null) return stock;
        
        stock = specialItemsStock.get(item);
        if (stock != null) return stock;
        
        return 0;
    }

    public boolean decreaseStock(ItemStack item) {
        Integer stock = mainItemsStock.get(item);
        if (stock != null && stock > 0) {
            mainItemsStock.put(item, stock - 1);
            return true;
        }
        
        stock = trapItemsStock.get(item);
        if (stock != null && stock > 0) {
            trapItemsStock.put(item, stock - 1);
            return true;
        }
        
        stock = foodItemsStock.get(item);
        if (stock != null && stock > 0) {
            foodItemsStock.put(item, stock - 1);
            return true;
        }
        
        stock = buildingItemsStock.get(item);
        if (stock != null && stock > 0) {
            buildingItemsStock.put(item, stock - 1);
            return true;
        }
        
        stock = weaponsItemsStock.get(item);
        if (stock != null && stock > 0) {
            weaponsItemsStock.put(item, stock - 1);
            return true;
        }
        
        stock = specialItemsStock.get(item);
        if (stock != null && stock > 0) {
            specialItemsStock.put(item, stock - 1);
            return true;
        }
        
        return false;
    }

    public boolean hasStock(ItemStack item) {
        return getStockForItem(item) > 0;
    }

    // Cheaper special items (1k-2k range)
    private ItemStack createSmokeBomb() {
        ItemStack item = new ItemStack(Material.FIREWORK_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "Smoke Bomb");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Creates a smoke cloud on impact");
            lore.add(ChatColor.GRAY + "Use for tactical retreats!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $300");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "smoke_bomb");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGrapplingHook() {
        ItemStack item = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Grappling Hook");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Enhanced fishing rod");
            lore.add(ChatColor.GRAY + "Pull yourself to locations!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $450");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "grappling_hook");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createTrackerCompass() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Tracker Compass");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Points to nearest player");
            lore.add(ChatColor.GRAY + "Hunt your enemies!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $600");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "tracker_compass");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createMedKit() {
        ItemStack item = new ItemStack(Material.RED_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Med Kit");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click to heal 5 hearts");
            lore.add(ChatColor.GRAY + "One-time use emergency healing!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $300");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "med_kit");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createAmmoBox() {
        ItemStack item = new ItemStack(Material.ARROW, 64);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "Ammo Box");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "64 high-quality arrows");
            lore.add(ChatColor.GRAY + "Bulk ammunition supply!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $450");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "ammo_box");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ============ NEW CUSTOM ITEMS ============

    public ItemStack createMoneyPrinter() {
        ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(
                ChatColor.GREEN + "" + ChatColor.BOLD + "Money Printer"
            );
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Place this block to print money!");
            lore.add(ChatColor.GRAY + "Generates $50 every 30 seconds");
            lore.add("");
            lore.add(ChatColor.RED + "WARNING: Disappears if broken!");
            lore.add(ChatColor.RED + "Must stay near the printer");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $30000");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "money_printer");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createJetpack() {
        ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Jetpack");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click while falling to boost!");
            lore.add(ChatColor.GRAY + "3 uses per jetpack");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Uses remaining: 3");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $1500");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "jetpack");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            NamespacedKey usesKey = new NamespacedKey(plugin, "jetpack_uses");
            container.set(usesKey, PersistentDataType.INTEGER, 3);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createLandmine() {
        ItemStack item = new ItemStack(Material.STONE_PRESSURE_PLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Landmine");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Place on ground as a trap");
            lore.add(ChatColor.GRAY + "Explodes when stepped on!");
            lore.add("");
            lore.add(ChatColor.RED + "Does not harm the placer");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $240");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "landmine");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createAdrenalineShot() {
        ItemStack item = new ItemStack(Material.YELLOW_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Adrenaline Shot");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click for a combat boost!");
            lore.add(ChatColor.GRAY + "Speed II, Strength I, Jump Boost");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Duration: 15 seconds");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $360");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "adrenaline_shot");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSpeedPowder() {
        ItemStack item = new ItemStack(Material.SUGAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(
                ChatColor.WHITE + "" + ChatColor.BOLD + "Speed Powder"
            );
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A mysterious white powder...");
            lore.add(ChatColor.GRAY + "Grants EXTREME speed!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Effects (20 seconds):");
            lore.add(ChatColor.WHITE + "• Speed III");
            lore.add(ChatColor.WHITE + "• Jump Boost II");
            lore.add(ChatColor.WHITE + "• Haste II");
            lore.add("");
            lore.add(ChatColor.RED + "Side effects may occur...");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $900");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "speed_powder");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInvisibilityCloak() {
        ItemStack item = new ItemStack(Material.WHITE_BANNER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(
                ChatColor.LIGHT_PURPLE +
                    "" +
                    ChatColor.BOLD +
                    "Invisibility Cloak"
            );
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Wrap yourself in shadows");
            lore.add(ChatColor.GRAY + "Become completely invisible!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Effects (30 seconds):");
            lore.add(ChatColor.WHITE + "• Full Invisibility");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $2400");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "invisibility_cloak");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createReviveSyringe() {
        ItemStack item = new ItemStack(Material.END_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(
                ChatColor.GREEN + "" + ChatColor.BOLD + "Revive Syringe"
            );
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Emergency self-revive!");
            lore.add(ChatColor.GRAY + "Right-click to activate");
            lore.add("");
            lore.add(ChatColor.AQUA + "When you die:");
            lore.add(ChatColor.WHITE + "• Prevents death");
            lore.add(ChatColor.WHITE + "• Restores 5 hearts");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $1500");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "revive_syringe");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createEMPGrenade() {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(
                ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "EMP Grenade"
            );
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Electromagnetic pulse device");
            lore.add(ChatColor.GRAY + "Disrupts enemy equipment!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Effects on enemies (10 blocks):");
            lore.add(ChatColor.WHITE + "• Removes all potion effects");
            lore.add(ChatColor.WHITE + "• Applies Mining Fatigue III");
            lore.add(ChatColor.WHITE + "• Applies Weakness II");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $750");
            meta.setLore(lore);
            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "emp_grenade");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ============ NEW CUSTOM ITEMS ============

    private ItemStack createPlasmaRifle() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Plasma Rifle");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Advanced energy weapon");
            lore.add(ChatColor.GRAY + "Right-click to shoot plasma bolts!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Features:");
            lore.add(ChatColor.WHITE + "• 10 plasma shots per rifle");
            lore.add(ChatColor.WHITE + "• High damage output");
            lore.add(ChatColor.WHITE + "• Sets targets on fire");
            lore.add("");
            lore.add(ChatColor.RED + "Warning: Single use item");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $3,100");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "plasma_rifle");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            NamespacedKey ammoKey = new NamespacedKey(plugin, "plasma_ammo");
            container.set(ammoKey, PersistentDataType.INTEGER, 10);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createQuantumShield() {
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Quantum Shield");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Defensive energy barrier");
            lore.add(ChatColor.GRAY + "Quantum-enchanted protection!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Abilities:");
            lore.add(ChatColor.WHITE + "• Blocks 50% more damage");
            lore.add(ChatColor.WHITE + "• Reflects damage to attackers");
            lore.add(ChatColor.WHITE + "• Regenerates durability");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click to activate shield mode");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $2,200");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "quantum_shield");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNanoMedKit() {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Nano Med Kit");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Nanotechnology medical device");
            lore.add(ChatColor.GRAY + "Advanced healing technology!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Effects (Instant):");
            lore.add(ChatColor.WHITE + "• Full health restoration");
            lore.add(ChatColor.WHITE + "• Regeneration III (30s)");
            lore.add(ChatColor.WHITE + "• Absorption IV (2 minutes)");
            lore.add(ChatColor.WHITE + "• Removes all negative effects");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $800");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "nano_med_kit");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCryptoWallet() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Crypto Wallet");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Digital cryptocurrency wallet");
            lore.add(ChatColor.GRAY + "Store and trade cryptocurrencies!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Right-click to open wallet");
            lore.add(ChatColor.WHITE + "• 5 supported currencies");
            lore.add(ChatColor.WHITE + "• Secure blockchain storage");
            lore.add(ChatColor.WHITE + "• Real-time trading");
            lore.add("");
            lore.add(ChatColor.RED + "" + ChatColor.BOLD + "⚠ WARNING:");
            lore.add(ChatColor.RED + "Wallet is tied to this item!");
            lore.add(ChatColor.RED + "If you lose it, all crypto is GONE!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $1,200");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "crypto_wallet");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            // Generate unique wallet ID
            String walletId = java.util.UUID.randomUUID().toString().substring(0, 8);
            NamespacedKey walletIdKey = new NamespacedKey(plugin, "wallet_id");
            container.set(walletIdKey, PersistentDataType.STRING, walletId);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ============ CUSTOM WEAPONS ============

    private ItemStack createAutomaticBow() {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Automatic Bow");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "High-tech rapid-fire weapon");
            lore.add(ChatColor.GRAY + "Automatically shoots arrows!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Features:");
            lore.add(ChatColor.WHITE + "• Rapid automatic fire");
            lore.add(ChatColor.WHITE + "• No draw time needed");
            lore.add(ChatColor.WHITE + "• Unlimited ammunition");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click to activate auto-fire");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $2,100");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "automatic_bow");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            NamespacedKey autoKey = new NamespacedKey(plugin, "auto_fire");
            container.set(autoKey, PersistentDataType.BYTE, (byte) 0); // 0 = off, 1 = on
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBurstCrossbow() {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Burst Crossbow");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Multi-shot crossbow system");
            lore.add(ChatColor.GRAY + "Shoots 8 arrows at once!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Features:");
            lore.add(ChatColor.WHITE + "• Fires 8 arrows per shot");
            lore.add(ChatColor.WHITE + "• Spread pattern for crowd control");
            lore.add(ChatColor.WHITE + "• High damage potential");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click to fire burst");
            lore.add(ChatColor.RED + "Warning: High ammo consumption!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $3,000");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "burst_crossbow");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            NamespacedKey ammoKey = new NamespacedKey(plugin, "burst_ammo");
            container.set(ammoKey, PersistentDataType.INTEGER, 8); // Shots remaining
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createRicochetBow() {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Ricochet Bow");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Trick shot specialist weapon");
            lore.add(ChatColor.GRAY + "Arrows bounce off surfaces!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Features:");
            lore.add(ChatColor.WHITE + "• Arrows bounce once off blocks");
            lore.add(ChatColor.WHITE + "• Perfect for corner shots");
            lore.add(ChatColor.WHITE + "• Hit enemies behind cover");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Arrows bounce 1 time before disappearing");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $1,625");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "ricochet_bow");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createEnchantedDiamondSword() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Master Sword");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Legendary blade of warriors");
            lore.add("");
            lore.add(ChatColor.AQUA + "Enchantments:");
            lore.add(ChatColor.WHITE + "• Sharpness V");
            lore.add(ChatColor.WHITE + "• Unbreaking III");
            lore.add(ChatColor.WHITE + "• Fire Aspect II");
            lore.add(ChatColor.WHITE + "• Knockback II");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $1,875");
            meta.setLore(lore);
            // Add enchantments
            meta.addEnchant(org.bukkit.enchantments.Enchantment.SHARPNESS, 5, true);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 3, true);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 2, true);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.KNOCKBACK, 2, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createEnchantedDiamondBow() {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Sniper Bow");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Precision long-range weapon");
            lore.add("");
            lore.add(ChatColor.AQUA + "Enchantments:");
            lore.add(ChatColor.WHITE + "• Power V");
            lore.add(ChatColor.WHITE + "• Infinity I");
            lore.add(ChatColor.WHITE + "• Punch II");
            lore.add(ChatColor.WHITE + "• Flame I");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $1,200");
            meta.setLore(lore);
            // Add enchantments
            meta.addEnchant(org.bukkit.enchantments.Enchantment.POWER, 5, true);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.INFINITY, 1, true);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.PUNCH, 2, true);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.FLAME, 1, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createExplosiveArrows(int amount) {
        ItemStack item = new ItemStack(Material.ARROW, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Explosive Arrows");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Special ammunition for explosions");
            lore.add("");
            lore.add(ChatColor.AQUA + "Effects:");
            lore.add(ChatColor.WHITE + "• Creates explosion on impact");
            lore.add(ChatColor.WHITE + "• Area damage to enemies");
            lore.add(ChatColor.WHITE + "• Destroys weak blocks");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Quantity: " + amount);
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $800");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "explosive_arrow");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPoisonDaggers(int amount) {
        ItemStack item = new ItemStack(Material.IRON_HOE, amount); // Using iron hoe as dagger base
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Poison Daggers");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Coated with deadly poison");
            lore.add("");
            lore.add(ChatColor.AQUA + "Effects:");
            lore.add(ChatColor.WHITE + "• Inflicts poison on hit");
            lore.add(ChatColor.WHITE + "• Damage over time");
            lore.add(ChatColor.WHITE + "• Slows movement speed");
            lore.add(ChatColor.WHITE + "• 5 second poison duration");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Quantity: " + amount);
            lore.add(ChatColor.RED + "Use with caution!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $1,025");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "poison_dagger");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ============ NEW WEAPONS ============

    private ItemStack createFlamethrower() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Flamethrower");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Incendiary weapon system");
            lore.add(ChatColor.GRAY + "Burns everything in its path!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Features:");
            lore.add(ChatColor.WHITE + "• Continuous fire stream");
            lore.add(ChatColor.WHITE + "• Sets targets on fire");
            lore.add(ChatColor.WHITE + "• Area damage");
            lore.add(ChatColor.WHITE + "• 50 fuel units");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click to fire");
            lore.add(ChatColor.RED + "Warning: High fuel consumption!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $2,225");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "flamethrower");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            NamespacedKey ammoKey = new NamespacedKey(plugin, "flamethrower_fuel");
            container.set(ammoKey, PersistentDataType.INTEGER, 50);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createRocketLauncher() {
        ItemStack item = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Rocket Launcher");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Heavy explosive weapon");
            lore.add(ChatColor.GRAY + "Launches devastating rockets!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Features:");
            lore.add(ChatColor.WHITE + "• Massive explosions");
            lore.add(ChatColor.WHITE + "• Area destruction");
            lore.add(ChatColor.WHITE + "• High damage radius");
            lore.add(ChatColor.WHITE + "• 5 rockets per launcher");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click to fire");
            lore.add(ChatColor.RED + "Warning: Destroys terrain!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $3,750");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "rocket_launcher");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            NamespacedKey ammoKey = new NamespacedKey(plugin, "rocket_ammo");
            container.set(ammoKey, PersistentDataType.INTEGER, 5);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createLightningStaff() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Lightning Staff");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Ancient magical weapon");
            lore.add(ChatColor.GRAY + "Channels the power of storms!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Features:");
            lore.add(ChatColor.WHITE + "• Strikes with lightning");
            lore.add(ChatColor.WHITE + "• Chain lightning effect");
            lore.add(ChatColor.WHITE + "• High precision damage");
            lore.add(ChatColor.WHITE + "• 20 charges per staff");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click to cast lightning");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $2,750");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "lightning_staff");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            NamespacedKey ammoKey = new NamespacedKey(plugin, "lightning_charges");
            container.set(ammoKey, PersistentDataType.INTEGER, 20);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createFreezeGun() {
        ItemStack item = new ItemStack(Material.BLUE_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Freeze Gun");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Cryogenic weapon system");
            lore.add(ChatColor.GRAY + "Freezes targets in their tracks!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Features:");
            lore.add(ChatColor.WHITE + "• Slows enemies");
            lore.add(ChatColor.WHITE + "• Freezes targets solid");
            lore.add(ChatColor.WHITE + "• Crowd control");
            lore.add(ChatColor.WHITE + "• 30 freeze charges");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click to freeze");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $2,375");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "freeze_gun");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            NamespacedKey ammoKey = new NamespacedKey(plugin, "freeze_charges");
            container.set(ammoKey, PersistentDataType.INTEGER, 30);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createShotgun() {
        ItemStack item = new ItemStack(Material.IRON_HOE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Combat Shotgun");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Close-quarters combat weapon");
            lore.add(ChatColor.GRAY + "Devastating at close range!");
            lore.add("");
            lore.add(ChatColor.AQUA + "Features:");
            lore.add(ChatColor.WHITE + "• 8 pellet spread shot");
            lore.add(ChatColor.WHITE + "• High close-range damage");
            lore.add(ChatColor.WHITE + "• Multiple target hit");
            lore.add(ChatColor.WHITE + "• 24 shells per shotgun");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click to fire");
            lore.add(ChatColor.RED + "Ineffective at long range!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $1,800");
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "shotgun");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            NamespacedKey ammoKey = new NamespacedKey(plugin, "shotgun_shells");
            container.set(ammoKey, PersistentDataType.INTEGER, 24);
            item.setItemMeta(meta);
        }
        return item;
    }
}
