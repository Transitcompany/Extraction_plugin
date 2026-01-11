package com.extraction.economy;

import com.extraction.ExtractionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {
    private final ExtractionPlugin plugin;
    private final Map<UUID, String> balances = new HashMap<>();
    private File dataFile;
    private YamlConfiguration config;

    public EconomyManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "balances.yml");
        if (!dataFile.exists()) try { plugin.getDataFolder().mkdirs(); dataFile.createNewFile(); } catch (IOException ignored) {}
        config = YamlConfiguration.loadConfiguration(dataFile);
        loadBalances();
    }

    private void loadBalances() {
        for (String key : config.getKeys(false)) {
            balances.put(UUID.fromString(key), config.getString(key, "0"));
        }
    }

    public void saveBalances() {
        for (Map.Entry<UUID, String> entry : balances.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }
        try { config.save(dataFile); } catch (IOException ignored) {}
    }

    public String getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, "0");
    }
    public void setBalance(UUID uuid, String amount) {
        balances.put(uuid, amount);
        saveBalances();
    }
    public boolean takeBalance(UUID uuid, double amt) {
        try {
            double cur = Double.parseDouble(getBalance(uuid));
            if (cur < amt) return false;
            setBalance(uuid, String.valueOf(cur - amt));
            return true;
        } catch (NumberFormatException e) {
            return false; // can't take from non-numeric
        }
    }
    public void addBalance(UUID uuid, double amt) {
        try {
            double cur = Double.parseDouble(getBalance(uuid));
            setBalance(uuid, String.valueOf(cur + amt));
        } catch (NumberFormatException e) {
            setBalance(uuid, String.valueOf(amt)); // set to amt if not number
        }
    }

    public double getBalanceAsDouble(UUID uuid) {
        try {
            return Double.parseDouble(getBalance(uuid));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}


