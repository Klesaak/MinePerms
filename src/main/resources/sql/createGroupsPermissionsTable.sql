CREATE TABLE IF NOT EXISTS `mp_groups_permissions_%suffix%` (i BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
        group_id VARCHAR(128) NOT NULL,
        permission VARCHAR(255) NOT NULL,
        UNIQUE (group_id, permission),
        INDEX (group_id),
        FOREIGN KEY (group_id) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE CASCADE) ENGINE = INNODB;