package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.extract.ExtractManager;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BannerListener implements Listener {

    private final ExtractionPlugin plugin;
    private final ExtractManager extractManager;
    private final NamespacedKey extractionBannerKey;
    private final NamespacedKey extractionFlareKey;
    private final NamespacedKey emergencyTeleporterKey;
    private final Map<Location, Boolean> placedExtractionBanners =
        new HashMap<>();

    public BannerListener(
        ExtractionPlugin plugin,
        ExtractManager extractManager
    ) {
        this.plugin = plugin;
        this.extractManager = extractManager;
        this.extractionBannerKey = new NamespacedKey(
            plugin,
            "extraction_banner"
        );
        this.extractionFlareKey = new NamespacedKey(plugin, "extraction_flare");
        this.emergencyTeleporterKey = new NamespacedKey(
            plugin,
            "emergency_teleporter"
        );
    }

    @EventHandler
    public void onBannerPlace(BlockPlaceEvent event) {
        if (!(event.getBlock().getState() instanceof Banner)) return;

        ItemStack item = event.getItemInHand();
        if (item != null && item.getType().name().endsWith("_BANNER")) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                PersistentDataContainer container =
                    meta.getPersistentDataContainer();
                if (
                    container.has(extractionBannerKey, PersistentDataType.BYTE)
                ) {
                    placedExtractionBanners.put(
                        event.getBlock().getLocation(),
                        true
                    );
                    event
                        .getPlayer()
                        .sendMessage(
                            ChatColor.YELLOW +
                                "Extraction banner placed! Break it to start extraction."
                        );
                }
            }
        }
    }

    @EventHandler
    public void onBannerBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Banner)) return;

        Location loc = event.getBlock().getLocation();
        if (placedExtractionBanners.containsKey(loc)) {
            Player player = event.getPlayer();
            event.setCancelled(true);
            event.setDropItems(false);
            event.getBlock().setType(Material.AIR);
            placedExtractionBanners.remove(loc);

            player.sendMessage(
                ChatColor.GREEN + "Easy Extraction activated! Don't move!"
            );
            extractManager.initiateBannerExtraction(player, null);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (
            event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK
        ) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Check for Extraction Flare
        if (container.has(extractionFlareKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);

            if (extractManager.isExtracting(player.getUniqueId())) {
                player.sendMessage(
                    ChatColor.RED + "You are already extracting!"
                );
                return;
            }

            // Consume one flare
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            player.sendMessage(
                ChatColor.GOLD +
                    "Extraction Flare activated! Fast extraction in progress..."
            );
            initiateFlareExtraction(player);
            return;
        }

        // Check for Emergency Teleporter
        if (container.has(emergencyTeleporterKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);

            if (extractManager.isExtracting(player.getUniqueId())) {
                player.sendMessage(
                    ChatColor.RED + "You are already extracting!"
                );
                return;
            }

            // Consume one teleporter
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            player.sendMessage(
                ChatColor.LIGHT_PURPLE +
                    "Emergency Teleporter activated! Instant extraction!"
            );
            instantExtraction(player);
        }
    }

    private void initiateFlareExtraction(Player player) {
        // Flare extraction - 10 seconds instead of 20, with movement check
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 10;
            final Location startLoc = player.getLocation().clone();

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                // Check if player moved
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
                    cancel();
                    return;
                }

                if (count == 0) {
                    player.sendActionBar(
                        ChatColor.GREEN + "Extracted! Sending to lobby..."
                    );
                    extractManager.extractPlayer(player, true);
                    cancel();
                    return;
                }

                player.sendActionBar(
                    ChatColor.GOLD +
                        "Flare Extraction: " +
                        count +
                        " seconds... " +
                        ChatColor.RED +
                        "Don't move!"
                );
                count--;
            }
        }
            .runTaskTimer(plugin, 0L, 20L);
    }

    private void instantExtraction(Player player) {
        // Instant extraction - no wait
        player.sendActionBar(ChatColor.LIGHT_PURPLE + "EMERGENCY EXTRACTION!");
        extractManager.extractPlayer(player, true);
    }
}
