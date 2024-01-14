package ua.klesaak.mineperms.manager.storage.file;

import com.google.gson.reflect.TypeToken;
import lombok.Synchronized;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.log.MPLogger;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.entity.User;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public final class FileStorage extends Storage {
    private static final TypeToken<Collection<Group>> GROUP_TOKEN = new TypeToken<Collection<Group>>() {};
    private static final TypeToken<Collection<User>> USER_TOKEN = new TypeToken<Collection<User>>() {};
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
        if (this.groupsFile.getFile().length() <= 0L) {
            val defaultGroup = new Group(configFile.getDefaultGroup());
            this.groups.put(defaultGroup.getGroupId(), defaultGroup);
            this.groupsFile.write(Collections.singletonList(defaultGroup), true);
        }
        Collection<Group> groupDataCollection = this.groupsFile.readAll(GROUP_TOKEN);
        groupDataCollection.forEach(group -> this.groups.put(group.getGroupId(), group));
        if (this.usersFile.getFile().length() > 0L) {
            Collection<User> dataCollection = this.usersFile.readAll(USER_TOKEN);
            dataCollection.forEach(user -> {
                user.recalculatePermissions(this.groups);
                this.users.put(user.getPlayerName().toLowerCase(), user);
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
            MPLogger.logError(new RuntimeException("Error while save users.json file", throwable));
            return null;
        });
    }

    @Override
    public void saveUser(String nickName, User user) {
        val nickNameLC = nickName.toLowerCase();
        this.users.put(nickNameLC, user);
        this.saveUser(nickNameLC);
    }

    @Override @Synchronized
    public void saveGroup(String groupID) {
        CompletableFuture.runAsync(()->this.groupsFile.write(this.groups.values(), true)).exceptionally(throwable -> {
            throw new RuntimeException("Error while save groups.json file", throwable);
        });
    }

    public User getUser(String nickName) {
        return this.users.get(nickName.toLowerCase());
    }

    @Override
    public User getCachedUser(String nickName) {
        throw new UnsupportedOperationException("Method getCachedUser don't supported in FileStorage");
    }

    @Override
    public String getUserPrefix(String nickName) {
        User user = this.getUser(nickName.toLowerCase());
        if (user == null) return this.getDefaultGroup().getPrefix();
        return user.getPrefix().isEmpty() ? this.getGroup(user.getGroupId()).getPrefix() : user.getPrefix();
    }

    @Override
    public String getUserSuffix(String nickName) {
        User user = this.getUser(nickName.toLowerCase());
        if (user == null) return this.getDefaultGroup().getSuffix();
        return user.getSuffix().isEmpty() ? this.getGroup(user.getGroupId()).getSuffix() : user.getSuffix();
    }

    @Override
    public void addUserPermission(String nickName, String permission) {
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user == null) {
            user = new User(nickNameLC, this.getDefaultGroup().getGroupId());
        }
        user.addPermission(permission);
        this.saveUser(nickNameLC, user);
    }

    @Override
    public void removeUserPermission(String nickName, String permission) {
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user == null) return;
        user.removePermission(permission);
        user.recalculatePermissions(this.groups);
        this.saveUser(nickNameLC, user);
    }

    @Override
    public void setUserPrefix(String nickName, String prefix) {
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user == null) {
            user = new User(nickNameLC, this.getDefaultGroup().getGroupId());
        }
        user.setPrefix(prefix);
        this.saveUser(nickNameLC, user);
    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user == null) {
            user = new User(nickNameLC, this.getDefaultGroup().getGroupId());
        }
        user.setSuffix(suffix);
        this.saveUser(nickNameLC, user);
    }

    @Override
    public void setUserGroup(String nickName, String groupID) {
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user == null) {
            user = new User(nickNameLC, this.getDefaultGroup().getGroupId());
        }
        if (this.groups.get(groupID) != null) {
            user.setGroupId(groupID);
            this.saveUser(nickNameLC, user);
            this.manager.getEventManager().callGroupChangeEvent(user);
            user.recalculatePermissions(this.groups);
        }
    }

    @Override
    public void deleteUser(String nickName) {
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user != null) {
            this.users.remove(nickNameLC);
            this.saveUser(nickNameLC);
        }
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

    @Synchronized
    @Override
    public void deleteGroup(String groupID) {
        val defaultGroupId = this.getDefaultGroup().getGroupId();
        if (groupID.equalsIgnoreCase(defaultGroupId)) return;
        this.groups.remove(groupID.toLowerCase());
        this.users.values().stream().filter(user -> user.hasGroup(groupID)).forEach(user -> {
            user.setGroupId(defaultGroupId);
            this.manager.getEventManager().callGroupChangeEvent(user);
        });
        this.groups.values().stream().filter(group -> group.hasGroup(groupID)).forEach(group -> group.removeInheritanceGroup(groupID));
        this.recalculateUsersPermissions();
        this.saveUser(null);
        this.saveGroup(groupID);
    }

    @Override
    public void createGroup(String groupID) {
        this.groups.put(groupID.toLowerCase(), new Group(groupID));
        this.saveGroup(groupID);
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
            MPLogger.logError(new RuntimeException("Error while importing users data", throwable));
            return null;
        });
    }

    @Override
    public void importGroupsData(Collection<Group> groups) {
        for (Group group : groups) this.groups.put(group.getGroupId(), group);
        CompletableFuture.runAsync(()->this.groupsFile.write(this.groups.values(), true)).exceptionally(throwable -> {
            MPLogger.logError(new RuntimeException("Error while importing groups data", throwable));
            return null;
        });
    }

    @Override
    public void close() {
        this.saveGroup(null);
        this.saveUser(null);
    }
}
