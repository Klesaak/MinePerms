package ua.klesaak.mineperms.manager.storage.redis;

import lombok.val;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool implements AutoCloseable {
    private final JedisPool pool;

    public RedisPool(RedisConfig redisConfig) {
        val jpc = new JedisPoolConfig();
        //jpc.setLifo(false);
        jpc.setTestOnBorrow(true);
        jpc.setMinIdle(3);
        jpc.setMaxTotal(500);
        this.pool = new JedisPool(jpc, redisConfig.getAddress(), redisConfig.getPort(), 30000, redisConfig.getPassword() == null || redisConfig.getPassword().isEmpty() ? null : redisConfig.getPassword());
    }

    public Jedis getRedis() {
        return this.pool.getResource();
    }

    public JedisPool getJedisPool() {
        return this.pool;
    }

    @Override
    public void close() {
        this.pool.destroy();
    }
}
