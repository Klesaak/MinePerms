package ua.klesaak.mineperms.manager.storage.redis;

import lombok.val;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool implements AutoCloseable {
    private final JedisPool pool;

    public RedisPool(String host, int port, String pass) {
        val jpc = new JedisPoolConfig();
        //jpc.setLifo(false);
        jpc.setTestOnBorrow(true);
        jpc.setMinIdle(3);
        jpc.setMaxTotal(500);
        this.pool = new JedisPool(jpc, host, port, 30000, pass == null || pass.isEmpty() ? null : pass);
    }

    public Jedis getRedis() {
        return pool.getResource();
    }

    @Override
    public void close() {
        pool.destroy();
    }
}
