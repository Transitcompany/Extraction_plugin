package com.extraction.governance;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Proposal {

    private final String description;
    private final UUID proposer;
    private final long createdAt;
    private final Set<UUID> yesVotes = new HashSet<>();
    private final Set<UUID> noVotes = new HashSet<>();

    public Proposal(String description, UUID proposer) {
        this.description = description;
        this.proposer = proposer;
        this.createdAt = System.currentTimeMillis();
    }

    public String getDescription() {
        return description;
    }

    public UUID getProposer() {
        return proposer;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getYesVotes() {
        return yesVotes.size();
    }

    public int getNoVotes() {
        return noVotes.size();
    }

    public int getTotalVotes() {
        return yesVotes.size() + noVotes.size();
    }

    public boolean addVote(UUID player, boolean yes) {
        if (yesVotes.contains(player) || noVotes.contains(player)) {
            return false; // already voted
        }
        if (yes) {
            yesVotes.add(player);
        } else {
            noVotes.add(player);
        }
        return true;
    }

    public boolean hasVoted(UUID player) {
        return yesVotes.contains(player) || noVotes.contains(player);
    }

    public boolean isPassed() {
        // Simple majority: more yes than no, and at least 5 votes
        return getYesVotes() > getNoVotes() && getTotalVotes() >= 5;
    }

    public boolean isExpired() {
        // Expire after 24 hours
        return System.currentTimeMillis() - createdAt > 24 * 60 * 60 * 1000;
    }
}