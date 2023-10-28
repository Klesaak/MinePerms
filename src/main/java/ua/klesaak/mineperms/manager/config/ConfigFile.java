package ua.klesaak.mineperms.manager.config;

import lombok.Getter;
import ua.klesaak.mineperms.manager.storage.mysql.MySQLConfig;
import ua.klesaak.mineperms.manager.storage.redis.RedisConfig;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.io.File;

@Getter
public class ConfigFile extends JsonData {
    private final StorageType storageType;
    private final boolean useRedisPubSub;
    private final String defaultGroup;
    private final MySQLConfig MySQLSettings;
    private final RedisConfig RedisSettings;

    public ConfigFile() {
        this.storageType = StorageType.FILE;
        this.defaultGroup = "default";
        this.useRedisPubSub = false;
        this.MySQLSettings = new MySQLConfig("mysql","root", "root", "mineperms", "localhost", "mp_users", "mp_groups", 3306, false);
        this.RedisSettings = new RedisConfig("localhost", "", "mp_groups", "mp_users", 6379,0);
    }

    public File getPluginDataFolder() {
        return this.getFile().getParentFile();
    }
}
