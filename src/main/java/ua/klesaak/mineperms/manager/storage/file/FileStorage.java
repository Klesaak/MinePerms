package ua.klesaak.mineperms.manager.storage.file;

import com.google.gson.reflect.TypeToken;
import lombok.Synchronized;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.entity.User;
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
    }

    public void init() {
        val configFile = this.manager.getConfigFile();
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
        CompletableFuture.runAsync(()->this.usersFile.write(this.users.values(), true)).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void saveUser(String nickName, User user) {
        this.users.put(nickName, user);
        this.saveUser(nickName);
    }

    @Override @Synchronized
    public void saveGroup(String groupID) {
        CompletableFuture.runAsync(()->this.groupsFile.write(this.groups.values(), true)).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    public User getUser(String nickName) {
        return this.users.get(nickName);
    }

    @Override
    public String getUserPrefix(String nickName) {
        User user = this.getUser(nickName);
        if (user == null) return this.getDefaultGroup().getPrefix();
        return user.getPrefix().isEmpty() ? this.getGroup(user.getGroup()).getPrefix() : user.getPrefix();
    }

    @Override
    public String getUserSuffix(String nickName) {
        User user = this.getUser(nickName);
        if (user == null) return this.getDefaultGroup().getSuffix();
        return user.getSuffix().isEmpty() ? this.getGroup(user.getGroup()).getSuffix() : user.getSuffix();
    }

    @Override
    public void addUserPermission(String nickName, String permission) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.getDefaultGroup().getGroupID());
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
            user = new User(nickName, this.getDefaultGroup().getGroupID());
        }
        user.setPrefix(prefix);
        this.saveUser(nickName, user);
    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.getDefaultGroup().getGroupID());
        }
        user.setSuffix(suffix);
        this.saveUser(nickName, user);
    }

    @Override
    public void setUserGroup(String nickName, String groupID) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.getDefaultGroup().getGroupID());
        }
        if (this.groups.get(groupID) != null) {
            user.setGroup(groupID);
            this.saveUser(nickName, user);
            this.manager.getEventManager().callGroupChangeEvent(user);
            user.recalculatePermissions(this.groups);
        }
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
    public void updateUser(String nickName, User user) {
        throw new UnsupportedOperationException("Don't update user by redis pub-sub, because used FileStorage!");
    }

    @Override
    public void addGroupPermission(String groupID, String permission) {
        this.getGroup(groupID).addPermission(permission);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {
        this.getGroup(groupID).removePermission(permission);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void addGroupParent(String groupID, String parentID) {
        this.getGroup(groupID).addInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {
        this.getGroup(groupID).removeInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {
        this.getGroup(groupID).setPrefix(prefix);
        this.saveGroup(groupID);
    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {
        this.getGroup(groupID).setSuffix(suffix);
        this.saveGroup(groupID);
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
    public void updateGroup(String groupID, Group group) {
        throw new UnsupportedOperationException("Don't update group by redis pub-sub, because used FileStorage!");
    }

    @Override
    public Collection<User> getAllUsersData() {
        return Collections.unmodifiableCollection(this.users.values());
    }

    @Override
    public Collection<Group> getAllGroupsData() {
        return Collections.unmodifiableCollection(this.groups.values());
    }

    @Override
    public void importUsersData(Collection<User> users) {
        for (User user : users) this.users.put(user.getPlayerName(), user);
        CompletableFuture.runAsync(()->this.usersFile.write(this.users.values(), true)).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void importGroupsData(Collection<Group> groups) {
        for (Group group : groups) this.groups.put(group.getGroupID(), group);
        CompletableFuture.runAsync(()->this.groupsFile.write(this.groups.values(), true)).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void close() {
        this.saveGroup(null);
        this.saveUser(null);
    }
}
