package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.extract.ExtractManager;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

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
                    player.sendMessage(
                        ChatColor.GREEN + "You have been returned to the lobby."
                    );
                }
            },
            5L
        );
    }
}
