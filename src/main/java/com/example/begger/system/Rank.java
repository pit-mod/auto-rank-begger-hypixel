package com.example.begger.system;

public enum Rank {
    NON("None", ""),
    VIP("VIP", "[VIP]"),
    VIP_PLUS("VIP+", "[VIP+]"),
    MVP("MVP", "[MVP]"),
    MVP_PLUS("MVP+", "[MVP+]"),
    MVP_PLUS_PLUS("MVP++", "[MVP++]");

    private final String name;
    private final String prefix;

    Rank(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }
}
