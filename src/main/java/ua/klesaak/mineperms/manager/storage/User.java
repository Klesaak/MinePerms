package ua.klesaak.mineperms.manager.storage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter @EqualsAndHashCode @ToString
public class User {
    private final UUID userUUID;
    private volatile String prefix, suffix, group;
    private final Set<String> permissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public User(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public boolean hasPermission(String permission) {
        if (this.permissions.contains(MinePermsManager.ROOT_WILDCARD)) return true;
        String[] parts = permission.toLowerCase().split("\\.");

        StringBuilder partsBuilder = new StringBuilder();

        for (String part : parts) {
            partsBuilder.append(part).append(".");
            if (permissions.contains(partsBuilder + MinePermsManager.ROOT_WILDCARD)) {
                return true;
            }
        }

        return false;
    }

    public void recalculatePermissions() {

    }
}
