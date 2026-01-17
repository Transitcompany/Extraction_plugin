package com.extraction.placeholders;

import com.extraction.ExtractionPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class BalancePlaceholder extends PlaceholderExpansion {

    private final ExtractionPlugin plugin;

    public BalancePlaceholder(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "Vbalance";
    }

    @Override
    public String getAuthor() {
        return "Extraction";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";
        if (identifier.equals("balance")) {
            String balance = plugin.getEconomyManager().formatBalance(player.getUniqueId());
            return balance;
        }
        return null;
    }
}