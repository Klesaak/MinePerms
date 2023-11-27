package ua.klesaak.mineperms.manager.storage.mysql;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.entity.User;
import ua.klesaak.mineperms.manager.storage.entity.data.GroupData;
import ua.klesaak.mineperms.manager.storage.entity.data.UserData;
import ua.klesaak.mineperms.manager.storage.redis.messenger.MessageData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ua.klesaak.mineperms.manager.storage.mysql.DatabaseConstants.*;

@Getter
public class MySQLStorage extends Storage {
    private final JdbcPooledConnectionSource connectionSource;
    private Dao<UserData, String> userDataDao;
    private Dao<GroupData, String> groupDataDao;

    public MySQLStorage(MinePermsManager manager) {
        super(manager);
        val config = this.manager.getConfigFile().getMySQLSettings();
        try {
            this.connectionSource = new JdbcPooledConnectionSource(config.getHost(), config.getUsername(), config.getPassword());
        } catch (SQLException ex) {
            throw new RuntimeException("Error while init MySQL, check your connection settings in config.json", ex);
        }
        this.createUsersTable(config);
        this.createGroupsTable(config);
        this.connectionSource.setTestBeforeGet(true);
    }

    @Override
    public void init() {
        CompletableFuture.runAsync(() -> {
            try {
                val allData = this.groupDataDao.queryForAll();
                allData.forEach(groupData -> this.groups.put(groupData.getGroupID(), groupData.getGroup()));
                val defaultGroup = manager.getConfigFile().getDefaultGroup();
                if (this.getGroup(defaultGroup) == null) {
                    this.createGroup(defaultGroup);
                }
            } catch (SQLException ex) {
                throw new RuntimeException("Error while init MySQL", ex);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    private void createUsersTable(MySQLConfig config) {
        try {
            List<DatabaseFieldConfig> usersFieldConfigs = new ArrayList<>();
            DatabaseFieldConfig playerName = new DatabaseFieldConfig("playerName");
            playerName.setId(true);
            playerName.setCanBeNull(false);
            playerName.setColumnName(USER_NAME_COLUMN);
            usersFieldConfigs.add(playerName);

            DatabaseFieldConfig groupField = new DatabaseFieldConfig("group");
            groupField.setCanBeNull(false);
            groupField.setDataType(DataType.STRING);
            groupField.setDefaultValue(this.manager.getConfigFile().getDefaultGroup());
            groupField.setColumnName(USER_GROUP_COLUMN);
            usersFieldConfigs.add(groupField);

            DatabaseFieldConfig prefixField = new DatabaseFieldConfig("prefix");
            prefixField.setCanBeNull(false);
            prefixField.setDataType(DataType.LONG_STRING);
            prefixField.setColumnName(PREFIX_COLUMN);
            usersFieldConfigs.add(prefixField);

            DatabaseFieldConfig suffixField = new DatabaseFieldConfig("suffix");
            suffixField.setCanBeNull(false);
            suffixField.setDataType(DataType.LONG_STRING);
            suffixField.setColumnName(SUFFIX_COLUMN);
            usersFieldConfigs.add(suffixField);

            DatabaseFieldConfig permsField = new DatabaseFieldConfig("serializedPerms");
            permsField.setCanBeNull(false);
            permsField.setDataType(DataType.LONG_STRING);
            permsField.setColumnName(PERMISSIONS_COLUMN);
            usersFieldConfigs.add(permsField);

            DatabaseTableConfig<UserData> usersTableConfig = new DatabaseTableConfig<>(UserData.class, config.getUsersTable(), usersFieldConfigs);
            this.userDataDao = DaoManager.createDao(this.connectionSource, usersTableConfig);
            TableUtils.createTableIfNotExists(this.connectionSource, usersTableConfig);
        } catch (SQLException ex) {
            throw new RuntimeException("Error while create users table", ex);
        }
    }



    private void createGroupsTable(MySQLConfig config) {
        try {
            List<DatabaseFieldConfig> groupsFiledConfigs = new ArrayList<>();
            DatabaseFieldConfig groupID = new DatabaseFieldConfig("groupID");
            groupID.setId(true);
            groupID.setCanBeNull(false);
            groupID.setColumnName(GROUP_ID_COLUMN);
            groupsFiledConfigs.add(groupID);

            DatabaseFieldConfig prefixField = new DatabaseFieldConfig("prefix");
            prefixField.setCanBeNull(false);
            prefixField.setDataType(DataType.LONG_STRING);
            prefixField.setColumnName(PREFIX_COLUMN);
            groupsFiledConfigs.add(prefixField);

            DatabaseFieldConfig suffixField = new DatabaseFieldConfig("suffix");
            suffixField.setCanBeNull(false);
            suffixField.setDataType(DataType.LONG_STRING);
            suffixField.setColumnName(SUFFIX_COLUMN);
            groupsFiledConfigs.add(suffixField);

            DatabaseFieldConfig parentField = new DatabaseFieldConfig("serializedInheritanceGroups");
            parentField.setCanBeNull(false);
            parentField.setDataType(DataType.LONG_STRING);
            parentField.setColumnName(GROUP_PARENTS_COLUMN);
            groupsFiledConfigs.add(parentField);

            DatabaseFieldConfig permsField = new DatabaseFieldConfig("serializedPerms");
            permsField.setCanBeNull(false);
            permsField.setDataType(DataType.LONG_STRING);
            permsField.setColumnName(PERMISSIONS_COLUMN);
            groupsFiledConfigs.add(permsField);

            DatabaseTableConfig<GroupData> groupsTableConfig = new DatabaseTableConfig<>(GroupData.class, config.getGroupsTable(), groupsFiledConfigs);
            this.groupDataDao = DaoManager.createDao(this.connectionSource, groupsTableConfig);
            TableUtils.createTableIfNotExists(this.connectionSource, groupsTableConfig);
        } catch (SQLException ex) {
            throw new RuntimeException("Error while create groups table", ex);
        }
    }

    @Override
    public void cacheUser(String nickName) {
        val tempUser = this.temporalUsersCache.getIfPresent(nickName);
        User user = tempUser != null ? tempUser : this.getUser(nickName);
        if (!this.manager.getConfigFile().isUseRedisPubSub()) { //загружаем игрока из бд при каждом заходе, чтобы была актуальность данных!
            user = this.loadUser(nickName).join();
        }
        if (user != null) {
            user.recalculatePermissions(this.groups);
            this.users.put(nickName, user);
            this.temporalUsersCache.invalidate(nickName);
            return;
        }
        val newUser = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        newUser.recalculatePermissions(this.groups);
        this.users.put(nickName, newUser);
    }

    /**
     * При выходе игрока с сервера отгружаем его из основного кеша во временный
     * чтобы в случае быстрого перезахода игрока не тратить лишние ресурсы на его подгрузку из БД
     */
    @Override
    public void unCacheUser(String nickName) {
        User user = this.users.remove(nickName);
        this.temporalUsersCache.put(nickName, user);
    }

    @Override
    public void saveUser(String nickName) {
        val tempUser = this.temporalUsersCache.getIfPresent(nickName);
        User user = tempUser != null ? tempUser : this.users.get(nickName);
        if (user != null) {
            this.saveUser(nickName, user);
        }
    }

    @Override
    public void saveUser(String nickName, User user) {
        CompletableFuture.runAsync(()-> {
            try {
                this.userDataDao.createOrUpdate(UserData.from(user));
            } catch (SQLException ex) {
                throw new RuntimeException("Error while save user data " + nickName, ex);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void saveGroup(String groupID) {
        val group = this.groups.get(groupID);
        CompletableFuture.runAsync(()-> {
            try {
                this.groupDataDao.createOrUpdate(GroupData.from(group));
            } catch (SQLException ex) {
                throw new RuntimeException("Error while save group data " + groupID, ex);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public User getUser(String nickName) {
        val tempUser = this.temporalUsersCache.getIfPresent(nickName);
        User user = tempUser != null ? tempUser : this.users.get(nickName);
        if (user != null) return user;
        user = this.loadUser(nickName).join();
        if (user != null) {
            this.temporalUsersCache.put(nickName, user);
            user.recalculatePermissions(this.groups);
        }
        return user;
    }

    private CompletableFuture<User> loadUser(String nickName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UserData userData = this.userDataDao.queryForId(nickName);
                if (userData == null) return null;
                return userData.getUser();
            } catch (SQLException e) {
                throw new RuntimeException("Error while load user " + nickName + " data", e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public String getUserPrefix(String nickName) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        }
        return user.getPrefix().isEmpty() ? this.getGroupOrDefault(user.getGroup()).getPrefix() : user.getPrefix();
    }

    @Override
    public String getUserSuffix(String nickName) {
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, this.manager.getConfigFile().getDefaultGroup());
        }
        return user.getSuffix().isEmpty() ? this.getGroupOrDefault(user.getGroup()).getSuffix() : user.getSuffix();
    }

    @Override
    public void addUserPermission(String nickName, String permission) {
        val config = this.manager.getConfigFile();
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, config.getDefaultGroup());
            this.temporalUsersCache.put(nickName, user);
        }
        user.addPermission(permission);
        this.saveUser(nickName, user);
        this.broadcastPacket(MessageData.goUpdateUserPacket(user, config.getMySQLSettings().getUsersTable()));
    }

    @Override
    public void removeUserPermission(String nickName, String permission) {
        User user = this.getUser(nickName);
        if (user == null) return;
        user.removePermission(permission);
        user.recalculatePermissions(this.groups);
        this.saveUser(nickName, user);
        this.broadcastPacket(MessageData.goUpdateUserPacket(user, this.manager.getConfigFile().getMySQLSettings().getUsersTable()));
    }

    @Override
    public void setUserPrefix(String nickName, String prefix) {
        val config = this.manager.getConfigFile();
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, config.getDefaultGroup());
            this.temporalUsersCache.put(nickName, user);
        }
        user.setPrefix(prefix);
        this.saveUser(nickName, user);
        this.broadcastPacket(MessageData.goUpdateUserPacket(user, config.getMySQLSettings().getUsersTable()));
    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {
        val config = this.manager.getConfigFile();
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, config.getDefaultGroup());
            this.temporalUsersCache.put(nickName, user);
        }
        user.setSuffix(suffix);
        this.saveUser(nickName, user);
        this.broadcastPacket(MessageData.goUpdateUserPacket(user, config.getMySQLSettings().getUsersTable()));
    }

    @Override
    public void setUserGroup(String nickName, String groupID) {
        val config = this.manager.getConfigFile();
        User user = this.getUser(nickName);
        if (user == null) {
            user = new User(nickName, config.getDefaultGroup());
            this.temporalUsersCache.put(nickName, user);
        }
        if (this.groups.get(groupID) != null) {
            user.setGroup(groupID);
            this.saveUser(nickName, user);
            user.recalculatePermissions(this.groups);
            this.manager.getEventManager().callGroupChangeEvent(user);
        }
        this.broadcastPacket(MessageData.goUpdateUserPacket(user, config.getMySQLSettings().getUsersTable()));
    }

    @Override
    public void deleteUser(String nickName) {
        val config = this.manager.getConfigFile();
        val newUser = new User(nickName, config.getDefaultGroup());
        if (this.users.get(nickName) != null) {
            this.users.put(nickName, newUser);
        }
        if (this.temporalUsersCache.getIfPresent(nickName) != null) {
            this.temporalUsersCache.put(nickName, newUser);
        }
        CompletableFuture.runAsync(()-> {
            try {
                this.userDataDao.deleteById(nickName);
                this.broadcastPacket(MessageData.goDeleteUserPacket(nickName, config.getMySQLSettings().getUsersTable()));
            } catch (SQLException e) {
                throw new RuntimeException("Error while delete user " + nickName + " data", e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void updateUser(String nickName, User user) {
        if (this.temporalUsersCache.getIfPresent(nickName) == null && this.users.get(nickName) == null) return;
        user.recalculatePermissions(this.groups);
        if (this.temporalUsersCache.getIfPresent(nickName) != null) {
            this.temporalUsersCache.put(nickName, user);
            return;
        }
        this.users.put(nickName, user);
    }

    @Override
    public void addGroupPermission(String groupID, String permission) {
        val group = this.getGroup(groupID);
        group.addPermission(permission);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
        this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getMySQLSettings().getGroupsTable()));
    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {
        val group = this.getGroup(groupID);
        group.removePermission(permission);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
        this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getMySQLSettings().getGroupsTable()));
    }

    @Override
    public void addGroupParent(String groupID, String parentID) {
        val group = this.getGroup(groupID);
        group.addInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
        this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getMySQLSettings().getGroupsTable()));
    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {
        val group = this.getGroup(groupID);
        group.removeInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        this.saveGroup(groupID);
        this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getMySQLSettings().getGroupsTable()));
    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {
        val group = this.getGroup(groupID);
        group.setPrefix(prefix);
        this.saveGroup(groupID);
        this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getMySQLSettings().getGroupsTable()));
    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {
        val group = this.getGroup(groupID);
        group.setSuffix(suffix);
        this.saveGroup(groupID);
        this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getMySQLSettings().getGroupsTable()));
    }

    @Synchronized
    @Override
    public void deleteGroup(String groupID) {
        val defaultGroupId = this.getDefaultGroup().getGroupID();
        if (groupID.equalsIgnoreCase(defaultGroupId)) return;
        this.groups.remove(groupID);
        for (val user : this.users.values()) {
            if (user.hasGroup(groupID)) {
                user.setGroup(defaultGroupId);
            }
        }
        for (val user : this.temporalUsersCache.asMap().values()) {
            if (user.hasGroup(groupID)) {
                user.setGroup(defaultGroupId);
            }
        }
        this.recalculateUsersPermissions();
        CompletableFuture.runAsync(()-> {
            try {
                val groupIDLC = groupID.toLowerCase();
                UpdateBuilder<UserData, String> updateUsersBuilder = this.userDataDao.updateBuilder();
                updateUsersBuilder.updateColumnValue(USER_GROUP_COLUMN, defaultGroupId);
                updateUsersBuilder.where().eq(USER_GROUP_COLUMN, groupIDLC);
                updateUsersBuilder.update();
                this.groupDataDao.deleteById(groupIDLC);

                UpdateBuilder<GroupData, String> updateGroupsBuilder = this.groupDataDao.updateBuilder();
                for (val group : this.groups.values()) {
                    if (group.hasGroup(groupID)) {
                        group.removeInheritanceGroup(groupID);
                        updateGroupsBuilder.updateColumnValue(GROUP_PARENTS_COLUMN, GroupData.from(group).getSerializedInheritanceGroups());
                        updateGroupsBuilder.where().eq(GROUP_ID_COLUMN, group.getGroupID());
                        updateGroupsBuilder.update();
                    }
                }
                this.broadcastPacket(MessageData.goDeleteGroupPacket(groupID, this.manager.getConfigFile().getMySQLSettings().getGroupsTable()));
            } catch (SQLException e) {
                throw new RuntimeException("Error while delete group " + groupID + " data", e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void createGroup(String groupID) {
        val newGroup = new Group(groupID);
        this.groups.put(groupID, newGroup);
        CompletableFuture.runAsync(()-> {
            try {
                this.groupDataDao.createOrUpdate(GroupData.from(newGroup));
                this.broadcastPacket(MessageData.goUpdateGroupPacket(newGroup, this.manager.getConfigFile().getMySQLSettings().getGroupsTable()));
            } catch (SQLException e) {
                throw new RuntimeException("Error while create group " + groupID + " data", e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public void updateGroup(String groupID, Group group) {
        this.groups.put(groupID, group);
        this.recalculateUsersPermissions();
    }

    @Override
    public Collection<User> getAllUsersData() {
        val list = new ArrayList<User>();
        try {
            val objectList = this.userDataDao.queryForAll();
            for (UserData userData : objectList) {
                list.add(userData.getUser());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while get all users data", e);
        }
        return list;
    }

    @Override
    public Collection<Group> getAllGroupsData() {
        val list = new ArrayList<Group>();
        try {
            val objectList = this.groupDataDao.queryForAll();
            for (GroupData groupData : objectList) {
                list.add(groupData.getGroup());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while get all groups data", e);
        }
        return list;
    }

    @Override
    public void importUsersData(Collection<User> users) {
        Collection<UserData> convertedUsers = new ArrayList<>();
        for (User user : users) {
            convertedUsers.add(UserData.from(user));
        }
        try {
            this.userDataDao.create(convertedUsers);
        } catch (SQLException e) {
            throw new RuntimeException("Error while import all users data", e);
        }
    }

    @Override
    public void importGroupsData(Collection<Group> groups) {
        Collection<GroupData> convertedGroups = new ArrayList<>();
        for (Group group : groups) {
            convertedGroups.add(GroupData.from(group));
        }
        try {
            this.groupDataDao.create(convertedGroups);
        } catch (SQLException e) {
            throw new RuntimeException("Error while import all groups data", e);
        }
    }

    @Override
    public void close() {
        this.userDataDao = null;
        this.groupDataDao = null;
        this.connectionSource.closeQuietly();
    }
}
