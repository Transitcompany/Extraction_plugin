package com.extraction.data;

import com.extraction.ExtractionPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final ExtractionPlugin plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private File dataFile;
    private YamlConfiguration config;

    public PlayerDataManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        loadPlayerData();
    }

    private void loadPlayerData() {
        for (String key : config.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            PlayerData data = new PlayerData(uuid);
            data.loadFromConfig(config.getConfigurationSection(key));
            playerDataMap.put(uuid, data);
        }
    }

    public void savePlayerData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            PlayerData data = entry.getValue();
            org.bukkit.configuration.ConfigurationSection section = config.createSection(entry.getKey().toString());
            saveToConfig(data, section);
        }
        try { config.save(dataFile); } catch (IOException ignored) {}
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data != null) {
            org.bukkit.configuration.ConfigurationSection section = config.createSection(uuid.toString());
            data.saveToConfig(section);
            try { config.save(dataFile); } catch (IOException ignored) {}
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(uuid));
    }

    public PlayerData getPlayerData(OfflinePlayer player) {
        return getPlayerData(player.getUniqueId());
    }

    public void wipePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
        config.set(uuid.toString(), null);
        try { config.save(dataFile); } catch (IOException ignored) {}
    }

    public void wipePlayerData(OfflinePlayer player) {
        wipePlayerData(player.getUniqueId());
    }

    private void saveToConfig(PlayerData data, org.bukkit.configuration.ConfigurationSection section) {
        data.saveToConfig(section);
    }

    public static class PlayerData {
        private final UUID uuid;
        private Rank rank;
        private int level;
        private double xp;
        private double totalXp;
        private int extractionsCompleted;
        private int itemsSold;
        private double totalMoneyEarned;
        private double totalMoneySpent;
        private int auctionsWon;
        private int auctionsCreated;
        private int highestSingleSale;
        private int totalPlaytimeMinutes;
        private int killStreak;
        private int highestKillStreak;
        private int monstersKilled;
        private long firstJoin;
        private long lastSeen;
        private long lastDailyReward;
        private int dailyStreak;

        public PlayerData(UUID uuid) {
            this.uuid = uuid;
            this.rank = Rank.P;
            this.level = 1;
            this.xp = 0;
            this.totalXp = 0;
            this.extractionsCompleted = 0;
            this.itemsSold = 0;
            this.totalMoneyEarned = 0;
            this.totalMoneySpent = 0;
            this.auctionsWon = 0;
            this.auctionsCreated = 0;
            this.highestSingleSale = 0;
            this.totalPlaytimeMinutes = 0;
            this.killStreak = 0;
            this.highestKillStreak = 0;
            this.monstersKilled = 0;
            this.firstJoin = System.currentTimeMillis();
            this.lastSeen = System.currentTimeMillis();
            this.lastDailyReward = 0;
            this.dailyStreak = 0;
        }

        public void addXp(double amount) {
            this.xp += amount;
            totalXp += amount;
            checkLevelUp();
        }

        public void setLevel(int level) {
            this.level = level;
        }

        private void checkLevelUp() {
            double xpNeeded = getXpNeededForNextLevel();
            while (xp >= xpNeeded && level < 100) {
                xp -= xpNeeded;
                level++;
                xpNeeded = getXpNeededForNextLevel();
            }
        }

        public double getXpNeededForNextLevel() {
            return 100 * Math.pow(1.1, level - 1);
        }

        public double getXpProgress() {
            return xp / getXpNeededForNextLevel();
        }

        public void loadFromConfig(org.bukkit.configuration.ConfigurationSection section) {
            if (section == null) return;

            this.rank = Rank.valueOf(section.getString("rank", "P"));
            this.level = section.getInt("level", 1);
            this.xp = section.getDouble("xp", 0);
            this.totalXp = section.getDouble("totalXp", 0);
            this.extractionsCompleted = section.getInt("extractionsCompleted", 0);
            this.itemsSold = section.getInt("itemsSold", 0);
            this.totalMoneyEarned = section.getDouble("totalMoneyEarned", 0);
            this.totalMoneySpent = section.getDouble("totalMoneySpent", 0);
            this.auctionsWon = section.getInt("auctionsWon", 0);
            this.auctionsCreated = section.getInt("auctionsCreated", 0);
            this.highestSingleSale = section.getInt("highestSingleSale", 0);
            this.totalPlaytimeMinutes = section.getInt("totalPlaytimeMinutes", 0);
            this.killStreak = section.getInt("killStreak", 0);
            this.highestKillStreak = section.getInt("highestKillStreak", 0);
            this.monstersKilled = section.getInt("monstersKilled", 0);
            this.firstJoin = section.getLong("firstJoin", System.currentTimeMillis());
            this.lastSeen = section.getLong("lastSeen", System.currentTimeMillis());
            this.lastDailyReward = section.getLong("lastDailyReward", 0);
            this.dailyStreak = section.getInt("dailyStreak", 0);
        }

        public void saveToConfig(org.bukkit.configuration.ConfigurationSection section) {
            section.set("rank", rank.name());
            section.set("level", level);
            section.set("xp", xp);
            section.set("totalXp", totalXp);
            section.set("extractionsCompleted", extractionsCompleted);
            section.set("itemsSold", itemsSold);
            section.set("totalMoneyEarned", totalMoneyEarned);
            section.set("totalMoneySpent", totalMoneySpent);
            section.set("auctionsWon", auctionsWon);
            section.set("auctionsCreated", auctionsCreated);
            section.set("highestSingleSale", highestSingleSale);
            section.set("totalPlaytimeMinutes", totalPlaytimeMinutes);
            section.set("killStreak", killStreak);
            section.set("highestKillStreak", highestKillStreak);
            section.set("monstersKilled", monstersKilled);
            section.set("firstJoin", firstJoin);
            section.set("lastSeen", lastSeen);
            section.set("lastDailyReward", lastDailyReward);
            section.set("dailyStreak", dailyStreak);
        }

        // Getters
        public UUID getUuid() { return uuid; }
        public Rank getRank() { return rank; }
        public int getLevel() { return level; }
        public double getXp() { return xp; }
        public double getTotalXp() { return totalXp; }
        public int getExtractionsCompleted() { return extractionsCompleted; }
        public int getItemsSold() { return itemsSold; }
        public double getTotalMoneyEarned() { return totalMoneyEarned; }
        public double getTotalMoneySpent() { return totalMoneySpent; }
        public int getAuctionsWon() { return auctionsWon; }
        public int getAuctionsCreated() { return auctionsCreated; }
        public int getHighestSingleSale() { return highestSingleSale; }
        public int getTotalPlaytimeMinutes() { return totalPlaytimeMinutes; }
        public int getKillStreak() { return killStreak; }
        public int getHighestKillStreak() { return highestKillStreak; }
        public int getMonstersKilled() { return monstersKilled; }
        public long getFirstJoin() { return firstJoin; }
        public long getLastSeen() { return lastSeen; }
        public long getLastDailyReward() { return lastDailyReward; }
        public int getDailyStreak() { return dailyStreak; }

        // Setters for tracking
        public void setRank(Rank rank) { this.rank = rank; }
        public void incrementExtractionsCompleted() { this.extractionsCompleted++; }
        public void incrementItemsSold() { this.itemsSold++; }
        public void addMoneyEarned(double amount) { 
            this.totalMoneyEarned += amount;
            if (amount > highestSingleSale) {
                this.highestSingleSale = (int) amount;
            }
        }
        public void addMoneySpent(double amount) { this.totalMoneySpent += amount; }
        public void incrementAuctionsWon() { this.auctionsWon++; }
        public void incrementAuctionsCreated() { this.auctionsCreated++; }
        public void incrementKillStreak() { 
            this.killStreak++;
            if (killStreak > highestKillStreak) {
                this.highestKillStreak = killStreak;
            }
        }
        public void resetKillStreak() { this.killStreak = 0; }
        public void incrementMonstersKilled() { this.monstersKilled++; }
        public void addPlaytime(int minutes) { this.totalPlaytimeMinutes += minutes; }
        public void setLastDailyReward() { this.lastDailyReward = System.currentTimeMillis(); }
        public void incrementDailyStreak() { this.dailyStreak++; }
        public void resetDailyStreak() { this.dailyStreak = 0; }
        public void updateLastSeen() { this.lastSeen = System.currentTimeMillis(); }
    }
}