package ua.klesaak.mineperms.manager.storage.entity;

import lombok.Getter;
import lombok.Setter;
import ua.klesaak.mineperms.manager.storage.Storage;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class Group extends AbstractEntity {
    private Set<String> inheritanceGroups = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Group(String groupId) {
        super(groupId);
    }

    @Override
    public boolean hasPermission(String permission) {
        return Storage.hasPermission(this.permissions, permission);
    }

    @Override
    public void addPermission(String permission) {
        this.permissions.add(permission.toLowerCase());
    }

    @Override
    public void removePermission(String permission) {
        this.permissions.remove(permission.toLowerCase());
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
