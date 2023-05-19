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
        try (Jedis jed = this.redisPool.getRedis()) {
            jed.select(this.redisConfig.getDatabase());
            val allData = jed.hgetAll(this.redisConfig.getGroupsKey());
            allData.forEach((groupID, groupJsonObject) -> this.groups.put(groupID, JsonData.GSON.fromJson(groupJsonObject, Group.class)));
        } catch (Exception e) {
            throw new RuntimeException("Error while load groups data", e);
        }
        if (this.getGroup(manager.getConfigFile().getDefaultGroup()) == null) {
            this.createGroup(manager.getConfigFile().getDefaultGroup());
        }
    }

    @Override
    public void cacheUser(String nickName) { //todo поместить в евенты!
        User user = this.temporalUsersCache.getIfPresent(nickName);
        if (user != null) {
            this.users.put(nickName, user);
            this.temporalUsersCache.invalidate(nickName);
            return;
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
    public void unCacheUser(String nickName) { //todo поместить в евенты!
        User user = this.users.remove(nickName);
        this.temporalUsersCache.put(nickName, user);
    }

    @Override
    public void saveUser(String nickName) {
        User user = this.temporalUsersCache.getIfPresent(nickName);
        if (user != null) {
            this.saveUser(nickName, user);
            return;
        }
        user = this.users.get(nickName);
        if (user != null) {
            this.saveUser(nickName, user);
        }
    }

    @Override
    public void saveUser(String nickName, User user) {
        try (Jedis jed = this.redisPool.getRedis()) {
            jed.select(this.redisConfig.getDatabase());
            jed.hset(this.redisConfig.getUsersKey(), nickName, JsonData.GSON.toJson(user));
        } catch (Exception e) {
            throw new RuntimeException("Error while save user " + nickName + " data", e);
        }
    }

    @Override
    public void saveGroup(String groupID) {
        val group = this.groups.get(groupID);
        if (group == null) return;
        try (Jedis jed = this.redisPool.getRedis()) {
            jed.select(this.redisConfig.getDatabase());
            jed.hset(this.redisConfig.getGroupsKey(), groupID, JsonData.GSON.toJson(group));
        } catch (Exception e) {
            throw new RuntimeException("Error while save group " + groupID + " data", e);
        }
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
        } catch (Exception e) {
            throw new RuntimeException("Error while get user " + nickName + " data", e);
        }
        return null;
    }

    @Override
    public String getUserPrefix(String nickName) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
            this.users.put(nickName, user);
        }
        return user.getPrefix().isEmpty() ? this.getGroup(user.getGroup()).getPrefix() : user.getPrefix();
    }

    @Override
    public String getUserSuffix(String nickName) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
            this.users.put(nickName, user);
        }
        return user.getSuffix().isEmpty() ? this.getGroup(user.getGroup()).getSuffix() : user.getSuffix();
    }

    @Override
    public void addUserPermission(String nickName, String permission) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.getDefaultGroup().getGroupID());
            this.users.put(nickName, user);
        }
        user.addPermission(permission);
        this.saveUser(nickName, user);
    }

    @Override
    public void removeUserPermission(String nickName, String permission) {
        User user = this.getUser(nickName);
        if (user == null) return;
        user.removePermission(permission);
        this.saveUser(nickName, user);
    }

    @Override
    public void setUserPrefix(String nickName, String prefix) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.getDefaultGroup().getGroupID());
            this.users.put(nickName, user);
        }
        user.setPrefix(prefix);
        this.saveUser(nickName, user);
    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.getDefaultGroup().getGroupID());
            this.users.put(nickName, user);
        }
        user.setSuffix(suffix);
        this.saveUser(nickName, user);
    }

    @Override
    public void setUserGroup(String nickName, String groupID) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.getDefaultGroup().getGroupID());
            this.users.put(nickName, user);
        }
        if (this.groups.get(groupID) != null) {
            user.setGroup(groupID);
            this.saveUser(nickName, user);
            user.recalculatePermissions(this.groups);
            this.manager.getEventManager().callGroupChangeEvent(user);
        }
    }

    @Override
    public void deleteUser(String nickName) {
        val newUser = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        if (this.temporalUsersCache.getIfPresent(nickName) != null) {
            this.temporalUsersCache.put(nickName, newUser);
        }
        if (this.users.get(nickName) != null) {
            this.users.put(nickName, newUser);
        }
        try (Jedis jed = this.redisPool.getRedis()) {
            jed.select(this.redisConfig.getDatabase());
            if (jed.hexists(this.redisConfig.getUsersKey(), nickName)) {
                jed.hdel(this.redisConfig.getUsersKey(), nickName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while delete user " + nickName + " data", e);
        }
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
        this.getGroup(groupID).addPermission(permission);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {
        this.getGroup(groupID).removePermission(permission);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void addGroupParent(String groupID, String parentID) {
        this.getGroup(groupID).addInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {
        this.getGroup(groupID).removeInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {
        this.getGroup(groupID).setPrefix(prefix);
        this.saveGroup(groupID);
    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {
        this.getGroup(groupID).setSuffix(suffix);
        this.saveGroup(groupID);
    }

    @Override
    public void deleteGroup(String groupID) {
        this.groups.remove(groupID);
        try (Jedis jed = this.redisPool.getRedis()) {
            jed.select(this.redisConfig.getDatabase());
            if (jed.hexists(this.redisConfig.getGroupsKey(), groupID)) {
                jed.hdel(this.redisConfig.getGroupsKey(), groupID);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while delete group " + groupID + " data", e);
        }
    }

    @Override
    public void createGroup(String groupID) {
        val newGroup = new Group(groupID);
        this.groups.put(groupID, newGroup);
        try (Jedis jed = this.redisPool.getRedis()) {
            jed.select(this.redisConfig.getDatabase());
            jed.hset(this.redisConfig.getGroupsKey(), groupID, JsonData.GSON.toJson(newGroup));
        } catch (Exception e) {
            throw new RuntimeException("Error while delete group " + groupID + " data", e);
        }
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
