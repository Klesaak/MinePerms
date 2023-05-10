package ua.klesaak.mineperms.api;

import lombok.experimental.UtilityClass;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class MinePermsAPI {
    private MinePermsManager MANAGER;

    public void register(MinePermsManager manager) {
        MANAGER = manager;
    }

    public boolean hasPermission(String playerName, String permission) {
        return false;
    }

    public String getUserGroup(String playerName) {
        return "";
    }

    public String getUserPrefix(String playerName) {
        return "";
    }

    public String getUserSuffix(String playerName) {
        return "";
    }

    public List<String> getGroupInheritance(String groupID) {
        return new ArrayList<>(); //todo return groups
    }

    public String getGroupPrefix(String groupID) {
        return "";
    }

    public String getGroupSuffix(String groupID) {
        return "";
    }
}
