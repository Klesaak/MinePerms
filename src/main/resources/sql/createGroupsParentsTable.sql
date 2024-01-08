CREATE TABLE IF NOT EXISTS `mp_groups_parents` (
        i BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
        group_id VARCHAR(128) NOT NULL,
        parent VARCHAR(255) NOT NULL,
        -- Indexes
        UNIQUE (group_id, parent),
        INDEX (group_id),
        INDEX (parent),
        FOREIGN KEY (parent) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE CASCADE,
        FOREIGN KEY (group_id) REFERENCES mp_groups (group_id) ON UPDATE CASCADE ON DELETE CASCADE) ENGINE = INNODB;