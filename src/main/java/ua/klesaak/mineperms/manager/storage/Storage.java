package ua.klesaak.mineperms.manager.storage;

import lombok.Getter;
import lombok.val;
import ua.klesaak.mineperms.MinePerms;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.entity.User;
import ua.klesaak.mineperms.manager.storage.redismessenger.MessageData;
import ua.klesaak.mineperms.manager.storage.redismessenger.RedisMessenger;
import ua.klesaak.mineperms.manager.utils.cache.ScheduledCache;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class Storage implements AutoCloseable {
    protected final MinePerms manager;
    protected final Map<String, Group> groups = new ConcurrentHashMap<>(100);
    protected final Map<String, User> users = new ConcurrentHashMap<>();

    //protected Cache<String, User> temporalUsersCache; //Временный кеш, чтобы уменьшить кол-во запросов в бд.
    protected ScheduledCache<String, User> temporalUsersCache; //Временный кеш, чтобы уменьшить кол-во запросов в бд.(Тест режим, в случае технических шоколадок вернуть кафеин)
    protected RedisMessenger redisMessenger;

    protected Storage(MinePerms manager) {
        this.manager = manager;
        if (manager.getStorageType().isSQL()) {
            //this.temporalUsersCache = Caffeine.newBuilder().executor(this.loaderPool).maximumSize(10_000).expireAfterWrite(Duration.ofMinutes(1)).build();
            this.temporalUsersCache = ScheduledCache.<String, User>builder().clearExpiredInterval(Duration.ofMinutes(10L)).setExpireTime(Duration.ofMinutes(1L)).build();
            if (manager.getConfigFile().isUseRedisPubSub() && this.redisMessenger == null) {
                this.redisMessenger = new RedisMessenger(manager, this);
            }
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
    public abstract User getCachedUser(String nickName);
    public abstract String getUserPrefix(String nickName);
    public abstract String getUserSuffix(String nickName);
    //////User operations//////
    public abstract void addUserPermission(String nickName, String permission);
    public abstract void removeUserPermission(String nickName, String permission);
    public abstract void setUserPrefix(String nickName, String prefix);
    public abstract void setUserSuffix(String nickName, String suffix);
    public abstract void setUserGroup(String nickName, String groupID);
    public abstract void deleteUser(String nickName);
    //////Group operations//////
    public abstract void addGroupPermission(String groupID, String permission);
    public abstract void removeGroupPermission(String groupID, String permission);
    public abstract void addGroupParent(String groupID, String parentID);
    public abstract void removeGroupParent(String groupID, String parentID);
    public abstract void setGroupPrefix(String groupID, String prefix);
    public abstract void setGroupSuffix(String groupID, String suffix);
    public abstract void deleteGroup(String groupID);
    public abstract void createGroup(String groupID);
    public abstract Collection<User> getAllUsersData();
    public abstract Collection<Group> getAllGroupsData();
    public abstract void importUsersData(Collection<User> users);
    public abstract void importGroupsData(Collection<Group> groups);

    @Override
    public abstract void close();

    public Collection<String> getGroupNames() {
        return this.groups.keySet();
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
        if (user == null) return this.getDefaultGroup().getGroupId().equalsIgnoreCase(groupId);
        return user.hasGroup(groupId);
    }

    public String getUserGroup(String playerName) {
        User user = this.getUser(playerName.toLowerCase());
        if (user == null) return this.getDefaultGroup().getGroupId();
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

    public void recalculateUsersPermissions() {
        //this.temporalUsersCache.asMap().values().forEach(user -> user.recalculatePermissions(this.groups));
        this.users.values().forEach(user -> user.recalculatePermissions(this.groups));
    }
}
