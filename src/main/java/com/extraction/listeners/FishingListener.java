package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.loot.LootTableManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Barrel;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class FishingListener implements Listener {
    private final ExtractionPlugin plugin;
    private final LootTableManager lootTableManager;
    private final Random random = new Random();

    public FishingListener(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.lootTableManager = plugin.getLootTableManager();
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            event.setCancelled(true); // Cancel default fish

            Player player = event.getPlayer();
            Item caughtItem = (Item) event.getCaught();

            // Remove the default item
            caughtItem.remove();

            // Chance to catch different things
            double rand = random.nextDouble();
            Location catchLoc = caughtItem.getLocation();
            if (rand < 0.3) { // 30% chance for barrel
                ItemStack barrel = createLootBarrel();
                animateCatch(player, barrel, catchLoc, false); // barrels not rare
            } else if (rand < 0.6) { // 30% chance for multiple items
                List<ItemStack> loot = lootTableManager.generateLootForTable("default");
                for (ItemStack item : loot) {
                    boolean isRare = isRareItem(item);
                    animateCatch(player, item, catchLoc, isRare);
                }
            } else { // 40% chance for single item
                List<ItemStack> loot = lootTableManager.generateLootForTable("default");
                if (!loot.isEmpty()) {
                    boolean isRare = isRareItem(loot.get(0));
                    animateCatch(player, loot.get(0), catchLoc, isRare);
                }
            }
        }
    }

    private ItemStack createLootBarrel() {
        ItemStack barrelItem = new ItemStack(Material.BARREL);
        BlockStateMeta meta = (BlockStateMeta) barrelItem.getItemMeta();
        if (meta != null) {
            Barrel barrel = (Barrel) meta.getBlockState();
            // Fill with random loot
            List<ItemStack> loot = lootTableManager.generateLootForTable("default");
            for (int i = 0; i < loot.size() && i < 27; i++) {
                barrel.getInventory().setItem(i, loot.get(i));
            }
            meta.setBlockState(barrel);
            barrelItem.setItemMeta(meta);
        }
        return barrelItem;
    }

    private boolean isRareItem(ItemStack item) {
        Material mat = item.getType();
        return mat == Material.DIAMOND || mat == Material.EMERALD || mat == Material.GOLDEN_APPLE ||
               mat == Material.IRON_HELMET || mat == Material.IRON_CHESTPLATE || mat.name().contains("DIAMOND") ||
               mat.name().contains("ENCHANTED");
    }

    private void animateCatch(Player player, ItemStack item, Location startLoc, boolean isRare) {
        Location loc = startLoc.clone().add(0, 1, 0); // Float above water
        ItemDisplay display = (ItemDisplay) player.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.ITEM_DISPLAY);
        display.setItemStack(item);
        display.setItemDisplayTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.GROUND);

        Location targetLoc = player.getLocation().add(0, 1, 0); // Above player
        Vector direction = targetLoc.toVector().subtract(loc.toVector()).normalize();

        new BukkitRunnable() {
            int ticks = 0;
            final int totalTicks = isRare ? 80 : 60; // Longer for rare

            @Override
            public void run() {
                if (ticks >= totalTicks) {
                    // Drop item and remove display
                    player.getWorld().dropItem(display.getLocation(), item);
                    display.remove();
                    cancel();
                    return;
                }

                if (ticks < 20) { // First second: float and spin above water
                    double offsetY = Math.sin(ticks * 0.3) * 0.2;
                    Location newLoc = loc.clone();
                    newLoc.setY(loc.getY() + offsetY);
                    display.teleport(newLoc);
                    display.setRotation(display.getLocation().getYaw() + (isRare ? 15 : 10), display.getLocation().getPitch());
                    player.getWorld().spawnParticle(Particle.CRIT, display.getLocation(), isRare ? 10 : 5, 0.2, 0.2, 0.2, 0);
                } else { // Move towards player
                    double progress = (ticks - 20.0) / (totalTicks - 20.0);
                    Location currentLoc = loc.clone().add(direction.clone().multiply(progress * loc.distance(targetLoc)));
                    currentLoc.setY(loc.getY() + Math.sin(progress * Math.PI) * 0.5); // Arc motion
                    display.teleport(currentLoc);
                    display.setRotation(display.getLocation().getYaw() + (isRare ? 20 : 10), display.getLocation().getPitch());
                    if (isRare) {
                        player.getWorld().spawnParticle(Particle.CRIT, display.getLocation(), 8, 0.1, 0.1, 0.1, 0.1);
                    }
                    player.getWorld().spawnParticle(Particle.CRIT, display.getLocation(), isRare ? 8 : 5, 0.3, 0.3, 0.3, 0);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}