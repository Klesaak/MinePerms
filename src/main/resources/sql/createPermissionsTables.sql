CREATE TABLE IF NOT EXISTS `mp_groups` (
        group_id VARCHAR(128) NOT NULL,
        prefix TEXT,
        suffix TEXT,
        --Indexes
        INDEX (group_id),
        PRIMARY KEY(group_id)) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS `mp_users` (
        user_name VARCHAR(16) NOT NULL,
        group_id VARCHAR(128),
        prefix TEXT,
        suffix TEXT,
        -- Indexes
        PRIMARY KEY(user_name),
        INDEX (group_id),
        INDEX (user_name),
        FOREIGN KEY (group_id) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE SET NULL) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS `mp_groups_permissions_%suffix%` (
        i BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
        group_id VARCHAR(128) NOT NULL,
        permission VARCHAR(255) NOT NULL,
        -- Indexes
        UNIQUE (group_id, permission),
        INDEX (group_id),
        FOREIGN KEY (group_id) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE CASCADE) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS `mp_groups_parents` (
        i BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
        group_id VARCHAR(128) NOT NULL,
        parent VARCHAR(128) NOT NULL,
        -- Indexes
        UNIQUE (group_id, parent),
        INDEX (group_id),
        INDEX (parent),
        FOREIGN KEY (parent) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE CASCADE,
        FOREIGN KEY (group_id) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE CASCADE) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS `mp_users_permissions` (
        i BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
        user_name VARCHAR(16) NOT NULL,
        permission VARCHAR(255) NOT NULL,
        -- Indexes
        UNIQUE (user_name, permission),
        INDEX (user_name),
        FOREIGN KEY (user_name) REFERENCES mp_users (user_name) ON UPDATE CASCADE ON DELETE CASCADE) ENGINE = INNODB;