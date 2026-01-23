package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoginLogManager {

    private final ExtractionPlugin plugin;
    private final File logFile;
    private final Yaml yaml;
    private List<LoginEntry> loginEntries;

    public LoginLogManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), "login_logs.yml");
        this.yaml = new Yaml();
        this.loginEntries = new ArrayList<>();
        loadLogs();
    }

    public void addLoginEntry(String username, String ip) {
        LoginEntry entry = new LoginEntry(username, ip, LocalDateTime.now());
        loginEntries.add(entry);
        saveLogs();
    }

    private void loadLogs() {
        if (!logFile.exists()) {
            return;
        }
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(logFile);
            List<Map<String, Object>> logs = (List<Map<String, Object>>) config.getList("logs");
            if (logs != null) {
                for (Map<String, Object> log : logs) {
                    String username = (String) log.get("username");
                    String ip = (String) log.get("ip");
                    String timestampStr = (String) log.get("timestamp");
                    LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    loginEntries.add(new LoginEntry(username, ip, timestamp));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load login logs: " + e.getMessage());
        }
    }

    private void saveLogs() {
        try {
            if (!logFile.exists()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(logFile);
            List<Map<String, Object>> logs = new ArrayList<>();
            for (LoginEntry entry : loginEntries) {
                Map<String, Object> log = Map.of(
                    "username", entry.username,
                    "ip", entry.ip,
                    "timestamp", entry.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
                logs.add(log);
            }
            config.set("logs", logs);
            config.save(logFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save login logs: " + e.getMessage());
        }
    }

    public List<LoginEntry> getLoginEntries() {
        return new ArrayList<>(loginEntries);
    }

    public static class LoginEntry {
        public final String username;
        public final String ip;
        public final LocalDateTime timestamp;

        public LoginEntry(String username, String ip, LocalDateTime timestamp) {
            this.username = username;
            this.ip = ip;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return username + " from " + ip + " at " + timestamp;
        }
    }
}