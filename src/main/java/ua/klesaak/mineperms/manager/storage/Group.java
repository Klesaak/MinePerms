package ua.klesaak.mineperms.manager.storage;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
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

    ///Transient Data///
    private transient String jsonInheritanceGroups; //костыль для ORMLite
    private transient String jsonPerms; //костыль для ORMLite

    public Group(String groupID) {
        this.groupID = groupID;
    }

    public boolean hasPermission(String permission) {
        val permissionLowerCase = permission.toLowerCase();
        if (this.permissions.contains(MinePermsManager.ROOT_WILDCARD)) return true;
        if (!permissionLowerCase.contains(MinePermsManager.DOT_WILDCARD)) return this.permissions.contains(permissionLowerCase);
        if (this.permissions.contains(permissionLowerCase)) return true;
        String[] parts = permissionLowerCase.toLowerCase().split("\\.");
        StringBuilder partsBuilder = new StringBuilder();
        for (String part : parts) {
            partsBuilder.append(part).append(MinePermsManager.DOT_WILDCARD);
            if (this.permissions.contains(partsBuilder + MinePermsManager.ROOT_WILDCARD)) return true;
        }
        return false;
    }

    public boolean hasOwnPermission(String permission) {
        val permissionLowerCase = permission.toLowerCase();
        return this.permissions.contains(permissionLowerCase);
    }

    public boolean hasGroup(String groupID) {
        return this.inheritanceGroups.contains(groupID.toLowerCase());
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
                ", inheritanceGroups=" + inheritanceGroups +
                ", permissions=" + permissions +
                ", jsonInheritanceGroups='" + jsonInheritanceGroups + '\'' +
                ", jsonPerms='" + jsonPerms + '\'' +
                '}';
    }
}
