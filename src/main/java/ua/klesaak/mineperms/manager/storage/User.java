package ua.klesaak.mineperms.manager.storage;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class User {
    private final String playerName;
    private volatile String group;
    private volatile String prefix = "";
    private volatile String suffix = "";
    private Set<String> permissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    ///Transient Data///
    private transient volatile Set<String> calculatedPermissions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private transient String jsonPerms; //костыль для ORMLite

    public User(String playerName, String groupID) {
        this.playerName = playerName;
        this.group = groupID;
    }

    public boolean hasPermission(String permission) {
        val permissionLowerCase = permission.toLowerCase();
        if (this.calculatedPermissions.contains(MinePermsManager.ROOT_WILDCARD)) return true;
        if (!permissionLowerCase.contains(MinePermsManager.DOT_WILDCARD)) return this.calculatedPermissions.contains(permissionLowerCase);
        if (this.calculatedPermissions.contains(permissionLowerCase)) return true;
        String[] parts = permissionLowerCase.toLowerCase().split("\\.");
        StringBuilder partsBuilder = new StringBuilder();
        for (String part : parts) {
            partsBuilder.append(part).append(MinePermsManager.DOT_WILDCARD);
            if (this.calculatedPermissions.contains(partsBuilder + MinePermsManager.ROOT_WILDCARD)) return true;
        }
        return false;
    }

    public boolean hasOwnPermission(String permission) {
        val permissionLowerCase = permission.toLowerCase();
        return this.permissions.contains(permissionLowerCase);
    }

    public boolean hasGroup(String groupID) {
        return this.group.equalsIgnoreCase(groupID);
    }

    public void recalculatePermissions(Map<String, Group> groupsMap) {
        this.calculatedPermissions = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.calculatedPermissions.addAll(this.permissions);
        val group = groupsMap.get(this.group);
        if (group != null) {
            this.calculatedPermissions.addAll(group.getPermissions());
            group.getInheritanceGroups().forEach(groupID -> {
                val inheritanceGroup = groupsMap.get(groupID);
                if (inheritanceGroup != null) this.calculatedPermissions.addAll(inheritanceGroup.getPermissions());
            });
        }
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
        return playerName.equals(user.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName);
    }

    @Override
    public String toString() {
        return "User{" +
                "playerName='" + playerName + '\'' +
                ", group='" + group + '\'' +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", permissions=" + permissions +
                ", calculatedPermissions=" + calculatedPermissions +
                ", jsonPerms='" + jsonPerms + '\'' +
                '}';
    }
}
