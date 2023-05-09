package ua.klesaak.mineperms.manager.storage;

import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Storage {
    protected final MinePermsManager manager;
    protected final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>(100);
    protected final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public Storage(MinePermsManager manager) {
        this.manager = manager;
    }

    public abstract void cacheUser(String nickName);
    public abstract void unCacheUser(String nickName);
    public abstract void saveUser(String nickName);
    public abstract void saveUser(String nickName, User user);
    public abstract void saveGroup(String groupID);
    public abstract User getUser(String nickName);
    public abstract String getUserPrefix(String nickName);
    public abstract String getUserSuffix(String nickName);
    //////User operations//////
    public abstract void addUserPermission(String nickName, String permission);
    public abstract void removeUserPermission(String nickName, String permission);
    public abstract void setUserPrefix(String nickName, String prefix);
    public abstract void setUserSuffix(String nickName, String suffix);
    public abstract void setUserGroup(String nickName, String groupID);
    public abstract void setUserOption(String nickName, String optionKey, String stringOption);
    public abstract void setUserOption(String nickName, String optionKey, boolean booleanOption);
    public abstract void setUserOption(String nickName, String optionKey, int integerOption);
    public abstract void deleteUser(String nickName);
    public abstract void updateUser(String nickName);
    //////Group operations//////
    public abstract void addGroupPermission(String groupID, String permission);
    public abstract void removeGroupPermission(String groupID, String permission);
    public abstract void addGroupParent(String groupID, String parentID);
    public abstract void removeGroupParent(String groupID, String parentID);
    public abstract void setGroupPrefix(String groupID, String prefix);
    public abstract void setGroupSuffix(String groupID, String suffix);
    public abstract void setGroupOption(String groupID, String optionKey, String stringOption);
    public abstract void setGroupOption(String groupID, String optionKey, boolean booleanOption);
    public abstract void setGroupOption(String groupID, String optionKey, int integerOption);
    public abstract void deleteGroup(String groupID);
    public abstract void createGroup(String groupID);
    public abstract void updateGroup(String groupID);

    public abstract void close();

    public List<String> getGroupNames() {
        return Collections.list(this.groups.keys());
    }

    public boolean hasPermission(String nickName, String permission) {
        User user = this.getUser(nickName);
        if (user != null) return user.hasPermission(permission);
        return this.getDefaultGroup().hasPermission(permission);
    }

    public Group getGroupOrDefault(String groupID) {
        Group group = this.groups.get(groupID.toLowerCase());
        if (group == null) {
            group = this.groups.get(this.manager.getConfigFile().getDefaultGroup());
        }
        return group;
    }

    public Group getGroup(String groupID) {
        return this.groups.get(groupID.toLowerCase());
    }

    public Group getDefaultGroup() {
        return this.groups.get(this.manager.getConfigFile().getDefaultGroup());
    }

    public Collection<String> getUserInheritedGroups(String nickName) {
        val list = new ArrayList<String>();
        val playerMainGroupID = this.getUser(nickName).getGroup();
        list.add(playerMainGroupID);
        list.addAll(this.getGroup(playerMainGroupID).getInheritanceGroups());
        return Collections.unmodifiableCollection(list);
    }

    public void recalculateUsersPermissionsByGroup(String groupId) {
        for (User user : this.users.values()) {
            if (user.getGroup().equalsIgnoreCase(groupId)) user.recalculatePermissions(this.groups);
        }
    }
    public void recalculateUsersPermissions() {
        this.users.values().forEach(user -> user.recalculatePermissions(this.groups));
    }
}
