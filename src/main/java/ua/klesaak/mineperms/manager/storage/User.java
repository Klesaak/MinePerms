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
    protected volatile String prefix, suffix, group;
    protected final Set<String> permissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public User(UUID userUUID) {
        this.userUUID = userUUID;
    }

    private boolean hasPermission(String permission) {
        if (this.permissions.contains(MinePermsManager.ROOT_WILDCARD)) return true;
        String[] parts = permission.split("\\.");

        StringBuilder partsBuilder = new StringBuilder();

        for (int in = 0; in < parts.length; in++) {
            partsBuilder.append(parts[0]).append(".");

            if (this.permissions.contains(partsBuilder + MinePermsManager.ROOT_WILDCARD)) {
                return true;
            }
        }

        return false;
    }
}
