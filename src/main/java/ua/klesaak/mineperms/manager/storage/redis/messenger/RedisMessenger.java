package ua.klesaak.mineperms.manager.storage.redis.messenger;

import lombok.val;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;
import ua.klesaak.mineperms.manager.storage.redis.RedisConfig;
import ua.klesaak.mineperms.manager.storage.redis.RedisPool;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.util.concurrent.CompletableFuture;

public class RedisMessenger {
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

    public void sendOutgoingMessage(String channel, MessageData messageData) {
        try (Jedis jedis = this.redisPool.getRedis()) {
            jedis.publish(channel, JsonData.GSON.toJson(messageData));
        } catch (Exception e) {
            e.printStackTrace();
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
            switch (messageData.getMessageType()) {
                case USER_UPDATE: {
                    val user = JsonData.GSON.fromJson(messageData.getObject(), User.class);
                    RedisMessenger.this.storage.updateUser(user.getPlayerName(), user);
                    break;
                }
                case USER_DELETE: {
                    val userName = messageData.getObject();
                    RedisMessenger.this.storage.deleteUser(userName);
                    break;
                }
                case GROUP_UPDATE: {
                    val group = JsonData.GSON.fromJson(messageData.getObject(), Group.class);
                    RedisMessenger.this.storage.updateGroup(group.getGroupID(), group);
                    break;
                }
                case GROUP_DELETE: {
                    val groupID = messageData.getObject();
                    RedisMessenger.this.storage.deleteGroup(groupID);
                    break;
                }
            }
        }
    }
}
