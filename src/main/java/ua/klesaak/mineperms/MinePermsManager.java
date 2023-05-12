package ua.klesaak.mineperms;

import lombok.Getter;
import ua.klesaak.mineperms.api.MinePermsAPI;
import ua.klesaak.mineperms.manager.command.MinePermsCommand;
import ua.klesaak.mineperms.manager.config.ConfigFile;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.file.FileStorage;
import ua.klesaak.mineperms.manager.storage.mysql.MySQLStorage;
import ua.klesaak.mineperms.manager.storage.redis.RedisStorage;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.io.File;

@Getter
public final class MinePermsManager {
    public static final String WILDCARD_SUFFIX = ".*";
    public static final String ROOT_WILDCARD = "*";
    public static final String ROOT_WILDCARD_WITH_QUOTES = "'*'";
    public static final String DOT_WILDCARD = ".";
    private final MinePermsCommand minePermsCommand;
    private volatile ConfigFile configFile;
    private volatile Storage storage;

    public MinePermsManager() {
        this.minePermsCommand = new MinePermsCommand(this);
        MinePermsAPI.register(this);
    }

    public void loadConfig(File pluginDataFolder) {
        this.configFile = JsonData.load(new File(pluginDataFolder, "config.json"), ConfigFile.class);
    }

    public void initStorage() {
        switch (this.configFile.getStorageType()) {
            case FILE: {
                this.storage = new FileStorage(this);
                break;
            }
            case MYSQL: {
                this.storage = new MySQLStorage(this);
                break;
            }
            case REDIS: {
                this.storage = new RedisStorage(this);
                break;
            }
            default: {
                this.storage = new FileStorage(this);
            }
        }
    }

    public boolean hasPermission(String nickName, String permission) {
        return this.storage.hasPermission(nickName, permission);
    }

    //todo метод на dump из одной базы в другую

}
