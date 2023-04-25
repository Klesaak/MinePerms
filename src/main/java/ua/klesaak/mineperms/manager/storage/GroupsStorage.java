package ua.klesaak.mineperms.manager.storage;

import lombok.Getter;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class GroupsStorage {
    protected final MinePermsManager manager;
    protected volatile Group defaultGroup;
    protected final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>(100);

    public GroupsStorage(MinePermsManager manager) {
        this.manager = manager;
    }

    public List<String> getGroupNames() {
        return Collections.list(groups.keys());
    }

    public Group getGroup(String name) {
        return groups.get(name);
    }

    public Group getGroupOrDefault(String name) {
        Group group = getGroup(name);
        if (group == null) {
            group = getDefaultGroup();
        }
        return group;
    }
}
