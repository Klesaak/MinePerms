package ua.klesaak.mineperms.manager.storage;

import ua.klesaak.mineperms.MinePermsManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UsersStorage {
    protected final MinePermsManager manager;
    protected final GroupsStorage groups;
    protected final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();

    public UsersStorage(MinePermsManager manager, GroupsStorage groups) {
        this.manager = manager;
        this.groups = groups;
    }

    public void recalculateUsersPermissionsByGroup(String groupId) {

    }
}
