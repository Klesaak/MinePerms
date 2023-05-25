package ua.klesaak.mineperms.manager.storage.mysql;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public class MySQLStorage extends Storage {
    private final JdbcPooledConnectionSource connectionSource;
    private Dao<User, String> userDataDao;
    private Dao<Group, String> groupDataDao;

    public MySQLStorage(MinePermsManager manager) {
        super(manager);
        val config = this.manager.getConfigFile().getMySQLConfig();
        try {
            this.connectionSource = new JdbcPooledConnectionSource(config.getHost());
        } catch (SQLException ex) {
            throw new RuntimeException("Error while init MySQL", ex);
        }
        this.createUsersTable(config);
        this.createGroupsTable(config);
        this.connectionSource.setTestBeforeGet(true);
    }

    private void createUsersTable(MySQLConfig config) {
        try {
            List<DatabaseFieldConfig> fieldConfigs = new ArrayList<>();
            DatabaseFieldConfig playerName = new DatabaseFieldConfig("playerName");
            playerName.setId(true);
            playerName.setCanBeNull(false);
            playerName.setColumnName("player_name");
            fieldConfigs.add(playerName);

            DatabaseFieldConfig groupField = new DatabaseFieldConfig("group");
            groupField.setCanBeNull(false);
            groupField.setDataType(DataType.STRING);
            groupField.setDefaultValue(this.manager.getConfigFile().getDefaultGroup());
            groupField.setColumnName("group");
            fieldConfigs.add(groupField);

            DatabaseFieldConfig prefixField = new DatabaseFieldConfig("prefix");
            prefixField.setCanBeNull(false);
            prefixField.setDataType(DataType.STRING);
            prefixField.setDefaultValue("");
            prefixField.setColumnName("prefix");
            fieldConfigs.add(prefixField);

            DatabaseFieldConfig suffixField = new DatabaseFieldConfig("suffix");
            suffixField.setCanBeNull(false);
            suffixField.setDataType(DataType.STRING);
            suffixField.setDefaultValue("");
            suffixField.setColumnName("suffix");
            fieldConfigs.add(suffixField);

            DatabaseFieldConfig permsField = new DatabaseFieldConfig("jsonPerms");
            permsField.setCanBeNull(false);
            permsField.setDataType(DataType.LONG_STRING);
            permsField.setColumnName("permissions");
            fieldConfigs.add(permsField);

            DatabaseTableConfig<User> usersTableConfig = new DatabaseTableConfig<>(User.class, config.getUsersTable(), fieldConfigs);
            this.userDataDao = DaoManager.createDao(this.connectionSource, usersTableConfig);
            TableUtils.createTableIfNotExists(this.connectionSource, usersTableConfig);
        } catch (SQLException ex) {
            throw new RuntimeException("Error while create users table", ex);
        }
    }



    private void createGroupsTable(MySQLConfig config) {
        try {
            List<DatabaseFieldConfig> fieldConfigs = new ArrayList<>();
            DatabaseFieldConfig groupID = new DatabaseFieldConfig("groupID");
            groupID.setId(true);
            groupID.setCanBeNull(false);
            groupID.setColumnName("group_id");
            fieldConfigs.add(groupID);

            DatabaseFieldConfig prefixField = new DatabaseFieldConfig("prefix");
            prefixField.setCanBeNull(false);
            prefixField.setDataType(DataType.STRING);
            prefixField.setDefaultValue("");
            prefixField.setColumnName("prefix");
            fieldConfigs.add(prefixField);

            DatabaseFieldConfig suffixField = new DatabaseFieldConfig("suffix");
            suffixField.setCanBeNull(false);
            suffixField.setDataType(DataType.STRING);
            suffixField.setDefaultValue("");
            suffixField.setColumnName("suffix");
            fieldConfigs.add(suffixField);

            DatabaseFieldConfig parentField = new DatabaseFieldConfig("jsonInheritanceGroups");
            parentField.setCanBeNull(false);
            parentField.setDataType(DataType.LONG_STRING);
            parentField.setColumnName("parent_groups");
            fieldConfigs.add(parentField);

            DatabaseFieldConfig permsField = new DatabaseFieldConfig("jsonPerms");
            permsField.setCanBeNull(false);
            permsField.setDataType(DataType.LONG_STRING);
            permsField.setColumnName("permissions");
            fieldConfigs.add(permsField);

            DatabaseTableConfig<Group> groupsTableConfig = new DatabaseTableConfig<>(Group.class, config.getGroupsTable(), fieldConfigs);
            this.groupDataDao = DaoManager.createDao(this.connectionSource, groupsTableConfig);
            TableUtils.createTableIfNotExists(this.connectionSource, groupsTableConfig);
        } catch (SQLException ex) {
            throw new RuntimeException("Error while create groups table", ex);
        }
    }

    @Override
    public void cacheUser(String nickName) {
        User user = this.temporalUsersCache.getIfPresent(nickName);
        if (user != null) {
            this.users.put(nickName, user);
            this.temporalUsersCache.invalidate(nickName);
        }
        //todo query
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

    }

    @Override
    public void saveUser(String nickName, User user) {
        CompletableFuture.runAsync(()-> {
            try {
                user.setJsonPerms(JsonData.GSON.toJson(user.getPermissions()));
                this.userDataDao.createOrUpdate(user);
                user.setJsonPerms(null);
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

    }

    @Override
    public User getUser(String nickName) {
        return null;
    }

    @Override
    public String getUserPrefix(String nickName) {
        return null;
    }

    @Override
    public String getUserSuffix(String nickName) {
        return null;
    }

    @Override
    public void addUserPermission(String nickName, String permission) {

    }

    @Override
    public void removeUserPermission(String nickName, String permission) {

    }

    @Override
    public void setUserPrefix(String nickName, String prefix) {

    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {

    }

    @Override
    public void setUserGroup(String nickName, String groupID) {

    }

    @Override
    public void deleteUser(String nickName) {

    }

    @Override
    public void updateUser(String nickName, User user) {

    }

    @Override
    public void addGroupPermission(String groupID, String permission) {

    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {

    }

    @Override
    public void addGroupParent(String groupID, String parentID) {

    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {

    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {

    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {

    }

    @Override
    public void deleteGroup(String groupID) {

    }

    @Override
    public void createGroup(String groupID) {

    }

    @Override
    public void updateGroup(String groupID, Group group) {

    }

    @Override
    public Collection<User> getAllUsersData() {
        return null;
    }

    @Override
    public Collection<Group> getAllGroupsData() {
        return null;
    }

    @Override
    public void close() {

    }
}
