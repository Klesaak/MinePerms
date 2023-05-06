package ua.klesaak.mineperms.manager.storage.mysql;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.j256.ormlite.dao.Dao;
import org.bukkit.Bukkit;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MySQLStorage extends Storage {
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(3);
    private final Cache<UUID, User> temporalUsersCache = CacheBuilder.newBuilder()
            .initialCapacity(Bukkit.getMaxPlayers())
            .concurrencyLevel(16)
            .expireAfterWrite(1, TimeUnit.MINUTES).build(); //Временный кеш, чтобы уменьшить кол-во запросов в бд.
    private Dao<User, String> userDataDao;
    private Dao<Group, String> groupDataDao;

    public MySQLStorage(MinePermsManager manager) {
        super(manager);
    }

    @Override
    public void cacheUser(UUID userID) {
        User user = this.temporalUsersCache.getIfPresent(userID);
        if (user != null) {
            this.users.put(userID, user);
            this.temporalUsersCache.invalidate(userID);
        }
        //todo query
    }

    /**
     *
     * При выходе игрока с сервера отгружаем его из основного кеша во временный
     * чтобы в случае быстрого перезахода игрока не тратить лишние ресурсы на его подгрузку из БД
     */
    @Override
    public void unCacheUser(UUID userID) {
        User user = this.users.remove(userID);
        this.temporalUsersCache.put(userID, user);
    }

    @Override
    public void saveUser(UUID userID) {

    }

    @Override
    public void saveGroup(String groupID) {

    }

    @Override
    public void updateUser(UUID userID) {

    }

    @Override
    public void close() {

    }
}
