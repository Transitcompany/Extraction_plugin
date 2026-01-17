package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ReportManager {

    private final ExtractionPlugin plugin;
    private final File reportFile;
    private final FileConfiguration reportConfig;
    private String webhookUrl;

    public ReportManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.reportFile = new File(plugin.getDataFolder(), "reports.yml");
        this.reportConfig = YamlConfiguration.loadConfiguration(reportFile);
        this.webhookUrl = reportConfig.getString("webhook_url", "");
    }

    public void setWebhookUrl(String url) {
        this.webhookUrl = url;
        reportConfig.set("webhook_url", url);
        saveConfig();
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public boolean sendReport(String reporter, String reported, String reason) {
        if (webhookUrl.isEmpty()) {
            return false;
        }

        String payload = String.format("{\"content\": \"Report: %s reported %s for %s\"}", reporter, reported, reason);

        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode >= 200 && responseCode < 300;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to send report to Discord: " + e.getMessage());
            return false;
        }
    }

    private void saveConfig() {
        try {
            reportConfig.save(reportFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save report config: " + e.getMessage());
        }
    }
}