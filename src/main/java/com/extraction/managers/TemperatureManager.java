package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemperatureManager {
    private final ExtractionPlugin plugin;
    private final Map<UUID, Double> temperatures = new HashMap<>();
    private final Map<UUID, Boolean> overheatedWarned = new HashMap<>();
    private final Map<UUID, Boolean> frozenWarned = new HashMap<>();
    private String lobbyWorld = "world";
    private boolean heatwave = false;
    private boolean coldwave = false;
    private int eventTimer = 0;

    public TemperatureManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        startTemperatureTask();
    }

    private void startTemperatureTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                eventTimer++;
                if (eventTimer >= 72000) { // 1 hour
                    eventTimer = 0;
                    if (Math.random() < 0.3) { // 30% chance
                        if (Math.random() < 0.5) {
                            startHeatwave();
                        } else {
                            startColdwave();
                        }
                    } else {
                        endEvents();
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateTemperature(player);
                    displayTemperature(player);
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Every second
    }

    public void startHeatwave() {
        heatwave = true;
        coldwave = false;
        Bukkit.broadcastMessage(ChatColor.RED + "A heatwave has begun! Stay cool!");
    }

    public void startColdwave() {
        coldwave = true;
        heatwave = false;
        // Make it snow - set weather
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            world.setStorm(true);
            world.setThundering(false);
        }
        Bukkit.broadcastMessage(ChatColor.AQUA + "A coldwave has begun! Stay warm!");
    }

    public void endEvents() {
        heatwave = false;
        coldwave = false;
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            world.setStorm(false);
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "The weather event has ended.");
    }

    private void updateTemperature(Player player) {
        if (player.getWorld().getName().equals(lobbyWorld)) return; // No effects in lobby

        UUID uuid = player.getUniqueId();
        double temp = temperatures.getOrDefault(uuid, 25.0); // Start at 25

        long time = player.getWorld().getTime();
        boolean isDay = time > 0 && time < 12000;

        // Base temps
        if (isDay) {
            temp += (50 - temp) * 0.01; // Approach 50
        } else {
            temp += (25 - temp) * 0.01; // Approach 25
        }

        // Events
        if (heatwave) {
            temp += 2;
        } else if (coldwave) {
            temp -= 2;
        }

        // Heat sources
        int heatSources = 0;
        Location loc = player.getLocation();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Material mat = loc.clone().add(x, y, z).getBlock().getType();
                    if (mat == Material.FIRE || mat == Material.SOUL_FIRE || mat == Material.CAMPFIRE || mat == Material.SOUL_CAMPFIRE) {
                        heatSources += 2;
                    } else if (mat == Material.LAVA) {
                        heatSources += 5;
                    }
                }
            }
        }
        temp += heatSources;

        // Shade
        if (player.getEyeLocation().getBlock().getType() != Material.AIR) {
            temp -= 10;
        }

        // Leather armor
        int leatherPieces = 0;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType().name().startsWith("LEATHER_")) {
                leatherPieces++;
            }
        }
        temp -= leatherPieces * 0.5;

        // In water
        if (player.getLocation().getBlock().getType() == Material.WATER) {
            temp -= 3;
        }

        // Rain
        if (player.getWorld().hasStorm()) {
            temp -= 1;
        }

        // Clamp realistic
        temp = Math.max(-10, Math.min(80, temp));

        temperatures.put(uuid, temp);

        // Effects
        if (temp > 60) {
            // Powerful heat effects
            try {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 2)); // Stronger nausea
            } catch (Exception e) {}
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1)); // Weakness
            if (temp > 70) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1)); // Mining fatigue
                player.setFoodLevel(Math.max(0, player.getFoodLevel() - 1)); // Hunger loss
            }
            if (!overheatedWarned.getOrDefault(uuid, false)) {
                player.sendMessage(ChatColor.DARK_RED + "You're overheating! Seek shade or water!");
                overheatedWarned.put(uuid, true);
            }
        } else {
            overheatedWarned.put(uuid, false);
        }

        if (temp < -5) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            if (!frozenWarned.getOrDefault(uuid, false)) {
                player.sendMessage(ChatColor.AQUA + "You're freezing!");
                frozenWarned.put(uuid, true);
            }
        } else {
            frozenWarned.put(uuid, false);
        }
    }

    private void displayTemperature(Player player) {
        if (player.getWorld().getName().equals(lobbyWorld)) return; // No display in lobby

        double temp = temperatures.getOrDefault(player.getUniqueId(), 0.0);
        ChatColor color = (heatwave && temp > 50) ? ChatColor.DARK_RED : temp > 60 ? ChatColor.RED : temp > 50 ? ChatColor.GOLD : temp < -20 ? ChatColor.BLUE : ChatColor.WHITE;
        String tempStr = String.format("%.1f°C", temp);
        String event = heatwave ? " [HEATWAVE]" : coldwave ? " [COLDWAVE]" : "";
        TextComponent message = new TextComponent("Temperature: " + color + tempStr + event);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
    }

    public void drinkWater(Player player) {
        double temp = temperatures.getOrDefault(player.getUniqueId(), 0.0);
        temp -= 20; // Cool down
        temperatures.put(player.getUniqueId(), temp);
        player.sendMessage(ChatColor.AQUA + "You feel refreshed!");
    }

    public double getTemperature(UUID uuid) {
        return temperatures.getOrDefault(uuid, 0.0);
    }
}