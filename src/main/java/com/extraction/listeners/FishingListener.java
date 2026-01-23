package com.extraction.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Cod;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.TropicalFish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FishingListener implements Listener {

    private static final NamespacedKey FISHED_KEY = new NamespacedKey("extraction", "fished");
    private static final List<Class<? extends Entity>> FISH_TYPES = Arrays.asList(Cod.class, Salmon.class, TropicalFish.class, PufferFish.class);
    private static final Random random = new Random();

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (event.getCaught() instanceof Item) {
                Item caughtItem = (Item) event.getCaught();
                caughtItem.remove(); // Remove the item
                // Spawn a random fish at the hook location
                Class<? extends Entity> fishType = FISH_TYPES.get(random.nextInt(FISH_TYPES.size()));
                Entity fish = event.getHook().getWorld().spawn(event.getHook().getLocation(), fishType);
                fish.getPersistentDataContainer().set(FISHED_KEY, PersistentDataType.BOOLEAN, true);
                // Set velocity towards the player with upward boost to reach land
                Vector direction = event.getPlayer().getLocation().toVector().subtract(fish.getLocation().toVector()).normalize();
                fish.setVelocity(direction.multiply(0.8).add(new Vector(0, 0.5, 0))); // Moderate pull with slight upward force
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if ((entity instanceof Cod || entity instanceof Salmon || entity instanceof TropicalFish || entity instanceof PufferFish)
            && entity.getPersistentDataContainer().has(FISHED_KEY, PersistentDataType.BOOLEAN)) {
            // Determine the item material
            Material itemMaterial;
            if (entity instanceof Cod) itemMaterial = Material.COD;
            else if (entity instanceof Salmon) itemMaterial = Material.SALMON;
            else if (entity instanceof TropicalFish) itemMaterial = Material.TROPICAL_FISH;
            else if (entity instanceof PufferFish) itemMaterial = Material.PUFFERFISH;
            else return; // Should not happen

            // Give player the fish item
            event.getPlayer().getInventory().addItem(new ItemStack(itemMaterial));
            // Play sound and particles
            event.getPlayer().playSound(entity.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 5, 0.5, 0.5, 0.5, 0.1);
            // Remove the entity
            entity.remove();
            event.setCancelled(true); // Prevent other interactions
        }
    }
}