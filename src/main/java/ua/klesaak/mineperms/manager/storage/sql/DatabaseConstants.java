package ua.klesaak.mineperms.manager.storage.sql;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DatabaseConstants {

    ///SQL
    ///CREATE TABLES
    public final String CREATE_GROUPS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `mp_groups` (group_id VARCHAR(128) NOT NULL, " +
            "                                    prefix TEXT," +
            "                                    suffix TEXT," +
            "                                    PRIMARY KEY(group_id)) ENGINE = INNODB;";
    public final String CREATE_USERS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `mp_users` (user_name VARCHAR(128), " +
            "                                    group_id VARCHAR(128)," +
            "                                    prefix TEXT," +
            "                                    suffix TEXT," +
            "                                    PRIMARY KEY(user_name)," +
            "                                    INDEX (group_id)," +
            "                                    FOREIGN KEY (group_id) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE SET NULL) ENGINE = INNODB;";
    public String CREATE_GROUPS_PERMISSIONS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `mp_groups_permissions_%suffix%` (i BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            "                                    group_id VARCHAR(128) NOT NULL," +
            "                                    permission VARCHAR(255) NOT NULL," +
            "                                    UNIQUE (group_id, permission)," +
            "                                    INDEX (group_id)," +
            "                                    FOREIGN KEY (group_id) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE CASCADE) ENGINE = INNODB;";
    public final String CREATE_GROUPS_PARENTS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `mp_groups_parents` (i BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            "                                    group_id VARCHAR(128) NOT NULL," +
            "                                    parent VARCHAR(255) NOT NULL," +
            "                                    UNIQUE (group_id, parent)," +
            "                                    INDEX (group_id), INDEX (parent)," +
            "                                    FOREIGN KEY (parent) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE CASCADE," +
            "                                    FOREIGN KEY (group_id) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE CASCADE) ENGINE = INNODB;";
    public final String CREATE_USERS_PERMISSIONS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `mp_users_permissions` (i BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            "                                    user_name VARCHAR(128) NOT NULL," +
            "                                    permission VARCHAR(255) NOT NULL," +
            "                                    UNIQUE (user_name, permission)," +
            "                                    INDEX (user_name)," +
            "                                    FOREIGN KEY (user_name) REFERENCES mp_users (user_name) ON UPDATE CASCADE ON DELETE CASCADE) ENGINE = INNODB;";

    ///FETCHING
    ///GROUP
    public final String GET_GROUP_DATA_SQL = "SELECT prefix, suffix FROM `mp_groups` WHERE group_id = ?";
    public final String GET_GROUP_PARENTS_SQL = "SELECT parent FROM `mp_groups_parents` WHERE group_id = ?";
    public String GET_GROUP_PERMISSIONS_SQL = "SELECT permission FROM `mp_groups_permissions_%suffix%` WHERE group_id = ?";
    public final String GET_ALL_GROUPS_DATA_SQL = "SELECT group_id, prefix, suffix FROM `mp_groups`";
    public final String GET_ALL_GROUPS_PARENTS_SQL = "SELECT group_id, parent FROM `mp_groups_parents`";
    public String GET_ALL_GROUPS_PERMISSIONS_SQL = "SELECT group_id, permission FROM `mp_groups_permissions_%suffix%`";
    ///USERS
    public final String GET_USER_DATA_SQL = "SELECT group_id, prefix, suffix FROM `mp_users` WHERE user_name = ?";
    public final String GET_USER_PERMISSIONS_SQL = "SELECT permission FROM `mp_users_permissions` WHERE user_name = ?";
    public final String GET_ALL_USERS_DATA_SQL = "SELECT user_name, group_id, prefix, suffix FROM `mp_users`";
    public final String GET_ALL_USERS_PERMISSIONS_SQL = "SELECT user_name, permission FROM `mp_users_permissions`";

    ///UPDATING
    ///USERS
    public final String UPDATE_USER_GROUP_SQL = "INSERT INTO `mp_users` (user_name, group_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE group_id = ?";
    public final String UPDATE_USER_PREFIX_SQL = "INSERT INTO `mp_users` (user_name, prefix) VALUES (?, ?) ON DUPLICATE KEY UPDATE prefix = ?";
    public final String UPDATE_USER_SUFFIX_SQL = "INSERT INTO `mp_users` (user_name, suffix) VALUES (?, ?) ON DUPLICATE KEY UPDATE suffix = ?";
    public final String INSERT_USER_PERMISSION_SQL = "INSERT IGNORE INTO `mp_users_permissions` (user_name, permission) VALUES (?, ?)";
    public final String REMOVE_USER_PERMISSION_SQL = "DELETE FROM `mp_users_permissions` WHERE user_name = ? AND permission = ? LIMIT 1";
    ///GROUP
    public final String INSERT_GROUP_DEFAULT = "INSERT INTO `mp_groups` (group_id) VALUES(?) ON DUPLICATE KEY UPDATE group_id=group_id";
    public final String UPDATE_GROUP_PREFIX_SQL = "INSERT INTO `mp_groups` (group_id, prefix) VALUES (?, ?) ON DUPLICATE KEY UPDATE prefix = ?";
    public final String UPDATE_GROUP_SUFFIX_SQL = "INSERT INTO `mp_groups` (group_id, suffix) VALUES (?, ?) ON DUPLICATE KEY UPDATE suffix = ?";
    public String INSERT_GROUP_PERMISSION_SQL = "INSERT IGNORE INTO `mp_groups_permissions_%suffix%` (group_id, permission) VALUES (?, ?)";
    public String REMOVE_GROUP_PERMISSION_SQL = "DELETE FROM `mp_groups_permissions_%suffix%` WHERE group_id = ? AND permission = ? LIMIT 1";
    public final String INSERT_GROUP_PARENT_SQL = "INSERT IGNORE INTO `mp_groups_parents` (group_id, parent) VALUES (?, ?)";
    public final String REMOVE_GROUP_PARENT_SQL = "DELETE FROM `mp_groups_parents` WHERE group_id = ? AND parent = ? LIMIT 1";

    ///DELETE
    public final String DELETE_USER_SQL = "DELETE FROM `mp_users` WHERE user_name = ?";
    public final String DELETE_GROUP_SQL = "DELETE FROM `mp_groups` WHERE group_id = ?";

    public void applyGroupsPermissionsSuffix(String tableSuffix) {
        CREATE_GROUPS_PERMISSIONS_TABLE_SQL = CREATE_GROUPS_PERMISSIONS_TABLE_SQL.replace("%suffix%", tableSuffix);
        GET_GROUP_PERMISSIONS_SQL = GET_GROUP_PERMISSIONS_SQL.replace("%suffix%", tableSuffix);
        INSERT_GROUP_PERMISSION_SQL = INSERT_GROUP_PERMISSION_SQL.replace("%suffix%", tableSuffix);
        REMOVE_GROUP_PERMISSION_SQL = REMOVE_GROUP_PERMISSION_SQL.replace("%suffix%", tableSuffix);
        GET_ALL_GROUPS_PERMISSIONS_SQL = GET_ALL_GROUPS_PERMISSIONS_SQL.replace("%suffix%", tableSuffix);
    }
}
