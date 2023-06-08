package ua.klesaak.mineperms.manager.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.redis.messenger.MessageData;
import ua.klesaak.mineperms.manager.storage.redis.messenger.RedisMessenger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


// TODO: 06.06.2023 синхронизировать группы если не включен пуб-суб
public abstract class Storage implements AutoCloseable {
    protected final MinePermsManager manager;
    protected final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>(100);
    protected final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    protected final Cache<String, User> temporalUsersCache = CacheBuilder.newBuilder()
            .concurrencyLevel(16)
            .expireAfterWrite(1, TimeUnit.MINUTES).build(); //Временный кеш, чтобы уменьшить кол-во запросов в бд.
    protected RedisMessenger redisMessenger;

    public Storage(MinePermsManager manager) {
        this.manager = manager;
        if (manager.getConfigFile().isUseRedisPubSub()) {
            this.redisMessenger = new RedisMessenger(manager, this);
        }
    }

    protected void broadcastPacket(MessageData messageData) {
        if (this.redisMessenger != null) this.redisMessenger.sendOutgoingMessage(messageData);
    }

    public abstract void init();
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
    public abstract void deleteUser(String nickName);
    public abstract void updateUser(String nickName, User user);
    //////Group operations//////
    public abstract void addGroupPermission(String groupID, String permission);
    public abstract void removeGroupPermission(String groupID, String permission);
    public abstract void addGroupParent(String groupID, String parentID);
    public abstract void removeGroupParent(String groupID, String parentID);
    public abstract void setGroupPrefix(String groupID, String prefix);
    public abstract void setGroupSuffix(String groupID, String suffix);
    public abstract void deleteGroup(String groupID);
    public abstract void createGroup(String groupID);
    public abstract void updateGroup(String groupID, Group group);
    public abstract Collection<User> getAllUsersData();
    public abstract Collection<Group> getAllGroupsData();
    public abstract void importUsersData(Collection<User> users);
    public abstract void importGroupsData(Collection<Group> groups);

    @Override
    public abstract void close();

    public List<String> getGroupNames() {
        return Collections.list(this.groups.keys());
    }

    public boolean hasPermission(String nickName, String permission) {
        User user = this.getUser(nickName);
        if (user != null) return user.hasPermission(permission);
        return this.getDefaultGroup().hasPermission(permission);
    }

    public static boolean hasPermission(Set<String> permissions, String permission) {
        val permissionLowerCase = permission.toLowerCase();
        if (permissions.contains(MinePermsManager.ROOT_WILDCARD)) return true;
        if (!permissionLowerCase.contains(MinePermsManager.DOT_WILDCARD)) return permissions.contains(permissionLowerCase);
        if (permissions.contains(permissionLowerCase)) return true;
        String[] parts = permissionLowerCase.toLowerCase().split("\\.");
        StringBuilder partsBuilder = new StringBuilder();
        for (String part : parts) {
            partsBuilder.append(part).append(MinePermsManager.DOT_WILDCARD);
            if (permissions.contains(partsBuilder + MinePermsManager.ROOT_WILDCARD)) return true;
        }
        return false;
    }

    public boolean hasPlayerInGroup(String playerName, String groupID) {
        Group group = this.getGroup(groupID);
        if (group == null) return false;
        User user = this.getUser(playerName);
        if (user == null) return this.getDefaultGroup().getGroupID().equalsIgnoreCase(groupID);
        return user.hasGroup(groupID);
    }

    public String getUserGroup(String playerName) {
        User user = this.getUser(playerName);
        if (user == null) return this.getDefaultGroup().getGroupID();
        return user.getGroup();
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
        val user = this.getUser(nickName);
        if (user != null) {
            val playerMainGroupID = user.getGroup();
            list.add(playerMainGroupID);
            list.addAll(this.getGroup(playerMainGroupID).getInheritanceGroups());
        }
        return Collections.unmodifiableCollection(list);
    }

    public ConcurrentHashMap<String, Group> getGroups() {
        return groups;
    }

    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }

    public Cache<String, User> getTemporalUsersCache() {
        return temporalUsersCache;
    }

    public RedisMessenger getRedisMessenger() {
        return redisMessenger;
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
