package ua.klesaak.mineperms.manager.storage.redis;

import lombok.Getter;
import lombok.val;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;
import ua.klesaak.mineperms.manager.storage.redis.messenger.MessageData;
import ua.klesaak.mineperms.manager.storage.redis.messenger.MessageType;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
public class RedisStorage extends Storage {
    private final RedisPool redisPool;

    public RedisStorage(MinePermsManager manager) {
        super(manager);
        this.redisPool = new RedisPool(manager.getConfigFile().getRedisSettings());
    }

    @Override
    public void init() {
        CompletableFuture.runAsync(() -> {
            try (Jedis jed = this.redisPool.getRedis()) {
                val config = manager.getConfigFile().getRedisSettings();
                jed.select(config.getDatabase());
                val allData = jed.hgetAll(config.getGroupsKey());
                allData.forEach((groupID, groupJsonObject) -> this.groups.put(groupID, JsonData.GSON.fromJson(groupJsonObject, Group.class)));

                val defaultGroup = manager.getConfigFile().getDefaultGroup();
                if (this.getGroup(defaultGroup) == null) {
                    this.createGroup(defaultGroup);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while load groups data", e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void cacheUser(String nickName) { //todo поместить в евенты(Velocity)!
        User user = this.temporalUsersCache.getIfPresent(nickName) != null ? this.temporalUsersCache.getIfPresent(nickName) : this.getUser(nickName);
        if (!this.manager.getConfigFile().isUseRedisPubSub()) { //загружаем игрока из бд при каждом заходе, чтобы была актуальность данных!
            user = CompletableFuture.supplyAsync(() -> this.loadUser(nickName))
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    }).join();
        }
        if (user != null) {
            user.recalculatePermissions(this.groups);
            this.users.put(nickName, user);
            this.temporalUsersCache.invalidate(nickName);
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
    public void unCacheUser(String nickName) { //todo поместить в евенты(Velocity)!
        User user = this.users.remove(nickName);
        this.temporalUsersCache.put(nickName, user);
    }

    @Override
    public void saveUser(String nickName) {
        User user = this.temporalUsersCache.getIfPresent(nickName) != null ? this.temporalUsersCache.getIfPresent(nickName) : this.users.get(nickName);
        if (user != null) {
            this.saveUser(nickName, user);
        }
    }

    @Override
    public void saveUser(String nickName, User user) {
        CompletableFuture.runAsync(()-> {
            try (Jedis jed = this.redisPool.getRedis()) {
                val config = manager.getConfigFile().getRedisSettings();
                jed.select(config.getDatabase());
                jed.hset(config.getUsersKey(), nickName, JsonData.GSON.toJson(user));
            } catch (Exception e) {
                throw new RuntimeException("Error while save user " + nickName + " data", e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void saveGroup(String groupID) {
        val group = this.groups.get(groupID);
        if (group == null) return;
        CompletableFuture.runAsync(()-> {
            try (Jedis jed = this.redisPool.getRedis()) {
                val config = manager.getConfigFile().getRedisSettings();
                jed.select(config.getDatabase());
                jed.hset(config.getGroupsKey(), groupID, JsonData.GSON.toJson(group));
            } catch (Exception e) {
                throw new RuntimeException("Error while save group " + groupID + " data", e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public User getUser(String nickName) {
        User user = this.temporalUsersCache.getIfPresent(nickName) != null ? this.temporalUsersCache.getIfPresent(nickName) : this.users.get(nickName);
        if (user != null) return user;
        user = CompletableFuture.supplyAsync(() -> this.loadUser(nickName))
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                }).join();
        if (user != null) {
            this.temporalUsersCache.put(nickName, user);
            user.recalculatePermissions(this.groups);
        }
        return user;
    }

    private User loadUser(String nickName) {
        try (Jedis jed = this.redisPool.getRedis()) {
            val config = manager.getConfigFile().getRedisSettings();
            jed.select(config.getDatabase());
            val userData = jed.hget(config.getUsersKey(), nickName);
            return JsonData.GSON.fromJson(userData, User.class);
        } catch (Exception e) {
            throw new RuntimeException("Error while load user data for " + nickName, e);
        }
    }

    @Override
    public String getUserPrefix(String nickName) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        }
        return user.getPrefix().isEmpty() ? this.getGroupOrDefault(user.getGroup()).getPrefix() : user.getPrefix();
    }

    @Override
    public String getUserSuffix(String nickName) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        }
        return user.getSuffix().isEmpty() ? this.getGroupOrDefault(user.getGroup()).getSuffix() : user.getSuffix();
    }

    @Override
    public void addUserPermission(String nickName, String permission) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
            this.temporalUsersCache.put(nickName, user);
        }
        user.addPermission(permission);
        this.saveUser(nickName, user);
        this.broadcastUpdatePacket(new MessageData(user, MessageType.USER_UPDATE));
    }

    @Override
    public void removeUserPermission(String nickName, String permission) {
        User user = this.getUser(nickName);
        if (user == null) return;
        user.removePermission(permission);
        this.saveUser(nickName, user);
        this.broadcastUpdatePacket(new MessageData(user, MessageType.USER_UPDATE));
    }

    @Override
    public void setUserPrefix(String nickName, String prefix) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
            this.temporalUsersCache.put(nickName, user);
        }
        user.setPrefix(prefix);
        this.saveUser(nickName, user);
        this.broadcastUpdatePacket(new MessageData(user, MessageType.USER_UPDATE));
    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
            this.temporalUsersCache.put(nickName, user);
        }
        user.setSuffix(suffix);
        this.saveUser(nickName, user);
        this.broadcastUpdatePacket(new MessageData(user, MessageType.USER_UPDATE));
    }

    @Override
    public void setUserGroup(String nickName, String groupID) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
            this.temporalUsersCache.put(nickName, user);
        }
        if (this.groups.get(groupID) != null) {
            user.setGroup(groupID);
            this.saveUser(nickName, user);
            user.recalculatePermissions(this.groups);
            this.manager.getEventManager().callGroupChangeEvent(user);
        }
        this.broadcastUpdatePacket(new MessageData(user, MessageType.USER_UPDATE));
    }

    @Override
    public void deleteUser(String nickName) {
        val newUser = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        if (this.users.get(nickName) != null) {
            this.users.put(nickName, newUser);
        }
        if (this.temporalUsersCache.getIfPresent(nickName) != null) {
            this.temporalUsersCache.put(nickName, newUser);
        }
        CompletableFuture.runAsync(()-> {
            try (Jedis jed = this.redisPool.getRedis()) {
                val config = manager.getConfigFile().getRedisSettings();
                jed.select(config.getDatabase());
                if (jed.hexists(config.getUsersKey(), nickName)) {
                    jed.hdel(config.getUsersKey(), nickName);
                    this.broadcastUpdatePacket(new MessageData(nickName, MessageType.USER_DELETE));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while delete user " + nickName + " data", e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
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
        val group = this.getGroup(groupID);
        group.addPermission(permission);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
        this.broadcastUpdatePacket(new MessageData(group, MessageType.GROUP_UPDATE));
    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {
        val group = this.getGroup(groupID);
        group.removePermission(permission);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
        this.broadcastUpdatePacket(new MessageData(group, MessageType.GROUP_UPDATE));
    }

    @Override
    public void addGroupParent(String groupID, String parentID) {
        val group = this.getGroup(groupID);
        group.addInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
        this.broadcastUpdatePacket(new MessageData(group, MessageType.GROUP_UPDATE));
    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {
        val group = this.getGroup(groupID);
        group.removeInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
        this.broadcastUpdatePacket(new MessageData(group, MessageType.GROUP_UPDATE));
    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {
        val group = this.getGroup(groupID);
        group.setPrefix(prefix);
        this.saveGroup(groupID);
        this.broadcastUpdatePacket(new MessageData(group, MessageType.GROUP_UPDATE));
    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {
        val group = this.getGroup(groupID);
        group.setSuffix(suffix);
        this.saveGroup(groupID);
        this.broadcastUpdatePacket(new MessageData(group, MessageType.GROUP_UPDATE));
    }

    @Override
    public void deleteGroup(String groupID) {
        this.groups.remove(groupID);
        CompletableFuture.runAsync(()-> {
            try (Jedis jed = this.redisPool.getRedis()) {
                val config = manager.getConfigFile().getRedisSettings();
                jed.select(config.getDatabase());
                if (jed.hexists(config.getGroupsKey(), groupID)) {
                    jed.hdel(config.getGroupsKey(), groupID);
                    this.broadcastUpdatePacket(new MessageData(groupID, MessageType.GROUP_DELETE));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while delete group " + groupID + " data", e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void createGroup(String groupID) {
        val newGroup = new Group(groupID);
        this.groups.put(groupID, newGroup);
        CompletableFuture.runAsync(()-> {
            try (Jedis jed = this.redisPool.getRedis()) {
                val config = manager.getConfigFile().getRedisSettings();
                jed.select(config.getDatabase());
                jed.hset(config.getGroupsKey(), groupID, JsonData.GSON.toJson(newGroup));
                this.broadcastUpdatePacket(new MessageData(newGroup, MessageType.GROUP_UPDATE));
            } catch (Exception e) {
                throw new RuntimeException("Error while delete group " + groupID + " data", e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void updateGroup(String groupID, Group group) {
        if (this.groups.get(groupID) == null) return;
        this.groups.put(groupID, group);
        this.recalculateUsersPermissions();
    }

    @Override
    public Collection<User> getAllUsersData() {
        Set<User> users = new HashSet<>();
        try (Jedis jed = this.redisPool.getRedis()) {
            val config = manager.getConfigFile().getRedisSettings();
            jed.select(config.getDatabase());
            val usersData = jed.hgetAll(config.getUsersKey()).values();
            for (String data : usersData) {
                users.add(JsonData.GSON.fromJson(data, User.class));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while get all users data", e);
        }

        return Collections.unmodifiableCollection(users);
    }

    @Override
    public Collection<Group> getAllGroupsData() {
        Set<Group> groups = new HashSet<>();
        try (Jedis jed = this.redisPool.getRedis()) {
            val config = manager.getConfigFile().getRedisSettings();
            jed.select(config.getDatabase());
            val groupsData = jed.hgetAll(config.getGroupsKey()).values();
            for (String data : groupsData) {
                groups.add(JsonData.GSON.fromJson(data, Group.class));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while get all groups data", e);
        }
        return Collections.unmodifiableCollection(groups);
    }

    @Override
    public void importUsersData(Collection<User> users) {
        try (Jedis jed = this.redisPool.getRedis()) {
            val config = manager.getConfigFile().getRedisSettings();
            jed.select(config.getDatabase());
            Pipeline pip = jed.pipelined();
            Map<String, String> usersData = new HashMap<>();
            for (User user : users) {
                usersData.put(user.getPlayerName(), JsonData.GSON.toJson(user));
            }
            pip.hmset(config.getUsersKey(), usersData);
            pip.sync();
        } catch (Exception e) {
            throw new RuntimeException("Error while import users data", e);
        }
    }

    @Override
    public void importGroupsData(Collection<Group> groups) {
        try (Jedis jed = this.redisPool.getRedis()) {
            val config = manager.getConfigFile().getRedisSettings();
            jed.select(config.getDatabase());
            Pipeline pip = jed.pipelined();
            Map<String, String> groupsData = new HashMap<>();
            for (Group group : groups) {
                groupsData.put(group.getGroupID(), JsonData.GSON.toJson(group));
            }
            pip.hmset(config.getGroupsKey(), groupsData);
            pip.sync();
        } catch (Exception e) {
            throw new RuntimeException("Error while import groups data", e);
        }
    }

    @Override
    public void close() {
        this.redisPool.getRedis().close();
        if (this.redisMessenger != null) this.redisMessenger.close();
    }
}
