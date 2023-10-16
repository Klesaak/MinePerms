package ua.klesaak.mineperms.manager.storage.entity;

import lombok.Getter;
import ua.klesaak.mineperms.manager.utils.PermissionsMatcher;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class AbstractEntity {
    protected final String entityId;
    protected volatile String prefix = "";
    protected volatile String suffix = "";
    protected Set<String> permissions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected transient volatile PermissionsMatcher permissionsMatcher = new PermissionsMatcher();

    protected AbstractEntity(String entityId) {
        this.entityId = entityId;
    }

    public abstract boolean hasPermission(String permission);
    public abstract void addPermission(String permission);
    public abstract void removePermission(String permission);

    public boolean hasOwnPermission(String permission) {
        return this.permissions.contains(permission.toLowerCase());
    }

    public void addOwnPermission(String permission) {
        this.permissions.add(permission.toLowerCase());
    }

    public void removeOwnPermission(String permission) {
        this.permissions.remove(permission.toLowerCase());
    }

    public void setPermissions(Set<String> permissions) {
        Set<String> perms = Collections.newSetFromMap(new ConcurrentHashMap<>());
        perms.addAll(permissions);
        this.permissions = perms;
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? "": prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix == null ? "": suffix;
    }
}
