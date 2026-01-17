package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestManager {

    private final ExtractionPlugin plugin;
    private final File chestFile;
    private final FileConfiguration chestConfig;
    private final Map<Location, ChestClaim> claimedChests = new HashMap<>();

    public ChestManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.chestFile = new File(plugin.getDataFolder(), "claimed_chests.yml");
        this.chestConfig = YamlConfiguration.loadConfiguration(chestFile);
        loadClaimedChests();
    }

    public boolean isChestClaimed(Location location) {
        return claimedChests.containsKey(location);
    }

    public boolean canPlayerAccessChest(UUID playerId, Location location) {
        ChestClaim claim = claimedChests.get(location);
        return claim != null && claim.owner.equals(playerId);
    }

    public boolean claimChest(UUID owner, Location location) {
        if (isChestClaimed(location)) return false;
        ChestClaim claim = new ChestClaim(owner, 0);
        claimedChests.put(location, claim);
        // If double chest, claim the other side too
        if (isDoubleChest(location)) {
            Location otherLocation = getOtherDoubleChestLocation(location);
            if (otherLocation != null && !isChestClaimed(otherLocation)) {
                claimedChests.put(otherLocation, new ChestClaim(owner, 0));
            }
        }
        saveClaimedChests();
        return true;
    }

    public void unclaimChest(Location location) {
        claimedChests.remove(location);
        saveClaimedChests();
    }

    public int getHitCount(Location location) {
        ChestClaim claim = claimedChests.get(location);
        return claim != null ? claim.hitCount : 0;
    }

    public void incrementHitCount(Location location) {
        ChestClaim claim = claimedChests.get(location);
        if (claim != null) {
            claim.hitCount++;
            if (claim.hitCount >= 250) {
                unclaimChest(location);
            } else {
                saveClaimedChests();
            }
        }
    }

    private Location getOtherDoubleChestLocation(Location location) {
        Block block = location.getBlock();
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            if (chest.getInventory().getHolder() instanceof org.bukkit.block.DoubleChest) {
                org.bukkit.block.DoubleChest doubleChest = (org.bukkit.block.DoubleChest) chest.getInventory().getHolder();
                Location left = ((Chest) doubleChest.getLeftSide()).getLocation();
                Location right = ((Chest) doubleChest.getRightSide()).getLocation();
                if (location.equals(left)) {
                    return right;
                } else {
                    return left;
                }
            }
        }
        return null;
    }

    public boolean isDoubleChest(Location location) {
        Block block = location.getBlock();
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            return chest.getInventory().getHolder() instanceof org.bukkit.block.DoubleChest;
        }
        return false;
    }

    private void loadClaimedChests() {
        if (!chestConfig.contains("chests")) return;
        for (String key : chestConfig.getConfigurationSection("chests").getKeys(false)) {
            String[] parts = key.split(",");
            if (parts.length == 4) {
                try {
                    String world = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                    UUID owner = UUID.fromString(chestConfig.getString("chests." + key + ".owner"));
                    int hitCount = chestConfig.getInt("chests." + key + ".hitCount", 0);
                    claimedChests.put(loc, new ChestClaim(owner, hitCount));
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load chest claim: " + key);
                }
            }
        }
    }

    private void saveClaimedChests() {
        chestConfig.set("chests", null);
        for (Map.Entry<Location, ChestClaim> entry : claimedChests.entrySet()) {
            Location loc = entry.getKey();
            ChestClaim claim = entry.getValue();
            String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
            chestConfig.set("chests." + key + ".owner", claim.owner.toString());
            chestConfig.set("chests." + key + ".hitCount", claim.hitCount);
        }
        try {
            chestConfig.save(chestFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save claimed chests: " + e.getMessage());
        }
    }

    public boolean isChestMaterial(Material material) {
        return material == Material.CHEST || material == Material.BARREL;
    }

    private static class ChestClaim {
        UUID owner;
        int hitCount;

        ChestClaim(UUID owner, int hitCount) {
            this.owner = owner;
            this.hitCount = hitCount;
        }
    }
}