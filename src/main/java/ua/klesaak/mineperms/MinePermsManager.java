package ua.klesaak.mineperms;

import lombok.Getter;
import ua.klesaak.mineperms.api.MinePermsAPI;
import ua.klesaak.mineperms.manager.command.MinePermsCommand;
import ua.klesaak.mineperms.manager.config.ConfigFile;
import ua.klesaak.mineperms.manager.event.IMPEventManager;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.StorageType;
import ua.klesaak.mineperms.manager.storage.file.FileStorage;
import ua.klesaak.mineperms.manager.storage.sql.SQLStorage;
import ua.klesaak.mineperms.manager.utils.JsonData;
import ua.klesaak.mineperms.manager.utils.Platform;

import java.io.File;

@Getter
public final class MinePermsManager {
    private final MinePermsCommand minePermsCommand;
    private IMPEventManager eventManager;
    private volatile ConfigFile configFile;
    private volatile Storage storage;
    private final Platform platform;
    private StorageType storageType;

    public MinePermsManager(Platform platform) {
        this.platform = platform;
        this.minePermsCommand = new MinePermsCommand(this);
    }

    public void init(File pluginDataFolder, IMPEventManager eventManager) {
        this.configFile = JsonData.load(new File(pluginDataFolder, "config.json"), ConfigFile.class);
        this.storageType = this.configFile.getStorageType();
        switch (this.storageType) {
            case FILE: {
                this.storage = new FileStorage(this);
                break;
            }
            case POSTGRESQL:
            case MARIADB:
            case MYSQL: {
                this.storage = new SQLStorage(this, this.storageType);
                break;
            }
        }
        this.storage.init();
        this.eventManager = eventManager;
        MinePermsAPI.register(this.storage);
    }
}
