package ua.klesaak.mineperms.manager.storage.redis.messenger;

import lombok.val;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;
import ua.klesaak.mineperms.manager.storage.mysql.MySQLStorage;
import ua.klesaak.mineperms.manager.storage.redis.RedisConfig;
import ua.klesaak.mineperms.manager.storage.redis.RedisPool;
import ua.klesaak.mineperms.manager.storage.redis.RedisStorage;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RedisMessenger {
    public static UUID SERVER_UUID = UUID.randomUUID();
    private final Storage storage;
    private final RedisPool redisPool;
    private final Subscription sub;
    private boolean closing = false;

    public RedisMessenger(Storage storage, RedisPool redisPool) {
        this.storage = storage;
        this.redisPool = redisPool;
        this.sub = new Subscription();
        CompletableFuture.runAsync(this.sub);
    }

    public void sendOutgoingMessage(MessageData messageData) {
        try (Jedis jedis = this.redisPool.getRedis()) {
            messageData.setUuid(SERVER_UUID);
            jedis.publish(RedisConfig.UPDATE_CHANNEL_NAME, JsonData.GSON.toJson(messageData));
        } catch (Exception e) {
            throw new RuntimeException("Error while publish message ", e);
        }
    }


    public void close() {
        this.closing = true;
        this.sub.unsubscribe();
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
                        System.out.println("[MinePerms]: Redis pub-sub connection re-established");
                    }

                    jedis.subscribe(this, RedisConfig.UPDATE_CHANNEL_NAME); // blocking call
                } catch (Exception e) {
                    if (RedisMessenger.this.closing) {
                        return;
                    }

                    System.out.println("[MinePerms]: Redis pub-sub connection dropped, trying to re-open the connection " + e);
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
            val messageData = JsonData.GSON.fromJson(msg, MessageData.class);
            if (messageData.getUuid().equals(SERVER_UUID)) return;
            switch (messageData.getMessageType()) {
                case USER_UPDATE: {
                    val user = JsonData.GSON.fromJson(messageData.getObject(), User.class);
                    RedisMessenger.this.storage.updateUser(user.getPlayerName(), user);
                    break;
                }
                case USER_DELETE: {
                    val userName = messageData.getObject();
                    val storage = RedisMessenger.this.storage;
                    if (storage instanceof RedisStorage) {
                        storage.getUsers().remove(userName);
                        ((RedisStorage)storage).getTemporalUsersCache().invalidate(userName);
                        break;
                    }
                    if (storage instanceof MySQLStorage) {
                        storage.getUsers().remove(userName);
                        ((MySQLStorage)storage).getTemporalUsersCache().invalidate(userName);
                    }
                    break;
                }
                case GROUP_UPDATE: {
                    val group = JsonData.GSON.fromJson(messageData.getObject(), Group.class);
                    RedisMessenger.this.storage.updateGroup(group.getGroupID(), group);
                    break;
                }
                case GROUP_DELETE: {
                    val groupID = messageData.getObject();
                    RedisMessenger.this.storage.getGroups().remove(groupID);
                    RedisMessenger.this.storage.recalculateUsersPermissions();
                    break;
                }
            }
        }
    }
}
