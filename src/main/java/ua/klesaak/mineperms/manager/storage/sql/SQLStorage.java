package ua.klesaak.mineperms.manager.storage.sql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static ua.klesaak.mineperms.manager.storage.sql.DatabaseConstants.*;

//todo момент с обновление в бд обьекта, которого там нет (как-то исправить)
@Getter
public class SQLStorage extends Storage {
    private final HikariDataSource hikariDataSource;

    public SQLStorage(MinePermsManager manager) {
        super(manager);
        val config = this.manager.getConfigFile().getSQLSettings();
        this.hikariDataSource = config.getSource(manager.getStorageType());
        applyGroupsPermissionsSuffix(config.getGroupsPermissionsTableSuffix());
        this.createTables();
    }

    @Override
    public void init() {
        CompletableFuture.runAsync(() -> {
            Collection<Group> groups = new ArrayList<>();
            Map<String, Collection<String>> groupsPerms = new HashMap<>();
            Map<String, Collection<String>> groupsParents = new HashMap<>();
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(GET_ALL_GROUPS_DATA_SQL)) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String groupId = rs.getString("group_id");
                        String prefix = rs.getString("prefix");
                        String suffix = rs.getString("suffix");
                        Group group = new Group(groupId);
                        if (prefix != null) group.setPrefix(prefix);
                        if (suffix != null) group.setSuffix(suffix);
                        groups.add(group);
                        groupsPerms.put(groupId, new ArrayList<>());
                        groupsParents.put(groupId, new ArrayList<>());
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error while load groups data from MySQL ", e);
            }

            //загружаем пермишены для групп
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(GET_ALL_GROUPS_PERMISSIONS_SQL)) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String groupId = rs.getString("group_id");
                        String permission = rs.getString("permission");
                        groupsPerms.get(groupId).add(permission);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error while load groups data from MySQL ", e);
            }

            //загружаем паренты
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(GET_ALL_GROUPS_PARENTS_SQL)) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String groupId = rs.getString("group_id");
                        String parent = rs.getString("parent");
                        groupsParents.get(groupId).add(parent);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error while load groups data from MySQL ", e);
            }

            //складываем всё в кучу
            groups.forEach(groupData -> {
                val id = groupData.getGroupID();
                val perms = groupsPerms.get(id);
                val parents = groupsParents.get(id);
                groupData.setPermissions(perms);
                groupData.setInheritanceGroups(parents);
                this.groups.put(id, groupData);
            });

            //проверяем наличие дефолт группы
            val defaultGroup = manager.getConfigFile().getDefaultGroup();
            if (this.getGroup(defaultGroup) == null) {
                this.createGroup(defaultGroup);
            }
        }).exceptionally(throwable -> {
          throw new RuntimeException("Error while init SQL storage", throwable);
        });
    }

    private void createTables() {
        this.executeSQL(CREATE_GROUPS_TABLE_SQL);
        this.executeSQL(CREATE_USERS_TABLE_SQL);
        this.executeSQL(CREATE_GROUPS_PERMISSIONS_TABLE_SQL);
        this.executeSQL(CREATE_GROUPS_PARENTS_TABLE_SQL);
        this.executeSQL(CREATE_USERS_PERMISSIONS_TABLE_SQL);
    }

    @Override
    public void cacheUser(String nickName) {
        val nickNameLC = nickName.toLowerCase();
        val tempUser = this.temporalUsersCache.getIfPresent(nickNameLC);
        User user = tempUser != null ? tempUser : this.getUser(nickNameLC);
        if (!this.manager.getConfigFile().isUseRedisPubSub()) { //загружаем игрока из бд при каждом заходе, чтобы была актуальность данных!
            user = this.loadUser(nickNameLC).join();
        }
        if (user != null) {
            user.recalculatePermissions(this.groups);
            this.users.put(nickNameLC, user);
            this.temporalUsersCache.invalidate(nickNameLC);
            return;
        }
        val newUser = new User(nickNameLC, this.manager.getConfigFile().getDefaultGroup());
        newUser.recalculatePermissions(this.groups);
        this.users.put(nickNameLC, newUser);
    }

    /**
     * При выходе игрока с сервера отгружаем его из основного кеша во временный
     * чтобы в случае быстрого перезахода игрока не тратить лишние ресурсы на его подгрузку из БД
     */
    @Override
    public void unCacheUser(String nickName) {
        val nickNameLC = nickName.toLowerCase();
        User user = this.users.remove(nickNameLC);
        this.temporalUsersCache.put(nickNameLC, user);
    }

    @Override
    public void saveUser(String nickName) {
        val nickNameLC = nickName.toLowerCase();
        val tempUser = this.temporalUsersCache.getIfPresent(nickNameLC);
        User user = tempUser != null ? tempUser : this.users.get(nickNameLC);
        if (user != null) {
            this.saveUser(nickNameLC, user);
        }
    }

    @Override
    public void saveUser(String nickName, User user) { //todo не забудь про batch perms
        /*CompletableFuture.runAsync(()-> {
            try {
                this.userDataDao.createOrUpdate(UserData.from(user));
            } catch (SQLException ex) {
                throw new RuntimeException("Error while save user data " + nickName, ex);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });*/
    }

    @Override
    public void saveGroup(String groupID) { //todo не забудь про batch perms
        val group = this.groups.get(groupID);
       /* CompletableFuture.runAsync(()-> {
            try {
                this.groupDataDao.createOrUpdate(GroupData.from(group));
            } catch (SQLException ex) {
                throw new RuntimeException("Error while save group data " + groupID, ex);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });*/
    }

    @Override
    public User getUser(String nickName) {
        val nickNameLC = nickName.toLowerCase();
        val tempUser = this.temporalUsersCache.getIfPresent(nickNameLC);
        User user = tempUser != null ? tempUser : this.users.get(nickNameLC);
        if (user != null) return user;
        user = this.loadUser(nickNameLC).join();
        if (user != null) {
            this.temporalUsersCache.put(nickNameLC, user);
            user.recalculatePermissions(this.groups);
        }
        return user;
    }

    private CompletableFuture<User> loadUser(String nickName) {
        return CompletableFuture.supplyAsync(() -> this.getUserFromSQL(nickName)).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
    }

    @Override
    public String getUserPrefix(String nickName) {
        User user = this.getUser(nickName.toLowerCase());
        if (user == null) return this.getDefaultGroup().getPrefix();
        return user.getPrefix().isEmpty() ? this.getGroupOrDefault(user.getGroupId()).getPrefix() : user.getPrefix();
    }

    @Override
    public String getUserSuffix(String nickName) {
        User user = this.getUser(nickName.toLowerCase());
        if (user == null) return this.getDefaultGroup().getSuffix();
        return user.getSuffix().isEmpty() ? this.getGroupOrDefault(user.getGroupId()).getSuffix() : user.getSuffix();
    }

    @Override
    public void addUserPermission(String nickName, String permission) {
        val config = this.manager.getConfigFile();
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user == null) {
            user = new User(nickNameLC, config.getDefaultGroup());
            this.temporalUsersCache.put(nickNameLC, user);
        }
        user.addPermission(permission);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(INSERT_USER_PERMISSION_SQL)) {
                statement.setString(1, nickNameLC);
                statement.setString(2, permission.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while add perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
        //this.broadcastPacket(MessageData.goUpdateUserPacket(user, config.getSQLSettings().getUsersTable()));
    }

    @Override
    public void removeUserPermission(String nickName, String permission) {
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user == null) return;
        user.removePermission(permission);
        user.recalculatePermissions(this.groups);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(REMOVE_USER_PERMISSION_SQL)) {
                statement.setString(1, nickNameLC);
                statement.setString(2, permission.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
        //this.broadcastPacket(MessageData.goUpdateUserPacket(user, this.manager.getConfigFile().getSQLSettings().getUsersTable()));
    }

    @Override
    public void setUserPrefix(String nickName, String prefix) {
        val nickNameLC = nickName.toLowerCase();
        val config = this.manager.getConfigFile();
        User user = this.getUser(nickNameLC);
        if (user == null) {
            user = new User(nickNameLC, config.getDefaultGroup());
            this.temporalUsersCache.put(nickNameLC, user);
        }
        user.setPrefix(prefix);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(UPDATE_USER_PREFIX_SQL)) {
                statement.setString(1, nickNameLC);
                statement.setString(2, prefix.isEmpty() ? null : prefix);
                statement.setString(3, prefix.isEmpty() ? null : prefix);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
        //this.broadcastPacket(MessageData.goUpdateUserPacket(user, config.getSQLSettings().getUsersTable()));
    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {
        val config = this.manager.getConfigFile();
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user == null) {
            user = new User(nickNameLC, config.getDefaultGroup());
            this.temporalUsersCache.put(nickNameLC, user);
        }
        user.setSuffix(suffix);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(UPDATE_USER_SUFFIX_SQL)) {
                statement.setString(1, nickNameLC);
                statement.setString(2, suffix.isEmpty() ? null : suffix);
                statement.setString(3, suffix.isEmpty() ? null : suffix);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
       // this.broadcastPacket(MessageData.goUpdateUserPacket(user, config.getSQLSettings().getUsersTable()));
    }

    @Override
    public void setUserGroup(String nickName, String groupID) {
        val config = this.manager.getConfigFile();
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user == null) {
            user = new User(nickNameLC, config.getDefaultGroup());
            this.temporalUsersCache.put(nickNameLC, user);
        }
        if (this.groups.get(groupID) != null) {
            user.setGroupId(groupID);
            user.recalculatePermissions(this.groups);
            this.manager.getEventManager().callGroupChangeEvent(user);
        }
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(UPDATE_USER_GROUP_SQL)) {
                statement.setString(1, nickNameLC);
                statement.setString(2, groupID);
                statement.setString(3, groupID);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
        //this.broadcastPacket(MessageData.goUpdateUserPacket(user, config.getSQLSettings().getUsersTable()));
    }

    @Override
    public void deleteUser(String nickName) {
        val nickNameLC = nickName.toLowerCase();
       val config = this.manager.getConfigFile();
        val newUser = new User(nickNameLC, config.getDefaultGroup());
        if (this.users.get(nickNameLC) != null) {
            this.users.put(nickNameLC, newUser);
        }
        if (this.temporalUsersCache.getIfPresent(nickNameLC) != null) {
            this.temporalUsersCache.put(nickNameLC, newUser);
        }
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(DELETE_USER_SQL)) {
                statement.setString(1, nickNameLC);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });

        //this.broadcastPacket(MessageData.goDeleteUserPacket(nickName, config.getSQLSettings().getUsersTable()));
    }

    @Override
    public void updateUser(String nickName, User user) {
        val nickNameLC = nickName.toLowerCase();
        if (this.temporalUsersCache.getIfPresent(nickNameLC) == null && this.users.get(nickNameLC) == null) return;
        user.recalculatePermissions(this.groups);
        if (this.temporalUsersCache.getIfPresent(nickNameLC) != null) {
            this.temporalUsersCache.put(nickNameLC, user);
            return;
        }
        this.users.put(nickNameLC, user);
    }

    @Override
    public void addGroupPermission(String groupID, String permission) {
        val group = this.getGroup(groupID);
        group.addPermission(permission);
        this.recalculateUsersPermissions();
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(INSERT_GROUP_PERMISSION_SQL)) {
                statement.setString(1, groupID);
                statement.setString(2, permission.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
       // this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getSQLSettings().getGroupsTable()));
    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {
        val group = this.getGroup(groupID);
        group.removePermission(permission);
        this.recalculateUsersPermissions();
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(REMOVE_GROUP_PERMISSION_SQL)) {
                statement.setString(1, groupID);
                statement.setString(2, permission.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
        //this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getSQLSettings().getGroupsTable()));
    }

    @Override
    public void addGroupParent(String groupID, String parentID) {
        val group = this.getGroup(groupID);
        group.addInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(INSERT_GROUP_PARENT_SQL)) {
                statement.setString(1, groupID);
                statement.setString(2, parentID.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
        //this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getSQLSettings().getGroupsTable()));
    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {
        val group = this.getGroup(groupID);
        group.removeInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(REMOVE_GROUP_PARENT_SQL)) {
                statement.setString(1, groupID);
                statement.setString(2, parentID.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
       // this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getSQLSettings().getGroupsTable()));
    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {
        val group = this.getGroup(groupID);
        group.setPrefix(prefix);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(UPDATE_GROUP_PREFIX_SQL)) {
                statement.setString(1, groupID);
                statement.setString(2, prefix.isEmpty() ? null : prefix);
                statement.setString(3, prefix.isEmpty() ? null : prefix);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
        //this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getSQLSettings().getGroupsTable()));
    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {
        val group = this.getGroup(groupID);
        group.setSuffix(suffix);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(UPDATE_GROUP_SUFFIX_SQL)) {
                statement.setString(1, groupID);
                statement.setString(2, suffix.isEmpty() ? null : suffix);
                statement.setString(3, suffix.isEmpty() ? null : suffix);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });
        //this.broadcastPacket(MessageData.goUpdateGroupPacket(group, this.manager.getConfigFile().getSQLSettings().getGroupsTable()));
    }

    @Synchronized
    @Override
    public void deleteGroup(String groupID) {
        //todo sql
      /*  val defaultGroupId = this.getDefaultGroup().getGroupID();
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
                this.broadcastPacket(MessageData.goDeleteGroupPacket(groupID, this.manager.getConfigFile().getSQLSettings().getGroupsTable()));
            } catch (SQLException e) {
                throw new RuntimeException("Error while delete group " + groupID + " data", e);
            }
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });*/
    }

    @Override
    public void createGroup(String groupId) {
        val newGroup = new Group(groupId);
        this.groups.put(groupId, newGroup);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(INSERT_GROUP_DEFAULT)) {
                statement.setString(1, groupId);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while create group ", e);
            }
            //this.broadcastPacket(MessageData.goUpdateGroupPacket(newGroup, this.manager.getConfigFile().getSQLSettings().getGroupsTable()));
        }).exceptionally(throwable -> {
            throw new RuntimeException(throwable);
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
        /*try {
            val objectList = this.userDataDao.queryForAll();
            for (UserData userData : objectList) {
                list.add(userData.getUser());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while get all users data", e);
        }*/
        return list;
    }

    @Override
    public Collection<Group> getAllGroupsData() {
        val list = new ArrayList<Group>();
       /* try {
            val objectList = this.groupDataDao.queryForAll();
            for (GroupData groupData : objectList) {
                list.add(groupData.getGroup());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while get all groups data", e);
        }*/
        return list;
    }

    @Override
    public void importUsersData(Collection<User> users) {
       /* Collection<UserData> convertedUsers = new ArrayList<>();
        for (User user : users) {
            convertedUsers.add(UserData.from(user));
        }
        try {
            this.userDataDao.create(convertedUsers);
        } catch (SQLException e) {
            throw new RuntimeException("Error while import all users data", e);
        }*/
    }

    @Override
    public void importGroupsData(Collection<Group> groups) {
        /*Collection<GroupData> convertedGroups = new ArrayList<>();
        for (Group group : groups) {
            convertedGroups.add(GroupData.from(group));
        }
        try {
            this.groupDataDao.create(convertedGroups);
        } catch (SQLException e) {
            throw new RuntimeException("Error while import all groups data", e);
        }*/
    }

    private void executeSQL(String sql) {
        try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error while executeSQL data", e);
        }
    }

    private User getUserFromSQL(String nickName) {
        User user = null;
        try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(GET_USER_DATA_SQL)) {
            statement.setString(1, nickName);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String group = rs.getString("group_id");
                    String prefix = rs.getString("prefix");
                    String suffix = rs.getString("suffix");
                    user = new User(nickName, group);
                    if (group == null) user.setGroupId(this.manager.getConfigFile().getDefaultGroup());
                    if (prefix != null) user.setPrefix(prefix);
                    if (suffix != null) user.setSuffix(suffix);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while load user data from MySQL " + nickName, e);
        }
        try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(GET_USER_PERMISSIONS_SQL)) {
            statement.setString(1, nickName);
            if (user != null) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        user.addPermission(rs.getString("permission"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while load user perms from MySQL " + nickName, e);
        }
        return user;
    }

    @Override
    public void close() {
        if (this.hikariDataSource != null) this.hikariDataSource.close();;
    }
}
