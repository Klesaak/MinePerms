package ua.klesaak.mineperms.manager.storage;

import lombok.Getter;
import lombok.Setter;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class Group {
    private final String groupID;
    private String prefix = "";
    private String suffix = "";
    private final Set<String> inheritanceGroups = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> permissions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Map<String, Object> options = new HashMap<>();

    public Group(String groupID) {
        this.groupID = groupID;
    }

    public boolean hasPermission(String permission) {
        if (this.permissions.contains(MinePermsManager.ROOT_WILDCARD)) return true;
        if (!permission.contains(MinePermsManager.DOT_WILDCARD)) return this.permissions.contains(permission);
        String[] parts = permission.toLowerCase().split("\\.");
        StringBuilder partsBuilder = new StringBuilder();
        for (String part : parts) {
            partsBuilder.append(part).append(MinePermsManager.DOT_WILDCARD);
            if (this.permissions.contains(partsBuilder + MinePermsManager.ROOT_WILDCARD)) return true;
        }
        return false;
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public void addPermission(String permission) {
        permissions.add(permission.toLowerCase());
    }

    public void removePermission(String permission) {
        permissions.remove(permission.toLowerCase());
    }

    public void addInheritanceGroup(String group) {
        inheritanceGroups.add(group.toLowerCase());
    }

    public void removeInheritanceGroup(String group) {
        inheritanceGroups.remove(group.toLowerCase());
    }


    public static String getIDorNull(Group group) {
        return group != null ? group.getGroupID() : "null";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(this.groupID, group.groupID) && Objects.equals(prefix, group.prefix) && Objects.equals(suffix, group.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupID, prefix, suffix);
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupID='" + groupID + '\'' +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                '}';
    }
}
