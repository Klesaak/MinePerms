package ua.klesaak.mineperms.api;

import lombok.experimental.UtilityClass;
import ua.klesaak.mineperms.manager.storage.Storage;

import java.util.Collections;
import java.util.Set;

@UtilityClass
public class MinePermsAPI {
    private Storage STORAGE;

    public void register(Storage storage) {
        STORAGE = storage;
    }

    public boolean hasPermission(String playerName, String permission) {
        return STORAGE.hasPermission(playerName, permission);
    }

    public String getUserGroup(String playerName) {
        return STORAGE.getUserGroup(playerName);
    }

    public String getUserPrefix(String playerName) {
        return STORAGE.getUserPrefix(playerName);
    }

    public String getUserSuffix(String playerName) {
        return STORAGE.getUserSuffix(playerName);
    }

    public Set<String> getGroupInheritance(String groupID) {
        return Collections.unmodifiableSet(STORAGE.getGroup(groupID).getInheritanceGroups());
    }

    public String getGroupPrefix(String groupID) {
        return STORAGE.getGroup(groupID).getPrefix();
    }

    public String getGroupSuffix(String groupID) {
        return STORAGE.getGroup(groupID).getSuffix();
    }
}
