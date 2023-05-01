package ua.klesaak.mineperms.manager.storage;

import lombok.Getter;
import lombok.Setter;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class User {
    private final UUID userUUID;
    private volatile String playerName, prefix, suffix, group;
    private volatile Set<String> permissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public User(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public boolean hasPermission(String permission) {
        if (this.permissions.contains(MinePermsManager.ROOT_WILDCARD)) return true;
        String[] parts = permission.toLowerCase().split("\\.");
        StringBuilder partsBuilder = new StringBuilder();
        for (String part : parts) {
            partsBuilder.append(part).append(".");
            if (this.permissions.contains(partsBuilder + MinePermsManager.ROOT_WILDCARD)) return true;
        }
        return false;
    }

    public void recalculatePermissions() { //todo закидывать в permissions только через .toLowerCase

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userUUID.equals(user.userUUID) && playerName.equals(user.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userUUID, playerName);
    }

    @Override
    public String toString() {
        return "User{" +
                "userUUID=" + userUUID +
                ", playerName='" + playerName + '\'' +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", group='" + group + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
