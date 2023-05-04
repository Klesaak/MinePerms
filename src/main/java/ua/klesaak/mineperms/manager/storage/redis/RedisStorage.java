package ua.klesaak.mineperms.manager.storage.redis;


import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Storage;

import java.util.UUID;

public class RedisStorage extends Storage {


    public RedisStorage(MinePermsManager manager) {
        super(manager);
    }

    @Override
    public void cacheUser(UUID userID) {

    }

    @Override
    public void unCacheUser(UUID userID) {

    }

    @Override
    public void saveUser(UUID userID) {

    }

    @Override
    public void saveGroup(String groupID) {

    }

    @Override
    public void close() {

    }
}
