package ua.klesaak.mineperms.manager.storage.redismessenger;

import lombok.val;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.StorageType;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.entity.User;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

public class RedisMessenger implements AutoCloseable {
    public static UUID SERVER_UUID = UUID.randomUUID(); //Уникальный идентификатор сервера для корректного обмена сообщениями через Redis-pub-sub
    private final MinePermsManager minePermsManager;
    private final Storage storage;
    private final RedisPool redisPool;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final Subscription sub;
    private boolean closing = false;

    public RedisMessenger(MinePermsManager minePermsManager, Storage storage) {
        this.minePermsManager = minePermsManager;
        this.storage = storage;
        this.redisPool = new RedisPool(minePermsManager.getConfigFile().getRedisSettings());
        this.sub = new Subscription();
        this.executorService.execute(this.sub);
    }

    public void sendOutgoingMessage(MessageData messageData) {
        try (Jedis jedis = this.redisPool.getRedis()) {
            messageData.setUuid(SERVER_UUID);
            jedis.publish(RedisConfig.UPDATE_CHANNEL_NAME, messageData.toJson());
        } catch (Exception e) {
            throw new RuntimeException("Error while publish message", e);
        }
    }

    @Override
    public void close() {
        this.closing = true;
        this.sub.unsubscribe();
        this.redisPool.getRedis().close();
        this.redisPool.getJedisPool().destroy();
        this.executorService.shutdown();
    }

    private class Subscription extends JedisPubSub implements Runnable {

        @Override
        public void run() {
            boolean first = true;
            while (!RedisMessenger.this.closing && !Thread.interrupted() && !RedisMessenger.this.redisPool.getJedisPool().isClosed()) {
                try (Jedis jedis = RedisMessenger.this.redisPool.getRedis()) {
                    if (first) {
                        first = false;
                    } else {
                        System.out.println("[MinePerms] Redis pub-sub connection re-established");
                    }

                    jedis.subscribe(this, RedisConfig.UPDATE_CHANNEL_NAME); // blocking call
                } catch (Exception e) {
                    if (RedisMessenger.this.closing) {
                        return;
                    }

                    System.out.println("[MinePerms] Redis pub-sub connection dropped, trying to re-open the connection " + e);
                    try {
                        unsubscribe();
                    } catch (Exception ignored) {

                    }

                    // Sleep for 5 seconds to prevent massive spam in console
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        @Override
        public void onMessage(String channel, String msg) {
            if (!channel.equals(RedisConfig.UPDATE_CHANNEL_NAME)) return;
            val messageData = MessageData.fromJson(msg);
            if (messageData.getUuid().equals(SERVER_UUID)) return;
            val storage = RedisMessenger.this.storage;
            val config = RedisMessenger.this.minePermsManager.getConfigFile();
            val userCache = storage.getUsers();
            val groupsCache = storage.getGroups();
            val temporalUserCache = storage.getTemporalUsersCache();
            val defaultGroupId = storage.getDefaultGroup().getGroupID();
            String subChannel = config.getSQLSettings().getGroupsPermissionsTableSuffix();
            if (Objects.requireNonNull(RedisMessenger.this.minePermsManager.getStorageType()) == StorageType.FILE) {
                throw new UnsupportedOperationException("Don't update data by redis pub-sub, because used FileStorage! You must change field 'useRedisPubSub' to 'false' in plugin config!");
            }
            if (messageData.getMessageType().isUserUpdate()) {
                val userId = messageData.getEntityId();
                User user = null;
                User temporalUser = temporalUserCache.getIfPresent(userId);
                if (temporalUser != null) user = temporalUser;
                User cachedUser = userCache.get(userId);
                if (cachedUser != null) user = cachedUser;
                if (user == null) return;
                switch (messageData.getMessageType()) {
                    case USER_PREFIX_UPDATE: {
                        val prefix = messageData.getObject();
                        user.setPrefix(prefix);
                        break;
                    }
                    case USER_SUFFIX_UPDATE: {
                        val suffix = messageData.getObject();
                        user.setSuffix(suffix);
                        break;
                    }
                    case USER_GROUP_UPDATE: {
                        val groupId = messageData.getObject();
                        user.setGroupId(groupId);
                        user.recalculatePermissions(groupsCache);
                        RedisMessenger.this.minePermsManager.getEventManager().callGroupChangeEvent(user);
                        break;
                    }
                    case USER_PERMISSION_ADD: {
                        val permission = messageData.getObject();
                        user.addPermission(permission);
                        user.recalculatePermissions(groupsCache);
                        break;
                    }
                    case USER_PERMISSION_REMOVE: {
                        val permission = messageData.getObject();
                        user.removePermission(permission);
                        user.recalculatePermissions(groupsCache);
                        break;
                    }
                    case USER_DELETE: {
                        if (userCache.get(userId) != null) {
                            userCache.put(userId, new User(userId, defaultGroupId));
                            break;
                        }
                        if (temporalUserCache.getIfPresent(userId) != null) {
                            temporalUserCache.put(userId, new User(userId, defaultGroupId));
                        }
                        break;
                    }
                }
            }
            if (!messageData.getMessageType().isUserUpdate()) {
                val groupId = messageData.getEntityId();
                Group group = groupsCache.get(groupId);
                switch (messageData.getMessageType()) {
                    case GROUP_PREFIX_UPDATE: {
                        val prefix = messageData.getObject();
                        if (group != null) {
                            group.setPrefix(prefix);
                        }
                        break;
                    }
                    case GROUP_SUFFIX_UPDATE: {
                        val suffix = messageData.getObject();
                        if (group != null) {
                            group.setSuffix(suffix);
                        }
                        break;
                    }
                    case GROUP_PARENT_ADD: {
                        val parent = messageData.getObject();
                        if (group != null) {
                            group.addInheritanceGroup(parent);
                            storage.recalculateUsersPermissions();
                        }
                        break;
                    }
                    case GROUP_PARENT_REMOVE: {
                        val parent = messageData.getObject();
                        if (group != null) {
                            group.removeInheritanceGroup(parent);
                            storage.recalculateUsersPermissions();
                        }
                        break;
                    }
                    case GROUP_PERMISSION_ADD: {
                        if (!subChannel.equalsIgnoreCase(messageData.getSubChannel())) return;
                        val permission = messageData.getObject();
                        if (group != null) {
                            group.addPermission(permission);
                            storage.recalculateUsersPermissions();
                        }
                        break;
                    }
                    case GROUP_PERMISSION_REMOVE: {
                        if (!subChannel.equalsIgnoreCase(messageData.getSubChannel())) return;
                        val permission = messageData.getObject();
                        if (group != null) {
                            group.removePermission(permission);
                            storage.recalculateUsersPermissions();
                        }
                        break;
                    }
                    case GROUP_CREATE: {
                        if (groupsCache.get(groupId) == null) groupsCache.put(groupId, new Group(groupId));
                        break;
                    }
                    case GROUP_DELETE: {
                        groupsCache.remove(groupId);
                        Stream.concat(storage.getUsers().values().stream(), storage.getTemporalUsersCache().asMap().values().stream())
                                .filter(user -> user.hasGroup(groupId)).forEach(user -> {
                                    user.setGroupId(defaultGroupId);
                                    RedisMessenger.this.minePermsManager.getEventManager().callGroupChangeEvent(user);
                                });
                        groupsCache.values().stream().filter(cachedGroup -> cachedGroup.hasGroup(groupId)).forEach(cachedGroup -> cachedGroup.removeInheritanceGroup(groupId));
                        storage.recalculateUsersPermissions();
                        break;
                    }
                }
            }
        }
    }
}
