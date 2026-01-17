package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.data.PlayerDataManager;
import com.extraction.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProximityChatListener implements Listener {
    private final ExtractionPlugin plugin;
    private final Map<UUID, Long> lastProximityMessage = new HashMap<>();
    private static final long MESSAGE_COOLDOWN = 60 * 60 * 1000; // 1 hour in milliseconds

    public ProximityChatListener(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        // Cancel default chat
        event.setCancelled(true);

        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(sender);
        String prefix = data.getRank().getPrefix();

        if (data.isTeamChatEnabled() && data.getTeamName() != null) {
            // Team chat
            var team = plugin.getTeamManager().getPlayerTeam(sender.getUniqueId());
            if (team != null) {
                for (UUID memberId : team.getMembers()) {
                    Player member = plugin.getServer().getPlayer(memberId);
                    if (member != null) {
                        member.sendMessage(ChatColor.BLUE + "[Team] [" + prefix + " " + sender.getName() + "] " + message);
                    }
                }
            }
        } else {
            // Proximity chat
            boolean sentToSomeone = false;
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getWorld().equals(sender.getWorld()) && player.getLocation().distance(sender.getLocation()) <= 50) {
                    player.sendMessage(ChatColor.GRAY + "[" + prefix + " " + sender.getName() + "] " + message);
                    sentToSomeone = true;
                }
            }

            if (!sentToSomeone) {
                long now = System.currentTimeMillis();
                Long lastTime = lastProximityMessage.get(sender.getUniqueId());
                if (lastTime == null || (now - lastTime) > MESSAGE_COOLDOWN) {
                    sender.sendMessage(ChatColor.RED + "No one can hear you. You are too far away from other players.");
                    lastProximityMessage.put(sender.getUniqueId(), now);
                }
            }
        }
    }
}