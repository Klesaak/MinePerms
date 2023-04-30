package ua.klesaak.mineperms.manager.storage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter @EqualsAndHashCode @ToString
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
}
