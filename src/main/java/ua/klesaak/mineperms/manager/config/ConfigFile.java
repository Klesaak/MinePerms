package ua.klesaak.mineperms.manager.config;

import lombok.Getter;
import ua.klesaak.mineperms.manager.storage.StorageType;
import ua.klesaak.mineperms.manager.storage.file.FileStorageConfig;
import ua.klesaak.mineperms.manager.storage.mysql.MySQLConfig;
import ua.klesaak.mineperms.manager.storage.redis.RedisConfig;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

@Getter
public class ConfigFile extends JsonData {
    private final StorageType storageType;
    private Map<String, Object> fileSettings;
    private Map<String, Object> MySQLSettings;
    private Map<String, Object> RedisLSettings;
    private final String defaultGroup;

    public ConfigFile(Path directoryPath) {
        super(new File(directoryPath.toString()));
        this.storageType = StorageType.FILE;
        this.defaultGroup = null;//todo
    }

    public void writeDefaults() {

    }

    public FileStorageConfig getFileStorageConfig() {
        return null; //todo
    }

    public MySQLConfig getMySQLConfig() {
        return null; //todo
    }

    public RedisConfig getRedisConfig() {
        return null; //todo
    }
}
