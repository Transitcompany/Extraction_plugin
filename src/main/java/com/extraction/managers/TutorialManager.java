package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TutorialManager {

    private final ExtractionPlugin plugin;
    private final Map<UUID, Integer> tutorialStep = new HashMap<>();

    public TutorialManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }

    public void startTutorial(Player player) {
        UUID uuid = player.getUniqueId();
        tutorialStep.put(uuid, 1);

        // Welcome message
        player.sendMessage(ChatColor.GOLD + "Welcome to Extraction! Let's get you started.");
        player.sendMessage(ChatColor.YELLOW + "You've received starter items. Check your inventory!");

        // Give starter items
        player.getInventory().addItem(new ItemStack(Material.APPLE, 5));
        plugin.getEconomyManager().addBalance(uuid, 100); // Give $100
        player.sendMessage(ChatColor.GREEN + "You received $100 and some basic items!");

        // Schedule next steps
        new BukkitRunnable() {
            @Override
            public void run() {
                if (tutorialStep.get(uuid) == 1) {
                    player.sendMessage(ChatColor.AQUA + "Tutorial: To sell items and earn money, use /sell");
                    tutorialStep.put(uuid, 2);
                }
            }
        }.runTaskLater(plugin, 600L); // 30 seconds

        new BukkitRunnable() {
            @Override
            public void run() {
                if (tutorialStep.get(uuid) == 2) {
                    player.sendMessage(ChatColor.AQUA + "Tutorial: To safely store your items, use /stash");
                    tutorialStep.put(uuid, 3);
                }
            }
        }.runTaskLater(plugin, 1800L); // 1.5 minutes

        new BukkitRunnable() {
            @Override
            public void run() {
                if (tutorialStep.get(uuid) == 3) {
                    player.sendMessage(ChatColor.AQUA + "Tutorial: To check your money, use /balance");
                    player.sendMessage(ChatColor.AQUA + "Tutorial: To view auctions, use /auction browse");
                    tutorialStep.put(uuid, 4);
                }
            }
        }.runTaskLater(plugin, 3000L); // 2.5 minutes

        new BukkitRunnable() {
            @Override
            public void run() {
                if (tutorialStep.get(uuid) == 4) {
                    player.sendMessage(ChatColor.GREEN + "Tutorial complete! Explore, survive, and extract. Use /help for more commands.");
                    tutorialStep.remove(uuid);
                }
            }
        }.runTaskLater(plugin, 3600L); // 3 minutes
    }

    public void advanceTutorial(UUID uuid) {
        // Optional: advance on command usage, but for now, timer-based
    }
}