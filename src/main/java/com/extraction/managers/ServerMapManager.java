package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ServerMapManager {

    private final ExtractionPlugin plugin;
    private final File mapFile;
    private final FileConfiguration mapConfig;
    private String mapUrl;

    public ServerMapManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.mapFile = new File(plugin.getDataFolder(), "servermap.yml");
        this.mapConfig = YamlConfiguration.loadConfiguration(mapFile);
        this.mapUrl = mapConfig.getString("map_url", "");
    }

    public void setMapUrl(String url) {
        this.mapUrl = url;
        mapConfig.set("map_url", url);
        saveConfig();
    }

    public String getMapUrl() {
        return mapUrl;
    }

    private void saveConfig() {
        try {
            mapConfig.save(mapFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save server map config: " + e.getMessage());
        }
    }
}