package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.ChestManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ChestListener implements Listener {

    private final ExtractionPlugin plugin;
    private final ChestManager chestManager;

    public ChestListener(ExtractionPlugin plugin, ChestManager chestManager) {
        this.plugin = plugin;
        this.chestManager = chestManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || !chestManager.isChestMaterial(block.getType())) return;

        Player player = event.getPlayer();

        if (block.getState() instanceof org.bukkit.block.Chest) {
            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
            if (chest.getInventory().getHolder() instanceof org.bukkit.block.DoubleChest) {
                // Double chest: check both sides
                org.bukkit.block.DoubleChest dc = (org.bukkit.block.DoubleChest) chest.getInventory().getHolder();
                Location left = ((org.bukkit.block.Chest) dc.getLeftSide()).getLocation();
                Location right = ((org.bukkit.block.Chest) dc.getRightSide()).getLocation();
                boolean canAccess = (!chestManager.isChestClaimed(left) || chestManager.canPlayerAccessChest(player.getUniqueId(), left)) &&
                                    (!chestManager.isChestClaimed(right) || chestManager.canPlayerAccessChest(player.getUniqueId(), right));
                if (!canAccess) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "This chest is claimed by another player.");
                }
            } else {
                // Single chest
                if (chestManager.isChestClaimed(block.getLocation()) && !chestManager.canPlayerAccessChest(player.getUniqueId(), block.getLocation())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "This chest is claimed by another player.");
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (!chestManager.isChestMaterial(block.getType())) return;

        // Check adjacent blocks for claimed chests
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // Skip the block itself
                    Block adjacent = block.getRelative(x, y, z);
                    if (chestManager.isChestMaterial(adjacent.getType()) && chestManager.isChestClaimed(adjacent.getLocation())) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot place a chest next to a claimed chest.");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!chestManager.isChestMaterial(block.getType())) return;

        if (chestManager.isChestClaimed(block.getLocation())) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            int currentHits = chestManager.getHitCount(block.getLocation());
            chestManager.incrementHitCount(block.getLocation());
            int newHits = chestManager.getHitCount(block.getLocation());
            if (newHits == 0 && currentHits >= 249) { // Unclaimed after 250th hit
                player.sendMessage(ChatColor.GREEN + "Chest unclaimed!");
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
                // Allow break now
                event.setCancelled(false);
            } else {
                int hitsLeft = 250 - newHits;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.YELLOW + "Hitting chest... " + hitsLeft + " hits left"));
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOOD_HIT, 1.0f, 1.0f);
            }
        }
    }
}