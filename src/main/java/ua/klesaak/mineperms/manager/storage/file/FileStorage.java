package ua.klesaak.mineperms.manager.storage.file;

import com.google.gson.reflect.TypeToken;
import lombok.Synchronized;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class FileStorage extends Storage {
    private final JsonData groupsFile, usersFile;

    public FileStorage(MinePermsManager manager) {
        super(manager);
        val configFile = manager.getConfigFile();
        val pluginDataFolder = configFile.getPluginDataFolder();
        this.groupsFile = new JsonData(new File(pluginDataFolder, "groups.json"));
        this.usersFile = new JsonData(new File(pluginDataFolder, "users.json"));

        if (this.groupsFile.getFile().length() > 0L) {
            Collection<Group> dataCollection = this.groupsFile.readAll(new TypeToken<Collection<Group>>() {});
            dataCollection.forEach(group -> this.groups.put(group.getName(), group));
        } else {
            val defaultGroup = new Group(configFile.getDefaultGroup());
            this.groups.put(defaultGroup.getName(), defaultGroup);
            this.groupsFile.write(Collections.singletonList(defaultGroup), true);
        }
        if (this.usersFile.getFile().length() > 0L) {
            Collection<User> dataCollection = this.usersFile.readAll(new TypeToken<Collection<User>>() {});
            dataCollection.forEach(user -> {
                user.recalculatePermissions();
                this.users.put(user.getUserUUID(), user);
            });
        }
    }

    @Override
    public void cacheUser(UUID userID) {
        //empty
    }

    @Override
    public void unCacheUser(UUID userID) {
        //empty
    }

    @Override @Synchronized
    public void saveUser(UUID userID) {
        this.usersFile.write(this.users.values(), true);
    }

    @Override @Synchronized
    public void saveGroup(String groupID) {
        this.groupsFile.write(this.groups.values(), true);
    }

    @Override
    public void close() {
        this.saveGroup(null);
        this.saveUser(null);
    }
}
