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
import java.util.concurrent.CompletableFuture;

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
            dataCollection.forEach(group -> this.groups.put(group.getGroupID(), group));
        } else {
            val defaultGroup = new Group(configFile.getDefaultGroup());
            this.groups.put(defaultGroup.getGroupID(), defaultGroup);
            this.groupsFile.write(Collections.singletonList(defaultGroup), true);
        }
        if (this.usersFile.getFile().length() > 0L) {
            Collection<User> dataCollection = this.usersFile.readAll(new TypeToken<Collection<User>>() {});
            dataCollection.forEach(user -> {
                user.recalculatePermissions(this.groups);
                this.users.put(user.getPlayerName(), user);
            });
        }
    }

    @Override
    public void cacheUser(String nickName) {
        //empty
    }

    @Override
    public void unCacheUser(String nickName) {
        //empty
    }

    @Override @Synchronized
    public void saveUser(String nickName) {
        CompletableFuture.runAsync(()->this.usersFile.write(this.users.values(), true));
    }

    @Override
    public void saveUser(String nickName, User user) {
        this.users.put(nickName, user);
        this.saveUser(nickName);
    }

    @Override @Synchronized
    public void saveGroup(String groupID) {
        CompletableFuture.runAsync(()->this.groupsFile.write(this.groups.values(), true));
    }

    public User getUser(String nickName) {
        return this.users.get(nickName);
    }

    @Override
    public String getUserPrefix(String nickName) {
        User user = this.getUser(nickName);
        if (user == null) return this.getGroup(this.manager.getConfigFile().getDefaultGroup()).getPrefix();
        return user.getPrefix().isEmpty() ? "" : this.getGroup(user.getGroup()).getPrefix();
    }

    @Override
    public String getUserSuffix(String nickName) {
        User user = this.getUser(nickName);
        if (user == null) return this.getGroup(this.manager.getConfigFile().getDefaultGroup()).getSuffix();
        return user.getPrefix().isEmpty() ? "" : this.getGroup(user.getGroup()).getSuffix();
    }

    @Override
    public void addUserPermission(String nickName, String permission) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        }
        user.addPermission(permission);
        this.saveUser(nickName, user);
    }

    @Override
    public void removeUserPermission(String nickName, String permission) {
        User user = this.getUser(nickName);
        if (user == null) return;
        user.removePermission(permission);
        this.saveUser(nickName, user);
    }

    @Override
    public void setUserPrefix(String nickName, String prefix) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        }
        user.setPrefix(prefix);
        this.saveUser(nickName, user);
    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        }
        user.setPrefix(suffix);
        this.saveUser(nickName, user);
    }

    @Override
    public void setUserGroup(String nickName, String groupID) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        }
        if (this.groups.get(groupID) != null) {
            user.setGroup(groupID);
            this.saveUser(nickName, user);
        }
        user.recalculatePermissions(this.groups);
    }

    @Override
    public void setUserOption(String nickName, String optionKey, String stringOption) {

    }

    @Override
    public void setUserOption(String nickName, String optionKey, boolean booleanOption) {

    }

    @Override
    public void setUserOption(String nickName, String optionKey, int integerOption) {

    }

    @Override
    public void deleteUser(String nickName) {
        User user = this.getUser(nickName);
        if (user != null) {
            this.users.remove(nickName);
            this.saveUser(nickName);
        }
    }

    @Override
    public void updateUser(String nickName) {
        throw new UnsupportedOperationException("Don't update user, because used FileStorage!");
    }

    @Override
    public void addGroupPermission(String groupID, String permission) {
        this.getGroup(groupID.toLowerCase()).addPermission(permission);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {
        this.getGroup(groupID.toLowerCase()).removePermission(permission);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void addGroupParent(String groupID, String parentID) {
        this.getGroup(groupID.toLowerCase()).addInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {
        this.getGroup(groupID.toLowerCase()).removeInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {
        this.getGroup(groupID.toLowerCase()).setPrefix(prefix);
        this.saveGroup(groupID);
    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {
        this.getGroup(groupID.toLowerCase()).setSuffix(suffix);
        this.saveGroup(groupID);
    }

    @Override
    public void setGroupOption(String groupID, String optionKey, String stringOption) {

    }

    @Override
    public void setGroupOption(String groupID, String optionKey, boolean booleanOption) {

    }

    @Override
    public void setGroupOption(String groupID, String optionKey, int integerOption) {

    }

    @Override
    public void deleteGroup(String groupID) {
        this.groups.remove(groupID.toLowerCase());
        this.saveGroup(groupID);
    }

    @Override
    public void createGroup(String groupID) {
        this.groups.put(groupID.toLowerCase(), new Group(groupID));
        this.saveGroup(groupID);
    }

    @Override
    public void updateGroup(String groupID) {
        throw new UnsupportedOperationException("Don't update group, because used FileStorage!");
    }

    @Override
    public void close() {
        this.saveGroup(null);
        this.saveUser(null);
    }
}
