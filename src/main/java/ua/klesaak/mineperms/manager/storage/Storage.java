package ua.klesaak.mineperms.manager.storage;

import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Storage {
    protected final MinePermsManager manager;
    protected final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>(100);
    protected final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();

    public Storage(MinePermsManager manager) {
        this.manager = manager;
    }

    public abstract void cacheUser(UUID userID);
    public abstract void unCacheUser(UUID userID);
    public abstract void saveUser(UUID userID);
    public abstract void saveGroup(String groupID);
    public abstract void close();

    public List<String> getGroupNames() {
        return Collections.list(groups.keys());
    }

    public Group getGroupOrDefault(String name) {
        Group group = this.groups.get(name);
        if (group == null) {
            group = this.groups.get(this.manager.getConfigFile().getDefaultGroup());
        }
        return group;
    }

    public User getUser(UUID uuid) {
        return this.users.get(uuid);
    }

    public User getUser(String nickName) {
        for (val user : this.users.values()) {
            if (user.getPlayerName().equalsIgnoreCase(nickName)) return user;
        }
        return null;
    }

    public void recalculateUsersPermissionsByGroup(String groupId) {
        for (User user : this.users.values()) {
            if (user.getGroup().equalsIgnoreCase(groupId)) user.recalculatePermissions();
        }
    }
}
