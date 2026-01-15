package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.DoorManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class DoorListener implements Listener {

    private final ExtractionPlugin plugin;
    private final DoorManager doorManager;

    public DoorListener(ExtractionPlugin plugin, DoorManager doorManager) {
        this.plugin = plugin;
        this.doorManager = doorManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || !isDoorMaterial(block.getType())) return;

        Player player = event.getPlayer();

        // Check if door is claimed
        if (doorManager.isDoorClaimed(block.getLocation())) {
            if (!doorManager.canPlayerOpenDoor(player.getUniqueId(), block.getLocation())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "This door is claimed by another player.");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isDoorMaterial(block.getType())) return;

        if (doorManager.isDoorClaimed(block.getLocation())) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            int currentHits = doorManager.getHitCount(block.getLocation());
            doorManager.incrementHitCount(block.getLocation());
            int newHits = doorManager.getHitCount(block.getLocation());
            if (newHits == 0 && currentHits >= 39) { // Unclaimed after 40th hit
                player.sendMessage(ChatColor.GREEN + "Door unclaimed!");
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
                // Allow break now
                event.setCancelled(false);
            } else {
                int hitsLeft = 40 - newHits;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.YELLOW + "Hitting door... " + hitsLeft + " hits left"));
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOOD_HIT, 1.0f, 1.0f);
            }
        }
    }



    private boolean isDoorMaterial(Material material) {
        return material.name().endsWith("_DOOR");
    }
}