package com.extraction.loot;

import com.extraction.ExtractionPlugin;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class LootTableManager {

    private final ExtractionPlugin plugin;
    private final Map<String, List<LootEntry>> lootTables = new HashMap<>();
    private final Random random = new Random();

    public LootTableManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        initTables();
    }

    private void initTables() {
        // Common loot table - improved basic resources and tools
        List<LootEntry> commonLoot = new ArrayList<>();

        // Basic resources - Increased chances and amounts
        commonLoot.add(new LootEntry(Material.COAL, 8, 24, 1.0)); // Increased amount
        commonLoot.add(new LootEntry(Material.IRON_INGOT, 4, 12, 0.9)); // Increased amount & chance
        commonLoot.add(new LootEntry(Material.GOLD_INGOT, 2, 8, 0.7)); // Increased chance
        commonLoot.add(new LootEntry(Material.EMERALD, 1, 4, 0.5)); // Increased chance
        commonLoot.add(new LootEntry(Material.DIAMOND, 1, 2, 0.3)); // Increased chance

        // Old supplies - kept as is, but slightly reduced cobweb chance
        commonLoot.add(new LootEntry(Material.COBWEB, 1, 4, 0.4)); // Decreased chance
        commonLoot.add(new LootEntry(Material.ROTTEN_FLESH, 4, 16, 0.8));
        commonLoot.add(new LootEntry(Material.BONE, 4, 12, 0.7));
        commonLoot.add(new LootEntry(Material.STRING, 2, 8, 0.6));
        commonLoot.add(new LootEntry(Material.SPIDER_EYE, 1, 4, 0.5));
        commonLoot.add(new LootEntry(Material.GUNPOWDER, 2, 6, 0.5));

        // Basic food - kept as is
        commonLoot.add(new LootEntry(Material.APPLE, 2, 6, 0.6));
        commonLoot.add(new LootEntry(Material.BREAD, 2, 8, 0.5));
        commonLoot.add(new LootEntry(Material.BEEF, 2, 4, 0.4));
        commonLoot.add(new LootEntry(Material.PORKCHOP, 2, 4, 0.4));
        commonLoot.add(new LootEntry(Material.POTATO, 3, 8, 0.5));
        commonLoot.add(new LootEntry(Material.CARROT, 3, 8, 0.5));

        // Old/damaged wooden weapons and tools - Increased minimum durability
        commonLoot.add(
            new LootEntry(Material.WOODEN_SWORD, 1, 1, 0.4, 0.3, 0.7) // minDurability 0.3 (was 0.2)
        );
        commonLoot.add(new LootEntry(Material.WOODEN_AXE, 1, 1, 0.4, 0.3, 0.7)); // minDurability 0.3 (was 0.2)
        commonLoot.add(
            new LootEntry(Material.WOODEN_PICKAXE, 1, 1, 0.4, 0.3, 0.7) // minDurability 0.3 (was 0.2)
        );
        commonLoot.add(
            new LootEntry(Material.WOODEN_SHOVEL, 1, 1, 0.3, 0.2, 0.6) // minDurability 0.2 (was 0.1)
        );

        // Stone weapons - Increased minimum durability and drop chance
        commonLoot.add(
            new LootEntry(Material.STONE_SWORD, 1, 1, 0.45, 0.4, 0.7) // minDurability 0.4 (was 0.15)
        );
        commonLoot.add(
            new LootEntry(Material.STONE_AXE, 1, 1, 0.45, 0.4, 0.7) // minDurability 0.4 (was 0.15)
        );
        commonLoot.add(
            new LootEntry(Material.STONE_PICKAXE, 1, 1, 0.45, 0.4, 0.7) // minDurability 0.4 (was 0.15)
        );

        // Misc common items - Increased arrow amount and torch chance
        commonLoot.add(new LootEntry(Material.LEATHER, 2, 6, 0.5));

        // Leather armor with low durability and trims
        commonLoot.add(new LootEntry(Material.LEATHER_HELMET, 1, 1, 0.3, 0.1, 0.3, true));
        commonLoot.add(new LootEntry(Material.LEATHER_CHESTPLATE, 1, 1, 0.3, 0.1, 0.3, true));
        commonLoot.add(new LootEntry(Material.LEATHER_LEGGINGS, 1, 1, 0.3, 0.1, 0.3, true));
        commonLoot.add(new LootEntry(Material.LEATHER_BOOTS, 1, 1, 0.3, 0.1, 0.3, true));
        commonLoot.add(new LootEntry(Material.FEATHER, 3, 10, 0.4));
        commonLoot.add(new LootEntry(Material.FLINT, 2, 6, 0.4));
        commonLoot.add(new LootEntry(Material.ARROW, 8, 24, 0.6)); // Increased amount and chance
        commonLoot.add(new LootEntry(Material.TORCH, 8, 24, 0.8)); // Increased amount and chance
        commonLoot.add(new LootEntry(Material.PAPER, 3, 8, 0.4));
        commonLoot.add(new LootEntry(Material.BOOK, 1, 3, 0.3));
        
        // GPS item (rare but in any chest)
        commonLoot.add(new LootEntry(Material.TRIAL_KEY, 1, 1, 0.15, "gps"));

        // Rare loot table - substantially better items, improved iron gear
        List<LootEntry> rareLoot = new ArrayList<>();

        // Better resources - Massively Increased Drop Chance and Amounts
        rareLoot.add(new LootEntry(Material.IRON_INGOT, 8, 24, 1.0)); // Increased amount
        rareLoot.add(new LootEntry(Material.GOLD_INGOT, 4, 16, 1.0)); // Increased amount
        rareLoot.add(new LootEntry(Material.DIAMOND, 1, 6, 0.8)); // Increased amount and chance (was 0.6)
        rareLoot.add(new LootEntry(Material.EMERALD, 4, 12, 0.9)); // Increased amount and chance (was 0.7)

        // Iron gear - much better durability and drop chance
        rareLoot.add(new LootEntry(Material.IRON_HELMET, 1, 1, 0.8, 0.5, 0.9, true)); // Higher chance and min durability, with trim
        rareLoot.add(
            new LootEntry(Material.IRON_CHESTPLATE, 1, 1, 0.75, 0.5, 0.9) // Higher chance and min durability
        );
        rareLoot.add(
            new LootEntry(Material.IRON_LEGGINGS, 1, 1, 0.75, 0.5, 0.9) // Higher chance and min durability
        );
        rareLoot.add(new LootEntry(Material.IRON_BOOTS, 1, 1, 0.8, 0.5, 0.9)); // Higher chance and min durability
        rareLoot.add(new LootEntry(Material.IRON_SWORD, 1, 1, 0.85, 0.6, 1.0)); // Higher chance and min durability
        rareLoot.add(new LootEntry(Material.IRON_AXE, 1, 1, 0.8, 0.5, 0.9)); // Higher chance and min durability
        rareLoot.add(new LootEntry(Material.IRON_PICKAXE, 1, 1, 0.8, 0.5, 0.9)); // Higher chance and min durability

        // Ranged weapons - Increased Drop Chance and Min Durability
        rareLoot.add(new LootEntry(Material.BOW, 1, 1, 0.75, 0.6, 1.0)); // Higher chance and min durability (was 0.4)
        rareLoot.add(new LootEntry(Material.CROSSBOW, 1, 1, 0.65, 0.6, 1.0)); // Higher chance and min durability
        rareLoot.add(new LootEntry(Material.ARROW, 16, 48, 1.0)); // Max chance and increased amount

        // Valuable items - Massively Increased Drop Chance and better items
        rareLoot.add(new LootEntry(Material.GOLDEN_APPLE, 1, 3, 0.7)); // Increased chance (was 0.5)
        rareLoot.add(new LootEntry(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 0.1)); // NEW: Rare chance for G-Apple
        rareLoot.add(new LootEntry(Material.ENDER_PEARL, 2, 6, 0.7)); // Increased amount and chance
        rareLoot.add(new LootEntry(Material.EXPERIENCE_BOTTLE, 5, 15, 0.8)); // Increased amount and chance
        rareLoot.add(new LootEntry(Material.ENCHANTED_BOOK, 1, 1, 0.6)); // Increased chance (was 0.45)
        rareLoot.add(new LootEntry(Material.NAME_TAG, 1, 2, 0.6)); // Increased chance
        rareLoot.add(new LootEntry(Material.SADDLE, 1, 1, 0.55)); // Increased chance (was 0.4)
        rareLoot.add(new LootEntry(Material.DIAMOND_HORSE_ARMOR, 1, 1, 0.3)); // NEW: Diamond Horse Armor
        rareLoot.add(new LootEntry(Material.SHIELD, 1, 1, 0.6, 0.7, 1.0)); // Increased chance, better durability

        // Misc decorative/old supplies
        rareLoot.add(new LootEntry(Material.COBWEB, 2, 6, 0.6));
        rareLoot.add(new LootEntry(Material.IRON_BARS, 4, 12, 0.7)); // Increased amount and chance
        rareLoot.add(new LootEntry(Material.SOUL_LANTERN, 1, 4, 0.6)); // Increased amount and chance

        // Epic loot table - max quality and best items
        List<LootEntry> epicLoot = new ArrayList<>();

        // Premium resources - Massive Amounts and Chances
        epicLoot.add(new LootEntry(Material.DIAMOND, 4, 16, 1.0)); // Max chance, massive amount
        epicLoot.add(new LootEntry(Material.EMERALD, 8, 32, 0.95)); // Max chance, massive amount
        epicLoot.add(new LootEntry(Material.GOLD_INGOT, 16, 64, 0.9)); // Max chance, massive amount
        epicLoot.add(new LootEntry(Material.NETHERITE_SCRAP, 1, 3, 0.5)); // Increased amount and chance (was 0.3)
        epicLoot.add(new LootEntry(Material.ANCIENT_DEBRIS, 1, 2, 0.2)); // NEW: Small chance for Ancient Debris

        // Diamond gear - almost perfect durability and high chance
        epicLoot.add(
            new LootEntry(Material.DIAMOND_HELMET, 1, 1, 0.85, 0.8, 1.0) // Higher chance and min durability (was 0.7)
        );
        epicLoot.add(
            new LootEntry(Material.DIAMOND_CHESTPLATE, 1, 1, 0.8, 0.8, 1.0) // Higher chance and min durability
        );
        epicLoot.add(
            new LootEntry(Material.DIAMOND_LEGGINGS, 1, 1, 0.8, 0.8, 1.0) // Higher chance and min durability
        );
        epicLoot.add(
            new LootEntry(Material.DIAMOND_BOOTS, 1, 1, 0.85, 0.8, 1.0) // Higher chance and min durability
        );
        epicLoot.add(
            new LootEntry(Material.DIAMOND_SWORD, 1, 1, 0.9, 0.85, 1.0) // Higher chance and min durability
        );
        epicLoot.add(new LootEntry(Material.DIAMOND_AXE, 1, 1, 0.85, 0.8, 1.0)); // Higher chance and min durability
        epicLoot.add(
            new LootEntry(Material.DIAMOND_PICKAXE, 1, 1, 0.85, 0.8, 1.0) // Higher chance and min durability
        );
        epicLoot.add(
            new LootEntry(Material.DIAMOND_HOE, 1, 1, 0.5, 0.8, 1.0) // NEW: Diamond Hoe
        );

        // Special items - The very best loot
        epicLoot.add(new LootEntry(Material.GOLDEN_APPLE, 4, 8, 0.8)); // Increased amount and chance
        epicLoot.add(
            new LootEntry(Material.ENCHANTED_GOLDEN_APPLE, 1, 2, 0.5) // Increased chance and amount (was 0.3)
        );
        epicLoot.add(new LootEntry(Material.TOTEM_OF_UNDYING, 1, 1, 0.5)); // Increased chance (was 0.35)
        epicLoot.add(new LootEntry(Material.ENDER_PEARL, 4, 12, 0.8)); // Increased amount and chance
        epicLoot.add(new LootEntry(Material.BLAZE_ROD, 4, 12, 0.7)); // Increased amount and chance
        epicLoot.add(new LootEntry(Material.GHAST_TEAR, 2, 6, 0.6)); // Increased amount and chance
        epicLoot.add(new LootEntry(Material.ENCHANTED_BOOK, 1, 3, 0.75)); // Increased amount and chance
        epicLoot.add(new LootEntry(Material.ELYTRA, 1, 1, 0.4)); // Increased chance (was 0.2)
        epicLoot.add(new LootEntry(Material.TRIDENT, 1, 1, 0.4, 0.7, 1.0)); // Increased chance and min durability (was 0.5)
        epicLoot.add(new LootEntry(Material.SHIELD, 1, 1, 0.6, 0.8, 1.0)); // Increased chance, perfect durability
        epicLoot.add(new LootEntry(Material.SADDLE, 1, 1, 0.6)); // Increased chance

        lootTables.put("default", commonLoot);
        lootTables.put("common", commonLoot);
        lootTables.put("rare", rareLoot);
        lootTables.put("epic", epicLoot);
    }

    public List<String> getAllTables() {
        return new ArrayList<>(lootTables.keySet());
    }

    public List<ItemStack> generateLootForTable(String id) {
        List<LootEntry> entries = lootTables.getOrDefault(
            id,
            lootTables.get("default")
        );
        List<ItemStack> selectedLoot = new ArrayList<>();

        // Shuffle and select items based on their weight/chance
        List<LootEntry> shuffled = new ArrayList<>(entries);
        Collections.shuffle(shuffled, random);

        // MODIFIED: Select 4-10 items (was 3-8)
        int targetItems = random.nextInt(7) + 4;
        int itemsAdded = 0;

        for (LootEntry entry : shuffled) {
            if (itemsAdded >= targetItems) break;

            if (random.nextDouble() < entry.dropChance) {
                ItemStack item = createItem(entry);
                if (item != null) {
                    selectedLoot.add(item);
                    itemsAdded++;
                }
            }
        }

        // 1 in 24 chance (~4.16%) to add extraction banner - kept as is
        if (random.nextInt(24) == 0) {
            ItemStack extractionBanner = createExtractionBanner();
            if (extractionBanner != null) {
                selectedLoot.add(extractionBanner);
            }
        }

        return selectedLoot;
    }

    private ItemStack createGpsItem(ItemStack item, ItemMeta meta) {
        // Replace with trial key material
        item.setType(Material.TRIAL_KEY);
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "GPS");
            meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "A device that reveals your coordinates",
                ChatColor.GRAY + "Left or right-click to see your current location",
                "",
                ChatColor.YELLOW + "Value: $150"
            ));
            
            // Set custom key to identify this item
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "gps_trail_key");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(LootEntry entry) {
        int amount =
            entry.minAmount +
            random.nextInt(entry.maxAmount - entry.minAmount + 1);
        ItemStack item = new ItemStack(entry.material, amount);

        ItemMeta meta = item.getItemMeta();

        // Handle custom items like GPS
        if (entry.customType != null && entry.customType.equals("gps")) {
            return createGpsItem(item, meta);
        }

        // Apply durability damage if specified
        if (entry.hasDurabilityRange() && meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            int maxDurability = entry.material.getMaxDurability();
            if (maxDurability > 0) {
                // Calculate durability between min and max percentage
                double durabilityPercent =
                    entry.minDurability +
                    (random.nextDouble() *
                        (entry.maxDurability - entry.minDurability));
                int damage = (int) (maxDurability *
                    (1.0 - durabilityPercent));
                damageable.setDamage(damage);
            }
        }

        // Apply trim if specified
        if (entry.hasTrim && meta instanceof ArmorMeta) {
            ArmorMeta armorMeta = (ArmorMeta) meta;
            TrimMaterial[] materials = {TrimMaterial.IRON, TrimMaterial.COPPER, TrimMaterial.GOLD};
            TrimPattern[] patterns = {TrimPattern.EYE, TrimPattern.SPIRE, TrimPattern.WAYFINDER, TrimPattern.SHAPER};
            TrimMaterial trimMaterial = materials[random.nextInt(materials.length)];
            TrimPattern pattern = patterns[random.nextInt(patterns.length)];
            ArmorTrim trim = new ArmorTrim(trimMaterial, pattern);
            armorMeta.setTrim(trim);
        }

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createExtractionBanner() {
        ItemStack banner = new ItemStack(Material.WHITE_BANNER);
        ItemMeta meta = banner.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Easy Extraction Banner");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Place and break to extract!");
            lore.add(ChatColor.GRAY + "Works anywhere on the map!");
            lore.add("");
            lore.add(ChatColor.GOLD + "Sells for: $100,000");
            meta.setLore(lore);

            PersistentDataContainer container =
                meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "extraction_banner");
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            banner.setItemMeta(meta);
        }
        return banner;
    }

    // Inner class to represent loot entries with weighted chances
    private static class LootEntry {

        final Material material;
        final int minAmount;
        final int maxAmount;
        final double dropChance;
        final double minDurability;
        final double maxDurability;
        final boolean hasTrim;
        final String customType; // For custom items like GPS

        // Constructor for items without durability variation
        LootEntry(
            Material material,
            int minAmount,
            int maxAmount,
            double dropChance
        ) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.dropChance = dropChance;
            this.minDurability = -1;
            this.maxDurability = -1;
            this.hasTrim = false;
            this.customType = null;
        }

        // Constructor for items with durability variation (damaged items)
        LootEntry(
            Material material,
            int minAmount,
            int maxAmount,
            double dropChance,
            double minDurability,
            double maxDurability
        ) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.dropChance = dropChance;
            this.minDurability = minDurability;
            this.maxDurability = maxDurability;
            this.hasTrim = false;
            this.customType = null;
        }

        // Constructor for items with durability and trim
        LootEntry(
            Material material,
            int minAmount,
            int maxAmount,
            double dropChance,
            double minDurability,
            double maxDurability,
            boolean hasTrim
        ) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.dropChance = dropChance;
            this.minDurability = minDurability;
            this.maxDurability = maxDurability;
            this.hasTrim = hasTrim;
            this.customType = null;
        }

        // Constructor for custom items (like GPS)
        LootEntry(
            Material material,
            int minAmount,
            int maxAmount,
            double dropChance,
            String customType
        ) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.dropChance = dropChance;
            this.minDurability = -1;
            this.maxDurability = -1;
            this.hasTrim = false;
            this.customType = customType;
        }

        boolean hasDurabilityRange() {
            return minDurability >= 0 && maxDurability >= 0;
        }
    }
}
