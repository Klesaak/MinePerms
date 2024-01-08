CREATE TABLE IF NOT EXISTS `mp_users_permissions` (
        i BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
        user_name VARCHAR(128) NOT NULL,
        permission VARCHAR(255) NOT NULL,
        -- Indexes
        UNIQUE (user_name, permission),
        INDEX (user_name),
        FOREIGN KEY (user_name) REFERENCES mp_users (user_name) ON UPDATE CASCADE ON DELETE CASCADE) ENGINE = INNODB;