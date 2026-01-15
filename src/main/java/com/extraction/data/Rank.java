package com.extraction.data;

public enum Rank {
    P("§b[P]", "Player"),
    D("§b[D]", "Donator"),
    VIP("§b[VIP]", "VIP"),
    H("§b[H]", "Helper"),
    O("§b[O]", "Owner");

    private final String prefix;
    private final String displayName;

    Rank(String prefix, String displayName) {
        this.prefix = prefix;
        this.displayName = displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Rank fromString(String rank) {
        for (Rank r : values()) {
            if (r.name().equalsIgnoreCase(rank)) {
                return r;
            }
        }
        return P; // default
    }
}