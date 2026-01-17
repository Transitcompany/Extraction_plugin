package com.extraction.listeners;

import com.extraction.managers.ChatModerationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatModerationListener implements Listener {

    private final ChatModerationManager moderationManager;

    public ChatModerationListener(ChatModerationManager moderationManager) {
        this.moderationManager = moderationManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String censored = moderationManager.censor(message);
        event.setMessage(censored);
    }
}