package com.extraction.managers;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class ChatModerationManager {

    private Set<String> badWords;
    private File blocklistFile;

    public ChatModerationManager(JavaPlugin plugin) {
        this.badWords = new HashSet<>();
        this.blocklistFile = new File(plugin.getDataFolder(), "blocklist.yml");
        initializeBlocklist(plugin);
        loadBlocklist();
    }

    private void initializeBlocklist(JavaPlugin plugin) {
        if (!blocklistFile.exists()) {
            plugin.saveResource("blocklist.yml", false);
        }
    }

    private void loadBlocklist() {
        Yaml yaml = new Yaml();
        try (FileInputStream inputStream = new FileInputStream(blocklistFile)) {
            Map<String, Object> data = yaml.load(inputStream);
            List<String> words = (List<String>) data.get("bad_words");
            if (words != null) {
                badWords.addAll(words);
            }
        } catch (Exception e) {
            // If file doesn't exist or error, load from resources as fallback
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("blocklist.yml")) {
                if (inputStream != null) {
                    Map<String, Object> data = yaml.load(inputStream);
                    List<String> words = (List<String>) data.get("bad_words");
                    if (words != null) {
                        badWords.addAll(words);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public String censor(String message) {
        String lowerMessage = message.toLowerCase();
        for (String word : badWords) {
            String lowerWord = word.toLowerCase();
            if (lowerMessage.contains(lowerWord)) {
                String replacement = new String(new char[word.length()]).replace('\0', '#');
                message = message.replaceAll("(?i)" + word, replacement);
            }
        }
        return message;
    }
}