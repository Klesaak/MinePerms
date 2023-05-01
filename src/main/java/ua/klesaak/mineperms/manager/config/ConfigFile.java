package ua.klesaak.mineperms.manager.config;

import ua.klesaak.mineperms.manager.storage.StorageType;
import ua.klesaak.mineperms.manager.storage.mysql.MySQLConfig;
import ua.klesaak.mineperms.manager.storage.redis.RedisConfig;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.util.Map;

public class ConfigFile extends JsonData {
    private final StorageType storageType;
    private final boolean useRedisPubSub;
    private final String defaultGroup;
    private final Map<String, Object> mySQLSettings;
    private final Map<String, Object> redisSettings;

    public ConfigFile() {
        this.storageType = StorageType.FILE;
        this.defaultGroup = "default";
        this.useRedisPubSub = false;
        this.mySQLSettings = JsonData.mapOf(
                JsonData.pairOf("userName", "mysql"),
                JsonData.pairOf("password", "mysql"),
                JsonData.pairOf("host", "localhost"),
                JsonData.pairOf("port", "3306"),
                JsonData.pairOf("database", "mineperms"),
                JsonData.pairOf("groups_table", "mp_groups"),
                JsonData.pairOf("users_table", "mp_users"),
                JsonData.pairOf("isUseSSL", false)
        );
        this.redisSettings = JsonData.mapOf(
                JsonData.pairOf("database", "0"),
                JsonData.pairOf("password", ""),
                JsonData.pairOf("host", "localhost"),
                JsonData.pairOf("port", "6379"),
                JsonData.pairOf("updateChanel", "mineperms-update"),
                JsonData.pairOf("groups_key", "mp_groups"),
                JsonData.pairOf("users_key", "mp_users")
        );
    }

    public StorageType getStorageType() {
        return this.storageType;
    }

    public String getDefaultGroup() {
        return this.defaultGroup;
    }

    public boolean isUseRedisPubSub() {
        return this.useRedisPubSub;
    }

    public MySQLConfig getMySQLConfig() {
        return new MySQLConfig(
                (String) this.mySQLSettings.get("userName"),
                (String) this.mySQLSettings.get("password"),
                (String) this.mySQLSettings.get("database"),
                (String) this.mySQLSettings.get("host"),
                (String) this.mySQLSettings.get("groups_table"),
                (String) this.mySQLSettings.get("users_table"),
                Integer.parseInt((String) this.mySQLSettings.get("port")),
                (Boolean) this.mySQLSettings.get("isUseSSL")
        );
    }

    public RedisConfig getRedisConfig() {
        return new RedisConfig(
                (String) this.redisSettings.get("host"),
                (String) this.redisSettings.get("password"),
                (String) this.redisSettings.get("updateChanel"),
                (String) this.redisSettings.get("groups_key"),
                (String) this.redisSettings.get("users_key"),
                Integer.parseInt((String)this.redisSettings.get("port")),
                Integer.parseInt((String) this.redisSettings.get("database"))
        );
    }
}
