package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FirstTimeJoinManager {
    private final ExtractionPlugin plugin;
    private final Set<UUID> firstTimeJoins = new HashSet<>();
    private File dataFile;
    private YamlConfiguration config;

    public FirstTimeJoinManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "firsttimejoins.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        loadFirstTimeJoins();
    }

    private void loadFirstTimeJoins() {
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String name = config.getString(key);
                firstTimeJoins.add(uuid);
            } catch (Exception ignored) {}
        }
    }

    public void saveFirstTimeJoins() {
        for (UUID uuid : firstTimeJoins) {
            // Assuming we have player name, but since UUID is key, maybe store name as value
            // But for simplicity, just store UUID as key, name as value if needed
            config.set(uuid.toString(), ""); // or get name somehow, but for now empty
        }
        try {
            config.save(dataFile);
        } catch (IOException ignored) {}
    }

    public boolean isFirstTimeJoin(UUID uuid) {
        return !firstTimeJoins.contains(uuid);
    }

    public void markJoined(UUID uuid, String name) {
        if (!firstTimeJoins.contains(uuid)) {
            firstTimeJoins.add(uuid);
            config.set(uuid.toString(), name);
            saveFirstTimeJoins();
        }
    }
}