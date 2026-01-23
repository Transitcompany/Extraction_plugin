package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TermsOfServiceManager {
    private final ExtractionPlugin plugin;
    private final Map<UUID, String> acceptedVersions = new HashMap<>();
    private File dataFile;
    private YamlConfiguration config;
    private final String currentTosVersion;

    public TermsOfServiceManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.currentTosVersion = plugin.getConfig().getString("terms-of-service.version", "1.0");
        this.dataFile = new File(plugin.getDataFolder(), "terms_of_service.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        loadAcceptedVersions();
    }

    private void loadAcceptedVersions() {
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String version = config.getString(key, "");
                acceptedVersions.put(uuid, version);
            } catch (Exception ignored) {}
        }
    }

    private void saveAcceptedVersions() {
        for (Map.Entry<UUID, String> entry : acceptedVersions.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            config.save(dataFile);
        } catch (IOException ignored) {}
    }

    public boolean needsToAcceptTerms(Player player) {
        String acceptedVersion = acceptedVersions.get(player.getUniqueId());
        return acceptedVersion == null || !acceptedVersion.equals(currentTosVersion);
    }

    public void acceptTerms(Player player) {
        acceptedVersions.put(player.getUniqueId(), currentTosVersion);
        saveAcceptedVersions();
    }

    public void displayTermsOfService(Player player) {
        var config = plugin.getConfig();
        var tosContent = config.getStringList("terms-of-service.content");

        // Send all lines except the last one as plain text
        for (int i = 0; i < tosContent.size() - 1; i++) {
            player.sendMessage(tosContent.get(i));
        }

        // Send the last line with clickable accept button
        if (!tosContent.isEmpty()) {
            Component acceptButton = Component.text("[ACCEPT]")
                .color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/accept"));

            Component message = Component.text("Click ")
                .color(NamedTextColor.GREEN)
                .append(acceptButton)
                .append(Component.text(" or type ").color(NamedTextColor.GREEN))
                .append(Component.text("/accept").color(NamedTextColor.AQUA))
                .append(Component.text(" to continue playing.").color(NamedTextColor.GREEN));

            player.sendMessage(message);
        }
    }
}