package com.extraction.listeners;

import com.extraction.managers.ChatModerationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.HashMap;
import java.util.Map;

public class ChatModerationListener implements Listener {

    private final ChatModerationManager moderationManager;
    private final Map<String, String> emojiMap;

    public ChatModerationListener(ChatModerationManager moderationManager) {
        this.moderationManager = moderationManager;
        this.emojiMap = new HashMap<>();
        loadEmojis();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String censored = moderationManager.censor(message);
        String withEmojis = replaceEmojis(censored);
        event.setMessage(withEmojis);
    }

    private void loadEmojis() {
        emojiMap.put(":skull:", "💀");
        emojiMap.put(":thumbs_up:", "👍");
        emojiMap.put(":thumbs_down:", "👎");
        emojiMap.put(":heart:", "❤️");
        emojiMap.put(":fire:", "🔥");
        emojiMap.put(":100:", "💯");
        emojiMap.put(":smile:", "😊");
        emojiMap.put(":laugh:", "😂");
        emojiMap.put(":cry:", "😢");
        emojiMap.put(":angry:", "😠");
        emojiMap.put(":wow:", "😮");
        emojiMap.put(":wink:", "😉");
        emojiMap.put(":cool:", "😎");
        emojiMap.put(":love:", "😍");
        emojiMap.put(":kiss:", "😘");
        emojiMap.put(":tongue:", "😛");
        emojiMap.put(":neutral:", "😐");
        emojiMap.put(":thinking:", "🤔");
        emojiMap.put(":shrug:", "🤷");
        emojiMap.put(":pray:", "🙏");
        emojiMap.put(":clap:", "👏");
        // Add more as needed
    }

    private String replaceEmojis(String message) {
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }
}