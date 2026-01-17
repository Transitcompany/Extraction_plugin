package com.extraction.team;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import java.util.List;
import java.util.UUID;

public class Team {
    private UUID id;
    private String name;
    private UUID leader;
    private List<UUID> members;
    private Inventory sharedStash;

    public Team(UUID id, String name, UUID leader) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.members = new java.util.ArrayList<>();
        this.members.add(leader);
        this.sharedStash = Bukkit.createInventory(null, 54, "Team Stash - " + name);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getLeader() { return leader; }
    public List<UUID> getMembers() { return members; }
    public Inventory getSharedStash() { return sharedStash; }

    public void addMember(UUID player) {
        if (!members.contains(player)) {
            members.add(player);
        }
    }

    public void removeMember(UUID player) {
        members.remove(player);
    }

    public boolean isLeader(UUID player) {
        return leader.equals(player);
    }
}