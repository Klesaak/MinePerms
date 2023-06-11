package ua.klesaak.mineperms.manager.storage.redis.messenger;

import lombok.val;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.entity.User;
import ua.klesaak.mineperms.manager.storage.redis.RedisConfig;
import ua.klesaak.mineperms.manager.storage.redis.RedisPool;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RedisMessenger implements AutoCloseable {
    public static UUID SERVER_UUID = UUID.randomUUID(); //Уникальный идентификатор сервера для корректного обмена сообщениями через Redis-pub-sub
    private final MinePermsManager minePermsManager;
    private final Storage storage;
    private final RedisPool redisPool;
    private final Subscription sub;
    private boolean closing = false;

    public RedisMessenger(MinePermsManager minePermsManager, Storage storage) {
        this.minePermsManager = minePermsManager;
        this.storage = storage;
        this.redisPool = new RedisPool(minePermsManager.getConfigFile().getRedisSettings());
        this.sub = new Subscription();
        CompletableFuture.runAsync(this.sub);
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
    }

    private class Subscription extends JedisPubSub implements Runnable {

        @Override
        public void run() {
            boolean first = true;
            while (!RedisMessenger.this.closing && !Thread.interrupted() && !RedisMessenger.this.redisPool.getJedisPool().isClosed()) {
                try (Jedis jedis = RedisMessenger.this.redisPool.getJedisPool().getResource()) {
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
            String usersSubChannel = "";
            String groupsSubChannel = "";
            switch (config.getStorageType()) {
                case FILE: {
                    throw new UnsupportedOperationException("Don't update data by redis pub-sub, because used FileStorage! You must change field 'useRedisPubSub' to 'false' in plugin config!");
                }
                case REDIS: {
                    val redisConfig = config.getRedisSettings();
                    usersSubChannel = redisConfig.getUsersKey();
                    groupsSubChannel = redisConfig.getGroupsKey();
                    break;
                }
                case MYSQL: {
                    val mysqlConfig = config.getMySQLSettings();
                    usersSubChannel = mysqlConfig.getUsersTable();
                    groupsSubChannel = mysqlConfig.getGroupsTable();
                    break;
                }
            }
            switch (messageData.getMessageType()) {
                case USER_UPDATE: {
                    if (!usersSubChannel.equalsIgnoreCase(messageData.getSubChannel())) return;
                    val user = messageData.getUserObject();
                    storage.updateUser(user.getPlayerName(), user);
                    break;
                }
                case USER_DELETE: {
                    if (!usersSubChannel.equalsIgnoreCase(messageData.getSubChannel())) return;
                    val userName = messageData.getStringObject();
                    if (storage.getUsers().get(userName) != null) {
                        storage.getUsers().put(userName, new User(userName, storage.getDefaultGroup().getGroupID()));
                        break;

                    }
                    if (storage.getTemporalUsersCache().getIfPresent(userName) != null) {
                        storage.getTemporalUsersCache().put(userName, new User(userName, storage.getDefaultGroup().getGroupID()));
                    }
                    break;
                }
                case GROUP_UPDATE: {
                    if (!groupsSubChannel.equalsIgnoreCase(messageData.getSubChannel())) return;
                    val group = messageData.getGroupObject();
                    storage.updateGroup(group.getGroupID(), group);
                    break;
                }
                case GROUP_DELETE: {
                    if (!groupsSubChannel.equalsIgnoreCase(messageData.getSubChannel())) return;
                    val groupID = messageData.getStringObject();
                    storage.getGroups().remove(groupID);
                    storage.recalculateUsersPermissions();
                    break;
                }
            }
        }
    }
}
