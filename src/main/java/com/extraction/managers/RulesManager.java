package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RulesManager {

    private final ExtractionPlugin plugin;
    private final File rulesFile;
    private YamlConfiguration rulesConfig;
    private List<String> rules;

    public RulesManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.rulesFile = new File(plugin.getDataFolder(), "rules.yml");
        if (!rulesFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                rulesFile.createNewFile();
            } catch (IOException ignored) {}
        }
        this.rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);
        loadRules();
    }

    private void loadRules() {
        rules = rulesConfig.getStringList("rules");
        if (rules == null) {
            rules = new ArrayList<>();
        }
    }

    public void saveRules() {
        rulesConfig.set("rules", rules);
        try {
            rulesConfig.save(rulesFile);
        } catch (IOException ignored) {}
    }

    public List<String> getRules() {
        return new ArrayList<>(rules);
    }

    public void addRule(String rule) {
        rules.add(rule);
        saveRules();
    }

    public boolean removeRule(int index) {
        if (index >= 0 && index < rules.size()) {
            rules.remove(index);
            saveRules();
            return true;
        }
        return false;
    }
}