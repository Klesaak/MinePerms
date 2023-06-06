package ua.klesaak.mineperms.manager.storage;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class Group {
    private String groupID;
    private String prefix = "";
    private String suffix = "";
    private Set<String> inheritanceGroups = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Set<String> permissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Group(String groupID) {
        this.groupID = groupID;
    }

    protected Group() {
    }

    public boolean hasPermission(String permission) {
        return Storage.hasPermission(this.permissions, permission);
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

    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? "": prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix == null ? "": suffix;
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
                '}';
    }
}
