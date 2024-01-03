package ua.klesaak.mineperms.manager.storage.entity;

import lombok.Getter;
import ua.klesaak.mineperms.manager.utils.PermissionsMatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Group extends AbstractEntity {
    private Set<String> inheritanceGroups = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Group(String groupId) {
        super(groupId);
    }

    @Override
    public boolean hasPermission(String permission) {
        if (this.permissionsMatcher == null) {
            this.permissionsMatcher = new PermissionsMatcher();
            this.permissionsMatcher.add(this.permissions);
        }
        return this.permissionsMatcher.hasPermission(permission);
    }

    @Override
    public void addPermission(String permission) {
        String perm = permission.toLowerCase();
        this.permissions.add(perm);
        if (this.permissionsMatcher == null) {
            this.permissionsMatcher = new PermissionsMatcher();
            this.permissionsMatcher.add(this.permissions);
            return;
        }
        this.permissionsMatcher.add(perm);
    }

    @Override
    public void removePermission(String permission) {
        this.permissions.remove(permission.toLowerCase());
        if (this.permissionsMatcher == null) {
            this.permissionsMatcher = new PermissionsMatcher();
            this.permissionsMatcher.add(this.permissions);
            return;
        }
        this.permissionsMatcher.clear();
        this.permissionsMatcher.add(this.permissions);
    }

    public boolean hasGroup(String groupId) {
        return this.inheritanceGroups.contains(groupId.toLowerCase());
    }

    public void addInheritanceGroup(String groupId) {
        inheritanceGroups.add(groupId.toLowerCase());
    }

    public void removeInheritanceGroup(String groupId) {
        inheritanceGroups.remove(groupId.toLowerCase());
    }

    public void setInheritanceGroups(Collection<String> inheritanceGroups) {
        Set<String> groups = Collections.newSetFromMap(new ConcurrentHashMap<>());
        groups.addAll(inheritanceGroups);
        this.inheritanceGroups = groups;
    }

    public String getGroupID() {
        return this.entityId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(this.entityId, group.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, prefix, suffix);
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId='" + entityId + '\'' +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", inheritanceGroups=" + inheritanceGroups +
                ", permissions=" + permissions +
                '}';
    }
}
