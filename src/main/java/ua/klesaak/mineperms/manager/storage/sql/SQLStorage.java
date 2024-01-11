package ua.klesaak.mineperms.manager.storage.sql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.log.MPLogger;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.StorageType;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.entity.User;
import ua.klesaak.mineperms.manager.storage.redismessenger.MessageData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class SQLStorage extends Storage {
    //create tables
    private final String createPermissionsTablesSql;
    //fetching
    private final String getAllGroupsDataSql, getAllGroupsParentsSql, getAllGroupsPermissionsSql, getGroupDataSql, getGroupParentsSql, getGroupPermissionsSql;
    private final String getAllUsersDataSql, getAllUsersPermissionsSql, getUserDataSql, getUserPermissionsSql;
    //update
    private final String insertGroupDefaultSql, insertGroupParentSql, insertGroupPermissionSql, removeGroupParentSql, removeGroupPermissionSql, updateGroupPrefixSql, updateGroupSuffixSql;
    private final String insertUserDefaultSql, insertUserPermissionSql, removeUserPermissionSql, updateUserGroupSql, updateUserPrefixSql, updateUserSuffixSql;
    //delete
    private final String deleteGroupSql;
    private final String deleteUserSql;

    private final String importUsersDataSql, importGroupsDataSql;

    private final HikariDataSource hikariDataSource;

    public SQLStorage(MinePermsManager manager, StorageType storageType) {
        super(manager);
        val config = this.manager.getConfigFile().getSQLSettings();
        this.hikariDataSource = config.getSource(storageType);
        this.createPermissionsTablesSql = this.loadSQL("createPermissionsTables").replace("%suffix%", config.getGroupsPermissionsTableSuffix());
        //fetching
        this.getAllGroupsDataSql = this.loadSQL("fetch/group/getAllGroupsData");
        this.getAllGroupsParentsSql = this.loadSQL("fetch/group/getAllGroupsParents");
        this.getAllGroupsPermissionsSql = this.loadSQL("fetch/group/getAllGroupsPermissions").replace("%suffix%", config.getGroupsPermissionsTableSuffix());
        this.getGroupDataSql = this.loadSQL("fetch/group/getGroupData");
        this.getGroupParentsSql = this.loadSQL("fetch/group/getGroupParents");
        this.getGroupPermissionsSql = this.loadSQL("fetch/group/getGroupPermissions").replace("%suffix%", config.getGroupsPermissionsTableSuffix());

        this.getAllUsersDataSql = this.loadSQL("fetch/user/getAllUsersData");
        this.getAllUsersPermissionsSql = this.loadSQL("fetch/user/getAllUsersPermissions");
        this.getUserDataSql = this.loadSQL("fetch/user/getUserData");
        this.getUserPermissionsSql = this.loadSQL("fetch/user/getUserPermissions");
        //updating
        this.insertGroupDefaultSql = this.loadSQL("update/group/insertGroupDefault");
        this.insertGroupParentSql = this.loadSQL("update/group/insertGroupParent");
        this.insertGroupPermissionSql = this.loadSQL("update/group/insertGroupPermission").replace("%suffix%", config.getGroupsPermissionsTableSuffix());
        this.removeGroupParentSql = this.loadSQL("update/group/removeGroupParent");
        this.removeGroupPermissionSql = this.loadSQL("update/group/removeGroupPermission").replace("%suffix%", config.getGroupsPermissionsTableSuffix());
        this.updateGroupPrefixSql = this.loadSQL("update/group/updateGroupPrefix");
        this.updateGroupSuffixSql = this.loadSQL("update/group/updateGroupSuffix");
        this.insertUserDefaultSql = this.loadSQL("update/user/insertUserDefault");
        this.insertUserPermissionSql = this.loadSQL("update/user/insertUserPermission");
        this.removeUserPermissionSql = this.loadSQL("update/user/removeUserPermission");
        this.updateUserGroupSql = this.loadSQL("update/user/updateUserGroup");
        this.updateUserPrefixSql = this.loadSQL("update/user/updateUserPrefix");
        this.updateUserSuffixSql = this.loadSQL("update/user/updateUserSuffix");
        this.deleteGroupSql = this.loadSQL("update/deleteGroup");
        this.deleteUserSql = this.loadSQL("update/deleteUser");
        this.importUsersDataSql = this.loadSQL("import/importUsersData");
        this.importGroupsDataSql = this.loadSQL("import/importGroupsData");
        this.executeSQL(this.createPermissionsTablesSql);//creating Tables
    }

    @Override
    public void init() {
        CompletableFuture.runAsync(() -> {
            this.getAllGroupsData().forEach(group -> this.groups.put(group.getGroupID(), group));
            //проверяем наличие дефолт группы
            val defaultGroup = manager.getConfigFile().getDefaultGroup();
            if (this.getGroup(defaultGroup) == null) {
                this.createGroup(defaultGroup);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
    }

    @Override
    public void cacheUser(String nickName) {
        val nickNameLC = nickName.toLowerCase();
        User user = this.temporalUsersCache.getIfPresent(nickNameLC);
        if (user != null && this.manager.getConfigFile().isUseRedisPubSub()) { //загружаем игрока из бд при каждом заходе, чтобы была актуальность данных!
            user.recalculatePermissions(this.groups);
            this.users.put(nickNameLC, user);
            this.temporalUsersCache.invalidate(nickNameLC);
            return;
        }
        user = this.loadUser(nickNameLC).join();
        if (user == null) {
            user = new User(nickNameLC, this.manager.getConfigFile().getDefaultGroup());
        }
        user.recalculatePermissions(this.groups);
        this.users.put(nickNameLC, user);
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
        //ничего не делаем
    }

    @Override
    public void saveUser(String nickName, User user) {
        //ничего не делаем
    }

    @Override
    public void saveGroup(String groupID) {
        //ничего не делаем
    }

    private CompletableFuture<User> loadUser(String nickName) {
        val nickNameLC = nickName.toLowerCase();
        return CompletableFuture.supplyAsync(() -> {
            User user = this.loadUserData(nickNameLC);
            if (user == null) return null;
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.getUserPermissionsSql)) {
                statement.setString(1, nickNameLC);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        user.addPermission(rs.getString("permission"));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error while load user perms from sql " + nickNameLC, e);
            }
            return user;
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
    }

    private User loadUserData(String nickName) {
        User user = null;
        try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.getUserDataSql)) {
            statement.setString(1, nickName);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String group = rs.getString("group_id");
                    String prefix = rs.getString("prefix");
                    String suffix = rs.getString("suffix");
                    user = new User(nickName, group);
                    if (group == null) user.setGroupId(this.manager.getConfigFile().getDefaultGroup());
                    user.setPrefix(prefix);
                    user.setSuffix(suffix);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while load user data from sql " + nickName, e);
        }
        return user;
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
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.insertUserDefaultSql)) {
                statement.setString(1, nickNameLC);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while add perm data", e);
            }
        }).thenRunAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.insertUserPermissionSql)) {
                statement.setString(1, nickNameLC);
                statement.setString(2, permission.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while add perm data", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goUpdateUserPermAddPacket(nickNameLC, permission));
    }

    @Override
    public void removeUserPermission(String nickName, String permission) {
        val nickNameLC = nickName.toLowerCase();
        User user = this.getUser(nickNameLC);
        if (user == null) return;
        user.removePermission(permission);
        user.recalculatePermissions(this.groups);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.removeUserPermissionSql)) {
                statement.setString(1, nickNameLC);
                statement.setString(2, permission.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove perm data", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goUpdateUserPermRemovePacket(nickNameLC, permission));
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
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.updateUserPrefixSql)) {
                statement.setString(1, nickNameLC);
                statement.setString(2, prefix.isEmpty() ? null : prefix);
                statement.setString(3, prefix.isEmpty() ? null : prefix);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while set user prefix", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goUpdateUserPrefixPacket(nickNameLC, prefix));
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
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.updateUserSuffixSql)) {
                statement.setString(1, nickNameLC);
                statement.setString(2, suffix.isEmpty() ? null : suffix);
                statement.setString(3, suffix.isEmpty() ? null : suffix);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while set user suffix", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goUpdateUserSuffixPacket(nickNameLC, suffix));
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
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.updateUserGroupSql)) {
                statement.setString(1, nickNameLC);
                statement.setString(2, groupID);
                statement.setString(3, groupID);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while set user group", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goUpdateUserGroupPacket(nickNameLC, groupID));
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
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.deleteUserSql)) {
                statement.setString(1, nickNameLC);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while delete user", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goDeleteUserPacket(nickNameLC));
    }

    @Override
    public void addGroupPermission(String groupID, String permission) {
        val group = this.getGroup(groupID);
        group.addPermission(permission);
        this.recalculateUsersPermissions();
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.insertGroupPermissionSql)) {
                statement.setString(1, groupID);
                statement.setString(2, permission.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while add group permission", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
       this.broadcastPacket(MessageData.goUpdateGroupPermAddPacket(groupID, permission, this.manager.getConfigFile().getSQLSettings().getGroupsPermissionsTableSuffix()));
    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {
        val group = this.getGroup(groupID);
        group.removePermission(permission);
        this.recalculateUsersPermissions();
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.removeGroupPermissionSql)) {
                statement.setString(1, groupID);
                statement.setString(2, permission.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove group permission", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goUpdateGroupPermRemovePacket(groupID, permission, this.manager.getConfigFile().getSQLSettings().getGroupsPermissionsTableSuffix()));
    }

    @Override
    public void addGroupParent(String groupID, String parentID) {
        val group = this.getGroup(groupID);
        group.addInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.insertGroupParentSql)) {
                statement.setString(1, groupID);
                statement.setString(2, parentID.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while add group parent", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goUpdateGroupParentAddPacket(groupID, parentID));
    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {
        val group = this.getGroup(groupID);
        group.removeInheritanceGroup(parentID);
        this.recalculateUsersPermissions();
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.removeGroupParentSql)) {
                statement.setString(1, groupID);
                statement.setString(2, parentID.toLowerCase());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while remove group parent", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goUpdateGroupParentRemovePacket(groupID, parentID));
    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {
        val group = this.getGroup(groupID);
        group.setPrefix(prefix);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.updateGroupPrefixSql)) {
                statement.setString(1, groupID);
                statement.setString(2, prefix.isEmpty() ? null : prefix);
                statement.setString(3, prefix.isEmpty() ? null : prefix);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while set group prefix", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goUpdateGroupPrefixPacket(groupID, prefix));
    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {
        val group = this.getGroup(groupID);
        group.setSuffix(suffix);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.updateGroupSuffixSql)) {
                statement.setString(1, groupID);
                statement.setString(2, suffix.isEmpty() ? null : suffix);
                statement.setString(3, suffix.isEmpty() ? null : suffix);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while set group suffix", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goUpdateGroupSuffixPacket(groupID, suffix));
    }

    @Synchronized
    @Override
    public void deleteGroup(String groupID) {
        val defaultGroupId = this.getDefaultGroup().getGroupID();
        if (groupID.equalsIgnoreCase(defaultGroupId)) return;
        this.groups.remove(groupID);
        Stream.concat(this.users.values().stream(), this.temporalUsersCache.asMap().values().stream())
                .filter(user -> user.hasGroup(groupID)).forEach(user -> {
                    user.setGroupId(defaultGroupId);
                    this.manager.getEventManager().callGroupChangeEvent(user);
                });
        this.groups.values().stream().filter(group -> group.hasGroup(groupID)).forEach(group -> group.removeInheritanceGroup(groupID));
        this.recalculateUsersPermissions();
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.deleteGroupSql)) {
                statement.setString(1, groupID);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while delete group " + groupID + " data", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goDeleteGroupPacket(groupID));
    }

    @Override
    public void createGroup(String groupId) {
        val newGroup = new Group(groupId);
        this.groups.put(groupId, newGroup);
        CompletableFuture.runAsync(()-> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.insertGroupDefaultSql)) {
                statement.setString(1, groupId);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error while create group ", e);
            }
        }).exceptionally(throwable -> {
            MPLogger.logError(throwable);
            return null;
        });
        this.broadcastPacket(MessageData.goCreteGroupPacket(groupId));
    }

    @Override
    public Collection<User> getAllUsersData() {
        val list = new ArrayList<User>();
        Map<String, Collection<String>> usersPerms = new HashMap<>();
        try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.getAllUsersDataSql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String userName = rs.getString("user_name");
                    String groupId = rs.getString("group_id");
                    String prefix = rs.getString("prefix");
                    String suffix = rs.getString("suffix");
                    String group = groupId == null ? this.manager.getConfigFile().getDefaultGroup() : groupId;
                    User user = new User(userName, group);
                    user.setPrefix(prefix);
                    user.setSuffix(suffix);
                    list.add(user);
                    usersPerms.put(userName, new ArrayList<>());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while load users data from SQL ", e);
        }

        //загружаем пермишены для юзеров
        try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.getAllUsersPermissionsSql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String groupId = rs.getString("user_name");
                    String permission = rs.getString("permission");
                    usersPerms.get(groupId).add(permission);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while load users data from SQL ", e);
        }

        //складываем всё в кучу
        list.forEach(usersData -> {
            val id = usersData.getPlayerName();
            val perms = usersPerms.get(id);
            usersData.setPermissions(perms);
        });
        return list;
    }

    @Override
    public Collection<Group> getAllGroupsData() {
        val list = new ArrayList<Group>();
        Map<String, Collection<String>> groupsPerms = new HashMap<>();
        Map<String, Collection<String>> groupsParents = new HashMap<>();
        try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.getAllGroupsDataSql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String groupId = rs.getString("group_id");
                    String prefix = rs.getString("prefix");
                    String suffix = rs.getString("suffix");
                    Group group = new Group(groupId);
                    group.setPrefix(prefix);
                    group.setSuffix(suffix);
                    list.add(group);
                    groupsPerms.put(groupId, new ArrayList<>());
                    groupsParents.put(groupId, new ArrayList<>());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while load groups data from SQL ", e);
        }

        //загружаем пермишены для групп
        try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.getAllGroupsPermissionsSql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String groupId = rs.getString("group_id");
                    String permission = rs.getString("permission");
                    groupsPerms.get(groupId).add(permission);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while load groups data from SQL ", e);
        }

        //загружаем паренты
        try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(this.getAllGroupsParentsSql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String groupId = rs.getString("group_id");
                    String parent = rs.getString("parent");
                    groupsParents.get(groupId).add(parent);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while load groups data from SQL ", e);
        }

        //складываем всё в кучу
        list.forEach(groupData -> {
            val id = groupData.getGroupID();
            val perms = groupsPerms.get(id);
            val parents = groupsParents.get(id);
            groupData.setPermissions(perms);
            groupData.setInheritanceGroups(parents);
        });
        return list;
    }

    @Override
    public void importUsersData(Collection<User> users) {
        try (val con = this.hikariDataSource.getConnection()) {
            con.setAutoCommit(false);
            try (val insertUsersDataStatement = con.prepareStatement(this.importUsersDataSql);
                 val insertUsersPermissionsStatement = con.prepareStatement(this.insertUserPermissionSql)) {
                for (val user : users) {
                    val prefix = user.getPrefix().isEmpty() ? null : user.getPrefix();
                    val suffix = user.getSuffix().isEmpty() ? null : user.getSuffix();
                    val groupId = user.getGroupId();
                    insertUsersDataStatement.setString(1, user.getPlayerName().toLowerCase());
                    insertUsersDataStatement.setString(2, groupId);
                    insertUsersDataStatement.setString(3, prefix);
                    insertUsersDataStatement.setString(4, suffix);
                    insertUsersDataStatement.addBatch();
                }
                insertUsersDataStatement.executeBatch();
                for (val user : users) {
                    for (String permission : user.getPermissions()) {
                        insertUsersPermissionsStatement.setString(1, user.getPlayerName().toLowerCase());
                        insertUsersPermissionsStatement.setString(2, permission);
                        insertUsersPermissionsStatement.addBatch();
                    }
                }
                insertUsersPermissionsStatement.executeBatch();
            } catch (SQLException e) {
                con.rollback();
                con.setAutoCommit(true);
                throw e;
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error while importing users data", e);
        }
    }

    @Override
    public void importGroupsData(Collection<Group> groups) { // это делаем первым, чтобы смогли импортироваться юзеры
        try (val con = this.hikariDataSource.getConnection()) {
            con.setAutoCommit(false);
            try (val insertGroupsDataStatement = con.prepareStatement(this.importGroupsDataSql);
                 val insertGroupsParentsStatement = con.prepareStatement(this.insertGroupParentSql);
                 val insertGroupsPermissionsStatement = con.prepareStatement(this.insertGroupPermissionSql)) {
                for (val group : groups) {
                    val prefix = group.getPrefix().isEmpty() ? null : group.getPrefix();
                    val suffix = group.getSuffix().isEmpty() ? null : group.getSuffix();
                    insertGroupsDataStatement.setString(1, group.getGroupID());
                    insertGroupsDataStatement.setString(2, prefix);
                    insertGroupsDataStatement.setString(3, suffix);
                    insertGroupsDataStatement.addBatch();
                }
                insertGroupsDataStatement.executeBatch();
                for (val group : groups) {
                    for (String parent : group.getInheritanceGroups()) {
                        insertGroupsParentsStatement.setString(1, group.getGroupID());
                        insertGroupsParentsStatement.setString(2, parent);
                        insertGroupsParentsStatement.addBatch();
                    }
                }
                insertGroupsParentsStatement.executeBatch();
                for (val group : groups) {
                    for (String permission : group.getPermissions()) {
                        insertGroupsPermissionsStatement.setString(1, group.getGroupID());
                        insertGroupsPermissionsStatement.setString(2, permission);
                        insertGroupsPermissionsStatement.addBatch();
                    }
                }
                insertGroupsPermissionsStatement.executeBatch();

            } catch (SQLException e) {
                con.rollback();
                con.setAutoCommit(true);
                throw e;
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error while importing groups data", e);
        }
    }

    private void executeSQL(String sql) {
        Collection<String> sqlList = new ArrayList<>(16);
        if (!sql.contains(";")) throw new IllegalArgumentException("Missed ';' in sql line: '" + sql + "'");
        sqlList.addAll(Arrays.asList(sql.split(";")));
        sqlList.forEach(sqlLine -> {
            try (val con = this.hikariDataSource.getConnection(); val statement = con.prepareStatement(sqlLine)) {
                statement.execute();
            } catch (SQLException e) {
                MPLogger.logError(new RuntimeException("Error while executeSQL data", e));
            }
        });
    }

    private String loadSQL(String name) {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sql/" + name + ".sql")) {
            if (inputStream != null) {
                val bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                return bufferedReader.lines().filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("--")).collect(Collectors.joining(" "));
            }
        } catch (IOException e) {
            MPLogger.logError(new RuntimeException("Error while load SQL file!"));
        }
        return "";
    }

    @Override
    public void close() {
        if (this.hikariDataSource != null) this.hikariDataSource.close();
    }
}
