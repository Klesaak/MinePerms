package ua.klesaak.mineperms.api;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class MinePermsAPI {

    public boolean hasPermission(String playerName, String permission) {
        return false;
    }

    public boolean hasPermission(UUID uuid, String permission) {
        return false;
    }

    public String getUserGroup(String playerName) {
        return "";
    }

    public String getUserGroup(UUID uuid) {
        return "";
    }

    public String getUserPrefix(String playerName) {
        return "";
    }

    public String getUserPrefix(UUID uuid) {
        return "";
    }

    public String getUserSuffix(String playerName) {
        return "";
    }

    public String getUserSuffix(UUID uuid) {
        return "";
    }
}
