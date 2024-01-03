package ua.klesaak.mineperms.manager.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.entity.User;
import ua.klesaak.mineperms.manager.storage.redis.messenger.MessageData;
import ua.klesaak.mineperms.manager.storage.redis.messenger.RedisMessenger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class Storage implements AutoCloseable {
    protected final MinePermsManager manager;
    protected final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>(100);
    protected final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    protected final Cache<String, User> temporalUsersCache = CacheBuilder.newBuilder()
            .concurrencyLevel(16)
            .expireAfterWrite(1, TimeUnit.MINUTES).build(); //Временный кеш, чтобы уменьшить кол-во запросов в бд.
    protected RedisMessenger redisMessenger;

    protected Storage(MinePermsManager manager) {
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
        User user = this.getUser(nickName.toLowerCase());
        if (user != null) return user.hasPermission(permission);
        return this.getDefaultGroup().hasPermission(permission);
    }

    public boolean hasPlayerInGroup(String playerName, String groupId) {
        Group group = this.getGroup(groupId);
        if (group == null) return false;
        User user = this.getUser(playerName.toLowerCase());
        if (user == null) return this.getDefaultGroup().getGroupID().equalsIgnoreCase(groupId);
        return user.hasGroup(groupId);
    }

    public String getUserGroup(String playerName) {
        User user = this.getUser(playerName.toLowerCase());
        if (user == null) return this.getDefaultGroup().getGroupID();
        return user.getGroupId();
    }

    public Group getGroupOrDefault(String groupId) {
        Group group = this.groups.get(groupId.toLowerCase());
        if (group == null) {
            group = this.groups.get(this.manager.getConfigFile().getDefaultGroup());
        }
        return group;
    }

    public Group getGroup(String groupId) {
        return this.groups.get(groupId.toLowerCase());
    }

    public Group getDefaultGroup() {
        return this.groups.get(this.manager.getConfigFile().getDefaultGroup());
    }

    public Collection<String> getUserInheritedGroups(String nickName) {
        val list = new ArrayList<String>();
        val user = this.getUser(nickName.toLowerCase());
        if (user != null) {
            val playerMainGroupID = user.getGroupId();
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

    public void recalculateUsersPermissions() {
        //this.temporalUsersCache.asMap().values().forEach(user -> user.recalculatePermissions(this.groups));
        this.users.values().forEach(user -> user.recalculatePermissions(this.groups));
    }
}
