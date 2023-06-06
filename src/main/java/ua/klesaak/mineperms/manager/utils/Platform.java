package ua.klesaak.mineperms.manager.utils;

import lombok.NonNull;

public enum Platform {
    BUKKIT("Bukkit"),
    BUNGEECORD("BungeeCord"),
    VELOCITY("Velocity");

    private final String friendlyName;

    Platform(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /**
     * Gets a readable name for the platform type.
     *
     * @return a readable name
     */
    public @NonNull String getFriendlyName() {
        return this.friendlyName;
    }
}