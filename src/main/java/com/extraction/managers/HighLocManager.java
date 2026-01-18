package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HighLocManager {

    private final ExtractionPlugin plugin;
    private final File highLocFile;
    private final FileConfiguration highLocConfig;
    private final Map<Location, String> highLocs; // Location to type ("skeletons" or "zombies")

    public HighLocManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.highLocFile = new File(plugin.getDataFolder(), "highlocs.yml");
        this.highLocConfig = YamlConfiguration.loadConfiguration(highLocFile);
        this.highLocs = new HashMap<>();
        loadHighLocs();
    }

    public void addHighLoc(Location location, String type) {
        highLocs.put(location, type);
        saveHighLocs();
    }

    public void removeHighLoc(Location location) {
        highLocs.remove(location);
        saveHighLocs();
    }

    public Map<Location, String> getHighLocs() {
        return highLocs;
    }

    private void loadHighLocs() {
        if (!highLocConfig.contains("highlocs")) return;
        for (String key : highLocConfig.getConfigurationSection("highlocs").getKeys(false)) {
            String[] parts = key.split(",");
            if (parts.length == 4) {
                try {
                    String world = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                    String type = highLocConfig.getString("highlocs." + key + ".type");
                    highLocs.put(loc, type);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load high loc: " + key);
                }
            }
        }
    }

    private void saveHighLocs() {
        highLocConfig.set("highlocs", null);
        for (Map.Entry<Location, String> entry : highLocs.entrySet()) {
            Location loc = entry.getKey();
            String type = entry.getValue();
            String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
            highLocConfig.set("highlocs." + key + ".type", type);
        }
        try {
            highLocConfig.save(highLocFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save high locs: " + e.getMessage());
        }
    }

    public boolean isHighLocMob(org.bukkit.entity.Entity entity) {
        return entity instanceof org.bukkit.entity.Skeleton || entity instanceof org.bukkit.entity.Zombie ||
               entity instanceof org.bukkit.entity.SkeletonHorse || entity instanceof org.bukkit.entity.ZombieHorse ||
               entity instanceof org.bukkit.entity.WitherSkeleton;
    }
}