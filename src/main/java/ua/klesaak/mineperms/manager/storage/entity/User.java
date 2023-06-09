package ua.klesaak.mineperms.manager.storage.entity;

import lombok.Getter;
import lombok.val;
import ua.klesaak.mineperms.manager.storage.Storage;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class User extends AbstractEntity {
    private String group;
    private transient volatile Set<String> calculatedPermissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public User(String playerName, String groupId) {
        super(playerName);
        this.group = groupId;
    }

    @Override
    public boolean hasPermission(String permission) {
        return Storage.hasPermission(this.calculatedPermissions, permission);
    }

    @Override
    public void addPermission(String permission) {
        val perm = permission.toLowerCase();
        this.permissions.add(perm);
        this.calculatedPermissions.add(perm);
    }

    @Override
    public void removePermission(String permission) {
        val perm = permission.toLowerCase();
        this.permissions.remove(perm);
        this.calculatedPermissions.remove(perm);
    }

    public boolean hasGroup(String groupID) {
        return this.group.equalsIgnoreCase(groupID);
    }

    public void setGroup(String groupId) {
        this.group = groupId;
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

    public String getPlayerName() {
        return this.entityId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return this.entityId.equals(user.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entityId);
    }

    @Override
    public String toString() {
        return "User{" +
                "playerName='" + entityId + '\'' +
                ", group='" + group + '\'' +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", permissions=" + permissions +
                ", calculatedPermissions=" + calculatedPermissions +
                '}';
    }
}
