package com.extraction.managers;

import com.extraction.ExtractionPlugin;
import com.extraction.governance.Proposal;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GovernanceManager {

    private final ExtractionPlugin plugin;
    private final RulesManager rulesManager;
    private Proposal activeProposal;

    public GovernanceManager(ExtractionPlugin plugin, RulesManager rulesManager) {
        this.plugin = plugin;
        this.rulesManager = rulesManager;
        startProposalChecker();
    }

    public boolean proposeRule(Player proposer, String description) {
        if (activeProposal != null) {
            proposer.sendMessage(ChatColor.RED + "There is already an active proposal. Wait for it to finish.");
            return false;
        }
        activeProposal = new Proposal(description, proposer.getUniqueId());
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Governance] " + proposer.getName() + " proposed: " + description);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Vote with /vote yes or /vote no. Proposal expires in 24 hours.");
        return true;
    }

    public boolean vote(Player voter, boolean yes) {
        if (activeProposal == null) {
            voter.sendMessage(ChatColor.RED + "No active proposal to vote on.");
            return false;
        }
        if (activeProposal.hasVoted(voter.getUniqueId())) {
            voter.sendMessage(ChatColor.RED + "You have already voted.");
            return false;
        }
        activeProposal.addVote(voter.getUniqueId(), yes);
        voter.sendMessage(ChatColor.GREEN + "Vote recorded: " + (yes ? "Yes" : "No"));
        checkProposal();
        return true;
    }

    private void checkProposal() {
        if (activeProposal.isPassed()) {
            rulesManager.addRule(activeProposal.getDescription());
            Bukkit.broadcastMessage(ChatColor.GREEN + "[Governance] Proposal passed! New rule added: " + activeProposal.getDescription());
            activeProposal = null;
        }
    }

    private void startProposalChecker() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (activeProposal != null && activeProposal.isExpired()) {
                Bukkit.broadcastMessage(ChatColor.RED + "[Governance] Proposal expired without passing: " + activeProposal.getDescription());
                activeProposal = null;
            }
        }, 0L, 1200L); // Check every minute (1200 ticks)
    }

    public void displayRules(Player player) {
        List<String> rules = rulesManager.getRules();
        if (rules.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No server rules set.");
            return;
        }
        player.sendMessage(ChatColor.GOLD + "Server Rules:");
        for (int i = 0; i < rules.size(); i++) {
            player.sendMessage(ChatColor.WHITE.toString() + (i + 1) + ". " + rules.get(i));
        }
    }

    public Proposal getActiveProposal() {
        return activeProposal;
    }
}