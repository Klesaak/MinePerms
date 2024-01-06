CREATE TABLE IF NOT EXISTS `mp_users` (user_name VARCHAR(128) NOT NULL,
        group_id VARCHAR(128),
        prefix TEXT,
        suffix TEXT,
        PRIMARY KEY(user_name),
        INDEX (group_id),
        FOREIGN KEY (group_id) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE SET NULL) ENGINE = INNODB;