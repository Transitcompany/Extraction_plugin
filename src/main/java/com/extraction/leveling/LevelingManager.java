package com.extraction.leveling;

import com.extraction.ExtractionPlugin;
import com.extraction.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelingManager implements Listener {
    private final ExtractionPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, Integer> lastNotifiedLevel = new HashMap<>();

    public LevelingManager(ExtractionPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.PlayerData data = playerDataManager.getPlayerData(player);
        data.updateLastSeen();
    }

    public void addSellXp(Player player, double sellAmount) {
        PlayerDataManager.PlayerData data = playerDataManager.getPlayerData(player);
        double xpAmount = calculateSellXp(sellAmount);
        data.addXp(xpAmount);
        data.incrementItemsSold();
        data.addMoneyEarned(sellAmount);
        
        checkLevelUp(player, data);
        
        // XP gain notification
        if (xpAmount >= 1) {
            showXpGain(player, xpAmount);
        }
    }

    public void addExtractionXp(Player player) {
        PlayerDataManager.PlayerData data = playerDataManager.getPlayerData(player);
        double xpAmount = calculateExtractionXp(data.getLevel());
        data.addXp(xpAmount);
        data.incrementExtractionsCompleted();
        
        checkLevelUp(player, data);
        showXpGain(player, xpAmount);
    }

    public void addAuctionXp(Player player, boolean isSeller, boolean sold) {
        PlayerDataManager.PlayerData data = playerDataManager.getPlayerData(player);
        double xpAmount = calculateAuctionXp(isSeller, sold);
        
        if (xpAmount > 0) {
            data.addXp(xpAmount);
            if (isSeller) {
                data.incrementAuctionsCreated();
                if (sold) {
                    data.addMoneyEarned(getAuctionPrice(player));
                }
            } else {
                data.incrementAuctionsWon();
                data.addMoneySpent(getAuctionPrice(player));
            }
            
            checkLevelUp(player, data);
            if (xpAmount >= 1) {
                showXpGain(player, xpAmount);
            }
        }
    }

    private void checkLevelUp(Player player, PlayerDataManager.PlayerData data) {
        int currentLevel = data.getLevel();
        UUID playerUuid = player.getUniqueId();
        Integer lastLevel = lastNotifiedLevel.get(playerUuid);
        
        if (lastLevel == null) {
            lastNotifiedLevel.put(playerUuid, currentLevel);
            return;
        }
        
        if (currentLevel > lastLevel) {
            int levelsGained = currentLevel - lastLevel;
            lastNotifiedLevel.put(playerUuid, currentLevel);
            
            // Play level up sounds
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            
            // Create level up animation
            createLevelUpAnimation(player, currentLevel, levelsGained);
            
            // Show level up message in action bar
            if (levelsGained == 1) {
                sendActionBar(player, "§6§lLEVEL UP! §eYou are now level " + currentLevel + "!");
            } else {
                sendActionBar(player, "§6§lLEVEL UP! §e+" + levelsGained + " levels! Now level " + currentLevel + "!");
            }
            
            // Additional bonuses for milestone levels
            checkMilestoneRewards(player, currentLevel);
        }
    }

    private void createLevelUpAnimation(Player player, int newLevel, int levelsGained) {
        // Create floating level up message
        for (int i = 0; i < 3; i++) {
            final int soundIndex = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendActionBar("§6§l» §eLEVEL UP! §6§l« §6Level " + newLevel);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f + (soundIndex * 0.2f), 0.8f);
            }, i * 10L);
        }
        
        // Show bonus information
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            double sellBonus = (getSellMultiplier(player) - 1.0) * 100;
            double extractBonus = (getExtractionBonus(player) - 1.0) * 100;
            sendActionBar(player, "§aNew Bonuses: §e+" + String.format("%.0f", sellBonus) + "% Sell §7| §e+" + String.format("%.0f", extractBonus) + "% Extract");
        }, 60L);
    }

    private void checkMilestoneRewards(Player player, int level) {
        // Milestone levels with special rewards
        if (level == 5) {
            sendActionBar(player, "§aMilestone! §eUnlocked §b+5% Sell Bonus §eat Level 5!");
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.5f);
        } else if (level == 10) {
            sendActionBar(player, "§aMilestone! §eUnlocked §d+10% Sell Bonus §eat Level 10!");
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.5f);
        } else if (level == 25) {
            sendActionBar(player, "§aMilestone! §eUnlocked §6+25% Sell Bonus §eat Level 25!");
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.5f);
        } else if (level == 50) {
            sendActionBar(player, "§aMilestone! §eUnlocked §cElite Status §eat Level 50!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.2f);
        } else if (level == 75) {
            sendActionBar(player, "§aMilestone! §eUnlocked §5Master Extractor Status §eat Level 75!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.2f);
        } else if (level == 100) {
            sendActionBar(player, "§6§lMAX LEVEL! §eYou've reached the pinnacle!");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
        }
    }

    private void showXpGain(Player player, double xpAmount) {
        if (xpAmount >= 50) {
            sendActionBar(player, "§a+" + String.format("%.1f", xpAmount) + " XP §e(Huge Gain!)");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
        } else if (xpAmount >= 20) {
            sendActionBar(player, "§a+" + String.format("%.1f", xpAmount) + " XP §e(Big Gain!)");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.2f);
        } else if (xpAmount >= 5) {
            sendActionBar(player, "§a+" + String.format("%.1f", xpAmount) + " XP");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        }
    }

    private void sendActionBar(Player player, String message) {
        player.sendActionBar(message);
    }

    private double calculateSellXp(double sellAmount) {
        return Math.max(1, sellAmount * 0.1);
    }

    private double calculateExtractionXp(int playerLevel) {
        return 10 + (playerLevel * 2);
    }

    private double calculateAuctionXp(boolean isSeller, boolean sold) {
        if (isSeller && sold) return 15;
        if (!isSeller) return 5;
        return 0;
    }

    private int getPreviousLevel(double totalXp) {
        int level = 1;
        double xpNeeded = 100;
        
        while (totalXp >= xpNeeded && level < 100) {
            totalXp -= xpNeeded;
            level++;
            xpNeeded = 100 * Math.pow(1.1, level - 1);
        }
        
        return level;
    }

    private double getAuctionPrice(Player player) {
        return 0;
    }

    public double getSellMultiplier(Player player) {
        int level = playerDataManager.getPlayerData(player).getLevel();
        return 1.0 + (level * 0.01);
    }

    public double getExtractionBonus(Player player) {
        int level = playerDataManager.getPlayerData(player).getLevel();
        return 1.0 + (level * 0.005);
    }

    public double getSellMultiplier(int level) {
        return 1.0 + (level * 0.01);
    }

    public double getExtractionBonus(int level) {
        return 1.0 + (level * 0.005);
    }
}