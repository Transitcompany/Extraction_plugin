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

    public String formatBalance(UUID uuid) {
        double balance = getBalanceAsDouble(uuid);
        return formatNumber(balance);
    }

    public double parseAmount(String input) throws NumberFormatException {
        if (input == null || input.isEmpty()) {
            throw new NumberFormatException("Input is null or empty");
        }

        input = input.trim().toLowerCase();
        String[] suffixes = {"k", "m", "b", "t", "q", "Q", "s", "S", "o", "n", "d", "U", "D", "T", "Qt", "Qd", "Sd", "St", "O", "N", "v", "c"};

        String suffix = "";
        for (String s : suffixes) {
            if (input.endsWith(s)) {
                suffix = s;
                input = input.substring(0, input.length() - s.length());
                break;
            }
        }

        double number;
        try {
            number = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid number format: " + input);
        }

        if (number < 0) {
            throw new NumberFormatException("Amount cannot be negative");
        }

        int exponent = 0;
        for (int i = 0; i < suffixes.length; i++) {
            if (suffixes[i].equals(suffix)) {
                exponent = i + 1;
                break;
            }
        }

        return number * Math.pow(1000, exponent);
    }

    public String formatNumber(double number) {
        if (number < 1000) {
            if (number % 1 == 0) {
                return String.valueOf((int) number);
            } else {
                return String.format("%.2f", number);
            }
        }

        String[] suffixes = {"k", "m", "b", "t", "q", "Q", "s", "S", "o", "n", "d", "U", "D", "T", "Qt", "Qd", "Sd", "St", "O", "N", "v", "c"};
        int exponent = 0;

        while (number / Math.pow(1000, exponent + 1) >= 1 && exponent < suffixes.length - 1) {
            exponent++;
        }

        double temp = number / Math.pow(1000, exponent);

        if (exponent >= suffixes.length) {
            return String.format("%.1e", number);
        }

        String suffix = exponent == 0 ? "" : suffixes[exponent - 1];

        if (temp % 1 == 0) {
            return (int) temp + suffix;
        } else {
            return String.format("%.1f", temp) + suffix;
        }
    }
}


