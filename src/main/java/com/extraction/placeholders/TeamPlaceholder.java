package com.extraction.placeholders;

import com.extraction.ExtractionPlugin;
import com.extraction.team.Team;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class TeamPlaceholder extends PlaceholderExpansion {

    private final ExtractionPlugin plugin;

    public TeamPlaceholder(ExtractionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "Extract_team";
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
        if (identifier == null) {
            Team team = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
            return team != null ? team.getName() : "None";
        }
        return null;
    }
}