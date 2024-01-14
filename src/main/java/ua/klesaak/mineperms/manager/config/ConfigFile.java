package ua.klesaak.mineperms.manager.config;

import lombok.Getter;
import ua.klesaak.mineperms.manager.storage.StorageType;
import ua.klesaak.mineperms.manager.storage.sql.SQLConfig;
import ua.klesaak.mineperms.manager.storage.redismessenger.RedisConfig;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.io.File;

@Getter
public class ConfigFile extends JsonData {
    private final String storageType;
    private final boolean useRedisPubSub;
    private final String defaultGroup;
    private final SQLConfig SQLSettings;
    private final RedisConfig RedisSettings;

    public ConfigFile() {
        this.storageType = "file";
        this.defaultGroup = "default";
        this.useRedisPubSub = false;
        this.SQLSettings = new SQLConfig("root", "root", "mineperms-db", "localhost", "survival",  3306, false);
        this.RedisSettings = new RedisConfig("localhost", "", 6379);
    }

    public File getPluginDataFolder() {
        return this.getFile().getParentFile();
    }

    public StorageType getStorageType() {
        return StorageType.parse(this.storageType, StorageType.FILE);
    }
}
