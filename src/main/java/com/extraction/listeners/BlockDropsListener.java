package com.extraction.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class BlockDropsListener implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (type == Material.STONE) {
            // Drop 1-3 extra cobblestone, total 2-4 (since stone normally drops 1 cobble)
            int extra = 1 + random.nextInt(3);
            for (int i = 0; i < extra; i++) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.COBBLESTONE));
            }
        } else if (type.name().endsWith("_LOG")) {
            // Drop 0-2 extra logs, total 1-3
            int extra = random.nextInt(3);
            for (int i = 0; i < extra; i++) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(type));
            }
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        if (event.getSource().getType() == Material.COBBLESTONE) {
            event.setCancelled(true);
        }
    }
}