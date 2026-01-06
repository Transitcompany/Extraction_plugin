package com.extraction.stash;

import com.extraction.ExtractionPlugin;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class StashManager {

    private final ExtractionPlugin plugin;
    private final Map<UUID, Inventory> stashes = new HashMap<>();
    private final File stashFolder;

    public StashManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        stashFolder = new File(plugin.getDataFolder(), "stashes");
        if (!stashFolder.exists()) stashFolder.mkdirs();
    }

    public Inventory getStash(Player player) {
        UUID uuid = player.getUniqueId();
        if (!stashes.containsKey(uuid)) {
            // Updated to 54 slots (Double Chest)
            Inventory inv = Bukkit.createInventory(null, 54, "Your Stash");
            loadStashFromDisk(uuid, inv);
            stashes.put(uuid, inv);
        }
        return stashes.get(uuid);
    }

    public void saveStash(Player player) {
        UUID uuid = player.getUniqueId();
        Inventory inv = stashes.get(uuid);
        if (inv == null) return;
        File file = new File(stashFolder, uuid + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();

        // Loop updated to save 54 slots
        for (int i = 0; i < 54; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) cfg.set("slot." + i, item);
        }
        try {
            cfg.save(file);
        } catch (IOException ignored) {}
    }

    private void loadStashFromDisk(UUID uuid, Inventory inv) {
        File file = new File(stashFolder, uuid + ".yml");
        if (!file.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        // Loop updated to load 54 slots
        for (int i = 0; i < 54; i++) {
            ItemStack item = cfg.getItemStack("slot." + i);
            if (item != null) {
                inv.setItem(i, item);
            }
        }
    }
}
