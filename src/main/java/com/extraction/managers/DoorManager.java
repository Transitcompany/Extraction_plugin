package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DoorManager {

    private final ExtractionPlugin plugin;
    private final File doorFile;
    private final FileConfiguration doorConfig;
    private final Map<Location, DoorClaim> claimedDoors = new HashMap<>();

    public DoorManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.doorFile = new File(plugin.getDataFolder(), "claimed_doors.yml");
        this.doorConfig = YamlConfiguration.loadConfiguration(doorFile);
        loadClaimedDoors();
    }

    public boolean isDoorClaimed(Location location) {
        return claimedDoors.containsKey(getDoorBottomLocation(location));
    }

    public boolean canPlayerOpenDoor(UUID playerId, Location location) {
        DoorClaim claim = claimedDoors.get(getDoorBottomLocation(location));
        return claim != null && claim.owner.equals(playerId);
    }

    public boolean claimDoor(UUID owner, Location location) {
        Location doorLoc = getDoorBottomLocation(location);
        if (isDoorClaimed(doorLoc)) return false;
        DoorClaim claim = new DoorClaim(owner, 0);
        claimedDoors.put(doorLoc, claim);
        saveClaimedDoors();
        return true;
    }

    public void unclaimDoor(Location location) {
        claimedDoors.remove(getDoorBottomLocation(location));
        saveClaimedDoors();
    }

    public int getHitCount(Location location) {
        DoorClaim claim = claimedDoors.get(getDoorBottomLocation(location));
        return claim != null ? claim.hitCount : 0;
    }

    public void incrementHitCount(Location location) {
        DoorClaim claim = claimedDoors.get(getDoorBottomLocation(location));
        if (claim != null) {
            claim.hitCount++;
            if (claim.hitCount >= 40) {
                unclaimDoor(location);
            } else {
                saveClaimedDoors();
            }
        }
    }

    private void loadClaimedDoors() {
        if (!doorConfig.contains("doors")) return;
        for (String key : doorConfig.getConfigurationSection("doors").getKeys(false)) {
            String[] parts = key.split(",");
            if (parts.length == 4) {
                try {
                    String world = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                    UUID owner = UUID.fromString(doorConfig.getString("doors." + key + ".owner"));
                    int hitCount = doorConfig.getInt("doors." + key + ".hitCount", 0);
                    claimedDoors.put(loc, new DoorClaim(owner, hitCount));
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load door claim: " + key);
                }
            }
        }
    }

    private void saveClaimedDoors() {
        doorConfig.set("doors", null);
        for (Map.Entry<Location, DoorClaim> entry : claimedDoors.entrySet()) {
            Location loc = entry.getKey();
            DoorClaim claim = entry.getValue();
            String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
            doorConfig.set("doors." + key + ".owner", claim.owner.toString());
            doorConfig.set("doors." + key + ".hitCount", claim.hitCount);
        }
        try {
            doorConfig.save(doorFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save claimed doors: " + e.getMessage());
        }
    }

    private Location getDoorBottomLocation(Location location) {
        if (isDoorMaterial(location.getBlock().getType())) {
            Location below = location.clone().subtract(0, 1, 0);
            if (isDoorMaterial(below.getBlock().getType())) {
                return below;
            }
        }
        return location;
    }

    private boolean isDoorMaterial(Material material) {
        return material.name().endsWith("_DOOR");
    }

    private static class DoorClaim {
        UUID owner;
        int hitCount;

        DoorClaim(UUID owner, int hitCount) {
            this.owner = owner;
            this.hitCount = hitCount;
        }
    }
}