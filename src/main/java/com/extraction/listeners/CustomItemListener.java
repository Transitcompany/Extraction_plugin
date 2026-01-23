package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.crypto.CryptoManager;
import com.extraction.economy.EconomyManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.event.EventPriority;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CustomItemListener implements Listener {

    private final ExtractionPlugin plugin;
    private final EconomyManager economyManager;
    private final CryptoManager cryptoManager;

    // Cooldowns
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    // Players with revive syringe active
    private final Set<UUID> playersWithRevive = new HashSet<>();

    // Active money printers: Location -> Owner UUID
    private final Map<Location, UUID> activeMoneyPrinters = new HashMap<>();
    private final Map<Location, Integer> moneyPrinterTasks = new HashMap<>();

    // Active landmines/bear traps: Location -> Owner UUID
    private final Map<Location, UUID> activeLandmines = new HashMap<>();
    private final Map<Location, UUID> activeBearTraps = new HashMap<>();

    // Thrown projectiles tracking
    private final Map<Entity, String> thrownItems = new HashMap<>();
    private final Map<Entity, UUID> thrownOwners = new HashMap<>();

    // Players with force field active
    private final Set<UUID> forceFieldActive = new HashSet<>();

    // Players with camo cloak
    private final Set<UUID> camoActive = new HashSet<>();

    // Placed C4 locations
    private final Map<UUID, Location> placedC4 = new HashMap<>();

    public CustomItemListener(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        this.cryptoManager = plugin.getCryptoManager();
    }

    private NamespacedKey key(String name) {
        return new NamespacedKey(plugin, name);
    }

    private boolean hasKey(ItemStack item, String keyName) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta
            .getPersistentDataContainer()
            .has(key(keyName), PersistentDataType.BYTE);
    }

    private boolean isOnCooldown(UUID uuid, String item, long cooldownMs) {
        String key = uuid.toString() + ":" + item;
        if (cooldowns.containsKey(uuid)) {
            long lastUse = cooldowns.get(uuid);
            return (System.currentTimeMillis() - lastUse) < cooldownMs;
        }
        return false;
    }

    private void setCooldown(UUID uuid, String item) {
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    private void consumeItem(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (
            event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK &&
            event.getAction() != Action.LEFT_CLICK_AIR &&
            event.getAction() != Action.LEFT_CLICK_BLOCK
        ) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        // Check all special items
        if (hasKey(item, "smoke_bomb")) {
            event.setCancelled(true);
            useSmokeBomb(player, item);
        } else if (hasKey(item, "tracker_compass")) {
            event.setCancelled(true);
            useTrackerCompass(player, item);
        } else if (hasKey(item, "med_kit")) {
            event.setCancelled(true);
            useMedKit(player, item);
        } else if (hasKey(item, "jetpack")) {
            event.setCancelled(true);
            useJetpack(player, item);
        } else if (hasKey(item, "adrenaline_shot")) {
            event.setCancelled(true);
            useAdrenalineShot(player, item);
        } else if (hasKey(item, "speed_powder")) {
            event.setCancelled(true);
            useSpeedPowder(player, item);
        } else if (hasKey(item, "invisibility_cloak")) {
            event.setCancelled(true);
            useInvisibilityCloak(player, item);
        } else if (hasKey(item, "revive_syringe")) {
            event.setCancelled(true);
            useReviveSyringe(player, item);
        } else if (hasKey(item, "emp_grenade")) {
            event.setCancelled(true);
            useEMPGrenade(player, item);
        } else if (hasKey(item, "throwing_knife")) {
            event.setCancelled(true);
            useThrowingKnife(player, item);
        } else if (hasKey(item, "poison_dart")) {
            event.setCancelled(true);
            usePoisonDart(player, item);
        } else if (hasKey(item, "frag_grenade")) {
            event.setCancelled(true);
            useFragGrenade(player, item);
        } else if (hasKey(item, "molotov")) {
            event.setCancelled(true);
            useMolotov(player, item);
        } else if (hasKey(item, "teleport_pearl")) {
            event.setCancelled(true);
            useTeleportPearl(player, item);
        } else if (hasKey(item, "thermal_scanner")) {
            event.setCancelled(true);
            useThermalScanner(player, item);
        } else if (hasKey(item, "portable_shield")) {
            event.setCancelled(true);
            usePortableShield(player, item);
        } else if (hasKey(item, "decoy_grenade")) {
            event.setCancelled(true);
            useDecoyGrenade(player, item);
        } else if (hasKey(item, "footstep_silencer")) {
            event.setCancelled(true);
            useFootstepSilencer(player, item);
        } else if (hasKey(item, "radar_jammer")) {
            event.setCancelled(true);
            useRadarJammer(player, item);
        } else if (hasKey(item, "team_heal_kit")) {
            event.setCancelled(true);
            useTeamHealKit(player, item);
        } else if (hasKey(item, "supply_drop")) {
            event.setCancelled(true);
            useSupplyDrop(player, item);
        } else if (hasKey(item, "c4_explosive")) {
            event.setCancelled(true);
            useC4(player, item);
        } else if (hasKey(item, "force_field")) {
            event.setCancelled(true);
            useForceField(player, item);
        } else if (hasKey(item, "armor_plate")) {
            event.setCancelled(true);
            useArmorPlate(player, item);
        } else if (hasKey(item, "crypto_wallet")) {
            event.setCancelled(true);
            useCryptoWallet(player, item);
        } else if (hasKey(item, "gps_trail_key")) {
            if (isOnCooldown(player.getUniqueId(), "gps", 5000L)) {
                player.sendMessage(ChatColor.RED + "GPS is on cooldown! Wait 5 seconds.");
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            setCooldown(player.getUniqueId(), "gps");
            useGpsTrailKey(player, item);
        } else if (hasKey(item, "medkit")) {
            if (isOnCooldown(player.getUniqueId(), "medkit", 10000L)) {
                player.sendMessage(ChatColor.RED + "Medkit is on cooldown! Wait 10 seconds.");
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            setCooldown(player.getUniqueId(), "medkit");
            useMedkit(player, item);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;

        Location loc = event.getBlock().getLocation();

        if (hasKey(item, "money_printer")) {
            placeMoneyPrinter(player, loc);
        } else if (hasKey(item, "landmine")) {
            placeLandmine(player, loc);
        } else if (hasKey(item, "bear_trap")) {
            placeBearTrap(player, loc);
        } else if (hasKey(item, "healing_station")) {
            placeHealingStation(player, event.getBlock());
        } else if (hasKey(item, "auto_turret")) {
            placeAutoTurret(player, event.getBlock());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();

        if (activeMoneyPrinters.containsKey(loc)) {
            event.setCancelled(true);
            event.setDropItems(false);
            activeMoneyPrinters.remove(loc);
            if (moneyPrinterTasks.containsKey(loc)) {
                Bukkit.getScheduler().cancelTask(moneyPrinterTasks.get(loc));
                moneyPrinterTasks.remove(loc);
            }
            event.getBlock().setType(Material.AIR);
            event
                .getPlayer()
                .sendMessage(ChatColor.RED + "Money Printer destroyed!");
        }

        if (activeLandmines.containsKey(loc)) {
            event.setCancelled(true);
            activeLandmines.remove(loc);
            event.getBlock().setType(Material.AIR);
            event
                .getPlayer()
                .sendMessage(ChatColor.YELLOW + "Landmine disarmed!");
        }

        if (activeBearTraps.containsKey(loc)) {
            event.setCancelled(true);
            activeBearTraps.remove(loc);
            event.getBlock().setType(Material.AIR);
            event
                .getPlayer()
                .sendMessage(ChatColor.YELLOW + "Bear trap disarmed!");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation().getBlock().getLocation();

        // Landmine check
        if (activeLandmines.containsKey(loc)) {
            UUID owner = activeLandmines.get(loc);
            if (!player.getUniqueId().equals(owner)) {
                activeLandmines.remove(loc);
                loc.getBlock().setType(Material.AIR);
                loc
                    .getWorld()
                    .createExplosion(
                        loc.add(0.5, 0.5, 0.5),
                        3.0f,
                        false,
                        false
                    );
                player.damage(8.0);
                player.sendMessage(
                    ChatColor.RED + "You stepped on a landmine!"
                );
            }
        }

        // Bear trap check
        if (activeBearTraps.containsKey(loc)) {
            UUID owner = activeBearTraps.get(loc);
            if (!player.getUniqueId().equals(owner)) {
                activeBearTraps.remove(loc);
                loc.getBlock().setType(Material.AIR);
                player.damage(8.0);
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOWNESS, 100, 10)
                );
                player.playSound(loc, Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 0.5f);
                player.sendMessage(
                    ChatColor.RED + "You're caught in a bear trap!"
                );
            }
        }

        // Camo cloak - break if moving
        if (camoActive.contains(player.getUniqueId())) {
            if (event.getFrom().distanceSquared(event.getTo()) > 0.01) {
                camoActive.remove(player.getUniqueId());
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.sendMessage(
                    ChatColor.YELLOW + "Camo broken - you moved!"
                );
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        ItemStack chest = player.getInventory().getChestplate();

        if (chest != null && hasKey(chest, "camo_cloak")) {
            if (event.isSneaking()) {
                camoActive.add(player.getUniqueId());
                player.addPotionEffect(
                    new PotionEffect(
                        PotionEffectType.INVISIBILITY,
                        Integer.MAX_VALUE,
                        0,
                        false,
                        false
                    )
                );
                player.sendMessage(
                    ChatColor.GREEN + "Camo activated - don't move!"
                );
            } else {
                camoActive.remove(player.getUniqueId());
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }

        // Rocket boots
        ItemStack boots = player.getInventory().getBoots();
        if (
            boots != null && hasKey(boots, "rocket_boots") && event.isSneaking()
        ) {
            player.setVelocity(new Vector(0, 1.5, 0));
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                1.0f,
                1.0f
            );
            player
                .getWorld()
                .spawnParticle(
                    Particle.FLAME,
                    player.getLocation(),
                    20,
                    0.3,
                    0.1,
                    0.3,
                    0.1
                );
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (hasKey(item, "grappling_hook")) {
            if (
                event.getState() == PlayerFishEvent.State.IN_GROUND ||
                event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY ||
                event.getState() == PlayerFishEvent.State.REEL_IN
            ) {
                Location hookLoc = event.getHook().getLocation();
                Vector direction = hookLoc
                    .toVector()
                    .subtract(player.getLocation().toVector());
                double distance = direction.length();

                if (distance > 2) {
                    direction
                        .normalize()
                        .multiply(Math.min(distance * 0.15, 2.5));
                    direction.setY(direction.getY() + 0.4);
                    player.setVelocity(direction);
                    player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_FISHING_BOBBER_RETRIEVE,
                        1.0f,
                        1.2f
                    );
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity projectile = event.getEntity();
        if (!thrownItems.containsKey(projectile)) return;

        String type = thrownItems.remove(projectile);
        UUID owner = thrownOwners.remove(projectile);
        Location loc = projectile.getLocation();
        World world = loc.getWorld();

        switch (type) {
            case "throwing_knife":
                if (event.getHitEntity() instanceof LivingEntity) {
                    ((LivingEntity) event.getHitEntity()).damage(12.0);
                }
                break;
            case "poison_dart":
                if (event.getHitEntity() instanceof LivingEntity) {
                    ((LivingEntity) event.getHitEntity()).addPotionEffect(
                        new PotionEffect(PotionEffectType.POISON, 200, 1)
                    );
                }
                break;
            case "frag_grenade":
                world.createExplosion(loc, 3.0f, false, false);
                for (Entity e : world.getNearbyEntities(loc, 5, 5, 5)) {
                    if (e instanceof Player && !e.getUniqueId().equals(owner)) {
                        ((Player) e).damage(10.0);
                    }
                }
                break;
            case "molotov":
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        Block b = loc.clone().add(x, 0, z).getBlock();
                        if (b.getType() == Material.AIR) {
                            b.setType(Material.FIRE);
                        }
                    }
                }
                world.playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
                break;
            case "decoy":
                // Play fake sounds
                new BukkitRunnable() {
                    int count = 0;

                    @Override
                    public void run() {
                        if (count >= 15) {
                            cancel();
                            return;
                        }
                        world.playSound(
                            loc,
                            Sound.ENTITY_PLAYER_HURT,
                            1.0f,
                            1.0f
                        );
                        world.playSound(
                            loc,
                            Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                            1.0f,
                            1.0f
                        );
                        count++;
                    }
                }
                    .runTaskTimer(plugin, 0L, 20L);
                break;
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Force field protection
        if (forceFieldActive.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // Revive syringe
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            if (playersWithRevive.contains(player.getUniqueId())) {
                event.setCancelled(true);
                playersWithRevive.remove(player.getUniqueId());
                player.setHealth(10.0);
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.RESISTANCE, 60, 4)
                );
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.REGENERATION, 100, 1)
                );
                player.playSound(
                    player.getLocation(),
                    Sound.ITEM_TOTEM_USE,
                    1.0f,
                    1.0f
                );
                player
                    .getWorld()
                    .spawnParticle(
                        Particle.TOTEM_OF_UNDYING,
                        player.getLocation().add(0, 1, 0),
                        50,
                        0.5,
                        1,
                        0.5,
                        0.5
                    );
                player.sendMessage(
                    ChatColor.GREEN +
                        "" +
                        ChatColor.BOLD +
                        "REVIVE SYRINGE ACTIVATED!"
                );
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        ItemStack item = attacker.getInventory().getItemInMainHand();

        // Stun baton
        if (
            hasKey(item, "stun_baton") &&
            event.getEntity() instanceof LivingEntity
        ) {
            LivingEntity target = (LivingEntity) event.getEntity();
            target.addPotionEffect(
                new PotionEffect(PotionEffectType.SLOWNESS, 60, 2)
            );
            target.addPotionEffect(
                new PotionEffect(PotionEffectType.WEAKNESS, 60, 1)
            );
            attacker.playSound(
                attacker.getLocation(),
                Sound.ENTITY_LIGHTNING_BOLT_IMPACT,
                0.5f,
                2.0f
            );
            target
                .getWorld()
                .spawnParticle(
                    Particle.ELECTRIC_SPARK,
                    target.getLocation().add(0, 1, 0),
                    20,
                    0.3,
                    0.5,
                    0.3,
                    0.1
                );
        }
    }

    // ========== ITEM IMPLEMENTATIONS ==========

    private void useSmokeBomb(Player player, ItemStack item) {
        consumeItem(player, item);
        Location loc = player.getLocation();
        World world = loc.getWorld();

        AreaEffectCloud cloud = world.spawn(loc, AreaEffectCloud.class);
        cloud.setRadius(5.0f);
        cloud.setDuration(200);
        cloud.setParticle(Particle.CAMPFIRE_COSY_SMOKE);
        cloud.addCustomEffect(
            new PotionEffect(PotionEffectType.BLINDNESS, 60, 0),
            true
        );
        cloud.addCustomEffect(
            new PotionEffect(PotionEffectType.POISON, 100, 1),
            true
        );

        player.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.5f);
        player.sendMessage(ChatColor.GRAY + "Smoke bomb deployed!");
    }

    private void useTrackerCompass(Player player, ItemStack item) {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (
                p.equals(player) || !p.getWorld().equals(player.getWorld())
            ) continue;
            double dist = p.getLocation().distance(player.getLocation());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = p;
            }
        }

        if (nearest != null) {
            if (item.getItemMeta() instanceof CompassMeta) {
                CompassMeta meta = (CompassMeta) item.getItemMeta();
                meta.setLodestoneTracked(false);
                meta.setLodestone(nearest.getLocation());
                item.setItemMeta(meta);
            }
            player.playSound(
                player.getLocation(),
                Sound.BLOCK_NOTE_BLOCK_PLING,
                1.0f,
                2.0f
            );
            player.sendMessage(
                ChatColor.RED +
                    "Tracking: " +
                    nearest.getName() +
                    " - " +
                    (int) nearestDist +
                    " blocks"
            );
        } else {
            player.sendMessage(ChatColor.RED + "No players to track!");
        }
    }

    private void useMedKit(Player player, ItemStack item) {
        double maxHealth = player
            .getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)
            .getValue();
        if (player.getHealth() >= maxHealth) {
            player.sendMessage(ChatColor.YELLOW + "Already at full health!");
            return;
        }

        consumeItem(player, item);
        player.setHealth(Math.min(player.getHealth() + 10, maxHealth));
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.REGENERATION, 100, 1)
        );
        player.removePotionEffect(PotionEffectType.POISON);
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_PLAYER_LEVELUP,
            1.0f,
            1.5f
        );
        player.sendMessage(ChatColor.GREEN + "Med Kit used! +5 hearts");
    }

    private void useJetpack(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int uses = pdc.getOrDefault(
            key("jetpack_uses"),
            PersistentDataType.INTEGER,
            3
        );

        if (uses <= 0) {
            player.sendMessage(ChatColor.RED + "Jetpack out of fuel!");
            return;
        }

        pdc.set(key("jetpack_uses"), PersistentDataType.INTEGER, uses - 1);
        List<String> lore = meta.getLore();
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).contains("Uses")) {
                    lore.set(
                        i,
                        ChatColor.YELLOW + "Uses remaining: " + (uses - 1)
                    );
                }
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);

        if (uses - 1 <= 0) {
            player.getInventory().setItemInMainHand(null);
        }

        Vector vel = player.getVelocity();
        vel.setY(1.2);
        vel.add(
            player
                .getLocation()
                .getDirection()
                .normalize()
                .setY(0)
                .multiply(0.5)
        );
        player.setVelocity(vel);
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
            1.0f,
            1.0f
        );
        player
            .getWorld()
            .spawnParticle(
                Particle.FLAME,
                player.getLocation(),
                20,
                0.2,
                0.1,
                0.2,
                0.1
            );
        player.sendMessage(
            ChatColor.AQUA + "Jetpack boost! " + (uses - 1) + " uses left"
        );
    }

    private void useAdrenalineShot(Player player, ItemStack item) {
        consumeItem(player, item);
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.SPEED, 300, 1)
        );
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.STRENGTH, 300, 0)
        );
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.JUMP_BOOST, 300, 1)
        );
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.RESISTANCE, 300, 0)
        );
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_EVOKER_PREPARE_ATTACK,
            1.0f,
            1.5f
        );
        player.sendMessage(
            ChatColor.YELLOW + "" + ChatColor.BOLD + "ADRENALINE RUSH!"
        );
    }

    private void useSpeedPowder(Player player, ItemStack item) {
        consumeItem(player, item);
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.SPEED, 400, 2)
        );
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.JUMP_BOOST, 400, 1)
        );
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.HASTE, 400, 1)
        );
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_WITCH_DRINK,
            1.0f,
            1.5f
        );
        player
            .getWorld()
            .spawnParticle(
                Particle.CLOUD,
                player.getLocation().add(0, 1, 0),
                20,
                0.3,
                0.3,
                0.3,
                0.1
            );
        player.sendMessage(
            ChatColor.WHITE + "" + ChatColor.BOLD + "SPEED POWDER ACTIVATED!"
        );

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.addPotionEffect(
                        new PotionEffect(PotionEffectType.SLOWNESS, 100, 1)
                    );
                    player.addPotionEffect(
                        new PotionEffect(PotionEffectType.NAUSEA, 100, 0)
                    );
                    player.sendMessage(
                        ChatColor.RED + "Speed powder wearing off..."
                    );
                }
            }
        }
            .runTaskLater(plugin, 400L);
    }

    private void useInvisibilityCloak(Player player, ItemStack item) {
        consumeItem(player, item);
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.INVISIBILITY, 600, 0)
        );
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_ILLUSIONER_CAST_SPELL,
            1.0f,
            1.0f
        );
        player
            .getWorld()
            .spawnParticle(
                Particle.WITCH,
                player.getLocation().add(0, 1, 0),
                30,
                0.5,
                0.5,
                0.5,
                0.1
            );
        player.sendMessage(
            ChatColor.LIGHT_PURPLE + "You vanish into thin air! (30 seconds)"
        );
    }

    private void useReviveSyringe(Player player, ItemStack item) {
        if (playersWithRevive.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "Revive already active!");
            return;
        }
        consumeItem(player, item);
        playersWithRevive.add(player.getUniqueId());
        player.playSound(
            player.getLocation(),
            Sound.BLOCK_BEACON_ACTIVATE,
            1.0f,
            1.5f
        );
        player.sendMessage(
            ChatColor.GREEN +
                "Revive Syringe ACTIVATED! You will auto-revive on death."
        );
    }

    private void useEMPGrenade(Player player, ItemStack item) {
        consumeItem(player, item);
        Location loc = player.getLocation();
        World world = loc.getWorld();

        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
        world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 1.5f, 0.5f);
        world.spawnParticle(
            Particle.ELECTRIC_SPARK,
            loc.add(0, 1, 0),
            100,
            5,
            3,
            5,
            0.1
        );

        for (Entity e : world.getNearbyEntities(loc, 10, 10, 10)) {
            if (e instanceof Player && !e.equals(player)) {
                Player target = (Player) e;
                for (PotionEffect effect : target.getActivePotionEffects()) {
                    target.removePotionEffect(effect.getType());
                }
                target.addPotionEffect(
                    new PotionEffect(PotionEffectType.MINING_FATIGUE, 200, 2)
                );
                target.addPotionEffect(
                    new PotionEffect(PotionEffectType.WEAKNESS, 200, 1)
                );
                target.addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOWNESS, 100, 0)
                );
                target.setCooldown(Material.SHIELD, 100);
                target.sendMessage(
                    ChatColor.RED + "You've been hit by an EMP!"
                );
            }
        }
        player.sendMessage(ChatColor.DARK_AQUA + "EMP activated!");
    }

    private void useThrowingKnife(Player player, ItemStack item) {
        consumeItem(player, item);
        Snowball proj = player.launchProjectile(Snowball.class);
        proj.setVelocity(player.getLocation().getDirection().multiply(2.0));
        thrownItems.put(proj, "throwing_knife");
        thrownOwners.put(proj, player.getUniqueId());
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_SNOWBALL_THROW,
            1.0f,
            0.5f
        );
    }

    private void usePoisonDart(Player player, ItemStack item) {
        consumeItem(player, item);
        Snowball proj = player.launchProjectile(Snowball.class);
        proj.setVelocity(player.getLocation().getDirection().multiply(2.5));
        thrownItems.put(proj, "poison_dart");
        thrownOwners.put(proj, player.getUniqueId());
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_ARROW_SHOOT,
            1.0f,
            2.0f
        );
    }

    private void useFragGrenade(Player player, ItemStack item) {
        consumeItem(player, item);
        Snowball proj = player.launchProjectile(Snowball.class);
        proj.setVelocity(player.getLocation().getDirection().multiply(1.5));
        thrownItems.put(proj, "frag_grenade");
        thrownOwners.put(proj, player.getUniqueId());
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_SNOWBALL_THROW,
            1.0f,
            0.8f
        );
        player.sendMessage(ChatColor.RED + "Frag out!");
    }

    private void useMolotov(Player player, ItemStack item) {
        consumeItem(player, item);
        Snowball proj = player.launchProjectile(Snowball.class);
        proj.setVelocity(player.getLocation().getDirection().multiply(1.2));
        thrownItems.put(proj, "molotov");
        thrownOwners.put(proj, player.getUniqueId());
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_SNOWBALL_THROW,
            1.0f,
            0.6f
        );
    }

    private void useTeleportPearl(Player player, ItemStack item) {
        consumeItem(player, item);
        Location target = player
            .getTargetBlock(null, 50)
            .getLocation()
            .add(0, 1, 0);
        player.teleport(target);
        player.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player
            .getWorld()
            .spawnParticle(Particle.PORTAL, target, 30, 0.5, 1, 0.5, 0.1);
        player.sendMessage(ChatColor.DARK_PURPLE + "Teleported!");
    }

    private void useThermalScanner(Player player, ItemStack item) {
        Location loc = player.getLocation();
        int found = 0;

        for (Entity e : player.getWorld().getNearbyEntities(loc, 30, 30, 30)) {
            if (e instanceof Player && !e.equals(player)) {
                Player target = (Player) e;
                double dist = loc.distance(target.getLocation());
                player.sendMessage(
                    ChatColor.RED +
                        "Detected: " +
                        target.getName() +
                        " - " +
                        (int) dist +
                        " blocks"
                );
                target
                    .getWorld()
                    .spawnParticle(
                        Particle.FLAME,
                        target.getLocation().add(0, 2, 0),
                        5,
                        0.2,
                        0.2,
                        0.2,
                        0
                    );
                found++;
            }
        }

        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
        if (found == 0) {
            player.sendMessage(
                ChatColor.YELLOW + "No players detected in range."
            );
        } else {
            player.sendMessage(
                ChatColor.GREEN + "Detected " + found + " player(s)!"
            );
        }
    }

    private void usePortableShield(Player player, ItemStack item) {
        consumeItem(player, item);
        forceFieldActive.add(player.getUniqueId());
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.SLOWNESS, 200, 2)
        );
        player.playSound(
            player.getLocation(),
            Sound.BLOCK_BEACON_ACTIVATE,
            1.0f,
            1.0f
        );
        player.sendMessage(
            ChatColor.BLUE + "Shield deployed! (10 seconds, slowed)"
        );

        new BukkitRunnable() {
            @Override
            public void run() {
                forceFieldActive.remove(player.getUniqueId());
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.YELLOW + "Shield expired!");
                    player.playSound(
                        player.getLocation(),
                        Sound.BLOCK_BEACON_DEACTIVATE,
                        1.0f,
                        1.0f
                    );
                }
            }
        }
            .runTaskLater(plugin, 200L);
    }

    private void useDecoyGrenade(Player player, ItemStack item) {
        consumeItem(player, item);
        Snowball proj = player.launchProjectile(Snowball.class);
        proj.setVelocity(player.getLocation().getDirection().multiply(1.0));
        thrownItems.put(proj, "decoy");
        thrownOwners.put(proj, player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Decoy thrown!");
    }

    private void useFootstepSilencer(Player player, ItemStack item) {
        consumeItem(player, item);
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.SPEED, 1200, 0, false, false)
        );
        player.playSound(
            player.getLocation(),
            Sound.ENTITY_ILLUSIONER_CAST_SPELL,
            0.5f,
            2.0f
        );
        player.sendMessage(
            ChatColor.DARK_PURPLE + "Footsteps silenced for 60 seconds!"
        );
    }

    private void useRadarJammer(Player player, ItemStack item) {
        consumeItem(player, item);
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.INVISIBILITY,
                2400,
                0,
                false,
                false
            )
        );
        player.playSound(
            player.getLocation(),
            Sound.BLOCK_BEACON_POWER_SELECT,
            1.0f,
            0.5f
        );
        player.sendMessage(
            ChatColor.RED +
                "Radar jammer active! You're invisible to trackers for 2 minutes."
        );
    }

    private void useTeamHealKit(Player player, ItemStack item) {
        consumeItem(player, item);
        Location loc = player.getLocation();
        int healed = 0;

        for (Entity e : player.getWorld().getNearbyEntities(loc, 10, 10, 10)) {
            if (e instanceof Player) {
                Player target = (Player) e;
                double maxHealth = target
                    .getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)
                    .getValue();
                target.setHealth(Math.min(target.getHealth() + 10, maxHealth));
                target.playSound(
                    target.getLocation(),
                    Sound.ENTITY_PLAYER_LEVELUP,
                    1.0f,
                    1.5f
                );
                target
                    .getWorld()
                    .spawnParticle(
                        Particle.HEART,
                        target.getLocation().add(0, 2, 0),
                        5,
                        0.3,
                        0.3,
                        0.3,
                        0
                    );
                healed++;
            }
        }

        player.sendMessage(
            ChatColor.RED +
                "Team Heal Kit used! Healed " +
                healed +
                " player(s)!"
        );
    }

    private void useSupplyDrop(Player player, ItemStack item) {
        consumeItem(player, item);
        Location loc = player.getLocation().add(0, 10, 0);
        World world = player.getWorld();

        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.5f);
        player.sendMessage(ChatColor.GOLD + "Supply drop incoming!");

        new BukkitRunnable() {
            Location dropLoc = loc.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (
                    ticks >= 40 || dropLoc.getBlock().getType() != Material.AIR
                ) {
                    // Drop supplies
                    world.dropItemNaturally(
                        dropLoc,
                        new ItemStack(Material.GOLDEN_APPLE, 2)
                    );
                    world.dropItemNaturally(
                        dropLoc,
                        new ItemStack(Material.ARROW, 32)
                    );
                    world.dropItemNaturally(
                        dropLoc,
                        new ItemStack(Material.DIAMOND, 3)
                    );
                    world.playSound(
                        dropLoc,
                        Sound.ENTITY_ITEM_PICKUP,
                        1.0f,
                        1.0f
                    );
                    world.spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        dropLoc,
                        20,
                        1,
                        1,
                        1,
                        0
                    );
                    cancel();
                    return;
                }

                world.spawnParticle(
                    Particle.CLOUD,
                    dropLoc,
                    3,
                    0.3,
                    0.3,
                    0.3,
                    0
                );
                dropLoc.subtract(0, 0.5, 0);
                ticks++;
            }
        }
            .runTaskTimer(plugin, 20L, 2L);
    }

    private void useC4(Player player, ItemStack item) {
        if (placedC4.containsKey(player.getUniqueId())) {
            // Detonate existing C4
            Location c4Loc = placedC4.remove(player.getUniqueId());
            c4Loc.getWorld().createExplosion(c4Loc, 5.0f, true, true);
            c4Loc
                .getWorld()
                .playSound(c4Loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
            player.sendMessage(
                ChatColor.RED + "" + ChatColor.BOLD + "C4 DETONATED!"
            );
        } else {
            // Place C4
            consumeItem(player, item);
            Block target = player.getTargetBlock(null, 5);
            Location loc = target.getLocation().add(0, 1, 0);
            loc.getBlock().setType(Material.RED_CONCRETE);
            placedC4.put(player.getUniqueId(), loc);
            player.playSound(loc, Sound.BLOCK_STONE_PLACE, 1.0f, 0.5f);
            player.sendMessage(
                ChatColor.RED + "C4 placed! Right-click again to detonate."
            );
        }
    }

    private void useForceField(Player player, ItemStack item) {
        consumeItem(player, item);
        forceFieldActive.add(player.getUniqueId());
        player.playSound(
            player.getLocation(),
            Sound.BLOCK_BEACON_ACTIVATE,
            1.0f,
            2.0f
        );
        player
            .getWorld()
            .spawnParticle(
                Particle.END_ROD,
                player.getLocation().add(0, 1, 0),
                50,
                1,
                1,
                1,
                0.1
            );
        player.sendMessage(
            ChatColor.AQUA +
                "" +
                ChatColor.BOLD +
                "FORCE FIELD ACTIVATED! (5 seconds)"
        );

        new BukkitRunnable() {
            @Override
            public void run() {
                forceFieldActive.remove(player.getUniqueId());
                if (player.isOnline()) {
                    player.sendMessage(
                        ChatColor.YELLOW + "Force field deactivated!"
                    );
                    player.playSound(
                        player.getLocation(),
                        Sound.BLOCK_BEACON_DEACTIVATE,
                        1.0f,
                        1.0f
                    );
                }
            }
        }
            .runTaskLater(plugin, 100L);
    }

    private void useArmorPlate(Player player, ItemStack item) {
        consumeItem(player, item);
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.RESISTANCE, 2400, 1)
        );
        player.playSound(
            player.getLocation(),
            Sound.ITEM_ARMOR_EQUIP_IRON,
            1.0f,
            1.0f
        );
        player.sendMessage(
            ChatColor.GRAY + "Armor plate applied! +Resistance for 2 minutes."
        );
    }

    // ========== PLACEMENT HANDLERS ==========

    private void placeMoneyPrinter(Player player, Location loc) {
        UUID uuid = player.getUniqueId();
        activeMoneyPrinters.put(loc, uuid);

        player.sendMessage(
            ChatColor.GREEN + "" + ChatColor.BOLD + "Money Printer placed!"
        );
        player.sendMessage(ChatColor.GRAY + "Generates $50 every 30 seconds");
        player.playSound(loc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.2f);

        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!activeMoneyPrinters.containsKey(loc)) {
                    cancel();
                    return;
                }

                Player owner = Bukkit.getPlayer(uuid);
                if (owner == null || !owner.isOnline()) return;

                if (
                    !owner.getWorld().equals(loc.getWorld()) ||
                    owner.getLocation().distance(loc) > 50
                ) {
                    owner.sendMessage(
                        ChatColor.YELLOW + "Too far from Money Printer!"
                    );
                    return;
                }

                economyManager.addBalance(uuid, 50);
                owner.sendMessage(ChatColor.GREEN + "+$50 from Money Printer");
                owner.playSound(
                    owner.getLocation(),
                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                    0.5f,
                    1.5f
                );
                loc
                    .getWorld()
                    .spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        loc.clone().add(0.5, 1.2, 0.5),
                        10,
                        0.3,
                        0.3,
                        0.3,
                        0
                    );
            }
        }
            .runTaskTimer(plugin, 600L, 600L)
            .getTaskId();

        moneyPrinterTasks.put(loc, taskId);
    }

    private void placeLandmine(Player player, Location loc) {
        activeLandmines.put(loc, player.getUniqueId());
        player.sendMessage(ChatColor.RED + "Landmine placed!");
        player.playSound(loc, Sound.BLOCK_TRIPWIRE_ATTACH, 1.0f, 1.0f);
    }

    private void placeBearTrap(Player player, Location loc) {
        activeBearTraps.put(loc, player.getUniqueId());
        player.sendMessage(ChatColor.GRAY + "Bear trap placed!");
        player.playSound(loc, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 1.0f, 0.5f);
    }

    private void placeHealingStation(Player player, Block block) {
        Location loc = block.getLocation();
        player.sendMessage(
            ChatColor.LIGHT_PURPLE + "Healing Station deployed!"
        );
        player.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 600 || block.getType() != Material.BEACON) {
                    block.setType(Material.AIR);
                    cancel();
                    return;
                }

                for (Entity e : block
                    .getWorld()
                    .getNearbyEntities(loc, 8, 8, 8)) {
                    if (e instanceof Player) {
                        Player p = (Player) e;
                        double maxHealth = p
                            .getAttribute(
                                org.bukkit.attribute.Attribute.MAX_HEALTH
                            )
                            .getValue();
                        if (p.getHealth() < maxHealth) {
                            p.setHealth(Math.min(p.getHealth() + 1, maxHealth));
                            p
                                .getWorld()
                                .spawnParticle(
                                    Particle.HEART,
                                    p.getLocation().add(0, 2, 0),
                                    2,
                                    0.2,
                                    0.2,
                                    0.2,
                                    0
                                );
                        }
                    }
                }

                loc
                    .getWorld()
                    .spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        loc.clone().add(0.5, 1, 0.5),
                        5,
                        0.5,
                        0.5,
                        0.5,
                        0
                    );
                ticks += 20;
            }
        }
            .runTaskTimer(plugin, 0L, 20L);
    }

    private void placeAutoTurret(Player player, Block block) {
        Location loc = block.getLocation();
        UUID owner = player.getUniqueId();
        player.sendMessage(ChatColor.RED + "Auto Turret deployed!");
        player.playSound(loc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.5f);

        new BukkitRunnable() {
            int ammo = 64;

            @Override
            public void run() {
                if (ammo <= 0 || block.getType() != Material.DISPENSER) {
                    block.setType(Material.AIR);
                    cancel();
                    return;
                }

                Player nearestEnemy = null;
                double nearestDist = 15;

                for (Entity e : block
                    .getWorld()
                    .getNearbyEntities(loc, 15, 15, 15)) {
                    if (e instanceof Player && !e.getUniqueId().equals(owner)) {
                        double dist = e.getLocation().distance(loc);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearestEnemy = (Player) e;
                        }
                    }
                }

                if (nearestEnemy != null) {
                    Vector dir = nearestEnemy
                        .getLocation()
                        .add(0, 1, 0)
                        .toVector()
                        .subtract(loc.toVector().add(new Vector(0.5, 0.5, 0.5)))
                        .normalize();
                    Arrow arrow = block
                        .getWorld()
                        .spawn(loc.clone().add(0.5, 0.5, 0.5), Arrow.class);
                    arrow.setVelocity(dir.multiply(2.0));
                    arrow.setDamage(4.0);
                    block
                        .getWorld()
                        .playSound(loc, Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.2f);
                    ammo--;
                }
            }
        }
            .runTaskTimer(plugin, 20L, 10L);
    }

    private void useCryptoWallet(Player player, ItemStack item) {
        if (cryptoManager == null) {
            player.sendMessage(ChatColor.RED + "Crypto system not available!");
            return;
        }
        
        if (!cryptoManager.isCryptoWallet(item)) {
            player.sendMessage(ChatColor.RED + "Invalid crypto wallet!");
            return;
        }
        
        String walletId = cryptoManager.getWalletId(item);
        if (walletId == null) {
            player.sendMessage(ChatColor.RED + "Invalid wallet ID!");
            return;
        }
        
        cryptoManager.openCryptoWallet(player, item);
        player.sendMessage(ChatColor.YELLOW + "Opening crypto wallet...");
    }

    private void useGpsTrailKey(Player player, ItemStack item) {
        Location loc = player.getLocation();

        // Start animated GPS display with sounds
        new BukkitRunnable() {
            int step = 0;
            final int x = (int) loc.getX();
            final int y = (int) loc.getY();
            final int z = (int) loc.getZ();
            final String world = loc.getWorld().getName();

            @Override
            public void run() {
                switch (step) {
                    case 0:
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "GPS");
                        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                        break;
                    case 1:
                        player.sendMessage(ChatColor.GRAY + "Our Coordinates:");
                        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.8f);
                        break;
                    case 2:
                        player.sendMessage(ChatColor.YELLOW + "X: " + x);
                        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.6f);
                        break;
                    case 3:
                        player.sendMessage(ChatColor.YELLOW + "Y: " + y);
                        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.4f);
                        break;
                    case 4:
                        player.sendMessage(ChatColor.YELLOW + "Z: " + z);
                        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                        break;
                    case 5:
                        player.sendMessage(ChatColor.GRAY + "World: " + world);
                        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

                        // Create final particle effect
                        player.getWorld().spawnParticle(
                            Particle.ENCHANT,
                            loc.add(0, 1, 0),
                            30,
                            0.7,
                            1.2,
                            0.7,
                            0.1
                        );
                        cancel();
                        return;
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 8L); // 8 ticks = 0.4 seconds between each line
    }

    private void useMedkit(Player player, ItemStack item) {
        Location loc = player.getLocation();
        World world = loc.getWorld();

        // Check uses remaining
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey usesKey = new NamespacedKey(plugin, "medkit_uses");
        int usesLeft = container.getOrDefault(usesKey, PersistentDataType.INTEGER, 3);

        // Heal the player
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        double currentHealth = player.getHealth();

        if (currentHealth >= maxHealth) {
            player.sendMessage(ChatColor.YELLOW + "You're already at full health!");
            return;
        }

        // Heal 8 hearts (16 health points)
        double healAmount = Math.min(16.0, maxHealth - currentHealth);
        double newHealth = currentHealth + healAmount;
        player.setHealth(newHealth);

        // Apply regeneration effect
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.REGENERATION, 100, 1)
        );

        // Remove poison if present
        player.removePotionEffect(PotionEffectType.POISON);

        // Play healing sound
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        // Create healing particles
        world.spawnParticle(Particle.HEART, loc.add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);

        // Calculate hearts healed (1 heart = 2 health points)
        int heartsHealed = (int) Math.ceil(healAmount / 2.0);
        player.sendMessage(ChatColor.GREEN + "Medkit used! +" + heartsHealed + " heart" + (heartsHealed != 1 ? "s" : "") + " healed!");

        // Decrease uses and update item
        usesLeft--;
        if (usesLeft > 0) {
            container.set(usesKey, PersistentDataType.INTEGER, usesLeft);

            // Update lore to show remaining uses
            java.util.List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 3) {
                lore.set(2, ChatColor.GRAY + "Uses remaining: " + usesLeft + "/3");
                meta.setLore(lore);
            }

            item.setItemMeta(meta);
            player.sendMessage(ChatColor.YELLOW + "Medkit has " + usesLeft + " use" + (usesLeft != 1 ? "s" : "") + " remaining!");
        } else {
            // Remove item when no uses left
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            player.sendMessage(ChatColor.RED + "Medkit depleted!");
        }
    }
}
