package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Particle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeaponListener implements Listener {

    private final ExtractionPlugin plugin;
    private final Map<UUID, BukkitRunnable> autoFireTasks = new HashMap<>();

    public WeaponListener(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Handle Automatic Bow toggle
        NamespacedKey autoBowKey = new NamespacedKey(plugin, "automatic_bow");
        if (container.has(autoBowKey, PersistentDataType.BYTE)) {
            toggleAutomaticBow(player, item, container);
            event.setCancelled(true);
            return;
        }

        // Handle Burst Crossbow
        NamespacedKey burstKey = new NamespacedKey(plugin, "burst_crossbow");
        if (container.has(burstKey, PersistentDataType.BYTE)) {
            fireBurstCrossbow(player, item, container);
            event.setCancelled(true);
            return;
        }

        // Handle Flamethrower
        NamespacedKey flamethrowerKey = new NamespacedKey(plugin, "flamethrower");
        if (container.has(flamethrowerKey, PersistentDataType.BYTE)) {
            fireFlamethrower(player, item, container);
            event.setCancelled(true);
            return;
        }

        // Handle Rocket Launcher
        NamespacedKey rocketKey = new NamespacedKey(plugin, "rocket_launcher");
        if (container.has(rocketKey, PersistentDataType.BYTE)) {
            fireRocket(player, item, container);
            event.setCancelled(true);
            return;
        }

        // Handle Lightning Staff
        NamespacedKey lightningKey = new NamespacedKey(plugin, "lightning_staff");
        if (container.has(lightningKey, PersistentDataType.BYTE)) {
            castLightning(player, item, container);
            event.setCancelled(true);
            return;
        }

        // Handle Freeze Gun
        NamespacedKey freezeKey = new NamespacedKey(plugin, "freeze_gun");
        if (container.has(freezeKey, PersistentDataType.BYTE)) {
            freezeTargets(player, item, container);
            event.setCancelled(true);
            return;
        }

        // Handle Shotgun
        NamespacedKey shotgunKey = new NamespacedKey(plugin, "shotgun");
        if (container.has(shotgunKey, PersistentDataType.BYTE)) {
            fireShotgun(player, item, container);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack bow = player.getInventory().getItemInMainHand();

        if (bow == null || !bow.hasItemMeta()) {
            return;
        }

        ItemMeta meta = bow.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Handle Automatic Bow automatic shooting
        NamespacedKey autoBowKey = new NamespacedKey(plugin, "automatic_bow");
        if (container.has(autoBowKey, PersistentDataType.BYTE)) {
            NamespacedKey autoFireKey = new NamespacedKey(plugin, "auto_fire");
            if (container.has(autoFireKey, PersistentDataType.BYTE)) {
                byte autoFire = container.get(autoFireKey, PersistentDataType.BYTE);
                if (autoFire == 1) {
                    startAutomaticFire(player, bow);
                }
            }
        }

        // Handle Ricochet Bow
        NamespacedKey ricochetKey = new NamespacedKey(plugin, "ricochet_bow");
        if (container.has(ricochetKey, PersistentDataType.BYTE)) {
            markArrowAsRicochet((Projectile) event.getProjectile());
        }

        // Handle Explosive Arrows - check if the arrow itself is explosive
        if (event.getProjectile() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getProjectile();
            // Check if player is shooting explosive arrows from inventory
            if (arrow.getFireTicks() > 0) {
                markArrowAsExplosive(arrow);
            }
        }
        
        // Also check if bow has explosive arrow capability
        NamespacedKey explosiveKey = new NamespacedKey(plugin, "explosive_arrow");
        if (container.has(explosiveKey, PersistentDataType.BYTE)) {
            markArrowAsExplosive((Projectile) event.getProjectile());
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        
        // Handle ricochet arrows (handle first)
        if (projectile.hasMetadata("ricochet")) {
            handleRicochet(projectile, event);
            return;
        }

        // Handle explosive rockets (check before explosive arrows)
        if (projectile.hasMetadata("rocket")) {
            createLargeExplosion(projectile.getLocation());
            projectile.remove();
            return;
        }

        // Handle explosive arrows (handle after ricochet check)
        if (projectile.hasMetadata("explosive")) {
            createExplosion(projectile.getLocation());
            projectile.remove();
            return;
        }
        
        // Check if player is shooting explosive arrows from inventory
        if (projectile.getShooter() instanceof Player) {
            Player player = (Player) projectile.getShooter();
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            
            if (heldItem != null && heldItem.hasItemMeta()) {
                ItemMeta meta = heldItem.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                
                NamespacedKey explosiveKey = new NamespacedKey(plugin, "explosive_arrow");
                if (container.has(explosiveKey, PersistentDataType.BYTE)) {
                    createExplosion(projectile.getLocation());
                    projectile.remove();
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        ItemStack weapon = damager.getInventory().getItemInMainHand();

        if (weapon == null || !weapon.hasItemMeta()) {
            return;
        }

        ItemMeta meta = weapon.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Handle Poison Dagger
        NamespacedKey poisonKey = new NamespacedKey(plugin, "poison_dagger");
        if (container.has(poisonKey, PersistentDataType.BYTE)) {
            applyPoisonEffect(event.getEntity());
        }
    }

    private void toggleAutomaticBow(Player player, ItemStack bow, PersistentDataContainer container) {
        NamespacedKey autoFireKey = new NamespacedKey(plugin, "auto_fire");
        byte autoFire = container.getOrDefault(autoFireKey, PersistentDataType.BYTE, (byte) 0);
        
        if (autoFire == 0) {
            container.set(autoFireKey, PersistentDataType.BYTE, (byte) 1);
            player.sendMessage(ChatColor.GREEN + "Automatic Fire: " + ChatColor.BOLD + "ENABLED");
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
        } else {
            container.set(autoFireKey, PersistentDataType.BYTE, (byte) 0);
            player.sendMessage(ChatColor.RED + "Automatic Fire: " + ChatColor.BOLD + "DISABLED");
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 0.5f);
        }

        ItemMeta meta = bow.getItemMeta();
        meta.getPersistentDataContainer().set(autoFireKey, PersistentDataType.BYTE, autoFire == 0 ? (byte) 1 : (byte) 0);
        bow.setItemMeta(meta);
    }

    private void startAutomaticFire(Player player, ItemStack bow) {
        UUID playerId = player.getUniqueId();
        
        // Cancel existing task if running
        if (autoFireTasks.containsKey(playerId)) {
            autoFireTasks.get(playerId).cancel();
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !player.isHandRaised()) {
                    this.cancel();
                    autoFireTasks.remove(playerId);
                    return;
                }

                // Check player still has automatic bow
                ItemStack currentBow = player.getInventory().getItemInMainHand();
                if (currentBow == null || !currentBow.hasItemMeta()) {
                    this.cancel();
                    autoFireTasks.remove(playerId);
                    return;
                }

                ItemMeta meta = currentBow.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                NamespacedKey autoFireKey = new NamespacedKey(plugin, "auto_fire");
                if (!container.has(autoFireKey, PersistentDataType.BYTE) ||
                    container.get(autoFireKey, PersistentDataType.BYTE) == 0) {
                    this.cancel();
                    autoFireTasks.remove(playerId);
                    return;
                }

                // Shoot arrow
                shootArrow(player, 1.0f);
            }
        };

        task.runTaskTimer(plugin, 0L, 3L); // Shoot every 3 ticks (rapid fire)
        autoFireTasks.put(playerId, task);
    }

    private void fireBurstCrossbow(Player player, ItemStack crossbow, PersistentDataContainer container) {
        NamespacedKey ammoKey = new NamespacedKey(plugin, "burst_ammo");
        Integer shots = container.getOrDefault(ammoKey, PersistentDataType.INTEGER, 8);
        
        if (shots <= 0) {
            player.sendMessage(ChatColor.RED + "Out of burst ammo! Reload needed.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Shoot 8 arrows in a spread pattern
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        
        for (int i = 0; i < 8; i++) {
            // Create spread
            double angle = (i - 3.5) * 0.1; // Spread between shots
            Vector spreadDir = direction.clone()
                .rotateAroundY(angle);
            
            shootArrowInDirection(player, eyeLoc, spreadDir, 1.5f);
        }

        // Update ammo count
        container.set(ammoKey, PersistentDataType.INTEGER, shots - 8);
        ItemMeta meta = crossbow.getItemMeta();
        meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, shots - 8);
        crossbow.setItemMeta(meta);

        player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1.0f, 1.0f);
        player.sendMessage(ChatColor.YELLOW + "Burst fired! " + (shots - 8) + " shots remaining.");
    }

    private void shootArrow(Player player, float power) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(player.getLocation().getDirection().multiply(power * 3.0));
        arrow.setDamage(2.0);
    }

    private void shootArrowInDirection(Player player, Location start, Vector direction, float power) {
        Arrow arrow = player.getWorld().spawnArrow(start, direction, power, 12.0f);
        arrow.setShooter(player);
        arrow.setDamage(3.0);
    }

    private void markArrowAsRicochet(Projectile projectile) {
        projectile.setMetadata("ricochet", new FixedMetadataValue(plugin, true));
        projectile.setMetadata("ricochet_count", new FixedMetadataValue(plugin, 0));
    }

    private void markArrowAsExplosive(Projectile projectile) {
        projectile.setMetadata("explosive", new FixedMetadataValue(plugin, true));
    }

    private void handleRicochet(Projectile projectile, ProjectileHitEvent event) {
        if (projectile.hasMetadata("ricochet_count")) {
            int bounceCount = projectile.getMetadata("ricochet_count").get(0).asInt();
            
            if (bounceCount == 0 && event.getHitBlock() != null) {
                // First bounce - bounce off the block
                Vector currentVel = projectile.getVelocity();
                Location hitLoc = event.getHitBlock().getLocation();
                
                // Calculate bounce direction (simple reflection)
                Vector bounceDir = currentVel.clone();
                
                // Determine which face was hit and bounce accordingly
                if (event.getHitBlockFace() != null) {
                    switch (event.getHitBlockFace()) {
                        case UP:
                            bounceDir.setY(Math.abs(bounceDir.getY()));
                            break;
                        case DOWN:
                            bounceDir.setY(-Math.abs(bounceDir.getY()));
                            break;
                        case NORTH:
                            bounceDir.setZ(Math.abs(bounceDir.getZ()));
                            break;
                        case SOUTH:
                            bounceDir.setZ(-Math.abs(bounceDir.getZ()));
                            break;
                        case EAST:
                            bounceDir.setX(Math.abs(bounceDir.getX()));
                            break;
                        case WEST:
                            bounceDir.setX(-Math.abs(bounceDir.getX()));
                            break;
                    }
                }
                
                // Create a new arrow for the bounce
                Arrow ricochetArrow = projectile.getWorld().spawnArrow(
                    projectile.getLocation().add(bounceDir.normalize()),
                    bounceDir.normalize().multiply(2.0),
                    2.0f,
                    0.0f
                );
                ricochetArrow.setShooter(projectile.getShooter());
                ricochetArrow.setDamage(3.0);
                ricochetArrow.setMetadata("ricochet", new FixedMetadataValue(plugin, true));
                ricochetArrow.setMetadata("ricochet_count", new FixedMetadataValue(plugin, 1));
                
                // Remove original arrow
                projectile.remove();
                
                // Visual effect
                hitLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, hitLoc, 10, 0.5, 0.5, 0.5, 0.1);
                hitLoc.getWorld().playSound(hitLoc, Sound.BLOCK_STONE_STEP, 0.5f, 1.5f);
            }
        }
    }

    private void createExplosion(Location loc) {
        loc.getWorld().createExplosion(loc, 2.0f, false, false); // Small explosion, doesn't destroy blocks
        
        // Visual effects
        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 20, 1.0, 1.0, 1.0, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }

    private void createLargeExplosion(Location loc) {
        loc.getWorld().createExplosion(loc, 4.0f, true, true); // Large explosion, destroys blocks
        
        // Visual effects
        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 40, 2.0, 2.0, 2.0, 0.2);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
    }

    private void applyPoisonEffect(Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            
            // Apply poison effect
            living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1, false)); // 5 seconds, level 1
            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, false)); // 5 seconds, level 1
            
            // Visual effect
            living.getWorld().spawnParticle(Particle.SWEEP_ATTACK, living.getLocation(), 15, 0.5, 0.5, 0.5, 0.1);
            living.getWorld().playSound(living.getLocation(), Sound.ENTITY_SPIDER_HURT, 0.8f, 1.5f);
        }
    }

    private void fireFlamethrower(Player player, ItemStack flamethrower, PersistentDataContainer container) {
        NamespacedKey fuelKey = new NamespacedKey(plugin, "flamethrower_fuel");
        Integer fuel = container.getOrDefault(fuelKey, PersistentDataType.INTEGER, 0);
        
        if (fuel <= 0) {
            player.sendMessage(ChatColor.RED + "Out of fuel! Reload needed.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Create flame effect
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        
        // Spawn fire particles in cone shape
        for (int i = 0; i < 15; i++) {
            double spread = 0.2;
            Vector spreadDir = direction.clone()
                .add(new Vector(
                    (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * spread
                ));
            
            Location fireLoc = eyeLoc.add(spreadDir.multiply(i * 0.5));
            player.getWorld().spawnParticle(Particle.FLAME, fireLoc, 5, 0.1, 0.1, 0.1, 0.05);
            player.getWorld().spawnParticle(Particle.SMOKE, fireLoc, 2, 0.1, 0.1, 0.1, 0.02);
            
            // Damage entities in flame path
            for (Entity entity : player.getWorld().getNearbyEntities(fireLoc, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity living = (LivingEntity) entity;
                    living.damage(1.0, player);
                    living.setFireTicks(60); // 3 seconds of fire
                }
            }
        }

        // Update fuel
        container.set(fuelKey, PersistentDataType.INTEGER, fuel - 1);
        ItemMeta meta = flamethrower.getItemMeta();
        meta.getPersistentDataContainer().set(fuelKey, PersistentDataType.INTEGER, fuel - 1);
        flamethrower.setItemMeta(meta);

        player.playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 1.0f);
        player.sendMessage(ChatColor.YELLOW + "Flamethrower fired! " + (fuel - 1) + " fuel remaining.");
    }

    private void fireRocket(Player player, ItemStack launcher, PersistentDataContainer container) {
        NamespacedKey ammoKey = new NamespacedKey(plugin, "rocket_ammo");
        Integer ammo = container.getOrDefault(ammoKey, PersistentDataType.INTEGER, 0);
        
        if (ammo <= 0) {
            player.sendMessage(ChatColor.RED + "Out of rockets! Reload needed.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Launch rocket
        Arrow rocket = player.launchProjectile(Arrow.class);
        rocket.setVelocity(player.getLocation().getDirection().multiply(2.5));
        rocket.setDamage(5.0);
        rocket.setFireTicks(1000); // Keep fire effect
        rocket.setMetadata("rocket", new FixedMetadataValue(plugin, true));

        // Update ammo
        container.set(ammoKey, PersistentDataType.INTEGER, ammo - 1);
        ItemMeta meta = launcher.getItemMeta();
        meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, ammo - 1);
        launcher.setItemMeta(meta);

        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
        player.sendMessage(ChatColor.RED + "Rocket launched! " + (ammo - 1) + " rockets remaining.");
    }

    private void castLightning(Player player, ItemStack staff, PersistentDataContainer container) {
        NamespacedKey chargesKey = new NamespacedKey(plugin, "lightning_charges");
        Integer charges = container.getOrDefault(chargesKey, PersistentDataType.INTEGER, 0);
        
        if (charges <= 0) {
            player.sendMessage(ChatColor.RED + "Out of charges! Staff needs to recharge.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Cast lightning
        org.bukkit.block.Block targetBlock = player.getTargetBlockExact(50);
        Location target = targetBlock != null ? targetBlock.getLocation() : player.getLocation();
        if (target != null) {
            // Strike lightning at target location
            player.getWorld().strikeLightningEffect(target);
            
            // Damage entities near strike
            for (Entity entity : player.getWorld().getNearbyEntities(target, 3.0, 3.0, 3.0)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity living = (LivingEntity) entity;
                    living.damage(8.0, player);
                    
                    // Chain lightning effect
                    for (Entity nearby : player.getWorld().getNearbyEntities(living.getLocation(), 5.0, 5.0, 5.0)) {
                        if (nearby instanceof LivingEntity && nearby != player && nearby != living) {
                            ((LivingEntity) nearby).damage(4.0, player);
                            nearby.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, nearby.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
                            break; // Only chain to one additional target
                        }
                    }
                }
            }
            
            // Visual effects
            target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target, 30, 1.0, 1.0, 1.0, 0.2);
        }

        // Update charges
        container.set(chargesKey, PersistentDataType.INTEGER, charges - 1);
        ItemMeta meta = staff.getItemMeta();
        meta.getPersistentDataContainer().set(chargesKey, PersistentDataType.INTEGER, charges - 1);
        staff.setItemMeta(meta);

        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Lightning cast! " + (charges - 1) + " charges remaining.");
    }

    private void freezeTargets(Player player, ItemStack gun, PersistentDataContainer container) {
        NamespacedKey chargesKey = new NamespacedKey(plugin, "freeze_charges");
        Integer charges = container.getOrDefault(chargesKey, PersistentDataType.INTEGER, 0);
        
        if (charges <= 0) {
            player.sendMessage(ChatColor.RED + "Out of freeze charges! Reload needed.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Create freeze beam
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        
        for (int i = 0; i < 20; i++) {
            Location beamLoc = eyeLoc.add(direction.clone().multiply(i * 0.5));
            
            // Freeze effects
            player.getWorld().spawnParticle(Particle.WHITE_ASH, beamLoc, 5, 0.2, 0.2, 0.2, 0.1);
            player.getWorld().spawnParticle(Particle.CLOUD, beamLoc, 3, 0.1, 0.1, 0.1, 0.05);
            
            // Freeze entities in beam path
            for (Entity entity : player.getWorld().getNearbyEntities(beamLoc, 1.5, 1.5, 1.5)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity living = (LivingEntity) entity;
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 5, false)); // 10 seconds, high level
                    living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 2, false)); // 10 seconds, level 2
                    
                    // Visual freeze effect
                    living.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, living.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }

        // Update charges
        container.set(chargesKey, PersistentDataType.INTEGER, charges - 1);
        ItemMeta meta = gun.getItemMeta();
        meta.getPersistentDataContainer().set(chargesKey, PersistentDataType.INTEGER, charges - 1);
        gun.setItemMeta(meta);

        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        player.sendMessage(ChatColor.AQUA + "Freeze ray fired! " + (charges - 1) + " charges remaining.");
    }

    private void fireShotgun(Player player, ItemStack shotgun, PersistentDataContainer container) {
        NamespacedKey shellsKey = new NamespacedKey(plugin, "shotgun_shells");
        Integer shells = container.getOrDefault(shellsKey, PersistentDataType.INTEGER, 0);
        
        if (shells <= 0) {
            player.sendMessage(ChatColor.RED + "Out of shells! Reload needed.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Fire shotgun spread
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        
        for (int i = 0; i < 8; i++) {
            // Create spread pattern
            double spread = 0.15;
            Vector spreadDir = direction.clone()
                .add(new Vector(
                    (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * spread
                ));
            
            Arrow pellet = player.getWorld().spawnArrow(eyeLoc, spreadDir, 2.5f, 12.0f);
            pellet.setShooter(player);
            pellet.setDamage(1.5); // Each pellet does 1.5 damage
            pellet.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY); // Can't be picked up
        }

        // Update shells
        container.set(shellsKey, PersistentDataType.INTEGER, shells - 1);
        ItemMeta meta = shotgun.getItemMeta();
        meta.getPersistentDataContainer().set(shellsKey, PersistentDataType.INTEGER, shells - 1);
        shotgun.setItemMeta(meta);

        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
        player.sendMessage(ChatColor.GRAY + "Shotgun fired! " + (shells - 1) + " shells remaining.");
    }

    public void cleanup() {
        for (BukkitRunnable task : autoFireTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        autoFireTasks.clear();
    }
}