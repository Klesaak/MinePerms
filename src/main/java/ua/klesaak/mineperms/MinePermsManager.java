package ua.klesaak.mineperms;

import lombok.Getter;
import ua.klesaak.mineperms.api.MinePermsAPI;
import ua.klesaak.mineperms.manager.command.MinePermsCommand;
import ua.klesaak.mineperms.manager.config.ConfigFile;
import ua.klesaak.mineperms.manager.event.IMPEventManager;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.file.FileStorage;
import ua.klesaak.mineperms.manager.storage.mysql.MySQLStorage;
import ua.klesaak.mineperms.manager.storage.redis.RedisStorage;
import ua.klesaak.mineperms.manager.utils.JsonData;
import ua.klesaak.mineperms.manager.utils.Platform;

import java.io.File;

@Getter
public final class MinePermsManager {
    public static final String WILDCARD_SUFFIX = ".*";
    public static final String ROOT_WILDCARD = "*";
    public static final String ROOT_WILDCARD_WITH_QUOTES = "'*'";
    public static final String DOT_WILDCARD = ".";
    private final MinePermsCommand minePermsCommand;
    private IMPEventManager eventManager;
    private volatile ConfigFile configFile;
    private volatile Storage storage;
    private final Platform platform;

    public MinePermsManager(Platform platform) {
        this.platform = platform;
        this.minePermsCommand = new MinePermsCommand(this);
        MinePermsAPI.register(this.storage);
    }

    public void loadConfig(File pluginDataFolder) {
        this.configFile = JsonData.load(new File(pluginDataFolder, "config.json"), ConfigFile.class);
    }

    public void registerEventsManager(IMPEventManager eventManager) {
        this.eventManager = eventManager;
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
        this.storage.init();
    }

    public boolean hasPermission(String nickName, String permission) {
        return this.storage.hasPermission(nickName, permission);
    }
}
