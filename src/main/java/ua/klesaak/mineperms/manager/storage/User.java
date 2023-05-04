package ua.klesaak.mineperms.manager.storage;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class User {
    private final UUID userUUID;
    private volatile String playerName, group, prefix, suffix;
    private Map<String, Object> options = new HashMap<>();
    private Set<String> permissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    ///Transient Data///
    private transient volatile Set<String> calculatedPermissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public User(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public boolean hasPermission(String permission) {
        if (this.calculatedPermissions.contains(MinePermsManager.ROOT_WILDCARD)) return true;
        if (!permission.contains(MinePermsManager.DOT_WILDCARD)) return this.calculatedPermissions.contains(permission);
        String[] parts = permission.toLowerCase().split("\\.");
        StringBuilder partsBuilder = new StringBuilder();
        for (String part : parts) {
            partsBuilder.append(part).append(MinePermsManager.DOT_WILDCARD);
            if (this.calculatedPermissions.contains(partsBuilder + MinePermsManager.ROOT_WILDCARD)) return true;
        }
        return false;
    }

    public void recalculatePermissions() { //todo закидывать в calculatedPermissions только через .toLowerCase

    }

    public void addPermission(String permission) {
        val perm = permission.toLowerCase();
        this.permissions.add(perm);
        this.calculatedPermissions.add(perm);
    }

    public void removePermission(String permission) {
        val perm = permission.toLowerCase();
        this.permissions.remove(perm);
        this.calculatedPermissions.remove(perm);
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
                ", permissions=" + calculatedPermissions +
                '}';
    }
}
