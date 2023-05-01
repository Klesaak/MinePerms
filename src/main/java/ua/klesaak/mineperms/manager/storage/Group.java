package ua.klesaak.mineperms.manager.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class Group {
    private final String name;
    private final Set<Group> inheritanceGroups = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> permissions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private String prefix = "";
    private String suffix = "";

    public Group(String name) {
        this.name = name;
    }

    public Set<Group> getInheritanceGroups() {
        return Collections.unmodifiableSet(this.inheritanceGroups);
    }

    public boolean hasParentGroup(Group group) {
        return inheritanceGroups.contains(group);
    }

    public void addInheritanceGroup(Group group) {
        inheritanceGroups.add(group);
    }

    public void removeInheritanceGroup(Group group) {
        inheritanceGroups.remove(group);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }


    public static String getNameOrNull(Group group) {
        return group != null ? group.getName() : "null";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(name, group.name) && Objects.equals(prefix, group.prefix) && Objects.equals(suffix, group.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, prefix, suffix);
    }

    @Override
    public String toString() {
        return "Group{" +
                "name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                '}';
    }
}
