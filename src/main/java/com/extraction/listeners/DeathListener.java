package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.extract.ExtractManager;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import java.time.LocalDate;
import java.util.Arrays;

public class DeathListener implements Listener {

    private final ExtractionPlugin plugin;
    private final ExtractManager extractManager;
    private final Set<UUID> pendingLobbyTeleport = new HashSet<>();

    public DeathListener(
        ExtractionPlugin plugin,
        ExtractManager extractManager
    ) {
        this.plugin = plugin;
        this.extractManager = extractManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String lobbyWorld = extractManager.getLobbyWorld();

        // Set custom death message
        String customMessage = getCustomDeathMessage(player);
        event.setDeathMessage(ChatColor.RED + customMessage);

        // Only queue teleport if they died outside the lobby
        if (
            lobbyWorld != null && player.getWorld().getName().equals(lobbyWorld)
        ) {
            return; // Died in lobby, no teleport needed
        }

        // Mark player for teleport on respawn
        pendingLobbyTeleport.add(player.getUniqueId());

        player.sendMessage(
            ChatColor.RED +
                "You died! You will be sent to the lobby on respawn."
        );
    }

    private String getCustomDeathMessage(Player player) {
        EntityDamageEvent lastDamage = player.getLastDamageCause();
        if (lastDamage == null) {
            return player.getName() + " met an unfortunate end.";
        }

        EntityDamageEvent.DamageCause cause = lastDamage.getCause();
        switch (cause) {
            case FALL:
                return player.getName() + " fell to their doom.";
            case FALLING_BLOCK:
                return player.getName() + " was crushed by falling debris.";
            case LAVA:
                return player.getName() + " was melted in lava.";
            case FIRE:
            case FIRE_TICK:
                return player.getName() + " burned to ashes.";
            case DROWNING:
                return player.getName() + " drowned.";
            case SUFFOCATION:
                return player.getName() + " suffocated.";
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                return player.getName() + " was slain by an enemy.";
            case PROJECTILE:
                return player.getName() + " was shot down.";
            case VOID:
                return player.getName() + " fell into the void.";
            case LIGHTNING:
                return player.getName() + " was struck by lightning.";
            case POISON:
                return player.getName() + " succumbed to poison.";
            case MAGIC:
                return player.getName() + " was defeated by magic.";
            case WITHER:
                return player.getName() + " withered away.";
            case STARVATION:
                return player.getName() + " starved.";
            case THORNS:
                return player.getName() + " was pricked by thorns.";
            case HOT_FLOOR:
                return player.getName() + " melted on magma.";
            case CRAMMING:
                return player.getName() + " was crushed.";
            case DRYOUT:
                return player.getName() + " dried out.";
            case FREEZE:
                return player.getName() + " froze to death.";
            case SONIC_BOOM:
                return player.getName() + " was blasted by sound.";
            default:
                return player.getName() + " died.";
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Give starting kit on respawn after death
        giveStartingKit(player);

        if (!pendingLobbyTeleport.contains(uuid)) {
            return;
        }

        pendingLobbyTeleport.remove(uuid);

        String lobbyWorld = extractManager.getLobbyWorld();
        if (lobbyWorld == null) {
            return;
        }

        World world = Bukkit.getWorld(lobbyWorld);
        if (world == null) {
            return;
        }

        Location spawn = world.getSpawnLocation();

        // Set respawn location to lobby
        event.setRespawnLocation(spawn);

        // Also schedule a delayed teleport as backup
        Bukkit.getScheduler().runTaskLater(
            plugin,
            () -> {
                if (player.isOnline()) {
                    player.teleport(spawn);
                    player.setFoodLevel(20); // Full hunger in lobby
                    player.sendMessage(
                        ChatColor.GREEN + "You have been returned to the lobby."
                    );
                }
            },
            5L
        );
    }

    private void giveStartingKit(Player player) {
        String date = LocalDate.now().toString();
        String owner = player.getName();

        // Leather boots with Netherite Tide trim
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        ArmorMeta bootsMeta = (ArmorMeta) boots.getItemMeta();
        bootsMeta.setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.TIDE));
        bootsMeta.setDisplayName(ChatColor.GOLD + "Starter Boots");
        bootsMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Starter Gear",
            ChatColor.GRAY + "Created on: " + date,
            ChatColor.GRAY + "Owned by: " + owner
        ));
        boots.setItemMeta(bootsMeta);

        // Leather leggings with Netherite Host trim
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ArmorMeta leggingsMeta = (ArmorMeta) leggings.getItemMeta();
        leggingsMeta.setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.HOST));
        leggingsMeta.setDisplayName(ChatColor.GOLD + "Starter Leggings");
        leggingsMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Starter Gear",
            ChatColor.GRAY + "Created on: " + date,
            ChatColor.GRAY + "Owned by: " + owner
        ));
        leggings.setItemMeta(leggingsMeta);

        // Stone tools
        ItemStack pickaxe = new ItemStack(Material.STONE_PICKAXE);
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        ItemStack shovel = new ItemStack(Material.STONE_SHOVEL);

        // 15 cooked beef
        ItemStack beef = new ItemStack(Material.COOKED_BEEF, 15);

        // Add to inventory
        player.getInventory().setBoots(boots);
        player.getInventory().setLeggings(leggings);
        player.getInventory().addItem(pickaxe, sword, shovel, beef);
    }
}
