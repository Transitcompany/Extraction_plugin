package com.extraction.extract;

import com.extraction.ExtractionPlugin;
import com.extraction.leveling.LevelingManager;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class ExtractManager {

    private final ExtractionPlugin plugin;
    private final LevelingManager levelingManager;
    private final Map<Location, Boolean> extractPoints = new HashMap<>();
    private final Map<Location, Boolean> extractOutBanners = new HashMap<>();
    private final List<Location> extractToPoints = new ArrayList<>();
    private final Map<UUID, Location> extractingPlayers = new HashMap<>();
    private final Map<UUID, BukkitRunnable> extractionTasks = new HashMap<>();
    private String extractWorld = null;
    private String lobbyWorld = null;
    private File extractDataFile;
    private YamlConfiguration extractDataConfig;

    public ExtractManager(ExtractionPlugin plugin, LevelingManager levelingManager) {
        this.plugin = plugin;
        this.levelingManager = levelingManager;
        loadExtractPoints();
        startMovementChecker();
    }

    private void loadExtractPoints() {
        extractDataFile = new File(plugin.getDataFolder(), "extract.yml");
        if (!extractDataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                extractDataFile.createNewFile();
            } catch (IOException ignored) {}
        }
        extractDataConfig = YamlConfiguration.loadConfiguration(
            extractDataFile
        );

        if (
            extractDataConfig.getConfigurationSection("extractPoints") != null
        ) {
            for (String key : extractDataConfig
                .getConfigurationSection("extractPoints")
                .getKeys(false)) {
                Location loc = deserializeLocation(key);
                if (loc != null) {
                    extractPoints.put(loc, true);
                }
            }
        }
        if (
            extractDataConfig.getConfigurationSection("extractOutBanners") !=
            null
        ) {
            for (String key : extractDataConfig
                .getConfigurationSection("extractOutBanners")
                .getKeys(false)) {
                Location loc = deserializeLocation(key);
                if (loc != null) {
                    extractOutBanners.put(loc, true);
                }
            }
        }
        if (extractDataConfig.getList("extractToPoints") != null) {
            for (String key : extractDataConfig.getStringList(
                "extractToPoints"
            )) {
                Location loc = deserializeLocation(key);
                if (loc != null) {
                    extractToPoints.add(loc);
                }
            }
        }
        extractWorld = extractDataConfig.getString("extractWorld");
        lobbyWorld = extractDataConfig.getString("lobbyWorld");
    }

    public void saveExtractPoints() {
        extractDataConfig.set("extractPoints", null);
        extractDataConfig.set("extractOutBanners", null);
        for (Location loc : extractPoints.keySet()) {
            extractDataConfig.set(
                "extractPoints." + serializeLocation(loc),
                true
            );
        }
        for (Location loc : extractOutBanners.keySet()) {
            extractDataConfig.set(
                "extractOutBanners." + serializeLocation(loc),
                true
            );
        }
        List<String> extractToPointsList = new ArrayList<>();
        for (Location loc : extractToPoints) {
            extractToPointsList.add(serializeLocation(loc));
        }
        extractDataConfig.set("extractToPoints", extractToPointsList);
        extractDataConfig.set("extractWorld", extractWorld);
        extractDataConfig.set("lobbyWorld", lobbyWorld);
        try {
            extractDataConfig.save(extractDataFile);
        } catch (IOException ignored) {}
    }

    private void startMovementChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, Location>> iterator = extractingPlayers
                    .entrySet()
                    .iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, Location> entry = iterator.next();
                    UUID playerId = entry.getKey();
                    Location startLoc = entry.getValue();
                    Player player = Bukkit.getPlayer(playerId);

                    if (player == null || !player.isOnline()) {
                        cancelExtraction(playerId);
                        iterator.remove();
                        continue;
                    }

                    Location currentLoc = player.getLocation();
                    double distance = Math.sqrt(
                        Math.pow(currentLoc.getX() - startLoc.getX(), 2) +
                            Math.pow(currentLoc.getY() - startLoc.getY(), 2) +
                            Math.pow(currentLoc.getZ() - startLoc.getZ(), 2)
                    );

                    if (distance > 0.5) {
                        player.sendMessage(
                            ChatColor.RED + "Extraction cancelled! You moved!"
                        );
                        player.sendActionBar(
                            ChatColor.RED + "Extraction cancelled!"
                        );
                        cancelExtraction(playerId);
                        iterator.remove();
                    }
                }
            }
        }
            .runTaskTimer(plugin, 0L, 5L);
    }

    private void cancelExtraction(UUID playerId) {
        BukkitRunnable task = extractionTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
        extractingPlayers.remove(playerId);
    }

    public void registerExtractPoint(Location location) {
        extractPoints.put(location, true);
        saveExtractPoints();
    }

    public boolean isExtractPoint(Location loc) {
        return extractPoints.containsKey(loc);
    }

    public Set<Location> getExtractPoints() {
        return extractPoints.keySet();
    }

    public void registerExtractOutBanner(Location location) {
        extractOutBanners.put(location, true);
        saveExtractPoints();
    }

    public boolean isExtractOutBanner(Location loc) {
        return extractOutBanners.containsKey(loc);
    }

    public boolean isValidExtractionBanner(ItemStack item) {
        if (item == null || !item.getType().name().endsWith("_BANNER")) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "extraction_banner");
        return container.has(key, PersistentDataType.BYTE);
    }

    public void initiateExtraction(Player player, Location bannerLoc) {
        if (!isExtractPoint(bannerLoc)) {
            player.sendMessage(ChatColor.RED + "Not an extraction point.");
            return;
        }

        if (extractingPlayers.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already extracting!");
            return;
        }

        extractingPlayers.put(
            player.getUniqueId(),
            player.getLocation().clone()
        );

        BukkitRunnable task = new BukkitRunnable() {
            int count = 20;

            @Override
            public void run() {
                if (!extractingPlayers.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }

                if (count == 0) {
player.sendActionBar(
                        ChatColor.GREEN + "Extracted! Sending to lobby..."
                    );
                    extractingPlayers.remove(player.getUniqueId());
                    extractionTasks.remove(player.getUniqueId());
                    
                    levelingManager.addExtractionXp(player);
                    
                    extractPlayerToLobby(player);
                    cancel();
                    return;
                }
                if (!player.isOnline()) {
                    extractingPlayers.remove(player.getUniqueId());
                    extractionTasks.remove(player.getUniqueId());
                    cancel();
                    return;
                }
            }
        };

        extractionTasks.put(player.getUniqueId(), task);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void initiateBannerExtraction(Player player, ItemStack bannerItem) {
        // If bannerItem is null, it means the banner was already validated (placed and broken)
        // If bannerItem is provided, validate it
        if (bannerItem != null && !isValidExtractionBanner(bannerItem)) {
            player.sendMessage(
                ChatColor.RED + "This banner cannot be used for extraction!"
            );
            return;
        }

        if (extractingPlayers.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already extracting!");
            return;
        }

        extractingPlayers.put(
            player.getUniqueId(),
            player.getLocation().clone()
        );

        BukkitRunnable task = new BukkitRunnable() {
            int count = 15;

            @Override
            public void run() {
                if (!extractingPlayers.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }

                if (count == 0) {
                    player.sendActionBar(
                        ChatColor.GREEN + "Extracted! Sending to lobby..."
                    );
                    extractingPlayers.remove(player.getUniqueId());
                    extractionTasks.remove(player.getUniqueId());
                    extractPlayerToLobby(player);
                    cancel();
                    return;
                }
                if (!player.isOnline()) {
                    extractingPlayers.remove(player.getUniqueId());
                    extractionTasks.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                player.sendActionBar(
                    ChatColor.GREEN +
                        "Banner Extraction: " +
                        count +
                        " seconds... " +
                        ChatColor.RED +
                        "Don't move!"
                );
                count--;
            }
        };

        extractionTasks.put(player.getUniqueId(), task);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void extractPlayerToLobby(Player player) {
        if (lobbyWorld == null || Bukkit.getWorld(lobbyWorld) == null) {
            player.sendMessage(ChatColor.RED + "No lobby world set!");
            return;
        }

        World world = Bukkit.getWorld(lobbyWorld);
        Location spawn = world.getSpawnLocation().clone();
        spawn.setY(spawn.getY() + 1);

        player.teleport(spawn);
    }

    public void extractPlayer(Player player, boolean toLobby) {
        if (toLobby) {
            extractPlayerToLobby(player);
        } else {
            if (
                !extractToPoints.isEmpty() &&
                extractWorld != null &&
                Bukkit.getWorld(extractWorld) != null
            ) {
                Random random = new Random();
                Location randomPoint = extractToPoints
                    .get(random.nextInt(extractToPoints.size()))
                    .clone();
                World world = Bukkit.getWorld(extractWorld);
                if (world != null) {
                             randomPoint.setWorld(world);
                    player.teleport(randomPoint);

                    // Deploy sound
                    player.playSound(randomPoint, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }
            } else {
                extractPlayerToLobby(player);
            }
        }
    }

    public void registerExtractToPoint(Location location) {
        if (extractToPoints.size() >= 30) {
            return;
        }
        extractToPoints.add(location);
        saveExtractPoints();
    }

    public int getExtractToPointCount() {
        return extractToPoints.size();
    }

    public String getLobbyWorld() {
        return lobbyWorld;
    }

    public String getExtractWorld() {
        return extractWorld;
    }

    public void initiateExtractOut(Player player, Location bannerLoc) {
        if (!isExtractOutBanner(bannerLoc)) {
            player.sendMessage(ChatColor.RED + "Not an extract out banner.");
            return;
        }
        if (extractWorld == null || Bukkit.getWorld(extractWorld) == null) {
            player.sendMessage(ChatColor.RED + "No extract world set!");
            return;
        }

        if (extractingPlayers.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already extracting!");
            return;
        }

        extractingPlayers.put(
            player.getUniqueId(),
            player.getLocation().clone()
        );

        BukkitRunnable task = new BukkitRunnable() {
            int count = 20;

            @Override
            public void run() {
                if (!extractingPlayers.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }

                if (count == 0) {
                    player.sendActionBar(
                        ChatColor.GREEN + "Deploying to extraction zone..."
                    );
                    extractingPlayers.remove(player.getUniqueId());
                    extractionTasks.remove(player.getUniqueId());

                    World world = Bukkit.getWorld(extractWorld);
                    if (world != null) {
                        if (!extractToPoints.isEmpty()) {
                            Random random = new Random();
                            Location randomPoint = extractToPoints
                                .get(random.nextInt(extractToPoints.size()))
                                .clone();
                            randomPoint.setWorld(world);
                    player.teleport(randomPoint);

                    // Deploy sound
                    player.playSound(randomPoint, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                        } else {
                            Location spawn = world.getSpawnLocation().clone();
                            spawn.setY(spawn.getY() + 1);
                            player.teleport(spawn);

                            // Deploy sound
                            player.playSound(spawn, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                        }
                    }
                    cancel();
                    return;
                }
                if (!player.isOnline()) {
                    extractingPlayers.remove(player.getUniqueId());
                    extractionTasks.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                player.sendActionBar(
                    ChatColor.YELLOW +
                        "Deploying in: " +
                        count +
                        " seconds... " +
                        ChatColor.RED +
                        "Don't move!"
                );

                // Play countdown sounds
                if (count == 10 || count == 5 || count <= 3) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }

                count--;
            }
        };

        extractionTasks.put(player.getUniqueId(), task);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void setWorld(String which, String worldName) {
        if (which.equalsIgnoreCase("lobby")) {
            lobbyWorld = worldName;
        } else if (which.equalsIgnoreCase("extract")) {
            extractWorld = worldName;
        }
        saveExtractPoints();
    }

    public boolean isExtracting(UUID playerId) {
        return extractingPlayers.containsKey(playerId);
    }

    private String serializeLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return "null";
        return (
            loc.getWorld().getName() +
            "," +
            loc.getBlockX() +
            "," +
            loc.getBlockY() +
            "," +
            loc.getBlockZ()
        );
    }

    private Location deserializeLocation(String s) {
        if (s == null || s.equals("null")) return null;
        String[] p = s.split(",");
        if (p.length < 4) return null;
        World w = Bukkit.getWorld(p[0]);
        if (w == null) return null;
        return new Location(
            w,
            Integer.parseInt(p[1]),
            Integer.parseInt(p[2]),
            Integer.parseInt(p[3])
        );
    }


}
