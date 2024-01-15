package ua.klesaak.mineperms.manager.utils.cache;

import lombok.val;
import ua.klesaak.mineperms.manager.log.MPLogger;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class ScheduledCache<K, V> implements AutoCloseable {
    private final Map<K, Pair<Long, V>> data = new ConcurrentHashMap<>();
    private final BiConsumer<K, V> afterRemoving;
    private final BiPredicate<K, V> cancelRemovingIf;
    private final long expireTime;
    private ScheduledExecutorService executorService;

    public ScheduledCache(Builder<K, V> builder) {
        this.afterRemoving = builder.afterRemoving;
        this.cancelRemovingIf = builder.cancelRemovingIf;
        this.expireTime = builder.expireTime;
        long clearExpiredInterval = builder.clearExpiredInterval;
        if (clearExpiredInterval > 0) {
            this.executorService = new ScheduledThreadPoolExecutor(2, runnable -> {
                Thread schedulerThread = new Thread(runnable, "ScheduledCache Thread");
                schedulerThread.setDaemon(false);
                return schedulerThread;
            });
            this.executorService.scheduleWithFixedDelay(this::invalidateData, clearExpiredInterval, clearExpiredInterval, TimeUnit.MINUTES);
        }
    }

    public void clear() {
        for (K key : this.data.keySet()) {
            this.invalidate(key);
        }
    }

    public void invalidate(K key) {
        val data = this.data.remove(key);
        if (data != null) this.afterRemoving.accept(key, data.getValue());
    }

    public void put(K key, V value) {
        this.invalidateData();
        this.data.put(key, Pair.of(System.nanoTime() + this.expireTime, value));
    }

    public void putIfAbsent(K key, V value) {
        this.invalidateData();
        this.data.putIfAbsent(key, Pair.of(System.nanoTime() + this.expireTime, value));
    }

    public boolean contains(K key) {
        this.invalidateData();
        return this.data.containsKey(key);
    }

    public V getIfPresent(K key) {
        val data = this.data.get(key);
        if (data != null && data.getKey() > System.nanoTime()) return data.getValue();
        return null;
    }

    private void invalidateData() {
        for (Map.Entry<K, Pair<Long, V>> obj : this.data.entrySet()) {
            K key = obj.getKey();
            V value = obj.getValue().getValue();
            long expireTime = obj.getValue().getKey();
            if (expireTime < System.nanoTime() && !this.cancelRemovingIf.test(key, value)) {
                this.data.remove(key);
                this.afterRemoving.accept(key, value);
            }
        }
    }

    public int size() {
        return this.data.size();
    }

    public ConcurrentHashMap<K, V> asMap() {
        val map = new ConcurrentHashMap<K, V>(this.data.size());
        for (Map.Entry<K, Pair<Long, V>> obj : this.data.entrySet()) {
            K key = obj.getKey();
            V value = obj.getValue().getValue();
            long expireTime = obj.getValue().getKey();
            if (expireTime > System.nanoTime()) {
                map.put(key, value);
            }
        }
        return map;
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    @Override
    public void close() {
        if (this.executorService == null) return;
        this.executorService.shutdown();
        while (!this.executorService.isTerminated()) {
            try {
                this.executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                MPLogger.logError(exception);
            }
        }
    }

    public static class Builder<K, V> {
        private BiConsumer<K, V> afterRemoving;
        private BiPredicate<K, V> cancelRemovingIf;
        private long expireTime;
        private long clearExpiredInterval;

        public Builder() {
            this.afterRemoving = ((k, v) -> {});
            this.cancelRemovingIf = ((k, v) -> false);
            this.expireTime = TimeUnit.MINUTES.toMillis(10L);
        }

        public Builder<K, V> setAfterRemoving(BiConsumer<K, V> afterRemoving) {
            this.afterRemoving = afterRemoving;
            return this;
        }

        public Builder<K, V> setCancelRemovingIf(BiPredicate<K, V> cancelRemovingIf) {
            this.cancelRemovingIf = cancelRemovingIf;
            return this;
        }

        public Builder<K, V> setExpireTime(Duration duration) {
            this.expireTime = duration.toNanos();
            return this;
        }

        public Builder<K, V> clearExpiredInterval(long minutes) {
            this.clearExpiredInterval = minutes;
            return this;
        }

        public ScheduledCache<K, V> build() {
            return new ScheduledCache<>(this);
        }
    }
}