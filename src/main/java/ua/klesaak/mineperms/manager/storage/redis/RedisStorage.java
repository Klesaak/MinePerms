package ua.klesaak.mineperms.manager.storage.redis;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.util.concurrent.TimeUnit;

public class RedisStorage extends Storage {
    private final RedisConfig redisConfig;
    private final RedisPool redisPool;
    private final Cache<String, User> temporalUsersCache = CacheBuilder.newBuilder()
            .initialCapacity(Bukkit.getMaxPlayers())
            .concurrencyLevel(16)
            .expireAfterWrite(1, TimeUnit.MINUTES).build(); //Временный кеш, чтобы уменьшить кол-во запросов в бд.


    public RedisStorage(MinePermsManager manager) {
        super(manager);
        this.redisConfig = manager.getConfigFile().getRedisConfig();
        this.redisPool = new RedisPool(this.redisConfig);
    }

    @Override
    public void cacheUser(String nickName) {
        User user = this.temporalUsersCache.getIfPresent(nickName);
        if (user != null) {
            this.users.put(nickName, user);
            this.temporalUsersCache.invalidate(nickName);
        }
        user = this.getUser(nickName);
        if (user != null) {
            this.users.put(nickName, user);
            return;
        }
        val newUser = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        newUser.recalculatePermissions(this.groups);
        this.users.put(nickName, newUser);
    }


    /**
     * При выходе игрока с сервера отгружаем его из основного кеша во временный
     * чтобы в случае быстрого перезахода игрока не тратить лишние ресурсы на его подгрузку из БД
     */
    @Override
    public void unCacheUser(String nickName) {
        User user = this.users.remove(nickName);
        this.temporalUsersCache.put(nickName, user);
    }

    @Override
    public void saveUser(String nickName) {

    }

    @Override
    public void saveUser(String nickName, User user) {

    }

    @Override
    public void saveGroup(String groupID) {

    }

    @Override
    public User getUser(String nickName) {
        User user = this.temporalUsersCache.getIfPresent(nickName);
        if (user != null) return user;

        user = this.users.get(nickName);
        if (user != null) return user;

        try (Jedis jed = this.redisPool.getRedis()) {
            jed.select(this.redisConfig.getDatabase());
            if (jed.hexists(this.redisConfig.getUsersKey(), nickName)) {
                return JsonData.GSON.fromJson(jed.hget(this.redisConfig.getUsersKey(), nickName), User.class);
            }
        }
        return null;
    }

    @Override
    public String getUserPrefix(String nickName) {
        return null;
    }

    @Override
    public String getUserSuffix(String nickName) {
        return null;
    }

    @Override
    public void addUserPermission(String nickName, String permission) {

    }

    @Override
    public void removeUserPermission(String nickName, String permission) {

    }

    @Override
    public void setUserPrefix(String nickName, String prefix) {

    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {

    }

    @Override
    public void setUserGroup(String nickName, String groupID) {

    }

    @Override
    public void deleteUser(String nickName) {

    }

    @Override
    public void updateUser(String nickName, User user) {
        if (this.temporalUsersCache.getIfPresent(nickName) == null && this.users.get(nickName) == null) return;
        user.recalculatePermissions(this.groups);
        if (this.temporalUsersCache.getIfPresent(nickName) != null) {
            this.temporalUsersCache.put(nickName, user);
            return;
        }
        this.users.put(nickName, user);
    }

    @Override
    public void addGroupPermission(String groupID, String permission) {

    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {

    }

    @Override
    public void addGroupParent(String groupID, String parentID) {

    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {

    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {

    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {

    }

    @Override
    public void deleteGroup(String groupID) {

    }

    @Override
    public void createGroup(String groupID) {

    }

    @Override
    public void updateGroup(String groupID, Group group) {
        this.groups.put(groupID, group); //не проверяем потому что может быть факт создания группы
    }

    @Override
    public void close() {
        this.redisPool.getRedis().close();
        if (this.redisMessenger != null) this.redisMessenger.close();
    }
}
