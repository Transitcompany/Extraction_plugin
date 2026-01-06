package com.extraction.loot;

import com.extraction.ExtractionPlugin;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class LootContainerManager {

    private final ExtractionPlugin plugin;
    private final LootTableManager lootTableManager;

    // Thread-safe map for container data: Location -> LootTableId
    private final ConcurrentHashMap<Location, String> containerLocations =
        new ConcurrentHashMap<>();

    // Map to manage configuration files: File Index (1, 2, 3...) -> FileConfiguration
    private final ConcurrentHashMap<Integer, YamlConfiguration> lootConfigMap =
        new ConcurrentHashMap<>();

    // Configuration settings for multi-file splitting
    private static final int MAX_CONTAINERS_PER_FILE = 500; // Limit containers per file to split data
    private static final String FILE_NAME_PATTERN = "lootchests%d.yml";
    private static final Random RANDOM = new Random();

    public LootContainerManager(
        ExtractionPlugin plugin,
        LootTableManager lootTableManager
    ) {
        this.plugin = plugin;
        this.lootTableManager = lootTableManager;
        loadLootData();
        startRestockTask();
    }

    // --- Persistence Methods (Multi-File Implementation) ---

    private File getLootFile(int index) {
        String fileName = String.format(FILE_NAME_PATTERN, index);
        return new File(plugin.getDataFolder(), fileName);
    }

    private YamlConfiguration getLootConfig(int index) {
        // Use computeIfAbsent for thread-safe retrieval/creation
        return lootConfigMap.computeIfAbsent(index, i -> {
            File file = getLootFile(i);
            if (!file.exists()) {
                try {
                    plugin.getDataFolder().mkdirs();
                    file.createNewFile();
                } catch (IOException e) {
                    plugin
                        .getLogger()
                        .severe(
                            "Could not create loot file " +
                                file.getName() +
                                ": " +
                                e.getMessage()
                        );
                }
            }
            return YamlConfiguration.loadConfiguration(file);
        });
    }

    private void loadLootData() {
        plugin
            .getLogger()
            .info("Loading loot containers from multiple files...");
        int loadedCount = 0;
        int fileIndex = 1;

        while (true) {
            File file = getLootFile(fileIndex);
            if (!file.exists() && fileIndex > 1) {
                break; // Stop if we don't find the next file
            }

            YamlConfiguration config = getLootConfig(fileIndex);

            // Iterate through all keys (serialized locations) in the current file
            for (String key : config.getKeys(false)) {
                try {
                    Location loc = deserializeLocation(key);
                    String table = config.getString(key + ".table");
                    if (loc.getWorld() != null && table != null) {
                        containerLocations.put(loc, table);
                        loadedCount++;
                    } else {
                        plugin
                            .getLogger()
                            .warning(
                                "Skipping invalid loot container data in " +
                                    file.getName() +
                                    " at key: " +
                                    key
                            );
                    }
                } catch (Exception e) {
                    plugin
                        .getLogger()
                        .severe(
                            "Error loading container at key " +
                                key +
                                " in " +
                                file.getName() +
                                ": " +
                                e.getMessage()
                        );
                }
            }

            // If the current file has less than the max limit, it's likely the last one.
            if (config.getKeys(false).size() < MAX_CONTAINERS_PER_FILE) {
                if (!getLootFile(fileIndex + 1).exists()) {
                    break;
                }
            }

            fileIndex++;
            if (fileIndex > 100) {
                // Safety break for extremely high index
                plugin
                    .getLogger()
                    .severe(
                        "Stopped loading after 100 loot chest files. Check MAX_CONTAINERS_PER_FILE setting."
                    );
                break;
            }
        }
        plugin
            .getLogger()
            .info(
                "Loaded " +
                    loadedCount +
                    " loot containers from " +
                    (fileIndex - 1) +
                    " files."
            );
    }

    public void saveLootData() {
        // Clear all configs to prepare for a fresh save of all data
        lootConfigMap
            .values()
            .forEach(config ->
                config.getKeys(false).forEach(key -> config.set(key, null))
            );

        int fileIndex = 1;
        int containerCount = 0;
        YamlConfiguration currentConfig = getLootConfig(fileIndex);

        // Iterate over a copy of the key set to avoid ConcurrentModificationException if a container is added/removed elsewhere
        for (Location loc : new HashSet<>(containerLocations.keySet())) {
            String table = containerLocations.get(loc);

            // Check if the current file is full
            if (containerCount >= MAX_CONTAINERS_PER_FILE) {
                saveConfig(fileIndex, currentConfig); // Save the full file
                fileIndex++;
                containerCount = 0;
                currentConfig = getLootConfig(fileIndex); // Switch to the next file
            }

            // Save data to the current config
            currentConfig.set(serializeLocation(loc) + ".table", table);
            containerCount++;
        }

        // Save the last file (which might not be full)
        saveConfig(fileIndex, currentConfig);
    }

    private void saveConfig(int index, YamlConfiguration config) {
        File file = getLootFile(index);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin
                .getLogger()
                .severe(
                    "Could not save to " +
                        file.getName() +
                        ": " +
                        e.getMessage()
                );
        }
    }

    // --- Core Logic Methods ---

    public void registerLootContainer(Location location, String table) {
        containerLocations.put(location, table);
        saveLootData();
    }

    /**
     * Registers multiple loot containers efficiently without calling saveLootData() for each one.
     * The caller is responsible for calling saveLootData() once after this completes.
     */
    public void registerMultipleLootContainers(
        List<Location> locations,
        String table
    ) {
        for (Location location : locations) {
            containerLocations.put(location, table);
        }
    }

    public void unregisterLootContainer(Location location) {
        containerLocations.remove(location);
        saveLootData();
    }

    public void restockAll() {
        // FIX: Iterate over a copy of the keys to prevent ConcurrentModificationException
        for (Location loc : new HashSet<>(containerLocations.keySet())) {
            // Schedule the restock to run on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> restock(loc));
        }
        plugin
            .getLogger()
            .info(
                "Scheduled restock for " +
                    containerLocations.size() +
                    " loot containers."
            );
    }

    public void restock(Location loc) {
        String table = containerLocations.get(loc);
        if (table == null) return;

        Block block = loc.getBlock();
        if (!(block.getState() instanceof InventoryHolder)) return;

        InventoryHolder holder = (InventoryHolder) block.getState();
        List<ItemStack> loot = lootTableManager.generateLootForTable(table);

        // Inventory manipulation MUST be on the main thread
        holder.getInventory().clear();

        // Randomly distribute items across chest slots
        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < holder.getInventory().getSize(); i++) {
            availableSlots.add(i);
        }
        Collections.shuffle(availableSlots, RANDOM);

        int slotIndex = 0;
        for (ItemStack item : loot) {
            if (slotIndex < availableSlots.size()) {
                holder
                    .getInventory()
                    .setItem(availableSlots.get(slotIndex++), item);
            } else {
                // If we run out of slots, add normally (may stack items)
                holder.getInventory().addItem(item);
            }
        }
    }

    private void startRestockTask() {
        // Runs restockAll() every 40 minutes
        Bukkit.getScheduler().runTaskTimer(
            plugin,
            this::restockAll,
            20L * 60L * 40L,
            20L * 60L * 40L
        );
    }

    // --- Utility Methods ---

    public boolean isLootContainer(Location location) {
        return containerLocations.containsKey(location);
    }

    public String getLootTable(Location location) {
        return containerLocations.get(location);
    }

    public Set<Location> getAllLocations() {
        return containerLocations.keySet();
    }

    private String serializeLocation(Location loc) {
        String worldName = loc.getWorld() != null
            ? loc.getWorld().getName()
            : "null_world";
        return (
            worldName +
            "," +
            loc.getBlockX() +
            "," +
            loc.getBlockY() +
            "," +
            loc.getBlockZ()
        );
    }

    private Location deserializeLocation(String s) {
        String[] p = s.split(",");
        if (p.length != 4) throw new IllegalArgumentException(
            "Invalid serialized location string: " + s
        );
        World w = Bukkit.getWorld(p[0]);
        return new Location(
            w,
            Integer.parseInt(p[1]),
            Integer.parseInt(p[2]),
            Integer.parseInt(p[3])
        );
    }

    public void protect(Block block) {
        // Placeholder: ContainerListener prevents break/interact for these blocks
    }

    // For use by listener
    public boolean matchesProtected(Location loc) {
        return isLootContainer(loc);
    }
}
